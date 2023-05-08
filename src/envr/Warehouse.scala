package sfenv
package envr

import fs2.Stream

import Sql.*

case class Warehouse(name: String, meta: ObjMeta, accRoles: AccRoles)

object Warehouse:
  given SqlObj[Warehouse] with
    type Key = String

    extension (wh: Warehouse)
      override def id = wh.name

      override def create[F[_]] =
        given SqlOperable[AccRoles] = AccRoles.sqlOperable(wh.name)
        Stream.emit(Sql.CreateObj("WAREHOUSE", wh.name, wh.meta.toString())) ++
          wh.accRoles.create

      override def unCreate[F[_]] =
        given SqlOperable[AccRoles] = AccRoles.sqlOperable(wh.name)
        wh.accRoles.unCreate ++
          Stream.emit(Sql.DropObj("WAREHOUSE", wh.name))

      override def alter[F[_]](old: Warehouse) =
        given SqlOperable[AccRoles] = AccRoles.sqlOperable(wh.name)
        wh.meta.alter("WAREHOUSE", wh.name, old.meta) ++
          wh.accRoles.alter(old.accRoles)
