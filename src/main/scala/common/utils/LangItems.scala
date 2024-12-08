package common.utils

import chisel3._
import chisel3.util._
import scala.language.implicitConversions

object ImplicitCast {
    implicit def uintToBitPat(x: UInt): BitPat = BitPat(x)
}
