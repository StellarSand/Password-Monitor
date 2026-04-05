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

package com.password.monitor.fragments.main

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.bottomsheets.ExceptionErrorBottomSheet
import com.password.monitor.databinding.FragmentScanBinding
import com.password.monitor.bottomsheets.NoNetworkBottomSheet
import com.password.monitor.bottomsheets.ScanMultiPwdBottomSheet
import com.password.monitor.common.getFormattedResultsText
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.ClipboardUtils.Companion.hideSensitiveContent
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import java.text.NumberFormat

class ScanFragment : Fragment() {
    
    private var _binding: FragmentScanBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private var isInitialLaunch = true
    private var collapsingToolbarLargeHeightInPx = 0
    private var collapsingToolbarTopInsets = -1
    private lateinit var naString: String
    private lateinit var breachedSuggestionString: String
    private lateinit var notBreachedSuggestionString: String
    private var hashPrefix = ""
    private var hashSuffix = ""
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        var job: Job? = null
        val displayMetrics = resources.displayMetrics
        TypedValue().let {
            requireContext().theme.resolveAttribute(
                com.google.android.material.R.attr.collapsingToolbarLayoutLargeSize,
                it,
                true
            )
            collapsingToolbarLargeHeightInPx = TypedValue.complexToDimensionPixelSize(it.data, displayMetrics)
        }
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        naString = getString(R.string.na)
        breachedSuggestionString = getString(R.string.breached_suggestion)
        notBreachedSuggestionString = getString(R.string.not_breached_suggestion)
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.collapsingToolbar) { _, windowInsets ->
            if (collapsingToolbarTopInsets == -1) {
                val insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    )
                collapsingToolbarTopInsets = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
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
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scanMultipleFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                rightMargin = insets.right + convertDpToPx(requireContext(), 16f)
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 25f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Set collapsing toolbar to center of screen for first time
        // Don't move this within setOnApplyWindowInsetsListener() above
        setCollapsingToolbarHeight(
            (collapsingToolbarTopInsets + displayMetrics.heightPixels) / 2
            - (64f * resources.displayMetrics.density).toInt()
        )
        
        // Prevent dragging of appbar when scrollview is not visible
        val appBarLayoutBehavior =
            AppBarLayout.Behavior().also {
                (fragmentBinding.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior = it
            }
        appBarLayoutBehavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return !isInitialLaunch
            }
        })
        
        fragmentBinding.passwordText.apply {
            if (get<PreferenceManager>().getBoolean(INCOG_KEYBOARD)) {
                imeOptions = IME_FLAG_NO_PERSONALIZED_LEARNING
                inputType = TYPE_TEXT_VARIATION_PASSWORD
            }
            
            doOnTextChanged { charSequence, _, _, _ ->
                // Introduce a subtle delay
                // So passwords are checked after typing is finished
                job?.cancel()
                job =
                    lifecycleScope.launch {
                        delay(300)
                        val isEmpty = charSequence!!.isEmpty()
                        fragmentBinding.checkBtn.isEnabled = !isEmpty
                        
                        if (isEmpty) {
                            fragmentBinding.foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(),
                                                                                               reset = true)
                            fragmentBinding.apply {
                                timesFoundSubtitle.text = naString
                                suggestionSubtitle.text = naString
                            }
                        }
                    }
            }
        }
        
        // Check
        fragmentBinding.checkBtn.apply {
            setOnClickListener {
                enableUiComponents(false)
                fragmentBinding.progressIndicator.show()
                fragmentBinding.passwordText.text.toString().generateSHA1Hash().apply {
                    hashPrefix = take(5).uppercase() // First 5 chars
                    hashSuffix = substring(5).uppercase() // Rest of the hash
                }
                checkPassword()
            }
        }
        
        // Copy
        fragmentBinding.copyChip.setOnClickListener {
            val clipData = ClipData.newPlainText("PasswordMonitor", fragmentBinding.getFormattedResultsText(requireContext()))
            clipData.hideSensitiveContent()
            clipboardManager.setPrimaryClip(clipData)
            // Show snackbar only if 12L or lower to avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
            if (Build.VERSION.SDK_INT <= 32) {
                showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                             requireContext().getString(R.string.copied_to_clipboard),
                             fragmentBinding.scanMultipleFab)
            }
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
        fragmentBinding.tapHereBtn.apply {
            setOnClickListener {
                openURL(mainActivity,
                        getString(R.string.app_wiki_url),
                        mainActivity.activityBinding.mainCoordLayout,
                        fragmentBinding.scanMultipleFab)
            }
        }
        
        // Fab
        fragmentBinding.scanMultipleFab.setOnClickListener {
            ScanMultiPwdBottomSheet().show(parentFragmentManager, "ScanMultiplePwdBottomSheet")
        }
    }
    
    private fun setCollapsingToolbarHeight(height: Int) {
        fragmentBinding.collapsingToolbar.apply {
            val params = layoutParams
            params.height = height
            layoutParams = params
            requestLayout()
        }
    }
    
    private fun enableUiComponents(enable: Boolean) {
        fragmentBinding.apply {
            passwordText.apply {
                isFocusable = enable
                isFocusableInTouchMode = enable
                isCursorVisible = enable
            }
            checkBtn.isEnabled = enable
        }
    }
    
    private fun displayResult(count: Int) {
        if (count > 0) {
            fragmentBinding.foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(),
                                                                               isFound = true)
            fragmentBinding.apply {
                timesFoundSubtitle.text = NumberFormat.getInstance().format(count)
                suggestionSubtitle.text = breachedSuggestionString
            }
        }
        else {
            fragmentBinding.foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(),
                                                                               isFound = false)
            fragmentBinding.apply {
                timesFoundSubtitle.text = naString
                suggestionSubtitle.text = notBreachedSuggestionString
            }
        }
        
        if (isInitialLaunch) {
            isInitialLaunch = false
            fragmentBinding.appBar.setExpanded(false, true)
            fragmentBinding.detailsCard.isVisible = true
            setCollapsingToolbarHeight(collapsingToolbarTopInsets + collapsingToolbarLargeHeightInPx)
        }
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
                            enableUiComponents(true)
                        }
                    ).show(parentFragmentManager, "ExceptionErrorBottomSheet")
                }
                fragmentBinding.progressIndicator.hide()
                enableUiComponents(true)
            }
            else {
                NoNetworkBottomSheet(
                    onPositiveBtnClick = { checkPassword() },
                    onNegativeBtnClick = {
                        fragmentBinding.progressIndicator.hide()
                        enableUiComponents(true)
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
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                 getString(R.string.export_success),
                                 fragmentBinding.scanMultipleFab)
                }
                catch (_: Exception) {
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
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