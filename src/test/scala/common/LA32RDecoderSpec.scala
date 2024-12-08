package common

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import common.decode.la32r.DecodeUnit
import common.decode.Consts.uopADDIW
import common.decode.Consts.uopLD
import common.decode.Consts.uopBNE
import common.decode.Consts.uopADD
import common.decode.Consts.uopSTA

class LA32RDecoderSpec extends AnyFreeSpec with Matchers {
  "Decoder should correctly decode instructions" in {
    simulate(new DecodeUnit) { dut =>
      // 1c000000:       addi.w   $t0,$zero,0x0      //置第1项的0
      // 1c000004:       addi.w   $t1,$zero,0x1      //置第2项的1
      // 1c000008:       addi.w   $s0,$zero,0x0      //循环变量i初始化为0
      // 1c00000c:       addi.w   $s1,$zero,0x1      //循环的步长置为1
      // 1c000010:       ld.w     $a0,$zero,1024     //读取拨码开关输入的终止值
      //           loop:
      // 1c000014:       add.w    $t2,$t0,$t1        //f(i) = f(i-2) + f(i-1) 
      // 1c000018:       addi.w   $t0,$t1,0x0        //记录f(i-1)
      // 1c00001c:       addi.w   $t1,$t2,0x0        //记录f(i) 
      // 1c000020:       add.w    $s0,$s0,$s1        //i++
      // 1c000024:       bne      $s0,$a0,loop       //if i!=n, goto loop
      // 1c000028:       st.w     $t2,$zero,1028     //将f(n)的值输出到数码管上
      //           end:
      // 1c00002c:       bne      $s1, $zero, end    //测试完毕，进入死循环

      val instructions = Seq(
        0x0280000c,
        0x0280040d,
        0x02800017,
        0x02800418,
        0x28900004,
        0x0010358e,
        0x028001ac,
        0x028001cd,
        0x001062f7,
        0x5ffff2e4,
        0x2990100e,
        0x5c000300
      )
      val uop = Seq(
        uopADDIW,
        uopADDIW,
        uopADDIW,
        uopADDIW,
        uopLD,
        uopADD,
        uopADDIW,
        uopADDIW,
        uopADD,
        uopBNE,
        uopSTA,
        uopBNE
      )
      for ((inst, expUop) <- instructions zip uop) {
        dut.io.enq.uop.instr.poke(inst)
        dut.io.enq.uop.xcpt_valid.poke(false.B)
        dut.clock.step(1)
        dut.io.deq.uop.uopc.expect(expUop)
      }
    }
  }
}
