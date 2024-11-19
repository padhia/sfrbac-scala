package sfenv
package rules

import org.virtuslab.yaml.*

import envr.{ObjMeta, Props, PropVal}
import envr.Props.*

case class UserId(x: UserId.Aux, props: Props):
  export x.*

  def resolve(name: String)(using n: NameResolver) =
    val defaults =
      List(
        "DEFAULT_WAREHOUSE" -> default_warehouse.map(x => PropVal.Str(n.wh(x))),
        "DEFAULT_NAMESPACE" -> default_namespace.map(_.resolve),
        "DEFAULT_ROLE"      -> default_role.map(x => PropVal.Str(n.fn(x)))
      )
        .collect:
          case (p, Some(v)) => p -> v
        .toMap

    envr.UserId(
      name = name,
      roles = roles.getOrElse(List.empty).map(r => envr.RoleName.Account(n.fn(r))),
      meta = ObjMeta(defaults ++ props, tags, comment)
    )

object UserId:
  case class Aux(
      roles: Option[List[String]],
      default_warehouse: Option[String],
      default_namespace: Option[Namespace],
      default_role: Option[String],
      tags: Tags,
      comment: Comment
  ) derives YamlDecoder

  given YamlDecoder[UserId] with
    def construct(node: Node)(implicit settings: LoadSettings) =
      for
        aux <- summon[YamlDecoder[Aux]].construct(node)
        ps  <- Props.fromYaml[Aux].construct(node)
      yield UserId(aux, ps)
