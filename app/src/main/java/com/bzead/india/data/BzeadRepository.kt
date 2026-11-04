package com.bzead.india.data

import com.bzead.india.model.CartItem
import com.bzead.india.model.DeliveryAddress
import com.bzead.india.model.LoginResult
import com.bzead.india.model.NotificationItem
import com.bzead.india.model.Order
import com.bzead.india.model.OrderItem
import com.bzead.india.model.OrderStatus
import com.bzead.india.model.Review
import com.bzead.india.model.ProductVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BzeadRepository {

    private val _heroBanners = MutableStateFlow<List<HeroBanner>>(emptyList())
    val heroBanners: StateFlow<List<HeroBanner>> = _heroBanners.asStateFlow()

    private val _adBanners = MutableStateFlow<List<AdBanner>>(emptyList())
    val adBanners: StateFlow<List<AdBanner>> = _adBanners.asStateFlow()

    private val _liveCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val liveCategories: StateFlow<List<CategoryItem>> = _liveCategories.asStateFlow()

    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()

    private val _trendingProducts = MutableStateFlow<List<Product>>(emptyList())
    val trendingProducts: StateFlow<List<Product>> = _trendingProducts.asStateFlow()

    private val _hotDealsProducts = MutableStateFlow<List<Product>>(emptyList())
    val hotDealsProducts: StateFlow<List<Product>> = _hotDealsProducts.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _mainDepartments = MutableStateFlow<List<CategoryItem>>(emptyList())
    val mainDepartments: StateFlow<List<CategoryItem>> = _mainDepartments.asStateFlow()

    private val _categorySectionsMap = MutableStateFlow<Map<String, List<SubCategorySection>>>(emptyMap())
    val categorySectionsMap: StateFlow<Map<String, List<SubCategorySection>>> = _categorySectionsMap.asStateFlow()

    val allCategories: List<CategoryItem> get() = _liveCategories.value


    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private val _isCartLoading = MutableStateFlow(false)
    val isCartLoading: StateFlow<Boolean> = _isCartLoading.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _wishlistProductIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistProductIds: StateFlow<Set<String>> = _wishlistProductIds.asStateFlow()

    /** Full product rows for wishlisted items (the backend join), so they show even if outside the loaded catalog page. */
    private val _wishlistProducts = MutableStateFlow<List<Product>>(emptyList())
    val wishlistProducts: StateFlow<List<Product>> = _wishlistProducts.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow<String?>(null)
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()

    private var sessionStore: SessionStore? = null
    private var session: AuthSession? = null
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    const val ALREADY_CONFIRMED = "ALREADY_CONFIRMED"
    private const val EXISTING_EMAIL_MESSAGE =
        "A user account already exists with this email address. Please use another email address."

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _hasMoreNotifications = MutableStateFlow(false)
    val hasMoreNotifications: StateFlow<Boolean> = _hasMoreNotifications.asStateFlow()
    private var notificationOffset = 0

    private val _addresses = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    val addresses: StateFlow<List<DeliveryAddress>> = _addresses.asStateFlow()

    val selectedAddress = MutableStateFlow<DeliveryAddress?>(null)

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _dataError = MutableStateFlow<String?>(null)
    /** Last backend error for cart/wishlist/address/order/notification actions; UI may show and clear it. */
    val dataError: StateFlow<String?> = _dataError.asStateFlow()
    fun clearDataError() { _dataError.value = null }

    private val tokenMutex = Mutex()

    /** Access token for the signed-in user, refreshed when about to expire. */
    private suspend fun validToken(): String? = tokenMutex.withLock {
        val s = session ?: return@withLock null
        if (s.expiresAtSec - 60 > System.currentTimeMillis() / 1000) return@withLock s.accessToken
        val res = SupabaseAuth.refresh(s.refreshToken)
        val fresh = res.session
        if (fresh == null) {
            if (res.body != null) clearLocalSession()
            return@withLock null
        }
        session = fresh
        sessionStore?.saveRefreshToken(fresh.refreshToken)
        fresh.accessToken
    }

    /** Web `useDeliveryEstimate.runCheck`; guests use the anon key like the web's anonymous client. */
    suspend fun checkDelivery(product: Product, pin: String, country: String): DeliveryResult {
        val token = validToken() ?: SupabaseClient.ANON_KEY
        return DeliveryEstimateApi.check(product, pin.trim(), country, token)
    }

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private fun guestHistory(): List<String> {
        val raw = appContext?.getSharedPreferences("bzead_search", android.content.Context.MODE_PRIVATE)
            ?.getString("bzead_search_history", null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }

    private fun writeGuestHistory(items: List<String>) {
        appContext?.getSharedPreferences("bzead_search", android.content.Context.MODE_PRIVATE)
            ?.edit()?.putString("bzead_search_history", org.json.JSONArray(items.take(20)).toString())?.apply()
    }

    private fun dedupeHistory(items: List<String>): List<String> {
        val seen = HashSet<String>()
        return items.filter { seen.add(it.lowercase()) }.take(5)
    }

    /** Web Search.tsx fetchHistory: user_search_history for users, local list for guests; 5 shown. */
    suspend fun loadSearchHistory() {
        val uid = session?.userId
        if (uid == null) {
            _recentSearches.value = dedupeHistory(guestHistory())
            return
        }
        val token = validToken() ?: return
        val r = SupabaseRest.request(
            "GET", "user_search_history?select=typed_input&user_id=eq.$uid&order=searched_at.desc&limit=20", token
        )
        _recentSearches.value = if (r.ok) dedupeHistory(r.rows.map { it.optString("typed_input") }.filter { it.isNotBlank() }) else emptyList()
    }

    fun saveSearch(typed: String) {
        val value = typed.trim()
        if (value.isEmpty()) return
        val uid = session?.userId
        if (uid == null) {
            writeGuestHistory(listOf(value) + guestHistory())
            _recentSearches.value = dedupeHistory(guestHistory())
            return
        }
        authScope.launch {
            val token = validToken() ?: return@launch
            val body = JSONObject().put("user_id", uid).put("typed_input", value).put("is_product_click", false)
            SupabaseRest.request("POST", "user_search_history", token, body.toString())
            loadSearchHistory()
        }
    }

    fun deleteSearch(typed: String) {
        val uid = session?.userId
        if (uid == null) {
            writeGuestHistory(guestHistory().filter { !it.equals(typed, ignoreCase = true) })
            _recentSearches.value = dedupeHistory(guestHistory())
            return
        }
        authScope.launch {
            val token = validToken() ?: return@launch
            SupabaseRest.request("DELETE", "user_search_history?user_id=eq.$uid&typed_input=eq.${java.net.URLEncoder.encode(typed, "UTF-8")}", token)
            loadSearchHistory()
        }
    }

    fun clearSearchHistory() {
        val uid = session?.userId
        if (uid == null) {
            writeGuestHistory(emptyList())
            _recentSearches.value = emptyList()
            return
        }
        authScope.launch {
            val token = validToken() ?: return@launch
            SupabaseRest.request("DELETE", "user_search_history?user_id=eq.$uid", token)
            _recentSearches.value = emptyList()
        }
    }

    private val _hasSponsored = MutableStateFlow(false)
    val hasSponsored: StateFlow<Boolean> = _hasSponsored.asStateFlow()

    suspend fun refreshHomeContent() {
        _isHomeLoading.value = true
        try {
            SupabaseClient.fetchBanners()?.let { (hero, ad) ->
                _heroBanners.value = hero
                _adBanners.value = ad
            }

            SupabaseClient.fetchCategoryTree()?.let { (mains, tree) ->
                _mainDepartments.value = mains
                _categorySectionsMap.value = tree
                // Home shortcuts: the first 16 level-1 categories (web `Categories`).
                _liveCategories.value = mains.take(16)
            }

            SupabaseClient.fetchHomeSections()?.let { sections ->
                _featuredProducts.value = sections.featured
                _trendingProducts.value = sections.trending
                _hotDealsProducts.value = sections.deals
                _hasSponsored.value = sections.hasSponsored
                val seen = LinkedHashMap<String, Product>()
                (_featuredProducts.value + _trendingProducts.value + _hotDealsProducts.value).forEach { seen.putIfAbsent(it.id, it) }
                _allProducts.value = (seen.values + _allProducts.value.filterNot { seen.containsKey(it.id) }).toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isHomeLoading.value = false
        }
    }

    private val _sectionProducts = MutableStateFlow<Map<String, List<Product>>>(emptyMap())
    val sectionProducts: StateFlow<Map<String, List<Product>>> = _sectionProducts.asStateFlow()

    /** Full "see more" list for a section (web `SectionProducts`, up to 200). */
    suspend fun loadSection(section: String) {
        val key = normalizeSection(section) ?: return
        val list = SupabaseClient.fetchSection(key, 200) ?: return
        _sectionProducts.value = _sectionProducts.value + (key to list)
        mergeIntoCatalog(list)
    }

    private val _categoryProducts = MutableStateFlow<Map<String, List<Product>>>(emptyMap())
    val categoryProducts: StateFlow<Map<String, List<Product>>> = _categoryProducts.asStateFlow()

    suspend fun loadCategoryProducts(categoryId: String) {
        val list = SupabaseClient.fetchCategoryProducts(categoryId) ?: return
        _categoryProducts.value = _categoryProducts.value + (categoryId to list)
        mergeIntoCatalog(list)
    }

    private fun mergeIntoCatalog(products: List<Product>) {
        val ids = products.map { it.id }.toSet()
        _allProducts.value = products + _allProducts.value.filterNot { it.id in ids }
    }

    /** Loads the product row with variants; used by the product page. */
    suspend fun loadProductDetail(id: String) {
        SupabaseClient.fetchProductDetail(id)?.let { mergeIntoCatalog(listOf(it)) }
    }

    /** Resolves a product id, public product id or slug (as accepted by the web `/products/:productId`) to the product id. */
    suspend fun resolveProductRef(ref: String): String? {
        val product = SupabaseClient.fetchProductDetail(ref) ?: return null
        mergeIntoCatalog(listOf(product))
        return product.id
    }

    /** Web `search_products_catalog` search. Null means the request failed. */
    suspend fun searchCatalog(query: String): List<Product>? {
        if (query.isBlank()) return emptyList()
        val results = SupabaseClient.searchProducts(query.trim()) ?: return null
        mergeIntoCatalog(results)
        return results
    }

    private fun normalizeSection(section: String): String? = when (section.lowercase()) {
        "featured" -> "featured"
        "hot-deals", "deals" -> "hot-deals"
        "trending" -> "trending"
        else -> null
    }


    /** Replaces the local cart with the backend cart (same as the web `syncCartWithBackend`). */
    fun syncCartWithBackend() {
        val uid = session?.userId ?: return
        authScope.launch { loadCart(uid) }
    }

    private suspend fun loadCart(uid: String) {
        _isCartLoading.value = true
        try {
            var items = validToken()?.let { UserDataApi.fetchCart(it, uid) }
            if (items == null) {
                kotlinx.coroutines.delay(500)
                items = validToken()?.let { UserDataApi.fetchCart(it, uid) }
            }
            if (items != null && session?.userId == uid) _cartItems.value = items
        } finally {
            _isCartLoading.value = false
        }
    }

    private fun persist(block: suspend (token: String, uid: String) -> String?) {
        val uid = session?.userId ?: return
        authScope.launch {
            val token = validToken() ?: return@launch
            block(token, uid)?.let { _dataError.value = it }
        }
    }

    fun addToCart(product: Product, variant: ProductVariant? = null) {
        addToCart(
            product, variant?.size, variant?.color, variant?.id, variant?.sku,
            variant?.let { it.price ?: (product.price + it.priceDelta) }
        )
    }

    /** Web PDP `addToCart(product, 1, { selectedSize, selectedColor, selectedVariantId, selectedVariantSku, variantPrice })`. */
    fun addToCart(product: Product, size: String?, color: String?, variantId: String?, sku: String?, variantPrice: Double?, quantity: Int = 1) {
        val key = UserDataApi.cartItemId(product.id, size, color, sku)
        val list = _cartItems.value.toMutableList()
        val idx = list.indexOfFirst { it.id == key }
        val item = if (idx >= 0) {
            list[idx].let { it.copy(quantity = it.quantity + quantity, variantPrice = variantPrice ?: it.variantPrice) }.also { list[idx] = it }
        } else {
            CartItem(
                id = key, product = product, quantity = quantity, selectedSize = size, selectedColor = color,
                selectedVariantId = variantId, selectedVariantSku = sku, variantPrice = variantPrice
            ).also { list.add(it) }
        }
        _cartItems.value = list
        if (UserDataApi.isUuid(product.id)) persist { t, uid -> UserDataApi.upsertCartItem(t, uid, item).error }
    }

    fun updateCartQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItemId)
            return
        }
        val list = _cartItems.value.toMutableList()
        val index = list.indexOfFirst { it.id == cartItemId || it.product.id == cartItemId }
        if (index < 0) return
        val item = list[index].copy(quantity = newQuantity)
        list[index] = item
        _cartItems.value = list
        if (UserDataApi.isUuid(item.product.id)) persist { t, uid -> UserDataApi.upsertCartItem(t, uid, item).error }
    }

    fun removeFromCart(cartItemId: String) {
        val removed = _cartItems.value.firstOrNull { it.id == cartItemId || it.product.id == cartItemId } ?: return
        _cartItems.value = _cartItems.value.filterNot { it === removed }
        if (UserDataApi.isUuid(removed.product.id)) persist { t, uid -> UserDataApi.deleteCartItem(t, uid, removed).error }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        persist { t, uid -> UserDataApi.clearCart(t, uid).error }
    }

    private fun saveWishlistLocally() {
        appContext?.getSharedPreferences("bzead_wishlist", android.content.Context.MODE_PRIVATE)
            ?.edit()?.putString("ids", org.json.JSONArray(_wishlistProductIds.value.toList()).toString())?.apply()
    }

    fun toggleWishlist(productId: String) {
        val adding = !_wishlistProductIds.value.contains(productId)
        if (adding) {
            _wishlistProductIds.value = _wishlistProductIds.value + productId
            val p = getProductById(productId) ?: _cartItems.value.firstOrNull { it.product.id == productId }?.product
            if (p != null) _wishlistProducts.value = _wishlistProducts.value + p
        } else {
            _wishlistProductIds.value = _wishlistProductIds.value - productId
            _wishlistProducts.value = _wishlistProducts.value.filterNot { it.id == productId }
        }
        saveWishlistLocally()
        if (!UserDataApi.isUuid(productId)) return
        persist { t, uid ->
            val res = if (adding) UserDataApi.addWishlist(t, uid, listOf(productId)) else UserDataApi.removeWishlist(t, uid, productId)
            if (!res.ok) {
                // roll back the optimistic change
                _wishlistProductIds.value = if (adding) _wishlistProductIds.value - productId else _wishlistProductIds.value + productId
            }
            res.error
        }
    }

    /** Web `useWishlistSync`: merge local and backend wishlists, then push the merged set. */
    private suspend fun loadWishlist(uid: String) {
        val token = validToken() ?: return
        val remote = UserDataApi.fetchWishlist(token, uid) ?: return
        val localIds = _wishlistProductIds.value.filter { UserDataApi.isUuid(it) }
        val remoteIds = remote.map { it.id }.toSet()
        val missing = localIds.filterNot { it in remoteIds }
        if (missing.isNotEmpty()) UserDataApi.addWishlist(token, uid, missing)
        val localProducts = missing.mapNotNull { getProductById(it) }
        if (session?.userId != uid) return
        _wishlistProducts.value = remote + localProducts
        _wishlistProductIds.value = remoteIds + missing
        saveWishlistLocally()
    }

    fun isInCart(productId: String): Boolean {
        return _cartItems.value.any { it.product.id == productId }
    }

    fun getProductById(id: String): Product? {
        return _allProducts.value.firstOrNull { it.id == id }
    }

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun markAllNotificationsRead() {
        _unreadCount.value = 0
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        persist { t, uid -> UserDataApi.markAllNotificationsRead(t, uid).error }
    }

    fun markNotificationAsRead(id: String) {
        if (_notifications.value.any { it.id == id && !it.isRead }) _unreadCount.value = maxOf(0, _unreadCount.value - 1)
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
        persist { t, uid -> UserDataApi.markNotificationRead(t, uid, id).error }
    }

    private suspend fun loadNotifications(uid: String) {
        val token = validToken() ?: return
        val (fetched, visible) = UserDataApi.fetchNotifications(token, uid, 0) ?: return
        if (session?.userId != uid) return
        _notifications.value = visible
        notificationOffset = fetched
        _hasMoreNotifications.value = fetched >= UserDataApi.NOTIFICATION_PAGE_SIZE
        UserDataApi.fetchBuyerUnreadCount(token, uid)?.let { _unreadCount.value = it }
    }

    fun loadMoreNotifications() {
        if (!_hasMoreNotifications.value) return
        val uid = session?.userId ?: return
        authScope.launch {
            val token = validToken() ?: return@launch
            val (fetched, visible) = UserDataApi.fetchNotifications(token, uid, notificationOffset) ?: return@launch
            _notifications.value = _notifications.value + visible
            notificationOffset += fetched
            _hasMoreNotifications.value = fetched >= UserDataApi.NOTIFICATION_PAGE_SIZE
        }
    }

    fun refreshNotifications() {
        val uid = session?.userId ?: return
        authScope.launch { loadNotifications(uid) }
    }

    fun refreshOrders() {
        val uid = session?.userId ?: return
        authScope.launch { loadOrders(uid) }
    }

    private suspend fun loadOrders(uid: String) {
        val token = validToken() ?: return
        val list = UserDataApi.fetchOrders(token, uid) ?: return
        if (session?.userId == uid) _orders.value = list
    }

    private suspend fun loadAddresses(uid: String) {
        val token = validToken() ?: return
        val list = UserDataApi.fetchAddresses(token, uid) ?: return
        if (session?.userId != uid) return
        _addresses.value = list
        selectedAddress.value = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
    }

    /** Creates the address on the backend (first address becomes default, like the web). Returns an error message or null. */
    suspend fun addAddress(address: DeliveryAddress): String? {
        val uid = session?.userId ?: return "Please sign in to save an address."
        val token = validToken() ?: return "Session expired. Please sign in again."
        val (created, err) = UserDataApi.createAddress(token, uid, address.copy(isDefault = address.isDefault || _addresses.value.isEmpty()))
        if (created == null) return err
        if (created.isDefault) UserDataApi.setDefaultAddress(token, uid, created.id)
        loadAddresses(uid)
        selectedAddress.value = _addresses.value.firstOrNull { it.id == created.id } ?: created
        return null
    }

    suspend fun updateAddress(address: DeliveryAddress): String? {
        val uid = session?.userId ?: return "Please sign in to update an address."
        val token = validToken() ?: return "Session expired. Please sign in again."
        val (updated, err) = UserDataApi.updateAddress(token, uid, address)
        if (updated == null) return err
        if (address.isDefault) UserDataApi.setDefaultAddress(token, uid, address.id)
        val keepSelected = selectedAddress.value?.id
        loadAddresses(uid)
        selectedAddress.value = _addresses.value.firstOrNull { it.id == (keepSelected ?: address.id) } ?: selectedAddress.value
        return null
    }

    suspend fun deleteAddress(id: String): String? {
        val uid = session?.userId ?: return "Please sign in."
        val token = validToken() ?: return "Session expired. Please sign in again."
        UserDataApi.deleteAddress(token, uid, id)?.let { return it }
        val wasSelected = selectedAddress.value?.id == id
        _addresses.value = _addresses.value.filter { it.id != id }
        if (wasSelected) selectedAddress.value = _addresses.value.firstOrNull { it.isDefault } ?: _addresses.value.firstOrNull()
        return null
    }

    suspend fun setDefaultAddress(id: String): String? {
        val uid = session?.userId ?: return "Please sign in."
        val token = validToken() ?: return "Session expired. Please sign in again."
        UserDataApi.setDefaultAddress(token, uid, id)?.let { return it }
        _addresses.value = _addresses.value.map { it.copy(isDefault = it.id == id) }.sortedByDescending { it.isDefault }
        selectedAddress.value = _addresses.value.firstOrNull { it.id == id }
        return null
    }

    fun getProductsBySection(section: String): List<Product> {
        val key = normalizeSection(section) ?: return emptyList()
        return _sectionProducts.value[key] ?: when (key) {
            "featured" -> _featuredProducts.value
            "hot-deals" -> _hotDealsProducts.value
            else -> _trendingProducts.value
        }
    }

    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()

    /** Web `fetchProductReviews`. Public read, uses the user token when signed in. */
    suspend fun loadReviews(productId: String) {
        val token = validToken() ?: SupabaseClient.ANON_KEY
        val list = ReviewApi.fetchReviews(productId, token) ?: return
        _reviews.value = _reviews.value + (productId to list)
    }

    /** Web `WriteReview.handleSubmitReview`. Returns the error text, or null on success. */
    suspend fun submitReview(
        productId: String,
        rating: Int,
        title: String,
        comment: String,
        images: List<ReviewApi.UploadImage>,
        benefits: List<String>
    ): String? {
        if (rating <= 0 || title.isBlank() || comment.isBlank()) {
            return "Please fill all required fields: Rating, Title, and Review"
        }
        val uid = session?.userId
        val token = validToken()
        if (uid == null || token == null) return "You must be logged in to submit a review."
        return try {
            val already = ReviewApi.hasReviewed(token, uid, productId)
            if (already == true) return "You have already reviewed this product."
            val urls = if (images.isEmpty()) emptyList() else ReviewApi.uploadImages(token, productId, images)
            val err = ReviewApi.createReview(token, uid, productId, rating, title.trim(), comment.trim(), urls, benefits)
            if (err != null) return "Failed to submit review. Please try again."
            loadReviews(productId)
            null
        } catch (e: Exception) {
            "Failed to submit review. Please try again."
        }
    }

    suspend fun updateUserProfile(fullName: String, phone: String): Boolean {
        _userName.value = fullName
        _userPhone.value = phone
        return true
    }

    // ---------- Checkout (port of the web OrderSummary + Checkout Razorpay flow) ----------

    private var appContext: android.content.Context? = null

    /** Cart rows chosen on the cart screen (web `beauzead_checkout_selected_cart_ids`). */
    private val checkoutSelectedIds = MutableStateFlow<Set<String>>(emptySet())

    fun setCheckoutSelection(cartItemIds: Set<String>) {
        checkoutSelectedIds.value = cartItemIds
    }

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()
    fun resetCheckoutState() {
        if (_checkoutState.value !is CheckoutState.Processing) _checkoutState.value = CheckoutState.Idle
    }

    /** Orders shown as "processing" while payment confirmation is still in flight (web temp pending orders). */
    private val _tempOrders = MutableStateFlow<List<Order>>(emptyList())

    fun findOrder(id: String): Order? = _orders.value.firstOrNull { it.id == id } ?: _tempOrders.value.firstOrNull { it.id == id }

    private fun activeCartItems(): List<CartItem> {
        val selected = checkoutSelectedIds.value
        val all = _cartItems.value
        val chosen = if (selected.isEmpty()) all else all.filter { it.id in selected }
        return chosen.ifEmpty { all }
    }

    private fun activeAddress(): DeliveryAddress? = selectedAddress.value ?: _addresses.value.firstOrNull()

    private suspend fun checkoutToken(): String =
        validToken() ?: throw CheckoutException("Authentication expired. Please log in again.")

    private suspend fun buildCheckoutItems(token: String): List<CheckoutItem> {
        val cart = activeCartItems()
        val sellers = CheckoutApi.fetchSellerIds(cart.map { it.product.id }, token)
        return cart.map {
            CheckoutItem(
                cartItemId = it.id,
                productId = it.product.id,
                productName = it.product.title,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                sellerId = sellers[it.product.id],
                selectedSize = it.selectedSize,
                selectedColor = it.selectedColor,
                selectedVariantId = it.selectedVariantId,
                selectedVariantSku = it.selectedVariantSku,
                productImage = it.product.imageUrl
            )
        }
    }

    /** Items re-priced with the verified prices from the quote, as the web passes them to checkout. */
    private fun applyQuote(items: List<CheckoutItem>, quote: CheckoutQuote): List<CheckoutItem> =
        items.mapIndexed { i, item ->
            item.copy(
                unitPrice = quote.unitPrices[i],
                selectedVariantId = quote.variantIds[i] ?: item.selectedVariantId,
                selectedVariantSku = quote.variantSkus[i] ?: item.selectedVariantSku
            )
        }

    class OrderSummaryData(
        val items: List<CheckoutItem>,
        val quote: CheckoutQuote,
        val address: DeliveryAddress,
        val missingWeightNames: List<String>,
        val serviceable: Boolean,
        val estimatedDeliveryDate: String?
    )

    /** Web OrderSummary pricing effect: quote, package-weight check, serviceability, then delivery estimate. */
    suspend fun loadOrderSummary(): OrderSummaryData {
        val token = checkoutToken()
        val address = activeAddress() ?: throw CheckoutException("Please add a delivery address before placing the order.")
        val base = buildCheckoutItems(token)
        if (base.isEmpty()) throw CheckoutException("Your cart is empty.")
        val quote = CheckoutApi.calculateQuote(base, address.country, address.pinCode, token)
        val items = applyQuote(base, quote)
        val missing = CheckoutApi.productsMissingWeight(items.map { it.productId }, token)
        val pin = address.pinCode.replace(Regex("\\s+"), "")
        var serviceable = true
        if (pin.length == 6) {
            serviceable = CheckoutApi.isServiceable(pin, items.firstOrNull()?.sellerId ?: session?.userId.orEmpty(), token)
        }
        var eta: String? = null
        if (serviceable) {
            val tat = CheckoutApi.fetchTat(items.map { it.productId }, pin, token)
            if (tat.maxTatDays > 0 && tat.maxExpectedDate.isNotEmpty()) eta = tat.maxExpectedDate
        }
        return OrderSummaryData(items, quote, address, missing, serviceable, eta)
    }

    /** Web OrderSummary "Proceed to payment" stock re-check. Returns an error message or null. */
    suspend fun recheckStock(items: List<CheckoutItem>): String? = CheckoutApi.checkStock(items, checkoutToken())

    class PreparedPayment(
        val items: List<CheckoutItem>,
        val quote: CheckoutQuote,
        val address: DeliveryAddress,
        val razorpayOrder: RazorpayOrder
    )

    /** Web Checkout mount: refresh the live quote, enforce the minimum order, then create the Razorpay order. */
    suspend fun prepareRazorpayPayment(): PreparedPayment {
        val token = checkoutToken()
        val uid = session?.userId ?: throw CheckoutException("Authentication expired. Please log in again.")
        val address = activeAddress() ?: throw CheckoutException("Please add a delivery address before placing the order.")
        val base = buildCheckoutItems(token)
        if (base.isEmpty()) throw CheckoutException("Your cart is empty.")
        val quote = CheckoutApi.calculateQuote(base, address.country, address.pinCode, token)
        minimumOrderMessage(address, quote)?.let { throw CheckoutException(it) }
        val amountPaise = Math.round(quote.total * 100)
        if (amountPaise <= 0) throw CheckoutException("Invalid checkout total. Please return to review and try again.")
        val order = CheckoutApi.createRazorpayOrder(token, amountPaise, uid, base.size)
        return PreparedPayment(applyQuote(base, quote), quote, address, order)
    }

    fun minimumOrderMessage(address: DeliveryAddress, quote: CheckoutQuote): String? {
        val m = quote.minimumOrder?.takeIf { !it.isMet } ?: return null
        val remaining = (m.minimumInr - m.currentSubtotalInr).coerceAtLeast(0.0)
        return "Minimum order for India-origin products shipping to ${address.country.trim().ifEmpty { "selected destination" }} is " +
            "${formatInr(m.minimumInr)} (${formatInr(m.minimumInr)}). Add ${formatInr(remaining)} more to continue."
    }

    private fun formatInr(v: Double): String = "₹" + java.text.DecimalFormat("#,##0.##").format(v)

    class BillingAddress(val sameAsShipping: Boolean, val street: String, val city: String, val state: String, val postalCode: String)

    private fun nextTempOrderId(): String {
        val dateToken = java.text.SimpleDateFormat("ddMMyy", java.util.Locale.US).format(java.util.Date())
        val prefs = appContext?.getSharedPreferences("bzead_checkout", android.content.Context.MODE_PRIVATE)
        val key = "temp_order_seq_DOM_$dateToken"
        val next = (prefs?.getInt(key, 0) ?: 0) + 1
        prefs?.edit()?.putInt(key, next)?.apply()
        return "TMDOM$dateToken${next.toString().padStart(7, '0')}"
    }

    /** Web `handleRazorpaySubmit`. Progress and the outcome are published through [checkoutState]. */
    fun startRazorpayPayment(activity: android.app.Activity, prepared: PreparedPayment, billing: BillingAddress) {
        if (_checkoutState.value is CheckoutState.Processing) return
        _checkoutState.value = CheckoutState.Processing
        authScope.launch { runRazorpayPayment(activity, prepared, billing) }
    }

    private suspend fun runRazorpayPayment(activity: android.app.Activity, prepared: PreparedPayment, billing: BillingAddress) {
        val items = prepared.items
        val quote = prepared.quote
        val address = prepared.address
        val rzpOrder = prepared.razorpayOrder
        val paymentIntentId = rzpOrder.orderId
        var paymentTaken = false
        try {
            val uid = session?.userId ?: throw CheckoutException("Authentication expired. Please log in again.")
            val token = checkoutToken()
            val pin = address.pinCode.replace(Regex("\\s+"), "")
            val sellerId = items.firstOrNull()?.sellerId ?: uid
            if (pin.length == 6 && !CheckoutApi.isServiceable(pin, sellerId, token)) {
                _checkoutState.value = CheckoutState.Failure(CheckoutApi.PINCODE_NOT_SERVICEABLE)
                return
            }
            val tat = CheckoutApi.fetchTat(items.map { it.productId }, pin, token)

            val customerName = address.fullName.ifBlank { _userName.value.orEmpty() }
            val customerPhone = address.phoneNumber
            val customerEmail = address.email.ifBlank { _userEmail.value.orEmpty() }

            val shippingAddress = JSONObject()
                .put("street", address.streetAddress)
                .put("city", address.city)
                .put("state", address.state)
                .put("postalCode", address.pinCode)
                .put("country", address.country)
                .put("countryCode", "IN")
            val billingAddress = if (billing.sameAsShipping) JSONObject(shippingAddress.toString())
            else JSONObject(shippingAddress.toString())
                .put("street", billing.street)
                .put("city", billing.city)
                .put("state", billing.state)
                .put("postalCode", billing.postalCode)
            val enrichedShipping = JSONObject(shippingAddress.toString())
                .put("full_name", customerName.ifEmpty { JSONObject.NULL })
                .put("phone", customerPhone.ifEmpty { JSONObject.NULL })
                .put("expected_delivery_days", if (tat.maxTatDays != 0) tat.maxTatDays else JSONObject.NULL)
                .put("expected_delivery_date", tat.maxExpectedDate.ifEmpty { JSONObject.NULL })

            val productMeta = CheckoutApi.fetchProductMeta(items.map { it.productId }, token)
            val rpcParams = CheckoutApi.buildRpcParams(
                customerId = uid,
                items = items,
                productMeta = productMeta,
                shippingAddress = enrichedShipping,
                billingAddress = billingAddress,
                customerPhone = customerPhone,
                notes = address.deliveryNotes,
                paymentIntentId = paymentIntentId,
                shippingAmount = quote.shipping,
                actualShippingCost = quote.actualShippingCost,
                platformShippingMargin = quote.platformShippingMargin,
                expectedDeliveryDate = tat.maxExpectedDate,
                expectedDeliveryDays = tat.maxTatDays
            )

            if (!CheckoutApi.saveSnapshot(token, uid, paymentIntentId, rpcParams)) {
                _checkoutState.value = CheckoutState.Failure("Unable to prepare secure order recovery right now. Please try again.")
                return
            }

            val result = RazorpayLauncher.open(
                activity, rzpOrder, customerName, customerEmail, customerPhone,
                "${items.size} item${if (items.size != 1) "s" else ""}"
            )
            val payment = when (result) {
                is RazorpayLauncher.Result.Dismissed -> {
                    _checkoutState.value = CheckoutState.Idle
                    return
                }
                is RazorpayLauncher.Result.Failed -> throw CheckoutException("Payment could not be completed right now. Please try again.")
                is RazorpayLauncher.Result.Success -> result.payment
            }
            paymentTaken = true

            var outcome = CheckoutApi.verifyPayment(checkoutToken(), payment)
            var attempt = 0
            while (attempt < 2 && outcome !is VerifyOutcome.Confirmed && !(outcome is VerifyOutcome.Error && outcome.fatal)) {
                kotlinx.coroutines.delay(1500L * (attempt + 1))
                outcome = CheckoutApi.verifyPayment(checkoutToken(), payment)
                attempt++
            }

            var orderId: String? = (outcome as? VerifyOutcome.Confirmed)?.orderId
            if (orderId == null && !(outcome is VerifyOutcome.Error && outcome.fatal)) {
                val recovered = CheckoutApi.waitForOrder(checkoutToken(), paymentIntentId, 12_000, 1_500)
                if (recovered != null && recovered.second in listOf("completed", "succeeded", "paid")) orderId = recovered.first
            }

            if (orderId != null) {
                finishCheckout(uid)
                _checkoutState.value = CheckoutState.Success(orderId)
                return
            }

            if (outcome is VerifyOutcome.Error && outcome.fatal) {
                _checkoutState.value = CheckoutState.Failure("${outcome.message} Payment reference: ${payment.paymentId}")
                return
            }

            // Money was taken but confirmation is still in flight: show the order as processing.
            val tempId = nextTempOrderId()
            val temp = Order(
                id = tempId,
                orderNumber = tempId,
                date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()),
                status = OrderStatus.PROCESSING,
                items = items.map {
                    val cart = _cartItems.value.firstOrNull { c -> c.id == it.cartItemId }
                    OrderItem(
                        product = cart?.product ?: Product(id = it.productId, title = it.productName, price = it.unitPrice, imageUrl = it.productImage),
                        quantity = it.quantity,
                        unitPrice = it.unitPrice
                    )
                },
                subtotal = quote.subtotal,
                shippingFee = quote.shipping,
                discount = quote.offerDiscount,
                totalAmount = quote.total,
                shippingAddress = address,
                paymentMethod = "razorpay",
                paymentStatus = "pending"
            )
            _tempOrders.value = listOf(temp) + _tempOrders.value.filterNot { it.id == tempId }.take(19)
            finishCheckout(uid)
            _checkoutState.value = CheckoutState.Success(tempId)
        } catch (e: Exception) {
            if (paymentTaken) {
                val recovered = runCatching { CheckoutApi.waitForOrder(checkoutToken(), paymentIntentId, 12_000, 1_500) }.getOrNull()
                val uid = session?.userId
                if (recovered != null && uid != null) {
                    finishCheckout(uid)
                    _checkoutState.value = CheckoutState.Success(recovered.first)
                    return
                }
            }
            _checkoutState.value = CheckoutState.Failure(
                e.message?.takeIf { it.isNotBlank() } ?: "Payment could not be completed right now. Please try again."
            )
        }
    }

    /** Web `clearCheckoutCartState` followed by loading the new order. */
    private suspend fun finishCheckout(uid: String) {
        validToken()?.let { UserDataApi.clearCart(it, uid) }
        _cartItems.value = emptyList()
        checkoutSelectedIds.value = emptySet()
        loadOrders(uid)
    }

    /** Restores a persisted session (token refresh + profile/ban check), like the web INITIAL_SESSION flow. */
    fun init(context: android.content.Context) {
        if (sessionStore != null) return
        appContext = context.applicationContext
        val store = SessionStore(context.applicationContext)
        sessionStore = store
        // Web `beauzead_wishlist` local storage: the wishlist survives restarts.
        val savedWishlist = appContext?.getSharedPreferences("bzead_wishlist", android.content.Context.MODE_PRIVATE)
            ?.getString("ids", null)
        if (savedWishlist != null) {
            try {
                val arr = org.json.JSONArray(savedWishlist)
                _wishlistProductIds.value = (0 until arr.length()).map { arr.getString(it) }.toSet()
            } catch (_: Exception) { }
        }
        val refreshToken = store.loadRefreshToken() ?: return
        authScope.launch {
            val res = SupabaseAuth.refresh(refreshToken)
            val fresh = res.session
            if (fresh == null) {
                // Only drop the stored session when the server rejected it, not on network failure.
                if (res.body != null) store.clear()
                return@launch
            }
            if (!establishSession(fresh).first) clearLocalSession()
        }
    }

    private fun loadUserData(uid: String) {
        authScope.launch { loadCart(uid) }
        authScope.launch { loadWishlist(uid) }
        authScope.launch { loadAddresses(uid) }
        authScope.launch { loadOrders(uid) }
        authScope.launch { loadNotifications(uid) }
    }

    private fun clearUserData() {
        _cartItems.value = emptyList()
        _wishlistProductIds.value = emptySet()
        _wishlistProducts.value = emptyList()
        saveWishlistLocally()
        _addresses.value = emptyList()
        selectedAddress.value = null
        _orders.value = emptyList()
        _notifications.value = emptyList()
        _unreadCount.value = 0
        _hasMoreNotifications.value = false
        notificationOffset = 0
    }

    private fun clearLocalSession() {
        session = null
        clearUserData()
        sessionStore?.clear()
        _isLoggedIn.value = false
        _userName.value = null
        _userEmail.value = null
        _userPhone.value = null
    }

    /** Loads the profile, rejects banned users, and publishes the session. Returns (ok, errorMessage). */
    private suspend fun establishSession(s: AuthSession): Pair<Boolean, String?> {
        val profile = SupabaseAuth.fetchProfile(s.accessToken, s.userId)
        if (profile?.optBoolean("is_banned", false) == true) {
            SupabaseAuth.signOut(s.accessToken)
            return Pair(false, "Your account has been suspended. Please contact support.")
        }
        session = s
        sessionStore?.saveRefreshToken(s.refreshToken)
        val meta = s.userMetadata
        _userEmail.value = profile?.optStringOrNull("email") ?: s.email
        _userName.value = profile?.optStringOrNull("full_name") ?: meta.optStringOrNull("full_name")
        _userPhone.value = profile?.optStringOrNull("phone") ?: meta.optStringOrNull("phone")
        _isLoggedIn.value = true
        loadUserData(s.userId)
        return Pair(true, null)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun roleOf(s: AuthSession, profile: JSONObject?): String =
        s.userMetadata.optStringOrNull("role")
            ?: s.appMetadata.optStringOrNull("role")
            ?: profile?.optStringOrNull("role")
            ?: "user"

    suspend fun login(email: String, password: String): LoginResult {
        val res = SupabaseAuth.signInWithPassword(email.trim().lowercase(), password)
        val s = res.session ?: return LoginResult(success = false, errorMessage = mapLoginError(res.error))

        val profile = SupabaseAuth.fetchProfile(s.accessToken, s.userId)
        if (profile?.optBoolean("is_banned", false) == true) {
            SupabaseAuth.signOut(s.accessToken)
            return LoginResult(false, "Your account has been suspended. Please contact support.")
        }
        val role = roleOf(s, profile)
        if (role == "seller" || role == "admin") {
            SupabaseAuth.signOut(s.accessToken)
            return LoginResult(success = false, role = role)
        }
        val (ok, err) = establishSession(s)
        return if (ok) LoginResult(success = true, role = role) else LoginResult(false, err)
    }

    private fun mapLoginError(message: String?): String {
        val m = message.orEmpty()
        return when {
            m.contains("Invalid login credentials") -> "Incorrect email or password."
            m.contains("Email not confirmed") -> "Please verify your email first. Check your inbox for the OTP code."
            m.contains("Database error querying schema") ->
                "Your account auth record is incomplete. Please contact support/admin to repair the account setup."
            m.isBlank() -> "Login failed. Please try again."
            else -> m
        }
    }

    suspend fun signUp(email: String, password: String, fullName: String, countryId: String, currency: String): Pair<Boolean, String?> {
        val data = JSONObject()
            .put("full_name", fullName)
            .put("role", "user")
            .put("currency", currency)
            .put("phone", "")
            .put("country_id", countryId)
        val res = SupabaseAuth.signUp(email.trim().lowercase(), password, data)
        if (!res.ok) {
            val msg = res.error.orEmpty()
            val l = msg.lowercase()
            val exists = listOf("already exists", "already registered", "already been registered", "duplicate key").any { l.contains(it) }
            return Pair(false, if (exists) EXISTING_EMAIL_MESSAGE else msg.ifBlank { "Failed to sign up" })
        }
        val user = res.body?.optJSONObject("user") ?: res.body
        // supabase-js behavior: empty identities means the email is already registered.
        if (user?.optJSONArray("identities")?.length() == 0) {
            return Pair(false, EXISTING_EMAIL_MESSAGE)
        }
        return Pair(true, null)
    }

    /** Verifies the signup OTP; success signs the user in. Returns [ALREADY_CONFIRMED] if the account is already verified. */
    suspend fun verifyOtp(email: String, code: String, type: String): Pair<Boolean, String?> {
        val res = SupabaseAuth.verifyOtp(email.trim().lowercase(), code, "signup")
        val s = res.session
        if (s == null) {
            return if (res.error?.contains("User already registered") == true) Pair(false, ALREADY_CONFIRMED)
            else Pair(false, res.error ?: "Verification failed. Please try again.")
        }
        val (ok, err) = establishSession(s)
        if (!ok) return Pair(false, err)
        s.userMetadata.optStringOrNull("country_id")?.let {
            SupabaseAuth.fillProfileCountry(s.accessToken, s.userId, it)
        }
        return Pair(true, null)
    }

    suspend fun resendOtp(email: String, type: String): Pair<Boolean, String?> {
        val e = email.trim().lowercase()
        val res = if (type == "password-reset") SupabaseAuth.recover(e) else SupabaseAuth.resendSignup(e)
        return if (res.ok) Pair(true, null) else Pair(false, res.error)
    }

    suspend fun resetPassword(email: String): Pair<Boolean, String?> {
        val res = SupabaseAuth.recover(email.trim().lowercase())
        return if (res.ok) Pair(true, null) else Pair(false, res.error)
    }

    /** Verifies the recovery OTP and sets the new password, then ends the temporary recovery session. */
    suspend fun confirmPasswordReset(email: String, code: String, newPassword: String): Pair<Boolean, String?> {
        val v = SupabaseAuth.verifyOtp(email.trim().lowercase(), code, "recovery")
        val s = v.session ?: return Pair(false, v.error ?: "Invalid or expired code.")
        val u = SupabaseAuth.updateUser(s.accessToken, JSONObject().put("password", newPassword))
        SupabaseAuth.signOut(s.accessToken, "local")
        return if (u.ok) Pair(true, null)
        else Pair(
            false,
            "Password update failed after code verification. Please request a new reset link and try again. (${u.error})"
        )
    }

    suspend fun logout() {
        val token = session?.accessToken
        if (token != null) {
            val res = SupabaseAuth.signOut(token)
            if (!res.ok) SupabaseAuth.signOut(token, "local")
        }
        clearLocalSession()
    }
}
