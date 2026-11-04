package com.bzead.india.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bzead.india.data.BzeadRepository
import com.bzead.india.model.CartItem
import com.bzead.india.theme.BzeadHeaderNavy
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.BzeadSuccess
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary
import com.bzead.india.ui.components.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by BzeadRepository.cartItems.collectAsState()
    val isCartLoading by BzeadRepository.isCartLoading.collectAsState()
    val isLoggedIn by BzeadRepository.isLoggedIn.collectAsState()

    var selectedItemIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmRemoveCartItemId by remember { mutableStateOf<String?>(null) }

    // Sync selected items whenever cartItems list updates
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            BzeadRepository.syncCartWithBackend()
        }
    }

    LaunchedEffect(cartItems) {
        val currentIds = cartItems.map { it.id }.toSet()
        if (selectedItemIds.isEmpty() || !currentIds.containsAll(selectedItemIds)) {
            selectedItemIds = currentIds
        }
    }

    val isAllSelected = cartItems.isNotEmpty() && selectedItemIds.size == cartItems.size
    val selectedCartItems = cartItems.filter { it.id in selectedItemIds }
    val payableSelectedItems = selectedCartItems.filter { it.product.inStock && it.product.stockCount > 0 }
    val displaySubtotal = payableSelectedItems.sumOf { it.totalPrice }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar matching Web App Header & Cart Page
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shopping Cart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${cartItems.size} item${if (cartItems.size != 1) "s" else ""} in your cart",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BzeadHeaderNavy,
                    titleContentColor = Color.White
                )
            )

            if (isCartLoading && cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BzeadPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading your cart...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else if (cartItems.isEmpty()) {
                // Empty Cart view matching web CartPage.tsx
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveShoppingCart,
                                contentDescription = "Empty Cart",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Cart is Empty",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F1111)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add items to your cart to get started with shopping.",
                                fontSize = 13.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToHome,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD814)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("empty_cart_continue_btn")
                            ) {
                                Text(
                                    text = "Continue Shopping",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F1111),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Select All Row (matching Web App CartPage.tsx)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        selectedItemIds = if (isAllSelected) emptySet() else cartItems.map { it.id }.toSet()
                                    }
                                ) {
                                    Checkbox(
                                        checked = isAllSelected,
                                        onCheckedChange = { checked ->
                                            selectedItemIds = if (checked) cartItems.map { it.id }.toSet() else emptySet()
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFFF9900),
                                            uncheckedColor = Color(0xFF94A3B8)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Select All Items",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF555555)
                                    )
                                }

                                Text(
                                    text = if (isAllSelected) "Deselect all" else "${selectedItemIds.size} selected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007185),
                                    modifier = Modifier
                                        .clickable {
                                            selectedItemIds = if (isAllSelected) emptySet() else cartItems.map { it.id }.toSet()
                                        }
                                        .padding(6.dp)
                                )
                            }
                        }
                    }

                    // Cart Items connected to real backend & state
                    items(cartItems, key = { it.id }) { item ->
                        val isSelected = selectedItemIds.contains(item.id)
                        CartItemRow(
                            cartItem = item,
                            isSelected = isSelected,
                            onToggleSelect = {
                                selectedItemIds = if (isSelected) {
                                    selectedItemIds - item.id
                                } else {
                                    selectedItemIds + item.id
                                }
                            },
                            onProductClick = { onNavigateToProduct(item.product.id) },
                            onIncrement = {
                                val currentQty = item.quantity
                                if (currentQty < item.product.stockCount) {
                                    BzeadRepository.updateCartQuantity(item.id, currentQty + 1)
                                }
                            },
                            onDecrement = {
                                val currentQty = item.quantity
                                if (currentQty <= 1) {
                                    confirmRemoveCartItemId = item.id
                                } else {
                                    BzeadRepository.updateCartQuantity(item.id, currentQty - 1)
                                }
                            },
                            onRemove = { confirmRemoveCartItemId = item.id },
                            onMoveToWishlist = {
                                BzeadRepository.toggleWishlist(item.product.id)
                                BzeadRepository.removeFromCart(item.id)
                            }
                        )
                    }

                    // Cart Summary Card matching Web App CartPage.tsx
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cart Summary",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F1111)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                PriceSummaryRow(
                                    label = "Subtotal (${payableSelectedItems.size} item${if (payableSelectedItems.size != 1) "s" else ""})",
                                    value = if (payableSelectedItems.isNotEmpty()) "₹${displaySubtotal.toInt()}" else "—"
                                )

                                PriceSummaryRow(
                                    label = "Delivery Fee",
                                    value = "Calculated at checkout"
                                )

                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFDDD000).copy(alpha = 0.2f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Estimated Total",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F1111)
                                    )
                                    Text(
                                        text = if (payableSelectedItems.isNotEmpty()) "₹${displaySubtotal.toInt()}" else "—",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB12704)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        BzeadRepository.setCheckoutSelection(payableSelectedItems.map { it.id }.toSet())
                                        onProceedToCheckout()
                                    },
                                    enabled = payableSelectedItems.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFD814),
                                        disabledContainerColor = Color(0xFFE2E8F0)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("proceed_checkout_btn")
                                ) {
                                    Text(
                                        text = "Proceed to Checkout →",
                                        fontWeight = FontWeight.Bold,
                                        color = if (payableSelectedItems.isNotEmpty()) Color(0xFF0F1111) else Color(0xFF94A3B8),
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = onNavigateToHome,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                ) {
                                    Text(
                                        text = "Continue Shopping",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF007185)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Confirm Remove Dialog (matching Web App CartPage.tsx modal)
        if (confirmRemoveCartItemId != null) {
            val itemToRemove = cartItems.find { it.id == confirmRemoveCartItemId }
            AlertDialog(
                onDismissRequest = { confirmRemoveCartItemId = null },
                title = {
                    Text(
                        text = "Remove Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F1111)
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${itemToRemove?.product?.title ?: "this item"}\" from your cart?",
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmRemoveCartItemId?.let { BzeadRepository.removeFromCart(it) }
                            confirmRemoveCartItemId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB12704)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Remove", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { confirmRemoveCartItemId = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F1111))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}


@Composable
fun CartItemRow(
    cartItem: CartItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onProductClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    onMoveToWishlist: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox matching Web App CartPage.tsx
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFF9900),
                        uncheckedColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Product Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9F9F9))
                        .border(0.5.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                        .clickable { onProductClick() }
                ) {
                    if (!cartItem.product.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = cartItem.product.imageUrl,
                            contentDescription = cartItem.product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (cartItem.product.drawableRes != null) {
                        Image(
                            painter = painterResource(id = cartItem.product.drawableRes),
                            contentDescription = cartItem.product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title & Price Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = cartItem.product.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F1111),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onProductClick() }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "₹${(cartItem.unitPrice * cartItem.quantity).toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB12704)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Brand / Seller
                    Text(
                        text = "Sold by: ${cartItem.product.brand.ifBlank { "BZEAD Seller" }}",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )

                    // Size / Color / Variant Details
                    val variantDetails = listOfNotNull(
                        cartItem.selectedSize?.let { "Size: $it" },
                        cartItem.selectedColor?.let { "Color: $it" }
                    ).joinToString(" · ")

                    if (variantDetails.isNotBlank()) {
                        Text(
                            text = variantDetails,
                            fontSize = 11.sp,
                            color = Color(0xFF888888)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stock Status Badge
                    if (cartItem.product.inStock && cartItem.product.stockCount > 0) {
                        Text(
                            text = "In Stock",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            modifier = Modifier
                                .background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Out of Stock",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quantity Stepper + Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuantityStepper(
                            quantity = cartItem.quantity,
                            onIncrement = onIncrement,
                            onDecrement = onDecrement
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Move to Wishlist",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007185),
                                modifier = Modifier
                                    .clickable { onMoveToWishlist() }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )

                            Text(
                                text = "|",
                                fontSize = 11.sp,
                                color = Color(0xFFDDD000),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Item",
                                    tint = Color(0xFF0F1111),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriceSummaryRow(
    label: String,
    value: String,
    isFree: Boolean = false,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.5.sp, color = Color(0xFF555555))
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFree || isDiscount) BzeadSuccess else Color(0xFF0F1111)
        )
    }
}
