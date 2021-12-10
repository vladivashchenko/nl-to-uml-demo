object DiagramEntity {
  case class Action(
    index: Int,
    value: String,
  )

  case class PreCondition(
    value: String,
    objects: List[Object]
  )

  case class PostCondition(
    value: String,
    objects: List[Object]
  )

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
    objectsWithAction: List[Object]
  )
}
