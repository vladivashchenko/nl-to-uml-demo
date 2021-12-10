import enumeratum.values.{ StringCirceEnum, StringEnum, StringEnumEntry }

sealed abstract class DependencyType(val value: String) extends StringEnumEntry

object DependencyType extends StringEnum[DependencyType] with StringCirceEnum[DependencyType] {

  val values = findValues

  case object NsubjXsubj extends DependencyType("nsubj:xsubj")
  case object Nsubj extends DependencyType("nsubj")
  case object Root extends DependencyType("ROOT")
  case object Cop extends DependencyType("cop")
  case object Mark extends DependencyType("mark")
  case object Xcomp extends DependencyType("xcomp")
  case object Advmod extends DependencyType("advmod")
  case object Mwe extends DependencyType("mwe")
  case object Nummod extends DependencyType("nummod")
  case object Dobj extends DependencyType("dobj")
  case object Case extends DependencyType("case")
  case object Det extends DependencyType("det")
  case object NmodAt extends DependencyType("nmod:at")
  case object Punct extends DependencyType("punct")
  case object Compound extends DependencyType("compound")
  case object Cc extends DependencyType("cc")
  case object ConjAnd extends DependencyType("conj:and")
  case object Obj extends DependencyType("onj")

}
