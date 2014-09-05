package org.tsg.android.common.concurrent

import java.util.concurrent._

import scala.concurrent.ExecutionContext

object Exec {

  object Implicits {
    implicit val exec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(3))
  }

}

