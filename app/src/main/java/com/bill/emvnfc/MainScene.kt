package com.bill.emvnfc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bill.emvnfc.emv.Store
import com.bill.emvnfc.emv.drawDiagonalShimmerLabel
import com.bill.emvnfc.emv.formatCardNumber
import com.bill.emvnfc.emv.formatExpireDate

@Composable
fun MainScene() {
    val cardData by Store.cardData.collectAsStateWithLifecycle()
    val isLoading by Store.isLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .shadow(
                    elevation = (8).dp, shape = RoundedCornerShape(16.dp)
                )
                .drawDiagonalShimmerLabel(
                    text = "Card",
                    color = Color(0xFF8BC34A),
                    labelTextRatio = 6F
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.4F))
                        .zIndex(1F)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF3F51B5),
                                Color(0xFF2196F3),
                            )
                        ), shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
            ) {
                Image(
                    modifier = Modifier.size(60.dp), painter = painterResource(
                        id = cardData?.brand?.resID ?: R.drawable.ic_unknown
                    ), contentDescription = null
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "CARD NUMBER",
                    color = Color.White.copy(alpha = 0.7F),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W300)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cardData?.cardNumber?.formatCardNumber() ?: "**** **** **** ****",
                    color = Color.White,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W500)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EXPIRE DATE",
                    color = Color.White.copy(alpha = 0.7F),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W300)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cardData?.expireDate?.formatExpireDate() ?: "**/**",
                    color = Color.White,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W500)
                )
            }

            Image(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(60.dp),
                painter = painterResource(id = R.drawable.ic_chip),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier
            .fillMaxWidth(0.5F)
            .clickable {
                Store.isEnableNfc.value = true
                Store.isLoading.value = true
                Store.cardData.value = null
            }
            .background(
                color = Color(0xFF4CAF50), shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = (8).dp, shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 8.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Scan card",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W500),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        if (cardData != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CopyableText(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Brand",
                    text = cardData!!.brand.alias
                )

                CopyableText(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Card number",
                    text = cardData!!.cardNumber
                )

                CopyableText(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Expire date",
                    text = cardData!!.expireDate.formatExpireDate()
                )
            }
        }
    }
}