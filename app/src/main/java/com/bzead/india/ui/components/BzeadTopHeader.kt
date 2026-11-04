package com.bzead.india.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import coil.compose.AsyncImage
import com.bzead.india.theme.BzeadHeaderNavy
import com.example.ui.splash.BZEAD_LOGO_URL

@Composable
fun BzeadTopHeader(
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    cartCount: Int,
    wishlistCount: Int,
    selectedCategory: String = "All"
) {
    val categories = listOf("All", "Mobiles", "Fashion", "Beauty", "Electronics", "Home", "Appliances", "Grocery", "Footwear")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BzeadHeaderNavy)
            .statusBarsPadding()
            .padding(bottom = 2.dp)
            .testTag("bzead_top_header")
    ) {
        // Ultra-Compact Single Row: Logo + Search Bar + Wishlist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Brand Logo Mark in Homepage Header (Transparent Background)
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clickable { onCategoryClick("All") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://res.cloudinary.com/ditsmq3r6/image/upload/v1793772394/logo_4_nuh9y9.png",
                    contentDescription = "Bzead Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.height(30.dp)
                )
            }

            // Compact Search Input
            Surface(
                onClick = onSearchClick,
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("header_search_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search products, brands...",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Wishlist Button
            IconButton(
                onClick = onWishlistClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("header_wishlist_button")
            ) {
                BadgedBox(
                    badge = {
                        if (wishlistCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ) {
                                Text("$wishlistCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Category Pills Scroll Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) Color(0xFFFEF08A) else Color(0xFF1E293B)
                        )
                        .clickable { onCategoryClick(cat) }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}

