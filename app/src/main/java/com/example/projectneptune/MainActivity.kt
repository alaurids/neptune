package com.example.projectneptune

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.projectneptune.ui.theme.ProjectNeptuneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectNeptuneTheme {
                ProjectNeptuneApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ProjectNeptuneApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.REFERENCE_GUIDE) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        when (val icon = it.icon) {
                            is ImageVector -> {
                                Icon(
                                    icon,
                                    contentDescription = it.label
                                )
                            }
                            is Int -> {
                                Icon(
                                    painterResource(icon),
                                    contentDescription = it.label
                                )
                            }
                        }
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.CAMERA -> CameraDestination()
            AppDestinations.CATCH_LOG -> CatchLogDestination()
            AppDestinations.REFERENCE_GUIDE -> ReferenceGuideDestination()
            AppDestinations.SETTINGS -> SettingsDestination()
        }
    }
}

// Lists of destinations of applications
enum class AppDestinations(
    val label: String,
    val icon: Any,
) {
    CAMERA("Camera", Icons.Default.CameraAlt),
    CATCH_LOG("Catch Log", Icons.Default.AutoStories),
    REFERENCE_GUIDE("Reference", R.drawable.reference_icon),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
fun CameraDestination(
    modifier: Modifier = Modifier
){
    Text("Camera", modifier = modifier)
}

@Composable
fun CatchLogDestination(
    modifier: Modifier = Modifier
){
    Text("Catch Log", modifier = modifier)
}

@Composable
fun ReferenceGuideDestination(
    modifier: Modifier = Modifier
){
    ReferenceGrid(modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    )
}

@Composable
fun SettingsDestination(
    modifier: Modifier = Modifier
){
    Text("Settings", modifier = modifier)
}

@Composable
fun ReferenceCard(
    @DrawableRes drawable: Int,
    @StringRes label: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .size(128.dp)
                    // Apply clip to the image with RoundedCornerShape for rounded edges
                    .clip(RoundedCornerShape(16.dp)),
            )
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun ReferenceGrid(
    modifier: Modifier = Modifier
)
{
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(10) {
            ReferenceCard(R.mipmap.manilla_clam_foreground, R.string.manilla_clam)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProjectNeptuneTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun ReferenceCardPreview() {
    ProjectNeptuneTheme {
        ReferenceCard(R.mipmap.manilla_clam_foreground, R.string.manilla_clam)
    }
}

@Preview(showBackground = true)
@Composable
fun ReferenceGridPreview() {
    ProjectNeptuneTheme {
        ReferenceGrid()
    }
}
