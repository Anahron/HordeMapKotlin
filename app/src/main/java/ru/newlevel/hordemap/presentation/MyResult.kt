package ru.newlevel.hordemap.presentation

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class MyResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : MyResult<T>()
    data class Error(val exception: Exception) : MyResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}