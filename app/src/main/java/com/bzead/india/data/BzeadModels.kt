package com.bzead.india.data

import com.bzead.india.model.ProductVariant

data class HeroBanner(
    val id: String,
    val imageUrl: String,
    val link: String? = null,
    val title: String = "",
    val subtitle: String = ""
)

data class AdBanner(
    val id: String,
    val imageUrl: String,
    val title: String = "",
    val link: String? = null,
    val adSlot: Int = 1
)

data class CategoryItem(
    val id: String,
    val name: String,
    val iconUrl: String = "",
    val tag: String = "Popular",
    val isLive: Boolean = true,
    val parentId: String? = null,
    val level: Int = 1
)

data class SubCategoryItem(
    val id: String,
    val name: String,
    val iconUrl: String = "",
    val parentId: String = "",
    val slug: String = ""
)

data class SubCategorySection(
    val id: String,
    val title: String,
    val items: List<SubCategoryItem> = emptyList()
)


data class Product(
    val id: String,
    val title: String,
    val brand: String = "BZEAD Direct",
    val price: Double,
    val originalPrice: Double = price * 1.5,
    val mrp: Double = originalPrice,
    val discountPercent: Int = (((originalPrice - price) / originalPrice) * 100).toInt(),
    val rating: Float = 4.5f,
    val ratingCount: Int = 850,
    val reviewCount: Int = ratingCount,
    val imageUrl: String,
    val drawableRes: Int? = null,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isDeal: Boolean = false,
    val sellerName: String = "Bzead India Verified",
    val category: String = "All",
    val categoryId: String = "electronics",
    val subCategoryId: String = "",
    val inStock: Boolean = true,
    val stockCount: Int = 45,
    val description: String = "High-quality product curated for BZEAD Indian marketplace with express 2-4 day delivery and 7 days return policy.",
    val variants: List<ProductVariant> = emptyList(),
    val specifications: Map<String, String> = mapOf(
        "Brand" to brand,
        "Warranty" to "1 Year Manufacturer Warranty",
        "Country of Origin" to "India"
    )
)
