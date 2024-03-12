import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curiouscreature.kotlin.math.Float3


@Composable
fun World(mesh: Mesh = cube, drawVertices: MutableState<Boolean>, camerax: MutableState<Float>) {
    val camera = Camera(
        position = Float3(20f, 110.1f, 110.0f),
        target = Float3(0.1f, camerax.value, 1.0f)
    )
    Text("Camera: ${camera.target.y}")
        val animatedProgress by rememberInfiniteTransition().animateFloat(
        initialValue = 0.01f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
        ),
    )

    Canvas(Modifier.size(50.dp, 50.dp)) {
        mesh.rotation = Float3(animatedProgress + 90, animatedProgress + 180, 0.01f)
        camera.target.y += 0.1f
        camera.target.x += 0.1f
        camera.target.z -= 0.1f
        val lines = render3d(camera, mesh)
        lines.forEach { (one, two, three) ->
            if (drawVertices.value) {
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(one.x, one.y))
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(two.x, two.y))
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(three.x, three.y))
            }
            drawLine(
                color = Color.Red,
                start = Offset(one.x, one.y),
                end = Offset(two.x, two.y)
            )
            drawLine(
                color = Color.Red,
                start = Offset(two.x, two.y),
                end = Offset(three.x, three.y)
            )
            drawLine(
                color = Color.Red,
                start = Offset(three.x, three.y),
                end = Offset(one.x, one.y)
            )
        }
    }
}