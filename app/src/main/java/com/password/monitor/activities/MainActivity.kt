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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.password.monitor.appmanager.ApplicationManager
import com.password.monitor.preferences.PreferenceManager.Companion.BLOCK_SS
import com.password.monitor.R
import com.password.monitor.databinding.ActivityMainBinding
import com.password.monitor.utils.UiUtils.Companion.setNavBarContrastEnforced

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var selectedItem = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setNavBarContrastEnforced(window)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        // Disable screenshots and screen recordings
        if ((applicationContext as ApplicationManager).preferenceManager.getBoolean(BLOCK_SS)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE)
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        
        selectedItem = savedInstanceState?.getInt("selectedItem") ?: R.id.nav_scan
        
        // Bottom nav
        activityBinding.mainBottomNav.apply {
            menu.findItem(selectedItemId).isChecked = true
            
            setOnItemSelectedListener { item ->
                selectedItem = item.itemId
                displayFragment(selectedItem)
                true
            }
            
            setOnItemReselectedListener {}
        }
        
    }
    
    // Setup fragments
    private fun displayFragment(clickedNavItem: Int) {
        val currentFragment = navController.currentDestination!!
        
        val navActionsMap =
            mapOf(Pair(R.id.settingsFragment, R.id.nav_scan) to R.id.action_settingsFragment_to_scanFragment,
                  Pair(R.id.scanFragment, R.id.nav_settings) to R.id.action_scanFragment_to_settingsFragment)
        
        val action = navActionsMap[Pair(currentFragment.id, clickedNavItem)] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedNavItem != currentFragment.id && action != 0) {
            activityBinding.mainBottomNav.menu.findItem(clickedNavItem).isChecked = true
            navController.navigate(action)
        }
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.currentDestination!!.id != navController.graph.startDestinationId) {
                selectedItem = R.id.nav_scan
                displayFragment(selectedItem)
            }
            else {
                finish()
            }
        }
    }
    
}