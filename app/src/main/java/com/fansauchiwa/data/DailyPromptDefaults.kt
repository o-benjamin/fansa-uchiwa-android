package com.fansauchiwa.data

/**
 * 今日のお題の同梱の控え（#359）。Remote Config に今日の分がない・取得できないときに、日付から順番に選ぶ（[DailyPromptSelector]）
 *
 * 日付に結びつけないので、季節もののお題は入れない（季節ものは Remote Config で配る）。
 * 文言の決まり：実在の人名・グループ名・曲名・歌詞を入れない。下品・過激な文言を入れない。定番のファンサを中心にする。
 * `id` は計測の `prompt_id` に出るので、文言を直しても同じ動作なら変えない。並べ替えると、その日に出るお題が変わる。
 */
object DailyPromptDefaults {

    val PROMPTS: List<DailyPrompt> = listOf(
        DailyPrompt(id = "default_peace", text = "ピースして"),
        DailyPrompt(id = "default_finger_heart", text = "指ハートして"),
        DailyPrompt(id = "default_wave", text = "手ふって"),
        DailyPrompt(id = "default_blow_kiss", text = "投げキスして"),
        DailyPrompt(id = "default_wink", text = "ウインクして"),
        DailyPrompt(id = "default_bang", text = "バーンして"),
        DailyPrompt(id = "default_look_here", text = "こっち見て"),
        DailyPrompt(id = "default_point", text = "こっち指さして"),
        DailyPrompt(id = "default_smile", text = "笑って"),
        DailyPrompt(id = "default_heart_hands", text = "両手でハートして"),
        DailyPrompt(id = "default_arm_heart", text = "腕でハートして"),
        DailyPrompt(id = "default_cheek_heart", text = "ほっぺハートして"),
        DailyPrompt(id = "default_eye_contact", text = "目線ちょうだい"),
        DailyPrompt(id = "default_find_me", text = "見つけて"),
        DailyPrompt(id = "default_call_name", text = "名前呼んで"),
        DailyPrompt(id = "default_double_peace", text = "Wピースして"),
        DailyPrompt(id = "default_thumbs_up", text = "グッドして"),
        DailyPrompt(id = "default_ok_sign", text = "OKサインして"),
        DailyPrompt(id = "default_salute", text = "敬礼して"),
        DailyPrompt(id = "default_fist_pump", text = "ガッツポーズして"),
        DailyPrompt(id = "default_spin", text = "くるっと回って"),
        DailyPrompt(id = "default_jump", text = "ジャンプして"),
        DailyPrompt(id = "default_fox", text = "キツネして"),
        DailyPrompt(id = "default_cat", text = "猫ポーズして"),
        DailyPrompt(id = "default_high_five", text = "ハイタッチして"),
        DailyPrompt(id = "default_funny_face", text = "変顔して"),
        DailyPrompt(id = "default_nod", text = "うなずいて"),
        DailyPrompt(id = "default_head_tilt", text = "首かしげて"),
        DailyPrompt(id = "default_pose", text = "決めポーズして"),
        DailyPrompt(id = "default_praise", text = "ほめて")
    )
}
