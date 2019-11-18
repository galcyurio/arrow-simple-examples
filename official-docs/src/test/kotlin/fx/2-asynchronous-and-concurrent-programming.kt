package fx

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.Fiber
import arrow.unsafe
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * # Asynchronous & Concurrent Programming
 *
 * Arrow Fx는 콜백없이 매우 간결한 프로그램을 생성하여 `!effect`와
 * 비동기 프로그래밍을 위한 직접 구문의 이점을 제공합니다.
 * 이를 통해 Type과 런타임에서 effect 제어를 유지하고 결과를 왼쪽에 바인딩하면서
 * 비동기 및 동시 작업에 대해 직접적인 스타일의 구문을 사용할 수 있습니다.
 * 결과 표현식은 대부분의 OOP 및 Java 프로그래머가 이미 익숙한 것과 동일한 구문을 사용하여
 * 직접적인 blocking imperative 스타일을 사용합니다.
 */
class AsynchronousAndConcurrentProgramming {

    /**
     * ## Dispatchers and Contexts
     *
     * 실행 컨텍스트를 전환하면서 effect를 수행하는 것을 쉽지 않습니다.
     */
    @Test
    fun `Dispatchers and Contexts`() {
        val contextA = newSingleThreadContext("A")
        suspend fun printThreadName(): Unit = println(Thread.currentThread().name)
        val program = IO.fx {
            continueOn(contextA)
            !effect { printThreadName() }
            continueOn(dispatchers().default())
            !effect { printThreadName() }
        }
        unsafe { runBlocking { program } }
        // continueOn 외에도 Arrow Fx를 사용하면 필요한 모든 함수에서 실행 컨텍스트를
        // override할 수 있습니다.
    }

    /**
     * ## Fibers
     *
     * [Fiber]는 동시에 시작되거나 결합될 수 있는 동시적인 데이터 타입의 순수한 결과를 나타냅니다.
     *
     * fiber들을 만들 때 `join()`을 통해 deferred non-blocking 결과를 얻어서 destructing할 수 있습니다.
     *
     * `dispatchers().default()`는 IO와 같은 모든 동시 데이터 타입에서 사용가능하며 fx 블록에서 직접 사용할
     * 수 있는 실행 컨텍스트입니다.
     *
     * 여기서는 모든 경우에 새 스레드를 만들 수 없는 [Fiber] 및 Dispatcher를 사용하므로 출력된 스레드 이름이
     * 다르다고 보장할 수 없습니다.
     */
    @Test
    fun Fibers() {
        suspend fun threadName(): String = Thread.currentThread().name
        val program = IO.fx {
            val fiberA = !effect { threadName() }.fork(dispatchers().default())
            val fiberB = !effect { threadName() }.fork(dispatchers().default())
            val threadA = !fiberA.join()
            val threadB = !fiberB.join()
            !effect { println(threadA) }
            !effect { println(threadB) }
        }
        unsafe { runBlocking { program } }
    }

    /**
     * ## Parallelization & Concurrency
     *
     * Arrow Fx는 내장된 `parMapN`, `parTraverse`, `parSequence`를 통해 사용자가 effect를 병렬로 디스패치할 수 있으며
     * 래퍼없이 non-blocking 결과를 받거나 직접 구문을 사용할 수 있습니다.
     */
    fun dummy1() {}

    /**
     * ## parMapN
     *
     * `parMapN`을 사용하면 N# effect를 병렬 non-blocking 으로 실행하여 모든 결과가 완료될 때 까지
     * 기다린 다음 결과에 최종 변환을 적용하는 사용자 제공 함수에 위임할 수 있습니다.
     * 함수가 유효한 반환 값을 지정하면 반환된 non-blocking 값이 왼쪽에 어떻게 바인딩되는지 확인할 수 있습니다.
     */
    @Test
    fun parMapN() {
        suspend fun threadName(): String = Thread.currentThread().name
        data class ThreadInfo(val threadA: String, val threadB: String)

        val program = IO.fx {
            val (threadA, threadB) = !dispatchers().default().parMapN(
                    effect { threadName() },
                    effect { threadName() },
                    ::ThreadInfo
            )
            !effect { println(threadA) }
            !effect { println(threadB) }
        }
        unsafe { runBlocking { program } }
    }

    /**
     * ## parTraverse
     *
     * `parTraverse`는 각 effect 결과에 대해 사용자가 제공한 함수를 적용한 다음 변환된 모든 결과를
     * `List<B>`에 수집할 때 `Iterable<suspend () -> A>`가 포함된 effect를 병렬로 반복합니다.
     */
    @Test
    fun parTraverse() {
        suspend fun threadName(i: Int): String = "$i on ${Thread.currentThread().name}"
        val program = IO.fx {
            val result = !listOf(1, 2, 3).parTraverse { i ->
                effect { threadName(i) }
            }
            !effect { println(result) }
        }
        unsafe { runBlocking { program } }
    }

    /**
     * `parSequence`는 `Iterable<suspend () -> A`의 모든 effect를 non-blocking 으로 병렬로 적용한 다음
     * 변환된 모든 결과를 수집하여 `List<B>`에 반환합니다.
     */
    @Test
    fun parSequence() {
        suspend fun threadName(): String = Thread.currentThread().name
        val program = IO.fx {
            val result = listOf(
                    effect { threadName() },
                    effect { threadName() },
                    effect { threadName() }
            ).parSequence().bind()
            !effect { println(result) }
        }
        unsafe { runBlocking { program } }
    }
}