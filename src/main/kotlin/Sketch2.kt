import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.draw.*
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extras.color.presets.CRIMSON
import org.openrndr.extras.color.presets.MAROON
import org.openrndr.extras.color.presets.MEDIUM_AQUAMARINE

fun main(args: Array<String>) {
    application {

        configure {
            width = (1920 * 1.9).toInt()
            height = (1080 * 1.9).toInt()
        }

        oliveProgram {
            var funcs = """
                float sinN(float n) { return (sin(n)+1)/2; }
                float cosN(float n) { return (cos(n)+1)/2; }
            """.trimIndent()
            var shader1 =  {color1: ColorRGBa, color2: ColorRGBa, time: Double ->
                shadeStyle {
                    fragmentPreamble = funcs
                    fragmentTransform = """
                        vec2 uvN = c_boundsPosition.xy;
                        float radDist = distance(uvN, vec2(0.5));
                        vec4 outCol = mix(p_color1, p_color2, radDist*3);
                        outCol.a = pow(1.-radDist*2, 2-sinN(p_time+uvN.x*3)*1.8);
                        x_fill = outCol;
                    """.trimIndent()
                    parameter("color1", color1)
                    parameter("color2", color2)
                    parameter("time", time)
                }
            }

            var numCircles = 10

            /*  Create several of these radial circles, positioned randomly.
                Select a random 1-3 colors, and generate other colors based on palette generation functions.
                Animate the circles to move to the position of the "next" circle and cycle.
             */

            extend {
                var sec = program.seconds
                drawer.shadeStyle = shader1(ColorRGBa.CRIMSON, ColorRGBa.BLUE, sec)
                drawer.circle(500.0, 500.0, 500.0)

                drawer.shadeStyle = shader1(ColorRGBa.MEDIUM_AQUAMARINE, ColorRGBa.MAROON, sec*0.81+5)
                drawer.circle(1500.0, 1500.0, 500.0)
            }
            extend(FPSDisplay())
        }
    }
}
