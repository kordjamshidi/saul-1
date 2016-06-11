package edu.illinois.cs.cogcomp.saul.classifier.SL_model

import edu.illinois.cs.cogcomp.lbjava.learn.LinearThresholdUnit
import edu.illinois.cs.cogcomp.saul.classifier.{ ConstrainedClassifier, SparseNetworkLBP }
import edu.illinois.cs.cogcomp.saul.datamodel.node.Node
import edu.illinois.cs.cogcomp.sl.core.{ AbstractInferenceSolver, IInstance, IStructure }
import edu.illinois.cs.cogcomp.sl.util.WeightVector

import scala.Array._
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/** Created by Parisa on 12/8/15.
  */
class Saul_SL_Inference[HEAD <: AnyRef](factors: List[ConstrainedClassifier[_, HEAD]], ltuTemplates: ListBuffer[Array[Float]], node: Node[HEAD])(implicit t: ClassTag[HEAD]) extends AbstractInferenceSolver {
  val a = factors
  val dataM = node

  override def getBestStructure(weight: WeightVector, ins: IInstance): IStructure = {

    //updates the weights of all factors in a
    updateWeights(weight)

    // Now make the predictions using the updated constrained classifiers
    val myStruct = makePredictions(ins)
    myStruct
  }

  override def getLoss(ins: IInstance, gold: IStructure, pred: IStructure): Float = {
    var TotalLoss: Float = 0
    val myGold = gold.asInstanceOf[Saul_SL_Label_Structure[HEAD]]
    val myPred = pred.asInstanceOf[Saul_SL_Label_Structure[HEAD]]
    var count = 0
    println(ins.asInstanceOf[Saul_SL_Instance[HEAD]].head)
    a.foreach {
      x =>
        var localLoss = 0
        // val oracle: Classifier = x.onClassifier.getLabeler()
        val candidates = x.getCandidates(ins.asInstanceOf[Saul_SL_Instance[HEAD]].head)
        candidates.foreach {
          ci =>
            if (myGold.labels(count) != (myPred.labels(count)))
              localLoss = localLoss + 1
            count = count + 1
        }
        if (candidates.size != 0)
          TotalLoss = TotalLoss + localLoss / candidates.size
    }
    TotalLoss = TotalLoss / factors.size
    println("Loss=" + TotalLoss)
    TotalLoss
  }

  override def getLossAugmentedBestStructure(weight: WeightVector, ins: IInstance, goldStructure: IStructure): IStructure = {

    val myIns = ins.asInstanceOf[Saul_SL_Instance[HEAD]]
    val FactorsNum = a.size

    //set the weights of the classifiers with the trained weight so far
    updateWeights(weight)

    // augment all factors with loss for finding mvc
    a.foreach {
      cf =>
        cf.onClassifier.classifier.setLossFlag()
        cf.onClassifier.classifier.setCandidates(cf.getCandidates(myIns.head).size * FactorsNum)
    }
    // Now make the predictions using the loss augmented inference
    val myStruct = makePredictions(ins)

    //make the classifier to use its normal inference without considering loss in the future calls
    a.map(x => x.onClassifier.classifier.unsetLossFlag())
    myStruct
  }

  //This module receives a global weight vector and distribute it among factors (i.e. classifiers)

  def updateWeights(weight: WeightVector): Unit =
    {
      var ltu_count = 0
      var offset = 0
      a.foreach {
        cf =>
          for (i <- 0 until cf.onClassifier.classifier.asInstanceOf[SparseNetworkLBP].net.size()) {
            val w1 = ltuTemplates(ltu_count) //cf.onClassifier.asInstanceOf[SparseNetworkLBP].net.get(i).asInstanceOf[LinearThresholdUnit].getParameters.asInstanceOf[LinearThresholdUnit.Parameters].weightVector
            print("w", ltu_count, " size:\t", w1.size)
            val myFactorJoinlyTrainedWeight = weight.getWeightArray.slice(offset, offset + w1.size)

            val exampleFeatureIndexes = ofDim[Int](myFactorJoinlyTrainedWeight.length)
            for (featureIndex <- 0 until myFactorJoinlyTrainedWeight.length) {
              exampleFeatureIndexes(featureIndex) = featureIndex;
            }

            val p = cf.onClassifier.classifier.asInstanceOf[SparseNetworkLBP].net.get(i).asInstanceOf[LinearThresholdUnit].getParameters.asInstanceOf[LinearThresholdUnit.Parameters]
            p.weightVector.clear()
            p.asInstanceOf[LinearThresholdUnit.Parameters].weightVector.scaledAdd(exampleFeatureIndexes, Utils.converFarrayToD(myFactorJoinlyTrainedWeight), 0.0)
            cf.onClassifier.classifier.asInstanceOf[SparseNetworkLBP].net.get(i).asInstanceOf[LinearThresholdUnit].setParameters(p)

            offset = offset + ltuTemplates(ltu_count).length
            ltu_count = ltu_count + 1
          }
      }
    }

  // Uses the current statu of the factors and makes the necessary predictions
  def makePredictions(ins: IInstance): Saul_SL_Label_Structure[HEAD] = {
    val myIns = ins.asInstanceOf[Saul_SL_Instance[HEAD]]
    val myStruct: Saul_SL_Label_Structure[HEAD] = new Saul_SL_Label_Structure[HEAD](factors.toList, myIns.head)

    var labelCount = 0
    a.foreach {
      cf =>
        cf.getCandidates(myIns.head).foreach {
          x =>
            //print(cf.onClassifier.classifier.name.substring(80) + "\t")
            print("gt:" + cf.onClassifier.classifier.getLabeler.discreteValue(x))
            myStruct.labels(labelCount) = cf.onClassifier.classifier.discreteValue(x)
            println("\tmvc:" + myStruct.labels(labelCount))
            labelCount = labelCount + 1
        }
    }
    myStruct
  }
}