package personal.mooq.jamogame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.mooq.jamogame.game.Match

/** 지우기 키 자리를 나타내는 표식. 자모가 아니라서 따로 구분한다. */
private const val BACKSPACE = "←"

/**
 * 두벌식 배열을 기본 자모만 남겨 줄인 전용 키보드.
 * 쌍자음(ㄱㄱ)과 이중모음(ㅗㅏㅣ)은 기본 자모를 이어 눌러서 입력한다.
 */
private val KEY_ROWS = listOf(
    listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", BACKSPACE),
    listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ"),
    listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ"),
)

/** 가장 긴 줄 기준으로 키 폭을 정해 모든 줄의 키 크기를 맞춘다. */
private val COLUMNS = KEY_ROWS.maxOf { it.size }

private val KeyGap = 5.dp
private val KeyHeight = 48.dp

@Composable
fun JamoKeyboard(
    keyMatches: Map<String, Match>,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val keyWidth = (maxWidth - KeyGap * (COLUMNS - 1)) / COLUMNS
        Column(verticalArrangement = Arrangement.spacedBy(KeyGap)) {
            KEY_ROWS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KeyGap, Alignment.CenterHorizontally),
                ) {
                    row.forEach { key ->
                        Key(
                            label = key,
                            match = keyMatches[key],
                            onClick = { if (key == BACKSPACE) onBackspace() else onKeyPress(key) },
                            modifier = Modifier.width(keyWidth),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Key(
                label = "입력",
                match = null,
                onClick = onSubmit,
                background = if (submitEnabled) MaterialTheme.colorScheme.primary else null,
                textColor = if (submitEnabled) MaterialTheme.colorScheme.onPrimary else null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Key(
    label: String,
    match: Match?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color? = null,
    textColor: Color? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = background
            ?: match?.let { MatchColors.background(it) }
            ?: MatchColors.keyIdle(),
        modifier = modifier.height(KeyHeight),
    ) {
        Box(
            modifier = Modifier.padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = textColor
                    ?: if (match != null) MatchColors.onMatch else MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
