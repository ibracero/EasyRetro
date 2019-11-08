package com.ibracero.retrum.common

interface Mapper<in T, out S> {
    fun map(from: T): S
}
