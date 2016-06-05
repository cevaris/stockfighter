package com.cevaris.stockfighter.common.util

import com.cevaris.stockfighter.common.util.Retry.BackoffStrategy
import com.twitter.util.{Duration, Future, Timer}

/**
 * Examples:
 *
 * A retry strategy with exponential backoff, starting at 10 ms, and doubling each time.
 * This strategy retries forever:
 *
 * exponentialRetry(10.millis, 2)
 *
 * The same exponential strategy, but limited to 5 attempts:
 *
 * stopOnceReached(5)(exponentialRetry(10.millis, 2))
 *
 * Now limited to 5 attempts, and a maximum backoff of 1 second (whichever comes first):
 *
 * stopOnceReached(5) _ compose stopOnceReached(1.second) apply exponentialRetry(10.millis, 2)
 *
 * Finally, we have the exponential strategy that retries forever, but is capped at 1 second backoffs.
 *
 * capMaxDuration(1.second)(exponentialRetry(10.millis, 2))
 *
 * Note that capMaxDuration and stopOnceReached do not necessarily commute.
 *
 * capMaxDuration(1.second) compose stopOnceReached(10.second) =/= stopOnceReached(10.second) compose capMaxDuration(1.second)
 *
 * Once you've described your retry strategy, use it with the Retry class:
 *
 * def fetchAGizmo: Future[Gizmo] = ???
 * def retryCallback(attempt: Int, t: Throwable) = logger.error(s"Failed. Starting attempt $attempt", t)
 * Retry(constantRetry(1.second))
 *   .onRetry(retryCallback)
 *   .apply(fetchAGizmo): Future[Gizmo]
 */

object Retry {

  def apply[T](
    backoffStrat: Stream[Duration]
  )(implicit timer: Timer): Retry[T] = new Retry[T](backoffStrat, (_, _) => ())(timer)

  type BackoffStrategy = Stream[Duration]

  def genericRetry(start: Duration, nextDuration: (Duration) => Duration): BackoffStrategy =
    Stream.iterate(start)(nextDuration)

  def exponentialRetry(start: Duration, backoffFactor: Double): BackoffStrategy =
    genericRetry(start, _ * backoffFactor)

  def linearRetry(start: Duration, backoffFactor: Duration): BackoffStrategy =
    genericRetry(start, _ + backoffFactor)

  def constantRetry(start: Duration): BackoffStrategy =
    genericRetry(start, identity)

  def stopOnceReached(maxRetries: Int)(strat: BackoffStrategy): BackoffStrategy =
    strat.take(maxRetries)

  def stopOnceReached(maxTime: Duration)(strat: BackoffStrategy): BackoffStrategy =
    strat.takeWhile(_ <= maxTime)

  def capMaxDuration(maxTime: Duration)(strat: BackoffStrategy): BackoffStrategy =
    strat.map(Ordering[Duration].min(_, maxTime))
}

class Retry[T] private(
  backoffStrat: BackoffStrategy,
  onRetryEffect: (Int, Throwable) => Unit
)(implicit timer: Timer) {

  def apply(f: => Future[T]): Future[T] = {
    def go(currentStrat: BackoffStrategy, retryCount: Int): Future[T] = {
      f.rescue { case t: Throwable =>
        currentStrat match {
          case duration #:: durations =>
            Future.sleep(duration).flatMap { _ =>
              onRetryEffect(retryCount, t)
              go(durations, retryCount + 1)
            }
          case Stream.Empty => Future.exception(t)
        }
      }
    }

    go(backoffStrat, 1)
  }

  /**
   * Add an effectful function to be called on retry.
   *
   * newOnRetryEffect takes an Int representing the
   * current retry attempt starting at 1, and the
   * Throwable that caused the current retry attempt.
   * Chained calls to onRetry sequence the specified effects.
   **/
  def onRetry(newOnRetryEffect: (Int, Throwable) => Unit): Retry[T] = {
    new Retry(
      backoffStrat,
      (i, t) => {
        onRetryEffect(i, t)
        newOnRetryEffect(i, t)
      }
    )
  }
}
