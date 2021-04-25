/*
 * Copyright (c) 2021 Olivier Patry
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.opatry.game.hues.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Objects
import kotlin.math.pow


enum class TileUpdate {
    NEW,
    NONE,
    MOVED_UP,
    MOVED_DOWN,
    MOVED_LEFT,
    MOVED_RIGHT,
    COMBINED;

    override fun toString(): String = when (this) {
        NEW -> "+"
        NONE -> " "
        COMBINED -> "×"
        MOVED_UP -> "↑"
        MOVED_DOWN -> "↓"
        MOVED_LEFT -> "←"
        MOVED_RIGHT -> "→"
    }
}

data class Tile(val rank: Int) : Comparable<Tile> {
    val id: Long = ID++

    // Empty tile value is 0 despite 2^0 == 1
    val value: Int = if (rank != 0) 2.0.pow(rank).toInt() else 0
    var update: TileUpdate = if (rank != 0) TileUpdate.NEW else TileUpdate.NONE

    // TODO should we consider update in compare & equals/hashCode?

    override fun compareTo(other: Tile): Int {
        // empty tiles are considered equals
        return if (rank == 0 && other.rank == 0) {
            0
        } else {
            (id - other.id).toInt()
        }
    }

    override fun hashCode(): Int {
        return if (rank == 0) {
            Objects.hashCode(EMPTY_TILE.id)
        } else {
            Objects.hashCode(id)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Tile) {
            // empty tiles are considered equals
            when (rank) {
                0 -> other.rank == rank
                else -> id == other.id
            }
        } else {
            false
        }
    }

    override fun toString(): String {
        return "$update${rank.toString().padStart(2, ' ')} (id=${id.toString().padStart(2, ' ')})"
    }

    companion object {
        private var ID: Long = 0L

        @JvmField
        val EMPTY_TILE = Tile(0)
    }
}

@Composable
fun TilesApp() {
    MaterialTheme {
        var tiles by remember { mutableStateOf(listOf(
            Tile(0),
            Tile(1),
            Tile(2),
            Tile(3),
        ))
        }
        Column {
            Tiles(tiles)
            Button(onClick = {
                tiles = listOf(
                    Tile(0),
                    Tile(2).apply { update = TileUpdate.MOVED_LEFT },
                    Tile(3).apply { update = TileUpdate.NEW },
                    Tile(4).apply { update = TileUpdate.COMBINED })
            }) {
                Text("Change")
            }
        }
    }
}

@Composable
fun Tiles(tiles: List<Tile>) {
    Box(contentAlignment = Alignment.Center) {
        LazyRow(Modifier.fillMaxWidth()) {
            items(tiles) { tile ->
                TileView(
                    tile,
                    Modifier
                        .fillMaxWidth()
                        .size(60.dp, 80.dp)
                )
            }
        }
    }
}


val TileColors = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFFFC426),
    Color(0xFFE87E2E),
    Color(0xFFD84444),
    Color(0xFF3FA2D9),
)

@Composable
fun TileView(
    tile: Tile,
    modifier: Modifier = Modifier
) {
    val duration = 600
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scale = remember(
        tile.id,
        tile.update
    ) { Animatable(if (tile.update == TileUpdate.NEW) 0f else 1f) }
    val angle = remember(tile.id, tile.update) { Animatable(0f) }
    val offsetX = remember(tile.id, tile.update) {
        Animatable(
            when (tile.update) {
                TileUpdate.MOVED_LEFT -> size.width.toFloat()
                TileUpdate.MOVED_RIGHT -> -size.width.toFloat()
                else -> 0f
            }
        )
    }
    val offsetY =
        remember(tile.id, tile.update) {
            Animatable(
                when (tile.update) {
                    TileUpdate.MOVED_UP -> size.height.toFloat()
                    TileUpdate.MOVED_DOWN -> -size.height.toFloat()
                    else -> 0f
                }
            )
        }
    LaunchedEffect(tile) {
        when (tile.update) {
            TileUpdate.NONE -> {
                scale.snapTo(1f)
                angle.snapTo(0f)
                offsetX.snapTo(size.width.toFloat())
                offsetY.snapTo(size.height.toFloat())
            }
            TileUpdate.NEW -> {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(duration)
                )
                angle.snapTo(0f)
                offsetX.snapTo(0f)
                offsetY.snapTo(0f)
            }
            TileUpdate.COMBINED -> {
                scale.snapTo(1f)
                angle.animateTo(
                    targetValue = 180f, // FIXME 180f mirrors the card, what we want is tile flip effect without mirroring
                    animationSpec = tween(duration)
                )
                offsetX.snapTo(0f)
                offsetY.snapTo(0f)
            }
            TileUpdate.MOVED_UP,
            TileUpdate.MOVED_DOWN -> {
                scale.snapTo(1f)
                angle.snapTo(0f)
                offsetX.snapTo(0f)
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(duration)
                )
            }
            TileUpdate.MOVED_LEFT,
            TileUpdate.MOVED_RIGHT -> {
                scale.snapTo(1f)
                angle.snapTo(0f)
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(duration)
                )
                offsetY.snapTo(0f)
            }
        }
    }

    Box(
        modifier
            .onSizeChanged { if (size == IntSize.Zero) size = it }
            .scale(scale.value)
            .graphicsLayer(rotationY = angle.value)
            .offset(x = offsetX.value.dp, y = offsetY.value.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // only draw tile slot for empty tiles
        if (tile != Tile.EMPTY_TILE) {
        Box(
            Modifier
                .padding(bottom = 8.dp, start = 2.dp, end = 2.dp)
                .background(TileColors[tile.rank % TileColors.size])
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
                Text(
                    tile.value.toString(),
                    color = Color.White,
                    fontSize = 24.sp,
                )

                // debug data
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                    Text(
                        "${tile.update} id=${tile.id}",
                        Modifier.padding(4.dp),
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
