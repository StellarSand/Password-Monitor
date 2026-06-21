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

package com.password.monitor.bottomsheets.common

import com.password.monitor.R

class NoNetworkBottomSheet(
    onPositiveBtnClick: () -> Unit,
    onNegativeBtnClick: () -> Unit
) : BaseErrorBottomSheet() {
    
    override val titleTextResId = R.string.no_network_title
    override val descriptionText = TextOrRes.Res(R.string.no_network_desc)
    override val positiveBtnClickAction = onPositiveBtnClick
    override val negativeBtnClickAction = onNegativeBtnClick
}