package livecricket.livecrickettv.cricketstreaming.secuirty

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class DatabaseCryptoException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

internal object DatabaseCryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "footscore_room_link_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val VERSION_PREFIX = "v1"

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                if (key != null) {
                    return key
                }
                keyStore.deleteEntry(KEY_ALIAS)
            }

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            throw DatabaseCryptoException("Failed to initialize or retrieve keystore encryption key", e)
        }
    }

    fun encrypt(plaintext: String?): String? {
        if (plaintext == null) return null
        if (plaintext.isEmpty()) return ""

        return try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv ?: throw DatabaseCryptoException("Cipher failed to generate IV")

            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ctBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

            "$VERSION_PREFIX:$ivBase64:$ctBase64"
        } catch (e: DatabaseCryptoException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseCryptoException("Database encryption operation failed", e)
        }
    }

    fun decrypt(encryptedValue: String?): String? {
        if (encryptedValue == null) return null
        if (encryptedValue.isEmpty()) return ""

        if (!encryptedValue.startsWith("$VERSION_PREFIX:")) {
            throw DatabaseCryptoException("Database decryption failed: unsupported format or legacy plaintext value")
        }

        val parts = encryptedValue.split(":")
        if (parts.size != 3) {
            throw DatabaseCryptoException("Database decryption failed: malformed encrypted payload")
        }

        return try {
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[2], Base64.NO_WRAP)
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: DatabaseCryptoException) {
            throw e
        } catch (e: Exception) {
            throw DatabaseCryptoException("Database decryption operation failed", e)
        }
    }
}