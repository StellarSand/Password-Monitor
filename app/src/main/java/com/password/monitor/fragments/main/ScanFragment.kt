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
import android.os.Bundle
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.appmanager.ApplicationManager
import com.password.monitor.databinding.FragmentScanBinding
import com.password.monitor.fragments.bottomsheets.NoNetworkBottomSheet
import com.password.monitor.fragments.bottomsheets.ScanMultiPwdBottomSheet
import com.password.monitor.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.password.monitor.utils.HashUtils.Companion.generateSHA1Hash
import com.password.monitor.utils.HashUtils.Companion.getHashCount
import com.password.monitor.utils.IntentUtils.Companion.openURL
import com.password.monitor.utils.NetworkUtils.Companion.hasInternet
import com.password.monitor.utils.NetworkUtils.Companion.hasNetwork
import com.password.monitor.utils.UiUtils.Companion.convertDpToPx
import com.password.monitor.utils.UiUtils.Companion.setFoundInBreachSubtitleText
import com.password.monitor.utils.UiUtils.Companion.showSnackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class ScanFragment : Fragment() {
    
    private var _binding: FragmentScanBinding? = null
    private val fragmentBinding get() = _binding!!
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
        
        var job: Job? = null
        naString = getString(R.string.na)
        breachedSuggestionString = getString(R.string.breached_suggestion)
        notBreachedSuggestionString = getString(R.string.not_breached_suggestion)
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scanCoordLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
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
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scanMultipleFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                rightMargin = insets.right + convertDpToPx(requireContext(), 16f)
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 25f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        fragmentBinding.passwordText.apply {
            if ((requireContext().applicationContext as ApplicationManager).preferenceManager.getBoolean(INCOG_KEYBOARD)) {
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
                            setFoundInBreachSubtitleText(context = requireContext(),
                                                         reset = true,
                                                         textView = fragmentBinding.foundInBreachSubtitle)
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
                fragmentBinding.progressIndicator.isVisible = true
                generateSHA1Hash(fragmentBinding.passwordText.text.toString()).apply {
                    hashPrefix = take(5).uppercase() // First 5 chars
                    hashSuffix = substring(5).uppercase() // Rest of the hash
                }
                checkPassword()
            }
        }
        
        // Tap here
        fragmentBinding.tapHereBtn.apply {
            setOnClickListener {
                openURL(requireActivity() as MainActivity,
                        getString(R.string.app_wiki_url),
                        fragmentBinding.scanCoordLayout,
                        fragmentBinding.scanMultipleFab)
            }
        }
        
        // Fab
        fragmentBinding.scanMultipleFab.setOnClickListener {
            ScanMultiPwdBottomSheet().show(parentFragmentManager, "ScanMultiplePwdBottomSheet")
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
            setFoundInBreachSubtitleText(context = requireContext(),
                                         isFound = true,
                                         textView = fragmentBinding.foundInBreachSubtitle)
            fragmentBinding.apply {
                timesFoundSubtitle.text = count.toString()
                suggestionSubtitle.text = breachedSuggestionString
            }
        }
        else {
            setFoundInBreachSubtitleText(context = requireContext(),
                                         isFound = false,
                                         textView = fragmentBinding.foundInBreachSubtitle)
            fragmentBinding.apply {
                timesFoundSubtitle.text = naString
                suggestionSubtitle.text = notBreachedSuggestionString
            }
        }
    }
    
    private fun checkPassword() {
        lifecycleScope.launch{
            val context = requireContext()
            if (hasNetwork(context) && hasInternet()) {
                val apiRepository = (context.applicationContext as ApplicationManager).apiRepository
                val hashesCall = apiRepository.getHashes(hashPrefix)
                val hashesResponse = hashesCall.awaitResponse()
                
                if (hashesResponse.isSuccessful) {
                    val responseBody = hashesResponse.body()
                    val count = getHashCount(responseBody, hashSuffix)
                    fragmentBinding.progressIndicator.visibility = View.INVISIBLE
                    displayResult(count)
                }
                else {
                    showSnackbar(fragmentBinding.scanCoordLayout,
                                         context.getString(R.string.something_went_wrong),
                                         fragmentBinding.scanMultipleFab)
                }
                
                enableUiComponents(true)
            }
            else {
                NoNetworkBottomSheet(positiveButtonClickListener = { checkPassword() },
                                     negativeButtonClickListener = {
                                         fragmentBinding.progressIndicator.visibility = View.INVISIBLE
                                         enableUiComponents(true)
                                     })
                    .show(parentFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}