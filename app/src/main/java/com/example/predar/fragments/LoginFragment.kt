package com.example.predar.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.predar.R
import com.example.predar.activities.LessonActivity
import com.example.predar.activities.StudentActivity
import com.example.predar.databinding.FragmentLoginBinding
import com.example.predar.viewModels.LoginViewModel

class LoginFragment: Fragment(), LoginViewModel.INavigationService {
    lateinit var viewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

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
        binding.studentLoginButton.text = getString(R.string.student_login_button)
        binding.teacherLoginButton.text = getString(R.string.teacher_login_button)
        binding.teacherLoginCode.hint = getString(R.string.teacher_login_hint)

        viewModel.shouldDisplayTeacherLoginField.observe(viewLifecycleOwner, Observer { fieldVisibility -> handleLoginFieldVisibility(fieldVisibility)})

        binding.teacherLoginButton.setOnClickListener { viewModel.teacherLoginButtonCommand() }
        binding.studentLoginButton.setOnClickListener { viewModel.studentLoginButtonCommand() }
        binding.teacherLoginConfirmationButton.setOnClickListener {
            viewModel.teacherLoginCode = binding.teacherLoginCode.text.toString()
            viewModel.confirmationButtonCommand()
        }
    }

    private fun handleLoginFieldVisibility(fieldVisibility: Boolean) {
        if (fieldVisibility) {
            binding.teacherLoginButton.visibility = GONE
            binding.teacherLoginSection.visibility = VISIBLE
        }
        else {
            binding.teacherLoginButton.visibility = VISIBLE
            binding.teacherLoginSection.visibility = GONE
        }
    }

    override fun navigateToHomepage() {
        binding.root.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun navigateToLesson() {
        val intent = Intent(context, StudentActivity::class.java)
        startActivity(intent)
    }

}