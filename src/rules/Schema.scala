package sfenv
package rules

import org.virtuslab.yaml.*

import envr.{ObjMeta, Props}
import envr.Props.*

case class Schema(x: Schema.Aux, props: Props):
  export x.*

  def resolve(dbName: String, schName: String)(using n: NameResolver) =
    envr.Schema(
      name = n.sch(dbName, schName),
      transient = transient.getOrElse(false),
      managed = managed.getOrElse(false),
      meta = ObjMeta(props, tags, comment),
      accRoles = acc_roles.map(_.resolve(dbName, schName)).getOrElse(Map.empty)
    )

object Schema:
  case class Aux(
      transient: Option[Boolean],
      managed: Option[Boolean],
      acc_roles: Option[AccRoles],
      tags: Tags,
      comment: Comment
  ) derives YamlDecoder

  given YamlDecoder[Schema] with
    def construct(node: Node)(implicit settings: LoadSettings) =
      for
        aux <- YamlDecoder[Aux].construct(node)
        ps  <- Props.fromYaml[Aux].construct(node)
      yield Schema(aux, Props.empty)
