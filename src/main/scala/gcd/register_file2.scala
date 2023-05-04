import chisel3._
import chisel3.util._
import javax.swing.text.StyledEditorKit.BoldAction

class register_file2 extends RawModule {
    var io = IO(new Bundle {
        var clk = Input(Clock())
        var rst = Input(AsyncReset())
        var R_Addr_A = Input(UInt(4.W))
        var R_Addr_B = Input(UInt(4.W))
        var R_Addr_C = Input(UInt(4.W))
        var W_Addr = Input(UInt(4.W))
        var W_Data = Input(UInt(32.W))
        // var W_PC = Input(Bool())
        var W_reg = Input(Bool())
        var M = Input(UInt(5.W))
        var R_Data_A = Output(UInt(32.W))
        var R_Data_B = Output(UInt(32.W))
        var R_Data_C = Output(UInt(32.W))
        var Error1 = Output(Bool())
        var Error2 = Output(Bool())
    })
    withClockAndReset((~io.clk.asUInt.asBool).asClock, io.rst) {
        var Ren = VecInit(Seq.fill(15)(false.B))
        var Ren_fiq = VecInit(Seq.fill(7)(false.B))
        var Ren_irq = VecInit(false.B, false.B)
        var Ren_abt = VecInit(false.B, false.B)
        var Ren_svc = VecInit(false.B, false.B)
        var Ren_und = VecInit(false.B, false.B)
        var Ren_mon = VecInit(false.B, false.B)
        var Ren_hyp = Wire(Bool())
        Ren_hyp := false.B
        var mySeq: Seq[Data] = (0 until 15).map(j => RegEnable(io.W_Data, 0.U, Ren(j)))
        var mySeq2: Seq[Data] = (0 until 7).map(j => RegEnable(io.W_Data, 0.U, Ren_fiq(j)))
        var mySeq3: Seq[Data] = (0 until 2).map(j => RegEnable(io.W_Data, 0.U, Ren_irq(j)))
        var mySeq4: Seq[Data] = (0 until 2).map(j => RegEnable(io.W_Data, 0.U, Ren_abt(j)))
        var mySeq5: Seq[Data] = (0 until 2).map(j => RegEnable(io.W_Data, 0.U, Ren_svc(j)))
        var mySeq6: Seq[Data] = (0 until 2).map(j => RegEnable(io.W_Data, 0.U, Ren_und(j)))
        var mySeq7: Seq[Data] = (0 until 2).map(j => RegEnable(io.W_Data, 0.U, Ren_mon(j)))
        var R = VecInit(mySeq)
        var R_fiq = VecInit(mySeq2)
        var R_irq = VecInit(mySeq3)
        var R_abt = VecInit(mySeq4)
        var R_svc = VecInit(mySeq5)
        var R_und = VecInit(mySeq6)
        var R_mon = VecInit(mySeq7)
        var R_hyp = RegEnable(io.W_Data, 0.U, Ren_hyp)

        val w_enable = io.W_reg & io.M(4)
        val W_enable_fiq = w_enable & (io.M(3, 0) === "b0001".U)

        io.Error1 := false.B
        for {
            i <- 0 to 7
        } yield {
            Ren(i) := w_enable & (io.W_Addr === i.U)
        }
        for {
            i <- 8 to 12
        } yield {
            Ren(i) := ~W_enable_fiq & (io.W_Addr === i.U)
            Ren_fiq(i - 8) := W_enable_fiq & (io.W_Addr === i.U)
        }
        switch(io.W_Addr) {
            is("hD".U) {
                switch(io.M(3, 0)) {
                    is("b0000".U, "b1111".U) { Ren(13) := w_enable }
                    is("b0001".U) { Ren_fiq(5) := w_enable }
                    is("b0010".U) { Ren_irq(0) := w_enable }
                    is("b0011".U) { Ren_svc(0) := w_enable }
                    is("b0110".U) { Ren_mon(0) := w_enable }
                    is("b0111".U) { Ren_abt(0) := w_enable }
                    is("b1010".U) { Ren_hyp := w_enable }
                    is("b1011".U) { Ren_und(0) := w_enable }
                    is("b0100".U, "b0101".U, "b1000".U, "b1001".U, "b1100".U, "b1101".U, "b1110".U) {
                        io.Error1 := true.B
                    }
                }
            }
            is("hE".U) {
                switch(io.M(3, 0)) {
                    is("b0000".U, "b1111".U) { Ren(14) := w_enable }
                    is("b0001".U) { Ren_fiq(6) := w_enable }
                    is("b0010".U) { Ren_irq(1) := w_enable }
                    is("b0011".U) { Ren_svc(1) := w_enable }
                    is("b0110".U) { Ren_mon(1) := w_enable }
                    is("b0111".U) { Ren_abt(1) := w_enable }
                    is("b1011".U) { Ren_und(1) := w_enable }
                    is("b1010".U, "b0100".U, "b0101".U, "b1000".U, "b1001".U, "b1100".U, "b1101".U, "b1110".U) {
                        io.Error1 := true.B
                    }
                }
            }
            is("hF".U) {
                io.Error1 := true.B
            }
        }

        io.Error2 := false.B
        var io_R_data_A1 = WireInit(0.U(32.W))
        switch(io.M(3, 0)) {
            is("b0000".U, "b1111".U) { io_R_data_A1 := R(13) }
            is("b0001".U) { io_R_data_A1 := R_fiq(5) }
            is("b0010".U) { io_R_data_A1 := R_irq(0) }
            is("b0111".U) { io_R_data_A1 := R_abt(0) }
            is("b0011".U) { io_R_data_A1 := R_svc(0) }
            is("b1011".U) { io_R_data_A1 := R_und(0) }
            is("b0110".U) { io_R_data_A1 := R_mon(0) }
            is("b1010".U) { io_R_data_A1 := R_hyp }
            is("b0100".U, "b0101".U, "b1000".U, "b1001".U, "b1100".U, "b1101".U, "b1110".U) {
                io.Error2 := true.B
            }
        }
        var io_R_data_A2 = WireInit(0.U(32.W))
        switch(io.M(3, 0)) {
            is("b0000".U, "b1111".U) { io_R_data_A2 := R(14) }
            is("b0001".U) { io_R_data_A2 := R_fiq(6) }
            is("b0010".U) { io_R_data_A2 := R_irq(1) }
            is("b0111".U) { io_R_data_A2 := R_abt(1) }
            is("b0011".U) { io_R_data_A2 := R_svc(1) }
            is("b1011".U) { io_R_data_A2 := R_und(1) }
            is("b0110".U) { io_R_data_A2 := R_mon(1) }
            is("b1010".U, "b0100".U, "b0101".U, "b1000".U, "b1001".U, "b1100".U, "b1101".U, "b1110".U) {
                io.Error2 := true.B
            }
        }

        io.R_Data_A := 0.U
        when((io.R_Addr_A < 8.U) || (io.R_Addr_A === 15.U)) {
            io.R_Data_A := R(io.R_Addr_A)
        }.elsewhen(io.R_Addr_A < 13.U) {
            io.R_Data_A := Mux(io.M(3, 0) === "b0001".U, R_fiq(io.R_Addr_A - 8.U), R(io.R_Addr_A))
        }.elsewhen(io.R_Addr_A === 13.U) {
            io.R_Data_A := io_R_data_A1
        }.otherwise {
            io.R_Data_A := io_R_data_A2
        }
        io.R_Data_B := 0.U
        when((io.R_Addr_B < 8.U) || (io.R_Addr_B === 15.U)) {
            io.R_Data_B := R(io.R_Addr_B)
        }.elsewhen(io.R_Addr_B < 13.U) {
            io.R_Data_B := Mux(io.M(3, 0) === "b0001".U, R_fiq(io.R_Addr_B - 8.U), R(io.R_Addr_B))
        }.elsewhen(io.R_Addr_B === 13.U) {
            io.R_Data_B := io_R_data_A1
        }.otherwise {
            io.R_Data_B := io_R_data_A2
        }
        io.R_Data_C := 0.U
        when((io.R_Addr_C < 8.U) || (io.R_Addr_C === 15.U)) {
            io.R_Data_C := R(io.R_Addr_C)
        }.elsewhen(io.R_Addr_C < 13.U) {
            io.R_Data_C := Mux(io.M(3, 0) === "b0001".U, R_fiq(io.R_Addr_C - 8.U), R(io.R_Addr_C))
        }.elsewhen(io.R_Addr_C === 13.U) {
            io.R_Data_C := io_R_data_A1
        }.otherwise {
            io.R_Data_C := io_R_data_A2
        }

    }
}

object register_file2 extends App {
    emitVerilog(new register_file2())
}
