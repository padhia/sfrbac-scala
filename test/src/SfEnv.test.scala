package sfenv
package envr
package test

import fs2.*

import munit.FunSuite
import sfenv.Main.toRbac

class RbacTests extends FunSuite:
  import RbacTests.*

  test("create"):
    val ddl = rule1.toRbac("DEV").map(x => x.create.flatMap(_.stream(x.sysAdm)).toList)
    val expected = List(
      "CREATE DATABASE IF NOT EXISTS EDW_DEV DATA_RETENTION_TIME_IN_DAYS = 10 COMMENT = 'EDW core database'",
      "GRANT CREATE DATABASE ROLE, USAGE ON DATABASE EDW_DEV TO ROLE RL_DEV_SECADMIN"
    )
    assert(clue(ddl) == Right(expected))

  test("drop"):
    val ddl      = rule1.toRbac("DEV").map(x => x.unCreate.flatMap(_.stream(x.sysAdm)).toList)
    val expected = List("--DROP DATABASE IF EXISTS EDW_DEV")
    assert(clue(ddl) == Right(expected))

  test("alter - drop"):
    val ddl =
      for
        curr <- rule2.toRbac("DEV")
        prev <- rule1.toRbac("DEV")
      yield curr.alter(prev).flatMap(_.stream(curr.sysAdm)).toList

    val expected = List("--DROP DATABASE IF EXISTS EDW_DEV")
    assert(clue(ddl) == Right(expected))

  test("alter - no change"):
    val ddl =
      for
        curr <- rule1.toRbac("DEV")
        prev <- rule1.toRbac("DEV")
      yield curr.alter(prev).flatMap(_.stream(curr.sysAdm)).toList

    val expected = List.empty[String]
    assert(clue(ddl) == Right(expected))

object RbacTests:
  val rule1 =
    s"""|$config
        |databases:
        |  EDW:
        |    data_retention_time_in_days: 10
        |    comment: EDW core database
        |""".stripMargin

  val rule2 =
    s"""|$config
        |""".stripMargin
