package com.bzead.india.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    var selectedVariant by remember(product.variants) { mutableStateOf<ProductVariant?>(product.variants.firstOrNull()) }
    var pinCodeInput by remember { mutableStateOf("560103") }
    var pinVerified by remember { mutableStateOf(true) }

    val activePrice = product.price + (selectedVariant?.priceDelta ?: 0.0)

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
                            model = product.imageUrl,
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (product.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BzeadAccent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${product.discountPercent}% OFF",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
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
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${activePrice.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (product.mrp > activePrice) {
                                Text(
                                    text = "₹${product.mrp.toInt()}",
                                    fontSize = 15.sp,
                                    color = TextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DiscountBadgeBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Save ₹${(product.mrp - activePrice).toInt()}",
                                        color = DiscountBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inclusive of all taxes in India",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // In stock status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "In Stock",
                                tint = BzeadSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "In Stock • Ships directly from verified Indian partner",
                                fontSize = 12.sp,
                                color = BzeadSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Variants Selector if any
                if (product.variants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Option / Variant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                product.variants.forEach { v ->
                                    val isSelected = selectedVariant?.id == v.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) BzeadPrimary else Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
                                            .clickable { selectedVariant = v }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("variant_option_${v.id}")
                                    ) {
                                        Text(
                                            text = v.size ?: v.color ?: v.name,
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

                // PIN Code Delivery Estimator
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = BzeadPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivery & Logistics (India)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = pinCodeInput,
                                onValueChange = {
                                    if (it.length <= 6) pinCodeInput = it
                                },
                                label = { Text("Enter 6-Digit PIN Code", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = BzeadPrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("pincode_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (pinCodeInput.length == 6) {
                                        pinVerified = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BzeadPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("check_pincode_button")
                            ) {
                                Text("Check", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (pinVerified) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = BzeadSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Eligible for Free Delivery to $pinCodeInput within 3-4 days via Shiprocket Express.",
                                    fontSize = 12.sp,
                                    color = BzeadSuccess,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
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
                    onClick = {
                        BzeadRepository.addToCart(product, selectedVariant)
                        scope.launch {
                            snackbarHostState.showSnackbar("Added to Bag: ${product.title}")
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
                    Text(text = "Add to Bag", fontWeight = FontWeight.Bold)
                }

                // Buy Now Button
                Button(
                    onClick = {
                        BzeadRepository.addToCart(product, selectedVariant)
                        onNavigateToCart()
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

