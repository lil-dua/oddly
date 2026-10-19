package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.LocalizedText
import dev.lildua.oddly.domain.model.Quote

/**
 * Offline quote database (spec §S14). One quote is surfaced per day.
 *
 * Attribution policy: a named author only for works in the public domain (author died before 1956,
 * line traceable to a specific work); a Vietnamese proverb or folk verse is labelled as such; a line
 * written for the app is attributed to "1% HUMAN". A line from a non-English original is the app's
 * own translation in both languages, never a modern published translation. Where the original is
 * English, the English side is that original. Do not add living or recently deceased authors, and do
 * not add a quote whose attribution you cannot trace — use a proverb or a "1% HUMAN" line instead.
 *
 * Ids are stored in favourites, so never rename or remove one; a few ids no longer match their text
 * (marked below) because the content was replaced, not the key.
 */
object QuoteSeed {

    val all: List<Quote> = listOf(
        // Nguồn: Viết cho 1% HUMAN
        // id giữ từ bản trước, nội dung đã thay vì câu cũ của Zig Ziglar còn bản quyền và nguồn gốc còn tranh cãi
        quote(
            id = "q_ziglar_start",
            vi = "Bắt đầu dở cũng không sao. Quan trọng là hôm nay bạn đã bắt đầu.",
            en = "Starting badly is fine. What matters is that you started today.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Aristotle, Đạo đức học Nicomachus II.1 (1103a-b), bản app tự dịch
        // id giữ từ bản trước, nội dung đã thay vì câu cũ là tóm ý của Will Durant (1926) bị gán cho Aristotle
        quote(
            id = "q_aristotle_habit",
            vi = "Ta trở nên công bằng nhờ làm những việc công bằng, và trở nên dũng cảm nhờ làm những việc dũng cảm.",
            en = "We become just by doing just acts, and brave by doing brave acts.",
            authorVi = "Aristotle",
            authorEn = "Aristotle",
        ),
        // Nguồn: Lão Tử, Đạo Đức Kinh chương 64 (千里之行，始於足下), bản app tự dịch
        quote(
            id = "q_lao_tzu_step",
            vi = "Chuyến đi ngàn dặm khởi đầu ngay dưới chân bạn.",
            en = "A journey of a thousand miles starts right where you stand.",
            authorVi = "Lão Tử",
            authorEn = "Lao Tzu",
        ),
        // Nguồn: Viết cho 1% HUMAN
        // id giữ từ bản trước, nội dung đã thay vì câu cũ của James Clear (Atomic Habits, 2018) còn bản quyền
        quote(
            id = "q_clear_systems",
            vi = "Mục tiêu cho bạn hướng đi, còn thói quen mới là con đường bạn thật sự bước mỗi ngày.",
            en = "A goal gives you a direction. Your habits are the road you actually walk each day.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Ngạn ngữ, nguồn không rõ (thường gọi là ngạn ngữ Trung Quốc)
        quote(
            id = "q_tree_time",
            vi = "Thời điểm tốt nhất để trồng một cái cây là hai mươi năm trước. Thời điểm tốt thứ hai là hôm nay.",
            en = "The best time to plant a tree was twenty years ago. The second best time is today.",
            authorVi = "Ngạn ngữ",
            authorEn = "Proverb",
        ),
        // Nguồn: Khổng Tử, Luận ngữ 9.19 (譬如為山，未成一簣，止，吾止也), bản app tự dịch
        // id giữ từ bản trước, nội dung đã thay vì câu cũ khó tìm thấy trong Luận ngữ (nhiều khả năng gán sai)
        quote(
            id = "q_confucius_slow",
            vi = "Ví như đắp núi, chỉ thiếu một sọt đất nữa là xong mà dừng lại, ấy là tự mình dừng.",
            en = "It is like building a mountain: if I stop one basket of earth short of the top, it is I who have stopped.",
            authorVi = "Khổng Tử",
            authorEn = "Confucius",
        ),
        // Nguồn: Viết cho 1% HUMAN
        // id giữ từ bản trước, nội dung đã thay vì câu cũ của Steve Jobs (Stanford 2005) còn bản quyền
        quote(
            id = "q_jobs_dots",
            vi = "Có những việc làm hôm nay chưa thấy ích gì. Cứ làm đi, mai sau bạn sẽ hiểu.",
            en = "Some things you do today won't seem useful yet. Do them anyway. You'll understand later.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Seneca, Thư gửi Lucilius 104.26 (non quia difficilia sunt non audemus, sed quia non audemus difficilia sunt), bản app tự dịch
        quote(
            id = "q_seneca_dare",
            vi = "Không phải vì việc khó mà ta chùn bước. Chính vì ta chùn bước nên việc mới hóa khó.",
            en = "We don't hold back because things are hard. Things are hard because we hold back.",
            authorVi = "Seneca",
            authorEn = "Seneca",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_small_things",
            vi = "Những hành động nhỏ tạo nên sự khác biệt lớn.",
            en = "Small actions add up to a big difference.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Emerson, Society and Solitude, tiểu luận Works and Days (1870)
        // id giữ từ bản trước, nội dung đã thay vì câu cũ là của Henry S. Haskins (1940) bị gán cho Emerson
        quote(
            id = "q_emerson_today",
            vi = "Hãy khắc vào lòng rằng mỗi ngày là ngày tốt nhất trong năm.",
            en = "Write it on your heart that every day is the best day in the year.",
            authorVi = "Ralph Waldo Emerson",
            authorEn = "Ralph Waldo Emerson",
        ),
        // Nguồn: Khuyết danh, cụm từ phổ biến
        quote(
            id = "q_progress_perfection",
            vi = "Tiến bộ, chứ không phải hoàn hảo.",
            en = "Progress, not perfection.",
            authorVi = "Khuyết danh",
            authorEn = "Unknown",
        ),
        // Nguồn: Viết cho 1% HUMAN (1,01^365 ≈ 37,8)
        quote(
            id = "q_one_percent",
            vi = "Tốt hơn 1% mỗi ngày nghĩa là tốt hơn 37 lần sau một năm.",
            en = "1% better every day is 37 times better after a year.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Marcus Aurelius, Suy tưởng (Meditations) 5.20, bản app tự dịch từ tiếng Hy Lạp
        quote(
            id = "q_aurelius_obstacle",
            vi = "Điều cản trở việc làm lại trở thành chính việc cần làm.",
            en = "What impedes the work becomes the work.",
            authorVi = "Marcus Aurelius",
            authorEn = "Marcus Aurelius",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_iron_needle",
            vi = "Có công mài sắt, có ngày nên kim.",
            en = "Grind iron long enough and it becomes a needle.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_tiny_step",
            vi = "Một bước nhỏ hôm nay nhẹ hơn nhiều so với bước lớn mà bạn cứ hẹn ngày mai.",
            en = "A tiny step today weighs less than a big one you keep promising for tomorrow.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Epictetus, Enchiridion (Cẩm nang) 5, bản app tự dịch từ tiếng Hy Lạp
        quote(
            id = "q_epictetus_judgments",
            vi = "Điều làm người ta phiền muộn không phải sự việc, mà là cách họ nghĩ về sự việc.",
            en = "It is not things that trouble people, but what they think about those things.",
            authorVi = "Epictetus",
            authorEn = "Epictetus",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_ant_nest",
            vi = "Kiến tha lâu cũng đầy tổ.",
            en = "Ant by ant, the nest gets filled.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_show_up",
            vi = "Có mặt đều đặn quan trọng hơn làm thật xuất sắc chỉ một lần.",
            en = "Showing up steadily matters more than being brilliant just once.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Thoreau, Walden (1854), chương Kết luận (Conclusion)
        quote(
            id = "q_thoreau_change",
            vi = "Sự vật không đổi; chúng ta mới là người đổi thay.",
            en = "Things do not change; we change.",
            authorVi = "Henry David Thoreau",
            authorEn = "Henry David Thoreau",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_water_stone",
            vi = "Nước chảy đá mòn.",
            en = "Water dripping day after day wears the stone away.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_bad_day",
            vi = "Ngày tệ vẫn đáng để làm một việc nhỏ. Chuỗi ngày không cần hoàn hảo, chỉ cần đừng đứt.",
            en = "Even a bad day deserves one small thing. A streak doesn't have to be perfect, only unbroken.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Benjamin Franklin, Poor Richard's Almanack (1737)
        quote(
            id = "q_franklin_well_done",
            vi = "Làm tốt còn hơn nói hay.",
            en = "Well done is better than well said.",
            authorVi = "Benjamin Franklin",
            authorEn = "Benjamin Franklin",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_day_road",
            vi = "Đi một ngày đàng, học một sàng khôn.",
            en = "A day on the road brings a basketful of wisdom.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_curious",
            vi = "Tò mò là cách đơn giản nhất để một ngày bình thường trở nên thú vị.",
            en = "Curiosity is the simplest way to make an ordinary day interesting.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Mark Twain, Pudd'nhead Wilson (1894), lịch ngôn đầu chương 6
        quote(
            id = "q_twain_habit",
            vi = "Thói quen là thói quen, không ai ném nó qua cửa sổ được, mà phải dỗ nó xuống cầu thang từng bậc một.",
            en = "Habit is habit, and not to be flung out of the window by any man, but coaxed downstairs a step at a time.",
            authorVi = "Mark Twain",
            authorEn = "Mark Twain",
        ),
        // Nguồn: Lev Tolstoy, Chiến tranh và Hòa bình (1869), tập 10 chương 16 (lời tướng Kutuzov), bản app tự dịch từ tiếng Nga
        quote(
            id = "q_tolstoy_patience",
            vi = "Hai chiến binh mạnh nhất là kiên nhẫn và thời gian.",
            en = "The strongest of all warriors are these two: patience and time.",
            authorVi = "Lev Tolstoy",
            authorEn = "Leo Tolstoy",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_beginning_hard",
            vi = "Vạn sự khởi đầu nan.",
            en = "Every beginning is hard.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_kind",
            vi = "Một lời tử tế chỉ tốn vài giây, nhưng có thể theo ai đó suốt cả ngày.",
            en = "A kind word takes a few seconds, but it can stay with someone all day.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Goethe, Faust phần I (1808), Lời mở đầu trên thiên đường (Es irrt der Mensch, solang er strebt), bản app tự dịch
        quote(
            id = "q_goethe_strive",
            vi = "Con người còn phấn đấu thì còn mắc sai lầm.",
            en = "Man errs as long as he strives.",
            authorVi = "Johann Wolfgang von Goethe",
            authorEn = "Johann Wolfgang von Goethe",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_failure_mother",
            vi = "Thất bại là mẹ thành công.",
            en = "Failure is the mother of success.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_patience",
            vi = "Kiên nhẫn không phải là ngồi chờ, mà là tiếp tục làm trong lúc chờ.",
            en = "Patience isn't sitting and waiting. It's carrying on while you wait.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Seneca, Về sự ngắn ngủi của đời người (De Brevitate Vitae) 1.3, bản app tự dịch
        quote(
            id = "q_seneca_time",
            vi = "Không phải ta có ít thời gian, mà là ta bỏ phí quá nhiều.",
            en = "It is not that we have a short time to live, but that we waste a great deal of it.",
            authorVi = "Seneca",
            authorEn = "Seneca",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_waves_oar",
            vi = "Chớ thấy sóng cả mà ngã tay chèo.",
            en = "Don't let a big wave make you drop the oar.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_put_off",
            vi = "Phần lớn việc ta trì hoãn chỉ mất vài phút để làm. Cái nặng là những lần ta nghĩ về nó.",
            en = "Most things we put off take only minutes to do. The weight is in all the thinking about them.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Emerson, Essays: First Series (1841), tiểu luận Compensation
        quote(
            id = "q_emerson_do_thing",
            vi = "Hãy làm việc đó, rồi bạn sẽ có sức mạnh để làm.",
            en = "Do the thing, and you shall have the power.",
            authorVi = "Ralph Waldo Emerson",
            authorEn = "Ralph Waldo Emerson",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_near_ink",
            vi = "Gần mực thì đen, gần đèn thì rạng.",
            en = "Stay near ink and you turn dark; stay near a lamp and you glow.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_pace",
            vi = "Mỗi người có một nhịp riêng. Việc của bạn là giữ nhịp của mình, không phải đuổi theo nhịp người khác.",
            en = "Everyone has their own pace. Your job is to keep yours, not to chase someone else's.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Lão Tử, Đạo Đức Kinh chương 63 (天下難事必作於易，天下大事必作於細), bản app tự dịch
        quote(
            id = "q_lao_tzu_small",
            vi = "Việc khó trong thiên hạ bắt đầu từ chỗ dễ, việc lớn trong thiên hạ bắt đầu từ chỗ nhỏ.",
            en = "The hard things in the world begin with the easy, and the great things begin with the small.",
            authorVi = "Lão Tử",
            authorEn = "Lao Tzu",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_will_way",
            vi = "Có chí thì nên.",
            en = "Where there's a will, there's a way.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_rest",
            vi = "Nghỉ ngơi cũng là một phần của tiến bộ, không phải thứ cản đường nó.",
            en = "Rest is part of progress, not something that stands in its way.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Khổng Tử, Luận ngữ 1.1 (學而時習之，不亦說乎), bản app tự dịch
        quote(
            id = "q_confucius_practice",
            vi = "Học rồi thường xuyên ôn luyện, chẳng phải là điều vui sao?",
            en = "To learn something and then practise it often, is that not a joy?",
            authorVi = "Khổng Tử",
            authorEn = "Confucius",
        ),
        // Nguồn: Tuân Tử, Khuyến học (勸學, chương 1: 不積跬步，無以至千里；不積小流，無以成江海), bản app tự dịch
        quote(
            id = "q_xunzi_half_steps",
            vi = "Không tích từng bước nhỏ thì không thể đi tới ngàn dặm; không gom từng dòng suối nhỏ thì không thể thành sông biển.",
            en = "Without gathering half-steps you cannot travel a thousand miles; without gathering small streams you cannot make rivers and seas.",
            authorVi = "Tuân Tử",
            authorEn = "Xunzi",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_leaf_wrap",
            vi = "Lá lành đùm lá rách.",
            en = "Whole leaves wrap the torn ones.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_notice",
            vi = "Chú ý là món quà rẻ nhất bạn có thể tặng cho một ngày bình thường.",
            en = "Paying attention is the cheapest gift you can give an ordinary day.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Louis Pasteur, bài giảng khai giảng ĐH Lille, 7/12/1854, bản app tự dịch từ tiếng Pháp
        quote(
            id = "q_pasteur_prepared",
            vi = "Trong lĩnh vực quan sát, may mắn chỉ ưu ái những bộ óc đã chuẩn bị.",
            en = "In the field of observation, chance favours only the prepared mind.",
            authorVi = "Louis Pasteur",
            authorEn = "Louis Pasteur",
        ),
        // Nguồn: Ca dao Việt Nam
        quote(
            id = "q_folk_kind_words",
            vi = "Lời nói chẳng mất tiền mua, lựa lời mà nói cho vừa lòng nhau.",
            en = "Words cost nothing to give, so choose the ones that please.",
            authorVi = "Ca dao",
            authorEn = "Vietnamese folk verse",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_small_win",
            vi = "Hãy ăn mừng một việc nhỏ vừa xong. Cảm giác ấy là nhiên liệu cho ngày mai.",
            en = "Celebrate the small thing you just finished. That feeling is fuel for tomorrow.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Marcus Aurelius, Suy tưởng (Meditations) 5.16, bản app tự dịch từ tiếng Hy Lạp
        quote(
            id = "q_aurelius_thoughts",
            vi = "Tâm hồn bị nhuộm bởi màu của những ý nghĩ ta thường có.",
            en = "The soul is dyed by the colour of its thoughts.",
            authorVi = "Marcus Aurelius",
            authorEn = "Marcus Aurelius",
        ),
        // Nguồn: Ca dao Việt Nam
        quote(
            id = "q_folk_gourd",
            vi = "Bầu ơi thương lấy bí cùng, tuy rằng khác giống nhưng chung một giàn.",
            en = "Dear gourd, love the squash beside you: different kinds, yet we share one trellis.",
            authorVi = "Ca dao",
            authorEn = "Vietnamese folk verse",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_mistake",
            vi = "Vấp một lần không có nghĩa là bạn đi sai đường. Đứng dậy và bước tiếp là đủ.",
            en = "Stumbling once doesn't mean you're on the wrong road. Getting up and walking on is enough.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Montaigne, Tiểu luận (Essais) III.13 Về kinh nghiệm (1588), bản app tự dịch từ tiếng Pháp
        quote(
            id = "q_montaigne_human",
            vi = "Chẳng có gì đẹp đẽ và chính đáng hơn là làm người cho tốt và cho trọn vẹn.",
            en = "Nothing is so beautiful and so right as to be a human being well and fully.",
            authorVi = "Michel de Montaigne",
            authorEn = "Michel de Montaigne",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_hardship_wit",
            vi = "Cái khó ló cái khôn.",
            en = "Hardship sharpens the wits.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_phone",
            vi = "Cất điện thoại đi năm phút, bạn có thể nghe thấy chính mình đang nghĩ gì.",
            en = "Put the phone down for five minutes and you may hear yourself think.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Khổng Tử, Luận ngữ 2.15 (學而不思則罔，思而不學則殆), bản app tự dịch
        quote(
            id = "q_confucius_think",
            vi = "Học mà không nghĩ thì vô ích; nghĩ mà không học thì nguy hiểm.",
            en = "Learning without thinking is wasted; thinking without learning is dangerous.",
            authorVi = "Khổng Tử",
            authorEn = "Confucius",
        ),
        // Nguồn: Tục ngữ Việt Nam
        quote(
            id = "q_proverb_fire_gold",
            vi = "Lửa thử vàng, gian nan thử sức.",
            en = "Fire tests gold; hardship tests strength.",
            authorVi = "Tục ngữ Việt Nam",
            authorEn = "Vietnamese proverb",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_compound",
            vi = "Mỗi việc nhỏ bạn làm hôm nay là một khoản gửi vào con người bạn sẽ trở thành.",
            en = "Every small thing you do today is a deposit into the person you will become.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
        // Nguồn: Benjamin Franklin, Poor Richard's Almanack (1748)
        quote(
            id = "q_franklin_lost_time",
            vi = "Thời gian đã mất thì không bao giờ tìm lại được.",
            en = "Lost time is never found again.",
            authorVi = "Benjamin Franklin",
            authorEn = "Benjamin Franklin",
        ),
        // Nguồn: Benjamin Franklin, Poor Richard's Almanack (1750)
        quote(
            id = "q_franklin_little_strokes",
            vi = "Nhiều nhát nhỏ cũng đốn ngã được cây sồi lớn.",
            en = "Little strokes fell great oaks.",
            authorVi = "Benjamin Franklin",
            authorEn = "Benjamin Franklin",
        ),
        // Nguồn: Ca dao Việt Nam
        quote(
            id = "q_folk_one_tree",
            vi = "Một cây làm chẳng nên non, ba cây chụm lại nên hòn núi cao.",
            en = "One tree does not make a mountain; three together make a tall peak.",
            authorVi = "Ca dao",
            authorEn = "Vietnamese folk verse",
        ),
        // Nguồn: Viết cho 1% HUMAN
        quote(
            id = "q_human_enough",
            vi = "Hôm nay làm được một việc nhỏ là đủ. Ngày mai vẫn còn chỗ cho một việc nữa.",
            en = "One small thing today is enough. Tomorrow will have room for another.",
            authorVi = "1% HUMAN",
            authorEn = "1% HUMAN",
        ),
    )

    /** Deterministic pick so the quote of the day is stable within a day. */
    fun forDayIndex(dayIndex: Int): Quote = all[((dayIndex % all.size) + all.size) % all.size]
}

private fun quote(id: String, vi: String, en: String, authorVi: String, authorEn: String) = Quote(
    id = id,
    text = LocalizedText(vi = vi, en = en),
    author = LocalizedText(vi = authorVi, en = authorEn),
)
