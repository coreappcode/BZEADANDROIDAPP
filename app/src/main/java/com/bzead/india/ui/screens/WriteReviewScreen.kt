package com.bzead.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import coil.compose.AsyncImage
import com.bzead.india.data.ReviewApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.bzead.india.data.BzeadRepository
import com.bzead.india.data.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WriteReviewScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onReviewSubmitted: () -> Unit
) {
    val catalog by BzeadRepository.allProducts.collectAsState()
    LaunchedEffect(productId) { BzeadRepository.loadProductDetail(productId) }
    val product = remember(productId, catalog) { BzeadRepository.getProductById(productId) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var rating by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var benefits by remember { mutableStateOf(listOf<String>()) }
    var images by remember { mutableStateOf(listOf<Uri>()) }
    var agreeTerms by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        if (uris.size + images.size > 5) {
            submitError = "Maximum 5 images allowed"
            return@rememberLauncherForActivityResult
        }
        val tooBig = uris.any { uri ->
            val size = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else 0L
            } ?: 0L
            size > ReviewApi.MAX_IMAGE_BYTES
        }
        if (tooBig) {
            submitError = "Some images exceed 5MB. Please choose smaller files."
            return@rememberLauncherForActivityResult
        }
        images = images + uris
    }

    suspend fun readImages(): List<ReviewApi.UploadImage> = withContext(Dispatchers.IO) {
        images.mapIndexed { index, uri ->
            val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else null
            } ?: "image-$index"
            val mime = context.contentResolver.getType(uri) ?: ""
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
            ReviewApi.UploadImage(name, mime, bytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write a Review", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (product != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.title,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A), maxLines = 2)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${product.price.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF166534))
                        }
                    }
                }
            }

            submitError?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text(it, color = Color(0xFFB91C1C), fontSize = 14.sp, modifier = Modifier.padding(14.dp))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Overall Rating *", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { starIndex ->
                            Icon(
                                imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "Rate $starIndex star" + if (starIndex > 1) "s" else "",
                                tint = if (starIndex <= rating) Color(0xFFF69931) else Color(0xFFD1D5DB),
                                modifier = Modifier.size(36.dp).clickable { rating = starIndex }
                            )
                        }
                    }
                    if (rating > 0) {
                        Text(
                            text = when (rating) {
                                1 -> "Poor"
                                2 -> "Fair"
                                3 -> "Good"
                                4 -> "Very Good"
                                else -> "Excellent"
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 100) title = it },
                        label = { Text("Review Title *") },
                        placeholder = { Text("Summarize your review in a few words") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 5000) comment = it },
                        label = { Text("Your Review *") },
                        placeholder = { Text("Share your detailed experience with this product...") },
                        supportingText = { Text("${comment.length}/5000 characters") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 6
                    )

                    Text("Add Photos (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Text("Supported formats: JPG, PNG | Max 5 images | Max 5MB each", fontSize = 12.sp, color = Color(0xFF64748B))
                    OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !isSubmitting) { Text("Select Images") }
                    if (images.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            images.forEachIndexed { index, uri ->
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Preview $index",
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = "✕",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .background(Color(0xCC000000))
                                            .clickable { images = images.filterIndexed { i, _ -> i != index } }
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text("What are the benefits? (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewApi.BENEFIT_OPTIONS.forEach { benefit ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    benefits = if (benefit in benefits) benefits - benefit else benefits + benefit
                                }
                            ) {
                                Checkbox(
                                    checked = benefit in benefits,
                                    onCheckedChange = { benefits = if (it) benefits + benefit else benefits - benefit }
                                )
                                Text(benefit, fontSize = 14.sp, color = Color(0xFF374151))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                        Text(
                            text = "I confirm that this review is based on my own experience and is my genuine opinion. I understand that false or misleading reviews may result in account suspension.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                Button(
                    onClick = {
                        if (rating <= 0 || title.isBlank() || comment.isBlank()) {
                            submitError = "Please fill all required fields: Rating, Title, and Review"
                            return@Button
                        }
                        if (!agreeTerms) {
                            submitError = "Please agree to the terms before submitting"
                            return@Button
                        }
                        isSubmitting = true
                        submitError = null
                        scope.launch {
                            val err = try {
                                BzeadRepository.submitReview(productId, rating, title, comment, readImages(), benefits)
                            } catch (e: Exception) {
                                "Failed to submit review. Please try again."
                            }
                            isSubmitting = false
                            if (err == null) onReviewSubmitted() else submitError = err
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Submit Review", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
