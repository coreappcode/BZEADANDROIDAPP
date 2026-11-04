package com.bzead.india.data

import com.bzead.india.model.CartItem
import com.bzead.india.model.DeliveryAddress
import com.bzead.india.model.LoginResult
import com.bzead.india.model.NotificationItem
import com.bzead.india.model.Order
import com.bzead.india.model.OrderItem
import com.bzead.india.model.OrderStatus
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

    private val _heroBanners = MutableStateFlow<List<HeroBanner>>(SampleCatalogData.liveHeroBanners)
    val heroBanners: StateFlow<List<HeroBanner>> = _heroBanners.asStateFlow()

    private val _adBanners = MutableStateFlow<List<AdBanner>>(SampleCatalogData.liveAdBanners)
    val adBanners: StateFlow<List<AdBanner>> = _adBanners.asStateFlow()

    private val _liveCategories = MutableStateFlow<List<CategoryItem>>(SampleCatalogData.categories)
    val liveCategories: StateFlow<List<CategoryItem>> = _liveCategories.asStateFlow()

    private val _featuredProducts = MutableStateFlow<List<Product>>(SampleCatalogData.products.filter { it.isFeatured })
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()

    private val _trendingProducts = MutableStateFlow<List<Product>>(SampleCatalogData.products.filter { it.isTrending })
    val trendingProducts: StateFlow<List<Product>> = _trendingProducts.asStateFlow()

    private val _hotDealsProducts = MutableStateFlow<List<Product>>(SampleCatalogData.products.filter { it.isDeal })
    val hotDealsProducts: StateFlow<List<Product>> = _hotDealsProducts.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(SampleCatalogData.products)
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _mainDepartments = MutableStateFlow<List<CategoryItem>>(SampleCatalogData.mainDepartments)
    val mainDepartments: StateFlow<List<CategoryItem>> = _mainDepartments.asStateFlow()

    private val _categorySectionsMap = MutableStateFlow<Map<String, List<SubCategorySection>>>(SampleCatalogData.departmentSectionsMap)
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

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("Headphones", "Mamaearth Sunscreen", "Smartwatch", "Sneakers", "Coffee Machine")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    suspend fun refreshHomeContent() {
        _isHomeLoading.value = true
        try {
            val (hero, ad) = SupabaseClient.fetchBanners()
            if (hero.isNotEmpty()) _heroBanners.value = hero
            if (ad.isNotEmpty()) _adBanners.value = ad

            val (mains, tree) = SupabaseClient.fetchCategoryTree()
            if (mains.isNotEmpty()) _mainDepartments.value = mains
            if (tree.isNotEmpty()) _categorySectionsMap.value = tree

            val cats = SupabaseClient.fetchCategories()
            if (cats.isNotEmpty()) _liveCategories.value = cats

            val prods = SupabaseClient.fetchProducts()
            if (prods.isNotEmpty()) {
                _allProducts.value = prods
                val feat = prods.filter { it.isFeatured }
                if (feat.isNotEmpty()) _featuredProducts.value = feat
                val trend = prods.filter { it.isTrending }
                if (trend.isNotEmpty()) _trendingProducts.value = trend
                val deals = prods.filter { it.isDeal }
                if (deals.isNotEmpty()) _hotDealsProducts.value = deals
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isHomeLoading.value = false
        }
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
        val size = variant?.size
        val color = variant?.color
        val sku = variant?.sku
        val variantPrice = variant?.let { it.price ?: (product.price + it.priceDelta) }
        val key = UserDataApi.cartItemId(product.id, size, color, sku)
        val list = _cartItems.value.toMutableList()
        val idx = list.indexOfFirst { it.id == key }
        val item = if (idx >= 0) {
            list[idx].let { it.copy(quantity = it.quantity + 1, variantPrice = variantPrice ?: it.variantPrice) }.also { list[idx] = it }
        } else {
            CartItem(
                id = key, product = product, quantity = 1, selectedSize = size, selectedColor = color,
                selectedVariantId = variant?.id, selectedVariantSku = sku, variantPrice = variantPrice
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
    }

    fun isInCart(productId: String): Boolean {
        return _cartItems.value.any { it.product.id == productId }
    }

    fun getProductById(id: String): Product? {
        return _allProducts.value.firstOrNull { it.id == id }
    }

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return emptyList()
        return _allProducts.value.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.brand.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        persist { t, uid -> UserDataApi.markAllNotificationsRead(t, uid).error }
    }

    fun markNotificationAsRead(id: String) {
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
        val lowerSection = section.lowercase()
        return _allProducts.value.filter { product ->
            when (lowerSection) {
                "featured" -> product.isFeatured
                "hot-deals", "deals" -> product.isDeal || product.discountPercent > 15
                "trending" -> product.isTrending || product.rating >= 4.5f
                else -> true
            }
        }
    }

    suspend fun submitReview(productId: String, rating: Int, title: String, comment: String): Boolean {
        return true
    }

    suspend fun updateUserProfile(fullName: String, phone: String): Boolean {
        _userName.value = fullName
        _userPhone.value = phone
        return true
    }

    fun placeOrder(paymentMethod: String, address: DeliveryAddress): Order {
        val currentCart = _cartItems.value
        val items = currentCart.map { OrderItem(product = it.product, quantity = it.quantity, unitPrice = it.unitPrice) }
        val subtotal = items.sumOf { it.totalPrice }
        val newOrder = Order(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = "BZ-${(10000..99999).random()}",
            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date()),
            status = OrderStatus.PENDING,
            items = items,
            subtotal = subtotal,
            shippingFee = if (subtotal >= 499.0) 0.0 else 40.0,
            shippingAddress = address,
            paymentMethod = paymentMethod
        )
        _orders.value = listOf(newOrder) + _orders.value
        // Local-only until checkout persistence (create_order_secure) is implemented; do not delete backend cart rows without a real order.
        _cartItems.value = emptyList()
        return newOrder
    }

    /** Restores a persisted session (token refresh + profile/ban check), like the web INITIAL_SESSION flow. */
    fun init(context: android.content.Context) {
        if (sessionStore != null) return
        val store = SessionStore(context.applicationContext)
        sessionStore = store
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
        _addresses.value = emptyList()
        selectedAddress.value = null
        _orders.value = emptyList()
        _notifications.value = emptyList()
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
