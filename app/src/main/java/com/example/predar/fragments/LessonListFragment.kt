package com.example.predar.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.predar.R
import com.example.predar.activities.GeommetryActivity
import com.example.predar.activities.LessonActivity
import com.example.predar.activities.SolarActivity
import com.example.predar.databinding.FragmentHomeTabBinding
import com.example.predar.viewModels.LessonListViewModel

private const val LESSON_TYPE_KEY = "lessonType"

class LessonListFragment: Fragment(), LessonListViewModel.INavigationService {
    lateinit var viewModel: LessonListViewModel
    private var _binding: FragmentHomeTabBinding? = null
    private val binding get() = _binding!!
    private var lessonType: Int = 0

    companion object LessonListFactory {
        fun newInstance(lessonType: Int): LessonListFragment {
            var fragment = LessonListFragment()
            val bundle = bundleOf(LESSON_TYPE_KEY to lessonType)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lessonType = arguments?.getInt(LESSON_TYPE_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeTabBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LessonListViewModel::class.java)

        setBindings()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        viewModel.navigationService = this
    }

    private fun setBindings() {
        initFragment(lessonType)

        binding.lessonCard.setOnClickListener { viewModel.lessonTappedCommand() }
    }

    private fun initFragment(lessonType: Int) {
        when (lessonType) {
            LessonListViewModel.LessonListType.SCIENCE.value -> {
                binding.lessonImage.setImageResource(R.drawable.solar_system)
                binding.lessonTitle.text = getString(R.string.science_lesson_title)
                binding.lessonIndex.text = getString(R.string.lesson_index)

            }
            LessonListViewModel.LessonListType.MATH.value -> {
                binding.lessonImage.setImageResource(R.drawable.geometric_shapes)
                binding.lessonTitle.text = getString(R.string.math_lesson_title)
                binding.lessonIndex.text = getString(R.string.lesson_index)
            }
        }
    }

    override fun navigateToLesson() {
        when (lessonType) {
            LessonListViewModel.LessonListType.SCIENCE.value -> {
                val intent = Intent(context, SolarActivity::class.java)
                startActivity(intent)
            }
            LessonListViewModel.LessonListType.MATH.value -> {
                val intent = Intent(context, GeommetryActivity::class.java)
                startActivity(intent)
            }
        }
    }
}