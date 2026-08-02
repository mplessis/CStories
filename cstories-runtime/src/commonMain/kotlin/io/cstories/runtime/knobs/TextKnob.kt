package io.cstories.runtime.knobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cstories.runtime.CStoriesColors
import io.cstories.runtime.CStoriesRadii

@Composable
fun TextKnob(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    codeKey: String? = null,
) {
    PublishKnobValue(codeKey, value)
    Knob(label = label) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = CStoriesColors.text, fontSize = 13.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(CStoriesColors.surfaceMuted, RoundedCornerShape(CStoriesRadii.sm))
                .border(1.dp, CStoriesColors.border, RoundedCornerShape(CStoriesRadii.sm))
                .padding(horizontal = 12.dp, vertical = 9.dp),
        )
    }
}
