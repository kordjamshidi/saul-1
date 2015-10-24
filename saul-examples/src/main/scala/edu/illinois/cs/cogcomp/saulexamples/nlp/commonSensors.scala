package edu.illinois.cs.cogcomp.saulexamples.nlp

import edu.illinois.cs.cogcomp.annotation.AnnotatorService
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, Sentence, TextAnnotation }
import edu.illinois.cs.cogcomp.core.utilities.ResourceManager
import edu.illinois.cs.cogcomp.curator.CuratorFactory
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory
import edu.illinois.cs.cogcomp.saulexamples.data.Document

import scala.collection.JavaConversions._

/** an object containing many popular sensors used in examples */
object commonSensors {

  def textCollection(x: List[Document]) = {
    x.map(documentContent)
  }

  def documentContent(x: Document): String = {
    x.getWords.mkString(" ")
  }

  def getSentences(x: TextAnnotation): List[Sentence] = {
    x.sentences().toList
  }

  def textAnnotationSentenceAlignment(ta: TextAnnotation, sentence: Sentence): Boolean = {
    ta.getId == sentence.getSentenceConstituent.getTextAnnotation.getId
  }

  def textAnnotationConstituentAlignment(ta: TextAnnotation, cons: Constituent): Boolean = {
    cons.getTextAnnotation.getId == ta.getId
  }

  def sentenceConstituentAlignment(sentence: Sentence, cons: Constituent): Boolean = {
    cons.getSentenceId == sentence.getSentenceId
  }

  def getConstituents(x: TextAnnotation): List[Constituent] = {
    x.getView(ViewNames.POS).getConstituents.toList
  }

  /** Annotation services */
  def processDocumentWith(annotatorService: AnnotatorService, cid: String, did: String, text: String, services: String*): TextAnnotation = {
    val ta = annotatorService.createBasicTextAnnotation(cid, did, text)
    println(ta.getAvailableViews)
    ta
  }

  def annotateWithCurator(document: Document): TextAnnotation = {
    val config = "./saul-examples/config/caching-curator.properties"
    val rm = new ResourceManager(config)
    val annotatorService = CuratorFactory.buildCuratorClient(rm)
    val content = documentContent(document)
    processDocumentWith(annotatorService, "corpus", document.getGUID, content)
  }

  def annotateWithCurator2(content: String, id: String): TextAnnotation = {
    val annotatorService = CuratorFactory.buildCuratorClient()
    processDocumentWith(annotatorService, "corpus", id, content)
  }

  def annotateWithPipeline(content: String, id: String): TextAnnotation = {
    val annotatorService = IllinoisPipelineFactory.buildPipeline()
    processDocumentWith(annotatorService, "corpus", id, content)
  }
}

