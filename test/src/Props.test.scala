package sfenv
package envr

import cats.syntax.all.*

import Props.propsToStrSeq
import PropVal.*
import munit.FunSuite

class PropsTests extends FunSuite:
  test("Props - Simple"):
    val testProps = Map("STR_PROP" -> Str("STR_VAL"), "NUM_PROP" -> Num(2), "BOOL_PROP" -> Bool(true))
    val expected  = "STR_PROP = STR_VAL NUM_PROP = 2 BOOL_PROP = TRUE"
    assert(clue(testProps.propsToStrSeq.mkString_(" ")) == expected)

  test("Props - LC"):
    val testProps = Map("str_prop" -> Str("str_val"), "num_prop" -> Num(2), "bool_prop" -> Bool(true))
    val expected  = "STR_PROP = str_val NUM_PROP = 2 BOOL_PROP = TRUE"
    assert(clue(testProps.propsToStrSeq.mkString_(" ")) == expected)

  test("Props - spaces in string"):
    val testProps = Map("STR_PROP" -> Str("value with spaces"))
    val expected  = "STR_PROP = 'value with spaces'"
    assert(clue(testProps.propsToStrSeq.mkString_(" ")) == expected)

  test("Props - non-alpha string"):
    val testProps = Map("SIZE" -> Str("2XLARGE"))
    val expected  = "SIZE = '2XLARGE'"
    assert(clue(testProps.propsToStrSeq.mkString_(" ")) == expected)
