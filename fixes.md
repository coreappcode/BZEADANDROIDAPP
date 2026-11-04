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

**Known gaps:** no realtime subscriptions (refresh on login/screen open); the web has no notification-delete RLS so none was added; guest cart/wishlist are in-memory only.

**Android evidence:** `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` seeds these values with examples. `syncCartWithBackend()` is empty, while cart, wishlist, address, and order operations only update local state.

**Web reference:** `src/contexts/CartContext.tsx` persists and synchronizes cart rows with Supabase; `src/contexts/WishlistContext.tsx` persists wishlist rows in Supabase.

**Impact:** Android account changes are not shared across sessions or devices. Displayed addresses, notifications, and orders are not the user's persistent backend records.

### Blocker: Android checkout does not persist an order or process payment

**Status: FIXED in code (not compiled or run — no Android SDK here; Razorpay must be tested on a real device).** Ported from the web: live quote (`CheckoutApi.kt`: variant verification, `get_public_product_prices`, offers, Shiprocket rate card / `shiprocket-rate`, 15 margin, minimum order), real shipping replaces the fake ₹40/₹499, express/COD options and ₹150 discount removed. OrderSummary does the weight check, `shiprocket-ops` serviceability, TAT estimate, and the stock re-check with the web's messages. Checkout creates the Razorpay order through `create-razorpay-order`, saves `checkout_payment_snapshots`, opens the Razorpay Android SDK (`com.razorpay:checkout:1.6.41`), verifies with `verify-razorpay-payment` (retries), polls `orders` by `payment_intent_id`, clears the cart, and shows the real order (or a temporary processing order, as on the web). Scope: the app is India-only (INR, Razorpay), so international/non-INR pricing and Stripe are intentionally out of scope; international-origin products are rejected at checkout with a message.

**Android evidence:** `app/src/main/java/com/bzead/india/ui/screens/CheckoutScreen.kt` calls `BzeadRepository.placeOrder()`. That method in `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` generates an in-memory order and clears the local cart.

**Web reference:** `src/pages/user/Checkout.tsx` creates orders through the secure `create_order_secure` RPC and has Stripe/Razorpay payment flows.

**Impact:** Android can display order success even though no backend order was created and no payment was completed.

### High: Android catalog data can diverge from backend data

**Status: FIXED in code (not compiled or run).** `SupabaseClient.kt` was rebuilt to the web queries: hero/ad banners by `banner_type`, paged 3-level categories (home shortcuts = first 16 level-1), sponsored sections with the web fallback, display prices and MRP from `get_public_product_prices` (strike-through only when `markup_mrp > price`), real `rating`/`review_count`, `search_products_catalog` search, category pages by descendant ids, and product detail with `product_variants`. Sample/fabricated data (1.5x MRP, 4.5 rating, trending/deal flags, sample fallbacks) was removed from the catalog path. The product-page variant selector and delivery estimator are covered in their own section below; reviews are a separate item.

**Android evidence:** `app/src/main/java/com/bzead/india/data/SupabaseClient.kt` fetches at most 100 products and maps only a subset of product fields. It synthesizes fallback MRP, ratings/review counts, and trending flags.

**Web reference:** `src/lib/productService.ts` queries products and product variants; `src/types/index.ts` defines a richer product shape.

**Impact:** Android product screens can show inaccurate prices, ratings, stock/variant choices, or deal labels.

### High: Review submission and review content are not connected

**Status: FIXED in code (not compiled or run).** New `ReviewApi.kt` ports the web: `fetchProductReviews` (unflagged reviews, newest first, names from `profiles`), the duplicate-review check ("You have already reviewed this product."), review image upload to the `product-images` bucket (`reviews/<productId>-<ts>-<i>-<name>`, max 5 images, 5 MB, JPEG/PNG/WebP/GIF), `createReview` insert (rating, heading, comment, images, benefits) and the `products.rating`/`review_count` recalculation. `WriteReviewScreen` now matches the web form (rating starts empty, labels Poor–Excellent, 100/5000 character limits, photos, benefits checkboxes, terms unchecked, error banner, Cancel). The product page shows the web's review summary (average, per-star %, first 4 reviews, "Verified Purchase" fallback heading, first image). Sample reviews were deleted.

**Android evidence:** `submitReview()` in `app/src/main/java/com/bzead/india/data/BzeadRepository.kt` returns success without saving a review. `app/src/main/java/com/bzead/india/ui/screens/ProductDetailScreen.kt` displays sample reviews.

**Web reference:** `src/lib/productService.ts` reads and writes the `reviews` table.

**Impact:** Android review submissions are lost, and product detail screens may show placeholder reviews instead of current backend reviews.

### Medium: Country selection is static on Android

**Status: FIXED (already live).** `fetchCountries()` reads the `countries` table with the web Signup query (`is_active`, ordered by name) and, as on the India storefront, Signup shows India only. The hard-coded list no longer exists. Not ported: the web address form's country/state dropdowns (`states` table); Android addresses stay India-only: the address form and the checkout shipping-address form show Country locked to "India" and always save it.

**Android evidence:** `fetchCountries()` in `app/src/main/java/com/bzead/india/data/SupabaseClient.kt` returns a hard-coded list of five countries.

**Web reference:** `src/lib/productService.ts` loads countries from Supabase; `src/contexts/CurrencyContext.tsx` handles currency selection and display.

**Impact:** Country options and currency behavior can differ between Android and web.

### Medium: Some Android UI navigation discards the selected destination

**Status: FIXED in code (not compiled or run).** Category taps open the category screen for the tapped category id (`AppScreen.CategoryDetail`), like `/category/:id`. Home "See more" opens the Featured / Trending / Hot Deals section screens (`/products/section/:section`). Hero and ad banners follow their `link`: in-app web routes (`/products/section/*`, `/products/featured|hot-deals`, `/category/:id`, `/products/<id|public id|slug>`, `/cart`, `/wishlist`, `/orders`, `/notifications`, `/profile`, `/contact`) open the matching screen; absolute `http(s)` links open in the browser; banners without a link do nothing. Product lookup now accepts id, `BZD#########` public id or slug and filters `approval_status=approved`, `is_active=true`, like `fetchProductById`. Note: the web "See more reviews" button does nothing, and Android matches that.

**Category navigation:** `app/src/main/java/com/example/MainActivity.kt` ignores the category ID and always opens the general category screen. The web app links a selected category to `/category/{id}` in `src/components/layout/Categories.tsx`.

**Banner navigation:** `app/src/main/java/com/bzead/india/ui/screens/HomeScreen.kt` sends banner taps to Search rather than following the banner link.

**Impact:** Tapping a category or promotional banner may not take the user to the content represented by that selection.

### Product page: fake pincode estimator and variant selector were not at web parity

**Status: FIXED in code (not compiled or run).** The fake "560103" estimator is removed. `DeliveryEstimateBox.kt` + `DeliveryEstimateApi.kt` port the web `DeliveryEstimate`/`useDeliveryEstimate`: the default (else first) saved address is auto-checked for signed-in users via Shiprocket serviceability + seller-pickup TAT, shows "Delivery by D MMM" / "Delivery not available to this location" / "Delivery details available at checkout", and has the address picker (search, saved addresses, Add New); "Use my current location" is intentionally not included (India-only app); the address form shows Country locked to India. `VariantSelection.kt` ports `productVariantSelection.ts`: colour circles (`resolveColorHex`), size buttons, in-stock defaults, `currentVariant`, variant-aware stock labels (Out Of Stock / Limited Stock / In Stock), variant images leading the gallery, price from `get_public_product_prices[_with_overrides]` with MRP and "(N% OFF)", Add To Cart / Buy Now disabled rules, login redirect for guests, and the cart item carrying size, colour, variant id, SKU and the public unit price.

**Not ported:** the offers section (`offer_rules`) and the size-guide modal.

## Re-audit (latest pass against `bz-india`)

### FIXED: policy and contact pages

`PolicyScreen.kt` now renders text generated from the web pages (`data/PolicyContent.kt`, India branch): Shipping, Cancellation & Return, Privacy, Terms & Conditions, Terms of Use and About, all reachable from the Profile menu. The invented fees, timelines and Bengaluru contact details were removed. `ContactScreen.kt` matches the web `Contact.tsx` (support@bzead.com, admin@bzead.com, +44 7555394997, Kannur address).

### FIXED: search history

`recentSearches` is now the web's behaviour: `user_search_history` (typed_input, newest first, de-duplicated, 5 shown) for signed-in users, a local list for guests; saved on search/product click, deletable per entry.

### FIXED: smaller gaps

- Offers section: `offer_rules` is fetched on the product detail query; the first active offer (is_active, start/end time) shows in an "Offer Section" box with the web's summary text.
- Billing fields: the web `Checkout.tsx` has no billing validation (no required attributes), so Android already matches; nothing to change.
- `SampleCatalogData.kt` deleted.

### Still open

- **Size-guide modal:** not ported. The web builds it from the 1,500-line `variantThemeConfig.ts` (per category/sub-category/product-type size charts), which Android does not have yet.
- **Realtime subscriptions:** not implemented (data refreshes on login or screen open). Guest cart and wishlist are in-memory only.
- International shipping/non-INR pricing and Stripe are intentionally out of scope (India-only app).

### Verification status

Nothing in this file has been compiled or run (no Android SDK in the environment). Razorpay checkout and the delivery estimate need a real-device test.

## Homepage audit (products, UI, backend) — Android vs `bz-india`

Method: read `pages/BzeadHomePage.tsx`, `components/layout/{HeroCarousel,Categories,MegaMenu,Header,StorefrontHeader,WelcomeBackBar,VideoAdsBanner,MobileNav,Footer}.tsx`, `components/products/ProductCard.tsx`, `hooks/useProductAddToCartFlow.ts`, `lib/{productService,sponsoredProductsService,pricingService,homePageCache}.ts` and compared with `HomeScreen.kt`, `BzeadTopHeader.kt`, `WebHeroCarousel.kt`, `AdBannerBetweenSections.kt`, `SectionHeaderWithSponsored.kt`, `WebProductCard.kt`, `CategoriesGrid3Col.kt`, `BzeadRepository.kt`, `SupabaseClient.kt`. Nothing was run; every item below comes from reading the code. **Status of all items: OPEN.**

### Backend / data: already matching (no change needed)
- Sponsored query (`sponsored_products` + `products!inner(*)`, active, `start_at <= now < end_at`, approved + active products, newest first, limit 200, max 100 per section), fallback to featured / hot-deals / trending only when there is no sponsored data in any section, 100 per section.
- Fallback queries and fields (`HOME_PRODUCT_FIELDS`), the hot-deals filter/sort (including the web's own discount formula), and the `get_public_product_prices` RPC with the strike-through MRP only when `markup_mrp > price`.
- Ad and hero banner queries (`banners`, `is_active`, `banner_type`, `position`), and the 16 first level-1 categories.

### Backend / data: mismatches
1. **`has_variants` not loaded.** The web attaches `has_variants` to every home product (`attachHasVariants`, a `product_variants` query). Android never queries it.
2. **`item_condition` is fetched but dropped.** `parseProduct` does not map `item_condition`, so the condition badges cannot show.
3. **No periodic refresh or cache.** The web refreshes sections and banners every 5 min (`HOME_CONTENT_CACHE_TTL_MS`) and caches prices for 2 min. Android refetches only when the Home screen is composed.
4. **Price of 0 / missing.** The web uses any finite RPC price; Android ignores an RPC `selling_price <= 0` and keeps the raw product price.
5. **Category images are not fetched.** The web shows `/images/*.png` category artwork from a name→file map (`Categories.tsx` `imageMap`; the files are in the web repo's `public/images`). Android's `fetchCategoryRows` does not select `image_url`, `CategoryItem.iconUrl` is never set, and those PNGs are not in the Android project (`res/drawable` has only the logo).
6. **No video ads.** The web reads `banners` with `banner_type = 'video'` and a `video_url`. Android never queries them.

### UI: sections the web home page has and Android does not
7. **Category shortcuts missing on the home screen.** The web shows the first 16 level-1 categories as circles (3/4/6/8 columns by width, name under each, `Package` icon fallback). `HomeScreen.kt` imports `CategoriesGrid3Col` and collects `liveCategories` but never renders them.
8. **Mega menu / "All" categories panel.** The web has a categories bar built from the live tree (3 levels). Android has a hard-coded pill row: "All, Mobiles, Fashion, Beauty, Electronics, Home, Appliances, Grocery, Footwear" in `BzeadTopHeader.kt`. These names are not read from the backend, and tapping one calls `onNavigateToCategory(name)`, which `MainActivity` treats as a category **id** (`AppScreen.CategoryDetail(id)` → `loadCategoryProducts(id)`), so a label is sent where an id is expected.
9. **Ad banner slots.** Web order: Featured → ad slot 1 → Hot Deals → ad slot 2 → Trending → ad slot 3; each slot is a carousel (7 s rotation, dots). Android order: Featured → one ad (slot 2, else the first banner) → Trending → Hot Deals; slots 1 and 3 are never shown and there is no rotation.
10. **Section order.** Web: Featured, Hot Deals, Trending. Android: Featured, Trending, Hot Deals.
11. **"Start Selling on BZEAD" block.** Shown on the web for guests and `role = user` ("Join thousands of successful sellers…", "Get Started Now" → `https://www.bzead.com/seller`). Not on Android.
12. **Video ads banner** (above the footer, mute/dismiss, 30 s rotation, YouTube or direct video) is missing; see 6.
13. **Footer** (Legal, Business: Become a Seller / About / Contact / Play Store, Contact Us: +44 7555394997 and support@bzead.com, social links) is missing on the Android home screen.
14. **Welcome bar:** the web hides it on native (`isNativePlatform`), so Android having none is correct.
15. **Empty and loading states.** The web shows 4 skeleton cards while loading and "No products yet" for an empty section. Android shows nothing in both cases.
16. **"Sponsored" tag.** The web shows a red "Sponsored" tag only when sponsored data exists (`hasSponsored`). Android always shows a yellow "SPONSORED" tag on all three sections, even when the products came from the fallback query.
17. **See More links** exist on both (`featured`, `hot-deals`, `trending`); the web link text is "See More" with an arrow.

### UI: hero, banners, header
18. **Hero carousel:** web rotates every 5 s, Android every 3.5 s. The web drops a banner whose image fails to load; Android keeps it. The web shows a skeleton until loaded and renders nothing if there are no banners. The web places it below the category bar on a white page with a 180 dp (phone) rounded card; Android puts it on the navy header at 190 dp.
19. **Ad banner overlay.** Android adds an "ADVERTISEMENT" tag over every ad banner; the web has none. The web ad image is `maxHeight 150` with `object-cover`; Android uses a fixed 110 dp card.
20. **Logo.** The web uses `/images/logo/logo.png`; Android loads a Cloudinary URL in `BzeadTopHeader.kt` while the project has a different `drawable/bzead_logo.png` (the two files differ).
21. **Search field text.** The web placeholder is "Search for products, brands..."; Android shows "Search products, brands...".
22. **Header actions.** Web: wishlist and cart icons with badges (cart badge only when logged in; guests are sent to `/login` when they tap either), plus a menu button. Android: wishlist icon in the header, cart as a floating button with a badge that counts the local cart, and no menu button. Guests are not sent to login.
23. **Bottom navigation.** Web native nav: Home, Orders, Notifications (unread badge, realtime), Profile/Login. Android: Home, Category, Cart, Account.

### UI: product card (`ProductCard.tsx` vs `WebProductCard.kt`)
24. **Discount badge:** web = red "`N% OFF`" (top left); Android = green "`-N%`".
25. **Price layout and colour:** web = struck MRP on the left (small), selling price on the right in red `#FF0000`, formatted by `formatCurrency` (en-IN, 2 decimals, e.g. ₹1,234.00). Android = dark price on the left with the MRP on the right, and `"₹${price.toInt()}"` (truncated, no decimals or grouping).
26. **Rating and brand row:** web = star + rating, brand in uppercase blue `#004aad`. Android = a yellow "rating (count)" pill, and the brand is not shown.
27. **Category/brand sub-line** under the title (shown on larger screens) is not on Android (web hides it on small screens, so this is for completeness).
28. **Condition badge** (Used – Open Box / Like New / Very Good / Good / Acceptable, Refurbished; nothing for `brand_new`): missing (see 2).
29. **Quick View** eye button and modal: missing.
30. **Image:** web `object-cover` on a square (`aspect-square`), with `resolveProductDisplayImage` (falls back to `images[0]`, builds a Supabase `product-images` URL for storage paths, ignores blob URLs). Android uses `ContentScale.Fit` in a 140 dp box with `imageUrl` only; `primary` falls back to `images[0]` but storage paths are not resolved.
31. **Add to Cart behaviour.**
    - Web: guests go to `/login`. Logged-in users: if the product has variants, either a variant dialog opens or the single/default variant is chosen (`useProductAddToCartFlow`); products without variants are added directly. A toast "Product added to cart" appears.
    - Android: `BzeadRepository.addToCart(product)` adds the product immediately with no variant, for guests too (in memory only), with no toast.
32. **Already in cart:** web shows a disabled green "Already In Cart" button (logged-in users only). Android shows a dark "In Cart" button that is still tappable and adds another unit.
33. **Button state while the price is loading:** the web disables the button and shows "Loading price…" until a price is available. Android has no equivalent.
34. **Wishlist heart:** works on both, but for guests the web keeps the item in local storage and syncs for logged-in users; Android keeps guest wishlist in memory only (lost on restart).

### Not verified (needs the running app or a device)
- Whether the category pills open a valid category screen (item 8).
- Visual spacing, fonts and colours beyond what the code states.

## Additional behavior to verify

**Status: FIXED.** The HomeScreen sample-content fallbacks were removed in the catalog fix; empty or failed responses now show empty sections.

The Android app does fetch banners, categories, and products from the matching Supabase project in `app/src/main/java/com/bzead/india/data/SupabaseClient.kt`. However, `app/src/main/java/com/bzead/india/ui/screens/HomeScreen.kt` substitutes sample content when live results are empty. This can make a backend failure or empty response appear to be a valid catalog response.
