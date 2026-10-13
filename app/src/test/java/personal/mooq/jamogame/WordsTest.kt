package personal.mooq.jamogame

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import personal.mooq.jamogame.game.Hangul
import personal.mooq.jamogame.game.JAMO_COUNT_OPTIONS
import personal.mooq.jamogame.game.WordDictionary
import personal.mooq.jamogame.game.WordSource

/**
 * assets 의 단어 목록을 파일로 직접 읽어 검사한다.
 * 목록을 우리말샘 데이터로 갈아 끼울 때 길이가 어긋난 단어를 바로 잡아내기 위함.
 */
class WordsTest {

    private fun linesIn(name: String): List<String> =
        File("src/main/assets/$name")
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

    private fun wordsIn(jamoCount: Int) = linesIn(WordSource.fileName(jamoCount))

    private fun validIn(jamoCount: Int) = linesIn(WordDictionary.fileName(jamoCount))

    @Test
    fun `자모 개수별 목록 파일이 있다`() {
        for (count in JAMO_COUNT_OPTIONS) {
            assertTrue(
                "${WordSource.fileName(count)} 가 비어 있다",
                wordsIn(count).isNotEmpty(),
            )
        }
    }

    @Test
    fun `목록의 모든 단어가 파일 이름의 자모 개수와 맞는다`() {
        for (count in JAMO_COUNT_OPTIONS) {
            for (word in wordsIn(count)) {
                assertEquals("$word 의 자모 수", count, Hangul.decompose(word).size)
            }
        }
    }

    @Test
    fun `목록에 중복이 없다`() {
        for (count in JAMO_COUNT_OPTIONS) {
            val words = wordsIn(count)
            assertEquals("자모 ${count}개 목록에 중복", words.size, words.toSet().size)
        }
    }

    @Test
    fun `검증 목록도 자모 개수가 맞는다`() {
        for (count in JAMO_COUNT_OPTIONS) {
            val words = validIn(count)
            assertTrue("${WordDictionary.fileName(count)} 가 비어 있다", words.isNotEmpty())
            for (word in words) {
                assertEquals("$word 의 자모 수", count, Hangul.decompose(word).size)
            }
        }
    }

    @Test
    fun `출제 단어는 모두 검증 목록에 들어 있다`() {
        // 정답을 그대로 입력했는데 "사전에 없는 단어" 로 막히면 안 된다.
        for (count in JAMO_COUNT_OPTIONS) {
            val valid = validIn(count).map { Hangul.decompose(it).joinToString("") }.toSet()
            for (word in wordsIn(count)) {
                assertTrue(
                    "$word 가 ${WordDictionary.fileName(count)} 에 없다",
                    Hangul.decompose(word).joinToString("") in valid,
                )
            }
        }
    }

    @Test
    fun `사전에 없는 자모 조합은 막는다`() {
        val dictionary = WordDictionary(assets = null)
        // assets 를 못 읽는 상황에서는 막지 않는다 (못 푸는 상황을 만들지 않기 위해)
        assertTrue(dictionary.contains(Hangul.decompose("보랗")))
    }

    @Test
    fun `assets 를 못 읽어도 단어를 돌려준다`() {
        val source = WordSource(assets = null)
        for (count in JAMO_COUNT_OPTIONS) {
            assertEquals(count, Hangul.decompose(source.random(count)).size)
        }
    }
}
