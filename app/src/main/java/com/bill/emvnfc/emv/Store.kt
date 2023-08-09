package com.bill.emvnfc.emv

import com.bill.emvnfc.CardData
import kotlinx.coroutines.flow.MutableStateFlow

object Store {
    val isEnableNfc = MutableStateFlow(false)
    val cardData = MutableStateFlow<CardData?>(null)
    val isLoading = MutableStateFlow(false)
}