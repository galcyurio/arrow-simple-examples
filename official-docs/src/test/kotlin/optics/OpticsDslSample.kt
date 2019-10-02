package optics

import arrow.core.ListK
import arrow.core.k
import arrow.optics.dsl.every
import arrow.optics.extensions.listk.each.each
import arrow.optics.typeclasses.Each
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

}