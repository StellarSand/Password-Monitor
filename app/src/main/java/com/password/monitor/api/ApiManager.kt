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

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiManager {
    
    companion object {
        
        private const val API_BASE_URL = "https://api.pwnedpasswords.com/range/"
        
        fun apiBuilder(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
