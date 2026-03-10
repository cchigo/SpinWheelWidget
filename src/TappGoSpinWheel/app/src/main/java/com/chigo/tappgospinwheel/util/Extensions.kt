package com.chigo.tappgospinwheel.util

import java.security.MessageDigest

fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
}