package personal.mooq.jamogame.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 길게 눌러야 실행되는 버튼. 잘못 눌러 판을 날리는 일을 막으려고 짧게 누르면 안내만 띄운다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressButton(
    text: String,
    onLongPress: () -> Unit,
    onShortPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(8.dp)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onShortPress,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            ),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
