package sfenv
package envr

import fs2.Stream

type ObjGrants = Map[ObjType, Grantables]
type AccRoles  = Map[RoleName, ObjGrants]

object AccRoles:
  def sqlOperable(objName: String, dbName: String = "") =
    new SqlOperable[AccRoles]:
      private def genRole(role: RoleName, opm: ObjGrants) =
        Stream.emit(Sql.CreateRole(role, ObjMeta.empty)) ++
          Stream.emits(opm.toList).flatMap((ot, gs) => gs.grant(ot, if ot == "DATABASE" then dbName else objName, role))

      private def alterGrants[F[_]](grantee: RoleName, opm: ObjGrants, old: ObjGrants): Stream[F, Sql] =
        Stream
          .emits(opm.merge(old))
          .flatMap((ot, gsN, gsO) =>
            (gsN, gsO) match
              case (Some(gs), None)   => gs.grant[F](ot, objName, grantee)
              case (None, Some(gs))   => gs.revoke[F](ot, objName, grantee)
              case (Some(n), Some(o)) => n.alter[F](o, ot, objName, grantee)
              case _                  => Stream.empty[F]
          )

      extension (ar: AccRoles)
        override def create[F[_]]: Stream[F, Sql] =
          Stream.emits(ar.toList).flatMap(genRole(_, _))

        override def unCreate[F[_]]: Stream[F, Sql] =
          Stream.emits(ar.keys.toList).map(r => Sql.DropRole(r))

        override def alter[F[_]](old: AccRoles): Stream[F, Sql] =
          Stream
            .emits(ar.merge(old))
            .flatMap((grantee, newOgm, oldOgm) =>
              (newOgm, oldOgm) match
                case (Some(ogm), None)  => genRole(grantee, ogm)
                case (None, Some(_))    => Stream.emit(Sql.DropRole(grantee))
                case (Some(n), Some(o)) => alterGrants(grantee, n, o)
                case _                  => Stream.empty
            )
