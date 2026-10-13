package personal.mooq.jamogame.game

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

/** 고를 수 있는 자모 개수. (단어 -> 5, 라이브 -> 6) */
val JAMO_COUNT_OPTIONS = listOf(5, 6)

/**
 * 기기에 남는 설정. SharedPreferences 에 저장하고, 값은 Compose state 라 바뀌면 화면이 따라온다.
 */
class GameSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var storedJamoCount by mutableStateOf(
        prefs.getInt(KEY_JAMO_COUNT, JAMO_COUNT_OPTIONS.first()),
    )

    /** 한 판에 쓰는 자모 개수. 값을 넣으면 그 자리에서 기기에 저장된다. */
    var jamoCount: Int
        get() = storedJamoCount
        set(count) {
            if (count == storedJamoCount || count !in JAMO_COUNT_OPTIONS) return
            storedJamoCount = count
            prefs.edit { putInt(KEY_JAMO_COUNT, count) }
        }

    private companion object {
        const val PREFS_NAME = "jamogame_settings"
        const val KEY_JAMO_COUNT = "jamo_count"
    }
}
