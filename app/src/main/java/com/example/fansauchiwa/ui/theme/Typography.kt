package com.example.fansauchiwa.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = ZenMaruGothicFont, fontProvider = provider, weight = FontWeight.W300),
)