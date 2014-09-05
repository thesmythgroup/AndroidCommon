package org.tsg.android.common

import android.content.Context
import org.tsg.android.common.util.Device

object Common {
  def apply(ctx: Context): Unit = {
    Device(ctx)
  }
}
