package com.github.galcyurio

import arrow.core.Try
import arrow.typeclasses.basics.*

interface NetworkOperations {
    val network: NetworkModule

    fun Index.requestUser(): Try<UserDto> =
        Try { network.fetch(this, mapOf("1" to "2")) }
}

interface DaoOperations {
    val dao: DaoDatabase

    fun Index.queryUser(): Try<UserDao> =
        Try { dao.query("SELECT * FROM users where userId = $this") }

    fun Index.queryCompany(): Try<UserDao> =
        Try { dao.query("SELECT * FROM companies WHERE companyId = $this") }
}