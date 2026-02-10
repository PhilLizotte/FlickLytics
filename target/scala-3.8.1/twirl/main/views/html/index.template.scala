
package views.html

import _root_.play.twirl.api.TwirlFeatureImports.*
import _root_.play.twirl.api.TwirlHelperImports.*
import scala.language.adhocExtensions
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.api.data.Field
import play.data._
import play.core.j.PlayFormsMagicForJava._
import scala.jdk.CollectionConverters._

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.4*/("""

"""),_display_(/*3.2*/main("Tab Title")/*3.19*/ {_display_(Seq[Any](format.raw/*3.21*/("""
  """),format.raw/*4.3*/("""<h1>Page Header</h1>
  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam sed mi sollicitudin, varius turpis pulvinar, lobortis magna. Etiam consectetur finibus mi at ornare. Pellentesque rhoncus erat ipsum, eget suscipit massa pulvinar ultrices. Nulla non elit ac elit feugiat egestas. Maecenas ac orci at mauris congue tempor. Duis tempor, erat et elementum commodo, sapien elit luctus massa, at auctor sapien ligula a sem. Nullam varius erat et libero scelerisque, id dignissim sem tincidunt. Integer luctus tellus a convallis tempor. Cras augue magna, rhoncus vitae iaculis non, varius ut nibh. Nam sodales lacus at ante ullamcorper porta. Ut tincidunt id nulla in lobortis. Maecenas placerat imperdiet urna vitae interdum. Cras turpis purus, pharetra sodales pharetra sit amet, pharetra sit amet mi. Integer erat odio, cursus ut libero nec, eleifend suscipit lacus.</p>
""")))}),format.raw/*6.2*/("""
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/index.scala.html
                  HASH: d936d0accb1587a536fef3dd147f612f3d03e432
                  MATRIX: 938->1|1034->3|1064->8|1089->25|1128->27|1158->31|2080->924
                  LINES: 28->1|33->1|35->3|35->3|35->3|36->4|38->6
                  -- GENERATED --
              */
          