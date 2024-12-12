package common.utils

import chisel3._
import chisel3.util._

object AXI4Parameters {
    val idBits    = 4
    val addrBits  = 32
    val lenBits   = 8
    val sizeBits  = 3
    val burstBits = 2
    val cacheBits = 4
    val protBits  = 3
    val dataBits  = 32
    val respBits  = 2

    def MLEN1   = 0x0.U(lenBits.W)
    def MLEN2   = 0x1.U(lenBits.W)
    def MLEN4   = 0x3.U(lenBits.W)
    def MLEN8   = 0x7.U(lenBits.W)
    def MLEN16  = 0xF.U(lenBits.W) // max supported length
    def MLEN32  = 0x1F.U(lenBits.W)
    def MLEN64  = 0x3F.U(lenBits.W)
    def MLEN128 = 0x7F.U(lenBits.W)
    def MLEN256 = 0xFF.U(lenBits.W)

    def MSIZE1   = 0.U(sizeBits.W)
    def MSIZE2   = 1.U(sizeBits.W)
    def MSIZE4   = 2.U(sizeBits.W)  // max supported size
    def MSIZE8   = 3.U(sizeBits.W)
    def MSIZE16  = 4.U(sizeBits.W)
    def MSIZE32  = 5.U(sizeBits.W)
    def MSIZE64  = 6.U(sizeBits.W)
    def MSIZE128 = 7.U(sizeBits.W)

    def BURST_FIXED    = 0.U(burstBits.W)
    def BURST_INCR     = 1.U(burstBits.W)
    def BURST_WRAP     = 2.U(burstBits.W)
    def BURST_RESERVED = 3.U(burstBits.W)
}

class AXI4BundleAR extends Bundle {
    val addr  = Output(UInt(AXI4Parameters.addrBits.W))
    val len   = Output(UInt(AXI4Parameters.lenBits.W))
    val size  = Output(UInt(AXI4Parameters.sizeBits.W)) 
    val burst = Output(UInt(AXI4Parameters.burstBits.W))
    val lock  = Output(UInt(2.W))
    val cache = Output(UInt(AXI4Parameters.cacheBits.W))
    val prot  = Output(UInt(AXI4Parameters.protBits.W))
    val id    = Output(UInt(AXI4Parameters.idBits.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
}

class AXI4BundleR extends Bundle {
    val data  = Input(UInt(AXI4Parameters.dataBits.W))
    val resp  = Input(UInt(AXI4Parameters.respBits.W))
    val last  = Input(Bool())
    val id    = Input(UInt(AXI4Parameters.idBits.W))
    val valid = Input(Bool())
    val ready = Output(Bool())
}

class AXI4BundleAW extends Bundle {
    val addr  = Output(UInt(AXI4Parameters.addrBits.W))
    val len   = Output(UInt(AXI4Parameters.lenBits.W))
    val size  = Output(UInt(AXI4Parameters.sizeBits.W)) 
    val burst = Output(UInt(AXI4Parameters.burstBits.W))
    val lock  = Output(UInt(2.W))
    val cache = Output(UInt(AXI4Parameters.cacheBits.W))
    val prot  = Output(UInt(AXI4Parameters.protBits.W))
    val id    = Output(UInt(AXI4Parameters.idBits.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
}

class AXI4BundleW extends Bundle {
    val data  = Output(UInt(AXI4Parameters.dataBits.W))
    val strb  = Output(UInt((AXI4Parameters.dataBits / 8).W))
    val last  = Output(Bool())
    val id    = Output(UInt(AXI4Parameters.idBits.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
}

class AXI4BundleB extends Bundle {
    val resp  = Input(UInt(AXI4Parameters.respBits.W))
    val id    = Input(UInt(AXI4Parameters.idBits.W))
    val valid = Input(Bool())
    val ready = Output(Bool())
}

class AXI4 extends Bundle {
    val ar = new AXI4BundleAR
    val r  = new AXI4BundleR
    val aw = new AXI4BundleAW
    val w  = new AXI4BundleW
    val b  = new AXI4BundleB
}

class AXI4ReversedBundleAR extends Bundle {
    val addr  = Input(UInt(AXI4Parameters.addrBits.W))
    val len   = Input(UInt(AXI4Parameters.lenBits.W))
    val size  = Input(UInt(AXI4Parameters.sizeBits.W)) 
    val burst = Input(UInt(AXI4Parameters.burstBits.W))
    val lock  = Input(UInt(2.W))
    val cache = Input(UInt(AXI4Parameters.cacheBits.W))
    val prot  = Input(UInt(AXI4Parameters.protBits.W))
    val id    = Input(UInt(AXI4Parameters.idBits.W))
    val valid = Input(Bool())
    val ready = Output(Bool())
}

class AXI4ReversedBundleR extends Bundle {
    val data  = Output(UInt(AXI4Parameters.dataBits.W))
    val resp  = Output(UInt(AXI4Parameters.respBits.W))
    val last  = Output(Bool())    
    val id    = Output(UInt(AXI4Parameters.idBits.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
}

class AXI4ReversedBundleAW extends Bundle {
    val addr  = Input(UInt(AXI4Parameters.addrBits.W))
    val len   = Input(UInt(AXI4Parameters.lenBits.W))
    val size  = Input(UInt(AXI4Parameters.sizeBits.W)) 
    val burst = Input(UInt(AXI4Parameters.burstBits.W))
    val lock  = Input(UInt(2.W))
    val cache = Input(UInt(AXI4Parameters.cacheBits.W))
    val prot  = Input(UInt(AXI4Parameters.protBits.W))
    val id    = Input(UInt(AXI4Parameters.idBits.W))
    val valid = Input(Bool())
    val ready = Output(Bool())
}

class AXI4ReversedBundleW extends Bundle {
    val data  = Input(UInt(AXI4Parameters.dataBits.W))
    val strb  = Input(UInt((AXI4Parameters.dataBits / 8).W))
    val last  = Input(Bool())
    val id    = Input(UInt(AXI4Parameters.idBits.W))
    val valid = Input(Bool())
    val ready = Output(Bool())
}

class AXI4ReversedBundleB extends Bundle {
    val resp  = Output(UInt(AXI4Parameters.respBits.W))
    val id    = Output(UInt(AXI4Parameters.idBits.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
}

class AXI4Reversed extends Bundle {
    val ar = new AXI4ReversedBundleAR
    val r  = new AXI4ReversedBundleR
    val aw = new AXI4ReversedBundleAW
    val w  = new AXI4ReversedBundleW
    val b  = new AXI4ReversedBundleB
}