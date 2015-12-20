package edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent
import edu.illinois.cs.cogcomp.lbj.pos.POSBaselineLearner
import edu.illinois.cs.cogcomp.lbjava.learn.{ SparseAveragedPerceptron, SparseNetworkLearner }
import edu.illinois.cs.cogcomp.saul.classifier.Learnable
import edu.illinois.cs.cogcomp.saul.constraint.ConstraintTypeConversion._

import edu.illinois.cs.cogcomp.saulexamples.nlp.POSTagger.POSDataModel._

object POSClassifiers {
  /** After POSTaggerKnown and POSTaggerUnknown are trained,
    * this classifier will return the prediction of POSTaggerKnown if
    * the input word was observed during training or of POSTaggerUnknown
    * if it wasn't.
    */
  object POSClassifier extends Learnable[Constituent](POSDataModel) {
    def label = POSLabel
    override lazy val classifier = new SparseNetworkLearner()
  }

  object POSTaggerKnown extends Learnable[Constituent](POSDataModel) {
    def label = POSLabel
    override def feature = using(wordForm, labelOrBaseline, labelTwoBefore) //, labelOneBefore,
    //          labelOneAfter, labelTwoAfter, L2bL1b, L1bL1a, L1aL2a)
    override lazy val classifier = new SparseNetworkLearner {
      val p = new SparseAveragedPerceptron.Parameters()
      p.learningRate = .1
      p.thickness = 2
      baseLTU = new SparseAveragedPerceptron(p)
    }
  }

  object POSTaggerUnknown extends Learnable[Constituent](POSDataModel) {
    def label = POSLabel
    override lazy val classifier = new SparseNetworkLearner()
  }

  object BaselineClassifier extends Learnable[Constituent](POSDataModel) {
    def label = POSLabel
    override def feature = using(wordForm)
    override lazy val classifier = new POSBaselineLearner()
  }

  object MikheevClassifier extends Learnable[Constituent](POSDataModel) {
    def label = POSLabel
    override def feature = using(wordForm)
    override lazy val classifier = new MikheevLearner
  }
}
