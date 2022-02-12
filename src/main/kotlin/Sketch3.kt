import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.fx.edges.Contour
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.osc.OSC
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import java.net.InetAddress
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

fun main() = application {
    configure {
        width = (1920 * 0.7).toInt()
        height = (1080 * 0.7).toInt()
    }

    //clean this up with something like https://github.com/stavshamir/kotlin-decorators
//        val osc = OSC() //default port 57110
    var osc = OSC(InetAddress.getByName("localhost"), portIn = 10000, portOut = 12000)
    println(osc.portIn)
    println(osc.address)
    var tx = 0.0
    var ty = 0.0
    osc.listen("/tx") { addr: String, args: List<Any> -> tx = args[0] as Double }
    osc.listen("/ty") { addr: String, args: List<Any> -> ty = args[0] as Double }
    // TODO: OSC seems to behave weirdly when defined inside an olive program? Check limits of this

    oliveProgram {

        var contour1 = Rectangle(100.0, 100.0, 500.0, 500.0).contour
        var contour2 = Circle(400.0, 400.0, 250.0).contour
        var numCircles = 600
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
//                    curve2 = randCurve()
                    ::a.animate(0.2, 800, Easing.QuadInOut, 2000)
                    ::a.complete()
//                    this.delay(2000)
                    ::a.animate(0.0,800, Easing.QuadInOut, 2000)
                    ::a.complete()
                }
            }
        }


        extend {
            var t = program.seconds * 0.3 * PI
            var t1 = program.seconds * 0.02
            lerpAnimation.update()
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            drawer.circles {
                for (i in 1..numCircles) {
                    var circlePhase = i.toDouble()/numCircles
                    var pt1 = contour1.position((t1+circlePhase) % 1.0)
                    var pt2 = curve2.position((t1+circlePhase) % 1.0)
//                    var pt3 = pt1.mix(pt2, lerpAnimation.a)
                    var pt3 = pt1.mix(pt2, ty)
                    circle(pt3, 3.0 + sinN(-t+circlePhase* 2 * PI).pow(6.0)*10)
                }
            }
        }
        extend(FPSDisplay())
    }
}
