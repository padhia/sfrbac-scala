package sfenv
package envr

import fs2.Stream

import Sql.*

case class Role(name: RoleName, accRoles: List[RoleName], meta: ObjMeta)

object Role:
  given SqlObj[Role] with
    override type Key = RoleName

    extension (role: Role)
      override def id = role.name

      override def create[F[_]]: Stream[F, Sql] =
        import role.*
        Stream.emit(Sql.CreateRole(name, meta)) ++
          Stream.emits(accRoles).map(ar => Sql.RoleGrant(ar, name))

      override def unCreate[F[_]] = Stream.emit(Sql.DropObj("ROLE", role.name.roleName))

      override def alter[F[_]](old: Role): Stream[F, Sql] =
        Stream.emits(role.accRoles.regrant(old.accRoles, role.name)) ++
          role.meta.alter(role.name.kind, role.name.roleName, old.meta)
