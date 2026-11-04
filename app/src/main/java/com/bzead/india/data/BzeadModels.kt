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
    val brand: String = "",
    val price: Double,
    val originalPrice: Double = price,
    val mrp: Double = originalPrice,
    val discountPercent: Int = if (originalPrice > price) Math.round((1 - price / originalPrice) * 100).toInt() else 0,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val reviewCount: Int = ratingCount,
    val imageUrl: String,
    val drawableRes: Int? = null,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isDeal: Boolean = false,
    val sellerName: String = "",
    val category: String = "All",
    val categoryId: String = "",
    val subCategoryId: String = "",
    val inStock: Boolean = true,
    val stockCount: Int = 0,
    val description: String = "",
    val variants: List<ProductVariant> = emptyList(),
    val sellerId: String = "",
    val originCountry: String = "",
    val shipsInternationally: Boolean = false,
    val images: List<String> = emptyList(),
    val specifications: Map<String, String> = emptyMap()
)
