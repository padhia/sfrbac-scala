package sfenv
package envr

import cats.data.Chain

import org.virtuslab.yaml.*

import scala.compiletime.constValueTuple
import scala.deriving.Mirror

type Props = Map[String, PropVal]

object Props:
  def empty = Map.empty[String, PropVal]

  def printKeys[T](xs: Seq[(String, T)]) = println(s">>> ${xs.map(_._1)}")

  def fromYaml_(exclude: List[String]): YamlDecoder[Props] =
    YamlDecoder[Map[String, Any]].map(_.filter((k, _) => exclude.forall(_ != k)).map((k, v) => (k, PropVal.fromAny(v))))
  // def fromYaml_(exclude: List[String]): YamlDecoder[Props] = YamlDecoder { case Node.MappingNode(mappings, _) =>
  //   for
  //     ks <- mappings.toSeq.traverse((k, v) => YamlDecoder[String].construct(k).map((_, v)))
  //     vs <- ks.tap(printKeys).filter((k, _) => exclude.forall(_ != k)).tap(printKeys).traverse((k, v) => YamlDecoder[PropVal].construct(v).map((k, _)))
  //   yield vs.toMap
  // }

  inline def fromYaml[A <: Product](using m: Mirror.ProductOf[A]): YamlDecoder[Props] =
    fromYaml_(constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]])

  extension (ps: Props)
    def propsToStrSeq: Chain[String] =
      Chain.fromSeq(ps.toSeq).map((k, v) => s"${k.toUpperCase()} = $v")
