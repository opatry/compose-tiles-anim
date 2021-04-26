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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow


class Tile(val rank: Int) {
    val id: Long = ID++

    // Empty tile value is 0 despite 2^0 == 1
    val value: Int = if (rank != 0) 2.0.pow(rank).toInt() else 0
    var line: Int = 0
    var column: Int = 0

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun toString(): String {
        return "${rank.toString().padStart(2, ' ')} (id=${id.toString().padStart(2, ' ')})"
    }

    companion object {
        private var ID: Long = 0L

        @JvmField
        val EMPTY_TILE = Tile(0)
    }
}


val lineCount = 4
val columnCount = 4
val tileWidth = 60.dp
val tileHeight = 80.dp

@Composable
fun TilesApp() {
    MaterialTheme {
        var tile by remember {
            mutableStateOf(Tile(2).apply {
                line = 0
                column = 0
            })
        }

        var line by remember { mutableStateOf(tile.line) }
        var column by remember { mutableStateOf(tile.column) }

        val diffLine = tile.line - line
        val diffColumn = tile.column - column
//        if (diffLine != 0 || diffColumn != 0) {
        println("diffLine = $diffLine")
        println("diffColumn = $diffColumn")

        println("LINEBEFOR=$line vs ${tile.line}")
        line = tile.line
        println("LINEAFTER=$line vs ${tile.line}")
        column = tile.column
//        }

        val offsetX = tileWidth * column
        val offsetY = tileHeight * line

//        println("COLU=$column OFFSETX=$offsetX")
//        println("LINE=$line OFFSETY=$offsetY")

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(tileWidth * lineCount, tileHeight * columnCount)) {
                TileView(
                    tile,
                    Modifier
                        .size(tileWidth, tileHeight)
                        .offset(offsetX, offsetY)
                )
            }

            Button(onClick = { tile = tile.apply { line -= 1 } }, enabled = line > 0) {
                Icon(Icons.Default.KeyboardArrowUp, "Up")
            }
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { tile = tile.apply { column -= 1 } }, enabled = column > 0) {
                    Icon(Icons.Default.KeyboardArrowLeft, "Left")
                }
                Button(
                    onClick = { tile = tile.apply { line += 1 } },
                    enabled = line < lineCount - 1
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, "Down")
                }
                Button(
                    onClick = { tile = tile.apply { column += 1 } },
                    enabled = column < columnCount - 1
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, "Right")
                }
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
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier
            .onSizeChanged { if (size == IntSize.Zero) size = it },
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
                        "id=${tile.id}",
                        Modifier.padding(4.dp),
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
