package personal.mooq.jamogame.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 채점 색(초록/노랑)이 유일한 색이 되도록 나머지는 무채색으로 고정한다.
// 같은 이유로 기기 테마를 따라가는 다이내믹 컬러도 쓰지 않는다.
private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = MistInk,
    error = Danger,
    onError = Paper,
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = Ink,
    background = PaperDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = MistDark,
    onSurfaceVariant = MistInkDark,
    error = DangerDark,
    onError = Ink,
)

@Composable
fun JamogameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
