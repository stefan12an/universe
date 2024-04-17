package com.stefan.universe.common

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    GRANTED,
    DENIED
}

data class Resource<out T>(
    val status: Status,
    val data: T? = null,
    val errorCode: Int? = null,
    val exception: Exception? = null
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data)
        }

        fun <T> error(errorCode: Int, exception: Exception? = null): Resource<T> {
            return Resource(Status.ERROR, errorCode = errorCode, exception = exception)
        }

        fun loading(): Resource<Nothing> {
            return Resource(Status.LOADING)
        }
    }
}

val Resource<*>.succeeded: Boolean
    get() = status == Status.SUCCESS

val Resource<*>?.loading : Boolean
    get() = this != null && this.status == Status.LOADING