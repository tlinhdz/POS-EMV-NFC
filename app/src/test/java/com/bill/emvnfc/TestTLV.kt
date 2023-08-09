package com.bill.emvnfc

import com.bill.emvnfc.emv.Tags
import com.bill.emvnfc.utility.Strings
import org.junit.Test

class TestTLV {
    @Test
    fun decodeSelectInit() {
        val tlvData = Strings("6F4E840E325041592E5359532E4444463031A53CBF0C39611E4F07A0000001523010500B44494E45525320434C55428701019F2A02000661174F08A0000003241010005008446973636F7665728701029000")

        val list =  buildList {
            while (tlvData.isNotBlank()) {
                var tag = tlvData.squish(2)
                while (!isValidTag(tag)) {
                    tag += tlvData.squish(2)
                }

                val length = tlvData.squish(2).toInt(16)

                if (Tags.isTemplateTag(tag))
                    continue

                val value = tlvData.squish(length * 2)

                add(Tags.getTagValue(tag, value))
            }
        }

        println(list)
    }
}