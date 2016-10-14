package ssvm

import edu.illinois.cs.cogcomp.saul.datamodel.DataModel

import scala.math._

/**
 * Created by Parisa on 10/14/16.
 */
object randomDataModel extends DataModel{
  val randomNode=node[String]
  val th = Pi/3
  val c = cos(th)
  val s = sin(th)
  val r = scala.util.Random
  r.setSeed(0)

  val randomLabel = property(randomNode) {
    x:String=>
      2 * (if (r.nextGaussian()> 0) 1 else 0) -1
  }

  val randomProperty= property(randomNode) {
    x: String =>
      val p = List(2* r.nextGaussian().toDouble, 0.5*r.nextGaussian().toDouble)
      val p1new = 0 //p(1)+randomLabel(x)
      val p2= List(c*p1new-s*p(1), c*p1new+s*p(1))
      p2
  }
}