package com.example.busmate.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GuidelineModel(
    val content: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable
