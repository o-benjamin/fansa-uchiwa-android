package com.fansauchiwa.data

import java.time.LocalDate
import javax.inject.Inject

/**
 * 端末の今日の日付。テストで日付を差し替えるために分けている
 */
interface TodayProvider {
    fun today(): LocalDate
}

class TodayProviderImpl @Inject constructor() : TodayProvider {
    override fun today(): LocalDate {
        return LocalDate.now()
    }
}
