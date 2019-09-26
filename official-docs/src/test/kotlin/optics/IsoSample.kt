package optics

import arrow.core.Tuple2
import arrow.core.toT
import arrow.optics.Iso
import org.junit.Test


class IsoSample {

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
        data class Point2D(val x: Int, val y: Int)

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
}