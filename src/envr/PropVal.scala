package sfenv
package envr

import org.virtuslab.yaml.*

import scala.util.Try

enum PropVal:
  case NumInt(value: BigInt)
  case NumDec(value: BigDecimal)
  case Str(value: String)
  case Sch(db: String, sch: String)
  case Bool(value: Boolean)

  override def toString(): String =
    this match
      case NumInt(x) => x.toString()
      case NumDec(x) => x.toString()
      case Str(x) =>
        if x.startsWith("'") || x.startsWith("(") || "[A-Za-z_][A-Za-z_0-9$]*".r.matches(x) then x else x.asSqlLiteral
      case Sch(d, s) => d + "." + s
      case Bool(x)   => if x then "TRUE" else "FALSE"

object PropVal:
  def apply(x: String): PropVal =
    Try(BigInt(x)).toOption
      .map(NumInt.apply)
      .orElse(Try(BigDecimal(x)).toOption.map(NumDec.apply))
      .orElse(Try(x.toBoolean).toOption.map(Bool.apply))
      .getOrElse(Str(x))

  def fromAny(x: Any): PropVal = x match
    case y: Byte       => NumInt(y.toInt)
    case y: Short      => NumInt(y.toInt)
    case y: Int        => NumInt(y)
    case y: Long       => NumInt(y)
    case y: BigInt     => NumInt(y)
    case y: Float      => NumDec(y.toDouble)
    case y: Double     => NumDec(y)
    case y: BigDecimal => NumDec(y)
    case y: String     => Str(y)
    case y: Boolean    => Bool(y)
    case _             => Str(x.toString())

  given YamlDecoder[PropVal] = YamlDecoder[String].map(PropVal.apply)
