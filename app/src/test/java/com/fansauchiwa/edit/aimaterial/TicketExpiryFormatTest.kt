package com.fansauchiwa.edit.aimaterial

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TicketExpiryFormatTest {

    private val tokyo = ZoneId.of("Asia/Tokyo")

    @Test
    fun formatTicketExpiry_Tokyo_ShowsDateAndTime() {
        val expiresAt = Instant.parse("2027-05-12T06:00:00Z")

        assertEquals("2027/05/12 15:00", formatTicketExpiry(expiresAt, tokyo))
    }

    @Test
    fun formatTicketExpiry_WithSeconds_TruncatesToMinute() {
        // 15:00:59 に切れる券を「15:01まで」と出すと、使えると思った1分に使えない
        val expiresAt = Instant.parse("2027-05-12T06:00:59.999Z")

        assertEquals("2027/05/12 15:00", formatTicketExpiry(expiresAt, tokyo))
    }

    @Test
    fun formatTicketExpiry_CrossesMidnightInZone_ShowsNextDay() {
        val expiresAt = Instant.parse("2027-05-11T15:30:00Z")

        assertEquals("2027/05/12 00:30", formatTicketExpiry(expiresAt, tokyo))
    }

    @Test
    fun formatTicketExpiry_OtherZone_ShowsTimeInThatZone() {
        val expiresAt = Instant.parse("2027-05-12T06:00:00Z")

        assertEquals("2027/05/12 06:00", formatTicketExpiry(expiresAt, ZoneId.of("UTC")))
    }

    @Test
    fun formatTicketExpiry_Midnight_ShowsZeroHour() {
        // 24時制で出す（「12:00」と書くと昼と取り違える）
        val expiresAt = Instant.parse("2027-05-11T15:00:00Z")

        assertEquals("2027/05/12 00:00", formatTicketExpiry(expiresAt, tokyo))
    }
}
