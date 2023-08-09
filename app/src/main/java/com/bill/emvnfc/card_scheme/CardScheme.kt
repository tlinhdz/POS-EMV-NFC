package com.bill.emvnfc.card_scheme

import android.nfc.tech.IsoDep
import com.bill.emvnfc.emv.EmvStepData

interface CardScheme {
    fun getProcessingOption(
        isoDep: IsoDep,
        tags: HashMap<String, String>
    ): Result<EmvStepData>

    fun readRecord(
        isoDep: IsoDep,
        tags: HashMap<String, String>
    ): Result<EmvStepData>
}