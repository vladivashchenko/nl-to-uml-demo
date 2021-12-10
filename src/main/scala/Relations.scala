import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations
import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class Relations(val value: String) extends StringEnumEntry

object Relations extends StringEnum[Relations] with StringCirceEnum[Relations] {

  val values = findValues

  case object CompoundPart extends Relations("compound:prt")
  case object Nsubj extends Relations("nsubj")
  case object NsubjAgent extends Relations("nsubj.*")
  case object NsubjXSubj extends Relations("nsubj:xsubj")
  case object Dobj extends Relations("dobj")
  case object Obj extends Relations("obj")
  case object Nsubjpass extends Relations("nsubj:pass")
  case object Nmod extends Relations("nmod")
  case object NmodAgent extends Relations("nmod:agent")
  case object OblAgent extends Relations("obl:agent")
  case object Compound extends Relations("compound")
  case object Ccomp extends Relations("ccomp")
  case object AdvclAgent extends Relations("advcl.*")
  case object Case extends Relations("case")
  case object Parataxis extends Relations("parataxis")
  case object Amod extends Relations("amod")
}
