import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = (1920 * 0.7).toInt()
        height = (1080 * 0.7).toInt()
    }

    oliveProgram {

        extend {

            drawer.fill = ColorRGBa.PINK
            drawer.circle(cos(seconds) * width / 2.0 + width / 2.0, sin(0.5 * seconds) * height / 2.0 + height / 2.0, 140.0)
        }
    }
}
