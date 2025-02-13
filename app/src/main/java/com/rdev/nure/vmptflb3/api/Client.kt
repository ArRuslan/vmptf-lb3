package com.rdev.nure.vmptflb3.api

import com.rdev.nure.vmptflb3.api.responses.ErrorResponse
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private var client: Retrofit? = null

fun getApiClient(): Retrofit {
    if (client == null)
        client = Retrofit.Builder()
            .baseUrl("http://192.168.0.111:3000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    return client!!
}

fun <T> Response<T>.getErrorResponse(): ErrorResponse? {
    if (isSuccessful)
        return null

    val errors = errorBody()?.string()?.let {
        val jsonErrors = JSONObject(it).getJSONArray("errors")
        List(jsonErrors.length()) { errorIt ->
            jsonErrors.getString(errorIt)
        }
    } ?: run {
        listOf("Unknown error")
    }

    return ErrorResponse(errors)
}

suspend fun <T> handleResponse(
    successResponse: (body: T) -> Unit,
    errorResponse: (err: ErrorResponse) -> Unit,
    onHttpError: () -> Unit,
    onNetworkError: () -> Unit,
    body: suspend () -> Response<T>,
): Boolean {
    try {
        val resp = body()
        val respBody = resp.body()
        if(respBody == null) {
            val errResp = resp.getErrorResponse()?.let {
                if(it.errors.isEmpty())
                    ErrorResponse(listOf("Unknown error"))
                else
                    it
            } ?: run {
                ErrorResponse(listOf("Unknown error"))
            }

            errorResponse(errResp)
            return false
        }

        successResponse(respBody)
        return true
    } catch (e: HttpException) {
        onHttpError()
    } catch (e: IOException) {
        onNetworkError()
    }

    return false
}