package com.bill.emvnfc.emv

enum class EmvState {
    IDLE,
    SELECT_PPSE,
    SELECT_AID,
    GET_PROCESSING_OPTION,
    READ_RECORD,
    DONE,
    FAILED,
}