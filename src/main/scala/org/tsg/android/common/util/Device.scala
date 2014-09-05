package org.tsg.android.common.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

object Device {

  implicit private var ctx: Context = null

  def apply(context: Context): Unit = ctx = context

  def windowManager(implicit ctx: Context) = ctx.getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager]

  /** Backwards compatible method for getting display size. */
  def displaySize: Point = {
    val display = windowManager.getDefaultDisplay
    val size = new Point

    if (Build.VERSION.SDK_INT >= 13) {
      display.getSize(size)
    } else {
      size.x = display.getWidth
      size.y = display.getHeight
    }

    size
  }

  def displayMetrics = {
    val metrics = new DisplayMetrics
    windowManager.getDefaultDisplay.getMetrics(metrics)
    metrics
  }

  def displayDensity = displayMetrics.density

  /** smallest width size qualifiers */
  def sw600dp: Boolean = displaySize.x >= dipValue(600)

  def sw720dp: Boolean = displaySize.x >= dipValue(720)

  /** dip value of f returned in pixels, accepts float for fuzzy pixels. */
  def dipValue(f: Float): Int = (f * displayDensity + 0.5f).toInt

  def dipValue(n: Int): Int = dipValue(n.toFloat)

  def dipValue(d: Double): Int = dipValue(d.toFloat)
}