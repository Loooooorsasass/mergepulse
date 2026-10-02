package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameWorld
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPink

@Composable
fun ControlPanel(
    world: GameWorld,
    onFlipClick: () -> Unit,
    onDropClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = world.currentCore
    val canFlip = current != null && world.flipEnergy >= 1f
    val flipInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val dropInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val flipPressed by flipInteraction.collectIsPressedAsState()
    val dropPressed by dropInteraction.collectIsPressedAsState()
    val flipScale = if (flipPressed) 0.96f else 1f
    val dropScale = if (dropPressed) 0.96f else 1f

    Row(
        modifier = modifier.fillMaxWidth().background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.width(82.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("READY", color = Color.White.copy(alpha = .5f), fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier.size(58.dp).clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                current?.let { PreviewCoreCanvas(it, 48) }
            }
        }

        TextButton(
            onClick = onFlipClick,
            enabled = canFlip,
            interactionSource = flipInteraction,
            modifier = Modifier.height(58.dp).graphicsLayer {
                scaleX = flipScale
                scaleY = flipScale
            }.weight(.9f).clip(RoundedCornerShape(14.dp))
                .border(1.dp, if (canFlip) Color.White.copy(alpha = .35f)
                    else Color.White.copy(alpha = .12f), RoundedCornerShape(14.dp))
                .testTag("flip_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sync, "Flip charge",
                    tint = if (canFlip) Color.White else Color.White.copy(alpha = .35f))
                Spacer(Modifier.width(6.dp))
                Text("FLIP ±", color = if (canFlip) Color.White else Color.White.copy(alpha = .35f),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(
            onClick = onDropClick,
            enabled = current != null,
            interactionSource = dropInteraction,
            modifier = Modifier.height(58.dp).graphicsLayer {
                scaleX = dropScale
                scaleY = dropScale
            }.weight(1.2f).clip(RoundedCornerShape(14.dp))
                .background(NeonCyan).testTag("drop_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDownward, "Drop core", tint = DarkSurface)
                Spacer(Modifier.width(6.dp))
                Text("DROP", color = DarkSurface, fontSize = 16.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
            }
        }
    }
}

@Composable
fun PreviewCoreCanvas(core: Core, sizeDp: Int) {
    val info = CoreLevelRegistry.getInfo(core.level)
    val color = if (core.charge == Charge.POSITIVE) info.positiveColor else info.negativeColor

    Canvas(Modifier.size(sizeDp.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * .36f

        drawCircle(color.copy(alpha = .18f), radius * 1.45f,
            androidx.compose.ui.geometry.Offset(cx, cy))
        drawCircle(color, radius, androidx.compose.ui.geometry.Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = .65f), radius, androidx.compose.ui.geometry.Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = radius * 1.15f
            isFakeBoldText = true
            color = android.graphics.Color.WHITE
        }
        val stroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = radius * 1.15f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(10, 13, 20)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
        }
        val baseline = cy - (paint.ascent() + paint.descent()) / 2f
        drawContext.canvas.nativeCanvas.drawText(core.charge.symbol, cx, baseline, stroke)
        drawContext.canvas.nativeCanvas.drawText(core.charge.symbol, cx, baseline, paint)
    }
}
