package com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler

import com.nemesiss.dev.HTMLContentParser.InvalidStepExecutorException

abstract class ParseContentErrorHandler {

    abstract fun Output(code : Int, message : String);

    fun Handle(e: Exception) {
        when(e) {
            is InvalidStepExecutorException -> {
                Output(-1, "InvalidStepExecutorException: ${e.message}")
            }
            is ClassNotFoundException -> {
                Output(-2, "ClassNotFoundException: ${e.message}")
            }
            else -> {
                Output(-3, "Exception: ${e.message}")
            }
        }
    }

}