package sfenv
package rules

import io.circe.*

import envr.{ObjMeta, Props}

case class Database(x: Database.Aux, props: Props):
  export x.*

  def resolve(dbName: String)(using n: NameResolver) =
    envr.Database(
      name = n.db(dbName),
      transient = transient.getOrElse(false),
      meta = ObjMeta(props, tags, comment),
      schemas = schemas.getOrElse(Map.empty).map((schName, x) => x.resolve(dbName, schName)).toList
    )

object Database:
  case class Aux(transient: Option[Boolean], schemas: Option[Map[String, Schema]], tags: Tags, comment: Comment) derives Decoder

  given Decoder[Database] with
    def apply(c: HCursor) = summon[Decoder[Aux]](c).map(Database(_, Util.fromCursor[Aux](c)))
