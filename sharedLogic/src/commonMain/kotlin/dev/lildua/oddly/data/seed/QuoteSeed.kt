package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.Quote

/** Offline quote database (spec §S14). One quote is surfaced per day. */
object QuoteSeed {

    val all: List<Quote> = listOf(
        Quote(
            id = "q_ziglar_start",
            text = "Bạn không cần phải tuyệt vời để bắt đầu, nhưng bạn phải bắt đầu để trở nên tuyệt vời.",
            author = "Zig Ziglar",
        ),
        Quote(
            id = "q_aristotle_habit",
            text = "Chúng ta là những gì chúng ta lặp đi lặp lại. Vì thế sự xuất sắc không phải một hành động, mà là một thói quen.",
            author = "Aristotle",
        ),
        Quote(
            id = "q_lao_tzu_step",
            text = "Hành trình vạn dặm bắt đầu từ một bước chân.",
            author = "Lão Tử",
        ),
        Quote(
            id = "q_clear_systems",
            text = "Bạn không vươn tới tầm mục tiêu của mình. Bạn rơi xuống tầm hệ thống của mình.",
            author = "James Clear",
        ),
        Quote(
            id = "q_tree_time",
            text = "Thời điểm tốt nhất để trồng một cái cây là hai mươi năm trước. Thời điểm tốt thứ hai là hôm nay.",
            author = "Ngạn ngữ",
        ),
        Quote(
            id = "q_confucius_slow",
            text = "Đi chậm không sao cả, miễn là bạn không dừng lại.",
            author = "Khổng Tử",
        ),
        Quote(
            id = "q_jobs_dots",
            text = "Bạn không thể nối các dấu chấm khi nhìn về phía trước; bạn chỉ nối được chúng khi nhìn lại.",
            author = "Steve Jobs",
        ),
        Quote(
            id = "q_seneca_dare",
            text = "Không phải vì mọi thứ khó khăn mà ta không dám làm. Chính vì ta không dám làm nên mọi thứ mới khó khăn.",
            author = "Seneca",
        ),
        Quote(
            id = "q_small_things",
            text = "Những hành động nhỏ, tạo nên sự khác biệt lớn.",
            author = "1% HUMAN",
        ),
        Quote(
            id = "q_emerson_today",
            text = "Điều nằm phía sau và điều nằm phía trước ta đều nhỏ bé so với điều nằm bên trong ta.",
            author = "Ralph Waldo Emerson",
        ),
        Quote(
            id = "q_progress_perfection",
            text = "Tiến bộ, chứ không phải hoàn hảo.",
            author = "Khuyết danh",
        ),
        Quote(
            id = "q_one_percent",
            text = "Tốt hơn 1% mỗi ngày nghĩa là tốt hơn 37 lần sau một năm.",
            author = "1% HUMAN",
        ),
    )

    /** Deterministic pick so the quote of the day is stable within a day. */
    fun forDayIndex(dayIndex: Int): Quote = all[((dayIndex % all.size) + all.size) % all.size]
}
