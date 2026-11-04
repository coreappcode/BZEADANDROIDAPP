package com.bzead.india.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bzead.india.data.BzeadRepository
import com.bzead.india.data.VariantSelection
import com.bzead.india.model.ProductVariant
import com.bzead.india.theme.BzeadAccent
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.BzeadSuccess
import com.bzead.india.theme.DiscountBadgeBg
import com.bzead.india.theme.DiscountBadgeText
import com.bzead.india.theme.StarGold
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWriteReview: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddresses: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val catalog by BzeadRepository.allProducts.collectAsState()
    val product = remember(productId, catalog) { catalog.firstOrNull { it.id == productId } }
    androidx.compose.runtime.LaunchedEffect(productId) { BzeadRepository.loadProductDetail(productId) }
    androidx.compose.runtime.LaunchedEffect(productId) { BzeadRepository.loadReviews(productId) }
    val reviewsByProduct by BzeadRepository.reviews.collectAsState()
    val productReviews = reviewsByProduct[productId].orEmpty()
    val wishlistIds by BzeadRepository.wishlistProductIds.collectAsState()
    val scope = rememberCoroutineScope()

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Product not found", color = TextSecondary)
        }
        return
    }

    val isWishlisted = wishlistIds.contains(product.id)
    val isLoggedIn by BzeadRepository.isLoggedIn.collectAsState()
    val sizes = remember(product.variants) { VariantSelection.availableSizes(product.variants) }
    val colors = remember(product.variants) { VariantSelection.availableColors(product.variants) }
    val colorHex = remember(product.variants) { VariantSelection.colorHexByName(product.variants) }
    val requiresSize = sizes.size > 1
    val requiresColor = colors.size > 1
    var selectedSize by remember(product.variants) { mutableStateOf("") }
    var selectedColor by remember(product.variants) { mutableStateOf("") }
    var galleryIndex by remember(product.id) { mutableStateOf(0) }

    // Web effects: default to the first in-stock size/colour; clear a selection that is no longer offered.
    androidx.compose.runtime.LaunchedEffect(sizes) {
        if (selectedSize.isEmpty() && sizes.isNotEmpty()) selectedSize = VariantSelection.preferredSize(product.variants, sizes)
        else if (sizes.size > 1 && selectedSize.isNotEmpty() && !sizes.contains(selectedSize)) selectedSize = ""
    }
    androidx.compose.runtime.LaunchedEffect(colors) {
        if (selectedColor.isEmpty() && colors.isNotEmpty()) selectedColor = VariantSelection.preferredColor(product.variants, colors)
        else if (colors.size > 1 && selectedColor.isNotEmpty() && !colors.contains(selectedColor)) selectedColor = ""
    }

    val currentVariant = VariantSelection.currentVariant(product.variants, selectedSize, selectedColor, requiresSize, requiresColor)
    val effectiveStock = if (currentVariant != null) currentVariant.stock else product.stockCount
    val inStock = effectiveStock > 0

    var rpcPrice by remember(product.id) { mutableStateOf<Pair<Double, Double>?>(null) }
    androidx.compose.runtime.LaunchedEffect(product.id, selectedSize, selectedColor, product.variants) {
        val override = VariantSelection.pricingVariant(product.variants, selectedSize, selectedColor)?.price
        com.bzead.india.data.SupabaseClient.fetchVariantPublicPrice(product.id, override)?.let { if (it.first > 0) rpcPrice = it }
    }
    val publicUnitPrice = rpcPrice?.first ?: currentVariant?.price ?: product.price
    val effectiveMrp = rpcPrice?.second?.takeIf { it > 0 } ?: product.mrp
    val discountPercent = if (effectiveMrp > publicUnitPrice) Math.round((effectiveMrp - publicUnitPrice) / effectiveMrp * 100).toInt() else 0
    val gallery = remember(product, currentVariant, selectedColor) { VariantSelection.galleryImages(product, currentVariant, selectedColor) }
    androidx.compose.runtime.LaunchedEffect(selectedSize, selectedColor) { galleryIndex = 0 }
    val canBuy = inStock && (!requiresSize || selectedSize.isNotEmpty()) && (!requiresColor || selectedColor.isNotEmpty())

    fun addSelectionToCart() {
        BzeadRepository.addToCart(
            product, selectedSize.ifEmpty { null }, selectedColor.ifEmpty { null },
            currentVariant?.id?.trim()?.ifEmpty { null }, currentVariant?.sku?.trim()?.ifEmpty { null }, publicUnitPrice
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Product link copied: https://www.bzead.in/products/${product.id}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { BzeadRepository.toggleWishlist(product.id) },
                        modifier = Modifier.testTag("pdp_wishlist_button")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Color(0xFFEF4444) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BzeadPrimary,
                    titleContentColor = Color.White
                )
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp)
            ) {
                // Product Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.drawableRes != null) {
                        Image(
                            painter = painterResource(id = product.drawableRes),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = gallery.getOrNull(galleryIndex) ?: product.imageUrl,
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                }

                if (gallery.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gallery.forEachIndexed { i, url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(if (i == galleryIndex) 2.dp else 1.dp, if (i == galleryIndex) BzeadPrimary else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                    .clickable { galleryIndex = i }
                            )
                        }
                    }
                }

                // Title & Pricing Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.brand,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BzeadPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating Bar
                        if (product.rating > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BzeadSuccess)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format("%.1f", product.rating),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.reviewCount} Ratings & Verified Reviews",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Price
                        if (effectiveMrp > publicUnitPrice) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "MRP:", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${formatInr(effectiveMrp)}",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                if (discountPercent > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "($discountPercent% OFF)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFCC0C39)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${formatInr(publicUnitPrice)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Selling Price", fontSize = 12.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inclusive of all taxes",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // Stock status
                        val stockLabel = if (!inStock) "Out Of Stock" else if (effectiveStock < 10) "Limited Stock" else "In Stock"
                        val stockColor = if (!inStock) Color(0xFFEF4444) else if (effectiveStock < 10) Color(0xFF3B82F6) else Color(0xFF22C55E)
                        Text(
                            text = stockLabel,
                            fontSize = 12.sp,
                            color = stockColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("pdp_stock_status")
                        )
                        if (!inStock) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This Product Is Currently Out Of Stock.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                // Size / colour selection (web: colour circles, size buttons)
                if (colors.isNotEmpty() || sizes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (colors.isNotEmpty()) {
                                Text(
                                    text = if (selectedColor.isNotEmpty()) "Colour: $selectedColor" else "Colour",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    colors.forEach { name ->
                                        val isSelected = selectedColor == name
                                        val hex = colorHex[name.lowercase()] ?: "#9CA3AF"
                                        val swatch = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF9CA3AF))
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) BzeadPrimary else Color(0xFFCBD5E1),
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(swatch)
                                                .clickable { selectedColor = name }
                                                .testTag("color_option_$name")
                                        )
                                    }
                                }
                            }
                            if (colors.isNotEmpty() && sizes.isNotEmpty()) Spacer(modifier = Modifier.height(14.dp))
                            if (sizes.isNotEmpty()) {
                                Text(
                                    text = if (selectedSize.isNotEmpty()) "Size: $selectedSize" else "Size",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sizes.forEach { size ->
                                        val isSelected = selectedSize == size
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) BzeadPrimary else Color(0xFFCBD5E1),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
                                                .clickable { selectedSize = size }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("size_option_$size")
                                        ) {
                                            Text(
                                                text = size,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) BzeadPrimary else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                product.offerSummary?.let { offer ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFF2F16C7), RoundedCornerShape(2.dp)),
                        color = Color.White
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2F16C7)).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text("OFFER SECTION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(offer, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111111), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                }

                // Delivery estimate (web DeliveryEstimate)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DeliveryEstimateBox(product = product, onAddNewAddress = onNavigateToAddresses)
                    }
                }

                // Description & Specifications
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )

                        if (product.specifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Specifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            product.specifications.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Reviews Section
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer reviews",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val averageRating = if (product.rating > 0f) product.rating.toDouble()
                        else if (productReviews.isNotEmpty()) Math.round(productReviews.sumOf { it.rating.toDouble() } / productReviews.size * 10) / 10.0
                        else 0.0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { starIndex ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (starIndex < Math.round(averageRating)) StarGold else Color(0xFFD1D5DB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(String.format("%.1f out of 5", averageRating), fontSize = 15.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${productReviews.size} global ratings", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(5, 4, 3, 2, 1).forEach { starValue ->
                            val count = productReviews.count { it.rating == starValue }
                            val pct = if (productReviews.isNotEmpty()) Math.round(count * 100.0 / productReviews.size).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$starValue star", fontSize = 13.sp, color = BzeadPrimary, modifier = Modifier.width(54.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(18.dp)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFD1D5DB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pct / 100f)
                                            .fillMaxHeight()
                                            .background(Color(0xFFFF6A00))
                                    )
                                }
                                Text("$pct%", fontSize = 13.sp, color = BzeadPrimary, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Review this product", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Share your thoughts with other customers", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onNavigateToWriteReview(productId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Write a product review", color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (productReviews.isEmpty()) {
                            Text("No reviews yet. Be the first to review.", fontSize = 14.sp, color = TextSecondary)
                        } else {
                            productReviews.take(4).forEach { review ->
                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF3F4F6))
                                                .border(1.dp, Color(0xFFD1D5DB), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(review.author.take(1).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(review.author, fontSize = 13.sp, color = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < review.rating) StarGold else Color(0xFFD1D5DB),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = review.heading.ifBlank { "Verified Purchase" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reviewed on ${review.date}", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(review.comment, fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                                    review.images.firstOrNull()?.let { img ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AsyncImage(
                                            model = img,
                                            contentDescription = "Review",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(2.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("0 people found this helpful", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            if (productReviews.size > 4) {
                                Text("See more reviews", fontSize = 13.sp, color = BzeadPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add to Cart Button
                Button(
                    enabled = canBuy,
                    onClick = {
                        if (!isLoggedIn) {
                            onNavigateToLogin()
                        } else {
                            addSelectionToCart()
                            scope.launch {
                                snackbarHostState.showSnackbar("Product added to cart")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pdp_add_to_cart_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BzeadPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add To Cart", fontWeight = FontWeight.Bold)
                }

                // Buy Now Button
                Button(
                    enabled = canBuy,
                    onClick = {
                        if (!isLoggedIn) {
                            onNavigateToLogin()
                        } else {
                            addSelectionToCart()
                            onNavigateToCart()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pdp_buy_now_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = BzeadAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Buy Now",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


private fun formatInr(value: Double): String =
    if (value % 1.0 == 0.0) String.format(java.util.Locale("en", "IN"), "%,d", value.toLong())
    else String.format(java.util.Locale("en", "IN"), "%,.2f", value)
