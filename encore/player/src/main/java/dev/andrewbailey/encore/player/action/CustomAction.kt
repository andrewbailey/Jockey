package dev.andrewbailey.encore.player.action

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class CustomAction(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val exposed: Boolean
)
