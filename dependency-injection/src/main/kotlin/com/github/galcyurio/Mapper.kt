package com.github.galcyurio

import arrow.Kind
import arrow.core.Try
import arrow.typeclasses.MonadError
import arrow.typeclasses.basics.User
import arrow.typeclasses.basics.UserDao
import arrow.typeclasses.basics.UserDto
import arrow.typeclasses.basics.realWorld

interface DomainMapper {
    fun Try<UserDto>.toUserFromNetwork(): Try<User> =
        flatMap { userDto -> Try { realWorld { User(userDto.id) } } }

    fun Try<UserDao>.toUserFromDatabase(): Try<User> =
        flatMap { userDao -> Try { realWorld { User(userDao.id) } } }
}

interface DomainMapperSync<F> : MonadError<F, Throwable> {
    fun Kind<F, UserDto>.toUserFromNetwork(): Kind<F, User> =
        flatMap { user -> catch { realWorld { User(user.id) } } }

    fun Kind<F, UserDao>.toUserFromDatabase(): Kind<F, User> =
        flatMap { user -> catch { realWorld { User(user.id) } } }
}