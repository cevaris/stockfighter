package com.cevaris.stockfighter.common.util

case class MinMaxRange[A](min: A, max: A)(implicit ordering: Ordering[A]) {
  /**
   * Inclusive check to see if value falles within the range.
   *
   * @param a
   *
   * @return Boolean
   */
  def includes(a: A): Boolean =
    ordering.lteq(min, a) && ordering.gteq(max, a)
}
