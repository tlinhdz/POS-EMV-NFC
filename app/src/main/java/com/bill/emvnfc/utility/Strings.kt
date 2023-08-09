package com.bill.emvnfc.utility;

data class Strings(
    private var _value: String
) {
    val value: String
        get() {
            return _value
        }

    val length: Int
        get() {
            return _value.length
        }

    fun squish(num: Int): String {
        val droppedPart = _value.take(num)
        _value = _value.drop(num)
        return droppedPart
    }

    fun isBlank(): Boolean = _value.isBlank()
    fun isNotBlank(): Boolean = _value.isNotBlank()
    fun isEmpty(): Boolean = _value.isEmpty()
    fun isNotEmpty(): Boolean = _value.isNotEmpty()
}