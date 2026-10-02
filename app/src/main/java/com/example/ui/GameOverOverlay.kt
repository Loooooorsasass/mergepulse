package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.game.engine.GameOverPhase
import com.example.game.engine.GameWorld
import com.example.game.model.CoreLevelRegistry
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold

@Composable
fun GameOverDialog(
    world: GameWorld,
    onPlayAgain: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationPromptDismissed: () -> Unit
) {
    val context = LocalContext.current
    val isWin = world.state == com.example.game.engine.GameState.MISSION_COMPLETE
    val fsmPhase = world.gameOverFsm.phase

    // One-time lifetime notification permission launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onNotificationPromptDismissed()
    }

    // 1-Time Lifetime Notification Dialog
    if (fsmPhase == GameOverPhase.PERMISSION_PROMPT) {
        AlertDialog(
            onDismissRequest = {
                onNotificationPromptDismissed()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Notifications",
                    tint = NeonCyan
                )
            },
            title = {
                Text(
                    text = "Keep Your Daily Streak!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Enable notifications to receive daily streak alerts, new daily missions, and claim bonus Energy Credits.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val isGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!isGranted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onNotificationPromptDismissed()
                            }
                        } else {
                            onNotificationPromptDismissed()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBg),
                    modifier = Modifier.testTag("enable_notifications_button")
                ) {
                    Text("ENABLE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onNotificationPromptDismissed()
                    },
                    modifier = Modifier.testTag("dismiss_notification_prompt_button")
                ) {
                    Text("MAYBE LATER", color = Color.Gray)
                }
            },
            containerColor = DarkSurface
        )
        return
    }

    // Result Screen Overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.88f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(2.dp, if (isWin) NeonCyan else DangerRed, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWin) "MISSION COMPLETED!" else "GAME OVER",
                color = if (isWin) NeonCyan else DangerRed,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Score Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FINAL SCORE",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format("%,d", world.score),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )

                if (world.score >= world.bestScore && world.score > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "New High Score",
                            tint = NeonGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NEW HIGH SCORE!",
                            color = NeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(label = "MAX COMBO", value = "x${world.bestCombo}")
                StatBox(label = "MERGES", value = "${world.totalMergesInGame}")
                val highestCoreInfo = CoreLevelRegistry.getInfo(world.highestLevelReached)
                StatBox(label = "BEST CORE", value = highestCoreInfo.name)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons: RETRY / MENU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("menu_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home Menu")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MENU")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Play Again")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RETRY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
