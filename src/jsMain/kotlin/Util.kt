import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit


suspend inline fun <reified R, reified T>postJsonData(address: String, postData: T, token: String? = null): Result<R>{
    val response = window.fetch(
        address,
        RequestInit(
            method = "post",
            body = Json.encodeToString(postData),
            headers = Headers().apply {
                append("content-type","application/json")
                token?.let {
                    append("Authorization", "Bearer $it")
                }
            }
        )
    ).await()
    if(response.ok){
        return runCatching {
            Json.decodeFromString(response.text().await())
        }
    }
    return Result.failure(Exception("failed"))
}