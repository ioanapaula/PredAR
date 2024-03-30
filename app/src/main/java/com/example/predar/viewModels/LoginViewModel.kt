package com.example.predar.viewModels

import android.provider.Settings.Global.getString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.predar.R

class LoginViewModel: ViewModel(){
    private val teacherCode = "9120"
    var teacherLoginCode = ""
    var shouldDisplayTeacherLoginField = MutableLiveData<Boolean>()
    lateinit var navigationService: INavigationService

    interface INavigationService {
        fun navigateToHomepage()
        fun navigateToLesson()
    }

    fun teacherLoginButtonCommand() {
        shouldDisplayTeacherLoginField.value = true
    }

    fun studentLoginButtonCommand() {
        navigationService.navigateToLesson()
    }

    fun confirmationButtonCommand() {
        if (teacherCode == teacherLoginCode) {
            navigationService.navigateToHomepage()
            shouldDisplayTeacherLoginField.value = false
        }
    }
}