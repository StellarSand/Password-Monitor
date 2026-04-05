/*
 *     Copyright (C) 2022-present StellarSand
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

package com.password.monitor.common

import android.content.Context
import com.password.monitor.R
import com.password.monitor.databinding.FragmentScanBinding

fun FragmentScanBinding.getFormattedResultsText(context: Context): String {
    return buildString {
        append("# ${context.getString(R.string.password)}\n")
        append("${passwordText.text.toString()}\n\n")
        append("## ${context.getString(R.string.found_in_breach)}\n")
        append("${foundInBreachSubtitle.text}\n\n")
        append("## ${context.getString(R.string.times_found)}\n")
        append("${timesFoundSubtitle.text}\n\n")
        append("## ${context.getString(R.string.suggestion)}\n")
        append("${suggestionSubtitle.text}\n\n")
    }
}
