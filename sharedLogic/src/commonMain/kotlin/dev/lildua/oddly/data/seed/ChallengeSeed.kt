package dev.lildua.oddly.data.seed

import dev.lildua.oddly.domain.model.Category
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.domain.model.Difficulty
import dev.lildua.oddly.domain.model.LocalizedText

/**
 * Bundled challenge content (spec §10). Ships inside the app so the daily loop
 * works with zero network.
 *
 * Every string is bilingual: an English reader gets English challenges, not an
 * English shell around Vietnamese content. This is the MVP slice — roughly ten
 * per category. The spec targets ~240 for beta; add to [all] as content is
 * written, no code changes required.
 */
object ChallengeSeed {

    val all: List<Challenge> = buildList {
        addAll(health)
        addAll(relationships)
        addAll(selfGrowth)
        addAll(creativity)
        addAll(finance)
        addAll(lifeExperience)
    }

    fun byId(id: String): Challenge? = all.firstOrNull { it.id == id }

    fun byCategory(category: Category): List<Challenge> = all.filter { it.category == category }
}

/** Shorthand so a seed entry stays readable at two languages per field. */
private fun t(vi: String, en: String) = LocalizedText(vi = vi, en = en)

private fun challenge(
    id: String,
    title: LocalizedText,
    shortDescription: LocalizedText,
    category: Category,
    difficulty: Difficulty,
    minutes: Int,
    why: LocalizedText,
    how: List<LocalizedText>,
) = Challenge(
    id = id,
    title = title,
    shortDescription = shortDescription,
    category = category,
    difficulty = difficulty,
    estimatedMinutes = minutes,
    whyItMatters = why,
    howToDoIt = how,
)

private val health = listOf(
    challenge(
        id = "health_walk_500m",
        title = t("Đi bộ 500m mà không dùng điện thoại", "Walk 500m without your phone"),
        shortDescription = t("Để đầu óc được nghỉ, chỉ đi và nhìn xung quanh.", "Let your head rest — just walk and look around."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Một quãng đi bộ ngắn không màn hình giúp đầu óc bạn thoát khỏi vòng lặp thông báo và cơ thể được vận động nhẹ.", "A short screen-free walk pulls your mind out of the notification loop and gives your body a gentle stretch."),
        how = listOf(
            t("Để điện thoại vào túi hoặc để ở nhà.", "Put your phone in a pocket, or leave it at home."),
            t("Đi một vòng quanh khu bạn ở, khoảng 500m.", "Do a lap of your neighbourhood, roughly 500m."),
            t("Chú ý tới ba thứ bạn chưa từng để ý trên đường.", "Notice three things on the way you have never looked at before."),
        ),
    ),
    challenge(
        id = "health_water_2l",
        title = t("Uống đủ 2 lít nước", "Drink two litres of water"),
        shortDescription = t("Chia thành nhiều lần trong ngày, không cần uống một hơi.", "Spread across the day — no need to down it in one go."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Mất nước nhẹ là nguyên nhân phổ biến gây mệt và mất tập trung mà hầu hết mọi người bỏ qua.", "Mild dehydration is a common cause of tiredness and lost focus that most people never connect."),
        how = listOf(
            t("Đặt một chai nước ngay trên bàn làm việc.", "Put a bottle of water on your desk."),
            t("Uống một ngụm mỗi lần bạn đứng dậy.", "Take a sip every time you stand up."),
            t("Kiểm tra lại vào buổi tối xem đã đủ chưa.", "Check in the evening whether you got there."),
        ),
    ),
    challenge(
        id = "health_stretch_3min",
        title = t("Giãn cơ 3 phút", "Stretch for three minutes"),
        shortDescription = t("Vai, cổ và lưng — những chỗ ngồi lâu hay mỏi nhất.", "Shoulders, neck and back — the first places sitting gets you."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 3,
        why = t("Ba phút giãn cơ đủ để giải phóng phần cổ vai gáy bị căng sau nhiều giờ ngồi.", "Three minutes of stretching is enough to release the neck and shoulders after hours in a chair."),
        how = listOf(
            t("Xoay vai chậm 10 vòng về mỗi phía.", "Roll your shoulders slowly, ten times each way."),
            t("Nghiêng cổ sang trái và phải, giữ 15 giây mỗi bên.", "Tilt your neck left and right, holding fifteen seconds a side."),
            t("Cúi người chạm mũi chân, giữ 20 giây.", "Fold forward to your toes and hold for twenty seconds."),
        ),
    ),
    challenge(
        id = "health_stairs",
        title = t("Đi cầu thang bộ thay vì thang máy", "Take the stairs instead of the lift"),
        shortDescription = t("Một lần thôi cũng được tính.", "Once is enough to count."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Leo cầu thang là bài tập tim mạch tiện nhất mà bạn không cần đến phòng gym.", "Climbing stairs is the most convenient cardio there is, and it needs no gym."),
        how = listOf(
            t("Chọn một lần đi lên hoặc xuống trong ngày.", "Pick one trip up or down today."),
            t("Đi chậm, thở đều, không cần vội.", "Go slowly, breathe evenly, no rush."),
        ),
    ),
    challenge(
        id = "health_sleep_early",
        title = t("Đi ngủ sớm hơn 30 phút", "Go to bed thirty minutes earlier"),
        shortDescription = t("Tắt màn hình trước khi lên giường.", "Screens off before you get in."),
        category = Category.HEALTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 30,
        why = t("Nửa tiếng ngủ thêm tạo khác biệt rõ rệt cho tâm trạng ngày hôm sau.", "Half an hour of extra sleep makes a noticeable difference to the next day's mood."),
        how = listOf(
            t("Đặt báo thức nhắc giờ đi ngủ.", "Set an alarm that reminds you to go to bed."),
            t("Cắm sạc điện thoại ở ngoài phòng ngủ.", "Charge your phone outside the bedroom."),
            t("Đọc vài trang sách giấy thay vì lướt mạng.", "Read a few pages of a paper book instead of scrolling."),
        ),
    ),
    challenge(
        id = "health_no_sugar_drink",
        title = t("Một ngày không đồ uống có đường", "A day without sugary drinks"),
        shortDescription = t("Nước lọc, trà không đường hoặc cà phê đen.", "Water, unsweetened tea or black coffee."),
        category = Category.HEALTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 1,
        why = t("Đồ uống ngọt là nguồn đường ẩn lớn nhất trong ngày mà ta ít khi nhận ra.", "Sweet drinks are the largest source of hidden sugar in a day, and the one we notice least."),
        how = listOf(
            t("Quyết định từ sáng, đừng để tới lúc khát mới chọn.", "Decide in the morning, not once you are already thirsty."),
            t("Mang theo chai nước riêng.", "Carry your own bottle of water."),
        ),
    ),
    challenge(
        id = "health_breathe_1min",
        title = t("Hít thở sâu 1 phút", "Breathe deeply for one minute"),
        shortDescription = t("Bốn nhịp vào, bảy nhịp giữ, tám nhịp ra.", "In for four, hold for seven, out for eight."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 1,
        why = t("Thở chậm có chủ đích là cách nhanh nhất để hạ nhịp tim khi bạn đang căng.", "Deliberate slow breathing is the fastest way to bring your heart rate down when you are wound up."),
        how = listOf(
            t("Ngồi thẳng lưng, nhắm mắt.", "Sit up straight and close your eyes."),
            t("Hít vào đếm 4, giữ đếm 7, thở ra đếm 8.", "Breathe in for four, hold for seven, out for eight."),
            t("Lặp lại bốn lần.", "Repeat four times."),
        ),
    ),
    challenge(
        id = "health_veggie_meal",
        title = t("Thêm rau vào một bữa ăn", "Add vegetables to one meal"),
        shortDescription = t("Không cần đổi cả thực đơn, chỉ thêm một phần.", "No need to redo the menu — just add a portion."),
        category = Category.HEALTH,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Thêm vào dễ duy trì hơn nhiều so với cắt bỏ.", "Adding something is far easier to keep up than cutting something out."),
        how = listOf(
            t("Chọn một bữa trong ngày.", "Pick one meal today."),
            t("Thêm một phần rau luộc, salad hoặc trái cây.", "Add a portion of steamed vegetables, salad or fruit."),
        ),
    ),
    challenge(
        id = "health_screen_break",
        title = t("Nghỉ mắt 20 giây mỗi 20 phút", "Rest your eyes 20 seconds every 20 minutes"),
        shortDescription = t("Nhìn ra xa ít nhất 6 mét.", "Look at least six metres away."),
        category = Category.HEALTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 5,
        why = t("Quy tắc 20-20-20 giúp giảm mỏi mắt do nhìn màn hình liên tục.", "The 20-20-20 rule reduces the eye strain that comes from staring at a screen without a break."),
        how = listOf(
            t("Đặt hẹn giờ 20 phút.", "Set a twenty-minute timer."),
            t("Khi chuông kêu, nhìn ra cửa sổ 20 giây.", "When it goes off, look out of the window for twenty seconds."),
            t("Làm được ba lần trong ngày là đạt.", "Three rounds in a day counts as done."),
        ),
    ),
    challenge(
        id = "health_walk_10min",
        title = t("Đi bộ 10 phút sau bữa ăn", "Walk for ten minutes after a meal"),
        shortDescription = t("Ngay sau khi ăn xong, đừng ngồi luôn.", "Right after you finish — don't sit straight back down."),
        category = Category.HEALTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Đi bộ nhẹ sau ăn giúp tiêu hóa và ổn định đường huyết tốt hơn ngồi yên.", "A gentle walk after eating helps digestion and blood sugar more than sitting still does."),
        how = listOf(
            t("Đứng dậy trong vòng 10 phút sau khi ăn.", "Get up within ten minutes of finishing."),
            t("Đi chậm quanh nhà hoặc quanh văn phòng.", "Walk slowly around the house or the office."),
        ),
    ),
)

private val relationships = listOf(
    challenge(
        id = "rel_thank_stranger",
        title = t("Nói lời cảm ơn với một người xa lạ", "Thank a stranger"),
        shortDescription = t("Đôi khi một câu nói nhỏ có thể làm ai đó vui cả ngày.", "Sometimes one small sentence makes someone's whole day."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 2,
        why = t("Lời cảm ơn cụ thể, nhìn thẳng vào mắt người nghe có sức nặng hơn ta tưởng — với cả hai phía.", "A specific thank you, said while looking someone in the eye, carries more weight than we expect — on both sides."),
        how = listOf(
            t("Có thể là nhân viên phục vụ, bảo vệ, tài xế, người bán hàng.", "It could be a waiter, a security guard, a driver, a shopkeeper."),
            t("Hãy nhìn vào mắt họ và nói một câu chân thành.", "Look them in the eye and say one sincere sentence."),
            t("Bạn sẽ ngạc nhiên về cảm giác sau khi làm điều này.", "You will be surprised by how it feels afterwards."),
        ),
    ),
    challenge(
        id = "rel_check_in",
        title = t("Hỏi thăm ai đó: \"Hôm nay bạn thế nào?\"", "Ask someone how their day really is"),
        shortDescription = t("Và thật sự lắng nghe câu trả lời.", "And actually listen to the answer."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Phần lớn câu hỏi thăm chỉ là phép lịch sự. Hỏi thật rồi nghe thật là chuyện hiếm.", "Most check-ins are just politeness. Asking properly and then listening properly is rare."),
        how = listOf(
            t("Chọn một người bạn quan tâm.", "Pick someone you care about."),
            t("Hỏi và đừng ngắt lời trong ít nhất một phút.", "Ask, and don't interrupt for at least a minute."),
            t("Hỏi thêm một câu nữa dựa trên điều họ vừa nói.", "Ask one more question based on what they just said."),
        ),
    ),
    challenge(
        id = "rel_old_friend",
        title = t("Nhắn tin cho một người bạn lâu chưa gặp", "Message a friend you haven't seen in ages"),
        shortDescription = t("Không cần lý do đặc biệt.", "You don't need a reason."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Mối quan hệ nguội đi vì không ai muốn là người nhắn trước. Hôm nay bạn là người đó.", "Friendships cool because nobody wants to be the one who messages first. Today that is you."),
        how = listOf(
            t("Nghĩ tới người bạn chợt nhớ gần đây.", "Think of the friend who has crossed your mind lately."),
            t("Nhắn một câu đơn giản: \"Tự nhiên nhớ ông, dạo này sao rồi?\"", "Send something simple: \"You popped into my head — how have you been?\""),
        ),
    ),
    challenge(
        id = "rel_compliment",
        title = t("Khen ai đó một cách cụ thể", "Give someone a specific compliment"),
        shortDescription = t("Khen việc họ làm, không phải vẻ ngoài.", "Praise what they did, not how they look."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 2,
        why = t("Lời khen cụ thể được nhớ lâu vì nó chứng minh bạn thật sự để ý.", "Specific praise is remembered because it proves you were actually paying attention."),
        how = listOf(
            t("Chọn một việc họ vừa làm tốt.", "Pick one thing they have just done well."),
            t("Nói rõ bạn thấy gì và vì sao nó đáng ghi nhận.", "Say exactly what you saw and why it deserves noticing."),
        ),
    ),
    challenge(
        id = "rel_call_family",
        title = t("Gọi điện cho người thân", "Ring a family member"),
        shortDescription = t("Gọi thoại, không nhắn tin.", "An actual call, not a message."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Giọng nói truyền tải nhiều hơn chữ viết rất nhiều, đặc biệt với người lớn tuổi.", "A voice carries far more than text does, especially with older relatives."),
        how = listOf(
            t("Chọn lúc bạn không vội.", "Pick a moment when you are not in a hurry."),
            t("Gọi và hỏi về một chuyện cụ thể trong ngày của họ.", "Call, and ask about one specific thing from their day."),
        ),
    ),
    challenge(
        id = "rel_listen_no_phone",
        title = t("Nói chuyện 10 phút không chạm điện thoại", "Talk for ten minutes without touching your phone"),
        shortDescription = t("Úp máy xuống và để nguyên đó.", "Turn it face down and leave it there."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Chỉ cần chiếc điện thoại nằm ngửa trên bàn cũng đủ làm chất lượng cuộc trò chuyện giảm xuống.", "A phone lying face up on the table is enough on its own to lower the quality of a conversation."),
        how = listOf(
            t("Úp điện thoại xuống hoặc cất đi.", "Turn your phone face down, or put it away."),
            t("Giữ như vậy trong suốt cuộc nói chuyện.", "Leave it there for the whole conversation."),
        ),
    ),
    challenge(
        id = "rel_say_sorry",
        title = t("Xin lỗi về một chuyện bạn còn áy náy", "Apologise for something still nagging at you"),
        shortDescription = t("Ngắn gọn, không kèm lời biện minh.", "Short, and with no justification attached."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.HARD,
        minutes = 10,
        why = t("Một lời xin lỗi không kèm chữ \"nhưng\" là điều khó làm và có giá trị nhất.", "An apology with no \"but\" in it is the hardest kind to give and the most valuable."),
        how = listOf(
            t("Nói rõ bạn xin lỗi về việc gì.", "Say clearly what you are apologising for."),
            t("Không giải thích, không đổ lỗi hoàn cảnh.", "No explaining, no blaming the circumstances."),
            t("Hỏi xem bạn có thể làm gì để sửa.", "Ask what you could do to put it right."),
        ),
    ),
    challenge(
        id = "rel_thank_note",
        title = t("Viết lời cảm ơn cho một đồng nghiệp", "Write a thank you to a colleague"),
        shortDescription = t("Một tin nhắn cũng được, miễn là cụ thể.", "A message is fine, as long as it is specific."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Ghi nhận công sức là thứ rẻ nhất để cho đi nhưng lại hiếm nhất ở nơi làm việc.", "Acknowledging someone's effort is the cheapest thing to give and the rarest thing at work."),
        how = listOf(
            t("Nhớ lại một việc họ giúp bạn gần đây.", "Think of something they helped you with recently."),
            t("Nhắn cho họ và nói rõ việc đó đã giúp bạn thế nào.", "Message them and say exactly how it helped."),
        ),
    ),
    challenge(
        id = "rel_share_meal",
        title = t("Ăn một bữa cùng người khác", "Eat a meal with someone"),
        shortDescription = t("Không vừa ăn vừa xem gì cả.", "And don't watch anything while you do."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.MEDIUM,
        minutes = 30,
        why = t("Ăn cùng nhau là nghi thức kết nối lâu đời nhất của con người.", "Eating together is the oldest connection ritual humans have."),
        how = listOf(
            t("Rủ một người ăn trưa hoặc ăn tối.", "Invite someone to lunch or dinner."),
            t("Tắt TV, cất điện thoại.", "Turn the TV off and put the phones away."),
        ),
    ),
    challenge(
        id = "rel_help_small",
        title = t("Giúp ai đó một việc rất nhỏ", "Help someone with something tiny"),
        shortDescription = t("Trước cả khi họ kịp nhờ.", "Before they get the chance to ask."),
        category = Category.RELATIONSHIPS,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Sự giúp đỡ không được yêu cầu là loại tử tế được nhớ lâu nhất.", "Unasked-for help is the kind of kindness that gets remembered longest."),
        how = listOf(
            t("Để ý ai đó đang loay hoay.", "Notice someone struggling with something."),
            t("Giữ cửa, xách giúp đồ, chỉ đường — bất cứ việc gì.", "Hold a door, carry a bag, give directions — anything at all."),
        ),
    ),
)

private val selfGrowth = listOf(
    challenge(
        id = "growth_read_10_pages",
        title = t("Đọc 10 trang sách", "Read ten pages"),
        shortDescription = t("Sách giấy hay ebook đều được.", "Paper or ebook, either is fine."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.EASY,
        minutes = 15,
        why = t("Mười trang mỗi ngày là khoảng 12 cuốn sách một năm mà gần như không tốn công.", "Ten pages a day is about twelve books a year for almost no effort."),
        how = listOf(
            t("Chọn cuốn sách đang đọc dở.", "Pick up the book you are part-way through."),
            t("Đặt hẹn giờ 15 phút.", "Set a fifteen-minute timer."),
            t("Đọc tới hết trang thứ 10 rồi dừng.", "Read to the end of page ten, then stop."),
        ),
    ),
    challenge(
        id = "growth_10_words",
        title = t("Học 10 từ mới", "Learn ten new words"),
        shortDescription = t("Bất kỳ ngôn ngữ nào bạn đang học.", "In whichever language you are studying."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Từ vựng là phần dễ tích lũy nhất và cũng là phần quyết định bạn hiểu được bao nhiêu.", "Vocabulary is the easiest thing to accumulate and the thing that decides how much you understand."),
        how = listOf(
            t("Chọn 10 từ từ một bài đọc hoặc app.", "Pick ten words from a text or an app."),
            t("Viết mỗi từ vào một câu của riêng bạn.", "Write each one into a sentence of your own."),
        ),
    ),
    challenge(
        id = "growth_journal_3_lines",
        title = t("Viết 3 dòng về hôm nay", "Write three lines about today"),
        shortDescription = t("Một điều tốt, một điều khó, một điều học được.", "One good thing, one hard thing, one thing you learned."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Viết ra buộc suy nghĩ mơ hồ phải thành câu rõ ràng — đó là lúc bạn thật sự hiểu mình.", "Writing forces vague thoughts into clear sentences — that is the moment you actually understand yourself."),
        how = listOf(
            t("Mở ghi chú trên điện thoại.", "Open the notes app on your phone."),
            t("Viết đúng ba dòng, không cần dài hơn.", "Write exactly three lines, no more."),
        ),
    ),
    challenge(
        id = "growth_learn_concept",
        title = t("Tìm hiểu một khái niệm bạn hay gật gù cho qua", "Look up a concept you usually nod along to"),
        shortDescription = t("Thứ mà bạn vẫn giả vờ là mình đã hiểu.", "The one you keep pretending you already understand."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Những lỗ hổng kiến thức nhỏ tích lại thành sự thiếu tự tin lớn.", "Small gaps in knowledge add up into a large lack of confidence."),
        how = listOf(
            t("Nghĩ ra một từ bạn hay nghe nhưng chưa hiểu rõ.", "Think of a word you hear often but have never pinned down."),
            t("Đọc về nó 15 phút.", "Read about it for fifteen minutes."),
            t("Giải thích lại bằng một câu của bạn.", "Explain it back in one sentence of your own."),
        ),
    ),
    challenge(
        id = "growth_no_social_1h",
        title = t("Một tiếng không mạng xã hội", "An hour without social media"),
        shortDescription = t("Lúc bạn thường lướt nhiều nhất.", "During the hour you normally scroll most."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 60,
        why = t("Nhận ra mình định mở app bao nhiêu lần trong một tiếng còn giá trị hơn chính việc nhịn.", "Noticing how many times you reach for the app in an hour is worth more than the abstinence itself."),
        how = listOf(
            t("Chọn khung giờ và tắt thông báo.", "Pick the hour and turn off notifications."),
            t("Đếm số lần bạn theo phản xạ định mở app.", "Count how many times you reach for the app on reflex."),
        ),
    ),
    challenge(
        id = "growth_teach_someone",
        title = t("Dạy ai đó một thứ bạn biết", "Teach someone something you know"),
        shortDescription = t("Năm phút là đủ.", "Five minutes is plenty."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Bạn chỉ thật sự hiểu một thứ khi giải thích được cho người chưa biết gì về nó.", "You only truly understand something once you can explain it to someone who knows nothing about it."),
        how = listOf(
            t("Chọn một kỹ năng nhỏ bạn thành thạo.", "Pick a small skill you are good at."),
            t("Giải thích cho một người, không dùng thuật ngữ.", "Explain it to one person, without jargon."),
        ),
    ),
    challenge(
        id = "growth_review_week",
        title = t("Nhìn lại tuần vừa qua trong 10 phút", "Look back at the week for ten minutes"),
        shortDescription = t("Cái gì hiệu quả, cái gì không.", "What worked, what didn't."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Không nhìn lại thì kinh nghiệm chỉ là chuyện đã xảy ra, không thành bài học.", "Without a look back, experience is just something that happened — it never becomes a lesson."),
        how = listOf(
            t("Viết ra ba việc hiệu quả.", "Write down three things that worked."),
            t("Viết ra một việc bạn sẽ làm khác đi.", "Write down one thing you will do differently."),
        ),
    ),
    challenge(
        id = "growth_listen_podcast",
        title = t("Nghe một tập podcast về chủ đề lạ", "Listen to a podcast on an unfamiliar subject"),
        shortDescription = t("Lĩnh vực bạn chưa từng quan tâm.", "A field you have never cared about."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.EASY,
        minutes = 30,
        why = t("Ý tưởng mới thường đến từ việc ghép hai lĩnh vực không liên quan lại với nhau.", "New ideas usually come from bolting two unrelated fields together."),
        how = listOf(
            t("Chọn một chủ đề bạn thấy xa lạ.", "Pick a subject that feels alien to you."),
            t("Nghe hết một tập.", "Listen to a whole episode."),
        ),
    ),
    challenge(
        id = "growth_single_task",
        title = t("Làm một việc trong 25 phút không chuyển tab", "Do one thing for 25 minutes without switching tabs"),
        shortDescription = t("Một việc duy nhất, không đa nhiệm.", "One task only, no multitasking."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.HARD,
        minutes = 25,
        why = t("Mỗi lần chuyển việc bạn mất trung bình vài phút để lấy lại nhịp tập trung.", "Every switch costs you a few minutes on average just to get your focus back."),
        how = listOf(
            t("Chọn một việc cụ thể.", "Pick one specific task."),
            t("Đóng hết tab và app không liên quan.", "Close every unrelated tab and app."),
            t("Đặt hẹn giờ 25 phút và chỉ làm việc đó.", "Set a 25-minute timer and do only that."),
        ),
    ),
    challenge(
        id = "growth_ask_feedback",
        title = t("Hỏi xin một góp ý thẳng thắn", "Ask for one piece of honest feedback"),
        shortDescription = t("Về một việc cụ thể bạn vừa làm.", "About something specific you have just done."),
        category = Category.SELF_GROWTH,
        difficulty = Difficulty.HARD,
        minutes = 15,
        why = t("Người ta chỉ nói thật khi bạn hỏi về một việc cụ thể chứ không hỏi chung chung.", "People only tell you the truth when you ask about something specific rather than in general."),
        how = listOf(
            t("Chọn một người bạn tin.", "Pick someone you trust."),
            t("Hỏi: \"Có một việc gì tôi nên làm khác đi không?\"", "Ask: \"Is there one thing I should do differently?\""),
            t("Nghe hết, không phản biện ngay.", "Listen to all of it without arguing back."),
        ),
    ),
)

private val creativity = listOf(
    challenge(
        id = "create_photo_odd",
        title = t("Chụp ảnh một vật kỳ lạ", "Photograph something odd"),
        shortDescription = t("Thứ bạn đi qua mỗi ngày mà chưa từng nhìn kỹ.", "Something you walk past daily and have never really looked at."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Cầm máy lên buộc bạn nhìn thế giới quen thuộc theo cách khác.", "Picking up a camera forces you to see a familiar world differently."),
        how = listOf(
            t("Đi bộ và tìm một thứ trông lạ.", "Go for a walk and find something that looks strange."),
            t("Chụp ít nhất ba góc khác nhau.", "Shoot it from at least three different angles."),
        ),
    ),
    challenge(
        id = "create_3_sentence_story",
        title = t("Viết một câu chuyện 3 câu", "Write a three-sentence story"),
        shortDescription = t("Mở đầu, biến cố, kết thúc.", "Opening, turn, ending."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Giới hạn chặt làm sự sáng tạo bật ra nhanh hơn là một trang giấy trắng.", "A tight constraint sparks creativity faster than a blank page ever does."),
        how = listOf(
            t("Câu 1: giới thiệu một nhân vật.", "Sentence one: introduce a character."),
            t("Câu 2: một chuyện bất ngờ xảy ra.", "Sentence two: something unexpected happens."),
            t("Câu 3: kết thúc, có thể mở.", "Sentence three: an ending, open if you like."),
        ),
    ),
    challenge(
        id = "create_doodle",
        title = t("Vẽ nguệch ngoạc 5 phút", "Doodle for five minutes"),
        shortDescription = t("Không cần đẹp, chỉ cần vẽ.", "It doesn't have to be good — just draw."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Vẽ tay kích hoạt phần não khác hẳn với gõ phím.", "Drawing by hand lights up a completely different part of your brain than typing does."),
        how = listOf(
            t("Lấy giấy và bút bất kỳ.", "Grab any paper and any pen."),
            t("Vẽ thứ trước mặt bạn, không tẩy xóa.", "Draw whatever is in front of you, without erasing."),
        ),
    ),
    challenge(
        id = "create_new_playlist",
        title = t("Nghe một thể loại nhạc bạn chưa từng nghe", "Listen to a genre you have never tried"),
        shortDescription = t("Cả một album, không phải một bài.", "A whole album, not one track."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.EASY,
        minutes = 30,
        why = t("Gu thẩm mỹ mở rộng nhờ tiếp xúc, không nhờ suy nghĩ về nó.", "Taste widens through exposure, not through thinking about it."),
        how = listOf(
            t("Chọn một thể loại bạn hay bỏ qua.", "Pick a genre you usually skip past."),
            t("Nghe hết một album.", "Listen to a full album."),
        ),
    ),
    challenge(
        id = "create_rearrange",
        title = t("Sắp xếp lại một góc trong nhà", "Rearrange one corner of your home"),
        shortDescription = t("Bàn làm việc, kệ sách, góc bếp.", "A desk, a bookshelf, a corner of the kitchen."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Thay đổi không gian vật lý là cách nhanh nhất để phá vỡ lối mòn suy nghĩ.", "Changing physical space is the quickest way to break a mental rut."),
        how = listOf(
            t("Chọn một góc nhỏ.", "Pick one small corner."),
            t("Bỏ hết ra rồi xếp lại theo cách khác.", "Take everything out, then put it back a different way."),
        ),
    ),
    challenge(
        id = "create_10_ideas",
        title = t("Viết ra 10 ý tưởng tệ", "Write down ten bad ideas"),
        shortDescription = t("Càng tệ càng tốt.", "The worse the better."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Cho phép mình nghĩ ra thứ dở là cách duy nhất để vượt qua nỗi sợ trang giấy trắng.", "Letting yourself produce something rubbish is the only way past the fear of the blank page."),
        how = listOf(
            t("Chọn một vấn đề bất kỳ.", "Pick any problem at all."),
            t("Viết 10 giải pháp, không được tự phê bình.", "Write ten solutions, with no self-criticism allowed."),
        ),
    ),
    challenge(
        id = "create_cook_new",
        title = t("Nấu một món bạn chưa từng nấu", "Cook something you have never cooked"),
        shortDescription = t("Đơn giản thôi cũng được.", "Simple is completely fine."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.HARD,
        minutes = 45,
        why = t("Nấu ăn là sáng tạo có kết quả ăn được ngay.", "Cooking is creativity with an immediately edible result."),
        how = listOf(
            t("Tìm một công thức đơn giản.", "Find a simple recipe."),
            t("Mua đủ nguyên liệu.", "Buy everything you need."),
            t("Làm theo và chấp nhận kết quả.", "Follow it and accept whatever comes out."),
        ),
    ),
    challenge(
        id = "create_short_poem",
        title = t("Viết một bài thơ ngắn về hôm nay", "Write a short poem about today"),
        shortDescription = t("Ba dòng, không cần vần.", "Three lines, no rhyme needed."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Nén một ngày vào ba dòng buộc bạn chọn ra thứ thật sự quan trọng.", "Compressing a day into three lines forces you to choose what actually mattered."),
        how = listOf(
            t("Nghĩ về một khoảnh khắc cụ thể trong ngày.", "Think of one specific moment from your day."),
            t("Viết ba dòng mô tả nó.", "Write three lines describing it."),
        ),
    ),
    challenge(
        id = "create_photo_color",
        title = t("Chụp 5 tấm ảnh cùng một màu", "Take five photos of the same colour"),
        shortDescription = t("Chọn một màu và đi tìm.", "Pick a colour and go hunting."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.EASY,
        minutes = 20,
        why = t("Đặt ra một luật chơi tùy hứng làm việc đi bộ bình thường thành một cuộc săn tìm.", "An arbitrary rule turns an ordinary walk into a treasure hunt."),
        how = listOf(
            t("Chọn một màu bất kỳ.", "Pick any colour."),
            t("Tìm và chụp 5 vật mang màu đó.", "Find and photograph five things in that colour."),
        ),
    ),
    challenge(
        id = "create_redesign",
        title = t("Nghĩ lại cách thiết kế một vật quen thuộc", "Redesign something familiar in your head"),
        shortDescription = t("Cái điều khiển TV, cái ổ cắm, cái thang máy.", "The TV remote, a plug socket, a lift."),
        category = Category.CREATIVITY,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Tư duy thiết kế bắt đầu từ việc nhận ra thứ tưởng đã hoàn hảo thật ra rất dở.", "Design thinking starts with noticing that something you assumed was finished is actually rather bad."),
        how = listOf(
            t("Chọn một vật bạn dùng hằng ngày.", "Pick an object you use every day."),
            t("Viết ra ba điều khó chịu về nó.", "Write down three things that annoy you about it."),
            t("Phác thảo một cách làm tốt hơn.", "Sketch a better way of doing it."),
        ),
    ),
)

private val finance = listOf(
    challenge(
        id = "fin_review_expense",
        title = t("Kiểm tra một khoản chi trong tuần", "Review one purchase from this week"),
        shortDescription = t("Xem lại xem có đáng không.", "Ask yourself whether it was worth it."),
        category = Category.FINANCE,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Nhìn lại một khoản đã tiêu dạy bạn nhiều hơn là lập ngân sách cho tương lai.", "Looking back at money already spent teaches you more than budgeting for the future does."),
        how = listOf(
            t("Mở lịch sử giao dịch.", "Open your transaction history."),
            t("Chọn khoản lớn nhất tuần này.", "Pick the largest amount from this week."),
            t("Tự hỏi: nếu quay lại, bạn có tiêu nữa không?", "Ask yourself: knowing what you know, would you spend it again?"),
        ),
    ),
    challenge(
        id = "fin_skip_purchase",
        title = t("Bỏ qua một món bạn không thật sự cần", "Skip something you don't really need"),
        shortDescription = t("Món đang nằm trong giỏ hàng.", "The thing sitting in your basket right now."),
        category = Category.FINANCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 5,
        why = t("Khoảng cách giữa \"muốn\" và \"cần\" thường chỉ dài bằng 24 tiếng.", "The gap between wanting and needing is usually only about 24 hours wide."),
        how = listOf(
            t("Mở giỏ hàng đang có.", "Open the basket you have open somewhere."),
            t("Xóa một món và chờ tới mai.", "Remove one item and wait until tomorrow."),
        ),
    ),
    challenge(
        id = "fin_check_subscriptions",
        title = t("Rà lại các gói đăng ký hằng tháng", "Audit your monthly subscriptions"),
        shortDescription = t("Xem bạn còn dùng cái nào.", "See which ones you still actually use."),
        category = Category.FINANCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 15,
        why = t("Gói đăng ký quên hủy là khoản rò rỉ tiền âm thầm nhất.", "A subscription you forgot to cancel is the quietest money leak there is."),
        how = listOf(
            t("Mở danh sách đăng ký trên điện thoại.", "Open the subscription list on your phone."),
            t("Đánh dấu cái nào bạn chưa mở trong 30 ngày.", "Mark any you haven't opened in thirty days."),
        ),
    ),
    challenge(
        id = "fin_save_small",
        title = t("Bỏ ống một khoản rất nhỏ", "Put away a very small amount"),
        shortDescription = t("Bằng đúng một ly cà phê.", "Exactly one cup of coffee's worth."),
        category = Category.FINANCE,
        difficulty = Difficulty.EASY,
        minutes = 5,
        why = t("Thói quen tiết kiệm quan trọng hơn số tiền tiết kiệm ở giai đoạn đầu.", "Early on, the habit of saving matters far more than the amount saved."),
        how = listOf(
            t("Chuyển một khoản nhỏ sang tài khoản tiết kiệm.", "Move a small amount into a savings account."),
            t("Đừng chạm vào nó.", "Then leave it alone."),
        ),
    ),
    challenge(
        id = "fin_price_compare",
        title = t("So giá trước khi mua một món", "Compare prices before you buy"),
        shortDescription = t("Ít nhất hai nơi bán.", "At least two sellers."),
        category = Category.FINANCE,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Năm phút so giá thường tiết kiệm nhiều hơn một giờ làm thêm.", "Five minutes of comparing usually saves more than an hour of overtime earns."),
        how = listOf(
            t("Chọn món bạn định mua.", "Pick the thing you are about to buy."),
            t("Kiểm tra giá ở hai nơi khác nhau.", "Check the price in two different places."),
        ),
    ),
    challenge(
        id = "fin_no_spend_day",
        title = t("Một ngày không tiêu gì cả", "A day with no spending at all"),
        shortDescription = t("Ngoài các chi phí bắt buộc.", "Beyond the unavoidable costs."),
        category = Category.FINANCE,
        difficulty = Difficulty.HARD,
        minutes = 1,
        why = t("Một ngày không tiêu cho thấy rõ bao nhiêu khoản chi của bạn là do phản xạ.", "One no-spend day shows you exactly how much of your spending is pure reflex."),
        how = listOf(
            t("Chuẩn bị đồ ăn từ tối hôm trước.", "Prepare food the night before."),
            t("Không mở app mua sắm.", "Don't open any shopping apps."),
        ),
    ),
    challenge(
        id = "fin_track_today",
        title = t("Ghi lại mọi khoản chi hôm nay", "Log every expense today"),
        shortDescription = t("Kể cả 5 nghìn gửi xe.", "Including the small change for parking."),
        category = Category.FINANCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 10,
        why = t("Bạn không thể quản lý thứ bạn không nhìn thấy.", "You cannot manage what you never look at."),
        how = listOf(
            t("Mở ghi chú và ghi ngay sau mỗi lần chi.", "Open your notes and log each spend as it happens."),
            t("Cộng tổng vào cuối ngày.", "Add it all up at the end of the day."),
        ),
    ),
    challenge(
        id = "fin_learn_term",
        title = t("Học một khái niệm tài chính", "Learn one financial concept"),
        shortDescription = t("Lãi kép, lạm phát, quỹ khẩn cấp...", "Compound interest, inflation, emergency fund..."),
        category = Category.FINANCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Hiểu biết tài chính là kỹ năng có lợi suất cao nhất mà trường học không dạy.", "Financial literacy is the highest-return skill school never teaches."),
        how = listOf(
            t("Chọn một khái niệm.", "Pick one concept."),
            t("Đọc 15 phút và viết lại bằng lời của bạn.", "Read for fifteen minutes and write it back in your own words."),
        ),
    ),
    challenge(
        id = "fin_sell_unused",
        title = t("Rao bán hoặc cho đi một món không dùng", "Sell or give away something unused"),
        shortDescription = t("Món đã nằm im hơn một năm.", "Something that hasn't moved in over a year."),
        category = Category.FINANCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Đồ không dùng vẫn tốn chỗ, tốn sự chú ý và giữ lại tiền chết.", "Unused things still cost you space, attention, and money left sitting dead."),
        how = listOf(
            t("Tìm một món bạn không đụng tới cả năm.", "Find something you haven't touched all year."),
            t("Chụp ảnh và đăng bán, hoặc đem cho.", "Photograph it and list it, or give it away."),
        ),
    ),
    challenge(
        id = "fin_review_goal",
        title = t("Đặt một mục tiêu tiền bạc nhỏ", "Set one small money goal"),
        shortDescription = t("Cho tháng này thôi.", "Just for this month."),
        category = Category.FINANCE,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Mục tiêu mơ hồ thì không đo được, mà không đo được thì không đạt được.", "A vague goal can't be measured, and what can't be measured doesn't get reached."),
        how = listOf(
            t("Viết một mục tiêu có con số và thời hạn.", "Write a goal with a number and a deadline."),
            t("Dán nó ở nơi bạn nhìn thấy mỗi ngày.", "Stick it somewhere you see every day."),
        ),
    ),
)

private val lifeExperience = listOf(
    challenge(
        id = "life_new_seat",
        title = t("Ngồi ở một nơi mình chưa từng ngồi", "Sit somewhere you have never sat"),
        shortDescription = t("Trong nhà, quán quen hay công viên gần đó.", "At home, in your usual cafe, or a nearby park."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.EASY,
        minutes = 15,
        why = t("Đổi góc nhìn theo nghĩa đen là cách rẻ nhất để đổi góc nhìn theo nghĩa bóng.", "Changing your viewpoint literally is the cheapest way to change it figuratively."),
        how = listOf(
            t("Chọn một chỗ ngồi mới.", "Pick a new place to sit."),
            t("Ngồi yên ít nhất 10 phút.", "Stay there for at least ten minutes."),
            t("Để ý xem bạn thấy gì khác so với chỗ quen.", "Notice what looks different from your usual spot."),
        ),
    ),
    challenge(
        id = "life_new_route",
        title = t("Đi một con đường khác về nhà", "Take a different route home"),
        shortDescription = t("Dài hơn vài phút cũng không sao.", "A few minutes longer is fine."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.EASY,
        minutes = 15,
        why = t("Não bạn tắt chế độ chú ý trên những tuyến đường đã quá quen.", "Your brain switches attention off on routes it already knows too well."),
        how = listOf(
            t("Chọn một hướng đi khác thường ngày.", "Pick a direction you don't normally take."),
            t("Không mở bản đồ nếu bạn vẫn biết đường.", "Skip the map if you already know the way."),
        ),
    ),
    challenge(
        id = "life_new_shop",
        title = t("Thử một quán mới", "Try a new place"),
        shortDescription = t("Quán bạn vẫn đi qua mà chưa vào.", "The one you keep walking past without going in."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.EASY,
        minutes = 30,
        why = t("Chọn cái quen là an toàn, nhưng cũng là cách chắc chắn để mọi ngày giống hệt nhau.", "Choosing the familiar is safe, and also the surest way to make every day identical."),
        how = listOf(
            t("Nhớ lại một quán bạn hay đi ngang.", "Think of a place you often walk past."),
            t("Vào và gọi thứ gì đó.", "Go in and order something."),
        ),
    ),
    challenge(
        id = "life_sunrise",
        title = t("Xem mặt trời mọc hoặc lặn", "Watch a sunrise or a sunset"),
        shortDescription = t("Không chụp ảnh, chỉ nhìn.", "No photos — just look."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Có những thứ mất giá trị ngay khi bạn nâng điện thoại lên.", "Some things lose their value the moment you raise a phone to them."),
        how = listOf(
            t("Tra giờ mặt trời mọc hoặc lặn.", "Look up the sunrise or sunset time."),
            t("Ra ngoài trước đó 10 phút.", "Get outside ten minutes before it."),
            t("Cất điện thoại đi.", "Put your phone away."),
        ),
    ),
    challenge(
        id = "life_talk_neighbor",
        title = t("Chào một người hàng xóm", "Say hello to a neighbour"),
        shortDescription = t("Người bạn thấy hằng ngày mà chưa nói chuyện.", "The one you see daily and have never spoken to."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 5,
        why = t("Cảm giác thuộc về một nơi được xây từ những cái gật đầu chào rất nhỏ.", "A sense of belonging somewhere is built out of very small nods hello."),
        how = listOf(
            t("Chào và tự giới thiệu.", "Say hello and introduce yourself."),
            t("Không cần nói chuyện lâu.", "It doesn't need to be a long conversation."),
        ),
    ),
    challenge(
        id = "life_new_food",
        title = t("Ăn một món chưa từng ăn", "Eat something you have never eaten"),
        shortDescription = t("Chỉ cần một món trong bữa.", "One dish in a meal is enough."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.EASY,
        minutes = 20,
        why = t("Khẩu vị của bạn rộng hơn nhiều so với danh sách 10 món bạn hay gọi.", "Your palate is far wider than the ten things you keep ordering."),
        how = listOf(
            t("Chọn món lạ nhất trong thực đơn.", "Pick the strangest thing on the menu."),
            t("Gọi nó, kể cả khi không chắc.", "Order it, even if you aren't sure."),
        ),
    ),
    challenge(
        id = "life_walk_no_dest",
        title = t("Đi bộ 20 phút không có đích đến", "Walk for twenty minutes with no destination"),
        shortDescription = t("Rẽ theo cảm hứng.", "Turn wherever you feel like."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.MEDIUM,
        minutes = 20,
        why = t("Đi mà không có mục tiêu là một trong số ít hoạt động còn lại không nhằm tối ưu điều gì.", "Walking without a goal is one of the few activities left that isn't optimising for anything."),
        how = listOf(
            t("Ra khỏi nhà, không chọn hướng trước.", "Leave the house without choosing a direction."),
            t("Mỗi ngã rẽ, chọn hướng bạn ít đi nhất.", "At every junction, take the way you go least often."),
        ),
    ),
    challenge(
        id = "life_visit_local",
        title = t("Ghé một nơi trong thành phố bạn chưa tới", "Visit somewhere in your city you have never been"),
        shortDescription = t("Bảo tàng, công viên, khu chợ.", "A museum, a park, a market."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.HARD,
        minutes = 60,
        why = t("Hầu hết chúng ta là khách du lịch tệ nhất ở chính thành phố mình sống.", "Most of us are the worst tourists in the city we actually live in."),
        how = listOf(
            t("Tìm một địa điểm cách bạn dưới 5km.", "Find somewhere less than 5km away."),
            t("Dành ít nhất một tiếng ở đó.", "Spend at least an hour there."),
        ),
    ),
    challenge(
        id = "life_silence_10min",
        title = t("Ngồi yên 10 phút không làm gì", "Sit still for ten minutes doing nothing"),
        shortDescription = t("Không nhạc, không điện thoại, không sách.", "No music, no phone, no book."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.HARD,
        minutes = 10,
        why = t("Buồn chán là nơi những ý tưởng hay nhất xuất hiện, và ta đã xóa sổ nó khỏi cuộc sống.", "Boredom is where the best ideas turn up, and we have deleted it from our lives."),
        how = listOf(
            t("Đặt hẹn giờ 10 phút.", "Set a ten-minute timer."),
            t("Ngồi và để suy nghĩ trôi đi.", "Sit, and let your thoughts drift."),
        ),
    ),
    challenge(
        id = "life_write_future",
        title = t("Viết một dòng cho bạn của một năm sau", "Write a line to yourself a year from now"),
        shortDescription = t("Cất đi và đọc lại vào năm tới.", "Put it away and read it next year."),
        category = Category.LIFE_EXPERIENCE,
        difficulty = Difficulty.EASY,
        minutes = 10,
        why = t("Bạn của một năm trước sẽ ngạc nhiên về những gì bạn coi là hiển nhiên hôm nay.", "You a year ago would be amazed at what you now take for granted."),
        how = listOf(
            t("Viết bạn đang lo và đang mong điều gì.", "Write down what you are worried about and what you are hoping for."),
            t("Ghi ngày tháng và cất vào ghi chú.", "Date it and file it in your notes."),
        ),
    ),
)
