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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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

    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<BzeadRepository.OrderSummaryData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var proceeding by remember { mutableStateOf(false) }

    LaunchedEffect(cartItems, selectedAddress) {
        loading = true
        loadError = null
        try {
            data = BzeadRepository.loadOrderSummary()
        } catch (e: Exception) {
            data = null
            loadError = e.message?.takeIf { it.isNotBlank() } ?: "Unable to calculate shipping charges right now."
        } finally {
            loading = false
        }
    }

    val summary = data
    val subtotal = summary?.quote?.subtotal ?: 0.0
    val shippingFee = summary?.quote?.shipping ?: 0.0
    val discount = summary?.quote?.offerDiscount ?: 0.0
    val grandTotal = summary?.quote?.total ?: 0.0

    fun proceed() {
        actionError = null
        val d = data
        val addr = selectedAddress
        when {
            loading -> actionError = "Shipping charges and delivery estimate are still refreshing. Please wait a moment."
            loadError != null -> actionError = loadError
            d == null || d.quote.total <= 0 -> actionError = "Pricing is still loading. Please wait a moment and try again."
            d.missingWeightNames.isNotEmpty() ->
                actionError = "Some products are missing package weight information required for accurate shipping. Please contact the seller."
            !d.serviceable -> actionError = com.bzead.india.data.CheckoutApi.PINCODE_NOT_SERVICEABLE
            addr != null && BzeadRepository.minimumOrderMessage(addr, d.quote) != null ->
                actionError = BzeadRepository.minimumOrderMessage(addr, d.quote)
            else -> {
                proceeding = true
                scope.launch {
                    try {
                        val stockError = BzeadRepository.recheckStock(d.items)
                        if (stockError != null) actionError = stockError else onProceedToPayment()
                    } catch (e: Exception) {
                        actionError = "Could not verify stock availability. Please try again."
                    } finally {
                        proceeding = false
                    }
                }
            }
        }
    }

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
                                text = "Items (${summary?.items?.size ?: cartItems.size})",
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

                    val shownItems = summary?.items.orEmpty()
                    if (loading && summary == null) {
                        Text("Calculating shipping charges and delivery estimate...", fontSize = 12.sp, color = TextSecondary)
                    }
                    shownItems.forEachIndexed { idx, item ->
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
                                    model = item.productImage,
                                    contentDescription = item.productName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Qty: ${item.quantity} · ${fmtInr(item.unitPrice)} each",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "₹${java.text.DecimalFormat("#,##0.##").format(item.unitPrice * item.quantity)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F1111)
                            )
                        }

                        if (idx < shownItems.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            summary?.estimatedDeliveryDate?.let { eta ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = BzeadPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Estimated Delivery", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(eta, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            if (summary != null && summary.missingWeightNames.isNotEmpty()) {
                Text(
                    "Missing package weight for: ${summary.missingWeightNames.joinToString(", ")}. Shipping cost may be inaccurate.",
                    fontSize = 12.sp, color = Color(0xFFB45309)
                )
            }
            if (summary != null && !summary.serviceable) {
                Text(com.bzead.india.data.CheckoutApi.PINCODE_NOT_SERVICEABLE, fontSize = 12.sp, color = Color(0xFFB91C1C))
            }
            loadError?.let { Text(it, fontSize = 12.sp, color = Color(0xFFB91C1C)) }

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
                    PriceSummaryRow(label = "Items Subtotal", value = fmtInr(subtotal))
                    PriceSummaryRow(
                        label = "Shipping Charges",
                        value = if (summary == null) "—" else fmtInr(shippingFee)
                    )
                    if (discount > 0.0) {
                        PriceSummaryRow(label = "Offer Discount", value = "-${fmtInr(discount)}", isDiscount = true)
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
                            text = if (summary == null) "—" else fmtInr(grandTotal),
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
                actionError?.let {
                    Text(it, fontSize = 12.sp, color = Color(0xFFB91C1C), modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = { proceed() },
                    enabled = !proceeding,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD814)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("proceed_to_payment_btn")
                ) {
                    Text(
                        text = if (proceeding) "Checking availability..." else "Proceed to Payment (${fmtInr(grandTotal)}) →",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1111),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

private fun fmtInr(v: Double): String = "₹" + java.text.DecimalFormat("#,##0.##").format(v)
