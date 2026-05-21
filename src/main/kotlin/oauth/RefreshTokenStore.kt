package com.kontenery.oauth

interface RefreshTokenStore {
    fun getRefreshToken(): String
    fun saveRefreshToken(token: String)
}
