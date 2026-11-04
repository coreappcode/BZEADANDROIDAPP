package com.bzead.india.util

import com.bzead.india.model.DeliveryAddress

/** Address validation copied from the web: AddressForm (account page) and ShippingAddress (checkout). */
object AddressValidation {
    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val PHONE = Regex("^\\+?[\\d\\s\\-()]{7,}$")

    fun validate(a: DeliveryAddress, checkout: Boolean): String? {
        if (!a.isIndia) return "BZEAD.IN shipping addresses must be in India."
        if (a.fullName.isBlank()) return "Full name is required"
        if (a.phoneNumber.isBlank()) return "Phone number is required"
        if (checkout) {
            if (a.phoneNumber.count { it.isDigit() } < 6) return "Phone number must have at least 6 digits"
        } else if (!PHONE.matches(a.phoneNumber.trim())) {
            return "Please enter a valid phone number"
        }
        if (checkout) {
            if (a.email.isBlank() || !EMAIL.matches(a.email.trim())) return "Valid email is required"
        } else {
            if (a.email.isBlank()) return "Email is required"
            if (!EMAIL.matches(a.email)) return "Please enter a valid email address"
        }
        if (a.streetAddress.isBlank()) return "Street address is required"
        if (a.city.isBlank()) return "City is required"
        if (a.state.isBlank()) return "State/Province is required"
        if (a.pinCode.isBlank()) return "Postal code is required"
        if (checkout && !Regex("^\\d{6}$").matches(a.pinCode.trim())) return "Indian postal code must be exactly 6 digits"
        return null
    }
}
