package sfenv
package rules

import io.circe.*

import scala.compiletime.constValueTuple
import scala.deriving.Mirror

import envr.{Props, PropVal}

object Util:
  def fromJsonObject(x: JsonObject): Props =
    x.toMap.view
      .mapValues(x =>
        x.asBoolean
          .map(PropVal.Bool(_))
          .orElse(x.asString.map(PropVal.Str(_)))
          .orElse(x.asNumber.flatMap(_.toBigDecimal.map(PropVal.Num(_))))
      )
      .collect { case (k, Some(v)) => (k, v) }
      .toMap

  def fromCursor(c: HCursor, excl: List[String]): Props =
    c.value.asObject
      .map(_.filterKeys(k => !excl.contains(k)))
      .map(fromJsonObject)
      .getOrElse(Map.empty)

  inline def fromCursor[A <: Product](c: HCursor)(using m: Mirror.ProductOf[A]): Props =
    fromCursor(c, constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]])
