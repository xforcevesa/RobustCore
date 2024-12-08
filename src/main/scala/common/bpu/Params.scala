package common.bpu

import chisel3._
import chisel3.util._

class BPUParameters {
    val numRasEntries: Int = 8
}

trait HasBPUParameters {
    val vaddrBits = 32
    val fetchWidth = 4
    val fetchBytes = fetchWidth * 4

    

    val mixSize = 24
    // def mixHILO(pc: UInt): UInt = Cat(pc(vaddrBits - 1 , mixSize) , pc(mixSize - 1, 0) ^ pc(vaddrBits - 1 , vaddrBits - mixSize))
    def mixHILO(pc: UInt): UInt = pc

    val targetSz = 15

    def getTargetPC(pc: UInt , target : UInt): UInt = {
        Cat(pc(vaddrBits - 1, targetSz + 2) , target(targetSz - 1 , 0) , 0.U(2.W))
    }

    def getTarget(tgtpc : UInt): UInt = tgtpc(targetSz + 2 - 1 , 2)
}
