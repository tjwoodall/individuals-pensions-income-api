/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.deletePensions

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{InternalError, MtdError, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.deletePensions.model.request.{Def1_DeletePensionsRequestData, DeletePensionsRequestData}

import scala.concurrent.Future

class DeletePensionsConnectorSpec extends ConnectorSpec {

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    val nino: String = "AA111111A"

    protected val request: DeletePensionsRequestData =
      Def1_DeletePensionsRequestData(
        nino = Nino(nino),
        taxYear = taxYear
      )

    protected val connector: DeletePensionsConnector = new DeletePensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "DeletePensionsIncomeConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear                               = TaxYear.fromMtd("2021-22")
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        await(connector.deletePensionsIncome(request)) shouldBe outcome
      }
    }
    "downstream returns a single error" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

      val outcome: Left[ResponseWrapper[NinoFormatError.type], Nothing] = Left(ResponseWrapper(correlationId, NinoFormatError))

      willDelete(
        url"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }

    "downstream returns multiple errors" in new IfsTest with Test {

      def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

      val outcome: Left[ResponseWrapper[Seq[MtdError]], Nothing] = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError)))

      willDelete(
        url"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }

  }

  "return the expected response for a TYS request" when {
    "a valid request is made" in new IfsTest with Test {
      def taxYear: TaxYear                               = TaxYear.fromMtd("2023-24")
      val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

      willDelete(
        url = url"$baseUrl/income-tax/income/pensions/${taxYear.asTysDownstream}/$nino"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }
  }

}
