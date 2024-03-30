package com.example.predar.activities

import android.os.Bundle
import android.view.View

class StudentActivity: LessonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.clearButton.visibility = View.GONE

    }
}
