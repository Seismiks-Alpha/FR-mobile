package com.seismiks.nyamapp.utils

open class Event<out T>(private val content: T) {

    private var eventHandled = false

    fun getContentIfNotHandled(): T? {
        return if (eventHandled) {
            null
        } else {
            eventHandled = true
            content
        }
    }

}