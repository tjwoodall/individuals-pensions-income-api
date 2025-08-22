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

package v1.createAmendPensions

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import v1.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}
import v1.createAmendPensions.model.request.{Def1_CreateAmendPensionsRequestBody, Def1_CreateAmendPensionsRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendPensionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendPensionsService
    with MockCreateAmendPensionsValidatorFactory
    with MockAppConfig {

  private val taxYear = "2019-20"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      },
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 200.25,
      |         "taxTakenOff": 1.27,
      |         "specialWithholdingTax": 2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.50
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      },
      |      {
      |         "customerReference": "PENSIONINCOME275",
      |         "exemptEmployersPensionContribs": 270.50,
      |         "migrantMemReliefQopsRefNo": "QOPS000245",
      |         "dblTaxationRelief": 5.50,
      |         "dblTaxationCountryCode": "NGA",
      |         "dblTaxationArticle": "AB3477-5",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-1235"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val foreignPensionsItem: List[CreateAmendForeignPensionsItem] = List(
    CreateAmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 3.23
    ),
    CreateAmendForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 3.50
    )
  )

  private val overseasPensionContributionsItem: List[CreateAmendOverseasPensionContributions] = List(
    CreateAmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    CreateAmendOverseasPensionContributions(
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

  private val createAmendPensionsRequestBody: Def1_CreateAmendPensionsRequestBody = Def1_CreateAmendPensionsRequestBody(
    foreignPensions = Some(foreignPensionsItem),
    overseasPensionContributions = Some(overseasPensionContributionsItem)
  )

  private val requestData: Def1_CreateAmendPensionsRequestData = Def1_CreateAmendPensionsRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = createAmendPensionsRequestBody
  )

  "CreateAmendPensionsController" should {
    "return OK with no response body" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendPensionsService
          .createAmendPensions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = None,
          maybeAuditResponseBody = None
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendPensionsService
          .createAmendPensions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateAmendPensionsController = new CreateAmendPensionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendPensionsValidatorFactory,
      service = mockCreateAmendPensionsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> false
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    protected def callController(): Future[Result] = controller.createAmendPensions(validNino, taxYear)(fakePostRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendPensionsIncome",
        transactionName = "create-amend-pensions-income",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "1.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
