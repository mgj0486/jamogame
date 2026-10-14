package personal.mooq.jamogame.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 화면 위에서 잠깐 내려오는 안내.
 *
 * 안드로이드 11부터 Toast 는 위치를 바꿀 수 없어서 앱 안에서 직접 그린다.
 * 보드를 가리지 않도록 상단바 바로 아래에 띄운다.
 */
@Composable
fun TopMessage(message: String?, modifier: Modifier = Modifier) {
    // 사라지는 동안에도 글자가 보이도록 마지막 문구를 들고 있는다
    var shown by remember { mutableStateOf("") }
    if (message != null) shown = message

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
        ) {
            Text(
                text = shown,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
