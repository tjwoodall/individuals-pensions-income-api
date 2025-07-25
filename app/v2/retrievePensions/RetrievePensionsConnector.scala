/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.retrievePensions

import shared.config.AppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.retrievePensions.model.request.RetrievePensionsRequestData
import v2.retrievePensions.model.response.RetrievePensionsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionsConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrievePensionsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrievePensionsResponse]] = {

    import request._
    import schema._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      IfsUri[DownstreamResp](s"income-tax/income/pensions/${taxYear.asTysDownstream}/${nino.value}")
    } else {
      IfsUri[DownstreamResp](s"income-tax/income/pensions/${nino.value}/${taxYear.asMtd}")
    }

    get(downstreamUri)
  }

}
