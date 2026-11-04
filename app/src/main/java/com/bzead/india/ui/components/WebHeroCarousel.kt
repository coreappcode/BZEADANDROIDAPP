package com.bzead.india.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bzead.india.data.HeroBanner
import kotlinx.coroutines.delay

@Composable
fun WebHeroCarousel(
    banners: List<HeroBanner>,
    onBannerClick: (HeroBanner) -> Unit,
    autoScrollDelayMs: Long = 3500L
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-swipe effect
    LaunchedEffect(banners, currentIndex) {
        delay(autoScrollDelayMs)
        currentIndex = (currentIndex + 1) % banners.size
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .testTag("web_hero_carousel")
    ) {
        // Banner Image with crossfade transition (Zero overlaid text as requested)
        Crossfade(
            targetState = currentIndex,
            animationSpec = tween(600),
            label = "BannerCrossfade"
        ) { page ->
            val banner = banners.getOrNull(page) ?: banners[0]
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = "Banner Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onBannerClick(banner) }
            )
        }

        // Left Chevron Arrow Button
        IconButton(
            onClick = {
                currentIndex = if (currentIndex > 0) currentIndex - 1 else banners.size - 1
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Banner",
                tint = Color.White
            )
        }

        // Right Chevron Arrow Button
        IconButton(
            onClick = {
                currentIndex = (currentIndex + 1) % banners.size
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Banner",
                tint = Color.White
            )
        }

        // Dot Indicators Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            banners.indices.forEach { index ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFFFEF08A) else Color.White.copy(alpha = 0.6f)
                        )
                        .clickable { currentIndex = index }
                )
            }
        }
    }
}
