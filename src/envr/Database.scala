package sfenv
package envr

import fs2.*

import Sql.*
import SqlOperable.given

case class Database(name: String, transient: Boolean, meta: ObjMeta, schemas: List[Schema])

object Database:
  def sqlObj(secAdm: RoleName) =
    new SqlObj[Database]:
      type Key = String

      extension (db: Database)
        override def id = db.name

        override def create[F[_]] =
          given SqlObj[Schema] = Schema.sqlObj(db.name)
          import db.*

          Stream.emit(Sql.CreateObj(if transient then "TRANSIENT DATABASE" else "DATABASE", name, meta.toString())) ++
            Stream.emit(Sql.ObjGrant("DATABASE", name, secAdm, List("CREATE DATABASE ROLE", "USAGE"))) ++
            Stream.emits(schemas).flatMap(_.create)

        override def unCreate[F[_]] =
          given SqlObj[Schema] = Schema.sqlObj(db.name)
          Stream.emits(db.schemas).flatMap(_.unCreate) ++
            Stream.emit(Sql.DropObj("DATABASE", db.name))

        override def alter[F[_]](old: Database) =
          if db.transient != old.transient then old.unCreate[F] ++ create[F]
          else
            given SqlObj[Schema] = Schema.sqlObj(db.name)
            db.meta.alter("DATABASE", db.name, old.meta) ++
              db.schemas.alter(old.schemas)
