package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mergepulse.R
import com.example.game.engine.GameWorld
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import kotlin.math.sin

@Composable
fun ControlPanel(
    world: GameWorld,
    uiTick: Long,
    onFlipClick: () -> Unit,
    onDropClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCore = world.currentCore ?: world.nextCore
    val isFlipReady = world.flipEnergy >= 1.0f || world.isTutorialActive

    val flipInteraction = remember { MutableInteractionSource() }
    val isFlipPressed by flipInteraction.collectIsPressedAsState()
    val flipScale by animateFloatAsState(
        targetValue = if (isFlipPressed) 0.96f else 1.0f,
        animationSpec = spring(),
        label = "flip_press"
    )

    val dropInteraction = remember { MutableInteractionSource() }
    val isDropPressed by dropInteraction.collectIsPressedAsState()
    val dropScale by animateFloatAsState(
        targetValue = if (isDropPressed) 0.96f else 1.0f,
        animationSpec = spring(),
        label = "drop_press"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. LEFT: PREVIEW CORE WITH TIER BADGE AND CHARGE SYMBOL
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            activeCore?.let { core ->
                ReadyCorePreviewCanvas(core = core, sizeDp = 60)

                // Clear Tier Badge (e.g. L1, L2)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.level_short,
                            core.level
                        ),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. CENTER: [ ⟳ flip ± ] SECONDARY OUTLINE BUTTON
        Box(
            modifier = Modifier
                .weight(1.05f)
                .height(54.dp)
                .scale(flipScale)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFBDBDBD), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = flipInteraction,
                    indication = null,
                    enabled = isFlipReady
                ) {
                    onFlipClick()
                }
                .testTag("flip_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = stringResource(R.string.cd_flip),
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.flip),
                    color = Color(0xFF1E1E1E),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. RIGHT: [ drop ] PRIMARY SOLID BLACK BUTTON
        Box(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .scale(dropScale)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121212))
                .clickable(
                    interactionSource = dropInteraction,
                    indication = null
                ) {
                    onDropClick()
                }
                .testTag("drop_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.drop),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PreviewCoreCanvas(core: Core, sizeDp: Int) {
    ReadyCorePreviewCanvas(core = core, sizeDp = sizeDp)
}

@Composable
fun ReadyCorePreviewCanvas(core: Core, sizeDp: Int) {
    val levelInfo = CoreLevelRegistry.getInfo(core.level)
    val fillColor = if (core.charge == Charge.POSITIVE) levelInfo.positiveColor else levelInfo.negativeColor
    val borderColor = Color(darkOutlineColor(fillColor.toArgb()))
    val symbolColor = Color(symbolTextColor(fillColor.toArgb()))

    val now = System.currentTimeMillis() / 1000f
    val phase = (core.id % 1000) / 100f
    val idleScale = 1f + sin(now * 2.2f + phase) * 0.02f

    Canvas(
        modifier = Modifier
            .size(sizeDp.dp)
            .scale(idleScale)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * 0.44f

        // Fill
        drawCircle(
            color = fillColor,
            radius = radius,
            center = Offset(cx, cy)
        )

        // Geometric tier inner ring for preview
        if (core.level >= 2) {
            drawCircle(
                color = borderColor.copy(alpha = 0.4f),
                radius = radius * 0.72f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // Delicate dark border
        drawCircle(
            color = borderColor,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.8f)
        )

        // Polarity Symbol (+ or -)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = symbolColor.toArgb()
            textSize = radius * 1.15f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }

        val textBounds = android.graphics.Rect()
        paint.getTextBounds(core.charge.symbol, 0, core.charge.symbol.length, textBounds)
        val textY = cy + textBounds.height() / 2f - 2f

        drawContext.canvas.nativeCanvas.drawText(core.charge.symbol, cx, textY, paint)
    }
}

private fun darkOutlineColor(argb: Int): Int {
    val a = android.graphics.Color.alpha(argb)
    val r = (android.graphics.Color.red(argb) * 0.68f).toInt()
    val g = (android.graphics.Color.green(argb) * 0.68f).toInt()
    val b = (android.graphics.Color.blue(argb) * 0.68f).toInt()
    return android.graphics.Color.argb(a, r, g, b)
}

private fun symbolTextColor(argb: Int): Int {
    val a = android.graphics.Color.alpha(argb)
    val r = (android.graphics.Color.red(argb) * 0.35f).toInt()
    val g = (android.graphics.Color.green(argb) * 0.35f).toInt()
    val b = (android.graphics.Color.blue(argb) * 0.35f).toInt()
    return android.graphics.Color.argb(a, r, g, b)
}
