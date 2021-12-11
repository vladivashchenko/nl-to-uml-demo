import java.net.{ URLDecoder, URLEncoder }
import scala.annotation.tailrec
import scala.collection.JavaConverters.asScalaBufferConverter
import DiagramEntity.{ Action, Condition, Executor, Object, PostCondition, PreCondition, UseCase }
import edu.stanford.nlp.trees.Tree
import scalaj.http.{ Http, HttpResponse }
import edu.stanford.nlp.trees.tregex.{ TregexMatcher, TregexPattern }

object Main extends App {

  val actionRelations =
    List(Relations.Nsubj, Relations.NsubjAgent, Relations.Nsubjpass, Relations.Dobj, Relations.CompoundPart)

  def actionsFromSentence(
    tokens: List[Token],
    enhancedPlusPlusDependency: List[EnhancedPlusPlusDependency]
  ): List[Action] = {
    val verbs: List[Token] = tokens.filter(t => VerbTag.values.map(_.value).contains(t.pos))
//    println(s"verbs = $verbs")
    val verbsDependencies: List[EnhancedPlusPlusDependency] =
      enhancedPlusPlusDependency.filter { eppDep =>
//        println(s"$eppDep => ${actionRelations.exists(_.value.matches(eppDep.dep))}")
        actionRelations.exists(_.value.matches(eppDep.dep))
      }
//    println(s"verbsDependencies = $verbsDependencies")

    val actionsFinal: List[Action] = verbsDependencies
      .filter { verbsDep =>
        verbs.exists(t => t.index == verbsDep.governor) ||
        tokens
          .find(_.index == verbsDep.dependent)
          .map(_.pos)
          .exists(NounTag.values.map(_.value).contains(_))
      }
      .map { dep =>
        Action(dep.governor, dep.governorGloss)
      }
      .distinct
//    println(s"actionsFinal = $actionsFinal")

    val actionsInLemmaForm: List[Action] = actionsFinal.flatMap {
      case action =>
        tokens
          .collect {
            case t if t.originalText == action.value =>
              Action(action.index, t.lemma)
          }
    }
//    println(s"actionsInLemmaForm = $actionsInLemmaForm")

    actionsInLemmaForm.map {

      case action =>
        val maybePart: Option[EnhancedPlusPlusDependency] = verbsDependencies.find { dep =>
          dep.dep == Relations.CompoundPart.value && dep.governor == action.index
        }
        maybePart
          .map(p => action.copy(value = s"${action.value} ${p.dependentGloss}"))
          .getOrElse(action)
    }
  }

  @tailrec
  def getUseCases(
    matcher: TregexMatcher,
    useCases: List[String],
  ): List[String] = {
    if (matcher.findNextMatchingNode()) {
      val `match`: Tree = matcher.getMatch
      // do what we want to do with the subtree
      val actor: Option[String] = `match`
        .getChild(0)
        .labeledYield()
        .asScala
        .toList
        .collectFirst {
          case labelledWord if NounTag.values.exists(_.value == labelledWord.tag().value()) =>
            labelledWord.word()
        }
      val action: String = `match`.getChild(1).yieldWords().asScala.mkString(" ")

      val actualUseCases: List[String] = actor match {
        case Some(actor) =>
          List(
            s" actor ${actor.replace(" ", "_").toLowerCase}",
            s"${actor.replace(" ", "_").toLowerCase} -- ($action)",
          )
        case None => List.empty
      }
      getUseCases(
        matcher,
        useCases ++ actualUseCases
      )
    } else {
      useCases
    }
  }

  val diagramExporter: DiagramExporter = new DiagramExporter
  val nlpDecoder: NlpDecoder = new NlpDecoder
  val url: String = "http://localhost:9000"

  val properties0: String =
    "{\"annotators\": \"coref\", \"date\": \"2021-12-05T03:11:10\"}"

  val properties: String =
    "{\"annotators\": \"depparse\", \"date\": \"2021-12-05T03:11:10\"}"

  val address0: String =
    String.format(s"$url/%s", URLEncoder.encode(s"?properties=$properties0", "ISO-8859-1"))

  val address: String =
    String.format(s"$url/%s", URLEncoder.encode(s"?properties=$properties", "ISO-8859-1"))

  private val requirement: String =
    List(
//      "Distributed lock ensures that request was processed successfully by server.",
//      "Dole was defeated by Clinton.",
//      "Before she ate the cake, Emma shut down her computer and she visited Tony in his room.",
      "Before exiting the room, user should turn out lights.",
      //      "Digicel requires us to set up a notification gateway API between their website for voucher generation.",
      //      "The idea behind this is to use the client app for receiving voucher codes from the Digicel website as push notification/inbox.",
//      "Users are able to play more than one game at a time.",
      //      "User is able to play more than one game at a time.",
      //      "Each user has individual profiles"
    ).mkString("\n")

  val response0: HttpResponse[String] = Http(address0)
    .postData(
      requirement
    )
    .timeout(60000, 60000)
    .asString

//  println(response0.body)

  val decoded0: Either[String, CoreNlpResult] = nlpDecoder.decodeNlpResponse(response0.body)

  val corefsMap: Either[String, Map[String, String]] = decoded0 match {
    case Left(value) => Left(value)
    case Right(value) =>
      Right(
        value.corefs.flatMap {
          case (_, coreferences) =>
            val a: Map[Boolean, List[Coreference]] = coreferences
              .groupBy(a => CoreferenceType.withValueOpt(a.`type`).contains(CoreferenceType.PROPER))
            val keys: List[String] = a(false).map(_.text)
            val value: String = a(true).head.text
            keys.map { key =>
              key -> value
            }.toMap
        }
      )
  }

  val formattedAfterCoreference: Either[String, String] =
    decoded0.map(
      _.sentences
        .flatMap(_.tokens)
        .map { t =>
          if (NounTag.withValueOpt(t.pos).contains(NounTag.PersonalPronoun)) {
            t.before + corefsMap.map(_.getOrElse(t.originalText, t.originalText)).getOrElse(t.originalText) + t.after
          } else {
            t.before + t.originalText + t.after
          }
        }
        .mkString
    )
  formattedAfterCoreference match {
    case Left(value) => Left(value)
    case Right(value) =>
      val response: HttpResponse[String] = Http(address)
        .postData(
          value
        )
        .timeout(60000, 60000)
        .asString

      val decoded: Either[String, CoreNlpResult] = nlpDecoder.decodeNlpResponse(response.body)

      decoded match {
        case Left(value) => Left(value)
        case Right(res) =>
          val useCases: List[UseCase] = res.sentences.map { sentence =>
            val tokens: List[Token] = sentence.tokens
            val enhancedPlusPlusDependencies: List[EnhancedPlusPlusDependency] = sentence.enhancedPlusPlusDependencies
            sentence.enhancedPlusPlusDependencies.foreach(println)

            val actions: List[Action] = actionsFromSentence(tokens, enhancedPlusPlusDependencies)
            println(s"actions = $actions ")
            val executors: List[Executor] = actions.flatMap { action =>
              enhancedPlusPlusDependencies
                .collect {
                  case eppDep if Relations.Nsubj.value == eppDep.dep && eppDep.governor == action.index =>
                    val token: (Int, String) = tokens
                      .find(_.originalText == eppDep.dependentGloss)
                      .map { a =>
                        a.index -> a.lemma
                      }
                      .get
                    Executor(token._1, token._2, action)
                  case eppDep
                      if (eppDep.dep.matches(Relations.NmodAgent.value)
                        || eppDep.dep.matches(Relations.OblAgent.value))
                        && eppDep.governor == action.index =>
                    val token: (Int, String) = tokens
                      .find(_.originalText == eppDep.dependentGloss)
                      .map { a =>
                        a.index -> a.lemma
                      }
                      .get
                    Executor(token._1, token._2, action)
                }
                .map { ex =>
                  val maybePart: Option[EnhancedPlusPlusDependency] = enhancedPlusPlusDependencies.find { dep =>
                    dep.dep == Relations.Amod.value && dep.governor == ex.index
                  }
                  maybePart
                    .map { dep =>
                      ex.copy(
                        value = s"${tokens.find(_.originalText == dep.dependentGloss).map(_.lemma).get} ${ex.value}"
                      )
                    }
                    .getOrElse(ex)

                }
            }
            println(s"executors = $executors ")
            val objectsWithActions: List[Object] = actions
              .flatMap { a =>
                enhancedPlusPlusDependencies.collect {
                  case eppDep if Relations.Dobj.value == eppDep.dep && eppDep.governor == a.index =>
                    Object(eppDep.dependentGloss, a)
                  case eppDep if eppDep.dep.matches(Relations.Nsubjpass.value) && eppDep.governor == a.index =>
                    Object(eppDep.dependentGloss, a)
                }
              }
            val objects: List[Object] = objectsWithActions ++ actions
                    .filterNot(a => objectsWithActions.exists(o => o.action == a))
                    .map { action =>
                      Object("", action)
                    }

            println(s"objects = $objects ")
            val tree: Tree = Tree.valueOf(sentence.parse)
            val patternMW: TregexPattern = TregexPattern.compile("SBAR")
            val matcher: TregexMatcher = patternMW.matcher(tree)
            val conditionFound: Boolean = matcher.findNextMatchingNode

            val conditions: List[Condition] = if (conditionFound) {
              enhancedPlusPlusDependencies.collect {
                case eppDep if eppDep.dep == Relations.AdvclBefore.value =>
                  val objs: List[Object] = objects.filter(_.action.index == eppDep.dependent)
                  val obj: Object = objects.filter(_.action.index == eppDep.governor).head
                  PostCondition(
                    obj,
                    objs
                  )
                case eppDep if eppDep.dep.matches(Relations.AdvclAgent.value) || eppDep.dep == Relations.Ccomp.value =>
                  val objs: List[Object] = objects.filter(_.action.index == eppDep.dependent)
                  val obj: Object = objects.filter(_.action.index == eppDep.governor).head
                  PreCondition(
                    obj,
                    objs
                  )
              }
            } else {
              List.empty
            }

            println(s"conditions = $conditions ")
            UseCase(executors, objects, conditions)
          }
          val a = "(*) --> "
          diagramExporter.generateUseCaseDiagram(useCases)
      }
  }
}
