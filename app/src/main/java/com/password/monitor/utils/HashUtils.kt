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

import java.security.MessageDigest

class HashUtils {
    
    companion object {
        
        private const val ITEM_NOT_FOUND = -1
        
        private fun String.generateSHA1Hash(): String {
            val messageDigest = MessageDigest.getInstance("SHA-1")
            val bytes = messageDigest.digest(toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
        
        fun getHashPrefixAndSuffix(password: String): Pair<String, String> {
            val hash = password.generateSHA1Hash().uppercase()
            
            // Prefix = first 5 chars
            // Suffix = rest of the hash
            return hash.take(5) to hash.drop(5)
        }
        
        fun getHashCount(response: String?, suffix: String): Int {
            return response?.lines()
                       ?.find { it.substringBefore(":").endsWith(suffix) }
                       ?.substringAfter(":")?.toIntOrNull()
                   ?: ITEM_NOT_FOUND
        }
        
    }
}