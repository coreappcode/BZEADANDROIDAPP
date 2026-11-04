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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bzead.india.data.BzeadRepository
import com.bzead.india.model.DeliveryAddress
import com.bzead.india.theme.BzeadHeaderNavy
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary
import com.bzead.india.data.CheckoutState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by BzeadRepository.cartItems.collectAsState()
    val addresses by BzeadRepository.addresses.collectAsState()
    val selectedAddress by BzeadRepository.selectedAddress.collectAsState()

    val activeAddress = selectedAddress ?: addresses.firstOrNull()

    var sameAsShipping by remember { mutableStateOf(true) }

    var billingStreet by remember { mutableStateOf("") }
    var billingCity by remember { mutableStateOf("") }
    var billingState by remember { mutableStateOf("") }
    var billingPinCode by remember { mutableStateOf("") }

    remember { BzeadRepository.resetCheckoutState(); true }
    val checkoutState by BzeadRepository.checkoutState.collectAsState()
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity

    var prepared by remember { mutableStateOf<BzeadRepository.PreparedPayment?>(null) }
    var preparing by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            prepared = BzeadRepository.prepareRazorpayPayment()
        } catch (e: Exception) {
            errorMessage = e.message?.takeIf { it.isNotBlank() }
                ?: "Unable to load backend checkout configuration. Please return to review and try again."
        } finally {
            preparing = false
        }
    }

    LaunchedEffect(checkoutState) {
        when (val st = checkoutState) {
            is CheckoutState.Success -> onOrderPlaced(st.orderId)
            is CheckoutState.Failure -> errorMessage = st.message
            else -> Unit
        }
    }

    val isLoading = preparing || checkoutState is CheckoutState.Processing
    val grandTotal = prepared?.quote?.total ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEAEDED))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Checkout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
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
            // Order Total Header Card (matching bz-india Checkout.tsx)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Amount",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (prepared == null) "—" else "₹" + java.text.DecimalFormat("#,##0.##").format(grandTotal),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB12704)
                        )
                    }
                }
            }

            // Billing Address Card (matching bz-india Checkout.tsx)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sameAsShipping = !sameAsShipping }
                    ) {
                        Checkbox(
                            checked = sameAsShipping,
                            onCheckedChange = { sameAsShipping = it },
                            colors = CheckboxDefaults.colors(checkedColor = BzeadPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Billing address same as shipping",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    if (!sameAsShipping) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = billingStreet,
                            onValueChange = { billingStreet = it },
                            label = { Text("Street Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = billingCity,
                                onValueChange = { billingCity = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = billingState,
                                onValueChange = { billingState = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = billingPinCode,
                            onValueChange = { billingPinCode = it },
                            label = { Text("PIN Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Payment Method Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PAYMENT METHOD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pay securely with UPI, cards, net banking or wallets.",
                        fontSize = 13.5.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(text = it, color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            val ready = prepared
                            if (ready == null || activity == null) {
                                errorMessage = "Unable to load backend checkout configuration. Please return to review and try again."
                                return@Button
                            }
                            errorMessage = null
                            BzeadRepository.startRazorpayPayment(
                                activity,
                                ready,
                                BzeadRepository.BillingAddress(sameAsShipping, billingStreet, billingCity, billingState, billingPinCode)
                            )
                        },
                        enabled = !isLoading && prepared != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD814),
                            disabledContainerColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pay_now_btn")
                    ) {
                        if (isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF0F1111),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing...", fontWeight = FontWeight.Bold, color = Color(0xFF0F1111))
                            }
                        } else {
                            Text(
                                text = "PAY NOW — ₹" + java.text.DecimalFormat("#,##0.##").format(grandTotal),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F1111),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
