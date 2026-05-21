package com.kontenery.oauth

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class FileRefreshTokenStore(
    private val filePath: Path,
    initialToken: String,
) : RefreshTokenStore {

    @Volatile
    private var currentToken: String = loadOrSeed(initialToken)

    override fun getRefreshToken(): String = currentToken

    override fun saveRefreshToken(token: String) {
        val trimmed = token.trim()
        require(trimmed.isNotEmpty()) { "Refresh token cannot be empty" }

        filePath.parent?.let { Files.createDirectories(it) }
        val tempFile = filePath.resolveSibling("${filePath.fileName}.tmp")
        Files.writeString(tempFile, trimmed, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)

        currentToken = trimmed
        println("Zapisano nowy refresh token do: $filePath")
    }

    private fun loadOrSeed(initialToken: String): String {
        if (Files.exists(filePath)) {
            val fromFile = Files.readString(filePath).trim()
            if (fromFile.isNotEmpty()) {
                println("Wczytano refresh token z pliku: $filePath")
                return fromFile
            }
        }

        filePath.parent?.let { Files.createDirectories(it) }
        Files.writeString(filePath, initialToken.trim(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        println("Utworzono plik refresh token: $filePath")
        return initialToken.trim()
    }
}
