package sfenv
package envr

import org.virtuslab.yaml.*

import scala.language.implicitConversions

import rules.Util.*
import rules.Util.given

enum SchWh:
  case Schema(db: String, sch: String)
  case Warehouse(wh: String)

  override def toString(): String = this match
    case Schema(db, sch) => s"$db.$sch"
    case Warehouse(wh)   => wh

object SchWh:
  def apply(x: String): Either[String, SchWh] =
    x.split("\\.") match
      case Array(db, sch) => Right(Schema(db, sch))
      case Array(wh)      => Right(Warehouse(wh))
      case _              => Left(s"$x is not valid Schema or Wahrehouse name")

  given YamlDecoder[SchWh] = YamlDecoder[String].mapError(SchWh.apply)
