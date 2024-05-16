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

package com.password.monitor.appmanager

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.password.monitor.api.ApiManager.Companion.apiBuilder
import com.password.monitor.models.MultiPwdItem
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.MATERIAL_YOU
import com.password.monitor.preferences.PreferenceManager.Companion.THEME
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.UiUtils.Companion.setAppTheme

class ApplicationManager : Application() {
    
    val preferenceManager by lazy {
        PreferenceManager(this)
    }
    private val apiService by lazy { apiBuilder() }
    val apiRepository by lazy { ApiRepository(apiService) }
    
    var multiPasswordsList = mutableListOf<MultiPwdItem>()
    
    override fun onCreate() {
        super.onCreate()
        
        // Theme
        setAppTheme(preferenceManager.getInt(THEME))
        
        // Material you
        if (preferenceManager.getBoolean(MATERIAL_YOU, defValue = false)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        
    }
    
}