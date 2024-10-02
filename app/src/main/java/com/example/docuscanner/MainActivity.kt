package com.example.docuscanner

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.docuscanner.ui.theme.DocuScannerTheme
import com.example.docuscanner.utils.DocuScannerInit
import com.google.android.datatransport.BuildConfig
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.FileOutputStream
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocuScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val documentScannerInitializer = GmsDocumentScanning.getClient(
                        GmsDocumentScannerOptions.Builder()
                            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                            .setPageLimit(50)
                            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                            .build()
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterVertically
                        )
                    ) {
                        val context = LocalContext.current.applicationContext
                        var imageUris by remember {
                            mutableStateOf<List<Uri>?>(
                                emptyList()
                            )
                        }
                        var pdfPath by remember {
                            mutableStateOf<Uri?>( null)
                        }
                        val scannerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                        ) { activityResult ->
                            Log.d("imageUris", " rememberLauncherForActivityResult")
                            if (activityResult.resultCode == RESULT_OK) {
                                val result = GmsDocumentScanningResult.fromActivityResultIntent(
                                    activityResult.data
                                )
                                Log.d("imageUris", " RESULT_OK == $result")
                                pdfPath = result?.pdf?.uri
                                imageUris = result?.pages?.map { it.imageUri } ?: emptyList()
                                result?.pdf?.let { scannedPdf ->
                                    val fos =
                                        FileOutputStream(File(context.filesDir, "scanned_pdf.pdf"))
                                    context.contentResolver.openInputStream(scannedPdf.uri)?.use {
                                        it.copyTo(fos)
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context.applicationContext,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        Box(modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp)
                            .background(Color.Magenta, CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                            ) {
                                Log.d("imageUris", " clicked")
                                documentScannerInitializer
                                    .getStartScanIntent(
                                        this@MainActivity
                                    )
                                    .addOnSuccessListener {
                                        Log.d("imageUris", " launched")
                                        scannerLauncher.launch(
                                            IntentSenderRequest
                                                .Builder(it)
                                                .build()
                                        )
                                    }
                                    .addOnFailureListener {
                                        Toast
                                            .makeText(
                                                context,
                                                "Couldn't scan try again",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                text = "SCAN PDF", style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        LaunchedEffect(imageUris) {
                            Log.d("imageUris", " imageUris == $imageUris")
                        }

                        AnimatedVisibility(
                            visible = !imageUris.isNullOrEmpty()
                        ) {
                            Column(
                                modifier = Modifier.scrollable(
                                    rememberScrollState(),
                                    Orientation.Vertical
                                )
                            ) {
                                imageUris?.forEach { uriCouldBeNull ->
                                    AsyncImage(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .clip(RoundedCornerShape(20.dp)),
                                        model = uriCouldBeNull,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(
                            visible = pdfPath != null
                        ) {
                            Column(
                                modifier = Modifier.scrollable(
                                    rememberScrollState(),
                                    Orientation.Vertical
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
                            ) {
                                Text(
                                    "saved to ${pdfPath.toString()}",
                                    modifier = Modifier.clickable {
                                        pdfPath?.let { pdfPath ->
                                            val pdfUri: Uri = Uri.parse(pdfPath.toString())
                                            // Create intent to open the PDF
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(pdfUri, "application/pdf")
                                                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }

                                            // Check if there is an app to handle the intent
                                            if (intent.resolveActivity(packageManager) != null) {
                                                startActivity(intent)
                                            } else {
                                                // Handle the case where no PDF viewer is available
                                                Toast.makeText(this@MainActivity, "Please install a PDF viewer.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
fun Context.openFileInExplorer(
    filePath: String
) {
    // File path (from your internal cache directory)
    val file = File(filePath)

    // Create URI with FileProvider
    val uri: Uri = FileProvider.getUriForFile(
        this,
        "${BuildConfig.APPLICATION_ID}.provider",
        file
    )

    // Create intent to view file in file explorer
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "*/*") // Set to * / * to allow any file manager to pick it up
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Verify if there is an app that can handle the intent
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        // Handle the case where no file explorer is available
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
    DocuScannerTheme {
        Greeting("Android")
    }
}