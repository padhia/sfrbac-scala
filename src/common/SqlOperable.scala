package sfenv

import fs2.Stream

/** A SqlOperable can be created, un-created (dropped) and altered using one or more Sql
  */
trait SqlOperable[T]:
  extension (x: T)
    def create[F[_]]: Stream[F, Sql]
    def unCreate[F[_]]: Stream[F, Sql]
    def alter[F[_]](y: T): Stream[F, Sql]

object SqlOperable:
  given [T](using SqlObj[T]): SqlOperable[List[T]] with
    extension (xs: List[T])
      override def create[F[_]]: Stream[F, Sql]   = Stream.emits(xs).flatMap(_.create)
      override def unCreate[F[_]]: Stream[F, Sql] = Stream.emits(xs).flatMap(_.unCreate)
      override def alter[F[_]](ys: List[T]): Stream[F, Sql] =
        val alters  = for { x <- xs; y <- ys; if x.id == y.id } yield (x, y)
        val creates = xs.filterNot(x => ys.exists(_.id == x.id))
        val drops   = ys.filterNot(y => xs.exists(_.id == y.id))

        Stream.emits(creates).flatMap(_.create) ++
          Stream.emits(drops).flatMap(_.unCreate) ++
          Stream.emits(alters).flatMap((c, p) => c.alter(p))
