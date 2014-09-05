package org.tsg.android.common.app

import android.app.Activity
import android.os.Build
import android.support.v4.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.view.animation.TranslateAnimation
import android.view.{View, ViewGroup}

class DrawerTogglePushContent(act: Activity, layout: DrawerLayout, drawer: ViewGroup, content: ViewGroup, imageRes: Int, openDescRes: Int, closeDescRes: Int)
  extends ActionBarDrawerToggle(act, layout, imageRes, openDescRes, closeDescRes) {

  var lastTranslate = 0f

  override def onDrawerSlide(drawerView: View, slideOffset: Float): Unit = {
    val moveFactor = drawer.getWidth * slideOffset
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      content.setTranslationX(moveFactor)
    } else {
      val anim = new TranslateAnimation(lastTranslate, moveFactor, 0f, 0f)
      anim.setDuration(0)
      anim.setFillAfter(true)
      content.startAnimation(anim)
      lastTranslate = moveFactor
    }
  }
}
