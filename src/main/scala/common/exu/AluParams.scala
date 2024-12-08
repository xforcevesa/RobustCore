package common.exu

import chisel3._
import chisel3.util._

class AluFuncCode {
    val SZ_ALU_FN = 4
    def FN_X    = BitPat("b????")
    def FN_ADD  = 0.U(SZ_ALU_FN.W)  // 0b0000
    def FN_SUB  = 1.U(SZ_ALU_FN.W)  // 0b0001
    def FN_AND  = 2.U(SZ_ALU_FN.W)  // 0b0010
    def FN_NOR  = 3.U(SZ_ALU_FN.W)  // 0b0011
    def FN_OR   = 4.U(SZ_ALU_FN.W)  // 0b0100
    def FN_XOR  = 5.U(SZ_ALU_FN.W)  // 0b0101
    def FN_SL   = 6.U(SZ_ALU_FN.W)  // 0b0110
    def FN_SRA  = 7.U(SZ_ALU_FN.W)  // 0b0111
    def FN_SRL  = 8.U(SZ_ALU_FN.W)  // 0b1000
    def FN_ANDN = 9.U(SZ_ALU_FN.W)  // 0b1011
    def FN_ORN  = 10.U(SZ_ALU_FN.W) // 0b1100
    def FN_SLT  = 11.U(SZ_ALU_FN.W) // 0b1011
    def FN_SLTU = 13.U(SZ_ALU_FN.W) // 0b1101

    def isSub(cmd: UInt) = cmd(0)
    def isCmp(cmd: UInt) = cmd >= FN_SLT
    def cmpUnsigned(cmd: UInt) = cmd(2)
}

object AluFuncCode {
    def apply() = new AluFuncCode()
}
