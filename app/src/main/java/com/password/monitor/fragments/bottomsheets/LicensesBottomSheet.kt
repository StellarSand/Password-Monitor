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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.adapters.LicenseItemAdapter
import com.password.monitor.databinding.BottomSheetFooterBinding
import com.password.monitor.databinding.BottomSheetHeaderBinding
import com.password.monitor.databinding.BottomSheetLicensesBinding
import com.password.monitor.models.License

class LicensesBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetLicensesBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var licenseList: ArrayList<License>
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetLicensesBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.third_party_licenses)
    
        licenseList = ArrayList<License>().apply {
            
            // Retrofit
            add(License(getString(R.string.retrofit),
                        "${getString(R.string.copyright_square)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.retrofit_license_url)))
            
            // Fastscroll
            add(License(title = getString(R.string.fastscroll),
                        desc = "${getString(R.string.copyright_fastscroll)}\n\n${getString(R.string.apache_2_0_license)}",
                        url = getString(R.string.fastscroll_license_url)))
            
            // Liberapay
            add(License(title = getString(R.string.liberapay_icon),
                        desc = getString(R.string.cc0_1_0_universal_public_domain_license),
                        url = getString(R.string.liberapay_icon_license_url)))
            
            // PayPal
            add(License(title = getString(R.string.paypal_icon),
                        desc = "",
                        url = getString(R.string.paypal_icon_license_url)))
            
            // Ko-fi
            add(License(title = getString(R.string.kofi_icon),
                        desc = "",
                        url = getString(R.string.kofi_icon_license_url)))
        }
    
        bottomSheetBinding.licensesRecyclerView.adapter = LicenseItemAdapter(licenseList,
                                                                             requireActivity() as MainActivity)
        
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