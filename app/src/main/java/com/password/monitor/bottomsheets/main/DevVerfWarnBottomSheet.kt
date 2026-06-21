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

package com.password.monitor.bottomsheets.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.databinding.BottomSheetDevVerfWarnBinding
import com.password.monitor.databinding.BottomSheetFooterBinding
import com.password.monitor.databinding.BottomSheetHeaderBinding
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.SHOW_DEV_VERF_WARNING
import com.password.monitor.utils.IntentUtils.Companion.openURL
import org.koin.android.ext.android.get

class DevVerfWarnBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetDevVerfWarnBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetDevVerfWarnBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val mainActivity = requireActivity() as MainActivity
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        // Details
        bottomSheetBinding.warnDetailsCard.setOnClickListener {
            openURL(mainActivity, getString(R.string.dev_verf_warn_details_url))
        }
        
        // Solutions
        bottomSheetBinding.warnSolutionsCard.setOnClickListener {
            openURL(mainActivity, getString(R.string.dev_verf_warn_solutions_url))
        }
        
        // Don't show again
        bottomSheetBinding.warnHideCheckbox.setOnCheckedChangeListener { _, isChecked ->
            get<PreferenceManager>().setBoolean(SHOW_DEV_VERF_WARNING, !isChecked)
        }
        
        footerBinding.positiveButton.isVisible = false
        
        // Dismiss
        footerBinding.negativeButton.apply {
            text = getString(R.string.dismiss)
            setOnClickListener {
                dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}