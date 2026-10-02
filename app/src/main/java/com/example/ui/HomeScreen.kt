package com.example.ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mergepulse.R
import com.example.storage.UserGameStats

@Composable
fun HomeScreen(
    stats: UserGameStats,
    onPlayEndless: () -> Unit,
    onMissionsClick: () -> Unit,
    onCollectionClick: () -> Unit,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReplayTutorialClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Daily Streak & Credit Balance Cards (Localized + Large text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = stringResource(R.string.streak_days, stats.dailyStreak),
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = stringResource(R.string.cd_credits),
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.credits_count, stats.credits),
                            color = Color(0xFF1F2937),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Dual-Core Logo
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    drawCircle(
                        color = Color(0xFF90CAF9).copy(alpha = 0.35f),
                        radius = (size.width * 0.44f) * pulseScale,
                        style = Stroke(width = 3.5f)
                    )

                    drawCircle(
                        color = Color(0xFFFFAB91),
                        radius = size.width * 0.28f,
                        center = Offset(cx - 18f, cy - 10f)
                    )
                    drawCircle(
                        color = Color(0xFFE59881),
                        radius = size.width * 0.28f,
                        center = Offset(cx - 18f, cy - 10f),
                        style = Stroke(width = 2.5f)
                    )

                    drawCircle(
                        color = Color(0xFF90CAF9),
                        radius = size.width * 0.28f,
                        center = Offset(cx + 18f, cy + 10f)
                    )
                    drawCircle(
                        color = Color(0xFF7CA9DC),
                        radius = size.width * 0.28f,
                        center = Offset(cx + 18f, cy + 10f),
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = Color(0xFF111827),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = stringResource(R.string.tagline),
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Best Score Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = stringResource(R.string.cd_trophy),
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.personal_best),
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%,d", stats.bestScore),
                                color = Color(0xFF111827),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.max_combo),
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "x${stats.bestCombo}",
                            color = Color(0xFF2563EB),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Main CTA: PLAY GAME (Solid Black Pill)
            Button(
                onClick = onPlayEndless,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("play_endless_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF121212),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play_game),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.play_game),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Actions
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onMissionsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("missions_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = stringResource(R.string.cd_missions),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.missions).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onShopClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("shop_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = stringResource(R.string.shop),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.shop),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCollectionClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("collection_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = stringResource(R.string.cd_corepedia),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.corepedia).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("settings_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.settings).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onReplayTutorialClick,
                modifier = Modifier.testTag("replay_tutorial_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = stringResource(R.string.replay_tutorial),
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.replay_tutorial),
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
