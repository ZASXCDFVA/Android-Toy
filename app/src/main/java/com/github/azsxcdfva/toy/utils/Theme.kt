package com.github.azsxcdfva.toy.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

fun Context.resolveThemedColor(@AttrRes resId: Int): Int {
    return TypedValue().apply {
        theme.resolveAttribute(resId, this, true)
    }.data
}
