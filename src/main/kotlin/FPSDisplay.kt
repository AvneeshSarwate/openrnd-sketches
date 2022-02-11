import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.math.Matrix44

class FPSDisplay : Extension {
    override var enabled: Boolean = true

    var frames = 0
    var startTime: Double = 0.0
    var secTime: Double = 0.1
    var lastTime: Double = 0.0
    var font =  loadFont("data/fonts/default.otf", 24.0)
    val calcWindow = 20

    override fun setup(program: Program) {
        startTime = program.seconds
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        frames++
        if(frames % calcWindow == 0) {
            secTime = program.seconds - lastTime
            lastTime = program.seconds
        }
        drawer.isolated {
            // -- set view and projections
            drawer.view = Matrix44.IDENTITY
            drawer.stroke = ColorRGBa.GREEN
            drawer.strokeWeight = 10.0
            drawer.fontMap = font
            drawer.fill = ColorRGBa.GREEN
            drawer.ortho()
            drawer.text("fps: ${"%.2f".format(calcWindow/secTime)}", 50.0, 50.0)
            drawer.text("sec: ${"%.2f".format(program.seconds)}", 50.0, 100.0)
        }
    }
}