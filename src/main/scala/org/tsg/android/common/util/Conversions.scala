package org.tsg.android.common.util

import android.app.Activity
import android.app.Fragment
import android.text.{Editable, TextWatcher}
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText

import scala.language.implicitConversions

object Conversions {

}

object ArgumentConversions {
  implicit def string2arg[T <: AnyRef](key: String)(implicit frag: Fragment): T = frag.getArguments.get(key).asInstanceOf[T]
}

object ExtrasConversions {

  case class Packed[T <: Any](s: String) {
    def or(x: T)(implicit act: Activity): T = {
      if (act.getIntent.getExtras == null) return x
      val v = act.getIntent.getExtras.get(s)
      if (v != null) v.asInstanceOf[T] else x
    }
  }

  implicit def string2packed[T <: Any](s: String): Packed[T] = new Packed[T](s)
}

object FragmentConversions {
  def runOnUi(r: => Unit)(implicit frag: Fragment): Unit = {
    frag.getActivity.runOnUiThread(new Runnable {
      override def run(): Unit = r
    })
  }
}

object ViewConversions {
  implicit def int2view[T <: View](id: Int)(implicit root: View): T = root.findViewById(id).asInstanceOf[T]

  class ViewConv[T <: View](v: T) {
    def onClick(r: => Unit): T = {
      v.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = r
      })
      v
    }
  }

  implicit def view2conv[T <: View](v: T): ViewConv[T] = new ViewConv[T](v)

  class EditTextConv[T <: EditText](v: T) {
    def afterTextChanged(r: => Unit): T = {
      v.addTextChangedListener(new TextWatcher {
        override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}

        override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {}

        override def afterTextChanged(s: Editable): Unit = r
      })
      v
    }
  }

  implicit def editText2conv[T <: EditText](v: T): EditTextConv[T] = new EditTextConv[T](v)
}