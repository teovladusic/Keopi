package com.techpuzzle.keopi.ui.cafebar.expandedviewpager

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.databinding.FragmentExpandedViewPagerBinding
import com.techpuzzle.keopi.ui.cafebar.CafeBarImagePagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpandedViewPagerFragment : Fragment(R.layout.fragment_expanded_view_pager) {

    private var _binding: FragmentExpandedViewPagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpandedViewPagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enterAnimation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        sharedElementEnterTransition = enterAnimation

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action =
                        ExpandedViewPagerFragmentDirections.actionExpandedViewPagerFragmentToCafeBarFragment(
                            viewModel.cafeBar,
                            position = viewModel.position,
                            imageUrls = viewModel.imageUrls.toTypedArray()
                        )
                    val extras = FragmentNavigatorExtras(binding.viewPagerExpanded to "view_pager_small")
                    findNavController().navigate(
                        R.id.action_expandedViewPagerFragment_to_cafeBarFragment,
                        action.arguments,
                        null,
                        extras
                    )
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpandedViewPagerBinding.bind(view)

        val listener = object : CafeBarImagePagerAdapter.OnItemClickListener {
            override fun onItemClick() {}
        }

        val imageUrls = viewModel.imageUrls
        val adapter = CafeBarImagePagerAdapter(listener)
        adapter.imageUrls = imageUrls
        binding.viewPagerExpanded.adapter = adapter
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPagerExpanded
        ) { _, _ -> }.attach()

        binding.viewPagerExpanded.setCurrentItem(viewModel.position, false)

        adapter.notifyDataSetChanged()

        binding.imgClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val viewPager2Callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.position = position
                super.onPageSelected(position)
            }
        }

        binding.viewPagerExpanded.registerOnPageChangeCallback(viewPager2Callback)
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}