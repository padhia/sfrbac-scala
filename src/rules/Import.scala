package sfenv
package rules

import io.circe.*

case class Import(provider: String, share: String, roles: Option[List[String]]) derives Decoder:
  def resolve(name: String)(using n: NameResolver) =
    envr.Import(n.db(name), provider, share, roles.map(_.map(r => envr.RoleName.Account(n.fn(r)))).getOrElse(List.empty))
