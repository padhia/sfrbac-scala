package sfenv
package rules

import io.circe.*

import envr.{ObjMeta, Props}

case class UserId(x: UserId.Aux, props: Props):
  export x.*

  def resolve(name: String)(using n: NameResolver) =
    envr.UserId(
      name,
      roles.getOrElse(List.empty).map(r => envr.RoleName.Account(n.fn(r))),
      ObjMeta(props, tags, comment)
    )

object UserId:
  case class Aux(roles: Option[List[String]], tags: Tags, comment: Comment) derives Decoder

  given Decoder[UserId] with
    def apply(c: HCursor) = summon[Decoder[Aux]].apply(c).map(UserId(_, Util.fromCursor[Aux](c)))
