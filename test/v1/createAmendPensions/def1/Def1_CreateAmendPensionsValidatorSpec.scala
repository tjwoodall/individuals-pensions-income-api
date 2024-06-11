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

package v1.createAmendPensions.def1

import play.api.libs.json.{JsObject, JsValue, Json}
import shared.UnitSpec
import shared.config.MockAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import v1.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}
import v1.createAmendPensions.model.request.{CreateAmendPensionsRequestData, Def1_CreateAmendPensionsRequestBody, Def1_CreateAmendPensionsRequestData}
import v1.models._

class Def1_CreateAmendPensionsValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val validNino                      = "AA123456A"
  private val validTaxYear                   = "2020-21"

  private val validRequestBodyJson: JsValue = Json.parse(
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

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": "no",
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [{"amountBeforeTax": 100.23}]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidQOPSRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidSF74RefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDoubleTaxationArticleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDoubleTaxationTreatyRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "PUR",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDoubleTaxationCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRANCE",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidForeignPensionsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "NGA",
      |         "amountBeforeTax": 100.239,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidOverseasPensionContributionsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.234,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "SBT",
      |         "amountBeforeTax": 100.234,
      |         "taxTakenOff": 1.235,
      |         "specialWithholdingTax": -2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": -3.23
      |      },
      |      {
      |         "countryCode": "FRANCE",
      |         "amountBeforeTax": -200.25,
      |         "taxTakenOff": 1.273,
      |         "specialWithholdingTax": -2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.508
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": 200.237,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": -4.238,
      |         "dblTaxationCountryCode": "PUR",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      },
      |      {
      |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |         "exemptEmployersPensionContribs": -270.509,
      |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
      |         "dblTaxationRelief": 5.501,
      |         "dblTaxationCountryCode": "GERMANY",
      |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
      |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
      |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedCreateAmendForeignPensionsItem = Seq(
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
      foreignTaxCreditRelief = None,
      taxableAmount = 3.50
    )
  )

  private val parsedCreateAmendOverseasPensionContributions = Seq(
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

  private val parsedBody = Def1_CreateAmendPensionsRequestBody(
    Some(parsedCreateAmendForeignPensionsItem),
    Some(parsedCreateAmendOverseasPensionContributions)
  )

  private def validator(nino: String, taxYear: String, body: JsValue) = new Def1_CreateAmendPensionsValidator(nino, taxYear, body)(mockAppConfig)

  private def setupMocks(): Unit = {
    MockAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendPensionsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator("invalid", validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, "201831", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in {
      setupMocks()
      val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
        validator(validNino, "2021-24", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
    }

    "return RuleTaxYearNotSupportedError error for an unsupported tax year" in {
      setupMocks()
      val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
        validator(validNino, "2019-20", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
    }

    "return multiple errors for multiple invalid request parameters" in {
      setupMocks()
      val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
        validator("invalid", "invalid", validRequestBodyJson).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          BadRequestError,
          Some(List(NinoFormatError, TaxYearFormatError))
        )
      )
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "a non-empty JSON body is submitted without any expected fields" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, nonsenseRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))

      }

      "the submitted request body is not in the correct format" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, nonValidRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignPensions/0/taxTakenOff")))
      }

      "the submitted request body has missing mandatory fields" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, missingMandatoryFieldJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(List("/foreignPensions/0/countryCode", "/foreignPensions/0/taxableAmount"))))
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidCustomerRefRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, CustomerRefFormatError.withPath("/overseasPensionContributions/0/customerReference")))

      }
    }

    "return QOPSRefFormatError error" when {
      "an incorrectly formatted migrantMemReliefQopsRefNo is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidQOPSRefRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, QOPSRefFormatError.withPath("/overseasPensionContributions/0/migrantMemReliefQopsRefNo")))
      }
    }

    "return SF74RefFormatError error" when {
      "an incorrectly formatted sf74reference is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidSF74RefRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, SF74RefFormatError.withPath("/overseasPensionContributions/0/sf74reference")))
      }
    }

    "return DoubleTaxationArticleFormatError error" when {
      "an incorrectly formatted dblTaxationArticle is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidDoubleTaxationArticleRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, DoubleTaxationArticleFormatError.withPath("/overseasPensionContributions/0/dblTaxationArticle")))
      }
    }

    "return DoubleTaxationTreatyFormatError error" when {
      "an incorrectly formatted dblTaxationTreaty is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidDoubleTaxationTreatyRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, DoubleTaxationTreatyFormatError.withPath("/overseasPensionContributions/0/dblTaxationTreaty")))
      }
    }

    "return CountryCodeFormatError error" when {
      "an incorrectly formatted dblTaxationCountryCode is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidDoubleTaxationCountryCodeRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/overseasPensionContributions/0/dblTaxationCountryCode")))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidCountryCodeRuleRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeRuleError.withPath("/foreignPensions/0/countryCode")))

      }
    }

    "return FORMAT_VALUE error (single failure)" when {
      "one field fails value validation (foreign pensions)" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidForeignPensionsRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath("/foreignPensions/0/amountBeforeTax")))

      }

      "one field fails value validation (Overseas Pension Contributions)" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, invalidOverseasPensionContributionsRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath("/overseasPensionContributions/0/exemptEmployersPensionContribs")))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator(validNino, validTaxYear, allInvalidValueRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                CountryCodeFormatError.withPaths(List("/foreignPensions/1/countryCode", "/overseasPensionContributions/1/dblTaxationCountryCode")),
                CustomerRefFormatError.withPaths(
                  List("/overseasPensionContributions/0/customerReference", "/overseasPensionContributions/1/customerReference")),
                DoubleTaxationArticleFormatError.withPaths(
                  List("/overseasPensionContributions/0/dblTaxationArticle", "/overseasPensionContributions/1/dblTaxationArticle")),
                DoubleTaxationTreatyFormatError.withPaths(
                  List("/overseasPensionContributions/0/dblTaxationTreaty", "/overseasPensionContributions/1/dblTaxationTreaty")),
                QOPSRefFormatError.withPaths(
                  List("/overseasPensionContributions/0/migrantMemReliefQopsRefNo", "/overseasPensionContributions/1/migrantMemReliefQopsRefNo")),
                SF74RefFormatError.withPaths(List("/overseasPensionContributions/0/sf74reference", "/overseasPensionContributions/1/sf74reference")),
                ValueFormatError.withPaths(List(
                  "/foreignPensions/0/amountBeforeTax",
                  "/foreignPensions/0/taxTakenOff",
                  "/foreignPensions/0/specialWithholdingTax",
                  "/foreignPensions/0/taxableAmount",
                  "/foreignPensions/1/amountBeforeTax",
                  "/foreignPensions/1/taxTakenOff",
                  "/foreignPensions/1/specialWithholdingTax",
                  "/foreignPensions/1/taxableAmount",
                  "/overseasPensionContributions/0/exemptEmployersPensionContribs",
                  "/overseasPensionContributions/0/dblTaxationRelief",
                  "/overseasPensionContributions/1/exemptEmployersPensionContribs",
                  "/overseasPensionContributions/1/dblTaxationRelief"
                )),
                CountryCodeRuleError.withPaths(List("/foreignPensions/0/countryCode", "/overseasPensionContributions/0/dblTaxationCountryCode"))
              )
            )
          ))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in {
        setupMocks()
        val result: Either[ErrorWrapper, CreateAmendPensionsRequestData] =
          validator("invalid", "20178", JsObject.empty).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }

}
