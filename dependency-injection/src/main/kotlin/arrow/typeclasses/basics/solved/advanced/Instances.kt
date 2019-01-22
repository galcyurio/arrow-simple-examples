package arrow.typeclasses.basics.solved.advanced

import arrow.extension
import arrow.typeclasses.basics.*

@extension
interface NetworkModuleNetworkFetcher : NetworkFetcher<NetworkModule> {
  companion object {
    private val nm = NetworkModule()
  }

  override fun fetch(id: Int, headers: Map<String, String>): UserDto =
    nm.fetch(id, headers)

  override fun fetchAsync(id: Int, headers: Map<String, String>, fe: (Throwable) -> Unit, f: (UserDto) -> Unit) =
    nm.fetchAsync(id, headers, fe, f)
}

// Generated by @extension
fun NetworkModule.Companion.networkFetcher(): NetworkFetcher<NetworkModule> =
  object : NetworkModuleNetworkFetcher {}

@extension
interface DaoDatabaseDaoFetcher : DaoFetcher<DaoDatabase> {

  companion object {
    private val dao = DaoDatabase()
  }

  override fun query(s: Query): UserDao =
    dao.query(s)

  override fun queryAsync(s: Query, fe: (Throwable) -> Unit, f: (UserDao) -> Unit) =
    dao.queryAsync(s, fe, f)
}

// Generated by @extension
fun DaoDatabase.Companion.daoFetcher(): DaoFetcher<DaoDatabase> =
  object : DaoDatabaseDaoFetcher {}

// I've seen @extension before...isn't it in KEEP 87?
