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
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec

class ApiManager {
    
    companion object {
        
        private val httpClient =
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                        connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS, ConnectionSpec.MODERN_TLS))
                        certificatePinner(
                            CertificatePinner.Builder()
                                .add("api.pwnedpasswords.com","sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=") // GTS Root R4
                                .build()
                        )
                    }
                }
            }
        
        fun apiBuilder(): ApiService {
            return ApiService(httpClient)
        }
        
    }
}
