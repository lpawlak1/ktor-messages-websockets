package com.example.loggin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

fun getLogger(forClass: Class<*>): Logger =
    LoggerFactory.getLogger(forClass)

inline fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: javaClass
}

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>)
            = getLogger(getClassForLogging(thisRef.javaClass))
}

open class Logging {
    protected val logger by LoggerDelegate()
}