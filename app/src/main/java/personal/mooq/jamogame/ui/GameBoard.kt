package personal.mooq.jamogame.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.mooq.jamogame.game.MAX_ATTEMPTS
import personal.mooq.jamogame.game.Tile

/** 타일 사이 간격. 보드가 차지할 높이를 화면에서 미리 계산할 때도 쓴다. */
val BoardTileGap = 6.dp

/**
 * 시도 횟수만큼의 줄 x [columns] 칸 보드.
 * 위에서부터 제출한 줄, 그 다음이 입력 중인 줄, 나머지는 빈 줄.
 */
@Composable
fun GameBoard(
    submitted: List<List<Tile>>,
    input: List<String>,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BoardTileGap),
    ) {
        repeat(MAX_ATTEMPTS) { row ->
            val tiles = submitted.getOrNull(row)
            when {
                tiles != null -> BoardRow(columns) { col, cellModifier ->
                    ScoredTile(tiles[col], cellModifier)
                }
                row == submitted.size -> BoardRow(columns) { col, cellModifier ->
                    InputTile(input.getOrNull(col), cellModifier)
                }
                else -> BoardRow(columns) { _, cellModifier -> InputTile(null, cellModifier) }
            }
        }
    }
}

@Composable
private fun BoardRow(columns: Int, cell: @Composable (col: Int, Modifier) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BoardTileGap),
    ) {
        repeat(columns) { col ->
            cell(col, Modifier.weight(1f).aspectRatio(1f))
        }
    }
}

@Composable
private fun ScoredTile(tile: Tile, modifier: Modifier) {
    val color by animateColorAsState(MatchColors.background(tile.match), label = "tile")
    TileBox(
        jamo = tile.jamo,
        textColor = MatchColors.onMatch,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color),
    )
}

@Composable
private fun InputTile(jamo: String?, modifier: Modifier) {
    TileBox(
        jamo = jamo,
        textColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(2.dp, MatchColors.tileBorder(filled = jamo != null), RoundedCornerShape(4.dp)),
    )
}

@Composable
private fun TileBox(jamo: String?, textColor: Color, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (jamo != null) {
            Text(
                text = jamo,
                color = textColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
