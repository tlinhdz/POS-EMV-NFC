package com.bill.emvnfc.emv

import android.nfc.tech.IsoDep
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bill.emvnfc.CardData
import com.bill.emvnfc.MainApplication
import com.bill.emvnfc.card_scheme.CardScheme
import com.bill.emvnfc.card_scheme.MasterCard
import com.bill.emvnfc.card_scheme.Visa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import kotlin.properties.Delegates


object Emv {
    private const val TAG = "NFC_EMV"

    @Volatile
    private var isProcessing: Boolean = false
    private val tags: HashMap<String, String> = hashMapOf()
    private var state = MutableStateFlow(EmvState.IDLE)
    private var tempEntries: List<EntryTag61> = listOf()
    private var cardScheme by Delegates.notNull<CardScheme>()
    private var coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val visaAidList: List<String> = listOf(
        "315041592E5359532E4444463031",
        "44464D46412E44466172653234313031",
        "A00000000101",
        "A000000003000000",
        "A00000000300037561",
        "A00000000305076010",
        "A0000000031010",
        "A000000003101001",
        "A000000003101002",
        "A0000000032010",
        "A0000000032020",
        "A0000000033010",
        "A0000000034010",
        "A0000000035010",
        "A000000003534441",
        "A0000000035350",
        "A000000003535041",
        "A0000000036010",
        "A0000000036020",
        "A0000000038002",
        "A0000000038010",
        "A0000000039010",
        "A000000003999910",
    )
    private val masterCardAidList: List<String> = listOf(
        "A0000000040000",
        "A00000000401",
        "A0000000041010",
        "A00000000410101213",
        "A00000000410101215",
        "A0000000041010BB5449435301",
        "A0000000042010",
        "A0000000042203",
        "A0000000043010",
        "A0000000043060",
        "A000000004306001",
        "A0000000044010",
        "A0000000045010",
        "A0000000045555",
        "A0000000046000",
        "A0000000048002",
        "A0000000049999",
    )

    /**
     * Only process VISA card for now, so dont need to detect card scheme
     */
    fun proceedEmv(isoDep: IsoDep) {
        coroutineScope.coroutineContext.cancelChildren()
        coroutineScope.launch {
            try {
                isProcessing = true
                isoDep.connect()
                initState()

                state.collect {
                    when (it) {
                        EmvState.IDLE -> {
                            // TODO
                        }

                        EmvState.SELECT_PPSE -> {
                            selectPPSE(isoDep)
                        }

                        EmvState.SELECT_AID -> {
                            selectAID(isoDep)
                        }

                        EmvState.GET_PROCESSING_OPTION -> {
                            getProcessingOption(isoDep)
                        }

                        EmvState.READ_RECORD -> {
                            readRecord(isoDep)
                        }

                        EmvState.DONE -> {
                            Store.cardData.value = CardData(
                                brand = CardData.getCardBrand(cardScheme),
                                cardNumber = tags["5A"] ?: throw Exception("require field 5A"),
                                expireDate = tags["5F24"] ?: throw Exception("require field 5F24"),
                            )
                            println("$TAG $tags")
                            resetData()
                        }

                        EmvState.FAILED -> {
                            showToast("Read card failed")
                            resetData()
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    println("$TAG coroutine scope cancelled")
                } else {
                    println("$TAG error $e")
                    resetData()
                    showToast("Read card failed")
                }
            } finally {
                isProcessing = false
            }
        }
    }

    private fun initState() {
        isProcessing = true
        state.value = EmvState.SELECT_PPSE
    }

    private fun resetData() {
        Store.isLoading.value = false
        Store.isEnableNfc.value = false
        isProcessing = false
        state.value = EmvState.IDLE
        tempEntries = listOf()
        tags.clear()
    }

    private fun selectPPSE(isoDep: IsoDep) {
        selectPPSEExecute(isoDep)
            .onSuccess { item ->
                println("$TAG SelectPPSE success")

                tempEntries = item
                state.value = EmvState.SELECT_AID
            }
            .onFailure {
                println("$TAG SelectPPSE fail ${it.message}")
                state.value = EmvState.FAILED
            }
    }

    private fun selectAID(isoDep: IsoDep) {
        selectAIDExecute(isoDep)
            .onSuccess {
                println("$TAG SelectAID success")

                println("$TAG $it")
                tags.putAll(it)
                cardScheme = detectCardScheme(tags["84"]!!)
                state.value = EmvState.GET_PROCESSING_OPTION
            }
            .onFailure {
                println("$TAG SelectAID fail ${it.message}")
                state.value = EmvState.FAILED
            }
    }

    private fun getProcessingOption(isoDep: IsoDep) {
        cardScheme.getProcessingOption(isoDep, tags)
            .onSuccess {
                println("$TAG GPO success")
                tags.putAll(it.tags)
                state.value = it.nextStep
            }
            .onFailure {
                println("$TAG GPO fail ${it.message}")
                state.value = EmvState.FAILED
            }
    }

    private fun readRecord(isoDep: IsoDep) {
        cardScheme.readRecord(isoDep, tags)
            .onSuccess {
                println("$TAG ReadRecord success")

                tags.putAll(it.tags)
                state.value = EmvState.DONE
            }
            .onFailure {
                println("$TAG Read Record fail")
                state.value = EmvState.FAILED
            }
    }

    private fun selectPPSEExecute(isoDep: IsoDep): Result<List<EntryTag61>> {
        return kotlin.runCatching {
            val command = APDU.genCommandSelectPPSE()
            println("$TAG APDU Request: $command")
            val response =
                isoDep.transceive(command.decodeHex())
            val hexRes = response.toHexString()
            println("$TAG APDU Response: $hexRes")

            hexRes.substring(hexRes.length - 4).let {
                if (it.isRspCodeSuccess) {
                    return@runCatching APDU.decodeSelectPPSEResponse(hexRes.dropLast(4))
                } else {
                    throw Exception("Response code: $it")
                }
            }
        }
    }

    private fun selectAIDExecute(isoDep: IsoDep): Result<HashMap<String, String>> {
        return kotlin.runCatching {
            tempEntries.sortedBy { it.priorityT87.toInt() }.forEach {
                val command = APDU.genCommandSelectAID(it.aidT84)
                println("$TAG APDU Request: $command")
                val response = isoDep.transceive(command.decodeHex())
                val hexRes = response.toHexString()
                println("$TAG APDU Response: $hexRes")

                hexRes.substring(hexRes.length - 4).let { rsp ->
                    if (rsp.isRspCodeSuccess)
                        return@runCatching APDU.decodeSelectAIDResponse(hexRes.dropLast(4))
                }
            }
            throw Exception("Not found any matched AID")
        }
    }

    private fun detectCardScheme(aid: String): CardScheme {
        return when (aid) {
            in visaAidList -> Visa()
            in masterCardAidList -> MasterCard()
            else -> throw Exception("Card scheme not supported")
        }
    }

    private fun showToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                MainApplication.context(),
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

