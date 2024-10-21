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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.password.monitor.R
import com.password.monitor.activities.MultiPwdActivity
import com.password.monitor.databinding.BottomSheetFooterBinding
import com.password.monitor.databinding.BottomSheetHeaderBinding
import com.password.monitor.databinding.BottomSheetScanMultiPwdBinding
import com.password.monitor.models.MultiPwdItem
import com.password.monitor.objects.MultiPwdList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class ScanMultiPwdBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetScanMultiPwdBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetScanMultiPwdBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.scan_multi_pwds)
        
        // Add
        bottomSheetBinding.addMultiple.setOnClickListener {
            dismiss()
            AddMultiPwdBottomSheet().show(parentFragmentManager, "AddMultiplePwdBottomSheet")
        }
        
        // Select file
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
        bottomSheetBinding.selectFile.setOnClickListener {
            filePicker.launch(intent)
        }
        
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).negativeButton.setOnClickListener { dismiss() }
    }
    
    private var filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data!!
                val fileUri = data.data
                
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val inputStream = requireActivity().contentResolver.openInputStream(fileUri!!)
                        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                        var lineList: List<String>
                        
                        MultiPwdList.pwdList.apply {
                            if (isNotEmpty()) clear()
                            // Read file line by line
                            while (bufferedReader.readLine().also { lineList = listOf(it) } != null) {
                                for (line in lineList) {
                                    if (line.isNotEmpty()) add(MultiPwdItem(passwordLine = line))
                                }
                            }
                        }
                        
                        inputStream!!.close()
                        bufferedReader.close()
                    }
                    catch (fileNotFoundException: FileNotFoundException) {
                        fileNotFoundException.printStackTrace()
                        withContext(Dispatchers.Main) {
                            /*UiUtils.showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                                 "File not found",
                                                 fragmentBinding.selectFab)*/
                        }
                    }
                    catch (ioException: IOException) {
                        ioException.printStackTrace()
                        withContext(Dispatchers.Main) {
                            /*UiUtils.showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                                 "Error reading file",
                                                 fragmentBinding.selectFab)*/
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        dismiss()
                        startActivity(Intent(requireActivity(), MultiPwdActivity::class.java))
                    }
                }
            }
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}