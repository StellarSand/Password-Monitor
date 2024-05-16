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

package com.password.monitor.fragments.bottomsheets

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.password.monitor.R
import com.password.monitor.appmanager.ApplicationManager
import com.password.monitor.databinding.BottomSheetFooterBinding
import com.password.monitor.databinding.BottomSheetHeaderBinding
import com.password.monitor.databinding.BottomSheetThemeBinding
import com.password.monitor.preferences.PreferenceManager.Companion.THEME
import com.password.monitor.utils.UiUtils.Companion.setAppTheme

class ThemeBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetThemeBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetThemeBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = (requireContext().applicationContext as ApplicationManager).preferenceManager
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.theme)
        
        // Show "follow system" option only on SDK 29 and above
        bottomSheetBinding.followSystem.isVisible = Build.VERSION.SDK_INT >= 29
        
        // Radio group
        bottomSheetBinding.themeChipGroup.apply {
        
            // Default checked chip
            if (preferenceManager.getInt(THEME) == 0) {
                if (Build.VERSION.SDK_INT >= 29) {
                    preferenceManager.setInt(THEME, R.id.followSystem)
                }
                else {
                    preferenceManager.setInt(THEME, R.id.light)
                }
            }
            check(preferenceManager.getInt(THEME))
        
            // On selecting option
            setOnCheckedStateChangeListener { _, checkedIds ->
                val checkedChip = checkedIds.first()
                preferenceManager.setInt(THEME, checkedChip)
                setAppTheme(checkedChip)
                dismiss()
            
            }
        }
        
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).apply {
            positiveButton.isVisible = false
            negativeButton.setOnClickListener { dismiss() }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}