package sfenv
package envr

import fs2.Stream

import Sql.*

case class Import(name: String, provider: String, share: String, roles: List[RoleName])

object Import:
  private val priv = List("IMPORTED PRIVILEGES")

  given SqlObj[Import] with
    type Key = String

    extension (shr: Import)
      override def id = shr.name

      override def create[F[_]] =
        import shr.*
        Stream.emit(Sql.CreateObj("DATABASE", name, s" FROM SHARE $provider.$share")) ++
          Stream.emits(roles).map(r => Sql.ObjGrant("DATABASE", name, r, priv))

      override def unCreate[F[_]] = Stream.emit(Sql.DropObj("DATABASE", shr.name, true))

      override def alter[F[_]](old: Import) =
        if shr.provider == old.provider && shr.share == old.share then
          Stream
            .emits(shr.roles.merge(old.roles))
            .collect:
              case (Some(n), None) => Sql.ObjGrant("DATABASE", shr.name, n, priv)
              case (None, Some(o)) => Sql.ObjGrant("DATABASE", shr.name, o, priv, revoke = true)
        else old.unCreate ++ create
