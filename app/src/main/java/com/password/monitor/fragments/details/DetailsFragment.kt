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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.password.monitor.R
import com.password.monitor.activities.DetailsActivity
import com.password.monitor.databinding.FragmentScanBinding
import com.password.monitor.bottomsheets.NoNetworkBottomSheet
import com.password.monitor.repositories.ApiRepository
import com.password.monitor.utils.HashUtils.Companion.generateSHA1Hash
import com.password.monitor.utils.HashUtils.Companion.getHashCount
import com.password.monitor.utils.IntentUtils.Companion.openURL
import com.password.monitor.utils.NetworkUtils.Companion.hasInternet
import com.password.monitor.utils.NetworkUtils.Companion.hasNetwork
import com.password.monitor.utils.UiUtils.Companion.convertDpToPx
import com.password.monitor.utils.UiUtils.Companion.setFoundInBreachSubtitleText
import com.password.monitor.utils.UiUtils.Companion.showSnackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import java.util.Locale

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
            loadingIndicator.isVisible = true
            scanMultipleFab.isVisible = false
            
            passwordText.text.toString().generateSHA1Hash().apply {
                hashPrefix = take(5).uppercase() // First 5 chars
                hashSuffix = substring(5).uppercase() // Rest of the hash
            }
            
            checkPassword()
            
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
            fragmentBinding.foundInBreachSubtitle.setFoundInBreachSubtitleText(context = requireContext(),
                                                                               isFound = true)
            fragmentBinding.apply {
                timesFoundSubtitle.text = String.format(Locale.getDefault(), "%d", count)
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
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 "${getString(R.string.something_went_wrong)}: $e",
                                 fragmentBinding.scanMultipleFab)
                }
                fragmentBinding.loadingIndicator.isVisible = false
            }
            else {
                NoNetworkBottomSheet(positiveButtonClickListener = { checkPassword() },
                                     negativeButtonClickListener = {
                                         fragmentBinding.loadingIndicator.isVisible = false
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