package com.techpuzzle.keopi.ui.caffebars.cachedcafebars

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.FragmentCachedCafeBarsBinding
import com.techpuzzle.keopi.ui.caffebars.CafeBarsAdapter
import com.techpuzzle.keopi.ui.caffebars.CafeBarsFragment
import com.techpuzzle.keopi.ui.caffebars.CafeBarsViewModel
import com.techpuzzle.keopi.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CachedCafeBarsFragment : Fragment(R.layout.fragment_cached_cafe_bars) {

    private val viewModel: CafeBarsViewModel by activityViewModels()

    private var _binding: FragmentCachedCafeBarsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCachedCafeBarsBinding.bind(view)


        val listener = object : CafeBarsAdapter.OnItemClickListener {
            override fun onAddRemoveBarClick(
                hostingFragments: CafeBarsFragment.HostingFragments,
                cafeBar: CafeBar
            ) {
                viewModel.onAddRemoveBarClick(hostingFragments, cafeBar)
            }

            override fun onItemClick(cafeBar: CafeBar) {
                viewModel.onCafeBarClick(cafeBar)
            }
        }

        val adapter = CafeBarsAdapter(listener)
        adapter.hostingFragment = CafeBarsFragment.HostingFragments.CachedCafeBars

        binding.recViewCachedCafeBars.adapter = adapter
        binding.recViewCachedCafeBars.layoutManager = LinearLayoutManager(requireContext())

        adapter.addLoadStateListener {
            val hasResults =
                it.source.refresh is LoadState.NotLoading && adapter.itemCount < 1 && it.append.endOfPaginationReached

            binding.tvNoResults.isVisible = hasResults
            binding.imgNoResults.isVisible = hasResults
            binding.tvNoResults2.isVisible = hasResults
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cachedCafeBarsFlow.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}