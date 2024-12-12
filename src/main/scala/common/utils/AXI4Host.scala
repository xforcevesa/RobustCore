package common.utils

import chisel3._
import chisel3.util._
import common.utils.AXI4Parameters.idBits

class AXI4MemoryTop(memDepth: Int) extends Module {
    val io = IO(new AXI4BridgeBundle)
    val host = Module(new AXI4Host(memDepth = 1024))
    val bridge = Module(new AXI4Bridge)
    host.io.axi <> bridge.io.axi
    host.reset := bridge.reset
    host.clock := bridge.clock
    io <> bridge.io.bridge
}

class AXI4Host(memDepth: Int) extends Module {
  val io = IO(new Bundle {
    val axi = new AXI4Reversed
  })

  // Simulate memory as a register array
  val mem = Mem(memDepth, UInt(AXI4Parameters.dataBits.W))

  // Registers for address, data, and control signals
  val awAddrReg = RegInit(0.U(AXI4Parameters.addrBits.W))
  val arAddrReg = RegInit(0.U(AXI4Parameters.addrBits.W))
  val writeDataReg = RegInit(0.U(AXI4Parameters.dataBits.W))
  val idReg = RegInit(0.U(idBits.W)) // Default ID
  val bvalidReg = RegNext(io.axi.w.valid && io.axi.w.last, init = true.B)
  val counter = RegInit(0.U(1.W))

  // Write address channel
  when(io.axi.aw.valid && io.axi.aw.ready) {
    awAddrReg := io.axi.aw.addr
  }
  io.axi.aw.ready := true.B

  // Write data channel
  when(io.axi.w.valid && io.axi.w.ready) {
    writeDataReg := io.axi.w.data
    when(io.axi.w.last) {
      mem.write(awAddrReg, writeDataReg)
    }
  }
  io.axi.w.ready := true.B

  // Write response channel
  io.axi.b.valid := bvalidReg
  io.axi.b.resp := 0.U // OKAY response
  io.axi.b.id := 0.U   // Default ID

  // Read address channel
  when(io.axi.ar.valid && io.axi.ar.ready) {
    arAddrReg := io.axi.ar.addr
  }
  io.axi.ar.ready := true.B

  // Read data channel
  val readDataReg = RegNext(mem.read(arAddrReg))
  io.axi.r.data := readDataReg
  io.axi.r.valid := RegNext(io.axi.ar.valid, init = true.B)
  io.axi.r.resp := 0.U // OKAY response
  io.axi.r.last := true.B // Single-beat read
  io.axi.r.id := idReg  // ID

  withClock(clock) {
    // Simulate memory initialization
    when(reset.asBool) {
        for (i <- 0 until memDepth) {
            mem.write(i.U, 0.U)
        }
    }.otherwise {
        idReg := Cat(idReg(idBits-2, 1), !idReg(0))
        counter := !counter
        when(counter === 0.U) {
            bvalidReg := !bvalidReg
        }
    }
  }
}
