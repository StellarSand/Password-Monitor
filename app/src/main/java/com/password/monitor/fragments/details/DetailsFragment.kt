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

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.password.monitor.R
import com.password.monitor.activities.DetailsActivity
import com.password.monitor.fragments.common.BaseResultsFragment
import com.password.monitor.models.MultiPwd
import com.password.monitor.utils.UiUtils.Companion.convertDpToPx

class DetailsFragment : BaseResultsFragment() {
    
    private lateinit var detailsActivity: DetailsActivity
    
    override fun setupFragmentContent() {
        detailsActivity = (requireActivity() as DetailsActivity)
        val pwdItem =
            if (Build.VERSION.SDK_INT >= 33) arguments?.getParcelable("PwdItem", MultiPwd::class.java)!!
            else arguments?.getParcelable("PwdItem")!!
        
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
        
        fragmentBinding.apply {
            passwordBox.hint = getString(R.string.password)
            passwordText.apply {
                setText(pwdItem.password)
                isFocusable = false
                isCursorVisible = false
            }
            checkBtn.isVisible = false
            scanMultipleFab.isVisible = false
        }
        
        displayResults(pwdItem.breachedCount)
        
        fragmentBinding.detailsCard.isVisible = true
    }
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return detailsActivity.activityBinding.detailsCoordLayout
    }
    
    override fun getSnackbarAnchorView(): View {
        return detailsActivity.activityBinding.detailsDockedToolbar
    }
}