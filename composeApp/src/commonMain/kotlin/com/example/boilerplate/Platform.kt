package com.example.boilerplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform