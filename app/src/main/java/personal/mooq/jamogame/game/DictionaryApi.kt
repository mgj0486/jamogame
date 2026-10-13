package personal.mooq.jamogame.game

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import personal.mooq.jamogame.BuildConfig

/**
 * 국립국어원 우리말샘 오픈 API 로 단어가 실재하는지 확인한다.
 *
 * 내장 목록(`valid_*.txt`)에 없는 입력만 여기까지 온다. 게임은 자모를 다루고 API 는 단어를
 * 받으므로, [Hangul.compositions] 로 만든 후보를 하나씩 물어본다.
 */
class DictionaryApi(private val apiKey: String = BuildConfig.OPENDICT_API_KEY) {

    /**
     * 자모열로 읽을 수 있는 단어가 사전에 하나라도 있으면 true, 하나도 없으면 false.
     * 키가 없거나 네트워크가 안 되면 null — 판단을 못 한 것이지 없는 게 아니다.
     */
    suspend fun exists(jamos: List<String>): Boolean? {
        if (apiKey.isEmpty()) return null
        val candidates = Hangul.compositions(jamos)
        if (candidates.isEmpty()) return false

        var reachedServer = false
        for (word in candidates) {
            when (search(word)) {
                null -> Unit          // 이 후보는 확인 실패. 다음 후보를 보고 판단한다
                0 -> reachedServer = true
                else -> return true
            }
        }
        return if (reachedServer) false else null
    }

    /** 검색 결과 건수. 확인에 실패하면 null. */
    private suspend fun search(word: String): Int? = withContext(Dispatchers.IO) {
        val url = URL(
            "$ENDPOINT?key=$apiKey" +
                "&q=${URLEncoder.encode(word, "UTF-8")}" +
                "&req_type=json&advanced=y&method=exact",
        )
        var connection: HttpURLConnection? = null
        try {
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                requestMethod = "GET"
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body).optJSONObject("channel")?.optInt("total", -1)?.takeIf { it >= 0 }
        } catch (e: IOException) {
            null
        } catch (e: org.json.JSONException) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private companion object {
        const val ENDPOINT = "https://opendict.korean.go.kr/api/search"
        const val TIMEOUT_MS = 4000
    }
}
