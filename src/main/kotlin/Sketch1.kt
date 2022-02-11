import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.draw.*
import org.openrndr.extra.olive.oliveProgram

fun main(args: Array<String>) {
    application {

        configure {
            width = (1920 * 1.9).toInt()
            height = (1080 * 1.9).toInt()
        }

        program {

            class AnimatedPosition(x: Double, y: Double ) : Animatable() {
                var x = x
                var y = y
                var radius = 8.0

                fun square() {
                    ::x.cancel()
                    ::y.cancel()
                    ::x.animate(program.width.toDouble() - radius, 1000, Easing.CubicInOut)
                    ::x.complete()
                    ::y.animate(program.height.toDouble() - radius, 1000, Easing.CubicInOut)
                    ::y.complete()
                    ::x.animate(0.0 + radius, 1000, Easing.CubicInOut)
                    ::x.complete()
                    ::y.animate(0.0 + radius, 1000, Easing.CubicInOut)
                    ::y.complete()
                }
            }

            class AnimatedRadius() : Animatable() {
                var radius = 10.0

                fun radiusChange() {
                    ::radius.cancel()
                    ::radius.animate(50.0, 2000, Easing.CubicInOut)
                    ::radius.complete()
                    ::radius.animate(10.0, 2000, Easing.CubicInOut)
                    ::radius.complete()
                }
            }

            class PosRad(x: Double, y: Double) {
                var pos = AnimatedPosition(x, y)
                var rad = AnimatedRadius()

                fun update() {
                    pos.updateAnimation()
                    rad.updateAnimation()
                    if(!pos.hasAnimations()) {
                        pos.delay(1000)
                        pos.square()
                    }
                    if(!rad.hasAnimations()) {
                        rad.delay(1000)
                        rad.radiusChange()
                    }
                }

                val x: Double
                    get() = this.pos.x
                val y: Double
                    get() = this.pos.y
                val radius
                    get() = this.rad.radius
            }

            val numCircles = 1
            val animatedCircles = List(numCircles) { PosRad(0.0, 0.0) }

            extend {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.PINK
                drawer.stroke = null
                animatedCircles.forEach { ac -> ac.update() }
                drawer.circles {
                    animatedCircles.forEach { ac -> circle(ac.x, ac.y, ac.radius) }
                }
            }
            extend(FPSDisplay())
        }
    }
}
