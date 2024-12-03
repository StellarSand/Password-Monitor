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

package com.password.monitor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.password.monitor.R
import com.password.monitor.activities.MainActivity
import com.password.monitor.models.SupportMethod
import com.password.monitor.utils.IntentUtils.Companion.openURL

class SupportMethodItemAdapter (private val aListViewItems: ArrayList<SupportMethod>,
                                private val mainActivity: MainActivity) : RecyclerView.Adapter<SupportMethodItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val supportMethodTitle: MaterialTextView = itemView.findViewById(R.id.supportMethodTitle)
        val supportMethodQr: ShapeableImageView = itemView.findViewById(R.id.supportMethodQr)
        val supportMethodUrl: MaterialTextView = itemView.findViewById(R.id.supportMethodUrl)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(parent.context)
                                  .inflate(R.layout.item_support_methods_recycler_view, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val supportMethod = aListViewItems[position]
        
        holder.supportMethodTitle.apply {
            text = supportMethod.title
            setCompoundDrawablesWithIntrinsicBounds(supportMethod.titleIcon, 0, 0, 0)
        }
        
        holder.supportMethodQr.setImageResource(supportMethod.qr)
        
        holder.supportMethodUrl.apply {
            text = supportMethod.url
            setOnClickListener {
                openURL(mainActivity, supportMethod.url)
            }
        }
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}