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

import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.password.monitor.activities.MainActivity
import com.password.monitor.bottomsheets.common.ExceptionErrorBottomSheet
import com.password.monitor.bottomsheets.common.NoNetworkBottomSheet
import com.password.monitor.bottomsheets.main.ScanMultiPwdBottomSheet
import com.password.monitor.fragments.common.BaseResultsFragment
import com.password.monitor.objects.AppState
import com.password.monitor.preferences.PreferenceManager
import com.password.monitor.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.password.monitor.utils.HashUtils.Companion.getHashCount
import com.password.monitor.utils.HashUtils.Companion.getHashPrefixAndSuffix
import com.password.monitor.utils.NetworkUtils.Companion.hasInternet
import com.password.monitor.utils.UiUtils.Companion.convertDpToPx
import com.password.monitor.utils.UiUtils.Companion.setFoundInBreachSubtitleText
import com.password.monitor.utils.UiUtils.Companion.showSupportAnimBtmSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import kotlin.time.Duration.Companion.milliseconds

class ScanFragment : BaseResultsFragment() {
    
    private lateinit var mainActivity: MainActivity
    private var isInitialLaunch = true
    private var collapsingToolbarLargeHeightInPx = 0
    private var collapsingToolbarTopInsets = -1
    private var hashPrefix = ""
    private var hashSuffix = ""
    
    override fun setupFragmentContent() {
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
                        delay(300.milliseconds)
                        val isEmpty = charSequence!!.isEmpty()
                        fragmentBinding.checkBtn.isEnabled = !isEmpty
                        
                        if (isEmpty) {
                            fragmentBinding.apply {
                                foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(), reset = true)
                                fragmentBinding.copyChipGroup.forEach {
                                    (it as? Chip)?.isEnabled = false
                                }
                                timesFoundSubtitle.text = naString
                                suggestionSubtitle.text = naString
                            }
                        }
                    }
            }
            
            // Detect if copied from this app
            customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }
                
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }
                
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        android.R.id.copy -> {
                            copyToClipboard(text.toString())
                            scheduleClipboardClear(requireContext())
                        }
                    }
                    return true
                }
                
                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }
        
        // Check
        fragmentBinding.checkBtn.apply {
            setOnClickListener {
                enableUiComponents(false)
                fragmentBinding.progressIndicator.show()
                lifecycleScope.launch {
                    getHashPrefixAndSuffix(fragmentBinding.passwordText.text.toString())
                        .let {
                            hashPrefix = it.first
                            hashSuffix = it.second
                        }
                    checkPassword()
                }
            }
        }
        
        // Fab
        fragmentBinding.scanMultipleFab.setOnClickListener {
            ScanMultiPwdBottomSheet().show(parentFragmentManager, "ScanMultiPwdBottomSheet")
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
            fragmentBinding.copyChipGroup.forEach {
                (it as? Chip)?.isEnabled = enable
            }
        }
    }
    
    override fun displayResults(breachedCount: Int) {
        super.displayResults(breachedCount)
        lifecycleScope.launch {
            if (!isInitialLaunch && AppState.showSupportBtmSheet) {
                showSupportAnimBtmSheet(parentFragmentManager)
            }
            if (isInitialLaunch) {
                isInitialLaunch = false
                fragmentBinding.appBar.setExpanded(false, true)
                fragmentBinding.detailsCard.isVisible = true
                setCollapsingToolbarHeight(collapsingToolbarTopInsets + collapsingToolbarLargeHeightInPx)
            }
        }
    }
    
    private fun checkPassword() {
        lifecycleScope.launch {
            if (hasInternet(requireContext())) {
                try {
                    val hashesResponse = get<ApiRepository>().getHashes(hashPrefix)
                    displayResults(getHashCount(hashesResponse, hashSuffix))
                }
                catch (e: Exception) {
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
                fragmentBinding.checkBtn.isEnabled = false
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
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return mainActivity.activityBinding.mainCoordLayout
    }
    
    override fun getSnackbarAnchorView(): View {
        return fragmentBinding.scanMultipleFab
    }
    
}