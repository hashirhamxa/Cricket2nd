package livecricket.livecrickettv.cricketstreaming.secuirty


import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Transparent OkHttp Interceptor that detects encrypted response bodies
 * and unscrambles them in RAM before passing clean JSON to Retrofit & Gson.
 */
class CryptoResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful || response.body == null) {
            return response
        }

        val rawBodyString = response.body?.string() ?: return response

        // Hybrid check: If already valid plain JSON, pass through directly
        val trimmed = rawBodyString.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            val originalBody = rawBodyString.toResponseBody(response.body?.contentType())
            return response.newBuilder().body(originalBody).build()
        }

        // Entire response body is encrypted: decrypt in RAM
        val decryptedJson = SecurePayloadEngine.decodePayload(trimmed) ?: rawBodyString
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val newBody = decryptedJson.toResponseBody(mediaType)

        return response.newBuilder()
            .body(newBody)
            .build()
    }
}