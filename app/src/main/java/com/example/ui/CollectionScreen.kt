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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelData
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.Vec2

@Composable
fun CollectionScreen(
    highestLevelUnlocked: Int,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("collection_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.corepedia).uppercase(),
                            color = Color(0xFF111827),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(R.string.unlocked_cores, highestLevelUnlocked),
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(CoreLevelRegistry.LEVELS) { levelData ->
                val isUnlocked = levelData.level <= highestLevelUnlocked
                CoreCollectionCard(levelData = levelData, isUnlocked = isUnlocked)
            }
        }
    }
}

@Composable
fun CoreCollectionCard(
    levelData: CoreLevelData,
    isUnlocked: Boolean
) {
    val sampleCore = Core(
        id = levelData.level.toLong(),
        position = Vec2(0f, 0f),
        velocity = Vec2(0f, 0f),
        level = levelData.level,
        charge = Charge.POSITIVE,
        radius = 28f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isUnlocked) Color(0xFFF3F4F6) else Color(0xFFF3F4F6).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    ReadyCorePreviewCanvas(core = sampleCore, sizeDp = 68)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.cd_lock),
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.level_prefix, levelData.level),
                color = Color(0xFF2563EB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = if (isUnlocked) levelData.name else "???",
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+${levelData.baseScore} Pts",
                color = Color(0xFFD97706),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isUnlocked) levelData.description else "...",
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                maxLines = 2
            )
        }
    }
}
