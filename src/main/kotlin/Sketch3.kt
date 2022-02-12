import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.fx.edges.Contour
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = (1920 * 0.7).toInt()
        height = (1080 * 0.7).toInt()
    }

    oliveProgram {
        var contour1 = Rectangle(100.0, 100.0, 500.0, 500.0).contour
        var contour2 = Circle(400.0, 400.0, 250.0).contour
        var numCircles = 300
        fun sinN(n: Double): Double = (sin(n)+1)/2
        fun randVec(): Vector2 = Vector2.uniform(Rectangle(0.0, 0.0, width.toDouble(), height.toDouble()))
        fun randVec2(): Vector2 = Vector2.uniform(Vector2(0.0, 0.0), Vector2(500.0, 500.0))
        fun randVec3(): Vector2 = Vector2(random(0.0, width.toDouble()), random(0.0, height.toDouble()))
        var numCurvePts = 20

        // is there a way to do centripital catmull rom? - https://github.com/AvneeshSarwate/SplineLibrary
        fun randCurve(): ShapeContour {
            return contour {
                var lastPoint = randVec()
                moveTo(lastPoint)
                for(i in 1..numCurvePts) {
                    val nextPoint = randVec()
                    continueTo(lastPoint.mix(nextPoint, 0.5), nextPoint)
                    lastPoint = nextPoint
                }
                close()
            }
        }

        var curve1 = randCurve()
        var curve2 = randCurve()

        var lerpAnimation = object : Animatable() {
            var a = 0.0

            fun update(){
                updateAnimation()
                if(!::a.hasAnimations) {
                    ::a.animate(1.0, 800, Easing.QuadInOut, 2000)
                    ::a.complete()
//                    this.delay(2000)
                    ::a.animate(0.0,800, Easing.QuadInOut, 2000)
                    ::a.complete()
                }
            }
        }


        extend {
            var t = program.seconds * 0.2
            var t1 = program.seconds * 0.02
            lerpAnimation.update()
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.circles {
                for (i in 1..numCircles) {
                    var pt1 = curve1.position((t1+i.toDouble()/numCircles) % 1.0)
                    var pt2 = curve2.position((t1+i.toDouble()/numCircles) % 1.0)
                    var pt3 = pt1.mix(pt2, lerpAnimation.a)
                    circle(pt3, 3.0)
                }
            }
        }
        extend(FPSDisplay())
    }
}
