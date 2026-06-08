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

package com.password.monitor.fragments.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.password.monitor.R
import com.password.monitor.databinding.FragmentScanBinding
import com.password.monitor.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.password.monitor.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.password.monitor.utils.IntentUtils.Companion.openURL
import com.password.monitor.utils.IntentUtils.Companion.shareText
import com.password.monitor.utils.UiUtils.Companion.setFoundInBreachSubtitleText
import com.password.monitor.utils.UiUtils.Companion.showSnackbar
import org.koin.android.ext.android.get
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class BaseResultsFragment : Fragment() {
    
    private var _binding: FragmentScanBinding? = null
    protected val fragmentBinding get() = _binding!!
    protected lateinit var clipboardManager: ClipboardManager
    protected lateinit var naString: String
    private lateinit var breachedSuggestionString: String
    private lateinit var notBreachedSuggestionString: String
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        naString = getString(R.string.na)
        breachedSuggestionString = getString(R.string.breached_suggestion)
        notBreachedSuggestionString = getString(R.string.not_breached_suggestion)
        
        setupFragmentContent()
        
        // Copy
        fragmentBinding.copyChip.setOnClickListener {
            copyToClipboard(getFormattedResultsText())
            scheduleClipboardClear(requireContext())
        }
        
        // Share
        fragmentBinding.shareChip.setOnClickListener {
            requireActivity().shareText(getFormattedResultsText())
        }
        
        // Export
        fragmentBinding.exportChip.setOnClickListener {
            exportToFilePicker.launch(generateNewFilename())
        }
        
        // Tap here
        fragmentBinding.tapHereBtn.apply {
            setOnClickListener {
                openURL(requireActivity(),
                        getString(R.string.app_wiki_url),
                        getCoordinatorLayout(),
                        getSnackbarAnchorView())
            }
        }
    }
    
    // Subclasses must override
    protected abstract fun setupFragmentContent()
    
    protected open fun displayResults(breachedCount: Int) {
        if (breachedCount > 0) {
            fragmentBinding.apply {
                foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(), isFound = true)
                timesFoundSubtitle.text = get<NumberFormat>().format(breachedCount)
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
    }
    
    protected fun copyToClipboard(copiedText: CharSequence) {
        val clipData = ClipData.newPlainText("", copiedText)
        clipData.hideSensitiveContent()
        clipboardManager.setPrimaryClip(clipData)
        // Show snackbar only if 12L or lower to avoid duplicate notifications
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
        if (Build.VERSION.SDK_INT <= 32) {
            showSnackbar(
                getCoordinatorLayout(),
                requireContext().getString(R.string.copied_to_clipboard),
                getSnackbarAnchorView()
            )
        }
    }
    
    private fun getFormattedResultsText(): String {
        return buildString {
            append("# ${requireContext().getString(R.string.password)}\n")
            append("${fragmentBinding.passwordText.text.toString()}\n\n")
            append("## ${requireContext().getString(R.string.found_in_breach)}\n")
            append("${fragmentBinding.foundInBreachSubtitle.text}\n\n")
            append("## ${requireContext().getString(R.string.times_found)}\n")
            append("${fragmentBinding.timesFoundSubtitle.text}\n\n")
            append("## ${requireContext().getString(R.string.suggestion)}\n")
            append("${fragmentBinding.suggestionSubtitle.text}\n\n")
        }
    }
    
    private fun generateNewFilename(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
        return "PasswordMonitor_export_${timestamp}.txt"
    }
    
    private val exportToFilePicker =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            uri?.let {
                try {
                    requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(getFormattedResultsText().toByteArray())
                    }
                    showSnackbar(getCoordinatorLayout(),
                                 getString(R.string.export_success),
                                 getSnackbarAnchorView())
                }
                catch (_: Exception) {
                    showSnackbar(getCoordinatorLayout(),
                                 getString(R.string.export_fail),
                                 getSnackbarAnchorView())
                }
            }
        }
    
    // Subclasses must override
    protected abstract fun getCoordinatorLayout(): CoordinatorLayout
    
    // Subclasses must override
    protected abstract fun getSnackbarAnchorView(): View
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}