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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.adapters.SupportMethodItemAdapter
import com.password.monitor.databinding.BottomSheetFooterBinding
import com.password.monitor.databinding.BottomSheetHeaderBinding
import com.password.monitor.databinding.BottomSheetSupportMethodsBinding
import com.password.monitor.models.SupportMethod

class SupportMethodsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetSupportMethodsBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var supportMethodsList: ArrayList<SupportMethod>
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetSupportMethodsBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.support)
        
        supportMethodsList = ArrayList<SupportMethod>().apply {
            
            // Liberapay
            add(SupportMethod(title = getString(R.string.liberapay),
                              titleIcon = R.drawable.ic_liberapay,
                              qr = R.drawable.ic_liberapay_qr,
                              url = getString(R.string.liberapay_url)))
            
            // PayPal
            add(SupportMethod(title = getString(R.string.paypal),
                              titleIcon = R.drawable.ic_paypal,
                              qr = R.drawable.ic_paypal_qr,
                              url = getString(R.string.paypal_url)))
            
            // Ko-fi
            add(SupportMethod(title = getString(R.string.kofi),
                              titleIcon = R.drawable.ic_kofi,
                              qr = R.drawable.ic_kofi_qr,
                              url = getString(R.string.kofi_url)))
            
        }
        
        bottomSheetBinding.licensesRecyclerView.adapter = SupportMethodItemAdapter(supportMethodsList,
                                                                                   requireActivity() as MainActivity)
        
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}