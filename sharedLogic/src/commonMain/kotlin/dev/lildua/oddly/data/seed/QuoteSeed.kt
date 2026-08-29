package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.LocalizedText
import dev.lildua.oddly.domain.model.Quote

/**
 * Offline quote database (spec §S14). One quote is surfaced per day.
 *
 * Where a quote has a known original in English, the English side is that
 * original rather than a translation back out of the Vietnamese.
 */
object QuoteSeed {

    val all: List<Quote> = listOf(
        Quote(
            id = "q_ziglar_start",
            text = LocalizedText(
                vi = "Bạn không cần phải tuyệt vời để bắt đầu, nhưng bạn phải bắt đầu để trở nên tuyệt vời.",
                en = "You don't have to be great to start, but you have to start to be great.",
            ),
            author = LocalizedText(vi = "Zig Ziglar", en = "Zig Ziglar"),
        ),
        Quote(
            id = "q_aristotle_habit",
            text = LocalizedText(
                vi = "Chúng ta là những gì chúng ta lặp đi lặp lại. Vì thế sự xuất sắc không phải một hành động, mà là một thói quen.",
                en = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            ),
            author = LocalizedText(vi = "Aristotle", en = "Aristotle"),
        ),
        Quote(
            id = "q_lao_tzu_step",
            text = LocalizedText(
                vi = "Hành trình vạn dặm bắt đầu từ một bước chân.",
                en = "A journey of a thousand miles begins with a single step.",
            ),
            author = LocalizedText(vi = "Lão Tử", en = "Lao Tzu"),
        ),
        Quote(
            id = "q_clear_systems",
            text = LocalizedText(
                vi = "Bạn không vươn tới tầm mục tiêu của mình. Bạn rơi xuống tầm hệ thống của mình.",
                en = "You do not rise to the level of your goals. You fall to the level of your systems.",
            ),
            author = LocalizedText(vi = "James Clear", en = "James Clear"),
        ),
        Quote(
            id = "q_tree_time",
            text = LocalizedText(
                vi = "Thời điểm tốt nhất để trồng một cái cây là hai mươi năm trước. Thời điểm tốt thứ hai là hôm nay.",
                en = "The best time to plant a tree was twenty years ago. The second best time is today.",
            ),
            author = LocalizedText(vi = "Ngạn ngữ", en = "Proverb"),
        ),
        Quote(
            id = "q_confucius_slow",
            text = LocalizedText(
                vi = "Đi chậm không sao cả, miễn là bạn không dừng lại.",
                en = "It does not matter how slowly you go, so long as you do not stop.",
            ),
            author = LocalizedText(vi = "Khổng Tử", en = "Confucius"),
        ),
        Quote(
            id = "q_jobs_dots",
            text = LocalizedText(
                vi = "Bạn không thể nối các dấu chấm khi nhìn về phía trước; bạn chỉ nối được chúng khi nhìn lại.",
                en = "You can't connect the dots looking forward; you can only connect them looking backwards.",
            ),
            author = LocalizedText(vi = "Steve Jobs", en = "Steve Jobs"),
        ),
        Quote(
            id = "q_seneca_dare",
            text = LocalizedText(
                vi = "Không phải vì mọi thứ khó khăn mà ta không dám làm. Chính vì ta không dám làm nên mọi thứ mới khó khăn.",
                en = "It is not because things are difficult that we do not dare; it is because we do not dare that things are difficult.",
            ),
            author = LocalizedText(vi = "Seneca", en = "Seneca"),
        ),
        Quote(
            id = "q_small_things",
            text = LocalizedText(
                vi = "Những hành động nhỏ, tạo nên sự khác biệt lớn.",
                en = "Small actions add up to a big difference.",
            ),
            author = LocalizedText(vi = "1% HUMAN", en = "1% HUMAN"),
        ),
        Quote(
            id = "q_emerson_today",
            text = LocalizedText(
                vi = "Điều nằm phía sau và điều nằm phía trước ta đều nhỏ bé so với điều nằm bên trong ta.",
                en = "What lies behind us and what lies before us are tiny matters compared to what lies within us.",
            ),
            author = LocalizedText(vi = "Ralph Waldo Emerson", en = "Ralph Waldo Emerson"),
        ),
        Quote(
            id = "q_progress_perfection",
            text = LocalizedText(
                vi = "Tiến bộ, chứ không phải hoàn hảo.",
                en = "Progress, not perfection.",
            ),
            author = LocalizedText(vi = "Khuyết danh", en = "Unknown"),
        ),
        Quote(
            id = "q_one_percent",
            text = LocalizedText(
                vi = "Tốt hơn 1% mỗi ngày nghĩa là tốt hơn 37 lần sau một năm.",
                en = "1% better every day is 37 times better after a year.",
            ),
            author = LocalizedText(vi = "1% HUMAN", en = "1% HUMAN"),
        ),
    )

    /** Deterministic pick so the quote of the day is stable within a day. */
    fun forDayIndex(dayIndex: Int): Quote = all[((dayIndex % all.size) + all.size) % all.size]
}
