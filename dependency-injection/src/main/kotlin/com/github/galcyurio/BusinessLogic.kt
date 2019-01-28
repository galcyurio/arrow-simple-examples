package com.github.galcyurio

import arrow.core.Try
import arrow.core.recoverWith
import arrow.typeclasses.basics.Index
import arrow.typeclasses.basics.User

interface RequestOperations : DaoOperations, NetworkOperations, DomainMapper {
    fun Index.fetchUser(): Try<User> =
        queryUser().toUserFromDatabase()
            .recoverWith { requestUser().toUserFromNetwork() }
}