import chisel3._
import chisel3.util._

class shift_barrel extends RawModule {
    var io = IO(new Bundle {
        var Shift_data = Input(UInt(32.W))
        var Shift_num = Input(UInt(8.W))
        var carry_flag = Input(UInt(1.W))
        var Shift_op = Input(UInt(3.W))
        var Shift_out = Output(UInt(32.W))
        var Shift_carry_out = Output(UInt(1.W))
    })
    switch(io.Shift_op(2, 1)) {
        is("b00".U) {
            when(io.Shift_num === 0.U) {
                io.Shift_out := io.Shift_data
            }.elsewhen(io.Shift_num <= 32.U) {
                io.Shift_out := io.Shift_data << io.Shift_num
                io.Shift_carry_out := io.Shift_data(32.U - io.Shift_num)
            }.otherwise {
                io.Shift_out := 0.U
                io.Shift_carry_out := 0.U
            }
        }
        is("b01".U) {
            when(io.Shift_num === 0.U) {
                when(~io.Shift_op(0)) {
                    io.Shift_out := 0.U
                    io.Shift_carry_out := io.Shift_data(31)
                }.otherwise {
                    io.Shift_out := io.Shift_data
                }
            }.elsewhen(io.Shift_num <= 32.U) {
                io.Shift_out := io.Shift_data >> io.Shift_num
                io.Shift_carry_out := io.Shift_data(io.Shift_num - 1.U)
            }.otherwise {
                io.Shift_out := 0.U
                io.Shift_carry_out := 0.U
            }
        }
        is("b10".U) {
            when(io.Shift_num === 0.U) {
                when(~io.Shift_op(0)) {
                    io.Shift_out := Fill(32, io.Shift_data(31))
                    io.Shift_carry_out := io.Shift_data(31)
                }.otherwise {
                    io.Shift_out := io.Shift_data
                }
            }.elsewhen(io.Shift_num <= 31.U) {
                io.Shift_out := Cat(Fill(32, io.Shift_data(31)), io.Shift_data) >> io.Shift_num
                io.Shift_carry_out := io.Shift_data(io.Shift_num - 1.U)
            }.otherwise {
                io.Shift_out := Fill(32, io.Shift_data(31))
                io.Shift_carry_out := io.Shift_data(31)
            }
        }
        is("b11".U) {
            when(io.Shift_num === 0.U) {
                when(~io.Shift_op(0)) {
                    io.Shift_out := Cat(io.carry_flag, io.Shift_data(31, 1))
                    io.Shift_carry_out := io.Shift_data(0)
                }.otherwise {
                    io.Shift_out := io.Shift_data
                }
            }.elsewhen(io.Shift_num <= 32.U) {
                io.Shift_out :=
                    Cat(io.Shift_data, io.Shift_data) >> io.Shift_num
                io.Shift_carry_out := io.Shift_data(io.Shift_num - 1.U)
            }.otherwise {
                io.Shift_out :=
                    Cat(Fill(32, io.Shift_data), io.Shift_data) >> io.Shift_num(4, 0)
                io.Shift_carry_out := io.Shift_data(io.Shift_num(4, 0) - 1.U)
            }
        }
    }

}
