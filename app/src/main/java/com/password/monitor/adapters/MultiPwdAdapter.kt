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

import androidx.recyclerview.widget.RecyclerView
import com.password.monitor.adapters.MultiPwdAdapter.ListViewHolder
import com.password.monitor.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.google.android.material.textview.MaterialTextView
import com.password.monitor.models.MultiPwd
import me.stellarsand.android.fastscroll.PopupTextProvider

class MultiPwdAdapter(private val aListViewItems: List<MultiPwd>,
                      private val clickListener: OnItemClickListener): RecyclerView.Adapter<ListViewHolder>(), PopupTextProvider {
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        
        val breachedStatusIcon: ImageView = itemView.findViewById(R.id.breached_status_icon)
        val passwordLine: MaterialTextView = itemView.findViewById(R.id.password_line)
        
        init {
            // Handle click events of items
            itemView.setOnClickListener(this)
        }
        
        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position)
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_multi_pwd_rv, parent, false)
        return ListViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        aListViewItems[position].let {
            holder.passwordLine.text = it.password
            if (it.isBreached) holder.breachedStatusIcon.setImageResource(R.drawable.ic_found_in_breach)
        }
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    override fun getPopupText(view: View, position: Int): CharSequence {
        return aListViewItems[position].password.first().let {
            if (it.isLowerCase()) it.uppercase()
            else it
        }.toString()
    }
}