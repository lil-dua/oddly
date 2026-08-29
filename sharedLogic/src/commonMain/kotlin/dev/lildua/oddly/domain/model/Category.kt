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
    val title: LocalizedText,
    val emoji: String,
    val colorHex: Long,
) {
    HEALTH(
        id = "health",
        title = LocalizedText(vi = "Sức khỏe", en = "Health"),
        emoji = "💪",
        colorHex = 0xFF34D399,
    ),
    RELATIONSHIPS(
        id = "relationships",
        title = LocalizedText(vi = "Mối quan hệ", en = "Relationships"),
        emoji = "💗",
        colorHex = 0xFFFB7BAA,
    ),
    SELF_GROWTH(
        id = "self_growth",
        title = LocalizedText(vi = "Phát triển bản thân", en = "Self Growth"),
        emoji = "🌱",
        colorHex = 0xFFFBBF24,
    ),
    CREATIVITY(
        id = "creativity",
        title = LocalizedText(vi = "Sáng tạo", en = "Creativity"),
        emoji = "🎨",
        colorHex = 0xFFFB923C,
    ),
    FINANCE(
        id = "finance",
        title = LocalizedText(vi = "Tài chính", en = "Finance"),
        emoji = "💰",
        colorHex = 0xFF22D3EE,
    ),
    LIFE_EXPERIENCE(
        id = "life_experience",
        title = LocalizedText(vi = "Trải nghiệm cuộc sống", en = "Life Experience"),
        emoji = "🌍",
        colorHex = 0xFFA78BFA,
    );

    companion object {
        fun fromId(id: String): Category? = entries.firstOrNull { it.id == id }
    }
}
