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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bzead.india.data.BzeadRepository
import com.bzead.india.model.DeliveryAddress
import com.bzead.india.theme.BzeadHeaderNavy
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary
import com.bzead.india.util.AddressValidation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingAddressScreen(
    onNavigateBack: () -> Unit,
    onProceedToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addresses by BzeadRepository.addresses.collectAsState()
    val selectedAddress by BzeadRepository.selectedAddress.collectAsState()

    var activeAddress by remember { mutableStateOf(selectedAddress ?: addresses.firstOrNull()) }
    var showAddAddressForm by remember { mutableStateOf(addresses.isEmpty()) }

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var streetAddress by remember { mutableStateOf("") }
    var streetAddress2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Select Shipping Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Step 1 of 3 · Delivery Details",
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
            // Saved Addresses Card
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
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saved Addresses",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = if (showAddAddressForm) "Cancel" else "+ Add New",
                            color = BzeadPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { showAddAddressForm = !showAddAddressForm }
                                .padding(4.dp)
                                .testTag("toggle_add_address_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (addresses.isEmpty() && !showAddAddressForm) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAddAddressForm = true },
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BzeadPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "No saved address. Tap to enter delivery address",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BzeadPrimary
                                )
                            }
                        }
                    }

                    addresses.forEach { addr ->
                        val isSelected = activeAddress?.id == addr.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) BzeadPrimary else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
                                .clickable {
                                    activeAddress = addr
                                    BzeadRepository.selectedAddress.value = addr
                                    showAddAddressForm = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    activeAddress = addr
                                    BzeadRepository.selectedAddress.value = addr
                                    showAddAddressForm = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = BzeadPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = addr.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = addr.phoneNumber,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = addr.formatted,
                                    fontSize = 12.5.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Add New Address Form (if toggled or no address saved)
            if (showAddAddressForm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Add New Indian Delivery Address",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number (+91) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = streetAddress,
                            onValueChange = { streetAddress = it },
                            label = { Text("Flat, House No., Building, Apartment *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = streetAddress2,
                            onValueChange = { streetAddress2 = it },
                            label = { Text("Area, Colony, Street, Sector") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = DeliveryAddress.INDIA_COUNTRY_NAME,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Country") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("PIN Code (6 Digits) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Delivery notes (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            enabled = !isSaving,
                            onClick = {
                                val newAddress = DeliveryAddress(
                                    id = "",
                                    fullName = fullName.trim(),
                                    phoneNumber = phone.trim(),
                                    email = email.trim(),
                                    streetAddress = streetAddress.trim(),
                                    streetAddress2 = streetAddress2.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pinCode = pinCode.trim(),
                                    country = DeliveryAddress.INDIA_COUNTRY_NAME,
                                    deliveryNotes = notes.trim(),
                                    isDefault = addresses.isEmpty()
                                )
                                val invalid = AddressValidation.validate(newAddress, checkout = true)
                                if (invalid != null) {
                                    errorMessage = invalid
                                    return@Button
                                }
                                isSaving = true
                                scope.launch {
                                    val err = BzeadRepository.addAddress(newAddress)
                                    isSaving = false
                                    if (err == null) {
                                        activeAddress = BzeadRepository.selectedAddress.value
                                        showAddAddressForm = false
                                        errorMessage = null
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BzeadPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("save_address_btn")
                        ) {
                            Text("Save & Deliver to This Address", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Bottom CTA Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val addressToUse = activeAddress ?: addresses.firstOrNull()
                        if (addressToUse != null) {
                            BzeadRepository.selectedAddress.value = addressToUse
                            onProceedToSummary()
                        } else {
                            showAddAddressForm = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD814)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("continue_to_review_btn")
                ) {
                    Text(
                        text = "Continue to Review Order →",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F1111),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
