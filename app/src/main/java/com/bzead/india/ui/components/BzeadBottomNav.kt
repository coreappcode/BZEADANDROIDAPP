package com.bzead.india.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomTab {
    HOME,
    CATEGORY,
    CART,
    ACCOUNT
}

@Composable
fun BzeadBottomNav(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    cartCount: Int = 0
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bzead_bottom_nav")
    ) {
        // 1. Home
        NavigationBarItem(
            selected = currentTab == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomTab.HOME) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFEF08A),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            )
        )

        // 2. Category
        NavigationBarItem(
            selected = currentTab == BottomTab.CATEGORY,
            onClick = { onTabSelected(BottomTab.CATEGORY) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomTab.CATEGORY) Icons.Default.GridView else Icons.Outlined.GridView,
                    contentDescription = "Category",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Category",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomTab.CATEGORY) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFEF08A),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            )
        )

        // 3. Cart
        NavigationBarItem(
            selected = currentTab == BottomTab.CART,
            onClick = { onTabSelected(BottomTab.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = Color(0xFFCA8A04),
                                contentColor = Color.White
                            ) {
                                Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == BottomTab.CART) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Cart",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomTab.CART) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFEF08A),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            )
        )

        // 4. Account
        NavigationBarItem(
            selected = currentTab == BottomTab.ACCOUNT,
            onClick = { onTabSelected(BottomTab.ACCOUNT) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomTab.ACCOUNT) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Account",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomTab.ACCOUNT) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFEF08A),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B)
            )
        )
    }
}
