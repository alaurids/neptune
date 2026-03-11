package com.example.projectneptune

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.projectneptune.ui.theme.ProjectNeptuneTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            ProjectNeptuneTheme {
                ProjectNeptuneApp(cameraExecutor)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@PreviewScreenSizes
@Composable
fun ProjectNeptuneApp(cameraExecutor: ExecutorService? = null) {
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
            AppDestinations.CAMERA -> CameraDestination(cameraExecutor!!)
            AppDestinations.CATCH_LOG -> CatchLogDestination()
            AppDestinations.MAP -> MapDestination()
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
    MAP("Map", Icons.Default.Map),
    REFERENCE_GUIDE("Reference", R.drawable.reference_icon),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
fun CameraDestination(
    cameraExecutor: ExecutorService,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreview(cameraExecutor, modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use this feature.")
        }
    }
}

@Composable
fun CameraPreview(
    cameraExecutor: ExecutorService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder()
            .build()
    }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    var capturedFile by remember { mutableStateOf<File?>(null) }
    var isCropping by remember { mutableStateOf(false) }

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = CameraXPreview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (capturedFile == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            
            Button(
                onClick = {
                    takePhoto(context, imageCapture, cameraExecutor) { file ->
                        capturedFile = file
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text("Capture")
            }
        } else if (isCropping) {
            CropScreen(
                file = capturedFile!!,
                onCropDone = { croppedFile ->
                    capturedFile = croppedFile
                    isCropping = false
                },
                onCancel = { isCropping = false }
            )
        } else {
            PhotoReviewScreen(
                file = capturedFile!!,
                onCrop = { isCropping = true },
                onSave = {
                    savePhotoToGallery(context, capturedFile!!)
                    capturedFile = null
                },
                onDiscard = {
                    capturedFile?.delete()
                    capturedFile = null
                }
            )
        }
    }
}

@Composable
fun CropScreen(
    file: File,
    onCropDone: (File) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(file) { rotateBitmapIfRequired(file) } ?: return
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val displayWidth = constraints.maxWidth.toFloat()
        val displayHeight = constraints.maxHeight.toFloat()

        val scale = minOf(displayWidth / bitmap.width, displayHeight / bitmap.height)
        val viewWidth = bitmap.width * scale
        val viewHeight = bitmap.height * scale

        var cropRect by remember(viewWidth, viewHeight) {
            val size = minOf(viewWidth, viewHeight) * 0.8f
            mutableStateOf(Rect(Offset((viewWidth - size) / 2, (viewHeight - size) / 2), Size(size, size)))
        }

        Box(
            modifier = Modifier
                .size(
                    width = with(density) { viewWidth.toDp() },
                    height = with(density) { viewHeight.toDp() }
                )
                .align(Alignment.Center)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(viewWidth, viewHeight) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            val handleSize = 40.dp.toPx()
                            val touchPoint = change.position
                            
                            val isBottomRight = (touchPoint.x - cropRect.right).let { it * it } + (touchPoint.y - cropRect.bottom).let { it * it } < handleSize * handleSize
                            
                            if (isBottomRight) {
                                val newWidth = (cropRect.width + dragAmount.x).coerceIn(50f, viewWidth - cropRect.left)
                                val newHeight = (cropRect.height + dragAmount.y).coerceIn(50f, viewHeight - cropRect.top)
                                cropRect = Rect(cropRect.topLeft, Size(newWidth, newHeight))
                            } else {
                                val newTopLeft = Offset(
                                    (cropRect.left + dragAmount.x).coerceIn(0f, viewWidth - cropRect.width),
                                    (cropRect.top + dragAmount.y).coerceIn(0f, viewHeight - cropRect.height)
                                )
                                cropRect = Rect(newTopLeft, cropRect.size)
                            }
                        }
                    }
            ) {
                drawRect(
                    color = Color.White,
                    topLeft = cropRect.topLeft,
                    size = cropRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Draw a handle at the bottom right corner
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = cropRect.bottomRight
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cancel")
            }
            Button(onClick = {
                val croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    (cropRect.left / scale).toInt(),
                    (cropRect.top / scale).toInt(),
                    (cropRect.width / scale).toInt(),
                    (cropRect.height / scale).toInt()
                )
                val croppedFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                FileOutputStream(croppedFile).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                onCropDone(croppedFile)
            }) {
                Text("Confirm Crop")
            }
        }
    }
}

@Composable
fun PhotoReviewScreen(
    file: File,
    onCrop: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    val bitmap = remember(file) {
        rotateBitmapIfRequired(file)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onDiscard,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Discard")
            }
            Button(
                onClick = onCrop,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Crop")
            }
            Button(onClick = onSave) {
                Text("Save")
            }
        }
    }
}

private fun rotateBitmapIfRequired(file: File): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
    val exif = try {
        ExifInterface(file.absolutePath)
    } catch (e: Exception) {
        return bitmap
    }
    
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }
    
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onPhotoCaptured: (File) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    
    // Save to cache directory first for review
    val photoFile = File(context.cacheDir, "$name.jpg")

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(photoFile)
        .build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraPreview", "Photo capture failed: ${exc.message}", exc)
                ContextCompat.getMainExecutor(context).execute {
                    Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraPreview", "Photo capture succeeded: ${photoFile.absolutePath}")
                ContextCompat.getMainExecutor(context).execute {
                    onPhotoCaptured(photoFile)
                }
            }
        }
    )
}

private fun savePhotoToGallery(context: Context, photoFile: File) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )

    if (uri != null) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(photoFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
            photoFile.delete()
        } catch (e: Exception) {
            Log.e("CameraPreview", "Failed to save photo", e)
            Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CatchLogDestination(
    modifier: Modifier = Modifier
){
    Text("Catch Log", modifier = modifier)
}

@Composable
fun MapDestination(
    modifier: Modifier = Modifier
){
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Map View (Coming Soon)")
    }
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
    @StringRes scientificName: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(drawable),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(64.dp)
                        // Apply clip to the image with RoundedCornerShape for rounded edges
                        .clip(RoundedCornerShape(8.dp)),
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = stringResource(label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(scientificName),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationState)
                )
            }
            if (expanded) {
                Text(
                    text = "Detailed information about ${stringResource(label)} goes here. " +
                            "This section provides more context and characteristics of the species.",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ReferenceCardPreview() {
    ProjectNeptuneTheme {
        ReferenceCard(R.mipmap.manilla_clam_foreground, R.string.manilla_clam, R.string.manilla_clam_scientific)
    }
}

@Composable
fun ReferenceGrid(
    modifier: Modifier = Modifier
)
{
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(10) {
            ReferenceCard(
                R.mipmap.manilla_clam_foreground,
                R.string.manilla_clam,
                R.string.manilla_clam_scientific
            )
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
fun ReferenceGridPreview() {
    ProjectNeptuneTheme {
        ReferenceGrid()
    }
}
