package com.bill.emvnfc.emv

data class EmvStepData(
    val tags: HashMap<String, String>,
    val nextStep: EmvState
)