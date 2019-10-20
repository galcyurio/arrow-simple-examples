package optics

import arrow.optics.optics

@optics
data class Pos(val x: Int, val y: Int) {
    companion object
}