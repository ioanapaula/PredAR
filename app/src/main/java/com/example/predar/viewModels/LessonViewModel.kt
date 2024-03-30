package com.example.predar.viewModels

import androidx.lifecycle.ViewModel

class LessonViewModel: ViewModel() {
    enum class AppAnchorState (val value: Int){
        NONE(0),
        HOSTING(1),
        HOSTED(2),
        RESOLVING(3),
        RESOLVED(4)
    }

    enum class LessonType (val value: Int){
        GEOMMETRY(0),
        SOLAR(1),
        UNKNOWN(2)
    }

    var anchorState: AppAnchorState = AppAnchorState.NONE
    var lessonType: LessonType = LessonType.UNKNOWN
}