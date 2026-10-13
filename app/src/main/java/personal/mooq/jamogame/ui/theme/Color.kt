package personal.mooq.jamogame.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 전부 같은 중성 회색 계열로 맞춘다. 화면에서 색을 쓰는 곳은 채점 결과(초록/노랑/회색)뿐이고,
 * 나머지는 색을 빼서 그 셋이 눈에 먼저 들어오게 한다.
 */

// 바탕과 글자
val Ink = Color(0xFF1A1B1C)
val Paper = Color(0xFFFFFFFF)
val InkDark = Color(0xFFF2F2F3)
val PaperDark = Color(0xFF121213)

// 버튼, 키, 테두리 같은 보조 면
val Mist = Color(0xFFE8EAED)
val MistInk = Color(0xFF5B5E63)
val MistDark = Color(0xFF2C2E31)
val MistInkDark = Color(0xFFAEB0B4)

// 입력 버튼처럼 강조가 필요한 면. 채점 색과 헷갈리지 않게 무채색으로 간다.
val Accent = Color(0xFF2E3133)
val AccentDark = Color(0xFFE3E5E8)

val Danger = Color(0xFFB3261E)
val DangerDark = Color(0xFFF2B8B5)

/** 채점 결과 타일/키 색상. 라이트-다크 공통으로 쓸 수 있게 채도를 낮춰 잡았다. */
val CorrectGreen = Color(0xFF6AAA64)
val PresentYellow = Color(0xFFC9B458)
val AbsentGrey = Color(0xFF787C7E)
val AbsentGreyDark = Color(0xFF3A3A3C)

/** 빈 타일 외곽선 / 자모가 들어찬 타일 외곽선. */
val TileBorderEmpty = Color(0xFFD5D8DC)
val TileBorderFilled = Color(0xFF878A8C)
val TileBorderEmptyDark = Color(0xFF3A3A3C)
val TileBorderFilledDark = Color(0xFF565758)

/** 아직 안 써본 키 배경. 보조 면보다 한 단계 진하게 둬서 키처럼 보이게 한다. */
val KeyIdle = Color(0xFFD5D8DC)
val KeyIdleDark = Color(0xFF3F4144)
