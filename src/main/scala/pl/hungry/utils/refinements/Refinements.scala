package pl.hungry.utils.refinements

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

object Refinements {
  type EmailRestrictions = MatchesRegex[W.`"""(^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$)"""`.T]
  type Email                     = String Refined EmailRestrictions
}
