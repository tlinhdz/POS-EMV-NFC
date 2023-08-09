package com.bill.emvnfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import com.bill.emvnfc.emv.Emv
import com.bill.emvnfc.emv.Store
import com.bill.emvnfc.ui.theme.EmvNfcTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var adapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val intentFilter = arrayOf(
        IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
    )
    private val techList = arrayOf(
        arrayOf(IsoDep::class.java.name)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmvNfcTheme {
                MainScene()
            }
        }

        adapter = NfcAdapter.getDefaultAdapter(this)

        lifecycleScope.launch(Dispatchers.Default) {
            Store.isEnableNfc.collect {
                lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
                    if (it) {
                        pendingIntent = PendingIntent.getActivity(
                            this@MainActivity,
                            0,
                            Intent(
                                this@MainActivity,
                                this@MainActivity.javaClass
                            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        )
                        adapter?.enableForegroundDispatch(
                            this@MainActivity,
                            pendingIntent,
                            intentFilter,
                            techList
                        )
                    } else {
                        adapter?.disableForegroundDispatch(this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        adapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (adapter == null) {
            println("NFC_EMV adapter null")
        } else {
            println("NFC_EMV receive intent")
        }

        try {
            val isoDep = IsoDep.get(intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG))!!

            Emv.proceedEmv(isoDep)
        } catch (e: Exception) {
            println("NFC_EMV e: $e")
        }
    }
}
