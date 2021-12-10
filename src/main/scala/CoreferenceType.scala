import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class CoreferenceType(val value: String) extends StringEnumEntry

object CoreferenceType extends StringEnum[CoreferenceType] with StringCirceEnum[CoreferenceType] {

  val values = findValues

  case object PROPER extends CoreferenceType("PROPER")

}
