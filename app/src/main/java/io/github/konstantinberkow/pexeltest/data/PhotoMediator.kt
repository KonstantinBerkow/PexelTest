package io.github.konstantinberkow.pexeltest.data

interface PhotoMediator {

    sealed interface Action {

        data object Refresh : Action

        data class LoadPage(val page: Int) : Action
    }

    fun performAction(action: Action, callback: (Result) -> Unit)

    sealed class Result {

        abstract val action: Action

        data class Success(override val action: Action) : Result()

        data class Failure(override val action: Action, val msg: String) : Result()
    }
}
