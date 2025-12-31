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

val HachiMaruPopFont = GoogleFont(name = "Hachi Maru Pop")
val hachiMaruPopFontFamily = FontFamily(
    Font(googleFont = HachiMaruPopFont, fontProvider = provider),
)

val MPlusRounded1cFont = GoogleFont(name = "M PLUS Rounded 1c")
val mPlusRounded1cFontFamily = FontFamily(
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = MPlusRounded1cFont, fontProvider = provider, weight = FontWeight.W100),
)

val MPlus1pFont = GoogleFont(name = "M PLUS 1p")
val mPlus1pFontFamily = FontFamily(
    Font(googleFont = MPlus1pFont, fontProvider = provider),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = MPlus1pFont, fontProvider = provider, weight = FontWeight.W100),
)

val ZenKakuGothicNewFont = GoogleFont(name = "Zen Kaku Gothic New")
val zenKakuGothicNewFontFamily = FontFamily(
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider),
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = ZenKakuGothicNewFont, fontProvider = provider, weight = FontWeight.W300),
)

val DelaGothicOneFont = GoogleFont(name = "Dela Gothic One")
val delaGothicOneFontFamily = FontFamily(
    Font(googleFont = DelaGothicOneFont, fontProvider = provider),
)

val DotGothic16Font = GoogleFont(name = "DotGothic16")
val dotGothic16FontFamily = FontFamily(
    Font(googleFont = DotGothic16Font, fontProvider = provider),
)

val RampartOneFont = GoogleFont(name = "Rampart One")
val rampartOneFontFamily = FontFamily(
    Font(googleFont = RampartOneFont, fontProvider = provider),
)

val RocknRollOneFont = GoogleFont(name = "RocknRoll One")
val rocknRollOneFontFamily = FontFamily(
    Font(googleFont = RocknRollOneFont, fontProvider = provider),
)

val YuseiMagicFont = GoogleFont(name = "Yusei Magic")
val yuseiMagicFontFamily = FontFamily(
    Font(googleFont = YuseiMagicFont, fontProvider = provider),
)

val KiwiMaruFont = GoogleFont(name = "Kiwi Maru")
val kiwiMaruFontFamily = FontFamily(
    Font(googleFont = KiwiMaruFont, fontProvider = provider),
    Font(googleFont = KiwiMaruFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = KiwiMaruFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = KiwiMaruFont, fontProvider = provider, weight = FontWeight.W300),
)

val KleeOneFont = GoogleFont(name = "Klee One")
val kleeOneFontFamily = FontFamily(
    Font(googleFont = KleeOneFont, fontProvider = provider),
    Font(googleFont = KleeOneFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = KleeOneFont, fontProvider = provider, weight = FontWeight.W400),
)

val KosugiFont = GoogleFont(name = "Kosugi")
val kosugiFontFamily = FontFamily(
    Font(googleFont = KosugiFont, fontProvider = provider),
)

val KosugiMaruFont = GoogleFont(name = "Kosugi Maru")
val kosugiMaruFontFamily = FontFamily(
    Font(googleFont = KosugiMaruFont, fontProvider = provider),
)

val MochiyPopOneFont = GoogleFont(name = "Mochiy Pop One")
val mochiyPopOneFontFamily = FontFamily(
    Font(googleFont = MochiyPopOneFont, fontProvider = provider),
)

val MochiyPopPOneFont = GoogleFont(name = "Mochiy Pop P One")
val mochiyPopPOneFontFamily = FontFamily(
    Font(googleFont = MochiyPopPOneFont, fontProvider = provider),
)

val MPlus2Font = GoogleFont(name = "M PLUS 2")
val mPlus2FontFamily = FontFamily(
    Font(googleFont = MPlus2Font, fontProvider = provider),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W200),
    Font(googleFont = MPlus2Font, fontProvider = provider, weight = FontWeight.W100),
)

val MPlus1Font = GoogleFont(name = "M PLUS 1")
val mPlus1FontFamily = FontFamily(
    Font(googleFont = MPlus1Font, fontProvider = provider),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W200),
    Font(googleFont = MPlus1Font, fontProvider = provider, weight = FontWeight.W100),
)

val MPlus1CodeFont = GoogleFont(name = "M PLUS 1 Code")
val mPlus1CodeFontFamily = FontFamily(
    Font(googleFont = MPlus1CodeFont, fontProvider = provider),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W200),
    Font(googleFont = MPlus1CodeFont, fontProvider = provider, weight = FontWeight.W100),
)

val NotoSansJPFont = GoogleFont(name = "Noto Sans JP")
val notoSansJPFontFamily = FontFamily(
    Font(googleFont = NotoSansJPFont, fontProvider = provider),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W200),
    Font(googleFont = NotoSansJPFont, fontProvider = provider, weight = FontWeight.W100),
)

val NotoSerifJPFont = GoogleFont(name = "Noto Serif JP")
val notoSerifJPFontFamily = FontFamily(
    Font(googleFont = NotoSerifJPFont, fontProvider = provider),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W300),
    Font(googleFont = NotoSerifJPFont, fontProvider = provider, weight = FontWeight.W200),
)

val PottaOneFont = GoogleFont(name = "Potta One")
val pottaOneFontFamily = FontFamily(
    Font(googleFont = PottaOneFont, fontProvider = provider),
)

val ReggaeOneFont = GoogleFont(name = "Reggae One")
val reggaeOneFontFamily = FontFamily(
    Font(googleFont = ReggaeOneFont, fontProvider = provider),
)

val SawarabiGothicFont = GoogleFont(name = "Sawarabi Gothic")
val sawarabiGothicFontFamily = FontFamily(
    Font(googleFont = SawarabiGothicFont, fontProvider = provider),
)

val SawarabiMinchoFont = GoogleFont(name = "Sawarabi Mincho")
val sawarabiMinchoFontFamily = FontFamily(
    Font(googleFont = SawarabiMinchoFont, fontProvider = provider),
)

val ShipporiAntiqueBFont = GoogleFont(name = "Shippori Antique B1")
val shipporiAntiqueBFontFamily = FontFamily(
    Font(googleFont = ShipporiAntiqueBFont, fontProvider = provider),
)

val ShipporiAntiqueFont = GoogleFont(name = "Shippori Antique")
val shipporiAntiqueFontFamily = FontFamily(
    Font(googleFont = ShipporiAntiqueFont, fontProvider = provider),
)

val ShipporiMinchoFont = GoogleFont(name = "Shippori Mincho")
val shipporiMinchoFontFamily = FontFamily(
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ShipporiMinchoFont, fontProvider = provider, weight = FontWeight.W400),
)

val ShipporiMinchoB1Font = GoogleFont(name = "Shippori Mincho B1")
val shipporiMinchoB1FontFamily = FontFamily(
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider),
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider, weight = FontWeight.W800),
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ShipporiMinchoB1Font, fontProvider = provider, weight = FontWeight.W400),
)

val StickyFont = GoogleFont(name = "Stick")
val stickyFontFamily = FontFamily(
    Font(googleFont = StickyFont, fontProvider = provider),
)

val TrainOneFont = GoogleFont(name = "Train One")
val trainOneFontFamily = FontFamily(
    Font(googleFont = TrainOneFont, fontProvider = provider),
)

val YomogiFont = GoogleFont(name = "Yomogi")
val yomogiFontFamily = FontFamily(
    Font(googleFont = YomogiFont, fontProvider = provider),
)

val ZenAntiqueFont = GoogleFont(name = "Zen Antique")
val zenAntiqueFontFamily = FontFamily(
    Font(googleFont = ZenAntiqueFont, fontProvider = provider),
)

val ZenAntiqueSoftFont = GoogleFont(name = "Zen Antique Soft")
val zenAntiqueSoftFontFamily = FontFamily(
    Font(googleFont = ZenAntiqueSoftFont, fontProvider = provider),
)

val ZenKakuGothicAntiqueFont = GoogleFont(name = "Zen Kaku Gothic Antique")
val zenKakuGothicAntiqueFontFamily = FontFamily(
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider),
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider, weight = FontWeight.W400),
    Font(googleFont = ZenKakuGothicAntiqueFont, fontProvider = provider, weight = FontWeight.W300),
)

val ZenKurenaidoFont = GoogleFont(name = "Zen Kurenaido")
val zenKurenaidoFontFamily = FontFamily(
    Font(googleFont = ZenKurenaidoFont, fontProvider = provider),
)

val ZenOldMinchoFont = GoogleFont(name = "Zen Old Mincho")
val zenOldMinchoFontFamily = FontFamily(
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider),
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider, weight = FontWeight.W900),
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider, weight = FontWeight.W700),
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider, weight = FontWeight.W600),
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider, weight = FontWeight.W500),
    Font(googleFont = ZenOldMinchoFont, fontProvider = provider, weight = FontWeight.W400),
)

