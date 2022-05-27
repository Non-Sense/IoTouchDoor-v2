package util

import AuthUser
import com.n0n5ense.model.json.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import serverAddress

fun getPhysicalLog(
    page: Int,
    width: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<List<DoorLog>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/door/log?p=$page&w=$width", authUser))
    }
}

fun getPhysicalLogCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/door/logcount", authUser))
    }
}


fun getTouchLog(
    page: Int,
    width: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<List<CardTouchLog>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/log?p=$page&w=$width", authUser))
    }
}

fun getTouchLogCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/logcount", authUser))
    }
}



fun getCards(
    page: Int,
    width: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<List<TouchCard>>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card?p=$page&w=$width", authUser))
    }
}

fun getCardCount(authUser: AuthUser, callback: (Result<DataWithAccessToken<Count>>) -> Unit) {
    MainScope().launch {
        callback.invoke(getJsonDataWithTokenRetry("$serverAddress/api/card/count", authUser))
    }
}

fun addCard(
    card: NewTouchCard,
    authUser: AuthUser,
    callback: (Result<DataWithAccessToken<Unit>>) -> Unit
) {
    MainScope().launch {
        callback.invoke(postJsonDataWithTokenRetry("$serverAddress/api/card", authUser, card))
    }
}

fun putCard(
    cardId: Int,
    card: EditTouchCard,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<Unit>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(putJsonDataWithTokenRetry("$serverAddress/api/card/$cardId", authUser, card))
    }
}

fun deleteCard(
    cardId: Int,
    authUser: AuthUser,
    callback: ((Result<DataWithAccessToken<Unit>>) -> Unit)
) {
    MainScope().launch {
        callback.invoke(deleteWithTokenRetry("$serverAddress/api/card/$cardId", authUser))
    }
}

enum class RequestMethod {
    GET,
    POST,
    PUT,
    DELETE
}

class NetException(val code: Int): Exception()

data class DataWithAccessToken<R>(
    val accessToken: String,
    val data: R
)

suspend inline fun <reified R, reified T> postJsonData(address: String, postData: T, accessToken: String? = null) =
    request<R, T>(RequestMethod.POST, address, accessToken, postData)

suspend inline fun <reified R> getJsonDataWithTokenRetry(address: String, authUser: AuthUser) =
    requestWithTokenRetry<R, Unit>(RequestMethod.GET, address, authUser)

suspend inline fun <reified R> deleteWithTokenRetry(address: String, authUser: AuthUser) =
    requestWithTokenRetry<R, Unit>(RequestMethod.DELETE, address, authUser)

suspend inline fun <reified R, reified T> postJsonDataWithTokenRetry(address: String, authUser: AuthUser, postData: T) =
    requestWithTokenRetry<R, T>(RequestMethod.POST, address, authUser, postData)

suspend inline fun <reified R, reified T> putJsonDataWithTokenRetry(address: String, authUser: AuthUser, postData: T) =
    requestWithTokenRetry<R, T>(RequestMethod.PUT, address, authUser, postData)

suspend inline fun <reified R, reified T> request(
    method: RequestMethod,
    address: String,
    accessToken: String?,
    sendData: T? = null
): Result<R> {
    val init = RequestInit(
        method = method.name,
        body = sendData?.let { Json.encodeToString(it) } ?: undefined,
        headers = Headers().apply {
            if(method == RequestMethod.POST || method == RequestMethod.PUT)
                append("content-type", "application/json")
            accessToken?.let {
                append("Authorization", "Bearer $it")
            }
        }
    )
    val response = window.fetch(
        address,
        init
    ).await()
    if(response.ok) {
        if(Unit is R)
            return Result.success(Unit)
        return runCatching {
            Json.decodeFromString(response.text().await())
        }
    }
    return Result.failure(NetException(response.status.toInt()))
}

suspend inline fun <reified R, reified T> requestWithTokenRetry(
    method: RequestMethod,
    address: String,
    authUser: AuthUser,
    sendData: T? = null
): Result<DataWithAccessToken<R>> {
    val throwable = request<R, T>(method, address, authUser.accessToken, sendData).fold(
        onSuccess = { return Result.success(DataWithAccessToken(authUser.accessToken ?: "", it)) },
        onFailure = { it }
    )
    if(throwable !is NetException)
        return Result.failure(throwable)
    if(throwable.code != 401)
        return Result.failure(throwable)
    return requestWithGetToken<R, T>(method, address, authUser.refreshToken, sendData)
}

suspend inline fun <reified R, reified T> requestWithGetToken(
    method: RequestMethod,
    address: String,
    refreshToken: String,
    sendData: T? = null
): Result<DataWithAccessToken<R>> {
    val newToken = getAccessToken(refreshToken).fold(
        onSuccess = { it },
        onFailure = { return Result.failure(it) }
    )
    return request<R, T>(method, address, newToken, sendData)
        .map { DataWithAccessToken(newToken, it) }
}

suspend inline fun getAccessToken(refreshToken: String): Result<String> {
    return postJsonData<AccessToken, RefreshToken>(
        "$serverAddress/api/token",
        RefreshToken(refreshToken)
    ).map { it.accessToken }
}