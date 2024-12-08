package common

import chisel3._
import chisel3.util._

class RegfileReadPort(val xlen: Int) extends Bundle {
    val addr = Input(UInt(log2Ceil(xlen).W))
    val data = Output(UInt(xlen.W))
}

class RegfileWritePort(val xlen: Int) extends Bundle {
    val addr = Input(UInt(log2Ceil(xlen).W))
    val data = Input(UInt(xlen.W))
    val we = Input(Bool())
}

class RegfileIO(val xlen: Int) extends Bundle {
    val r1 = new RegfileReadPort(xlen)
    val r2 = new RegfileReadPort(xlen)
    val w = new RegfileWritePort(xlen)
}

class Regfile(val xlen: Int = 32) extends Module {
    val io = IO(new RegfileIO(xlen))
    val regs = RegInit(VecInit(Seq.fill(xlen)(0.U(xlen.W))))
    when(io.w.we) {
        regs(io.w.addr) := io.w.data
    }
    io.r1.data := regs(io.r1.addr)
    io.r2.data := regs(io.r2.addr)
}
