package com.example.predar.viewModels

import androidx.lifecycle.ViewModel

class LessonListViewModel: ViewModel() {
    var lessonType: Int = 0
    lateinit var navigationService: INavigationService

    init {

    }

    enum class LessonListType(val value: Int) {
        SCIENCE(0),
        MATH(1)
    }

    interface INavigationService {
        fun navigateToLesson()
    }

    fun lessonTappedCommand() {
        navigationService.navigateToLesson()
    }
}