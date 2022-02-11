import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.*
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extras.color.presets.MEDIUM_AQUAMARINE
import org.openrndr.extras.color.presets.TOMATO
import kotlin.math.cos
import kotlin.math.sin

fun main(args: Array<String>) {
    application {

        configure {
            width = (1920 * 1.9).toInt()
            height = (1080 * 1.9).toInt()
        }

        oliveProgram {
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
                    vec2 bbN = vec2(texCoord.x, 1.-texCoord.y);
                    bbN = mix(bbN, vec2(0.5), 0.001);
                    vec4 bb = texture(p_backbuffer, bbN);
                    vec4 brush = texture(p_brush, texCoord);
                    float decay = 0.02;
                    float lastFdbk = bb.a;
                    float drawBrush = brush.r > 0 ? 1 : 0; 
                    vec4 fdbkCol = lastFdbk > 0.1 ? vec4(bb.rgb, lastFdbk-decay) : vec4(0, 0, 0, 1);
                    vec4 outCol = mix(fdbkCol, brush, drawBrush);
                    x_fill = outCol;
                    """.trimIndent()
                    parameter("brush", brush)
                    parameter("backbuffer", backbuffer)
                    parameter("time", program.seconds)
                }
            }

            fun sinN(n: Double): Double = (sin(n)+1)/2
            fun cosN(n: Double): Double = (cos(n)+1)/2
            fun mix(v1: Double, v2: Double, a: Double): Double = (1-a)*v1 + a*v2

            val numCircles = 10000
            var elapsed = 0.0;
            extend {
                elapsed += program.deltaTime *  mix(1.0, sinN(program.seconds*0.21), 0.0)
                fdbkTarget.swapBuffers()
                var w = program.width
                var h = program.height
                var t = program.seconds
                var e = elapsed + 500

                drawer.isolatedWithTarget(brushTarget) {
                    drawer.clear(ColorRGBa.BLACK)
                    drawer.fill = ColorRGBa.PINK
                    drawer.stroke = ColorRGBa.TRANSPARENT
                    drawer.circles {
                        for (i in 1..numCircles) {
//                            ColorRGBa.in
                            fill = mix(ColorRGBa.TOMATO, ColorRGBa.MEDIUM_AQUAMARINE, sinN(t+i/3000)).opacify(sinN(t+i/300))
                            var speed = (1.0 + (i*sinN(t*0.3)*3)/numCircles.toDouble())*0.025
                            circle(sinN(e*speed+i)*w, cosN(e+i)*h, 2.0*sinN(i/500.0)*10)
                        }
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
