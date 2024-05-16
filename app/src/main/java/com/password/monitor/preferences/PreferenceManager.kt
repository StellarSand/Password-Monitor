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

package com.password.monitor.preferences

import android.content.Context

class PreferenceManager(context: Context) {

    companion object {
        // Shared pref keys
        const val THEME = "theme"
        const val GRID_VIEW = "grid_view"
        const val SORT_ASC = "sort_asc"
        const val MATERIAL_YOU = "material_you"
        const val BLOCK_SS = "block_ss"
        const val INCOG_KEYBOARD = "incog_keyboard"
    }
    
    private val sharedPreferences =
        context.getSharedPreferences("com.password.monitor_preferences", Context.MODE_PRIVATE)

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }
    
    fun getBoolean(key: String, defValue: Boolean = true): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }
    
    fun setBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

}