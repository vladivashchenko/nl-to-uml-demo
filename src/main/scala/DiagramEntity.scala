object DiagramEntity {
  case class Action(
    index: Int,
    value: String,
  )

  sealed trait Condition {
    val obj: Object
    val objects: List[Object]
  }

  case class PreCondition(
    obj: Object,
    objects: List[Object]
  ) extends Condition

  case class PostCondition(
    val obj: Object,
    objects: List[Object]
  ) extends Condition

  case class Executor(
    index: Int,
    value: String,
    action: Action
  )
  case class Object(
    value: String,
    action: Action
  )

  case class UseCase(
    executors: List[Executor],
    objectsWithAction: List[Object],
    condition: List[Condition],
  )
}
