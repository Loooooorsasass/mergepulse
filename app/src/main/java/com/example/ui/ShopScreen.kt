package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.storage.UserGameStats

data class ThemeShopItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val accentColor: Color
)

object ShopCatalog {
    val THEMES = listOf(
        ThemeShopItem("default", "Minimal Pure White", "Clean, high-visibility lab chamber aesthetic.", 0, Color(0xFF1976D2)),
        ThemeShopItem("cyber_neon", "Soft Pastel Sage", "Calm botanical soft mint and sage tones.", 300, Color(0xFF10B981)),
        ThemeShopItem("plasma_void", "Nordic Frost Blue", "Crisp Nordic arctic ice and crystal borders.", 600, Color(0xFF2563EB)),
        ThemeShopItem("golden_core", "Warm Amber Sunset", "Soft peach, warm honey and radiant amber.", 1000, Color(0xFFD97706))
    )
}

@Composable
fun ShopScreen(
    stats: UserGameStats,
    onUnlockTheme: (themeId: String, cost: Int) -> Unit,
    onSelectTheme: (themeId: String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick, modifier = Modifier.testTag("shop_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.chamber_shop),
                            color = Color(0xFF111827),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.2.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = stringResource(R.string.cd_credits),
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.credits}",
                                color = Color(0xFF1F2937),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(ShopCatalog.THEMES) { themeItem ->
                val isUnlocked = stats.unlockedThemes.contains(themeItem.id)
                val isSelected = stats.selectedTheme == themeItem.id

                ThemeItemCard(
                    themeItem = themeItem,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    canAfford = stats.credits >= themeItem.cost,
                    onUnlockClick = { onUnlockTheme(themeItem.id, themeItem.cost) },
                    onSelectClick = { onSelectTheme(themeItem.id) }
                )
            }
        }
    }
}

@Composable
private fun ThemeItemCard(
    themeItem: ThemeShopItem,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    onUnlockClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeItem.accentColor.copy(alpha = 0.2f))
                        .border(2.dp, themeItem.accentColor, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = themeItem.name,
                        color = Color(0xFF111827),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = themeItem.description,
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.active), color = Color(0xFF2563EB), fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            } else if (isUnlocked) {
                OutlinedButton(
                    onClick = onSelectClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E7EB))
                ) {
                    Text(stringResource(R.string.equip), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = onUnlockClick,
                    enabled = canAfford,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF121212),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${themeItem.cost}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
