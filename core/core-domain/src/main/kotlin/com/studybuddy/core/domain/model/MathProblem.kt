package com.studybuddy.core.domain.model

data class MathProblem(val operandA: Int, val operandB: Int, val operator: Operator, val correctAnswer: Int) {
    val displayString: String
        get() = "$operandA ${operator.symbol} $operandB"
}
