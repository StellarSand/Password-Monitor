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

package com.password.monitor.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.password.monitor.R
import com.password.monitor.appmanager.ApplicationManager
import com.password.monitor.databinding.ActivityDetailsBinding
import com.password.monitor.fragments.details.DetailsFragment
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.utils.UiUtils.Companion.blockScreenshots
import com.password.monitor.utils.UiUtils.Companion.setNavBarContrastEnforced

class DetailsActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivityDetailsBinding
    lateinit var passwordLine: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setNavBarContrastEnforced(window)
        super.onCreate(savedInstanceState)
        activityBinding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        passwordLine = intent.getStringExtra("PwdLine")!!
        
        // Disable screenshots and screen recordings
        blockScreenshots(this,
                                (applicationContext as ApplicationManager).preferenceManager.getBoolean(PreferenceManager.BLOCK_SS))
        
        activityBinding.detailsToolbar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        supportActionBar?.title = getString(R.string.details)
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_host_fragment, DetailsFragment())
            .commitNow()
        
    }
    
}