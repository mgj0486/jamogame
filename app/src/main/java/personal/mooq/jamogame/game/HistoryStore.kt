package personal.mooq.jamogame.game

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

/**
 * 끝난 판들의 기록. 최근 것이 앞에 온다.
 *
 * 기기에만 남기면 되는 가벼운 데이터라 SharedPreferences 에 한 덩어리로 넣는다.
 * 무한정 쌓이지 않게 [LIMIT] 개까지만 들고 있는다.
 */
class HistoryStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var records: List<GameRecord> by mutableStateOf(GameRecord.decodeAll(prefs.getString(KEY, null)))
        private set

    fun add(record: GameRecord) {
        records = (listOf(record) + records).take(LIMIT)
        persist()
    }

    fun clear() {
        records = emptyList()
        persist()
    }

    private fun persist() = prefs.edit { putString(KEY, GameRecord.encodeAll(records)) }

    private companion object {
        const val PREFS_NAME = "jamogame_history"
        const val KEY = "records"
        const val LIMIT = 200
    }
}
