package com.example.predar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.example.predar.databinding.FragmentHomeBinding
import com.example.predar.viewModels.HomeViewModel
import com.example.predar.viewModels.LessonListViewModel

class HomeFragment: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pager.adapter = HomeTabsCollectionAdapter(childFragmentManager, viewModel)
        binding.tabLayout.setupWithViewPager(binding.pager)
    }
}

class HomeTabsCollectionAdapter(
    fm: FragmentManager,
    private val viewModel: HomeViewModel
) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int  = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            LessonListViewModel.LessonListType.SCIENCE.value -> {
                LessonListFragment.newInstance(LessonListViewModel.LessonListType.SCIENCE.value)
            }
            LessonListViewModel.LessonListType.MATH.value -> {
                LessonListFragment.newInstance(LessonListViewModel.LessonListType.MATH.value)
            }
            else -> {
                LessonListFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return viewModel.tabs[position]
    }
}

