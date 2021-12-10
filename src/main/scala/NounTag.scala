import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class NounTag(val value: String) extends StringEnumEntry

object NounTag extends StringEnum[NounTag] with StringCirceEnum[NounTag] {

  val values = findValues

  case object Noun extends NounTag("NN")
  case object PluralNoun extends NounTag("NNS")
  case object ProperNoun extends NounTag("NNP")
  case object PluralProperNoun extends NounTag("NNPS")
  case object PersonalPronoun extends NounTag("PRP")

}
