package sfenv
package envr

import fs2.Stream

import Sql.*

case class UserId(name: String, roles: List[RoleName], meta: ObjMeta)

object UserId:
  given SqlObj[UserId] with
    type Key = String

    extension (user: UserId)
      override def id = user.name

      override def create[F[_]] =
        import user.*
        Stream.emit(Sql.CreateObj("USER", name, meta.toString())) ++
          Stream.emits(roles).map(r => Sql.RoleGrant(r, name))

      override def unCreate[F[_]] = Stream.emit(Sql.DropObj("USER", user.name))

      override def alter[F[_]](old: UserId) =
        Stream.emits(user.roles.regrant(old.roles, user.name)) ++
          user.meta.alter("USER", user.name, old.meta)
