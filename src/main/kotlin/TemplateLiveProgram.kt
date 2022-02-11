import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.draw.*
import org.openrndr.extra.noise.uniform

fun main(args: Array<String>) {
    application {

        configure {
            width = (1920 * 0.7).toInt()
            height = (1080 * 0.7).toInt()
        }

        oliveProgram {
            class AnimatedCircle : Animatable() {
                var x = Math.random() * program.width //bug when set to Math.random() * program.width or random(0.0, program.width.toDouble())
                var y = 0.0 //bug when set to Math.random() * program.height or random(0.0, program.height.toDouble())
                var radius = 8.0
                var latch = 0.0

                fun shrink() {
                    // -- first stop any running animations for the radius property
                    ::radius.cancel()
                    ::radius.animate(3.0, 50, Easing.CubicInOut)
                }

                fun grow() {
                    ::radius.cancel()
                    ::radius.animate(Double.uniform(5.0, 8.0), 800, Easing.CubicInOut)
                }

                fun jump() {
                    ::x.cancel()
                    ::y.cancel()
                    ::x.animate(Double.uniform(0.0, width.toDouble()), 3700, Easing.CubicInOut)
                    ::y.animate(Double.uniform(0.0, height.toDouble()), 4900, Easing.CubicInOut)
                }

                fun update() {
                    updateAnimation()
                    if (!::latch.hasAnimations) {
                        val duration = Double.uniform(100.0, 700.0).toLong()
                        ::latch.animate(1.0, duration).completed.listen {
                            val action = listOf(::shrink, ::grow, ::jump).random()
                            action()
                        }
                    }
                }
            }

            class FeedbackTarget(width: Int, height: Int) {
                var backbuffer = renderTarget(width, height) { colorBuffer() }
                var target = renderTarget(width, height) { colorBuffer() }
                fun swapBuffers() { backbuffer = target.also { target = backbuffer } }
            }

            var fdbkTarget = FeedbackTarget(program.width, program.height)
            var brushTarget = renderTarget(width, height) { colorBuffer() }

            fun bindTrailShader(brush: ColorBuffer, backbuffer: ColorBuffer): ShadeStyle {
                return shadeStyle {
                    fragmentTransform = """
                    vec2 texCoord = c_boundsPosition.xy;
//                    texCoord.y *= 2.;
                    vec2 bbN = vec2(texCoord.x, 1.-texCoord.y);
                    vec4 bb = texture(p_backbuffer, bbN);
                    vec4 brush = texture(p_brush, texCoord);
                    float decay = 0.01;
                    float lastFdbk = bb.a;
                    float drawBrush = brush.r > 0 ? 1 : 0; 
                    vec4 debugCol = brush.r > 0 ? vec4(1,0,0,1) : vec4(0,1,0,1);
                    vec4 fdbkCol = lastFdbk > 0.1 ? vec4(bb.rgb, lastFdbk-decay) : vec4(0, 0, 0, 1);
                    debugCol = vec4(texCoord, 0, 1);
                    debugCol = texCoord.y < 0.5 ? vec4(1,0,0,1) : vec4(0,1,0,1);
                    vec4 outCol = mix(fdbkCol, brush, drawBrush);
                    outCol = mix(brush, bb, 0.98);
                    x_fill = outCol;
                    """.trimIndent()
                    parameter("brush", brush)
                    parameter("backbuffer", backbuffer)
                }
            }

            val numCircles = 10
            val animatedCircles = List(numCircles) { AnimatedCircle() }

            extend {
                fdbkTarget.swapBuffers()

                drawer.isolatedWithTarget(brushTarget) {
                    drawer.clear(ColorRGBa.BLACK)
                    drawer.fill = ColorRGBa.PINK
                    drawer.stroke = null
                    animatedCircles.forEach { ac -> ac.update() }
                    drawer.circles {
//                        fill = ColorRGBa.PINK.shade(Math.random())
                        animatedCircles.forEach { ac -> circle(ac.x, ac.y, ac.radius) }
                    }
                }

                drawer.isolatedWithTarget(fdbkTarget.target) {
                    drawer.clear(ColorRGBa.BLACK)
                    // can change filtering via - rt.colorBuffer(0).filter( [...] )
                    drawer.shadeStyle = bindTrailShader(brushTarget.colorBuffer(0), fdbkTarget.backbuffer.colorBuffer(0))
                    drawer.rectangle(0.0, 0.0, program.width.toDouble(), program.height.toDouble())
                }

                drawer.image(fdbkTarget.target.colorBuffer(0))
            }
            extend(FPSDisplay())
        }
    }
}
