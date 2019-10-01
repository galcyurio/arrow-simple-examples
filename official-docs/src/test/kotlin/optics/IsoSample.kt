package optics

import arrow.core.Try
import arrow.core.Tuple2
import arrow.core.extensions.`try`.functor.functor
import arrow.core.toT
import arrow.optics.Iso
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
        val point = Point2D(1, 2)
        pointIsoTuple.modifyF(Try.functor(), point) { tuple ->
            Try { tuple.a / 2 toT (tuple.b / 2) }
        }
    }
}