import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class VerbTag(val value: String) extends StringEnumEntry

object VerbTag extends StringEnum[VerbTag] with StringCirceEnum[VerbTag] {

  val values = findValues

  case object Verb extends VerbTag("VB")
  case object ThirdPersonSingularPresentVerb extends VerbTag("VBZ")
  case object NonThirdPersonSingularPresentVerb extends VerbTag("VBP")
  case object PastTenseVerb extends VerbTag("VBD")
  case object PluralProperNoun extends VerbTag("VBN")
  case object Gerund extends VerbTag("VBG")

}
