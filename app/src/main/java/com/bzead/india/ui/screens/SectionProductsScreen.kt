package com.bzead.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bzead.india.data.BzeadRepository
import com.bzead.india.ui.components.WebProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionProductsScreen(
    sectionId: String,
    onNavigateBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val wishlistIds by BzeadRepository.wishlistProductIds.collectAsState()
    val sectionTitle = remember(sectionId) {
        when (sectionId.lowercase()) {
            "featured" -> "Featured Collections"
            "hot-deals", "deals" -> "Hot Deals & Offers"
            "trending" -> "Trending Products"
            else -> sectionId.replace("-", " ").replaceFirstChar { it.uppercase() }
        }
    }
    
    val sectionMap by BzeadRepository.sectionProducts.collectAsState()
    val featured by BzeadRepository.featuredProducts.collectAsState()
    val trending by BzeadRepository.trendingProducts.collectAsState()
    val hotDeals by BzeadRepository.hotDealsProducts.collectAsState()
    androidx.compose.runtime.LaunchedEffect(sectionId) { BzeadRepository.loadSection(sectionId) }
    val allSectionProducts = remember(sectionId, sectionMap, featured, trending, hotDeals) {
        BzeadRepository.getProductsBySection(sectionId)
    }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts = remember(searchQuery, allSectionProducts) {
        if (searchQuery.isBlank()) {
            allSectionProducts
        } else {
            allSectionProducts.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.brand.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sectionTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Search Bar
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search in $sectionTitle...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No products found in this section.",
                        color = Color(0xFF64748B),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        WebProductCard(
                            product = product,
                            onProductClick = { onProductClick(product.id) },
                            onWishlistToggle = { BzeadRepository.toggleWishlist(product.id) },
                            isInWishlist = wishlistIds.contains(product.id),
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }
}
