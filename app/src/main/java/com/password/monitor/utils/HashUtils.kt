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

package com.password.monitor.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.security.MessageDigest

class HashUtils {
    
    companion object : KoinComponent {
        
        private const val ITEM_NOT_FOUND = -1
        
        suspend fun getHashPrefixAndSuffix(password: String): Pair<String, String> {
            return withContext(Dispatchers.Default) {
                get<MessageDigest>().digest(password.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                    .uppercase()
                    .let {
                        // Prefix = first 5 chars
                        // Suffix = rest of the hash
                        it.take(5) to it.drop(5)
                    }
            }
        }
        
        suspend fun getHashCount(response: String?, suffix: String): Int {
            return withContext(Dispatchers.Default) {
                response?.lines()
                    ?.find { it.substringBefore(":").endsWith(suffix) }
                    ?.substringAfter(":")?.toIntOrNull()
                ?: ITEM_NOT_FOUND
            }
        }
    }
}