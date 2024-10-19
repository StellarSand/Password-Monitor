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

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.password.monitor.R

class UiUtils {
    
    companion object {
        
        fun setAppTheme(selectedTheme: Int) {
            when(selectedTheme) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= 29){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                R.id.followSystem -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                R.id.light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        
        fun setNavBarContrastEnforced(window: Window) {
            if (Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = false
            }
        }
        
        fun convertDpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
        
        fun setFoundInBreachSubtitleText(context: Context,
                                         isFound: Boolean = false,
                                         reset: Boolean = false,
                                         textView: MaterialTextView) {
            val (displayText, icon) =
                if (isFound) {
                    Pair("${context.getString(R.string.yes)} \t", R.drawable.ic_found_in_breach)
                }
                else if (reset) {
                    Pair(context.getString(R.string.na), 0)
                }
                else {
                    Pair("${context.getString(R.string.no)} \t", R.drawable.ic_not_found_in_breach)
                }
            textView.apply {
                text = displayText
                if (reset) {
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                else {
                    setCompoundDrawablesWithIntrinsicBounds(null,
                                                            null,
                                                            ContextCompat.getDrawable(context, icon),
                                                            null)
                }
            }
        }
        
        fun blockScreenshots(activity: Activity,
                             shouldBlock: Boolean) {
            if (shouldBlock) {
                activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                         WindowManager.LayoutParams.FLAG_SECURE)
            }
            else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        
        fun showSnackbar(coordinatorLayout: CoordinatorLayout,
                         message: String,
                         anchorView: View?) {
            Snackbar.make(coordinatorLayout,
                          message,
                          BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
    }
}