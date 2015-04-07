package org.template.vanilla

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(intlPlan: Boolean,
  voiceMailPlan: Boolean,
  numVmailMsg: Long,
  totalDayMins: Double,
  totalDayCalls: Long,
  totalDayCharge: Double,
  totalEveMins: Double,
  totalEveCalls: Long,
  totalEveCharge: Double,
  totalNightMins: Double,
  totalNightCalls: Long,
  totalNightCharge: Double,
  totalIntlMins: Double,
  totalIntlCalls: Long,
  totalIntlCharge: Double,
  customerServiceCalls: Long) extends Serializable

case class PredictedResult(p: String) extends Serializable

object VanillaEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("algo" -> classOf[Algorithm]),
      classOf[Serving])
  }
}
