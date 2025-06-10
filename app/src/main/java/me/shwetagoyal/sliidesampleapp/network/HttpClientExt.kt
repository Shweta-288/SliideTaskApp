package me.shwetagoyal.sliidesampleapp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(route)
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf()
): Result<Response, DataError.Network> {
    return safeCall {
        delete {
            url(route)
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request
): Result<Response, DataError.Network> {
    return safeCall {
        post {
            url(route)
            setBody(body)
        }
    }
}

suspend fun HttpClient.getLastPageNumberFromHeader(
    route: String,
    pageParam: String = "page",
    headerKey: String = "X-Pagination-Pages"
): Result<Int, DataError.Network> {
    val response = try {
        get {
            url(route)
            parameter(pageParam, 1)
        }
    } catch (e: Exception) {
        return handleNetworkException(e)
    }

    val lastPage = response.headers[headerKey]?.toIntOrNull() ?: 1
    return Result.Success(lastPage)
}

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.NO_INTERNET)
    }  catch (e: Exception) {
        if (e is CancellationException) throw e
        e.printStackTrace()
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Network> {
    return when (response.status.value) {
        204 -> handleNoContent<T>()
        in 200..299 -> Result.Success(response.body())
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        422 -> Result.Error(DataError.Network.CONFLICT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}

inline fun <reified T> handleNoContent(): Result<T, DataError.Network> {
    return if (T::class == Unit::class) {
        Result.Success(Unit as T)
    } else {
        Result.Error(DataError.Network.UNKNOWN)
    }
}

fun handleNetworkException(e: Exception): Result.Error<DataError.Network> {
    return when (e) {
        is UnresolvedAddressException -> Result.Error(DataError.Network.NO_INTERNET)
        is CancellationException -> throw e
        else -> {
            e.printStackTrace()
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}