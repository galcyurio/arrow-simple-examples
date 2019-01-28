package com.github.galcyurio

import arrow.Kind
import arrow.core.Try
import arrow.typeclasses.Applicative
import arrow.typeclasses.ApplicativeError
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

interface NetworkOperationsUnsafe<F> : Applicative<F> {
    val network: NetworkModule

    fun Index.requestUser(): Kind<F, UserDto> =
        just(network.fetch(this, mapOf("1" to "2")))
}

interface DaoOperationsUnsave<F> : Applicative<F> {
    val dao: DaoDatabase

    fun Index.queryUser(): Kind<F, UserDao> =
        just(dao.query("SELECT * FROM users WHERE userId = $this"))
}

interface NetworkOperationsSync<F> : ApplicativeError<F, Throwable> {
    val network: NetworkModule

    fun Index.requestUser(): Kind<F, UserDto> =
        catch { network.fetch(this, mapOf("1" to "2")) }
//        try {
//            just(network.fetch(this, mapOf("1" to "2")))
//        } catch (e: Throwable) {
//            raiseError(e)
//        }
}

interface DaoOperationsSync<F> : ApplicativeError<F, Throwable> {
    val dao: DaoDatabase

    fun Index.queryUser(): Kind<F, UserDao> =
        catch { dao.query("SELECT * FROM users WHERE userId = $this") }
}