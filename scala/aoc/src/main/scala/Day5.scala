object IntCodeComputer {

	def run(programString: String, input: Int): String = {
		val program = programString.split(",").map(_.toInt).toArray
		val computer = new IntCodeComputer(program)
		val (diagnostics, _) = computer.run(List(input))
		diagnostics match {
			case Nil => "NO OUTPUT"
			case d => d.last.toString
		}
	}

	sealed trait Param {
		def getValue(program: Array[Int]): Int
		def getImmediate: Int
	}
	case class ImmediateParam(value: Int) extends Param {
		override def getValue(program: Array[Int]) = value
		override def getImmediate = value
	}
	case class PositionParam(index: Int) extends Param {
		override def getValue(program: Array[Int]) = program(index)
		override def getImmediate = index
	}

	sealed trait Instruction
	case object Add extends Instruction
	case object Multiply extends Instruction
	case object StoreInput extends Instruction
	case object Output extends Instruction
	case object JumpIfTrue extends Instruction
	case object JumpIfFalse extends Instruction
	case object LessThan extends Instruction
	case object Equals extends Instruction
	case object Halt extends Instruction
	
	class IntCodeComputer(val program: Array[Int]) {

		def run(inputs: List[Int]): (List[Int], IntCodeComputer) = {
			var prog = program
			def getVal(pa: Param) = pa.getValue(prog)
			var i = 0
			var inputIndex = 0
			var limit = 0
			var out: List[Int] = Nil
			while (i < program.size && limit < 600) {
				//printProg(prog, i)
				val code = prog(i)
				val op = parseInstruction(code)
				val np = numParams(op)
				val intparams = for (j <- i + 1 to i + np) yield prog(j)
				val params = parseParams(code, intparams.toList)
				var doModifyInstrPointer = true
				(op, params) match {
					case (Add, Seq(p1, p2, PositionParam(index))) => {
						prog(index) = getVal(p1) + getVal(p2)
					}
					case (Multiply, Seq(p1, p2, PositionParam(index))) => {
						prog(index) = getVal(p1) * getVal(p2)
					}
					case (StoreInput, Seq(PositionParam(index))) => {
						if (inputIndex >= inputs.size) {
							throw new IllegalArgumentException("This program excepts at least " + (inputIndex + 1) + " inputs. " + "Only " + inputs.size + " were supplied.");
						}
						prog(index) = inputs(inputIndex)
						inputIndex = inputIndex + 1
					}
					case (Output, Seq(p)) => {
						out = getVal(p) :: out
					}
					case (JumpIfTrue, Seq(p1, p2)) => {
						if (getVal(p1) != 0) {
							i = getVal(p2)
							doModifyInstrPointer = false
						}
					}
					case (JumpIfFalse, Seq(p1, p2)) => {
						if (getVal(p1) == 0) {
							i = getVal(p2)
							doModifyInstrPointer = false
						}
					}
					case (LessThan, Seq(p1, p2, PositionParam(index))) => {
						prog(index) = if (getVal(p1) < getVal(p2)) 1 else 0
					}
					case (Equals, Seq(p1, p2, PositionParam(index))) => {
						prog(index) = if (getVal(p1) == getVal(p2)) 1 else 0
					}
					case (Halt, _) => i = program.size
					case _ => ???
				}
				if (doModifyInstrPointer) {
					i = i + np + 1	
				}
				limit = limit + 1
			}
			(out.reverse, new IntCodeComputer(prog))
		}

		def parseParams(code: Int, params: List[Int]): Seq[Param] = {
			val pairs = "%03d".format(("0"+code.toString.dropRight(2)).toInt).reverse zip params 
			for (p <- pairs) yield p match {
				case ('0', i) => PositionParam(i)
				case ('1', i) => ImmediateParam(i)
				case _ => ???
			}
		}

		def numParams(op: Instruction) = op match {
			case Add => 3
			case Multiply => 3
			case StoreInput => 1
			case Output => 1
			case JumpIfTrue => 2
			case JumpIfFalse => 2
			case LessThan => 3
			case Equals => 3
			case Halt => 0
		}

		def parseInstruction(code: Int): Instruction = code.toString.takeRight(2).toInt match {
			case 1 => Add
			case 2 => Multiply
			case 3 => StoreInput
			case 4 => Output
			case 5 => JumpIfTrue
			case 6 => JumpIfFalse
			case 7 => LessThan
			case 8 => Equals
			case 99 => Halt
			case _ => ???
		}

		def printProg(prog: Array[Int], index: Int): Unit = {
			var copy = prog.map(_.toString)
			copy(index) = "(" + copy(index) + ")"
			println(index + ": " + copy.mkString(", "))
		}

	}

}