# Android and Web App Mismatch Audit

## Scope

Compared the Android app in this workspace with `https://github.com/coreappcode/bz-india` at commit `b258437` (2026-10-28). This was a source-code comparison; neither app was run and the production backend was not queried.

Both apps reference the same Supabase project. The primary mismatches are in Android data flows and UI behavior rather than the project URL.

## Findings

### Blocker: Android authentication is simulated

**Status: FIXED in code (not compiled or run — no Android SDK in this environment).** Android now calls Supabase GoTrue/PostgREST (`SupabaseAuth.kt`) with the web's flows and messages: password login with role/ban checks, signup (`identities` empty → existing-account error, real `countries` UUID), signup OTP verify, resend, recovery OTP + new-password screen, logout, and encrypted session restore (`SessionStore.kt`, excluded from backup). Validation rules now match the web.

**Android evidence:** `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` initializes the app as logged in with sample user details. Its `login`, `signUp`, `verifyOtp`, `resendOtp`, and `resetPassword` methods report success without calling Supabase auth.

**Web reference:** `src/contexts/AuthContext.tsx` uses Supabase auth methods for signup, sign-in, OTP verification, and password reset.

**Impact:** Android can show a false signed-in state; credentials and OTPs are not validated, and Android sessions do not correspond to real web/backend accounts.

### Blocker: Cart, wishlist, addresses, orders, and notifications are sample or in-memory data

**Status: FIXED in code (not compiled or run — no Android SDK in this environment).** Sample seed data is removed; everything loads from Supabase with the user's access token (`SupabaseRest.kt`, `UserDataApi.kt`), mirroring the web:
- **Cart** (`cart_items`): load with `products(*)` join + variant stock resolution, upsert on `(user_id,product_id,selected_size,selected_color,selected_variant_sku)`, delete/clear; backend cart replaces the local cart on login; web `cartItemId` keys.
- **Wishlist** (`wishlists`): add/remove, merge local + backend on login, product join (shown even outside the loaded catalog page).
- **Addresses** (`user_addresses`): load/create/update/delete/set-default with the web's field set (`country`, `delivery_notes`, `street_address_1/2`, `postal_code`) and web validation; fake "Karnataka" default and non-existent `locality` removed.
- **Orders** (`orders` + `order_items` + `shiprocket_shipments`): real list, 12 web statuses and filter groups, `ORD-<12 digits>` number, dd/MM/yyyy date, additional charges; fake courier/tracking/ETD/payment defaults removed.
- **Notifications** (`notifications`): 50-per-page fetch, seller-only types hidden, mark read / mark all read, tap opens `metadata.order_id`.
- Cleared on logout; `CartScreen` no longer re-syncs on every cart change.

**Known gaps:** no realtime subscriptions (refresh on login/screen open); `placeOrder()` is still local-only (next blocker — it no longer deletes backend cart rows); the web has no notification-delete RLS so none was added; guest cart/wishlist are in-memory only.

**Android evidence:** `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` seeds these values with examples. `syncCartWithBackend()` is empty, while cart, wishlist, address, and order operations only update local state.

**Web reference:** `src/contexts/CartContext.tsx` persists and synchronizes cart rows with Supabase; `src/contexts/WishlistContext.tsx` persists wishlist rows in Supabase.

**Impact:** Android account changes are not shared across sessions or devices. Displayed addresses, notifications, and orders are not the user's persistent backend records.

### Blocker: Android checkout does not persist an order or process payment

**Status: FIXED in code (not compiled or run — no Android SDK here; Razorpay must be tested on a real device).** Ported from the web: live quote (`CheckoutApi.kt`: variant verification, `get_public_product_prices`, offers, Shiprocket rate card / `shiprocket-rate`, 15 margin, minimum order), real shipping replaces the fake ₹40/₹499, express/COD options and ₹150 discount removed. OrderSummary does the weight check, `shiprocket-ops` serviceability, TAT estimate, and the stock re-check with the web's messages. Checkout creates the Razorpay order through `create-razorpay-order`, saves `checkout_payment_snapshots`, opens the Razorpay Android SDK (`com.razorpay:checkout:1.6.41`), verifies with `verify-razorpay-payment` (retries), polls `orders` by `payment_intent_id`, clears the cart, and shows the real order (or a temporary processing order, as on the web). Known gaps: international-origin products and non-INR pricing (web Shippo/currency path) are rejected with a message; Stripe is not implemented (the web India flow uses Razorpay).

**Android evidence:** `app/src/main/java/com/bzead/india/ui/screens/CheckoutScreen.kt` calls `BzeadRepository.placeOrder()`. That method in `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` generates an in-memory order and clears the local cart.

**Web reference:** `src/pages/user/Checkout.tsx` creates orders through the secure `create_order_secure` RPC and has Stripe/Razorpay payment flows.

**Impact:** Android can display order success even though no backend order was created and no payment was completed.

### High: Android catalog data can diverge from backend data

**Status: FIXED in code (not compiled or run).** `SupabaseClient.kt` was rebuilt to the web queries: hero/ad banners by `banner_type`, paged 3-level categories (home shortcuts = first 16 level-1), sponsored sections with the web fallback, display prices and MRP from `get_public_product_prices` (strike-through only when `markup_mrp > price`), real `rating`/`review_count`, `search_products_catalog` search, category pages by descendant ids, and product detail with `product_variants`. Sample/fabricated data (1.5x MRP, 4.5 rating, trending/deal flags, sample fallbacks) was removed from the catalog path. Not done: the web's full variant-selection pricing UI and the product-page delivery estimator on Android; reviews are a separate item below.

**Android evidence:** `app/src/main/java/com/bzead/india/data/SupabaseClient.kt` fetches at most 100 products and maps only a subset of product fields. It synthesizes fallback MRP, ratings/review counts, and trending flags.

**Web reference:** `src/lib/productService.ts` queries products and product variants; `src/types/index.ts` defines a richer product shape.

**Impact:** Android product screens can show inaccurate prices, ratings, stock/variant choices, or deal labels.

### High: Review submission and review content are not connected

**Android evidence:** `submitReview()` in `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` returns success without saving a review. `app/src/main/java/com/bzead/india/ui/screens/ProductDetailScreen.kt` displays sample reviews.

**Web reference:** `src/lib/productService.ts` reads and writes the `reviews` table.

**Impact:** Android review submissions are lost, and product detail screens may show placeholder reviews instead of current backend reviews.

### Medium: Country selection is static on Android

**Android evidence:** `fetchCountries()` in `app/src/main/java/com/bzead/india/data/SupabaseClient.kt` returns a hard-coded list of five countries.

**Web reference:** `src/lib/productService.ts` loads countries from Supabase; `src/contexts/CurrencyContext.tsx` handles currency selection and display.

**Impact:** Country options and currency behavior can differ between Android and web.

### Medium: Some Android UI navigation discards the selected destination

**Category navigation:** `app/src/main/java/com/example/MainActivity.kt` ignores the category ID and always opens the general category screen. The web app links a selected category to `/category/{id}` in `src/components/layout/Categories.tsx`.

**Banner navigation:** `app/src/main/java/com/bzead/india/ui/screens/HomeScreen.kt` sends banner taps to Search rather than following the banner link.

**Impact:** Tapping a category or promotional banner may not take the user to the content represented by that selection.

## Additional behavior to verify

The Android app does fetch banners, categories, and products from the matching Supabase project in `app/src/main/java/com/bzead/india/data/SupabaseClient.kt`. However, `app/src/main/java/com/bzead/india/ui/screens/HomeScreen.kt` substitutes sample content when live results are empty. This can make a backend failure or empty response appear to be a valid catalog response.
