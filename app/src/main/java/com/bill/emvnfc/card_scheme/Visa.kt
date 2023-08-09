package com.bill.emvnfc.card_scheme

import android.nfc.tech.IsoDep
import com.bill.emvnfc.emv.APDU
import com.bill.emvnfc.emv.EmvState
import com.bill.emvnfc.emv.EmvStepData
import com.bill.emvnfc.emv.decodeHex
import com.bill.emvnfc.emv.isRspCodeSuccess
import com.bill.emvnfc.emv.toHexString
import com.bill.emvnfc.utility.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class Visa : CardScheme {
    companion object {
        const val TAG = "NFC_EMV VISA"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun getProcessingOption(
        isoDep: IsoDep,
        tags: HashMap<String, String>
    ): Result<EmvStepData> {
        return kotlin.runCatching {
            val command = APDU.genCommandGPO(tags["9F38"] ?: "")

            println("$TAG APDU Request: $command")
            val response = isoDep.transceive(command.decodeHex())
            val hexRes = response.toHexString()
            println("$TAG APDU Response: $hexRes")

            hexRes.substring(hexRes.length - 4).let { rsp ->
                if (rsp.isRspCodeSuccess)
                    return@runCatching EmvStepData(
                        tags = APDU.decodeGPOResponse(hexRes.dropLast(4)),
                        nextStep = EmvState.READ_RECORD
                    )
                else
                    throw Exception(rsp)
            }
        }
    }

    override fun readRecord(
        isoDep: IsoDep,
        tags: HashMap<String, String>
    ): Result<EmvStepData> {
        return kotlin.runCatching {
            val aflTag = Strings(tags["94"]!!)
            val hash = hashMapOf<String, String>()

            val listAFL = buildList {
                while (aflTag.isNotBlank()) {
                    add(aflTag.squish(8))
                }
            }

            listAFL.forEach {
                val startInd = it.substring(2, 4).toInt()
                val endInd = it.substring(4, 6).toInt()
                val commands = arrayListOf<String>()
                val cdLatch = CountDownLatch(endInd - startInd + 1)

                (startInd..endInd).forEach { ind ->
                    commands.add(
                        APDU.genCommandReadRecord(
                            ind.toString().padStart(2, '0'),
                            it.substring(0, 2),
                        )
                    )
                }

                commands.forEachIndexed { index, command ->
                    coroutineScope.launch {
                        try {
                            delay(index * 50L)
                            println("$TAG APDU Request: $command")
                            val response = isoDep.transceive(command.decodeHex())
                            val hexRes = response.toHexString()
                            println("$TAG APDU Response: $hexRes")

                            hexRes.substring(hexRes.length - 4).let { rsp ->
                                if (rsp.isRspCodeSuccess)
                                    hash.putAll(APDU.decodeReadRecordResponse(hexRes.dropLast(4)))
                                else
                                    throw Exception(rsp)
                            }
                        } catch (e: Exception) {
                            println("$TAG exception: $e")
                        } finally {
                            cdLatch.countDown()
                        }
                    }
                }

                cdLatch.await()
            }

            return@runCatching EmvStepData(tags = hash, nextStep = EmvState.IDLE)
        }
    }
}