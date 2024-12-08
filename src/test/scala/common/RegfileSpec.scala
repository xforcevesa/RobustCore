package common

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RegfileSpec extends AnyFreeSpec with Matchers {
  "Regfile should be able to read and write" in {
    simulate(new Regfile) { dut =>
      // Generate some random data
      val data = Seq.fill(32)(scala.util.Random.nextInt(1 << 32))
      // Write data to the regfile
      for (i <- 0 until 32) {
        dut.io.w.we.poke(true.B)
        dut.io.w.addr.poke(i.U)
        dut.io.w.data.poke(data(i).U)
        dut.clock.step()
      }
      // Read data from the regfile
      for (i <- 0 until 32 by 2) {
        dut.io.r1.addr.poke(i.U)
        dut.clock.step()
        dut.io.r1.data.expect(data(i).U)
        dut.io.r2.addr.poke((i + 1).U)
        dut.clock.step()
        dut.io.r2.data.expect(data(i + 1).U)
      }
    }
  }
}
