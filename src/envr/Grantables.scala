package sfenv
package envr

import fs2.Stream

enum Grantables:
  case Roles(rs: List[RoleName])
  case Privileges(ps: List[String])

  def permit[F[_]](revoke: Boolean)(of: ObjType, to: String, grantee: RoleName): Stream[F, Sql] =
    this match
      case Roles(rs)      => Stream.emits(rs.map(r => Sql.RoleGrant(r, grantee, revoke)))
      case Privileges(ps) => Stream.emit(Sql.ObjGrant(of, to, grantee, ps, revoke))

  def grant[F[_]]  = permit[F](false)
  def revoke[F[_]] = permit[F](true)

  def alter[F[_]](other: Grantables, of: ObjType, to: String, grantee: RoleName): Stream[F, Sql] =
    (this, other) match
      case (Roles(rsN), Roles(rsO)) =>
        Roles(rsN.filter(!rsO.contains(_))).grant(of, to, grantee) ++
          Roles(rsO.filter(!rsN.contains(_))).revoke(of, to, grantee)
      case (Privileges(psN), Privileges(psO)) =>
        Privileges(psN.filter(!psO.contains(_))).grant(of, to, grantee) ++
          Privileges(psO.filter(!psN.contains(_))).revoke(of, to, grantee)
      case _ => Stream.empty
