package com.example.ui

import android.Manifest
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mergepulse.R
import com.example.game.engine.GameOverPhase
import com.example.game.engine.GameWorld
import com.example.game.model.CoreLevelRegistry

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
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.keep_streak),
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notification_prompt_desc),
                    color = Color(0xFF4B5563),
                    fontSize = 16.sp,
                    lineHeight = 22.sp
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF121212), contentColor = Color.White),
                    modifier = Modifier.testTag("enable_notifications_button")
                ) {
                    Text(stringResource(R.string.enable), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onNotificationPromptDismissed()
                    },
                    modifier = Modifier.testTag("dismiss_notification_prompt_button")
                ) {
                    Text(stringResource(R.string.maybe_later), color = Color(0xFF6B7280), fontSize = 14.sp)
                }
            },
            containerColor = Color.White
        )
        return
    }

    // Result Screen Overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827).copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWin) stringResource(R.string.mission_completed) else stringResource(R.string.game_over),
                color = if (isWin) Color(0xFF10B981) else Color(0xFFDC2626),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Score Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.final_score),
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%,d", world.score),
                    color = Color(0xFF111827),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )

                if (world.score >= world.bestScore && world.score > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.new_high_score),
                            color = Color(0xFFD97706),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(label = stringResource(R.string.max_combo), value = "x${world.bestCombo}")
                StatBox(label = stringResource(R.string.total_merges), value = "${world.totalMergesInGame}")
                val highestCoreInfo = CoreLevelRegistry.getInfo(world.highestLevelReached)
                StatBox(label = stringResource(R.string.best_core), value = highestCoreInfo.name)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons: MENU / RETRY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("menu_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.menu), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF121212), contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = Color(0xFF111827),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
    }
}
