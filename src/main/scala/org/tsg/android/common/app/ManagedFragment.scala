package org.tsg.android.common.app

import android.app.Fragment
import android.view.View

trait FragmentViewFinder {
  def getView: View

  def findView[T <: View](id: Int): Option[T] = if (getView == null) None else Option(getView.findViewById(id).asInstanceOf[T])
}

trait ActivityViewFinder {
  def findViewById(id: Int): View

  def findView[T <: View](id: Int): Option[T] = Option(findViewById(id).asInstanceOf[T])
}

trait ManagedFragment extends Fragment {
  // this: Viewer =>
  private type Func = () => Unit

  protected var pauseFuncs: Seq[Func] = Seq()
  protected var resumeFuncs: Seq[Func] = Seq()

  def arg[T](x: String): T = getArguments.get(x).asInstanceOf[T]

  def pauseFunc(fs: Func*): Unit = pauseFuncs = pauseFuncs ++ fs

  def resumeFunc(fs: Func*): Unit = resumeFuncs = resumeFuncs ++ fs

  override def onResume(): Unit = {
    super.onResume()
    resumeFuncs.foreach(_())
  }

  override def onPause(): Unit = {
    pauseFuncs.foreach(_())
    super.onPause()
  }
}