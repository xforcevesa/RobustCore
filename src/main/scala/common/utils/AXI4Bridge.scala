package common.utils

import chisel3._
import chisel3.util._
import dataclass.data

object AXI4BridgeStates {
  val read_request_empty = 1.U
  val read_request_ready = 2.U

  val read_respond_empty = 0.U
  val read_respond_transfer = 1.U

  val write_request_empty = "b000".U(3.W)
  val write_data_wait = "b101".U(3.W)
  val write_data_transform = "b100".U(3.W)
  val write_wait_b = "b110".U(3.W)
  val write_addr_ready = "b001".U(3.W)
  val write_data_ready = "b010".U(3.W)
  val write_all_ready = "b011".U(3.W)
}

class AXIBridgeReadRequestInput(addr_width: Int, data_width: Int)
    extends Bundle {
  // Read request send signal
  val rd_req = Bool()
  // Read request type signal
  val rd_type = UInt(3.W)
  // Read request address signal
  val rd_addr = UInt(addr_width.W)
}

class AXIBridgeReadRequestOutput(data_width: Int) extends Bundle {
  // Read request ready signal
  val rdy = Bool()
  // Read request valid signal (after handshake)
  val ret_valid = Bool()
  // Read request concluded signal
  val ret_last = Bool()
  // Read data
  val ret_data = UInt(data_width.W)
}

class AXIBridgeWriteRequestInput(addr_width: Int, data_width: Int)
    extends Bundle {
  // Write request send signal
  val req = Bool()
  // Write request type signal
  val rtype = UInt(3.W)
  // Write request address signal
  val addr = UInt(addr_width.W)
  // Write request WSTRB signal
  val wstrb = UInt(4.W)
  // Write request data signal
  val data = UInt(data_width.W)
}

class AXIBridgeWriteRequestOutput(data_width: Int) extends Bundle {
  // Write request ready signal
  val rdy = Bool()
}

class AXI4BridgeBundle extends Bundle {
  val inst = new Bundle {
    val in = Input(new AXIBridgeReadRequestInput(32, 32))
    val out = Output(new AXIBridgeReadRequestOutput(32))
  }
  val data = new Bundle {
    val in = Input(new AXIBridgeReadRequestInput(32, 32))
    val out = Output(new AXIBridgeReadRequestOutput(32))
  }
  val inst_wr = new Bundle {
    val in = Input(new AXIBridgeWriteRequestInput(32, 32))
    val out = Output(new AXIBridgeWriteRequestOutput(32))
  }
  val data_wr = new Bundle {
    val in = Input(new AXIBridgeWriteRequestInput(32, 128))
    val out = Output(new AXIBridgeWriteRequestOutput(128))
  }
}

class AXI4ReversedBridgeBundle extends Bundle {
  val inst = new Bundle {
    val in = Output(new AXIBridgeReadRequestInput(32, 32))
    val out = Input(new AXIBridgeReadRequestOutput(32))
  }
  val data = new Bundle {
    val in = Output(new AXIBridgeReadRequestInput(32, 32))
    val out = Input(new AXIBridgeReadRequestOutput(32))
  }
  val inst_wr = new Bundle {
    val in = Output(new AXIBridgeWriteRequestInput(32, 32))
    val out = Input(new AXIBridgeWriteRequestOutput(32))
  }
  val data_wr = new Bundle {
    val in = Output(new AXIBridgeWriteRequestInput(32, 128))
    val out = Input(new AXIBridgeWriteRequestOutput(128))
  }
}

class AXI4Bridge extends Module {
  import AXI4BridgeStates._
  val io = IO(new Bundle {
    val axi = new AXI4
    val bridge = new AXI4BridgeBundle
    val write_buffer_empty = Output(Bool())
  })

  val araddr = RegNext(io.axi.ar.addr)
  val arlen = RegNext(io.axi.ar.len)
  val arsize = RegNext(io.axi.ar.size)
  val arid = RegNext(io.axi.ar.id)
  val arvalid = RegNext(io.axi.ar.valid)

  val rready = RegNext(io.axi.r.ready)
  val awaddr = RegNext(io.axi.aw.addr)
  val awlen = RegNext(io.axi.aw.len)
  val awsize = RegNext(io.axi.aw.size)
  val awvalid = RegNext(io.axi.aw.valid)

  val wdata = RegNext(io.axi.w.data)
  val wstrb = RegNext(io.axi.w.strb)
  val wvalid = RegNext(io.axi.w.valid)
  val wlast = RegNext(io.axi.w.last)
  val bready = RegNext(io.axi.b.ready)

  io.axi.ar.addr := araddr
  io.axi.ar.len := arlen
  io.axi.ar.size := arsize
  io.axi.ar.id := arid
  io.axi.ar.valid := arvalid

  io.axi.r.ready := rready
  io.axi.aw.addr := awaddr
  io.axi.aw.len := awlen
  io.axi.aw.size := awsize
  io.axi.aw.valid := awvalid

  io.axi.w.data := wdata
  io.axi.w.strb := wstrb
  io.axi.w.valid := wvalid
  io.axi.w.last := wlast
  io.axi.b.ready := bready

  // fixed signals
  io.axi.ar.burst := 1.U(AXI4Parameters.burstBits.W)
  io.axi.ar.cache := 0.U(AXI4Parameters.cacheBits.W)
  io.axi.ar.lock := 0.U(2.W)
  io.axi.ar.prot := 0.U(AXI4Parameters.protBits.W)

  io.axi.aw.burst := 1.U(AXI4Parameters.burstBits.W)
  io.axi.aw.cache := 0.U(AXI4Parameters.cacheBits.W)
  io.axi.aw.lock := 0.U(2.W)
  io.axi.aw.prot := 0.U(AXI4Parameters.protBits.W)
  io.axi.aw.id := 1.U(AXI4Parameters.idBits.W)
  io.axi.w.id := 1.U(AXI4Parameters.idBits.W)

  io.bridge.inst_wr.out.rdy := 1.U

  val read_request_state = Reg(UInt(1.W))
  val read_respond_state = Reg(UInt(1.W))
  val write_request_state = Reg(UInt(3.W))

  val write_wait_enable = Wire(Bool())
  val rd_requst_state_is_empty = Wire(Bool())
  val rd_requst_can_receive = Wire(Bool())

  rd_requst_state_is_empty := read_request_state === read_request_empty

  val data_rd_cache_line = Wire(Bool())
  val inst_rd_cache_line = Wire(Bool())
  val data_real_rd_size = Wire(UInt(3.W))
  val data_real_rd_len = Wire(UInt(8.W))
  val inst_real_rd_size = Wire(UInt(3.W))
  val inst_real_rd_len = Wire(UInt(8.W))

  val data_wr_cache_line = Wire(Bool())
  val data_real_wr_size = Wire(UInt(3.W))
  val data_real_wr_len = Wire(UInt(8.W))

  val write_buffer_data = Reg(UInt(128.W))
  val write_buffer_num = Reg(UInt(3.W))
  val write_buffer_last = Wire(Bool())
  io.write_buffer_empty := write_buffer_num === 0.U(3.W) && !write_wait_enable

  rd_requst_can_receive :=
    rd_requst_state_is_empty && !(write_wait_enable && !(io.axi.b.valid && bready))

  io.bridge.data.out.rdy := rd_requst_can_receive
  io.bridge.inst.out.rdy := !io.bridge.data.in.rd_req && rd_requst_can_receive

  data_rd_cache_line := io.bridge.data.in.rd_type === "b100".U(3.W)
  data_real_rd_size :=
    Mux(data_rd_cache_line, "b10".U(3.W), io.bridge.data.in.rd_type)
  data_real_rd_len := Mux(data_rd_cache_line, "b11".U(8.W), 0.U(8.W))

  inst_rd_cache_line := io.bridge.inst.in.rd_type === "b100".U
  inst_real_rd_size :=
    Mux(inst_rd_cache_line, "b10".U(3.W), io.bridge.inst.in.rd_type)
  inst_real_rd_len := Mux(inst_rd_cache_line, "b11".U(8.W), 0.U(8.W))

  data_wr_cache_line := io.bridge.data_wr.in.rtype === "b100".U(3.W)
  data_real_wr_size :=
    Mux(data_wr_cache_line, "b10".U(3.W), io.bridge.data_wr.in.rtype)
  data_real_wr_len := Mux(data_wr_cache_line, "b11".U(8.W), 0.U(8.W))

  io.bridge.inst.out.ret_valid := !io.axi.r.id(0) && io.axi.r.valid
  io.bridge.inst.out.ret_last := !io.axi.r.id(0) && io.axi.r.last
  io.bridge.inst.out.ret_data := io.axi.r.data

  io.bridge.data.out.ret_valid := io.axi.r.id(0) && io.axi.r.valid
  io.bridge.data.out.ret_last := io.axi.r.id(0) && io.axi.r.last
  io.bridge.data.out.ret_data := io.axi.r.data

  io.bridge.data_wr.out.rdy := write_request_state === write_request_empty

  write_buffer_last := write_buffer_num === 1.U(3.W)

  // Read request state machine
  withClock(clock) {
    when(reset.asBool) {
      read_request_state := read_request_empty
      arvalid := 0.U(1.W)
    }.otherwise {
      switch(read_request_state) {
        is(read_request_empty) {
          when(io.bridge.data.in.rd_req) {
            when(write_wait_enable) {
              when(io.axi.b.valid && bready) {
                read_request_state := read_request_ready
                arid := 1.U(AXI4Parameters.idBits.W)
                araddr := io.bridge.data.in.rd_addr
                arlen := data_real_rd_len
                arsize := data_real_rd_size
                arvalid := 1.U
              }
            }.otherwise {
              read_request_state := read_request_ready
              arid := 1.U((AXI4Parameters.idBits.W))
              araddr := io.bridge.data.in.rd_addr
              arlen := data_real_rd_len
              arsize := data_real_rd_size
              arvalid := 1.U
            }
          }.elsewhen(io.bridge.inst.in.rd_req) {
            when(write_wait_enable) {
              when(io.axi.b.valid && bready) {
                read_request_state := read_request_ready
                arid := 0.U(AXI4Parameters.idBits.W)
                araddr := io.bridge.inst.in.rd_addr
                arlen := inst_real_rd_len
                arsize := inst_real_rd_size
                arvalid := 1.U
              }
            }.otherwise {
              read_request_state := read_request_ready
              arid := 0.U(AXI4Parameters.idBits.W)
              araddr := io.bridge.inst.in.rd_addr
              arlen := inst_real_rd_len
              arsize := inst_real_rd_size
              arvalid := 1.U
            }
          }
        }
        is(read_request_ready) {
          when(io.axi.ar.ready) {
            read_request_state := read_request_empty
            arvalid := 0.U
          }
        }
      }
    }
  }

  // Read respond state machine
  withClock(clock) {
    when(reset.asBool) {
      read_respond_state := read_respond_empty
      rready := 1.U
    }.otherwise {
      switch(read_respond_state) {
        is(read_respond_empty) {
          when(io.axi.r.valid && rready) {
            read_respond_state := read_respond_transfer
          }
        }
        is(read_respond_transfer) {
          when(io.axi.r.last && io.axi.r.valid) {
            read_respond_state := read_respond_empty
          }
        }
      }
    }
  }

  // Write request state machine
  withClock(clock) {
    when(reset.asBool) {
      write_request_state := write_request_empty
      awvalid := 0.U
      wvalid := 0.U
      wlast := 0.U
      bready := 0.U
      write_buffer_data := 0.U(128.W)
      write_buffer_num := 0.U(3.W)
    }.otherwise {
      when(write_request_state === write_request_empty) {
        when(io.bridge.data_wr.in.req) {
          write_request_state := write_data_wait
          awaddr := io.bridge.data_wr.in.addr
          awsize := data_real_wr_size
          awlen := data_real_wr_len
          awvalid := 1.U
          wdata := io.bridge.data_wr.in.data(31, 0)
          wstrb := io.bridge.data_wr.in.wstrb

          write_buffer_data := Cat(
            0.U(32.W),
            io.bridge.data_wr.in.data(127, 32)
          )

          when(io.bridge.data_wr.in.rtype === "b100".U) {
            write_buffer_num := "b011".U(3.W)
          }.otherwise {
            write_buffer_num := 0.U(3.W)
            wlast := 1.U
          }
        }
      }.elsewhen(write_request_state === write_data_wait) {
        when(io.axi.aw.ready) {
          write_request_state := write_data_transform
          awvalid := 0.U
          wvalid := 1.U
        }
      }.elsewhen(write_request_state === write_data_transform) {
        when(io.axi.w.ready) {
          when(wlast) {
            write_request_state := write_wait_b
            wvalid := 0.U
            wlast := 0.U
            bready := 1.U
          }.otherwise {
            when(write_buffer_last) {
              wlast := 1.U
            }
            write_request_state := write_data_transform
            wdata := write_buffer_data(31, 0)
            wvalid := 1.U
            write_buffer_data := Cat(
              0.U(32.W),
              write_buffer_data(127, 32)
            )
            write_buffer_num := write_buffer_num - 1.U(1.W)
          }
        }
      }.elsewhen(write_request_state === write_wait_b) {
        when(io.axi.b.valid && bready) {
          write_request_state := write_request_empty
          bready := 0.U
        }
      }.otherwise {
        write_request_state := write_request_empty
      }
    }

  }
  write_wait_enable := !(write_request_state === write_request_empty)
}
