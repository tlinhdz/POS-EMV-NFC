package com.bill.emvnfc

import com.bill.emvnfc.card_scheme.CardScheme
import com.bill.emvnfc.card_scheme.MasterCard
import com.bill.emvnfc.card_scheme.Visa

data class CardData(
    val brand: Brand = Brand.VISA,
    val cardNumber: String = "",
    val expireDate: String = ""
) {
    companion object {
        fun getCardBrand(scheme: CardScheme): Brand {
            return when (scheme) {
                is Visa -> Brand.VISA
                is MasterCard -> Brand.MASTERCARD
                else -> throw Exception("Card scheme not supported")
            }
        }
    }
}

enum class Brand(val resID: Int, val alias: String) {
    VISA(R.drawable.ic_visa, "Visa"),
    MASTERCARD(R.drawable.ic_master_card, "Master Card")
}