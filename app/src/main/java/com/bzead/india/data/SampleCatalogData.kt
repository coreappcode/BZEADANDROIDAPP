package com.bzead.india.data

import com.bzead.india.model.ProductVariant
import com.bzead.india.model.Review

object SampleCatalogData {

    val liveHeroBanners = listOf(
        HeroBanner(
            id = "hero_1",
            imageUrl = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&q=80",
            title = "Mega Super Saver Festival",
            subtitle = "Up to 80% Off on Top Electronics & Fashion"
        ),
        HeroBanner(
            id = "hero_2",
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200&q=80",
            title = "Smart Gadgets & Wearables",
            subtitle = "Latest Tech Arrivals at Unbeatable Prices"
        ),
        HeroBanner(
            id = "hero_3",
            imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&q=80",
            title = "Trendy Fashion Collection",
            subtitle = "Elevate Your Style with Bzead Premium Range"
        ),
        HeroBanner(
            id = "hero_4",
            imageUrl = "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=1200&q=80",
            title = "Home & Kitchen Essentials",
            subtitle = "Transform Your Living Space Today"
        )
    )

    val liveAdBanners = listOf(
        AdBanner(
            id = "ad_slot_1",
            imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=1000&q=80",
            title = "Exclusive Audio Tech Sale",
            adSlot = 1
        ),
        AdBanner(
            id = "ad_slot_2",
            imageUrl = "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=1000&q=80",
            title = "Mamaearth Ultra Light Sunscreen Pack",
            adSlot = 2
        ),
        AdBanner(
            id = "ad_slot_3",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1000&q=80",
            title = "Premium Headphones Festival",
            adSlot = 3
        )
    )

    val mainDepartments = listOf(
        CategoryItem("cat_fashion_men", "Men", "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=300&q=80"),
        CategoryItem("cat_fashion_women", "Women", "https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=300&q=80"),
        CategoryItem("cat_kids", "Kids & Toys", "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=300&q=80"),
        CategoryItem("cat_electronics", "Electronics", "https://images.unsplash.com/photo-1498049860654-af1a5c566876?w=300&q=80"),
        CategoryItem("cat_beauty", "Beauty & Health", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=300&q=80"),
        CategoryItem("cat_home", "Home & Kitchen", "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=300&q=80"),
        CategoryItem("cat_jewellery", "Jewellery & Acc.", "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=300&q=80"),
        CategoryItem("cat_footwear", "Bags & Footwear", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300&q=80"),
        CategoryItem("cat_grocery", "Grocery & Food", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300&q=80")
    )

    val categories = mainDepartments

    val departmentSectionsMap = mapOf(
        "cat_fashion_men" to listOf(
            SubCategorySection(
                id = "sec_topwear",
                title = "Top Wear",
                items = listOf(
                    SubCategoryItem("sub_1", "Summer T-Shirts", "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=200&q=80"),
                    SubCategoryItem("sub_2", "Casual Shirts", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=200&q=80"),
                    SubCategoryItem("sub_3", "T-Shirt Combos", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=200&q=80"),
                    SubCategoryItem("sub_4", "Polo T-Shirts", "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=200&q=80"),
                    SubCategoryItem("sub_5", "Jackets & Coats", "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=200&q=80"),
                    SubCategoryItem("sub_6", "Formal Shirts", "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_bottomwear",
                title = "Bottom Wear",
                items = listOf(
                    SubCategoryItem("sub_7", "Jeans", "https://images.unsplash.com/photo-1542272604-780c96856592?w=200&q=80"),
                    SubCategoryItem("sub_8", "Cargos / Trousers", "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=200&q=80"),
                    SubCategoryItem("sub_9", "Dhotis / Lungis", "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=200&q=80"),
                    SubCategoryItem("sub_10", "Casual Shorts", "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=200&q=80"),
                    SubCategoryItem("sub_11", "Track Pants", "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_ethnic",
                title = "Ethnic Wear",
                items = listOf(
                    SubCategoryItem("sub_12", "Kurtas", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=200&q=80"),
                    SubCategoryItem("sub_13", "Kurta Sets", "https://images.unsplash.com/photo-1609357605129-26f69add5d6e?w=200&q=80"),
                    SubCategoryItem("sub_14", "Nehru Jacket", "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_innerwear",
                title = "Innerwear & Loungewear",
                items = listOf(
                    SubCategoryItem("sub_15", "Vests", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=200&q=80"),
                    SubCategoryItem("sub_16", "Briefs", "https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=200&q=80"),
                    SubCategoryItem("sub_17", "Boxers", "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=200&q=80")
                )
            )
        ),
        "cat_fashion_women" to listOf(
            SubCategorySection(
                id = "sec_ethnic_women",
                title = "Ethnic & Fusion Wear",
                items = listOf(
                    SubCategoryItem("w_1", "Kurtas & Suits", "https://images.unsplash.com/photo-1583391733956-6c78276477e2?w=200&q=80"),
                    SubCategoryItem("w_2", "Sarees & Blouses", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=200&q=80"),
                    SubCategoryItem("w_3", "Ethnic Dresses", "https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=200&q=80"),
                    SubCategoryItem("w_4", "Lehenga Cholis", "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_western_women",
                title = "Western Wear",
                items = listOf(
                    SubCategoryItem("w_5", "Tops & Tees", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=200&q=80"),
                    SubCategoryItem("w_6", "Dresses & Jumpsuits", "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=200&q=80"),
                    SubCategoryItem("w_7", "Jeans & Jeggings", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=200&q=80"),
                    SubCategoryItem("w_8", "Skirts & Shorts", "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=200&q=80")
                )
            )
        ),
        "cat_electronics" to listOf(
            SubCategorySection(
                id = "sec_mobiles_tech",
                title = "Mobiles & Gadgets",
                items = listOf(
                    SubCategoryItem("e_1", "Smartphones", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=200&q=80"),
                    SubCategoryItem("e_2", "Feature Phones", "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=200&q=80"),
                    SubCategoryItem("e_3", "Mobile Covers", "https://images.unsplash.com/photo-1541877944-ac82a091518a?w=200&q=80"),
                    SubCategoryItem("e_4", "Power Banks", "https://images.unsplash.com/photo-1609592424074-325bdf378b8a?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_laptops_pc",
                title = "Laptops & Computers",
                items = listOf(
                    SubCategoryItem("e_5", "Gaming Laptops", "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=200&q=80"),
                    SubCategoryItem("e_6", "Desktops & All-in-One", "https://images.unsplash.com/photo-1587831990711-23ca6441447b?w=200&q=80"),
                    SubCategoryItem("e_7", "Computer Accessories", "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_audio",
                title = "Audio & Smart Devices",
                items = listOf(
                    SubCategoryItem("e_8", "Wireless Earbuds", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=200&q=80"),
                    SubCategoryItem("e_9", "Noise Cancelling Headphones", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&q=80"),
                    SubCategoryItem("e_10", "Smartwatches", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&q=80")
                )
            )
        ),
        "cat_beauty" to listOf(
            SubCategorySection(
                id = "sec_skincare",
                title = "Skincare",
                items = listOf(
                    SubCategoryItem("b_1", "Face Wash", "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=200&q=80"),
                    SubCategoryItem("b_2", "Sunscreen SPF 50", "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=200&q=80"),
                    SubCategoryItem("b_3", "Face Serums & Oils", "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=200&q=80"),
                    SubCategoryItem("b_4", "Moisturizers & Creams", "https://images.unsplash.com/photo-1608248597349-9069d2d80d28?w=200&q=80")
                )
            ),
            SubCategorySection(
                id = "sec_haircare",
                title = "Haircare & Fragrance",
                items = listOf(
                    SubCategoryItem("b_5", "Shampoo & Conditioners", "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=200&q=80"),
                    SubCategoryItem("b_6", "Hair Oils & Masks", "https://images.unsplash.com/photo-1526947425960-945c6e72858f?w=200&q=80"),
                    SubCategoryItem("b_7", "Perfumes & Deodorants", "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=200&q=80")
                )
            )
        )
    )


    val sampleReviews = listOf(
        Review("Amit K.", 5, "Outstanding sound quality and deep bass! Delivered to Delhi in 2 days.", "2 days ago", true),
        Review("Priya S.", 5, "Authentic product at great price. Packaging was super secure.", "5 days ago", true),
        Review("Vikram R.", 4, "Value for money product. Battery life easily lasts full day.", "1 week ago", true)
    )

    val products = listOf(
        Product(
            id = "prod_1",
            title = "Wireless Noise Cancelling Over-Ear Headphones",
            brand = "BZEAD Audio Pro",
            price = 2999.0,
            originalPrice = 6999.0,
            rating = 4.6f,
            ratingCount = 1420,
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80",
            isFeatured = true,
            isTrending = false,
            isDeal = true,
            category = "Electronics",
            categoryId = "cat_electronics",
            variants = listOf(
                ProductVariant("v1", "Black Edition", 0.0, "Standard", "Black"),
                ProductVariant("v2", "Silver Edition", 200.0, "Standard", "Silver")
            )
        ),
        Product(
            id = "prod_2",
            title = "Smart Fitness Watch with HD AMOLED Display",
            brand = "BZEAD Tech",
            price = 1999.0,
            originalPrice = 4999.0,
            rating = 4.5f,
            ratingCount = 890,
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80",
            isFeatured = true,
            isTrending = true,
            isDeal = false,
            category = "Electronics",
            categoryId = "cat_electronics",
            variants = listOf(
                ProductVariant("v3", "Midnight Black", 0.0, "44mm", "Black"),
                ProductVariant("v4", "Rose Gold", 100.0, "40mm", "Gold")
            )
        ),
        Product(
            id = "prod_3",
            title = "Mamaearth Ultra Light Indian Sunscreen SPF 50",
            brand = "Mamaearth",
            price = 449.0,
            originalPrice = 599.0,
            rating = 4.8f,
            ratingCount = 3200,
            imageUrl = "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&q=80",
            isFeatured = true,
            isTrending = true,
            isDeal = true,
            category = "Beauty",
            categoryId = "cat_beauty"
        ),
        Product(
            id = "prod_4",
            title = "Ergonomic Mesh Swivel Office Chair",
            brand = "BZEAD Living",
            price = 4599.0,
            originalPrice = 8999.0,
            rating = 4.4f,
            ratingCount = 610,
            imageUrl = "https://images.unsplash.com/photo-1580481072645-022f9a6d83d0?w=600&q=80",
            isFeatured = true,
            isTrending = false,
            isDeal = false,
            category = "Home",
            categoryId = "cat_home"
        ),
        Product(
            id = "prod_5",
            title = "Pro Gaming Optical Mouse RGB 12000 DPI",
            brand = "Logitech",
            price = 899.0,
            originalPrice = 1999.0,
            rating = 4.7f,
            ratingCount = 2150,
            imageUrl = "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&q=80",
            isFeatured = false,
            isTrending = true,
            isDeal = false,
            category = "Electronics",
            categoryId = "cat_electronics"
        ),
        Product(
            id = "prod_6",
            title = "Classic Leather Sneakers for Men",
            brand = "Puma",
            price = 1499.0,
            originalPrice = 3499.0,
            rating = 4.3f,
            ratingCount = 980,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80",
            isFeatured = false,
            isTrending = true,
            isDeal = true,
            category = "Footwear",
            categoryId = "cat_footwear"
        ),
        Product(
            id = "prod_7",
            title = "Stainless Steel Insulated Water Bottle 1L",
            brand = "Milton",
            price = 499.0,
            originalPrice = 999.0,
            rating = 4.6f,
            ratingCount = 1840,
            imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600&q=80",
            isFeatured = false,
            isTrending = true,
            isDeal = false,
            category = "Home",
            categoryId = "cat_home"
        ),
        Product(
            id = "prod_8",
            title = "Minimalist Waterproof Backpack 25L",
            brand = "Wildcraft",
            price = 1299.0,
            originalPrice = 2999.0,
            rating = 4.5f,
            ratingCount = 1230,
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&q=80",
            isFeatured = false,
            isTrending = true,
            isDeal = true,
            category = "Accessories",
            categoryId = "cat_accessories"
        )
    )
}
