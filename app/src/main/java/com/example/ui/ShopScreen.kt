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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storage.UserGameStats
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold

data class ThemeShopItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val accentColor: Color
)

object ShopCatalog {
    val THEMES = listOf(
        ThemeShopItem("default", "Dark Slate Deep Blue", "Classic deep space containment chamber with cyan energy borders.", 0, Color(0xFF00E5FF)),
        ThemeShopItem("cyber_neon", "Cyberpunk Neon Chamber", "Futuristic neon pink and purple high-voltage grid aesthetic.", 300, Color(0xFFFF007F)),
        ThemeShopItem("plasma_void", "Plasma Void Chamber", "Deep cosmic indigo void with electric purple particle containment.", 600, Color(0xFF7C4DFF)),
        ThemeShopItem("golden_core", "Golden Reactor Chamber", "Luxurious golden energy matrix with radiant amber light fields.", 1000, Color(0xFFFFD600))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("shop_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHAMBER SHOP",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Credit Balance Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Credits",
                            tint = NeonGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stats.credits} CREDITS",
                            color = NeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ShopCatalog.THEMES) { item ->
                val isUnlocked = stats.unlockedThemes.contains(item.id)
                val isSelected = stats.selectedTheme == item.id

                ThemeCard(
                    item = item,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    userCredits = stats.credits,
                    onUnlock = { onUnlockTheme(item.id, item.cost) },
                    onSelect = { onSelectTheme(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    item: ThemeShopItem,
    isUnlocked: Boolean,
    isSelected: Boolean,
    userCredits: Int,
    onUnlock: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.dp,
                if (isSelected) NeonCyan else item.accentColor.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        color = item.accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[ACTIVE]",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isUnlocked) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active Theme",
                        tint = NeonCyan,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    OutlinedButton(
                        onClick = onSelect,
                        modifier = Modifier.testTag("select_theme_${item.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EQUIP", color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onUnlock,
                    enabled = userCredits >= item.cost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = DarkBg,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("unlock_theme_${item.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Unlock Theme")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${item.cost}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
