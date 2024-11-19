package sfenv
package rules

import org.virtuslab.yaml.*

import envr.{ObjMeta, Props}
import envr.Props.*

case class Warehouse(x: Warehouse.Aux, props: Props):
  export x.*
  def resolve(whName: String)(using n: NameResolver) =
    envr.Warehouse(
      name = n.wh(whName),
      meta = ObjMeta(props, tags, comment),
      accRoles = acc_roles.resolve(whName)
    )

object Warehouse:
  case class Aux(acc_roles: AccRoles, tags: Tags, comment: Comment) derives YamlDecoder

  given YamlDecoder[Warehouse] with
    def construct(node: Node)(implicit settings: LoadSettings) =
      for
        aux <- summon[YamlDecoder[Aux]].construct(node)
        ps  <- Props.fromYaml[Aux].construct(node)
      yield Warehouse(aux, ps)
