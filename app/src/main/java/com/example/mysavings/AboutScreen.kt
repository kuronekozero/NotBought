package com.example.mysavings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kuronekozero/msme"))
            context.startActivity(intent)
        }) {
            Text(stringResource(R.string.about_source_code))
        }

        val version = try {
            val pm = context.packageManager
            val pInfo = pm.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) { "1.0" }
        Text(stringResource(R.string.about_version, version), style = MaterialTheme.typography.bodyMedium)

        Text(stringResource(R.string.about_contact_header), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(
            text = "prometheus.tim123@gmail.com",
            modifier = Modifier.clickable {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:prometheus.tim123@gmail.com"))
                context.startActivity(emailIntent)
            },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 