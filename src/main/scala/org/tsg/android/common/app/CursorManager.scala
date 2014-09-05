package org.tsg.android.common.app

import android.app.{LoaderManager, Activity}
import android.content.{Loader, CursorLoader, Context}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.{View, ViewGroup}
import android.widget.SimpleCursorAdapter

import scala.collection.immutable.ListMap

object CursorManager {

}

trait CursorManager extends LoaderManager.LoaderCallbacks[Cursor] {

  def context: Context

  private var _uri: Uri = null

  private var _select: Seq[String] = null

  private var _where: String = ""

  private var _args: Seq[Any] = Seq()

  private var _orderBy: String = ""

  def query(uri: Uri, select: Seq[String], where: String = "", args: Seq[Any] = Seq(), orderBy: String = ""): Unit = {
    _uri = uri
    _select = select
    _where = where
    _args = args
    _orderBy = orderBy
  }

  private var _cursor: Cursor = null

  def cursor = _cursor

  def changeCursor(c: Cursor): Boolean = {
    if (_cursor == c) {
      false
    } else {
      _cursor = c
      true
    }
  }

  override def onCreateLoader(id: Int, b: Bundle): Loader[Cursor] = new CursorLoader(
    context,
    _uri,
    _select.toArray,
    _where,
    (for {arg <- _args} yield arg.toString).toArray,
    _orderBy
  )

  override def onLoaderReset(l: Loader[Cursor]): Unit = changeCursor(null)

  override def onLoadFinished(l: Loader[Cursor], c: Cursor): Unit = changeCursor(c)
}

trait CursorLoaderManager extends LoaderManager.LoaderCallbacks[Cursor] {
  def ctx: Context

  def contentUri: Uri

  def projection: ListMap[String, Int]

  def selection: String

  def selectionArgs: Seq[String]

  def sortOrder: String

  def changeCursor(cursor: Cursor): Unit

  override def onCreateLoader(id: Int, args: Bundle) = new CursorLoader(
    ctx,
    contentUri,
    projection.keys.toArray,
    selection,
    selectionArgs.toArray,
    sortOrder
  )

  override def onLoadFinished(loader: Loader[Cursor], data: Cursor) = changeCursor(data)

  override def onLoaderReset(loader: Loader[Cursor]) = changeCursor(null)
}


trait SimpleCursorManager extends CursorLoaderManager with SimpleCursorAdapter.ViewBinder {

  def layout: Int

  def modView(position: Int, view: View): View = view

  def changeCursor(cursor: Cursor): Unit = adapter.changeCursor(cursor)

  lazy val adapter = {
    val a = new SimpleCursorAdapter(ctx, layout, null, projection.keys.toArray, projection.values.toArray, 0) {
      override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
        SimpleCursorManager.this.modView(position, super.getView(position, convertView, parent))
      }
    }
    a.setViewBinder(this)
    a
  }
}

trait FragmentPagerAdapterCursorManager extends FragmentStatePagerAdapter with CursorLoaderManager {
  var cursor: Cursor = null

  def changeCursor(c: Cursor) {
    if (cursor == c) {
      return
    }
    cursor = c
    notifyDataSetChanged()
  }

  override def getCount: Int = if (cursor == null) 0 else cursor.getCount

  override def getItemPosition(obj: AnyRef): Int = PagerAdapter.POSITION_UNCHANGED
}

abstract class RecyclerCursorAdapter[A <: RecyclerView.ViewHolder](implicit act: Activity)
  extends RecyclerView.Adapter[A]
  with CursorManager {

  def context = act

  override def changeCursor(c: Cursor): Boolean = {
    val changed = super.changeCursor(c)
    if (changed) {
      act.runOnUiThread(new Runnable {
        override def run(): Unit = notifyDataSetChanged()
      })
    }
    changed
  }

  override def getItemCount: Int = if (cursor == null) 0 else cursor.getCount
}