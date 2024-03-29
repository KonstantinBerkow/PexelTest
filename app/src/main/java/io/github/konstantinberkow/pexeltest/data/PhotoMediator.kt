package io.github.konstantinberkow.pexeltest.data

interface PhotoMediator {

    sealed interface Action {

        data class Refresh(val pageSize: Int) : Action

        data class LoadPage(val page: Int, val pageSize: Int) : Action
    }

    fun performAction(action: Action, callback: (Result) -> Unit)

    sealed interface Result {

        val action: Action

        data class Success(override val action: Action) : Result

        data class Failure(override val action: Action, val msg: String) : Result
    }
}
