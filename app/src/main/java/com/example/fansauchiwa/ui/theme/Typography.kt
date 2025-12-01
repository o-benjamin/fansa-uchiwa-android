package com.example.fansauchiwa.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.fansauchiwa.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val ZenMaruGothicFont = GoogleFont(name = "Zen Maru Gothic")

val zenMaruGothicFontFamily = FontFamily(
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider),
)