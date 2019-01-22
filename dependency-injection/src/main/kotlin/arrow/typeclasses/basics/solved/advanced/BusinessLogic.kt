package arrow.typeclasses.basics.solved.advanced

import arrow.Kind
import arrow.effects.typeclasses.Async
import arrow.typeclasses.basics.Index
import arrow.typeclasses.basics.User
import arrow.typeclasses.basics.solved.DomainMapperSync
import kotlin.coroutines.CoroutineContext

// Have you already checked the changes to the Framework?

interface RequestOperationsKAsync<F, N, D>
  : DaoOperationsKAsync<F, D>, NetworkOperationsKAsync<F, N>, DomainMapperSync<F>, Async<F>, CoroutineContext {
  fun Index.fetchUser(): Kind<F, User> = defer(this@RequestOperationsKAsync) {
    queryUser().toUserFromDatabase()
      .handleErrorWith { requestUser().toUserFromNetwork() }
  }
}

// Okay, so Usage should be simple, right?
