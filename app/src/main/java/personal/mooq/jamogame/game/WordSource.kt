package personal.mooq.jamogame.game

import android.content.res.AssetManager

/**
 * 출제 단어를 assets 의 목록 파일에서 읽어온다.
 *
 * 파일은 `words_<자모개수>.txt`, 한 줄에 단어 하나. `#` 로 시작하는 줄과 빈 줄은 건너뛴다.
 * 우리말샘에서 걸러낸 목록으로 파일만 갈아 끼우면 되도록 형식을 단순하게 뒀다.
 */
class WordSource(private val assets: AssetManager?) {

    private val cache = mutableMapOf<Int, List<String>>()

    fun of(jamoCount: Int): List<String> = cache.getOrPut(jamoCount) {
        readAsset(jamoCount).ifEmpty { FALLBACK[jamoCount].orEmpty() }
    }

    /** 자모 개수가 [jamoCount] 인 단어 하나. */
    fun random(jamoCount: Int): String =
        of(jamoCount).randomOrNull() ?: FALLBACK.getValue(JAMO_COUNT_OPTIONS.first()).first()

    private fun readAsset(jamoCount: Int): List<String> = try {
        assets?.open(fileName(jamoCount))?.bufferedReader()?.use { reader ->
            reader.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()
        }.orEmpty()
    } catch (e: java.io.IOException) {
        // 목록 파일이 아직 없는 자모 개수. 내장 목록으로 돌아간다.
        emptyList()
    }

    companion object {
        fun fileName(jamoCount: Int) = "words_$jamoCount.txt"

        /** assets 를 못 읽을 때 쓰는 최소 목록. 미리보기나 파일 누락 상황용. */
        private val FALLBACK = mapOf(
            5 to listOf("단어"),
            6 to listOf("라이브"),
        )
    }
}
