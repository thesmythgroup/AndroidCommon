package org.tsg.android.common.content

import android.content.Context

class Preferences(context: Context, name: String) {

  private def prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

  def contains(key: String): Boolean = prefs.contains(key)

  def put(name: String, value: Any) {
    value match {
      case v: String => prefs.edit().putString(name, v).commit()
      case v: Int => prefs.edit().putInt(name, v).commit()
      case v: Long => prefs.edit().putLong(name, v).commit()
      case v: Boolean => prefs.edit().putBoolean(name, v).commit()
      case v: Float => prefs.edit().putFloat(name, v).commit()
    }
  }

  def get[T](name: String, defaultVal: T): T = defaultVal match {
    case v: String => prefs.getString(name, v).asInstanceOf[T]
    case v: Int => prefs.getInt(name, v).asInstanceOf[T]
    case v: Long => prefs.getLong(name, v).asInstanceOf[T]
    case v: Boolean => prefs.getBoolean(name, v).asInstanceOf[T]
    case v: Float => prefs.getFloat(name, v).asInstanceOf[T]
  }

}