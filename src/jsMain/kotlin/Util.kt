import com.n0n5ense.model.json.AccessToken
import com.n0n5ense.model.json.RefreshToken
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.EventTarget
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

interface ValueInterface {
    var value: String
}

val EventTarget.value: String
    get() {
        return this.unsafeCast<ValueInterface>().value
    }

class NetException(val code: Int): Exception()

data class DataWithAccessToken<R> (
    val accessToken: String,
    val data: R
)

suspend inline fun <reified R, reified T>postJsonData(address: String, postData: T, accessToken: String? = null): Result<R>{
    val response = window.fetch(
        address,
        RequestInit(
            method = "post",
            body = Json.encodeToString(postData),
            headers = Headers().apply {
                append("content-type","application/json")
                accessToken?.let {
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
    return Result.failure(NetException(response.status.toInt()))
}

suspend inline fun <reified R>getJsonData(address: String, accessToken: String? = null): Result<R>{
    val response = window.fetch(
        address,
        RequestInit(
            method = "get",
            headers = Headers().apply {
                append("content-type","application/json")
                accessToken?.let {
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
    return Result.failure(NetException(response.status.toInt()))
}

suspend inline fun <reified R, reified T>postJsonDataWithTokenRetry(address: String, postData: T, authUser: AuthUser): Result<DataWithAccessToken<R>>{
    val throwable = postJsonData<R, T>(address, postData, authUser.accessToken).fold(
        onSuccess = { return Result.success(DataWithAccessToken(authUser.accessToken?:"", it)) },
        onFailure = { it }
    )
    if(throwable !is NetException)
        return Result.failure(throwable)
    if(throwable.code != 401)
        return Result.failure(throwable)
    return postJsonDataWithGetToken(address, postData, authUser.refreshToken)
}

suspend inline fun <reified R>getJsonDataWithTokenRetry(address: String, authUser: AuthUser): Result<DataWithAccessToken<R>>{
    val throwable = getJsonData<R>(address, authUser.accessToken).fold(
        onSuccess = { return Result.success(DataWithAccessToken(authUser.accessToken?:"", it)) },
        onFailure = { it }
    )
    if(throwable !is NetException)
        return Result.failure(throwable)
    if(throwable.code != 401)
        return Result.failure(throwable)
    return getJsonDataWithGetToken(address, authUser.refreshToken)
}

suspend inline fun <reified R, reified T>postJsonDataWithGetToken(address: String, postData: T, refreshToken: String): Result<DataWithAccessToken<R>> {
    val newToken = getAccessToken(refreshToken).fold(
        onSuccess = { it },
        onFailure = { return Result.failure(it) }
    )
    return postJsonData<R, T>(address, postData, newToken).map { DataWithAccessToken(newToken, it) }
}

suspend inline fun <reified R>getJsonDataWithGetToken(address: String, refreshToken: String): Result<DataWithAccessToken<R>> {
    val newToken = getAccessToken(refreshToken).fold(
        onSuccess = { it },
        onFailure = { return Result.failure(it) }
    )
    return getJsonData<R>(address, newToken).map { DataWithAccessToken(newToken, it) }
}

suspend fun getAccessToken(refreshToken: String): Result<String> {
    return postJsonData<AccessToken, RefreshToken>("$serverAddress/api/token", RefreshToken(refreshToken)).map { it.accessToken }
}