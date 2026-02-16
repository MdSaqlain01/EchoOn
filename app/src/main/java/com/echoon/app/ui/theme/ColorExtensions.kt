package com.echoon.app.ui.theme

import androidx.compose.ui.graphics.Color

fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
