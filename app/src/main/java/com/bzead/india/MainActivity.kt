package com.bzead.india

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.bzead.india.data.BzeadRepository
import com.bzead.india.theme.BzeadHeaderNavy
import com.bzead.india.ui.components.BottomTab
import com.bzead.india.ui.components.BzeadBottomNav
import com.bzead.india.ui.screens.CartScreen
import com.bzead.india.ui.screens.CategoryScreen
import com.bzead.india.ui.screens.CheckoutScreen
import com.bzead.india.ui.screens.ForgotPasswordScreen
import com.bzead.india.ui.screens.HomeScreen
import com.bzead.india.ui.screens.LoginScreen
import com.bzead.india.ui.screens.NotificationsScreen
import com.bzead.india.ui.screens.NewPasswordScreen
import com.bzead.india.ui.screens.OTPVerificationScreen
import com.bzead.india.ui.screens.OrderDetailScreen
import com.bzead.india.ui.screens.OrderSuccessScreen
import com.bzead.india.ui.screens.OrdersScreen
import com.bzead.india.ui.screens.PolicyScreen
import com.bzead.india.ui.screens.ProductDetailScreen
import com.bzead.india.ui.screens.ProfileScreen
import com.bzead.india.ui.screens.SearchScreen
import com.bzead.india.ui.screens.SignupScreen
import com.bzead.india.ui.screens.WishlistScreen
import com.bzead.india.ui.splash.SplashScreen
import com.bzead.india.ui.theme.MyApplicationTheme

import com.bzead.india.ui.screens.ShippingAddressScreen
import com.bzead.india.ui.screens.OrderSummaryScreen
import com.bzead.india.ui.screens.AddressManagementScreen
import com.bzead.india.ui.screens.WriteReviewScreen
import com.bzead.india.ui.screens.SettingsScreen
import com.bzead.india.ui.screens.SectionProductsScreen
import com.bzead.india.ui.screens.ContactScreen
import com.bzead.india.ui.screens.NotFoundScreen
import com.bzead.india.ui.screens.OfflineScreen
import com.bzead.india.ui.screens.rememberNetworkStatus

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Home : AppScreen()
    data object Category : AppScreen()
    data class CategoryDetail(val categoryId: String) : AppScreen()
    data object Cart : AppScreen()
    data object Profile : AppScreen()
    data object Search : AppScreen()
    data class ProductDetail(val productId: String) : AppScreen()
    data object Wishlist : AppScreen()
    data object Orders : AppScreen()
    data class OrderDetail(val orderId: String) : AppScreen()
    data class OrderSuccess(val orderId: String) : AppScreen()
    data object ShippingAddress : AppScreen()
    data object OrderSummary : AppScreen()
    data object Checkout : AppScreen()
    data object Login : AppScreen()
    data object Signup : AppScreen()
    data class OTPVerification(val email: String, val fullName: String = "", val purpose: String = "signup") : AppScreen()
    data class NewPassword(val email: String, val code: String) : AppScreen()
    data object ForgotPassword : AppScreen()
    data object Notifications : AppScreen()
    data class Policy(val type: String) : AppScreen()
    data object AddressManagement : AppScreen()
    data class WriteReview(val productId: String) : AppScreen()
    data object Settings : AppScreen()
    data class SectionProducts(val sectionId: String) : AppScreen()
    data object Contact : AppScreen()
    data object NotFound : AppScreen()
}

private fun AppScreen.toSavedRoute(): ArrayList<String> = ArrayList(when (this) {
    AppScreen.Splash -> listOf("splash")
    AppScreen.Home -> listOf("home")
    AppScreen.Category -> listOf("category")
    is AppScreen.CategoryDetail -> listOf("category-detail", categoryId)
    AppScreen.Cart -> listOf("cart")
    AppScreen.Profile -> listOf("profile")
    AppScreen.Search -> listOf("search")
    is AppScreen.ProductDetail -> listOf("product-detail", productId)
    AppScreen.Wishlist -> listOf("wishlist")
    AppScreen.Orders -> listOf("orders")
    is AppScreen.OrderDetail -> listOf("order-detail", orderId)
    is AppScreen.OrderSuccess -> listOf("order-success", orderId)
    AppScreen.ShippingAddress -> listOf("shipping-address")
    AppScreen.OrderSummary -> listOf("order-summary")
    AppScreen.Checkout -> listOf("checkout")
    AppScreen.Login -> listOf("login")
    AppScreen.Signup -> listOf("signup")
    is AppScreen.OTPVerification -> listOf("otp", email, fullName, purpose)
    is AppScreen.NewPassword -> listOf("new-password", email, code)
    AppScreen.ForgotPassword -> listOf("forgot-password")
    AppScreen.Notifications -> listOf("notifications")
    is AppScreen.Policy -> listOf("policy", type)
    AppScreen.AddressManagement -> listOf("address-management")
    is AppScreen.WriteReview -> listOf("write-review", productId)
    AppScreen.Settings -> listOf("settings")
    is AppScreen.SectionProducts -> listOf("section-products", sectionId)
    AppScreen.Contact -> listOf("contact")
    AppScreen.NotFound -> listOf("not-found")
})

private fun List<String>.toAppScreen(): AppScreen = when (firstOrNull()) {
    "splash" -> AppScreen.Splash
    "category" -> AppScreen.Category
    "category-detail" -> getOrNull(1)?.let { AppScreen.CategoryDetail(it) } ?: AppScreen.Home
    "cart" -> AppScreen.Cart
    "profile" -> AppScreen.Profile
    "search" -> AppScreen.Search
    "product-detail" -> getOrNull(1)?.let { AppScreen.ProductDetail(it) } ?: AppScreen.Home
    "wishlist" -> AppScreen.Wishlist
    "orders" -> AppScreen.Orders
    "order-detail" -> getOrNull(1)?.let { AppScreen.OrderDetail(it) } ?: AppScreen.Home
    "order-success" -> getOrNull(1)?.let { AppScreen.OrderSuccess(it) } ?: AppScreen.Home
    "shipping-address" -> AppScreen.ShippingAddress
    "order-summary" -> AppScreen.OrderSummary
    "checkout" -> AppScreen.Checkout
    "login" -> AppScreen.Login
    "signup" -> AppScreen.Signup
    "otp" -> getOrNull(1)?.let { AppScreen.OTPVerification(it, getOrNull(2).orEmpty(), getOrNull(3) ?: "signup") } ?: AppScreen.Login
    "new-password" -> if (size >= 3) AppScreen.NewPassword(this[1], this[2]) else AppScreen.ForgotPassword
    "forgot-password" -> AppScreen.ForgotPassword
    "notifications" -> AppScreen.Notifications
    "policy" -> getOrNull(1)?.let { AppScreen.Policy(it) } ?: AppScreen.Home
    "address-management" -> AppScreen.AddressManagement
    "write-review" -> getOrNull(1)?.let { AppScreen.WriteReview(it) } ?: AppScreen.Home
    "settings" -> AppScreen.Settings
    "section-products" -> getOrNull(1)?.let { AppScreen.SectionProducts(it) } ?: AppScreen.Home
    "contact" -> AppScreen.Contact
    "not-found" -> AppScreen.NotFound
    else -> AppScreen.Home
}

private fun AppScreen.requiresBuyerAuth(): Boolean = when (this) {
    AppScreen.Cart, AppScreen.Checkout, AppScreen.Orders, AppScreen.Wishlist,
    AppScreen.Notifications, AppScreen.Profile, AppScreen.ShippingAddress,
    AppScreen.OrderSummary, AppScreen.AddressManagement, AppScreen.Settings,
    is AppScreen.OrderDetail, is AppScreen.OrderSuccess, is AppScreen.WriteReview -> true
    else -> false
}

class MainActivity : ComponentActivity(), com.razorpay.PaymentResultWithDataListener {
    override fun onPaymentSuccess(razorpayPaymentId: String?, data: com.razorpay.PaymentData?) {
        com.bzead.india.data.RazorpayLauncher.onSuccess(data, razorpayPaymentId)
    }

    override fun onPaymentError(code: Int, description: String?, data: com.razorpay.PaymentData?) {
        com.bzead.india.data.RazorpayLauncher.onError(code, description)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.bzead.india.data.BzeadRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as? Activity)?.window
                        window?.statusBarColor = BzeadHeaderNavy.toArgb()
                        window?.navigationBarColor = BzeadHeaderNavy.toArgb()
                    }
                }

                val appScreenSaver = remember {
                    Saver<AppScreen, ArrayList<String>>(
                        save = { it.toSavedRoute() },
                        restore = { it.toAppScreen() }
                    )
                }
                val nullableAppScreenSaver = remember {
                    Saver<AppScreen?, ArrayList<String>>(
                        save = { it?.toSavedRoute() },
                        restore = { it?.toAppScreen() }
                    )
                }
                var currentScreen by rememberSaveable(stateSaver = appScreenSaver) {
                    mutableStateOf<AppScreen>(AppScreen.Splash)
                }
                var pendingLoginDestination by rememberSaveable(stateSaver = nullableAppScreenSaver) {
                    mutableStateOf<AppScreen?>(null)
                }
                var loginSuccessMessage by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                val cartItems by BzeadRepository.cartItems.collectAsState()
                val cartCount = cartItems.sumOf { it.quantity }
                val isLoggedIn by BzeadRepository.isLoggedIn.collectAsState()
                val authLoading by BzeadRepository.isAuthLoading.collectAsState()

                androidx.compose.runtime.LaunchedEffect(currentScreen, isLoggedIn, authLoading) {
                    if (authLoading) return@LaunchedEffect
                    if (!isLoggedIn && currentScreen.requiresBuyerAuth()) {
                        pendingLoginDestination = currentScreen
                        currentScreen = AppScreen.Login
                    } else if (isLoggedIn && (currentScreen == AppScreen.Login || currentScreen == AppScreen.Signup)) {
                        currentScreen = AppScreen.Home
                    }
                }

                val currentBottomTab = when (currentScreen) {
                    AppScreen.Home -> BottomTab.HOME
                    AppScreen.Orders -> BottomTab.ORDERS
                    AppScreen.Category -> BottomTab.CATEGORY
                    is AppScreen.CategoryDetail -> BottomTab.CATEGORY
                    AppScreen.Cart -> BottomTab.CART
                    AppScreen.Profile -> BottomTab.ACCOUNT
                    AppScreen.Login -> BottomTab.ACCOUNT
                    else -> null
                }

                val linkScope = rememberCoroutineScope()
                val openLink: (String) -> Unit = { raw ->
                    val link = raw.trim()
                    if (link.startsWith("/")) {
                        // Relative links resolve against the web app's routes (App.tsx).
                        val parts = link.substringBefore('?').substringBefore('#').trim('/').split('/')
                        when {
                            parts.size == 3 && parts[0] == "products" && parts[1] == "section" -> currentScreen = AppScreen.SectionProducts(parts[2])
                            parts.size == 2 && parts[0] == "products" && (parts[1] == "featured" || parts[1] == "hot-deals") ->
                                currentScreen = AppScreen.SectionProducts(parts[1])
                            parts.size == 2 && parts[0] == "category" -> currentScreen = AppScreen.CategoryDetail(parts[1])
                            parts.size == 2 && (parts[0] == "products" || parts[0] == "share") && parts[1] != "section" ->
                                linkScope.launch {
                                    BzeadRepository.resolveProductRef(parts[1])?.let { currentScreen = AppScreen.ProductDetail(it) }
                                }
                            parts.size == 1 && parts[0] == "cart" -> currentScreen = AppScreen.Cart
                            parts.size == 1 && parts[0] == "wishlist" -> currentScreen = AppScreen.Wishlist
                            parts.size == 1 && parts[0] == "orders" -> currentScreen = AppScreen.Orders
                            parts.size == 1 && parts[0] == "notifications" -> currentScreen = AppScreen.Notifications
                            parts.size == 1 && parts[0] == "profile" -> currentScreen = AppScreen.Profile
                            parts.size == 1 && parts[0] == "contact" -> currentScreen = AppScreen.Contact
                            parts.size == 1 && parts[0] == "shipping-policy" -> currentScreen = AppScreen.Policy("shipping")
                            parts.size == 1 && parts[0] == "refund-policy" -> currentScreen = AppScreen.Policy("refund")
                            parts.size == 1 && parts[0] == "privacy-policy" -> currentScreen = AppScreen.Policy("privacy")
                            parts.size == 1 && parts[0] == "terms-and-conditions" -> currentScreen = AppScreen.Policy("terms")
                            parts.size == 1 && parts[0] == "terms-of-service" -> currentScreen = AppScreen.Policy("terms-of-service")
                            parts.size == 1 && parts[0] == "about" -> currentScreen = AppScreen.Policy("about")
                            parts.size == 1 && parts[0] == "not-found" -> currentScreen = AppScreen.NotFound
                            parts.size == 1 && (parts[0] == "all-categories" || parts[0].isEmpty()) -> currentScreen = AppScreen.Home
                            else -> currentScreen = AppScreen.NotFound
                        }
                    } else if (link.startsWith("http://") || link.startsWith("https://")) {
                        try {
                            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link)))
                        } catch (e: Exception) {
                        }
                    }
                }

                BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
                    currentScreen = AppScreen.Home
                }

                val networkStatus = rememberNetworkStatus()
                Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BzeadHeaderNavy,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (currentBottomTab != null) {
                            BzeadBottomNav(
                                currentTab = currentBottomTab,
                                onTabSelected = { tab ->
                                    currentScreen = when (tab) {
                                        BottomTab.HOME -> AppScreen.Home
                                        BottomTab.ORDERS -> AppScreen.Orders
                                        BottomTab.CATEGORY -> AppScreen.Category
                                        BottomTab.CART -> if (isLoggedIn) AppScreen.Cart else AppScreen.Login
                                        BottomTab.ACCOUNT -> if (isLoggedIn) AppScreen.Profile else AppScreen.Login
                                    }
                                },
                                isLoggedIn = isLoggedIn,
                                cartCount = cartCount
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Crossfade(
                            targetState = currentScreen,
                            animationSpec = tween(durationMillis = 350),
                            label = "MainCrossfade"
                        ) { screen ->
                            when (screen) {
                                is AppScreen.Splash -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            currentScreen = AppScreen.Home
                                        }
                                    )
                                }
                                is AppScreen.Home -> {
                                    HomeScreen(
                                        onNavigateToSearch = { currentScreen = AppScreen.Search },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        onNavigateToCategory = { id -> currentScreen = AppScreen.CategoryDetail(id) },
                                        onNavigateToSection = { id -> currentScreen = AppScreen.SectionProducts(id) },
                                        onOpenLink = openLink,
                                        onNavigateToNotifications = { currentScreen = AppScreen.Notifications },
                                        onNavigateToCart = { currentScreen = AppScreen.Cart },
                                        onNavigateToWishlist = { currentScreen = AppScreen.Wishlist },
                                        onNavigateToAccount = { currentScreen = AppScreen.Profile },
                                        onNavigateToOrders = { currentScreen = AppScreen.Orders },
                                        onNavigateToContact = { currentScreen = AppScreen.Contact },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToSignup = { currentScreen = AppScreen.Signup },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is AppScreen.CategoryDetail -> {
                                    CategoryScreen(
                                        initialCategoryId = screen.categoryId,
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        onNavigateToSearch = { currentScreen = AppScreen.Search },
                                        onNavigateToCart = { currentScreen = AppScreen.Cart },
                                        onNavigateToWishlist = { currentScreen = AppScreen.Wishlist },
                                        snackbarHostState = snackbarHostState,
                                        isCategoryPage = true
                                    )
                                }
                                is AppScreen.Category -> {
                                    CategoryScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        onNavigateToSearch = { currentScreen = AppScreen.Search },
                                        onNavigateToCart = { currentScreen = AppScreen.Cart },
                                        onNavigateToWishlist = { currentScreen = AppScreen.Wishlist },
                                        snackbarHostState = snackbarHostState
                                    )
                                }

                                is AppScreen.Cart -> {
                                    CartScreen(
                                        onNavigateToHome = { currentScreen = AppScreen.Home },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        onProceedToCheckout = { currentScreen = AppScreen.ShippingAddress }
                                    )
                                }
                                is AppScreen.ShippingAddress -> {
                                    ShippingAddressScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Cart },
                                        onProceedToSummary = { currentScreen = AppScreen.OrderSummary }
                                    )
                                }
                                is AppScreen.OrderSummary -> {
                                    OrderSummaryScreen(
                                        onNavigateBack = { currentScreen = AppScreen.ShippingAddress },
                                        onProceedToPayment = { currentScreen = AppScreen.Checkout },
                                        onNavigateToCart = { currentScreen = AppScreen.Cart }
                                    )
                                }
                                is AppScreen.Profile -> {
                                    ProfileScreen(
                                        onNavigateToOrders = { currentScreen = AppScreen.Orders },
                                        onNavigateToWishlist = { currentScreen = AppScreen.Wishlist },
                                        onNavigateToNotifications = { currentScreen = AppScreen.Notifications },
                                        onNavigateToPolicy = { type -> currentScreen = AppScreen.Policy(type) },
                                        onNavigateToAddresses = { currentScreen = AppScreen.AddressManagement },
                                        onNavigateToSettings = { currentScreen = AppScreen.Settings },
                                        onNavigateToContact = { currentScreen = AppScreen.Contact },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToSignup = { currentScreen = AppScreen.Signup },
                                        onNavigateToHome = {
                                            pendingLoginDestination = null
                                            currentScreen = AppScreen.Home
                                        },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is AppScreen.Search -> {
                                    SearchScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is AppScreen.ProductDetail -> {
                                    ProductDetailScreen(
                                        productId = screen.productId,
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToCart = { currentScreen = AppScreen.Cart },
                                        onNavigateToWriteReview = { id -> currentScreen = AppScreen.WriteReview(id) },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToAddresses = { currentScreen = AppScreen.AddressManagement },
                                        onNavigateHome = { currentScreen = AppScreen.Home },
                                        onNavigateToCategory = { id -> currentScreen = AppScreen.CategoryDetail(id) },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is AppScreen.AddressManagement -> {
                                    AddressManagementScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Profile }
                                    )
                                }
                                is AppScreen.WriteReview -> {
                                    WriteReviewScreen(
                                        productId = screen.productId,
                                        onNavigateBack = { currentScreen = AppScreen.ProductDetail(screen.productId) },
                                        onReviewSubmitted = { currentScreen = AppScreen.ProductDetail(screen.productId) },
                                        onGoHome = { currentScreen = AppScreen.Home }
                                    )
                                }
                                is AppScreen.Settings -> {
                                    SettingsScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Profile }
                                    )
                                }
                                is AppScreen.SectionProducts -> {
                                    SectionProductsScreen(
                                        sectionId = screen.sectionId,
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onProductClick = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToSearch = { currentScreen = AppScreen.Search }
                                    )
                                }
                                is AppScreen.Contact -> {
                                    ContactScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Profile }
                                    )
                                }
                                is AppScreen.Wishlist -> {
                                    WishlistScreen(
                                        onNavigateToHome = { currentScreen = AppScreen.Home },
                                        onNavigateToProduct = { id -> currentScreen = AppScreen.ProductDetail(id) },
                                        snackbarHostState = snackbarHostState,
                                        onNavigateToLogin = { currentScreen = AppScreen.Login }
                                    )
                                }
                                is AppScreen.Checkout -> {
                                    CheckoutScreen(
                                        onNavigateBack = { currentScreen = AppScreen.OrderSummary },
                                        onOrderPlaced = { orderId -> currentScreen = AppScreen.OrderSuccess(orderId) }
                                    )
                                }
                                is AppScreen.OrderSuccess -> {
                                    OrderSuccessScreen(
                                        orderId = screen.orderId,
                                        onViewOrder = { orderId -> currentScreen = AppScreen.OrderDetail(orderId) },
                                        onContinueShopping = { currentScreen = AppScreen.Home },
                                        onViewOrders = { currentScreen = AppScreen.Orders },
                                        onContactSupport = { currentScreen = AppScreen.Contact }
                                    )
                                }
                                is AppScreen.Orders -> {
                                    OrdersScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Profile },
                                        onNavigateToOrderDetail = { id -> currentScreen = AppScreen.OrderDetail(id) },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login }
                                    )
                                }
                                is AppScreen.OrderDetail -> {
                                    OrderDetailScreen(
                                        orderId = screen.orderId,
                                        onNavigateBack = { currentScreen = AppScreen.Orders },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                                is AppScreen.Login -> {
                                    LoginScreen(
                                        onNavigateBack = {
                                            pendingLoginDestination = null
                                            currentScreen = AppScreen.Home
                                        },
                                        onNavigateToSignup = {
                                            pendingLoginDestination = null
                                            currentScreen = AppScreen.Signup
                                        },
                                        onNavigateToForgotPassword = {
                                            pendingLoginDestination = null
                                            currentScreen = AppScreen.ForgotPassword
                                        },
                                        onLoginSuccess = {
                                            currentScreen = pendingLoginDestination ?: AppScreen.Home
                                            pendingLoginDestination = null
                                            loginSuccessMessage = null
                                        },
                                        initialSuccessMessage = loginSuccessMessage
                                    )
                                }
                                is AppScreen.Signup -> {
                                    SignupScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToOtp = { email, name -> currentScreen = AppScreen.OTPVerification(email, name) }
                                    )
                                }
                                is AppScreen.OTPVerification -> {
                                    OTPVerificationScreen(
                                        email = screen.email,
                                        purpose = screen.purpose,
                                        onNavigateBack = {
                                            currentScreen = if (screen.purpose == "password-reset") AppScreen.ForgotPassword else AppScreen.Signup
                                        },
                                        onVerificationSuccess = { currentScreen = AppScreen.Home },
                                        onNavigateHome = { currentScreen = AppScreen.Home },
                                        onResetCodeEntered = { code -> currentScreen = AppScreen.NewPassword(screen.email, code) },
                                        onAlreadyConfirmed = { currentScreen = AppScreen.Login }
                                    )
                                }
                                is AppScreen.ForgotPassword -> {
                                    ForgotPasswordScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                                        onNavigateToOtp = { email -> currentScreen = AppScreen.OTPVerification(email, purpose = "password-reset") }
                                    )
                                }
                                is AppScreen.NewPassword -> {
                                    NewPasswordScreen(
                                        email = screen.email,
                                        code = screen.code,
                                        onNavigateBack = { currentScreen = AppScreen.OTPVerification(screen.email, purpose = "password-reset") },
                                        onPasswordReset = {
                                            loginSuccessMessage = "Password reset successfully! Please login with your new password."
                                            currentScreen = AppScreen.Login
                                        },
                                        onNavigateHome = { currentScreen = AppScreen.Home }
                                    )
                                }
                                is AppScreen.Notifications -> {
                                    NotificationsScreen(
                                        onNavigateBack = { currentScreen = AppScreen.Home },
                                        onNavigateToOrder = { id -> currentScreen = AppScreen.OrderDetail(id) },
                                        onNavigateToLogin = { currentScreen = AppScreen.Login }
                                    )
                                }
                                is AppScreen.NotFound -> {
                                    NotFoundScreen(
                                        onGoHome = { currentScreen = AppScreen.Home },
                                        onGoBack = { currentScreen = AppScreen.Home }
                                    )
                                }
                                is AppScreen.Policy -> {
                                    PolicyScreen(
                                        policyType = screen.type,
                                        onNavigateBack = { currentScreen = AppScreen.Profile },
                                        onNavigateHome = { currentScreen = AppScreen.Home }
                                    )
                                }
                            }
                        }
                    }
                }
                if (!networkStatus.isOnline) {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E293B)) {
                        OfflineScreen(onRetry = networkStatus.refresh)
                    }
                }
                }
            }
        }
    }
}
