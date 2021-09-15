package com.techpuzzle.keopi.ui.caffebars.allcafebars

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.data.entities.CafeBar
import com.techpuzzle.keopi.databinding.FragmentAllCafeBarsBinding
import com.techpuzzle.keopi.ui.caffebars.CafeBarsAdapter
import com.techpuzzle.keopi.ui.caffebars.CafeBarsFragment
import com.techpuzzle.keopi.ui.caffebars.CafeBarsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

class AllCafeBarsFragment : Fragment(R.layout.fragment_all_cafe_bars) {

    private val viewModel: CafeBarsViewModel by activityViewModels()

    private var _binding: FragmentAllCafeBarsBinding? = null
    private val binding get() = _binding!!

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllCafeBarsBinding.bind(view)

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
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.allCafeBarsRecview.adapter = adapter
        binding.allCafeBarsRecview.layoutManager = LinearLayoutManager(requireContext())

        adapter.addLoadStateListener {
            binding.progressBar.isVisible = it.refresh is LoadState.Loading

            val hasResults =
                it.source.refresh is LoadState.NotLoading && adapter.itemCount < 1 && it.append.endOfPaginationReached
            binding.tvNoResults.isVisible = hasResults
            binding.imgNoResults.isVisible = hasResults
            binding.tvNoResults2.isVisible = hasResults

            if (it.append is LoadState.Error) {
                Snackbar.make(binding.root, "Error", Snackbar.LENGTH_INDEFINITE).show()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cafeBarsFlow.collectLatest {
                viewModel.lastCafeBars = it
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}