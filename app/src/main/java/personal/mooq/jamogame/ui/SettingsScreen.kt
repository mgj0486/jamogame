package personal.mooq.jamogame.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.mooq.jamogame.game.JAMO_COUNT_OPTIONS
import personal.mooq.jamogame.ui.theme.JamogameTheme

/** 자모 개수별 예시 단어. 몇 글자짜리가 나오는지 감을 주려고 같이 보여준다. */
private val EXAMPLE_WORDS = mapOf(5 to "단어", 6 to "라이브")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    jamoCount: Int,
    onJamoCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = "자모 개수",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp),
            )
            JAMO_COUNT_OPTIONS.forEach { count ->
                JamoCountRow(
                    count = count,
                    selected = count == jamoCount,
                    onSelect = { onJamoCountChange(count) },
                )
            }
            Text(
                text = "바꾸면 새 단어로 다시 시작해요. 설정은 기기에 저장됩니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Spacer(Modifier.weight(1f))

            // CC BY-SA 2.0 KR 라 출처를 밝혀야 한다.
            HorizontalDivider()
            Text(
                text = "출제 단어는 국립국어원 \"한국어기초사전\"에서 가져왔습니다.\n" +
                    "크리에이티브 커먼즈 저작자표시-동일조건변경허락 2.0 대한민국 라이선스",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun JamoCountRow(count: Int, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = "자모 ${count}개", fontSize = 16.sp)
        EXAMPLE_WORDS[count]?.let { example ->
            Text(
                text = "예: $example",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    JamogameTheme {
        SettingsScreen(jamoCount = 5, onJamoCountChange = {}, onBack = {})
    }
}
