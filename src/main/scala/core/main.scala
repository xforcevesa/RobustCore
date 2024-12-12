package core

import chisel3._
import chisel3.util._
import common.decode.la32r.DecodeUnit
import common.utils.AXI4Bridge
import common.utils.AXI4Host
import common.utils.AXI4MemoryTop

object RobustDecoder extends App {
  emitVerilog(new DecodeUnit)
}

object RobustAXIBridge extends App {
  emitVerilog(new AXI4Bridge)
}

object RobustAXIHost extends App {
  emitVerilog(new AXI4MemoryTop(memDepth = 1024))
}
