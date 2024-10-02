package com.example.docuscanner.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.datatransport.BuildConfig
import java.io.File

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

fun Context.openPdfOrDisplayWarning(pdfPath: Uri) {
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
        Toast.makeText(this, "Please install a PDF viewer.", Toast.LENGTH_LONG).show()
    }
}
