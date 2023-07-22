package com.example.util.connectivity



interface ConnectivityObserver {

    enum class Status {
        AVAILABLE,
        UNAVAILABLE,
        LOSING,
        LOST
    }

    fun observe(): kotlinx.coroutines.flow.Flow<Status>

}