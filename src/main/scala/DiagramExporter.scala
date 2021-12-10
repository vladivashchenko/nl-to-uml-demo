import java.io.{ FileOutputStream, OutputStream }
import scala.util.{ Failure, Success, Try }
import DiagramEntity.{ Executor, Object, UseCase }
import net.sourceforge.plantuml.SourceStringReader

class DiagramExporter {
  val startUml: String = "@startuml"
  val endUml: String = "@enduml"

  def generateUseCaseDiagram(useCases: List[UseCase]): Either[String, String] = {
    val source: String = generateDiagramSource(useCases)
    val filename = "case_class.png"
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

  private def generateDiagramSource(useCases: List[UseCase]): String = {
    useCaseDiagram(useCases.flatMap { useCase =>
      val executors: List[Executor] = useCase.executors
      val actors: List[String] = useCase.executors.map(_.value).distinct.map { executor =>
        s" actor ${executor.replace(" ", "_").toLowerCase}"
      }
      actors ++ useCase.objectsWithAction.flatMap { obj =>
        executors.map { executor =>
          if (executor.action == obj.action) {
            s"${executor.value.replace(" ", "_").toLowerCase} -- (${obj.action.value} ${obj.value})"
          } else {
            s"${executor.value.replace(" ", "_").toLowerCase} -- (${executor.action.value})"
          }
        }
      }
    })

  }

  def useCaseDiagram(useCases: List[String]): String = {
    s"""
       |$startUml
       |${useCases.mkString("\n")}
       |$endUml
       |""".stripMargin
  }
}
