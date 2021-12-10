import scala.collection.immutable
import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class ConditionIdentification(val value: String) extends StringEnumEntry

object ConditionIdentification
    extends StringEnum[ConditionIdentification]
    with StringCirceEnum[ConditionIdentification] {

  val values: immutable.IndexedSeq[ConditionIdentification] = findValues

  case object PreconditionForAllNonSBARSentenceParts extends ConditionIdentification("S << SBAR")
  case object SBARinsideVerbPhrase extends ConditionIdentification("S << VP << SBAR")
  case object VerbPhraseSBARAnd extends ConditionIdentification("VBP")

}
