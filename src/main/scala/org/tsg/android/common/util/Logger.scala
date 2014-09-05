package org.tsg.android.common.util

import java.lang.reflect.{InvocationTargetException, Method}

import android.util.Log

object Logger {
  def apply(obj: AnyRef, level: Int = Log.DEBUG): Logger = new Logger(obj.getClass.getName, level)
  def apply(tag: String, level: Int): Logger = new Logger(tag, level)
}

class Logger(tag: String, level: Int) {

  private def isLoggable(lvl: Int): Boolean = lvl >= level

  def d(msg: String, t: Throwable = null): Int = if (isLoggable(Log.DEBUG)) Log.d(tag, msg) else -1

  def e(msg: String, t: Throwable = null): Int = if (isLoggable(Log.ERROR)) Log.e(tag, msg) else -1

  def i(msg: String, t: Throwable = null): Int = if (isLoggable(Log.INFO)) Log.i(tag, msg) else -1

  def w(msg: String, t: Throwable = null): Int = if (isLoggable(Log.WARN)) Log.w(tag, msg) else -1

  def v(msg: String, t: Throwable = null): Int = if (isLoggable(Log.VERBOSE)) Log.v(tag, msg) else -1

  def wtf(msg: String, t: Throwable = null): Int = {
    try {
      val m: Method = classOf[Log].getMethod("wtf", classOf[String], classOf[String], classOf[Throwable])
      val i: Integer = m.invoke(null, Array[AnyRef](tag, msg, t)).asInstanceOf[Integer]
      return i.intValue
    } catch {
      case e: IllegalAccessException => e.printStackTrace()
      case e: InvocationTargetException => e.printStackTrace()
      case e: NoSuchMethodException => w("Log.wtf method not supported.")
    }
    Log.e(tag, msg, t)
  }
}