package org.tsg.android.common.app

import android.os.Bundle
import android.support.v7.widget.RecyclerView.OnItemTouchListener
import android.support.v7.widget.{DefaultItemAnimator, LinearLayoutManager, RecyclerView}
import android.view.GestureDetector.OnGestureListener
import android.view._

object RecyclerFragment {

  class GestureListener(frag: RecyclerFragment, root: RecyclerView) extends OnGestureListener {

    var _selection: Option[View] = None

    def selection = _selection

    def selection_=(view: Option[View]) = {
      _selection map {
        _.setSelected(false)
      }
      _selection = view
      _selection map {
        _.setSelected(true)
      }
    }

    def select(e: MotionEvent): Option[View] = Option(root.findChildViewUnder(e.getX, e.getY))

    override def onSingleTapUp(e: MotionEvent): Boolean = {
      selection = select(e)
      selection.map({ case s => if (s.isSoundEffectsEnabled) s.playSoundEffect(SoundEffectConstants.CLICK)})
      selection.map(root.getChildPosition).map(frag.onItemClick)
      true
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {
      selection = None
      true
    }

    override def onShowPress(e: MotionEvent): Unit = {
      selection = select(e)
    }

    override def onLongPress(e: MotionEvent): Unit = {}

    override def onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      selection = None
      true
    }

    override def onDown(e: MotionEvent): Boolean = {
      true
    }
  }

  class ItemTouchListener(gd: GestureDetector, l: OnGestureListener) extends OnItemTouchListener {
    override def onTouchEvent(rv: RecyclerView, ev: MotionEvent): Unit = {}

    override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
      if (!gd.onTouchEvent(e)) {
        if (e.getAction == MotionEvent.ACTION_UP) {
          l.onScroll(null, null, -1, -1)
        }
      }
      false
    }
  }

}

trait RecyclerFragment extends ManagedFragment {

  var recycler: RecyclerView = null

  def onItemClick(pos: Int): Unit

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    recycler = new RecyclerView(inflater.getContext)
    recycler.setHasFixedSize(true)
    recycler.setLayoutManager(new LinearLayoutManager(inflater.getContext))
    recycler.setItemAnimator(new DefaultItemAnimator)
    recycler.setSoundEffectsEnabled(true)
    val l = new RecyclerFragment.GestureListener(this, recycler)
    val gd = new GestureDetector(inflater.getContext, l)
    recycler.addOnItemTouchListener(new RecyclerFragment.ItemTouchListener(gd, l))
    recycler
  }
}
