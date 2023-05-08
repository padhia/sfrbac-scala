package sfenv
package envr
package test

val config =
  """|config:
     |  secadm: "RL_{env}_SECADMIN"
     |  dbadm: "RL_{env}_SYSADMIN"
     |  database: "{db}_{env}"
     |  schema: "{sch}"
     |  warehouse: "WH_{env}_{wh}"
     |  acc_role: "{sch}_{acc}"
     |  wacc_role: "_WH_{env}_{wh}_{acc}"
     |  fn_role: "RL_{env}_{role}"
     |  app_id: "APP_{env}_{app}"
     |""".stripMargin
