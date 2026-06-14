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
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.password.monitor.R
import com.password.monitor.bottomsheets.ExceptionErrorBottomSheet
import com.password.monitor.bottomsheets.NoNetworkBottomSheet
import com.password.monitor.databinding.ActivityMultiPwdBinding
import com.password.monitor.objects.MultiPwdList
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.GRID_VIEW
import com.password.monitor.preferences.PreferenceManager.Companion.SORT_ASC
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.HashUtils.Companion.getHashCount
import com.password.monitor.utils.HashUtils.Companion.getHashPrefixAndSuffix
import com.password.monitor.utils.NetworkUtils.Companion.hasInternet
import com.password.monitor.utils.UiUtils.Companion.blockScreenshots
import com.password.monitor.utils.UiUtils.Companion.setNavBarContrastEnforced
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MultiPwdActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivityMultiPwdBinding
    private lateinit var navController: NavController
    private val prefManager by inject<PreferenceManager>()
    var isGridView = false
    var isAscSort = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.apply {
            setNavBarContrastEnforced()
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMultiPwdBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.multi_pwd_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        isGridView = prefManager.getBoolean(GRID_VIEW, defValue = false)
        isAscSort = prefManager.getBoolean(SORT_ASC)
        
        // Disable screenshots and screen recordings
        window.blockScreenshots(prefManager.getBoolean(PreferenceManager.BLOCK_SS))
        
        // Back
        activityBinding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // View
        activityBinding.viewButton.apply {
            isEnabled = false
            setViewButtonIcon()
            setOnClickListener {
                isGridView = !isGridView
                setViewButtonIcon()
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
        }
        
        // Sort
        activityBinding.sortButton.apply {
            isEnabled = false
            setOnClickListener {
                isAscSort = !isAscSort
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
        }
        
        scanAllPasswords()
    }
    
    private fun scanAllPasswords() {
        lifecycleScope.launch {
            val listSize = MultiPwdList.pwdList.size
            activityBinding.scanningText.text = getString(R.string.scanning_passwords, "$listSize")
            
            if (hasInternet(this@MultiPwdActivity)) {
                try {
                    val maxThreads = 12
                    var completedItems = 0
                    
                    // Scan all passwords in batches of 12 (in parallel)
                    (0 until listSize step maxThreads).forEach {
                        val lastIndexInBatch = (it + maxThreads).coerceAtMost(listSize)
                        
                        (it until lastIndexInBatch).map { index ->
                            async {
                                val (prefix, suffix) = getHashPrefixAndSuffix(MultiPwdList.pwdList[index].password)
                                getHashCount(get<ApiRepository>().getHashes(prefix), suffix)
                                    .let { hashCount ->
                                        if (hashCount > 0) {
                                            MultiPwdList.pwdList[index].apply {
                                                breachedCount = hashCount
                                                isBreached = true
                                            }
                                        }
                                    }
                            }
                        }.awaitAll()
                        
                        val itemsInBatch = lastIndexInBatch - it
                        completedItems += itemsInBatch
                        activityBinding.multiScanProgressIndicator.setProgress(
                            (completedItems * 100) / listSize,
                            true
                        )
                    }
                    
                    afterScanComplete()
                }
                catch (e: Exception) {
                    ExceptionErrorBottomSheet(
                        exception = e,
                        onPositiveBtnClick = { scanAllPasswords() },
                        onNegativeBtnClick = { finishAfterTransition() }
                    ).show(supportFragmentManager, "ExceptionErrorBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(
                    onPositiveBtnClick = { scanAllPasswords() },
                    onNegativeBtnClick = { finishAfterTransition() }
                ).show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private fun afterScanComplete() {
        activityBinding.multiScanProgressIndicator.hide()
        activityBinding.scanningLayout.isVisible = false
        
        navController.navInflater.inflate(R.navigation.multi_pwd_fragments_nav_graph).apply {
            navController.setGraph(this, intent.extras)
        }
        
        activityBinding.multiPwdNavHost.isVisible = true
        activityBinding.viewButton.isEnabled = true
        activityBinding.sortButton.isEnabled = true
    }
    
    private fun MaterialButton.setViewButtonIcon() {
        icon = ContextCompat.getDrawable(this@MultiPwdActivity,
                                         if (!isGridView) R.drawable.ic_view_grid
                                         else R.drawable.ic_view_list)
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAfterTransition()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        prefManager.apply {
            setBoolean(GRID_VIEW, isGridView)
            setBoolean(SORT_ASC, isAscSort)
        }
        MultiPwdList.pwdList.clear()
    }
}