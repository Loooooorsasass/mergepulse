package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameMode
import com.example.game.engine.GameWorld
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPink

@Composable
fun HudHeader(
    world: GameWorld,
    onMenuClick: () -> Unit,
    onOverchargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = (world.overchargePercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "overcharge_meter"
    )
    val ready = world.overchargePercent >= 100f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(88.dp)) {
            Text("SCORE", color = Color.White.copy(alpha = .55f), fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("%,d".format(world.score), color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = ready, onClick = onOverchargeClick)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("OVERCHARGE", color = if (ready) NeonCyan else Color.White.copy(alpha = .55f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                Text(if (ready) "READY" else "${world.overchargePercent.toInt()}%",
                    color = if (ready) NeonCyan else Color.White.copy(alpha = .55f),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
                    .testTag("overcharge_meter"),
                color = if (ready) NeonCyan else NeonGold,
                trackColor = Color.White.copy(alpha = .09f)
            )
        }

        Spacer(Modifier.width(12.dp))
        IconButton(onClick = onMenuClick, modifier = Modifier.size(42.dp).testTag("menu_button")) {
            Icon(Icons.Default.MoreVert, "Game menu", tint = Color.White)
        }
    }
}
