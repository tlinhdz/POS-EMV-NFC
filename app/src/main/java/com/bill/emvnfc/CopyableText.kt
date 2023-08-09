package com.bill.emvnfc

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CopyableText(
    modifier: Modifier = Modifier,
    label: String = "",
    text: String = ""
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1F),
                text = text
            )

            Spacer(modifier = Modifier.width(16.dp))

            Image(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(text))
                        Toast
                            .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                    },
                painter = painterResource(id = R.drawable.ic_copy),
                contentDescription = null
            )
        }
    }
}