package personal.mooq.jamogame.game

import android.content.Context
import androidx.core.content.edit

/**
 * 진행 중인 판을 기기에 남긴다. 앱을 껐다 켜도 이어서 풀 수 있도록.
 *
 * 저장하는 건 정답과 제출한 줄들뿐이다. 타일 색은 그 둘로 다시 계산할 수 있어서
 * 따로 둘 필요가 없다. 아직 제출하지 않은 입력은 저장하지 않는다.
 */
class GameStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(game: JamoGame) {
        prefs.edit {
            putString(KEY_ANSWER, game.answer)
            putString(KEY_GUESSES, game.submitted.joinToString(ROW_SEPARATOR) { row ->
                row.joinToString("") { it.jamo }
            })
            putBoolean(KEY_REVEALED, game.revealed)
        }
    }

    fun clear() = prefs.edit { clear() }

    /** 저장된 판을 되살린다. 자모 개수가 설정과 다르면 버린다. */
    fun restore(jamoCount: Int): JamoGame? {
        val answer = prefs.getString(KEY_ANSWER, null) ?: return null
        val game = JamoGame(answer)
        if (game.jamoCount != jamoCount) return null

        prefs.getString(KEY_GUESSES, null).orEmpty()
            .split(ROW_SEPARATOR)
            .filter { it.isNotEmpty() }
            .forEach { row -> game.play(row.map { it.toString() }) }

        if (prefs.getBoolean(KEY_REVEALED, false)) game.revealAnswer()
        return game
    }

    private companion object {
        const val PREFS_NAME = "jamogame_progress"
        const val KEY_ANSWER = "answer"
        const val KEY_GUESSES = "guesses"
        const val KEY_REVEALED = "revealed"
        const val ROW_SEPARATOR = "|"
    }
}
