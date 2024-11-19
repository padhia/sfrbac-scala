package sfenv
package rules

import org.virtuslab.yaml.*

import envr.{ObjMeta, Props}
import envr.Props.*

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
  case class Aux(transient: Option[Boolean], schemas: Option[Map[String, Schema]], tags: Tags, comment: Comment)
      derives YamlDecoder

  given YamlDecoder[Database] with
    def construct(node: Node)(implicit settings: LoadSettings) =
      for
        aux <- summon[YamlDecoder[Aux]].construct(node)
        ps  <- Props.fromYaml[Aux].construct(node)
      yield Database(aux, ps)
