package sfenv
package rules

import org.virtuslab.yaml.*

object Util:
  extension [T](x: Either[String, T])
    def value: T =
      x match
        case Right(y) => y
        case Left(y)  => throw new RuntimeException(y)

  given [T]: Conversion[Either[String, T], Either[ConstructError, T]] with
    def apply(x: Either[String, T]) = x.left.map(ConstructError.from)
