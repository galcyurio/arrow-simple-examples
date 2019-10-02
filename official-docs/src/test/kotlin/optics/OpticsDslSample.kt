package optics

import arrow.core.ListK
import arrow.core.MapK
import arrow.core.k
import arrow.optics.dsl.at
import arrow.optics.dsl.every
import arrow.optics.dsl.index
import arrow.optics.dsl.some
import arrow.optics.extensions.listk.each.each
import arrow.optics.extensions.listk.index.get
import arrow.optics.extensions.listk.index.index
import arrow.optics.extensions.mapk.at.at
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.Each
import arrow.optics.typeclasses.Index
import org.junit.Test

/**
 * main SourceSet 의 `OpticsDslSample.kt` 파일 참고하세요.
 */
class OpticsDslSample {

    /**
     * Arrow는 Optics DSL을 제공하여 사용 편의성과 가독성을 향상시키기 위해 서로 다른 Optics를 구성합니다.
     * boilerplate 코드를 피하기 위해 Arrow는 @optics 어노테이션을 사용하여 속성과 같은 dsl을 생성합니다.
     */
    fun dummy1() {}

    /**
     * DSL은 같은 패키지의 데이터 클래스에 생성되며 클래스의 `Companion`을 통해서 사용할 수 있습니다.
     */
    @Test
    fun opticsDsl1() {
        val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val optional = Employee.company.address.street.name
        val actual = optional.modify(john, String::toUpperCase)
        println(actual)
    }
//    Employee(name=John Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=42, name=LAMBDA STREET))))

    /**
     * Arrow는 boilerplate 코드를 줄이거나 가독성을 향상시키는데 도움이 되는 `sealed class`에 대해 DSL을 생성할 수도 있습니다.
     *
     * `(HttpError) -> HttpError` 타입의 함수 `f`가 있고 [NetworkResult]에서 호출하려고 한다고 가정합니다.
     */
    @Test
    fun opticsDsl2() {
        val networkResult = HttpError("Boom!")
        val f: (String) -> String = String::toUpperCase

        @Suppress("USELESS_IS_CHECK")
        when (networkResult) {
            is HttpError -> networkResult.copy(f(networkResult.message))
            else -> networkResult
        }.also { println(it) }

        // 생성된 DSL을 통해 위 코드를 다음과 같이 작성할 수 있습니다.
        NetworkResult.networkError.httpError.message.modify(networkResult, f)
                .also { println(it) }
    }
//    HttpError(message=BOOM!)
//    HttpError(message=BOOM!)

    /**
     * ## Each
     *
     * [Each] 구조 `S`에 집중하고 모든 초점(foci) `A`를 보는데 사용될 수 있습니다.
     * 다음 예제에서는 `Employees` 안에 있는 `Employee`에 초점을 둡니다.
     */
    @Test
    fun opticsDsl3() {
        val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val jane = Employee("Jane Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val employees = Employees(listOf(john, jane).k())
        Employees.employees.every(ListK.each()).company.address.street.name.modify(employees, String::capitalize)
                .also { println(it) }
    }
//    Employees(employees=ListK(list=[Employee(name=John Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=42, name=Lambda street)))), Employee(name=Jane Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=42, name=Lambda street))))]))

    /**
     * ## At
     *
     * [At]은 구조 `S`에 대해 인덱스 `I`에서 `A`에 초점을 맞추는 데 사용할 수 있습니다.
     */
    @Test
    fun opticsDsl4() {
        val db = Db(mapOf(
                1 to "one",
                2 to "two",
                3 to "three"
        ).k())

        Db.content.at(MapK.at(), 2).some.modify(db, String::reversed)
                .also { println(it) }
    }
//    Db(content=MapK(map={1=one, 2=owt, 3=three}))

    /**
     * ## Index
     *
     * [Index]는 구조 `S`에서 인덱스 `I`에 의해 `A`를 인덱스할 수 있습니다.
     * 즉, 인덱스 위치별 `List<Employee>` 또는 키 `K`별 `Map<K, V>` 입니다.
     */
    @Test
    fun opticsDsl5() {
        val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val jane = Employee("Jane Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val employees = Employees(listOf(john, jane).k())
        Employees.employees.index(ListK.index(), 0).company.address.street.name.modify(employees, String::capitalize)
                .also { println(it) }
    }
//    Employees(employees=ListK(list=[Employee(name=John Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=42, name=Lambda street)))), Employee(name=Jane Doe, company=Company(name=Kategory, address=Address(city=Functional city, street=Street(number=42, name=lambda street))))]))

    /**
     * [Index]의 범위에서는 인스턴스를 지정할 필요가 없으므로 `operator fun get` 구문을 사용할 수 있습니다.
     */
    @Test
    fun opticsDsl6() {
        val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val jane = Employee("Jane Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
        val employees = Employees(listOf(john, jane).k())

//        ListK.index<Employee>().run {
//            Employees.employees[0].company.address.street.name.getOption(employees)
//        }

        // 또는 get 함수를 import 하면 바로 사용할 수 있습니다.
        Employees.employees[0].company.address.street.name.getOption(employees)
                .also { println(it) }

        // `Index`는 `Optional`을 반환하므로 안전합니다.
        Employees.employees[2].company.address.street.name.getOption(employees)
                .also { println(it) }
    }
//    Some(lambda street)
//    None
}