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
    onPauseClick: () -> Unit,
    onOverchargeShockwave: () -> Unit,
    onOverchargeOverdrive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overchargeProgress by animateFloatAsState(
        targetValue = (world.overchargePercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "overcharge"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Row: Score, Combo Badge, Best Score, Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCORE",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format("%,d", world.score),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Combo Badge
            if (world.comboCount > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPink)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("combo_badge")
                ) {
                    Text(
                        text = "COMBO x${world.comboCount}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BEST",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format("%,d", world.bestScore),
                        color = NeonGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2X Score Overdrive Banner
        if (world.scoreOverdriveTimer > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGold)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "2X Overdrive",
                        tint = DarkSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2X OVERDRIVE ACTIVE! (${String.format("%.1fs", world.scoreOverdriveTimer)})",
                        color = DarkSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Mission Goal Tracker or Danger Alert
        if (world.mode == GameMode.MISSION && world.activeMission != null) {
            val mission = world.activeMission!!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "GOAL: ${mission.description}",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Danger Alert Banner
        AnimatedVisibility(
            visible = world.isDangerActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DangerRed.copy(alpha = 0.88f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Danger Warning",
                    tint = Color.White,
                    modifier = Modifier.scale(0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CRITICAL CHAMBER OVERFLOW DANGER!",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Overcharge Meter Bar & Action Choices
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OVERCHARGE",
                color = if (world.overchargePercent >= 100f) NeonCyan else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.width(80.dp)
            )

            if (world.overchargePercent >= 100f) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan)
                            .clickable { onOverchargeShockwave() }
                            .padding(vertical = 4.dp)
                            .testTag("shockwave_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "Shockwave",
                                tint = DarkSurface
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "SHOCKWAVE",
                                color = DarkSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonGold)
                            .clickable { onOverchargeOverdrive() }
                            .padding(vertical = 4.dp)
                            .testTag("overdrive_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "2x Overdrive",
                                tint = DarkSurface
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "2X OVERDRIVE",
                                color = DarkSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            } else {
                LinearProgressIndicator(
                    progress = { overchargeProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonCyan,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}
