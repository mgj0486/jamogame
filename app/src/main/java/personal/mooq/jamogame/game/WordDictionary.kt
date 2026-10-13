package personal.mooq.jamogame.game

import android.content.res.AssetManager

/**
 * 입력한 자모가 실제로 있는 단어인지 확인한다.
 *
 * assets 의 `valid_<자모개수>.txt` 를 자모열 집합으로 바꿔 들고 있는다. 자모만 보기 때문에
 * 같은 자모열을 가진 단어가 하나라도 있으면 통과다. 사용자가 음절이 아니라 자모를
 * 입력하는 게임이라 그게 맞는 기준이다.
 *
 * 목록에 없는 단어까지 받아주려면 나중에 사전 API 조회를 뒤에 덧붙이면 된다.
 */
class WordDictionary(private val assets: AssetManager?) {

    private val cache = mutableMapOf<Int, Set<String>>()

    fun contains(jamos: List<String>): Boolean {
        val entries = entriesFor(jamos.size)
        // 목록을 못 읽었으면 막지 않는다. 못 푸는 상황을 만드는 것보다 낫다.
        return entries.isEmpty() || jamos.joinToString("") in entries
    }

    private fun entriesFor(jamoCount: Int): Set<String> = cache.getOrPut(jamoCount) {
        try {
            assets?.open(fileName(jamoCount))?.bufferedReader()?.use { reader ->
                reader.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { Hangul.decompose(it).joinToString("") }
                    .toSet()
            }.orEmpty()
        } catch (e: java.io.IOException) {
            emptySet()
        }
    }

    companion object {
        fun fileName(jamoCount: Int) = "valid_$jamoCount.txt"
    }
}
