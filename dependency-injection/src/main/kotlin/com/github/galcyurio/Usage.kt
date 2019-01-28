package com.github.galcyurio

import arrow.typeclasses.basics.DaoDatabase
import arrow.typeclasses.basics.NetworkModule
import kotlinx.coroutines.runBlocking

val requestOperations: RequestOperations = object : RequestOperations {
    override val network: NetworkModule = NetworkModule()
    override val dao: DaoDatabase = DaoDatabase()
}

class MyViewModel(private val dep: RequestOperations) : RequestOperations by dep

class MyActivity {
    private val myViewModel: MyViewModel = MyViewModel(requestOperations)

    fun onStart() {
        runBlocking { myViewModel.run { 1.fetchUser() } }
    }
}
