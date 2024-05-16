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

package com.password.monitor.fragments.multipwd

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.password.monitor.activities.DetailsActivity
import com.password.monitor.activities.MultiPwdActivity
import com.password.monitor.adapters.MultiPwdAdapter
import com.password.monitor.appmanager.ApplicationManager
import com.password.monitor.databinding.FragmentMultiPwdBinding
import com.password.monitor.models.MultiPwdItem
import me.stellarsand.android.fastscroll.FastScrollerBuilder

class MultiPwdFragment : Fragment(), MultiPwdAdapter.OnItemClickListener {
    
    private var _binding: FragmentMultiPwdBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var multiplePwdList: List<MultiPwdItem>
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentMultiPwdBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val appManager = requireContext().applicationContext as ApplicationManager
        val multiPwdActivity = requireActivity() as MultiPwdActivity
        multiplePwdList =
            if (multiPwdActivity.isAscSort) appManager.multiPasswordsList.sortedBy { it.passwordLine.lowercase() }
            else appManager.multiPasswordsList.sortedByDescending { it.passwordLine.lowercase() }
        
        fragmentBinding.recyclerView.apply {
            adapter = MultiPwdAdapter(multiplePwdList, this@MultiPwdFragment)
            layoutManager =
                if (!multiPwdActivity.isGridView) LinearLayoutManager(requireContext())
                else StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            FastScrollerBuilder(this).build()
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        startActivity(Intent(requireContext(), DetailsActivity::class.java)
                          .putExtra("PwdLine", multiplePwdList[position].passwordLine))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}