package com.bzead.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bzead.india.data.BzeadRepository
import com.bzead.india.theme.BzeadHeaderNavy
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.BzeadSuccess
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(
    onNavigateBack: () -> Unit,
    onProceedToPayment: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by BzeadRepository.cartItems.collectAsState()
    val selectedAddress by BzeadRepository.selectedAddress.collectAsState()

    var isExpressShipping by remember { mutableStateOf(false) }
    var selectedPaymentOption by remember { mutableStateOf("ONLINE") } // "ONLINE" or "COD"

    val subtotal = cartItems.sumOf { it.totalPrice }
    val baseShipping = if (subtotal >= 499.0) 0.0 else 40.0
    val shippingFee = if (isExpressShipping) baseShipping + 99.0 else baseShipping
    val discount = if (subtotal >= 2000.0) 150.0 else 0.0
    val grandTotal = (subtotal + shippingFee - discount).coerceAtLeast(0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Review Order Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Step 2 of 3 · Verify & Select Payment",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BzeadHeaderNavy,
                titleContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Delivery Address Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BzeadPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivering To",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Change",
                            color = BzeadPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { onNavigateBack() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedAddress != null) {
                        Text(
                            text = selectedAddress!!.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${selectedAddress!!.formatted} · Phone: ${selectedAddress!!.phoneNumber}",
                            fontSize = 12.5.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "No address selected. Please tap Change to add delivery address.",
                            fontSize = 12.5.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }

            // Items Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = BzeadPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Items (${cartItems.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Edit Cart",
                            color = BzeadPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { onNavigateToCart() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    cartItems.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                AsyncImage(
                                    model = item.product.imageUrl,
                                    contentDescription = item.product.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Qty: ${item.quantity} · ₹${item.unitPrice.toInt()} each",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "₹${item.totalPrice.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F1111)
                            )
                        }

                        if (idx < cartItems.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            // Shipping Tier Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            text = "Delivery Speed & Carrier",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (!isExpressShipping) BzeadPrimary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .background(if (!isExpressShipping) Color(0xFFEFF6FF) else Color.White)
                            .clickable { isExpressShipping = false }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isExpressShipping,
                            onClick = { isExpressShipping = false },
                            colors = RadioButtonDefaults.colors(selectedColor = BzeadPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Standard Shipping (Shiprocket)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(text = "Est. 2-4 business days across India", fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            text = if (baseShipping == 0.0) "FREE" else "₹40",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (baseShipping == 0.0) BzeadSuccess else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isExpressShipping) BzeadPrimary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .background(if (isExpressShipping) Color(0xFFEFF6FF) else Color.White)
                            .clickable { isExpressShipping = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isExpressShipping,
                            onClick = { isExpressShipping = true },
                            colors = RadioButtonDefaults.colors(selectedColor = BzeadPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Express Air Priority (Delhivery)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FASTEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C), modifier = Modifier.background(Color(0xFFFFEDD5), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Text(text = "Priority Air dispatch, 1-2 business days", fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            text = "+₹99",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }
            }



            // Price Details Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Price Details Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PriceSummaryRow(label = "Items Subtotal", value = "₹${subtotal.toInt()}")
                    PriceSummaryRow(
                        label = "Shipping Charges",
                        value = if (shippingFee == 0.0) "FREE" else "₹${shippingFee.toInt()}",
                        isFree = shippingFee == 0.0
                    )
                    if (discount > 0.0) {
                        PriceSummaryRow(label = "Festive Offer Discount", value = "-₹${discount.toInt()}", isDiscount = true)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${grandTotal.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFFB12704)
                        )
                    }
                }
            }
        }

        // Bottom CTA
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = onProceedToPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD814)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("proceed_to_payment_btn")
                ) {
                    Text(
                        text = "Proceed to Payment (₹${grandTotal.toInt()}) →",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1111),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
