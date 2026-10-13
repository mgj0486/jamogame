package personal.mooq.jamogame.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import personal.mooq.jamogame.game.Match
import personal.mooq.jamogame.ui.theme.AbsentGrey
import personal.mooq.jamogame.ui.theme.AbsentGreyDark
import personal.mooq.jamogame.ui.theme.CorrectGreen
import personal.mooq.jamogame.ui.theme.KeyIdle
import personal.mooq.jamogame.ui.theme.KeyIdleDark
import personal.mooq.jamogame.ui.theme.PresentYellow
import personal.mooq.jamogame.ui.theme.TileBorderEmpty
import personal.mooq.jamogame.ui.theme.TileBorderEmptyDark
import personal.mooq.jamogame.ui.theme.TileBorderFilled
import personal.mooq.jamogame.ui.theme.TileBorderFilledDark

/** 보드 타일과 키보드 키가 같은 색 규칙을 쓰도록 한곳에 모아둔다. */
object MatchColors {

    @Composable
    @ReadOnlyComposable
    fun background(match: Match): Color = when (match) {
        Match.CORRECT -> CorrectGreen
        Match.PRESENT -> PresentYellow
        Match.ABSENT -> if (isSystemInDarkTheme()) AbsentGreyDark else AbsentGrey
    }

    /** 채점된 타일 위 글자는 어떤 배경에서도 흰색이 가장 잘 읽힌다. */
    val onMatch: Color = Color.White

    @Composable
    @ReadOnlyComposable
    fun keyIdle(): Color = if (isSystemInDarkTheme()) KeyIdleDark else KeyIdle

    @Composable
    @ReadOnlyComposable
    fun tileBorder(filled: Boolean): Color = when {
        isSystemInDarkTheme() && filled -> TileBorderFilledDark
        isSystemInDarkTheme() -> TileBorderEmptyDark
        filled -> TileBorderFilled
        else -> TileBorderEmpty
    }
}
