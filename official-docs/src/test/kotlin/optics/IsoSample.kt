package optics

import arrow.core.*
import arrow.core.extensions.`try`.functor.functor
import arrow.core.extensions.either.functor.functor
import arrow.optics.Iso
import arrow.optics.PIso
import arrow.optics.optics
import arrow.optics.toNullable
import kotlinx.coroutines.runBlocking
import org.junit.Test


class IsoSample {
    data class Point2D(val x: Int, val y: Int)

    val pointIsoTuple: Iso<Point2D, Tuple2<Int, Int>> = Iso(
        get = { point -> point.x toT point.y },
        reverseGet = { tuple -> Point2D(tuple.a, tuple.b) }
    )

    /**
     * [Iso]는 유형 S와 A 사이의 동형사상, 즉 데이터 클래스와
     * TupleN으로 표시되는 속성 사이의 동형을 정의하는 무손실의 뒤집을 수 있는 optic 입니다.
     *
     * [Iso]는 isomorphism을 나타내는 get 및 reverseGet 함수들의 쌍으로 볼 수 있습니다.
     * 따라서 `Iso<S,A>`는 두 개의 getter `get: (S) -> A` 그리고 `reverseGet: (A) -> S` 라고 말할 수 있습니다.
     * 여기서 `S`는 Iso의 source 이며 `A`를 초점(focus) 또는 대상(target)이라고 합니다.
     *
     * 간단한 구조의 `Point2D`는 `Tuple2<Int, Int>`와 동일하므로
     * `Iso<Point2D, Tuple2<Int, Int>>`를 만들 수 있습니다.
     */
    @Test
    fun iso1() {
        val pointIsoTuple: Iso<Point2D, Tuple2<Int, Int>> = Iso(
            get = { point -> point.x toT point.y },
            reverseGet = { tuple -> Point2D(tuple.a, tuple.b) }
        )
        val p = Point2D(5, 10)
        val t = pointIsoTuple.get(p)
        println(t)

        val p2 = pointIsoTuple.reverseGet(t)
        println(p2)
    }
//    Tuple2(a=5, b=10)
//    Point2D(x=5, y=10)

    /**
     * `Iso<Point2D, Tuple2<Int, Int>>`가 주어지면 우리는 `Iso<Tuple2<Int, Int>, Point2D>`도 가질 수 있습니다.
     * 왜냐하면 `Iso`는 동등한 구조들 사이의 동형(Isomorphism)을 나타내기 때문에 우리는 그것을 뒤집을 수 있으니까요.
     */
    fun iso2() {
        val reversedIso: Iso<Tuple2<Int, Int>, Point2D> = pointIsoTuple.reverse()
    }

    /**
     * [Iso]를 사용하여 focus `A` 에서 작동하던 함수로 source `S`를 변경할 수 있습니다.
     */
    @Test
    fun iso3() {
        val point = Point2D(3, 5)
        val addFive: (Tuple2<Int, Int>) -> Tuple2<Int, Int> = { tuple2 ->
            (tuple2.a + 5) toT (tuple2.b + 5)
        }
        val modified = pointIsoTuple.modify(point, addFive)
        println("modified = $modified")

        /** 함수 `(A) -> A`를 함수 `(S) -> S`로 lift 할 수 있습니다.  */
        val liftedAddFive: (Point2D) -> Point2D = pointIsoTuple.lift(addFive)
        val modified2 = liftedAddFive(point)
        println("modified2 = $modified2")
    }
//    modified = Point2D(x=8, y=10)
//    modified2 = Point2D(x=8, y=10)n

    /** Functor 매핑으로 동일한 작업을 수행할 수 있습니다. */
    @Test
    fun iso4() {
        val point = Point2D(5, 10)
        pointIsoTuple.modifyF(Try.functor(), point) { tuple ->
            Try { tuple.a / 2 toT (tuple.b / 2) }
        }.also { println(it) }

        // ===== Try 대신 Either 사용 =====
        pointIsoTuple.modifyF(Either.functor(), point) { tuple ->
            runBlocking {
                Either.catch { tuple.a / 2 toT (tuple.b / 2) }
            }
        }.also { println(it) }
    }
//    Success(value=Point2D(x=2, y=5))
//    Right(b=Point2D(x=2, y=5))

    @Test
    fun iso5() {
        val point = Point2D(5, 10)
        val liftF = pointIsoTuple.liftF(Try.functor()) { tuple ->
            Try { (tuple.a / 2) toT (tuple.b / 0) }
        }
        liftF(point).also { println(it) }

        // ===== Try 대신 Either 사용 =====
        val liftF2 = pointIsoTuple.liftF(Either.functor()) { tuple ->
            runBlocking {
                Either.catch { (tuple.a / 2) toT (tuple.b / 0) }
            }
        }
        liftF2(point).also { println(it) }
    }
//    Failure(exception=java.lang.ArithmeticException: / by zero)
//    Left(a=java.lang.ArithmeticException: / by zero)

    /**
     * ## Composition (구성)
     *
     * [Iso]들을 구성함으로써 새로운 추가적인 [Iso]를 정의하지 않고 생성할 수 있습니다.
     * 서로 다른 `API`들과 프레임워크들을 다룰 때 우리는 종종
     * [Point2D], [Tuple2], [Pair] [Coord] 등과 같은 여러 개의 동일하지만
     * 다른 구조를 가진 것들을 사용합니다.
     */
    fun dummy1() {}

    data class Coord(val xAxis: Int, val yAxis: Int)

    val pairIsoCoord: Iso<Pair<Int, Int>, Coord> = Iso(
        get = { pair -> Coord(pair.first, pair.second) },
        reverseGet = { coord -> coord.xAxis to coord.yAxis }
    )
    val tupleIsoPair: Iso<Tuple2<Int, Int>, Pair<Int, Int>> = Iso(
        get = { tuple -> tuple.a to tuple.b },
        reverseGet = { pair -> pair.first toT pair.second }
    )

    /**
     * [pointIsoTuple], [pairIsoCoord] 그리고 [tupleIsoPair]를 구성함으로써
     * 우리는 [Point2D], [Tuple2], [Pair] 그리고 [Coord]를 서로 바꿔 사용할 수 있습니다.
     *
     * [Iso]와 함수들을 구성하면 함수의 입력 또는 출력 유형을 변경하는데 유용합니다.
     * `Iso<A?, Option<A>>`는 arrow-optics 에서 `nullableToOption()`으로 사용할 수 있습니다.
     */
    @Test
    fun iso6() {
        val unknownCode: (String) -> String? = { value ->
            "unknown $value"
        }
        val nullableOptionIso = Option.toNullable<String>()
        val func = unknownCode andThen nullableOptionIso::reverseGet
        func("Retrieve an Option").also { println(it) }
    }

    /**
     * [Iso]는 모든 `optics`와 구성할 수 있으며 다음과 같은 `optics`로 구성됩니다.
     *
     * ```
     * |     | Iso | Lens | Prism | Optional | Getter | Setter | Fold | Traversal |
     * |-----|-----|------|-------|----------|--------|--------|------|-----------|
     * | Iso | Iso | Lens | Prism | Optional | Getter | Setter | Fold | Traversal |
     * ```
     */
    fun dummy2() {}

    /**
     * ## [Iso] 만들기
     *
     * boilerplate 코드를 피하려면 `@optics` 어노테이션을 사용해서 데이터 클래스와
     * 2~10개의 매개변수를 가진 `TupleN`의 [Iso]를 생성할 수 있습니다.
     * [Iso]는 `companion object`의 확장 변수 `val T.Companion.iso`를 통해서 생성될 수 있습니다.
     */
    fun dummy3() {}

    // Pos 클래스는 main sourceSet 에 있음
    val posIso = Pos.iso

    /**
     * Polymorphic isos
     *
     * 다형성과 같은 구조를 처리할 때 [PIso]의 focus 유형(결과적으로 생성된 유형 A)을
     * 변경할 수 있는 다형성 [Iso]를 만들 수 있습니다.
     *
     * 우리에게 `Tuple2<A, B>`와 `Pair<A, B>` 있으면
     * `get: (Tuple2<A, B>) -> Pair<A, B>`와 `reverseGet: (Tuple2<C, D>) -> Pair<C, D>`를 가지는
     * 다형성 [PIso]를 생성할 수 있습니다.
     */
    fun <A, B, C, D> tuple2(): PIso<Tuple2<A, B>, Pair<C, D>, Pair<A, B>, Tuple2<C, D>> = PIso(
        get = { tuple -> tuple.a to tuple.b },
        reverseGet = { tuple -> tuple.a to tuple.b }
    )

    /**
     * Above defined [PIso] can lift a `reverse` function of `(Pair<A, B>) -> Tuple2<B, A>` to a function `(Tuple2<A, B>) -> Pair<B, A>`.
     */
    @Test
    fun iso7() {
        val reverseTupleAsPair: (Tuple2<Int, String>) -> Pair<String, Int> =
            tuple2<Int, String, String, Int>().lift { it.second toT it.first }
        val reverse: Pair<String, Int> = reverseTupleAsPair(5 toT "five")
        println(reverse)
    }
//    (five, 5)
}