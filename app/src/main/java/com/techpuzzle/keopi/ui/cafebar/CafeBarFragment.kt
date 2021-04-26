package com.techpuzzle.keopi.ui.cafebar

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.techpuzzle.keopi.R
import com.techpuzzle.keopi.databinding.FragmentCafeBarBinding
import com.techpuzzle.keopi.utils.Status
import com.techpuzzle.keopi.utils.connection.ConnectionLiveData
import com.techpuzzle.keopi.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CafeBarFragment : Fragment(R.layout.fragment_cafe_bar) {

    private val TAG = "CafeBarFragment"

    private var _binding: FragmentCafeBarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CafeBarViewModel by viewModels()

    private lateinit var connectionLiveData: ConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionLiveData = ConnectionLiveData(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val action = CafeBarFragmentDirections.actionCafeBarFragmentToCaffesFragment()
                    findNavController().navigate(action)
                }
            })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCafeBarBinding.bind(view)
        viewModel.loadImageUrls(viewModel.cafeBar._id)
        setup()


        val listener = object : CafeBarImagePagerAdapter.OnItemClickListener {
            override fun onItemClick() {
                val extras = FragmentNavigatorExtras(binding.viewPagerImages to "view_pager_big")

                val action =
                    CafeBarFragmentDirections.actionCafeBarFragmentToExpandedViewPagerFragment(
                        imageUrls = viewModel.imageUrlsFlow.value.toTypedArray(),
                        position = viewModel.position,
                        cafeBar = viewModel.cafeBar
                    )

                findNavController().navigate(
                    R.id.action_cafeBarFragment_to_expandedViewPagerFragment,
                    action.arguments,
                    null,
                    extras
                )
            }
        }

        val viewPagerImagesAdapter = CafeBarImagePagerAdapter(listener)
        binding.viewPagerImages.adapter = viewPagerImagesAdapter
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPagerImages
        ) { _, _ -> }.attach()


        viewModel.loadImageUrlsStatus.observe(viewLifecycleOwner) {
            if (it.getContentIfNotHandled()?.status == Status.ERROR) {
                viewPagerImagesAdapter.isError = true
                viewPagerImagesAdapter.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.imageUrlsFlow.collectLatest {
                it.let {
                    viewPagerImagesAdapter.imageUrls = it
                    viewPagerImagesAdapter.notifyDataSetChanged()
                    if (!viewModel.isStartPositionUsed) {
                        binding.viewPagerImages.setCurrentItem(viewModel.startPosition, false)
                        viewModel.isStartPositionUsed = true
                    } else {
                        binding.viewPagerImages.setCurrentItem(viewModel.position, false)
                    }
                }
            }
        }

        val viewPager2Callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.position = position
                super.onPageSelected(position)
            }
        }

        binding.viewPagerImages.registerOnPageChangeCallback(viewPager2Callback)


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cafeBarEvents.collectLatest { event ->
                when (event) {
                    is CafeBarViewModel.CafeBarEvents.SendMessage -> {
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }

        binding.apply {
            imgBack.setOnClickListener {
                requireActivity().onBackPressed()
            }

            imgAdd.setOnClickListener {
                viewModel.onAddClicked(viewModel.cafeBar)
            }

            cardView.setOnClickListener {}

            btnTakeToCafe.setOnClickListener {
                val latitude = viewModel.cafeBar.latitude
                val longitude = viewModel.cafeBar.longitude
                val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(binding.root, "Greska", Snackbar.LENGTH_SHORT).show()
                }
            }

            imgCalendar.setOnClickListener {
                val action =
                    CafeBarFragmentDirections.actionCafeBarFragmentToCalendarFragment(viewModel.cafeBar)
                findNavController().navigate(action)
            }

            imgInstagram.setOnClickListener {
                val uri = Uri.parse(viewModel.cafeBar.instagram)
                val instagramIntent = Intent(Intent.ACTION_VIEW, uri)
                instagramIntent.setPackage("com.instagram.android")
                try {
                    startActivity(instagramIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
            imgFacebook.setOnClickListener {
                val facebook = viewModel.cafeBar.facebook
                val facebookIntent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/$facebook"))
                try {
                    startActivity(facebookIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/$facebook")))
                }
            }
        }
    }

    private fun setup() {
        val cafeBar = viewModel.cafeBar
        binding.apply {
            tvAge.text = cafeBar.age
            tvCafeBarName.text = cafeBar.name
            val startTime =
                if (cafeBar.startingWorkTime < 10) "0${cafeBar.startingWorkTime}:00" else "${cafeBar.startingWorkTime}:00"
            val endTime =
                if (cafeBar.endingWorkTime < 10) "0${cafeBar.endingWorkTime}:00" else "${cafeBar.endingWorkTime}:00"
            binding.tvWorkingTime.text = "$startTime $endTime"
            tvLocation.text = cafeBar.location
            tvTables.text = cafeBar.capacity
            tvDescription.text = cafeBar.bio
            tvMusic.text = cafeBar.music
            tvTerasa.text = if (cafeBar.terrace) "Terrace" else "No terrace"
            setImageColorIfHasProperty(imgSmoking, cafeBar.smoking)
            setImageColorIfHasProperty(imgHookah, cafeBar.hookah)
            setImageColorIfHasProperty(imgBetting, cafeBar.betting)
            setImageColorIfHasProperty(imgDart, cafeBar.dart)
            setImageColorIfHasProperty(imgBilliards, cafeBar.billiards)
            setImageColorIfHasProperty(imgPlayground, cafeBar.playground)
        }
    }

    private fun setImageColorIfHasProperty(imgView: ImageView, hasProperty: Boolean) {
        val color =
            if (hasProperty) ContextCompat.getColor(requireContext(), R.color.yellow)
            else ContextCompat.getColor(requireContext(), R.color.gray_no_property)
        imgView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}