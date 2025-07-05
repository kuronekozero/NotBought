package com.example.mysavings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DonateScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val wallets = remember {
        listOf(
            "Bitcoin" to "bc1qk96twh35ep7rmaz7al4ees0estpe3qzgpg7mgd",
            "Ethereum" to "0x55AdC4dAd18f15128cfa0CBc476D70F46208797F",
            "Solana" to "AACk437jRR29eNoYeiG4J7DELAfEgMKwLmyeNhhiiov3",
            "USDT (ERC20)" to "AACk437jRR29eNoYeiG4J7DELAfEgMKwLmyeNhhiiov3",
            "USDT (TRC20)" to "TUwfBT8MrWBxT5XTp7EtG2zjr2rGMCWS1x",
            "TON" to "UQAPpJrLuGO2YhR9MH_R3h-TMmO4b4NLwe_tUamitz7G78Sf"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.donate_thanks_header),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        wallets.forEach { (name, address) ->
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = address,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable {
                            clipboard.setText(androidx.compose.ui.text.AnnotatedString(address))
                            Toast.makeText(context, context.getString(R.string.donate_copied), Toast.LENGTH_SHORT).show()
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 