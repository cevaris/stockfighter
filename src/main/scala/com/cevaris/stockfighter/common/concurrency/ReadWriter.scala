package com.cevaris.stockfighter.common.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock

trait ReadWriter {
  protected val rwLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  protected def read[A](f: => A): A = try {
    rwLock.readLock().lock(); f
  } finally rwLock.readLock().unlock()

  protected def write[A](f: => A): A = try {
    rwLock.writeLock().lock(); f
  } finally rwLock.writeLock().unlock()

}
