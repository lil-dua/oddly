package dev.lildua.oddly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform