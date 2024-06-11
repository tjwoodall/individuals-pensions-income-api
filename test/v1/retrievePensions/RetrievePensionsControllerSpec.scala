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

package v1.retrievePensions

import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.retrievePensions.def1.model.RetrievePensionsControllerFixture
import v1.retrievePensions.def1.model.response.{ForeignPensionsItem, OverseasPensionContributions}
import v1.retrievePensions.model.request.Def1_RetrievePensionsRequestData
import v1.retrievePensions.model.response.Def1_RetrievePensionsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrievePensionsService
    with MockRetrievePensionsValidatorFactory
    with MockAppConfig {

  private val taxYear = "2019-20"

  private val requestData: Def1_RetrievePensionsRequestData = Def1_RetrievePensionsRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val foreignPensionsItem = List(
    ForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 3.23
    ),
    ForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 3.50
    )
  )

  private val overseasPensionContributionsItem = List(
    OverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    OverseasPensionContributions(
      customerReference = Some("PENSIONINCOME275"),
      exemptEmployersPensionContribs = 270.50,
      migrantMemReliefQopsRefNo = Some("QOPS000245"),
      dblTaxationRelief = Some(5.50),
      dblTaxationCountryCode = Some("NGA"),
      dblTaxationArticle = Some("AB3477-5"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-1235")
    )
  )

  private val retrievePensionsResponse = Def1_RetrievePensionsResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    foreignPensions = Some(foreignPensionsItem),
    overseasPensionContributions = Some(overseasPensionContributionsItem)
  )

  private val mtdResponse = RetrievePensionsControllerFixture.fullRetrievePensionsResponse

  "RetrievePensionsController" should {
    "return a successful response with status 200 (OK)" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePensionsService
          .retrievePensions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrievePensionsResponse))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "service errors occur" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePensionsService
          .retrievePensions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrievePensionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrievePensionsValidatorFactory,
      service = mockRetrievePensionsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakeGetRequest)
  }

}
