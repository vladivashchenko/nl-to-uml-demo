import java.io.{ FileOutputStream, OutputStream }
import scala.::
import scala.util.{ Failure, Success, Try }
import DiagramEntity.{ Condition, Executor, Object, PostCondition, PreCondition, UseCase }
import net.sourceforge.plantuml.SourceStringReader

class DiagramExporter {
  val startUml: String = "@startuml"
  val endUml: String = "@enduml"
  // activity
  val start: String = "start"
  val end: String = "end"

  def generateActivityDiagram(useCases: List[UseCase]): Either[String, String] = {
    val source: String = generateActivity(useCases)
    val filename = "activity.png"
    generateDiagramFile(filename, source)
  }

  private def generateActivity(useCases: List[UseCase]): String = {

    val a: List[(Object, Option[Executor])] = useCases.flatMap { useCase =>
      val executors: List[Executor] = useCase.executors
      useCase.objectsWithAction.map { o =>
        o -> executors.find { e =>
          e.action == o.action
        }
      }
    }

    val b: Map[String, List[Object]] =
      a.groupBy(_._2.get.value).map {
        case (str, tuples) => str -> tuples.map(_._1)
      }
    activityDiagram(b.flatMap {
      case (executor, objects) =>
        val partitionStart: String = s"partition $executor {"
        val partitionEnd: String = s"}"
        List(partitionStart) ++ objects.map { obj =>
          s": ${obj.action.value} ${obj.value};"
        } ++ List(partitionEnd)

    }.toList)

  }

  def generateUseCaseDiagram(useCases: List[UseCase]): Either[String, String] = {
    val source: String = generateDiagramSource(useCases)
    val filename = "case_class.png"
    generateDiagramFile(filename, source)
  }

  private def generateDiagramSource(useCases: List[UseCase]): String = {
    useCaseDiagram(useCases.flatMap { useCase =>
      val executors: List[Executor] = useCase.executors
      val conditions: List[DiagramEntity.Condition] = useCase.condition
      val actors: List[String] = useCase.executors.map(_.value).distinct.map { executor =>
        s" actor ${executor.replace(" ", "_").toLowerCase}"
      }
      actors ++ useCase.objectsWithAction.flatMap {
        obj =>
          executors.flatMap {
            executor =>
              conditions.find(_.obj.action == executor.action) match {
                case Some(cond) =>
                  cond match {
                    case pre: PreCondition =>
                      pre.objects.map { o =>
                        s"${executor.value.replace(" ", "_").toLowerCase} -- (${o.action.value} ${o.value}): ${pre.obj.action.value}"
                      }
                    case post: PostCondition =>
                      post.objects.map { o =>
                        s"(${o.action.value} ${o.value}) -- (${post.obj.action.value} ${o.value}): ${o.action.value}"
                      }
                  }

                case None =>
                  if (executor.action == obj.action) {
                    List(s"${executor.value.replace(" ", "_").toLowerCase} -- (${obj.action.value} ${obj.value})")
                  } else {
                    List()
                  }
              }
          }
      }.distinct
    })

  }

  private def generateDiagramFile(
    filename: String,
    source: String
  ): Either[String, String] = {
    Try {
      val png: OutputStream = new FileOutputStream(filename)

      val reader: SourceStringReader = new SourceStringReader(source)
      // Write the first image to "png"
      val desc: String = reader.outputImage(png).getDescription
      if (desc == null) {
        Left("Failed to generate case class diagram")
      } else {
        Right(desc)
      }
    } match {
      case Failure(exception) => Left(s"Diagram generation failed with $exception")
      case Success(value) => value
    }
  }

  private def useCaseDiagram(useCases: List[String]): String = {
    val a = s"""
               |$startUml
               |${useCases.mkString("\n")}
               |$endUml
               |""".stripMargin
    println(a)
    a
  }

  private def activityDiagram(useCases: List[String]): String = {
    println(useCases)
    s"""
       |$startUml
       |$start
       |${useCases.mkString("\n")}
       |$end
       |$endUml
       |""".stripMargin
  }
}
