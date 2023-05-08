package sfenv

/** A SqlObj has an ID of type Key and can be operated using Sql
  */
trait SqlObj[T] extends SqlOperable[T]:
  type Key
  extension (x: T) def id: Key
