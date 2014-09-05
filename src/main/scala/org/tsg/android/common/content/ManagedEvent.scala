package org.tsg.android.common.content

import android.app.Activity
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import org.tsg.android.common.app.ManagedFragment
import org.tsg.android.common.content.ManagedEvent.{ObserverFunc, PackedObserver, PackedReceiver, ReceiverFunc}
import org.tsg.android.common.util.Logger

import scala.collection.mutable

trait ManagedEvent[A, B, C] {

  private var evs = new mutable.HashMap[A, Boolean]()
  private var act: Option[Activity] = None

  protected val log = new Logger("ManagedEvent", Log.ERROR)

  protected def registerEvent(a: Activity, e: A): Unit

  protected def unregisterEvent(a: Activity, e: A): Unit

  protected def makeEvent(c: C, b: B): A

  def purge(): Unit = synchronized {
    log.d("purging all events")
    unregisterAll()
    evs = evs.empty
    act = None
  }

  def registerActivity(a: Activity): Unit = synchronized {
    log.d("registering activity")
    unregisterAll()
    act = Option(a)
    registerAll()
  }

  def unregisterActivity(a: Activity): Unit = synchronized {
    log.d("unregister activity")
    if (act.isEmpty) {
      log.e("No activity registered.")
    } else {
      unregisterAll()
    }
  }

  def register(events: A*): Unit = synchronized {
    log.d("registering events")
    for (e <- events) {
      if (!evs.contains(e)) {
        evs(e) = false
      }
      for (a <- act) {
        registerEvent(a, e)
        evs(e) = true
      }
    }
  }

  def unregister(events: A*): Unit = synchronized {
    log.d("unregister events")
    for (e <- events) {
      if (!evs.contains(e)) {
        log.d("Attempting to unregister event not managed.")
      }
      for (a <- act) {
        try {
          unregisterEvent(a, e)
        } catch {
          case e: IllegalArgumentException => log.e("unregister event failed", e)
          case t: Throwable => throw t
        }
      }
      evs.remove(e)
    }
  }

  def registerFunc(c: C, b: B): Unit = synchronized {
    log.d("register func")
    val e = makeEvent(c, b)
    register(e)
  }

  def registerFragmentFunc(c: C, b: B)(implicit frag: ManagedFragment): Unit = synchronized {
    log.d("register fragment func")
    val e = makeEvent(c, b)
    frag.resumeFunc(() => register(e))
    frag.pauseFunc(() => unregister(e))
  }

  private def unregisterAll(): Unit = {
    log.d("unregister all events")
    for {
      a <- act
      (e, init) <- evs
      if init
    } {
      evs(e) = false
      try {
        unregisterEvent(a, e)
      } catch {
        case e: IllegalArgumentException => log.e("unregister failed", e)
        case t: Throwable => throw t
      }
    }
  }

  private def registerAll(): Unit = {
    log.d("register all events")
    for {
      a <- act
      (e, init) <- evs
      if !init
    } {
      evs(e) = true
      try {
        registerEvent(a, e)
      } catch {
        case e: IllegalArgumentException => log.e("register failed", e)
        case t: Throwable => throw t
      }
    }
  }
}

object ManagedEvent {
  type ReceiverFunc = (PackedReceiver, Intent) => Unit
  type ObserverFunc = (PackedObserver, Boolean) => Unit

  abstract class PackedReceiver extends BroadcastReceiver {
    def filter: IntentFilter
  }

  abstract class PackedObserver(handler: Handler) extends ContentObserver(handler) {
    def uri: Uri
  }

}

class ReceiverImpl extends ManagedEvent[PackedReceiver, ReceiverFunc, String] {

  override protected def makeEvent(s: String, f: ReceiverFunc): PackedReceiver =
    new PackedReceiver {
      val filter = new IntentFilter(s)

      override def onReceive(context: Context, intent: Intent): Unit = f(this, intent)
    }

  override protected def registerEvent(a: Activity, r: PackedReceiver): Unit = {
    log.d("receiver register event for " + r.filter.getAction(0))
    a.registerReceiver(r, r.filter)
  }

  override protected def unregisterEvent(a: Activity, r: PackedReceiver): Unit = {
    log.d("receiver unregister event for " + r.filter.getAction(0))
    a.unregisterReceiver(r)
  }
}

object Receiver extends ReceiverImpl

class ObserverImpl extends ManagedEvent[PackedObserver, ObserverFunc, Uri] {

  override protected def makeEvent(u: Uri, f: ObserverFunc): PackedObserver =
    new PackedObserver(new Handler) {
      override def uri: Uri = u

      override def onChange(selfChange: Boolean): Unit = f(this, selfChange)
    }

  override protected def registerEvent(a: Activity, o: PackedObserver): Unit = {
    log.d("observer register event for " + o.uri.toString)
    a.getContentResolver.registerContentObserver(o.uri, false, o)
  }

  override protected def unregisterEvent(a: Activity, o: PackedObserver): Unit = {
    log.d("observer unregister event for " + o.uri.toString)
    a.getContentResolver.unregisterContentObserver(o)
  }
}

object Observer extends ObserverImpl
