package io.cstories.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DemoCard(
    title: String,
    body: String,
    elevated: Boolean = true,
) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (elevated) 8.dp else 0.dp),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(text = title)
            Text(text = body)
        }
    }
}
