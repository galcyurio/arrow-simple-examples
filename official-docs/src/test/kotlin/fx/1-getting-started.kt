package fx

import arrow.fx.IO
import arrow.fx.extensions.fx
import org.junit.Test

/**
 * # Arrow Fx. Typed FP for masses
 *
 * Arrow Fx는 코틀린에서 효과적이고 다형성 프로그래밍을 first class로 만드는
 * 차세대 Typed Fx Effects 라이브러리입니다.
 * 그리고 코틀린의 suspend 시스템의 확장(extension)으로 작동합니다.
 *
 * 라이브러리는 순수성과 참조 투명성 그리고 명령형 문법을 제공하여
 * 코틀린에서 Typed FP를 가능하게 합니다.
 * Fx는 Typed Pure 함수형 프로그램을 만드는데 재미있고 쉬운 도구입니다.
 *
 * Arrow Fx 프로그램은 Arrow Effects IO, KotlinX Coroutines Deffered, Rx2 Observable 등등의
 * 여러가지 지원되는 프레임워크와 런타임에서 수정되지 않은 상태로 동작합니다.
 */
class GettingStarted {

    /**
     * # Pure Functions, Side Effects, and Program Execution
     *
     * ## Pure & Referentially Transparent Functions
     *
     * 순수 함수는 동일한 입력이 주어지면 동일한 출력을 일관되게 반환하는 함수입니다.
     * 또한 결정지어진 행동만을 나타내며 외부적으로 관찰 가능한 영향을 일으키지 않습니다.
     * 이러한 속성을 우리는 참조 투명성(referential transparency)이라고 부릅니다.
     *
     * 참조 투명성을 통해 프로그램의 다른 부분을 따로 따로 추론할 수 있습니다.
     *
     * 코틀린에서 순수함수를 만들어 봅시다.
     */
    fun helloWorld(): String = "Hello World"

    /**
     * [helloWorld]를 호출하면 동일한 입력이 주어지면 동일한 출력을 일관되게 반환하고
     * 외부에서 눈에 띄는 변화를 일으키지 않기 때문에 [helloWorld]는 순수하고 참조적으로
     * 투명한 함수라고 말할 수 있습니다.
     */
    fun dummy1() {}

    /**
     * ## Side effects
     *
     * side effect는 값을 반환하는 것 외에도 함수 외부에서 관찰할 수 있는 것을 말합니다.
     *
     * 네트워크 또는 파일 IO, stream 사용 및 일반적으로 [Unit]을 반환하는 모든 함수는
     * side effect를 일으킬 가능성이 높습니다.
     * 왜냐하면 [Unit]을 반환한다는 건 함수가 어떠한 것도 반환하지 않지만 프로그램에
     * 영향을 끼치는 어떠한 일을 수행하기 때문입니다.
     *
     * Arrow Fx에서는 `suspend fun` 키워드가 side effect를 일으키는 함수임을 의미합니다.
     *
     * 아래의 예제에서 `println(a: Any): Unit`은 side effect 입니다.
     * 호출될 때마다 `System.out` 스트림을 사용해 관찰가능한 side effect를 일으키고
     * 쓸모없는 [Unit]을 반환하기 때문입니다.
     *
     * side effect를 `suspend` 키워드로 나타내면 코틀린 컴파일러는 순수한 환경에서
     * 통제되지 않은 side effect를 적용하지 않도록 합니다.
     */
    @Test
    fun `Side effect`() {
        fun helloWorld(): String = "Hello World"
        suspend fun sayHello(): Unit = println(helloWorld())
        // sayHello() // 주석제거하면 에러!
    }
    // javax.script.ScriptException: error: suspend function 'sayHello' should be called only from a coroutine or another suspend function
    // sayHello()
    // ^

    /**
     * 위의 예제는 컴파일에러가 일어납니다.
     *
     * 코틀린 컴파일러는 순수한 환경에서 suspend 함수의 호출을 허용하지 않습니다.
     * 왜냐하면 suspend 함수는 내부에 다른 suspend 함수 또는 continuation을 포함해야하기 때문입니다.
     *
     * continuation은 코틀린 인터페이스입니다.
     * `Continuation<A>`은 suspended effect를 실행하여 발생하는 성공 및 오류를
     * 처리하는 방법을 알고 있음을 증명합니다.
     *
     * 이것을 코틀린 컴파일러의 내장된 굉장한 기능이며 이미 Typed FP에 이상적인 선택이지만
     * 다른 방법이 없는 것은 아닙니다.
     *
     * 코틀린 컴파일러와 Arrow Fx가 직접 구문과 모나드를 사용하여 어떻게 여러 함수형 관용구들을
     * 제거할 수 있었는지 궁금하다면 계속해서 이 글을 읽으세요.
     */
    fun dummy2() {}

    /**
     * ### suspend composition
     * 다른 일시 중단된(suspended) side effect가 있는 경우 일시 중단된 side effect를 applying 하고 composing 할 수 있습니다.
     * 아래 예제에서 `sayHello`와 `sayGoodBye`는 모두 suspend function 이기 때문에 `greet` 내에서 사용할 수 있습니다.
     */
    @Test
    fun `suspend composition`() {
        suspend fun sayGoodBye(): Unit = println("Good bye World!")
        suspend fun sayHello(): Unit = println("Hello World")
        suspend fun greet() {
            sayHello() // this is ok because
            sayGoodBye() // `greet` is also `suspend`
        }
    }

    /**
     * ### fx composition
     * side effect는 `fx` 블록에서 구성(compose)하여 순수한 값으로 바뀔 수 있습니다.
     */
    fun dummy3() {}

    /**
     * ### effect를 활용하여 side effect를 순수한 값으로 바꾸기
     *
     * `effect`는 effect를 감싸서 `suspend () -> A`와 같은 유저가 정의한 side effect를
     * `IO<A>` 값으로 변경하여 순수한 값으로 바꾸어 줍니다.
     *
     * `effect`를 이용해서 suspended side effect를 [IO] 값으로 잡아두면 effect를 적용할
     * 준비가 될 때까지 어느 곳이든 넘겨주거나 구성(compose)할 수 있습니다.
     * 이 시점에서 `greet`와 `effect`는 지연된 값이므로 아무 일도 벌어지지 않습니다.
     */
    @Test
    fun `Turning side effects into pure values with effect`() {
        suspend fun sayHello(): Unit = println("Hello World")
        suspend fun sayGoodBye(): Unit = println("Good bye World!")
        fun greet(): IO<Unit> = IO.fx {
            val pureHello = effect { sayHello() }
            val pureGoodBye = effect { sayGoodBye() }
        }
    }

    /**
     * ### !effect를 이용해 side effect 적용하기
     *
     * `!` 연산자를 이용해 side effect를 적용할 수 있습니다.
     * `effect`를 이용해 side effect를 순수하게 만들었다면 non-blocking 방식으로 값을 꺼내올 수 있습니다.
     * `!effect`는 suspended side effect를 가져와 continuation context가 실행되기 전에 이를 제어하도록 만듭니다.
     * 이는 effect composition이 순수하고 참조적으로 투명하며 끝(edge)에서만 실행되도록 합니다.
     *
     * 이전 예제에서 `greet()` 함수를 실행하는 것은 래핑된 지연된 값이 반환되므로 아무런 영향을 끼치지 않습니다.
     * 이 함수를 호출해도 효과(effect)가 발생하지 않기 때문에 우리는 `greet` 함수가 effects application을
     * 참조하였더라도 순수하고 참조적으로 투명하다는 것을 확신할 수 있습니다.
     */
    @Test
    fun `Applying side effects with !effect`() {
        suspend fun sayHello(): Unit = println("Hello World")
        suspend fun sayGoodBye(): Unit = println("Good bye World!")
        fun greet(): IO<Unit> = IO.fx {
            !effect { sayHello() }
            !effect { sayGoodBye() }
        }

        val io = greet()

        println("===== Nothing happens before unsafeRunAsync() =====")
        io.unsafeRunAsync { println(it) }
    }

    /**
     * `effect` 또는 `!effect`로 구분되지 않은 `fx` 블록에서 side effect를 실행하려고 하면 컴파일 오류가 발생합니다.
     * Arrow는 명시적으로 effect를 적용하도록 사용법을 강제합니다.
     */
    fun dummy4() {
        suspend fun sayHello(): Unit = println("Hello World")
        suspend fun sayGoodBye(): Unit = println("Good bye World!")
        fun greet(): IO<Unit> = IO.fx {
            // sayHello() // 주석 풀면 에러!
            // sayGoodBye()
        }
    }

    /**
     * ### 기존에 존재하는 데이터 타입들에 적용하기
     *
     * [IO]와 같은 일반 데이터 유형을 사용하는 composition은 `effect` 블록과 같은 방식으로 `fx` 블록을 통해 가능합니다.
     * 게다가 이전에 `!`를 이용해 사용했던 확장 함수인 `bind()` 또한 사용할 수 있습니다.
     */
    @Test
    fun `Applying existing datatypes`() {
        fun sayInIO(s: String): IO<Unit> = IO { println(s) }
        fun greet(): IO<Unit> = IO.fx {
            sayInIO("Hello World").bind()
        }

        val io = greet()

        println("===== Nothing happens before unsafeRunAsync() =====")
        io.unsafeRunAsync { println(it) }
    }
}