package sfenv
package rules
import org.virtuslab.yaml.*

import scala.language.implicitConversions
import scala.util.*

import envr.PropVal
import rules.Util.given

enum Namespace:
  case Schema(db: String, sch: String)
  case Database(db: String)

  def resolve(using n: NameResolver): PropVal =
    this match
      case Schema(db, sch) => PropVal.Sch(n.db(db), n.sch(db, sch))
      case Database(db)    => PropVal.Str(n.db(db))

object Namespace:
  def apply(x: String): Either[String, Namespace] =
    x.split("\\.") match
      case Array(db, sch) => Right(Schema(db, sch))
      case Array(wh)      => Right(Database(wh))
      case _              => Left(s"Invalid namespace '$x'; must be either <db> or <db>.<sch>")

  given YamlDecoder[Namespace] = YamlDecoder[String].mapError(Namespace.apply)
