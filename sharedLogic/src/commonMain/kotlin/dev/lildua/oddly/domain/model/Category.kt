package dev.lildua.oddly.domain.model

/**
 * The six challenge categories from Appendix B of the product spec.
 *
 * [emoji] is used as the category icon everywhere in the UI so the app stays
 * fully offline with no image assets to ship. [colorHex] is consumed by the UI
 * layer to tint cards, chips and charts.
 */
enum class Category(
    val id: String,
    val title: String,
    val englishTitle: String,
    val emoji: String,
    val colorHex: Long,
) {
    HEALTH(
        id = "health",
        title = "Sức khỏe",
        englishTitle = "Health",
        emoji = "💪",
        colorHex = 0xFF34D399,
    ),
    RELATIONSHIPS(
        id = "relationships",
        title = "Mối quan hệ",
        englishTitle = "Relationships",
        emoji = "💗",
        colorHex = 0xFFFB7BAA,
    ),
    SELF_GROWTH(
        id = "self_growth",
        title = "Phát triển bản thân",
        englishTitle = "Self Growth",
        emoji = "🌱",
        colorHex = 0xFFFBBF24,
    ),
    CREATIVITY(
        id = "creativity",
        title = "Sáng tạo",
        englishTitle = "Creativity",
        emoji = "🎨",
        colorHex = 0xFFFB923C,
    ),
    FINANCE(
        id = "finance",
        title = "Tài chính",
        englishTitle = "Finance",
        emoji = "💰",
        colorHex = 0xFF22D3EE,
    ),
    LIFE_EXPERIENCE(
        id = "life_experience",
        title = "Trải nghiệm cuộc sống",
        englishTitle = "Life Experience",
        emoji = "🌍",
        colorHex = 0xFFA78BFA,
    );

    companion object {
        fun fromId(id: String): Category? = entries.firstOrNull { it.id == id }
    }
}
