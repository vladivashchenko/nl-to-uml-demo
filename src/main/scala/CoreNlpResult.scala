case class OpenIE(
  subject: String,
  relation: String,
  `object`: String,
)

case class ConditionDependency(
  governor: Int,
  dependent: Int,
  dep: String,
)

case class Token(
  index: Int,
  word: String,
  originalText: String,
  lemma: String,
  pos: String,
  before: String,
  after: String
)

case class EnhancedPlusPlusDependency(
  dep: String,
  governor: Int,
  governorGloss: String,
  dependent: Int,
  dependentGloss: String
)

case class Coreference(
  id: Int,
  text: String,
  `type`: String,
)

case class Sentence(
  index: Int,
  parse: String,
  tokens: List[Token],
  enhancedPlusPlusDependencies: List[EnhancedPlusPlusDependency],
)

case class CoreNlpResult(
  sentences: List[Sentence],
  corefs: Map[String, List[Coreference]] = Map(),
)

case class CoreferenceResult(
  corefs: Map[String, List[Coreference]],
  tokens: List[Token],
)
