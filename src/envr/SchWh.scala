package sfenv
package envr

import io.circe.*

enum SchWh:
  case Schema(db: String, sch: String)
  case Warehouse(wh: String)

  override def toString(): String = this match
    case Schema(db, sch) => s"$db.$sch"
    case Warehouse(wh)   => wh

object SchWh:
  given KeyDecoder[SchWh] with
    def apply(x: String) =
      x.split("\\.") match
        case Array(db, sch) => Some(Schema(db, sch))
        case Array(wh)      => Some(Warehouse(wh))
        case _              => None
