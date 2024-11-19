package sfenv
package envr

enum PropVal:
  case Num(value: BigDecimal)
  case Str(value: String)
  case Sch(db: String, sch: String)
  case Bool(value: Boolean)

  override def toString(): String =
    this match
      case Num(x) => x.toString()
      case Str(x) =>
        if x.startsWith("'") || x.startsWith("(") || "[A-Za-z_][A-Za-z_0-9$]*".r.matches(x) then x else x.asSqlLiteral
      case Sch(d, s) => d + "." + s
      case Bool(x)   => if x then "TRUE" else "FALSE"
