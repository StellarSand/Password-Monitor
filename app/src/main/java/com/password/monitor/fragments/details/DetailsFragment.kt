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

package com.password.monitor.fragments.details

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.password.monitor.R
import com.password.monitor.activities.DetailsActivity
import com.password.monitor.bottomsheets.ExceptionErrorBottomSheet
import com.password.monitor.databinding.FragmentScanBinding
import com.password.monitor.bottomsheets.NoNetworkBottomSheet
import com.password.monitor.fragments.common.getFormattedResultsText
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.password.monitor.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.password.monitor.utils.FormatUtils.Companion.generateNewFilename
import com.password.monitor.utils.HashUtils.Companion.generateSHA1Hash
import com.password.monitor.utils.HashUtils.Companion.getHashCount
import com.password.monitor.utils.IntentUtils.Companion.openURL
import com.password.monitor.utils.IntentUtils.Companion.shareText
import com.password.monitor.utils.NetworkUtils.Companion.hasInternet
import com.password.monitor.utils.NetworkUtils.Companion.hasNetwork
import com.password.monitor.utils.UiUtils.Companion.convertDpToPx
import com.password.monitor.utils.UiUtils.Companion.setFoundInBreachSubtitleText
import com.password.monitor.utils.UiUtils.Companion.showSnackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import java.text.NumberFormat

class DetailsFragment : Fragment() {
    
    private var _binding: FragmentScanBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var detailsActivity: DetailsActivity
    private lateinit var passwordString: String
    private lateinit var naString: String
    private lateinit var breachedSuggestionString: String
    private lateinit var notBreachedSuggestionString: String
    private var hashPrefix = ""
    private var hashSuffix = ""
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        detailsActivity = (requireActivity() as DetailsActivity)
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        passwordString = detailsActivity.passwordLine
        naString = getString(R.string.na)
        breachedSuggestionString = getString(R.string.breached_suggestion)
        notBreachedSuggestionString = getString(R.string.not_breached_suggestion)
        
        fragmentBinding.apply {
            // Adjust UI components for edge to edge
            ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.passwordBox) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                            or WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(left = insets.left, right = insets.right)
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = insets.top + convertDpToPx(requireContext(), 12f)
                }
                WindowInsetsCompat.CONSUMED
            }
            ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scrollView) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                            or WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
                WindowInsetsCompat.CONSUMED
            }
            
            passwordText.apply {
                setText(passwordString)
                isFocusable = false
                isCursorVisible = false
            }
            checkBtn.isVisible = false
            progressIndicator.show()
            scanMultipleFab.isVisible = false
            
            passwordText.text.toString().generateSHA1Hash().apply {
                hashPrefix = take(5).uppercase() // First 5 chars
                hashSuffix = substring(5).uppercase() // Rest of the hash
            }
            
            checkPassword()
            
            // Copy
            fragmentBinding.copyChip.setOnClickListener {
                val clipData = ClipData.newPlainText("", fragmentBinding.getFormattedResultsText(requireContext()))
                clipData.hideSensitiveContent()
                clipboardManager.setPrimaryClip(clipData)
                // Show snackbar only if 12L or lower to avoid duplicate notifications
                // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                if (Build.VERSION.SDK_INT <= 32) {
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 requireContext().getString(R.string.copied_to_clipboard),
                                 fragmentBinding.scanMultipleFab)
                }
                scheduleClipboardClear(requireContext())
            }
            
            // Share
            fragmentBinding.shareChip.setOnClickListener {
                requireActivity().shareText(fragmentBinding.getFormattedResultsText(requireContext()))
            }
            
            // Export
            fragmentBinding.exportChip.setOnClickListener {
                exportToFilePicker.launch(generateNewFilename())
            }
            
            // Tap here
            tapHereBtn.apply {
                setOnClickListener {
                    openURL(detailsActivity,
                            getString(R.string.app_wiki_url),
                            detailsActivity.activityBinding.detailsCoordLayout,
                            fragmentBinding.scanMultipleFab)
                }
            }
        }
        
    }
    
    private fun displayResult(count: Int) {
        if (count > 0) {
            fragmentBinding.apply {
                foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(), isFound = true)
                timesFoundSubtitle.text = NumberFormat.getInstance().format(count)
                suggestionSubtitle.text = breachedSuggestionString
            }
        }
        else {
            fragmentBinding.apply {
                foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(), isFound = false)
                timesFoundSubtitle.text = naString
                suggestionSubtitle.text = notBreachedSuggestionString
            }
        }
        
        fragmentBinding.detailsCard.isVisible = true
    }
    
    private fun checkPassword() {
        lifecycleScope.launch {
            if (hasNetwork(requireContext()) && hasInternet()) {
                try {
                    val hashesResponse = get<ApiRepository>().getHashes(hashPrefix)
                    displayResult(getHashCount(hashesResponse, hashSuffix))
                }
                catch (e: Exception) {
                    // Handle other exceptions
                    ExceptionErrorBottomSheet(
                        exception = e,
                        onPositiveBtnClick = { checkPassword() },
                        onNegativeBtnClick = {
                            fragmentBinding.progressIndicator.hide()
                        }
                    ).show(parentFragmentManager, "ExceptionErrorBottomSheet")
                }
                fragmentBinding.progressIndicator.hide()
            }
            else {
                NoNetworkBottomSheet(
                    onPositiveBtnClick = { checkPassword() },
                    onNegativeBtnClick = {
                        fragmentBinding.progressIndicator.hide()
                    }
                ).show(parentFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private val exportToFilePicker =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            uri?.let {
                try {
                    requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(fragmentBinding.getFormattedResultsText(requireContext()).toByteArray())
                    }
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 getString(R.string.export_success),
                                 fragmentBinding.scanMultipleFab)
                }
                catch (_: Exception) {
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 getString(R.string.export_fail),
                                 fragmentBinding.scanMultipleFab)
                }
            }
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}