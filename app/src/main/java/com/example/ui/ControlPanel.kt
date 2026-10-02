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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NEXT Preview Card
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NEXT",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                world.nextCore?.let { core ->
                    PreviewCoreCanvas(core = core, sizeDp = 44)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // FLIP ± Button with Flip Energy Gauge
        val currentCharge = world.currentCore?.charge ?: Charge.POSITIVE
        val chargeColor = if (currentCharge == Charge.POSITIVE) NeonPink else NeonCyan
        val isFlipReady = world.flipEnergy >= 1.0f

        Button(
            onClick = onFlipClick,
            enabled = isFlipReady,
            modifier = Modifier
                .height(54.dp)
                .weight(1f)
                .testTag("flip_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurfaceVariant,
                disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isFlipReady) chargeColor else Color.Gray)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Flip Charge",
                        tint = if (isFlipReady) chargeColor else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FLIP ${currentCharge.symbol}",
                        color = if (isFlipReady) Color.White else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Energy gauge indicator (3 segments)
                LinearProgressIndicator(
                    progress = { (world.flipEnergy / world.MAX_FLIP_ENERGY).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = chargeColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // DROP Button
        Button(
            onClick = onDropClick,
            modifier = Modifier
                .height(54.dp)
                .weight(1.2f)
                .testTag("drop_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DarkSurface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Drop Core",
                    tint = DarkSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DROP",
                    color = DarkSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PreviewCoreCanvas(core: Core, sizeDp: Int) {
    val levelInfo = CoreLevelRegistry.getInfo(core.level)
    val color = if (core.charge == Charge.POSITIVE) levelInfo.positiveColor else levelInfo.negativeColor

    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * 0.4f

        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius * 1.2f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        drawCircle(
            color = color,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = radius * 0.8f,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}
