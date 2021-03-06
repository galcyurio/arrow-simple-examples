@file:Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")

package arrow.typeclasses.basics.solved

import arrow.Kind
import arrow.effects.DeferredK
import arrow.effects.ForDeferredK
import arrow.effects.deferredk.async.async
import arrow.effects.fix
import arrow.effects.typeclasses.Async
import arrow.effects.unsafeRunSync
import arrow.typeclasses.basics.DaoDatabase
import arrow.typeclasses.basics.Index
import arrow.typeclasses.basics.NetworkModule
import arrow.typeclasses.basics.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

fun <F> RequestOperationsAsync<F>.fetchUser(idx: Index): Kind<F, User> =
  idx.fetchUser()

class MyViewModel<F>(dep: RequestOperationsAsync<F>) : RequestOperationsAsync<F> by dep {
  fun onStart() {
    1.fetchUser()
  }
}

class MyActivity {
  fun onStart() {
    dependenciesAsValues.run { 1.fetchUser() }.fix().unsafeRunSync()

    runBlocking { dependenciesAsValues.fetchUser(1) }

    runBlocking { MyViewModel(dependenciesAsValues).fetchUser(1) }
  }
}

val dependenciesAsValues: RequestOperationsAsync<ForDeferredK> =
  object : RequestOperationsAsync<ForDeferredK>,
    Async<ForDeferredK> by DeferredK.async() {
    override val network: NetworkModule = NetworkModule()
    override val dao: DaoDatabase = DaoDatabase()
    override val ctx: CoroutineContext = Dispatchers.Default
  }

// Interlude: Retrofit, Dependency Injection and KEEP 87
