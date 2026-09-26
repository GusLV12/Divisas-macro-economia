package com.gusslinaresv.activitefinanca.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gusslinaresv.activitefinanca.data.QuizRepository
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.QuizQuestion

class QuizViewModel : ViewModel() {

    var questions by mutableStateOf(QuizRepository.newRound())
        private set
    var index by mutableIntStateOf(0)
        private set
    /** Opción elegida en la pregunta actual (null = aún no responde). */
    var selected by mutableStateOf<Int?>(null)
        private set
    var score by mutableIntStateOf(0)
        private set
    var finished by mutableStateOf(false)
        private set
    var isNewRecord by mutableStateOf(false)
        private set

    val current: QuizQuestion get() = questions[index]
    val total: Int get() = questions.size

    fun answer(option: Int) {
        if (selected != null) return
        selected = option
        if (option == current.correctIndex) score++
    }

    fun next() {
        if (index < questions.lastIndex) {
            index++
            selected = null
        } else {
            finished = true
            isNewRecord = UserPreferences.submitQuizScore(score)
        }
    }

    fun restart() {
        questions = QuizRepository.newRound()
        index = 0
        selected = null
        score = 0
        finished = false
        isNewRecord = false
    }
}
