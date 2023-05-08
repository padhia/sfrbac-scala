package sfenv
package rules

import envr.{Grantables, ObjType, RoleName}

type AccRoles = Map[String, Map[ObjType, List[String]]]

extension (ar: AccRoles)
  def resolve(db: String, sch: String)(using n: NameResolver): envr.AccRoles =
    def role(acc: String) = RoleName.Database(n.db(db), n.acc(db, sch, acc))

    def grantables(t: String, ps: List[String]) = t match
      case "ROLE" => Grantables.Roles(ps.map(role))
      case _      => Grantables.Privileges(ps)

    def resolvePriv(ops: Map[ObjType, List[String]]) =
      ops.map((t, ps) => (t.toUpperCase(), grantables(t.toUpperCase(), ps.map(_.toUpperCase()))))

    ar.map((k, v) => (role(k), resolvePriv(v)))

  def resolve(wh: String)(using n: NameResolver): envr.AccRoles =
    def role(acc: String) = RoleName.Account(n.wacc(wh, acc))

    def grantables(t: String, ps: List[String]) = t match
      case "ROLE" => Grantables.Roles(ps.map(role))
      case _      => Grantables.Privileges(ps)

    def resolvePriv(ops: Map[ObjType, List[String]]) =
      ops.map((t, ps) => (t.toUpperCase(), grantables(t.toUpperCase(), ps.map(_.toUpperCase()))))

    ar.map((k, v) => (role(k), resolvePriv(v)))
