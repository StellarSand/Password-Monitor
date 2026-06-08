/*
 *     Copyright (C) 2024-present StellarSand
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.password.monitor.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.UserAgent
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import java.util.concurrent.TimeUnit

class ApiManager {
    
    companion object {
        
        private val httpClient =
            HttpClient(OkHttp) {
                engine {
                    config {
                        connectTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        readTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        writeTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        followRedirects(true)
                        followSslRedirects(true)
                        connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS, ConnectionSpec.MODERN_TLS))
                        certificatePinner(
                            CertificatePinner.Builder()
                                .add("api.pwnedpasswords.com","sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=") // GTS Root R4
                                .build()
                        )
                    }
                }
                install(UserAgent) {
                    agent = "com.password.monitor (https://github.com/StellarSand/Password-Monitor)"
                }
                install(HttpCallValidator) {
                    validateResponse { response ->
                        if (!response.status.isSuccess()) {
                            throw IOException("\n\nHTTP ${response.status.value} - ${getStatusMessage(response.status.value)}")
                        }
                    }
                }
            }
        
        private fun getStatusMessage(statusCode: Int): String {
            return when (statusCode) {
                400 -> "Bad Request"
                401 -> "Unauthorized"
                403 -> "Forbidden"
                404 -> "Not Found"
                500 -> "Internal Server Error"
                502 -> "Bad Gateway"
                503 -> "Service Unavailable"
                else -> "HTTP Error"
            }
        }
        
        fun apiBuilder(): ApiService {
            return ApiService(httpClient)
        }
        
    }
}
