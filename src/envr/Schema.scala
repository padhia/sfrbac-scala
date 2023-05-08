package sfenv
package envr

import fs2.Stream

import Sql.*

case class Schema(name: String, transient: Boolean, managed: Boolean, meta: ObjMeta, accRoles: AccRoles)

object Schema:
  def sqlObj(dbName: String) =
    new SqlObj[Schema]:
      type Key = String

      extension (sch: Schema)
        private def schName = s"$dbName.${sch.name}"

        override def id = sch.name

        override def create[F[_]]: Stream[F, Sql] =
          given SqlOperable[AccRoles] = AccRoles.sqlOperable(sch.schName, dbName)

          val createDdl =
            if sch.name.toLowerCase == "public" || sch.name.toLowerCase == "information_schema" then Stream.empty
            else
              val kind    = if sch.transient then "TRANSIENT SCHEMA" else "SCHEMA"
              val managed = if sch.managed then " WITH MANAGED ACCESS" else ""
              Stream.emit(CreateObj(kind, sch.schName, s"$managed${sch.meta}"))

          createDdl ++ sch.accRoles.create

        override def unCreate[F[_]]: Stream[F, Sql] =
          given SqlOperable[AccRoles] = AccRoles.sqlOperable(sch.schName, dbName)

          sch.accRoles.unCreate ++ (
            if sch.name.toLowerCase == "public" || sch.name.toLowerCase == "information_schema" then Stream.empty
            else Stream.emit(DropObj("SCHEMA", sch.schName))
          )

        override def alter[F[_]](old: Schema): Stream[F, Sql] =
          if sch.transient != old.transient then old.unCreate[F] ++ create[F]
          else
            given SqlOperable[AccRoles] = AccRoles.sqlOperable(sch.schName, dbName)

            def alter_managed =
              if sch.managed != old.managed then
                val state = if sch.managed then "ENABLE" else "DISABLE"
                Stream.emit(Sql.AlterObj("SCHEMA", sch.schName, s" $state MANAGED ACCESS"))
              else Stream.empty

            alter_managed ++
              sch.meta.alter("SCHEMA", sch.schName, old.meta) ++
              sch.accRoles.alter(old.accRoles)
