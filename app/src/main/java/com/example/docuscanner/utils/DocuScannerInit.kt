package com.example.docuscanner.utils

import android.app.Activity.RESULT_OK
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.docuscanner.MainActivity
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream

@Composable
fun DocuScannerInit(modifier: Modifier = Modifier) {
    val documentScannerInitializer = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setPageLimit(500)
        .setResultFormats(RESULT_FORMAT_PDF)
        .build()
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        val context = LocalContext.current
        var imageUris by remember {
            mutableStateOf<List<Uri>?>(
                emptyList()
            )
        }
        val scannerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(
                    activityResult.data
                )
                imageUris = result?.pages?.map { it.imageUri } ?: emptyList()
                result?.pdf?.let { scannedPdf ->
                    val fos = FileOutputStream(File(context.filesDir, "scanned_pdf.pdf"))
                    context.contentResolver.openInputStream(scannedPdf.uri)?.use {
                        it.copyTo(fos)
                    }
                }
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
                documentScannerInitializer.getStartScanIntent(
                    context as MainActivity
                ).addOnSuccessListener {
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Couldn't scan try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = "SCAN PDF", color = Color.White
            )
        }

        AnimatedVisibility(
            visible = !imageUris.isNullOrEmpty()
        ) {
            imageUris?.forEach { uriCouldBeNull ->
                uriCouldBeNull?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmapForCurrentURi = BitmapFactory.decodeStream(inputStream)
                    Image(
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(RoundedCornerShape(20.dp)),
                        bitmap = bitmapForCurrentURi.asImageBitmap(),
                        contentDescription = null
                    )
                }
            }
        }
    }
}