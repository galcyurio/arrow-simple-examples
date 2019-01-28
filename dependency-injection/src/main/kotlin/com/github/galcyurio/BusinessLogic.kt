package com.github.galcyurio

import arrow.Kind
import arrow.core.Try
import arrow.core.recoverWith
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.basics.Index
import arrow.typeclasses.basics.User

interface RequestOperations : DaoOperations, NetworkOperations, DomainMapper {
    fun Index.fetchUser(): Try<User> =
        queryUser().toUserFromDatabase()
            .recoverWith { requestUser().toUserFromNetwork() }
}

interface RequestOperationsSync<F>
    : DaoOperationsSync<F>, NetworkOperationsSync<F>, DomainMapperSync<F> {
    fun Index.fetchUser(): Kind<F, User> =
        queryUser().toUserFromDatabase()
            .handleErrorWith { requestUser().toUserFromNetwork() }
}

interface RequestOperationsLazy<F>
    : DaoOperationsSync<F>, NetworkOperationsSync<F>, DomainMapperSync<F>, MonadDefer<F> {
    fun Index.fetchUser(): Kind<F, User> = defer {
        queryUser().toUserFromDatabase()
            .handleErrorWith { requestUser().toUserFromNetwork() }
    }
}