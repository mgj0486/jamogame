package personal.mooq.jamogame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import personal.mooq.jamogame.game.Hangul

class HangulTest {

    @Test
    fun `정답 단어는 자모 5개로 쪼개진다`() {
        assertEquals(listOf("ㄷ", "ㅏ", "ㄴ", "ㅇ", "ㅓ"), Hangul.decompose("단어"))
    }

    @Test
    fun `이중모음은 기본 모음들로 쪼갠다`() {
        assertEquals(listOf("ㅇ", "ㅗ", "ㅏ", "ㅣ"), Hangul.decompose("왜"))
        assertEquals(listOf("ㄱ", "ㅗ", "ㅏ", "ㅈ", "ㅏ"), Hangul.decompose("과자"))
        assertEquals(listOf("ㅎ", "ㅡ", "ㅣ", "ㄴ"), Hangul.decompose("흰"))
    }

    @Test
    fun `쌍자음은 같은 자음 두 번으로 쪼갠다`() {
        assertEquals(listOf("ㄱ", "ㄱ", "ㅏ", "ㄱ", "ㄱ"), Hangul.decompose("깎"))
        assertEquals(listOf("ㅅ", "ㅅ", "ㅏ", "ㅇ"), Hangul.decompose("쌍"))
    }

    @Test
    fun `겹받침은 자모 두 개로 쪼갠다`() {
        assertEquals(listOf("ㅁ", "ㅏ", "ㄹ", "ㄱ"), Hangul.decompose("맑"))
        assertEquals(listOf("ㄱ", "ㅏ", "ㅂ", "ㅅ"), Hangul.decompose("값"))
    }

    @Test
    fun `받침 유무를 가린다`() {
        assertTrue(Hangul.endsWithFinalConsonant("눈물"))
        assertTrue(Hangul.endsWithFinalConsonant("선물"))
        assertEquals(false, Hangul.endsWithFinalConsonant("단어"))
        assertEquals(false, Hangul.endsWithFinalConsonant("바나나"))
    }

    @Test
    fun `어떤 음절이든 키보드에 있는 자모만으로 쪼개진다`() {
        val keys = (Hangul.BASIC_CONSONANTS + Hangul.BASIC_VOWELS).toSet()
        for (code in 0xAC00..0xD7A3) {
            val syllable = code.toChar().toString()
            val jamos = Hangul.decompose(syllable)
            assertTrue(
                "$syllable -> $jamos 에 키보드에 없는 자모가 있다",
                jamos.all { it in keys },
            )
        }
    }

    @Test
    fun `자모를 음절로 되돌린다`() {
        assertTrue("단어" in Hangul.compositions(Hangul.decompose("단어")))
        assertTrue("깎" in Hangul.compositions(Hangul.decompose("깎")))
        assertTrue("학교" in Hangul.compositions(Hangul.decompose("학교")))
        assertTrue("왜" in Hangul.compositions(Hangul.decompose("왜")))
        assertTrue("맑" in Hangul.compositions(Hangul.decompose("맑")))
        // 없는 단어라도 음절로 떨어지면 조합은 나온다. 단어인지는 사전이 판단한다.
        assertTrue("보랗" in Hangul.compositions(Hangul.decompose("보랗")))
    }

    @Test
    fun `음절로 떨어지지 않으면 조합이 없다`() {
        assertEquals(emptyList<String>(), Hangul.compositions(listOf("ㄱ", "ㄴ")))
        assertEquals(emptyList<String>(), Hangul.compositions(listOf("ㅏ", "ㅓ")))
    }

    @Test
    fun `모든 음절은 자기 자모열에서 되살아난다`() {
        for (code in 0xAC00..0xD7A3) {
            val syllable = code.toChar().toString()
            assertTrue(
                "$syllable 를 되살리지 못했다",
                syllable in Hangul.compositions(Hangul.decompose(syllable), limit = 64),
            )
        }
    }

    @Test
    fun `키보드 자모 수는 자음 14개 모음 10개다`() {
        assertEquals(14, Hangul.BASIC_CONSONANTS.size)
        assertEquals(10, Hangul.BASIC_VOWELS.size)
    }
}
