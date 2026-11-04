package com.bzead.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bzead.india.data.BzeadRepository
import com.bzead.india.data.DeliveryEstimateApi
import com.bzead.india.data.DeliveryResult
import com.bzead.india.data.Product
import com.bzead.india.model.DeliveryAddress
import com.bzead.india.theme.BzeadPrimary
import com.bzead.india.theme.TextPrimary
import com.bzead.india.theme.TextSecondary
import kotlinx.coroutines.launch

/** Port of the web `DeliveryEstimate` component driven by `useDeliveryEstimate`. */
@Composable
fun DeliveryEstimateBox(product: Product, onAddNewAddress: () -> Unit) {
    val scope = rememberCoroutineScope()
    val addresses by BzeadRepository.addresses.collectAsState()
    val isLoggedIn by BzeadRepository.isLoggedIn.collectAsState()

    var address by remember { mutableStateOf<DeliveryAddress?>(null) }
    var pincode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DeliveryResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var addressApplied by remember { mutableStateOf(false) }

    suspend fun runCheck(pin: String, buyerCountry: String) {
        loading = true
        result = null
        result = BzeadRepository.checkDelivery(product, pin, buyerCountry)
        loading = false
    }

    // The web loads the default address (else the first) once, for signed-in users.
    LaunchedEffect(isLoggedIn, addresses) {
        if (isLoggedIn && !addressApplied && addresses.isNotEmpty()) {
            addressApplied = true
            val def = addresses.firstOrNull { it.isDefault } ?: addresses.first()
            address = def
            pincode = def.pinCode
            country = def.country
        }
    }

    // Auto-check for signed-in users once the address and the product detail (seller id) are ready; re-check when the product detail changes.
    LaunchedEffect(isLoggedIn, pincode, country, product.sellerId, product.originCountry, product.shipsInternationally) {
        if (isLoggedIn && product.sellerId.isNotEmpty() && pincode.trim().length >= 3) {
            runCheck(pincode.trim(), country)
        }
    }

    val typeLabel = when (address?.addressType) {
        "home" -> "HOME"
        "work" -> "WORK"
        else -> "OTHER"
    }
    val topText = address?.let { a ->
        a.streetAddress + (if (a.streetAddress2.isNotBlank()) ", ${a.streetAddress2}" else "") + (if (a.city.isNotBlank()) ", ${a.city}" else "")
    } ?: "Select delivery location"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(2.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEF3FA))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (address == null) Icons.Default.LocationOn else if (address?.addressType == "work") Icons.Default.Work else Icons.Default.Home,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            if (address != null) {
                Text(typeLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111111))
                Spacer(Modifier.width(6.dp))
            }
            Text(topText, fontSize = 11.sp, color = Color(0xFF111111), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text("›", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            val r = result
            when {
                loading -> Text("Checking delivery availability…", fontSize = 12.sp, color = TextSecondary)
                r?.error != null -> Text(r.error, fontSize = 12.sp, color = Color(0xFFDC2626))
                r?.serviceable == false -> Text("Delivery not available to this location", fontSize = 12.sp, color = Color(0xFFDC2626))
                r?.serviceable == true && r.expectedDeliveryDate.isNotEmpty() ->
                    Text("Delivery by ${DeliveryEstimateApi.formatDate(r.expectedDeliveryDate)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                else -> Text("Delivery details available at checkout", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }

    if (showPicker) {
        val query = search.trim().lowercase()
        val filtered = addresses.filter { a ->
            "${a.fullName} ${a.streetAddress} ${a.streetAddress2} ${a.city} ${a.pinCode}".lowercase().contains(query)
        }
        Dialog(onDismissRequest = { showPicker = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select delivery address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search by area, street name, pin code", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Saved addresses", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        TextButton(onClick = { showPicker = false; onAddNewAddress() }) { Text("Add New") }
                    }
                    if (filtered.isEmpty()) {
                        Text("No saved addresses found.", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(filtered, key = { it.id }) { a ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            address = a
                                            pincode = a.pinCode
                                            country = a.country
                                            result = null
                                            showPicker = false
                                            scope.launch { runCheck(a.pinCode.trim(), a.country) }
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(a.fullName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(a.formatted, fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
