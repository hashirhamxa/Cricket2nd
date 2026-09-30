package livecricket.livecrickettv.cricketstreaming.secuirty

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Multi-Stage Secure Payload Engine with Hybrid Auto-Detection.
 *
 * Decodes both:
 * 1. Hybrid field-level encrypted strings starting with "enc:"
 * 2. Entire encrypted response bodies
 * 3. Plain text strings (returns them as-is with zero errors)
 */
object SecurePayloadEngine {

    private const val PREFIX = "enc:"
    private const val AES_KEY = "WT1sdkEvUlR4ckd2"        // 16-byte key (128-bit)
    private const val AES_IV  = "Q7sKcm9LR4VaX2pN"        // 16-byte IV
    private const val SUFFIX_SALT = "abcdefghijklmnop"     // 16-char salt

    /**
     * Decodes a hybrid payload string.
     * If the string starts with "enc:", it strips the prefix and runs the full 10-step decryption pipeline.
     * If the string is plain text or null/blank, it returns it as-is (Hybrid fallback).
     */
    fun decodePayload(input: String?): String? {
        if (input.isNullOrBlank()) return input

        val trimmed = input.trim()

        // 1. Check for hybrid prefix
        val cipherPayload = if (trimmed.startsWith(PREFIX)) {
            trimmed.substring(PREFIX.length)
        } else {
            // Check if it's a plain JSON or URL (e.g. http://, https://, {, [)
            if (isLikelyPlainText(trimmed)) {
                return input
            }
            trimmed
        }

        return try {
            executeMultiStageDecode(cipherPayload)
        } catch (e: Exception) {
            // Safe hybrid fallback: if decryption fails, return raw input so nothing crashes
            input
        }
    }

    /**
     * Core 10-step decryption pipeline matching the server's multi-stage encoder.
     */
    private fun executeMultiStageDecode(cipherPayload: String): String {
        // Step 1: Base64 Decode
        val step1 = String(Base64.decode(cipherPayload, Base64.DEFAULT), StandardCharsets.UTF_8)

        // Step 2: Pair Swap
        val step2 = swapPairs(step1.toCharArray())

        // Step 3: Reverse
        val step3 = String(step2).reversed()

        // Step 4: Verify and Strip Suffix Salt
        val step4 = if (step3.endsWith(SUFFIX_SALT)) {
            step3.substring(0, step3.length - SUFFIX_SALT.length)
        } else {
            step3
        }

        // Step 5: Base64 Decode to raw ciphertext bytes
        val step5Bytes = Base64.decode(step4, Base64.DEFAULT)

        // Step 6: AES-128-CBC Decrypt
        val step6DecryptedBytes = decryptAesCbc(step5Bytes, AES_KEY, AES_IV)
        val step6Str = String(step6DecryptedBytes, StandardCharsets.UTF_8)

        // Step 7: Second Pair Swap
        val step7 = swapPairs(step6Str.toCharArray())

        // Step 8: Second Reverse
        val step8 = String(step7).reversed()

        // Step 9 & 10: Clean Base64 & Final Decode to UTF-8 Plain Text
        val cleanB64 = cleanBase64(step8)
        val finalBytes = Base64.decode(cleanB64, Base64.DEFAULT)

        return String(finalBytes, StandardCharsets.UTF_8)
    }

    private fun isLikelyPlainText(s: String): Boolean {
        return s.startsWith("http://") ||
                s.startsWith("https://") ||
                s.startsWith("{") ||
                s.startsWith("[") ||
                s.startsWith("rtmp://") ||
                s.startsWith("content://") ||
                s.startsWith("file://")
    }

    private fun swapPairs(chars: CharArray): CharArray {
        var i = 0
        while (i < chars.size - 1) {
            val temp = chars[i]
            chars[i] = chars[i + 1]
            chars[i + 1] = temp
            i += 2
        }
        return chars
    }

    private fun decryptAesCbc(ciphertext: ByteArray, key: String, iv: String): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray(StandardCharsets.UTF_8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(ciphertext)
    }

    private fun cleanBase64(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '+' || c == '/' || c == '=') {
                sb.append(c)
            }
        }
        while (sb.length % 4 != 0) {
            sb.append('=')
        }
        return sb.toString()
    }
}