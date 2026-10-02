package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mergepulse.R
import com.example.game.engine.GameWorld

@Composable
fun HudHeader(
    world: GameWorld,
    uiTick: Long,
    onMenuClick: () -> Unit,
    onOverchargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overchargeProgress by animateFloatAsState(
        targetValue = (world.overchargePercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "overcharge"
    )

    val isReady = world.overchargePercent >= 100f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: SCORE
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.score),
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format("%,d", world.score),
                    color = Color(0xFF111827),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // CENTER: OVERCHARGE METER
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .clickable(enabled = isReady) { onOverchargeClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isReady) stringResource(R.string.overcharge_ready) else stringResource(R.string.overcharge),
                    color = if (isReady) Color(0xFF1976D2) else Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = if (isReady) FontWeight.Black else FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { overchargeProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF1976D2),
                    trackColor = Color(0xFFEEEEEE)
                )
            }

            // RIGHT: MENU ICON
            Box(
                modifier = Modifier.weight(0.7f),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.cd_menu),
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
