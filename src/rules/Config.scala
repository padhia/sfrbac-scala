package sfenv
package rules

import org.virtuslab.yaml.*

case class Config(
    secadm: String,
    dbadm: String,
    database: String,
    schema: String,
    warehouse: String,
    acc_role: String,
    wacc_role: String,
    fn_role: String,
    app_id: String
) derives YamlDecoder
