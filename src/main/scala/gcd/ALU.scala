import chisel3._
import chisel3.util._

class ALU extends RawModule {
    var io = IO(new Bundle {
        var A = Input(UInt(32.W))
        var B = Input(UInt(32.W))
        var C = Input(UInt(1.W))
        var V = Input(UInt(1.W))
        var Shift_carry_out = Input(UInt(1.W))
        var ALU_op = Input(UInt(4.W))
        var NZCV = Output(UInt(4.W))
        var F = Output(UInt(32.W))
    })
    var Cout = Wire(0.U(1.W))
    switch(io.ALU_op) {
        is("h0".U) { io.F := io.A & io.B }
        is("h1".U) { io.F := io.A ^ io.B }
        is("h2".U) { Cat(Cout, io.F) := io.A - io.B }
        is("h3".U) { Cat(Cout, io.F) := io.B - io.A }
        is("h4".U) { Cat(Cout, io.F) := io.A + io.B }
        is("h5".U) { Cat(Cout, io.F) := io.A + io.B + io.C }
        is("h6".U) { Cat(Cout, io.F) := io.A - io.B + io.C - 1.U }
        is("h7".U) { Cat(Cout, io.F) := io.B - io.A + io.C - 1.U }
        is("h8".U) { io.F := io.A }
        is("hA".U) { Cat(Cout, io.F) := io.A - io.B + "h4".U(32.W) }
        is("hC".U) { io.F := io.A | io.B }
        is("hD".U) { io.F := io.B }
        is("hE".U) { io.F := io.A & (~io.B) }
        is("h0".U) { io.F := ~io.B }
    }
    switch(io.ALU_op) {
        is("h0".U, "h1".U, "hC".U, "hE".U, "hF".U, "h8".U, "hD".U) {
            io.NZCV(1) := io.Shift_carry_out
            io.NZCV(0) := io.V
        }
        is("h2".U, "h3".U, "h4".U, "h5".U, "h6".U, "h7".U, "hA".U) {
            io.NZCV(1) := io.ALU_op(1) ^ Cout
            io.NZCV(0) := io.A(31) ^ io.B(31) ^ io.F(31) ^ Cout
        }
    }
    io.NZCV(3) := io.F(31)
    io.NZCV(2) := Mux((io.F === 0.U), 1.U(1.W), 0.U(1.W))
}
