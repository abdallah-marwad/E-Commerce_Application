package com.abdallah.ecommerce.utils

import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

class CustomShimmerDrawable {
    private val shimmer = Shimmer.AlphaHighlightBuilder()
        .setDuration(1100)
        .setBaseAlpha(0.8f)
        .setHighlightAlpha(0.6f)
        .setAutoStart(true)
        .build()

    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }

}