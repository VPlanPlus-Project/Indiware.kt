package plus.vplan.lib.indiware.source

sealed class Response<out T> {
    sealed class Error : Response<Nothing>() {
        data class Other(val message: String = "Other error") : OnlineError()
        data class ParsingError(val throwable: Throwable) : Error()
        data object Cancelled : Error()

        sealed class OnlineError: Error() {
            data object ConnectionError : OnlineError()
            data object Unauthorized : OnlineError()
            data object NotFound : OnlineError()
        }
    }
    data class Success<out T>(val data: T) : Response<T>()
}