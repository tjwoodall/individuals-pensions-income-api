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

package v2.createAmendPensions.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec
import v2.models._

class Def1_CreateAmendPensionsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"

    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
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

    def mtdTaxYear: String

    private def uri: String = s"/$nino/$mtdTaxYear"

    def downstreamUri: String

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {
    def mtdTaxYear: String    = "2019-20"
    def downstreamUri: String = s"/income-tax/income/pensions/$nino/$mtdTaxYear"
  }

  private trait TysTest extends Test {
    def mtdTaxYear: String    = "2023-24"
    def downstreamUri: String = s"/income-tax/income/pensions/23-24/$nino"
  }

  "Calling the 'amend pensions' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
      }

      "any valid Tax-Year-Specific (TYS) request is made" in new TysTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body shouldBe ""
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new NonTysTest {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
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

        val allInvalidValueRequestError: List[MtdError] = List(
          CountryCodeFormatError.copy(
            paths = Some(
              List(
                "/foreignPensions/1/countryCode",
                "/overseasPensionContributions/1/dblTaxationCountryCode"
              ))
          ),
          CustomerRefFormatError.copy(
            paths = Some(
              List(
                "/overseasPensionContributions/0/customerReference",
                "/overseasPensionContributions/1/customerReference"
              ))
          ),
          DoubleTaxationArticleFormatError.copy(
            paths = Some(
              List(
                "/overseasPensionContributions/0/dblTaxationArticle",
                "/overseasPensionContributions/1/dblTaxationArticle"
              ))
          ),
          DoubleTaxationTreatyFormatError.copy(
            paths = Some(
              List(
                "/overseasPensionContributions/0/dblTaxationTreaty",
                "/overseasPensionContributions/1/dblTaxationTreaty"
              ))
          ),
          QOPSRefFormatError.copy(
            paths = Some(
              List(
                "/overseasPensionContributions/0/migrantMemReliefQopsRefNo",
                "/overseasPensionContributions/1/migrantMemReliefQopsRefNo"
              ))
          ),
          SF74RefFormatError.copy(
            paths = Some(
              List(
                "/overseasPensionContributions/0/sf74reference",
                "/overseasPensionContributions/1/sf74reference"
              ))
          ),
          ValueFormatError.copy(
            message = "The value must be between 0 and 99999999999.99",
            paths = Some(
              List(
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
              ))
          ),
          CountryCodeRuleError.copy(
            paths = Some(
              List(
                "/foreignPensions/0/countryCode",
                "/overseasPensionContributions/0/dblTaxationCountryCode"
              ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "complex error scenario" in new NonTysTest {

        val createAmendErrorsRequest: JsValue = Json.parse("""
            |{
            |   "foreignPensions":[
            |      {
            |         "countryCode":"ABCD",
            |         "amountBeforeTax":100.23,
            |         "taxTakenOff":1.23,
            |         "specialWithholdingTax":2.23,
            |         "foreignTaxCreditRelief":false,
            |         "taxableAmount":3.23
            |      },
            |      {
            |         "countryCode":"ABC",
            |         "amountBeforeTax":100.23,
            |         "taxTakenOff":1.23,
            |         "specialWithholdingTax":2.23,
            |         "foreignTaxCreditRelief":false,
            |         "taxableAmount":3.239
            |      }
            |   ],
            |   "overseasPensionContributions":[
            |      {
            |         "customerReference":"PENSIONINCOME245",
            |         "exemptEmployersPensionContribs":200.23,
            |         "migrantMemReliefQopsRefNo":"QOPS000000",
            |         "dblTaxationRelief":4.239,
            |         "dblTaxationCountryCode":"ABC",
            |         "dblTaxationArticle":"AB3211-1",
            |         "dblTaxationTreaty":"Treaty",
            |         "sf74reference":"SF74-123456"
            |      },
            |      {
            |         "customerReference":"PENSIONINCOME246#!",
            |         "exemptEmployersPensionContribs":200.23,
            |         "migrantMemReliefQopsRefNo":"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901",
            |         "dblTaxationRelief":4.23,
            |         "dblTaxationCountryCode":"ABCD",
            |         "dblTaxationArticle":"AB3211-1#!",
            |         "dblTaxationTreaty":"Treaty#!",
            |         "sf74reference":"SF74-123456#!"
            |      }
            |   ]
            |}
            |""".stripMargin)

        val createAmendErrorsResponse: JsValue = Json.parse("""
            |{
            |   "code":"INVALID_REQUEST",
            |   "message":"Invalid request",
            |   "errors": [
            |        {
            |            "code": "FORMAT_COUNTRY_CODE",
            |            "message": "The provided Country code is invalid",
            |            "paths": [
            |                "/foreignPensions/0/countryCode",
            |                "/overseasPensionContributions/1/dblTaxationCountryCode"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_CUSTOMER_REF",
            |            "message": "The provided customer reference is invalid",
            |            "paths": [
            |                "/overseasPensionContributions/1/customerReference"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_DOUBLE_TAXATION_ARTICLE",
            |            "message": "The provided double taxation article is invalid",
            |            "paths": [
            |                "/overseasPensionContributions/1/dblTaxationArticle"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_DOUBLE_TAXATION_TREATY",
            |            "message": "The provided double taxation treaty is invalid",
            |            "paths": [
            |                "/overseasPensionContributions/1/dblTaxationTreaty"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_QOPS_REF",
            |            "message": "The provided QOPS reference number is invalid",
            |            "paths": [
            |                "/overseasPensionContributions/1/migrantMemReliefQopsRefNo"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_SF74_REF",
            |            "message": "The provided SF74 reference is invalid",
            |            "paths": [
            |                "/overseasPensionContributions/1/sf74reference"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The value must be between 0 and 99999999999.99",
            |            "paths": [
            |                "/foreignPensions/1/taxableAmount",
            |                "/overseasPensionContributions/0/dblTaxationRelief"
            |            ]
            |        },
            |        {
            |            "code": "RULE_COUNTRY_CODE",
            |            "message": "The country code is not a valid ISO 3166-1 alpha-3 country code",
            |            "paths": [
            |                "/foreignPensions/1/countryCode",
            |                "/overseasPensionContributions/0/dblTaxationCountryCode"
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(createAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe createAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
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

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignPensions": [
          |      {
          |         "countryCode": "GERMANY",
          |         "amountBeforeTax": 100.23,
          |         "taxTakenOff": 1.23,
          |         "specialWithholdingTax": 2.23,
          |         "foreignTaxCreditRelief": false,
          |         "taxableAmount": 3.23
          |      },
          |      {
          |         "countryCode": "FRANCE",
          |         "amountBeforeTax": 200.25,
          |         "taxTakenOff": 1.27,
          |         "specialWithholdingTax": 2.50,
          |         "foreignTaxCreditRelief": true,
          |         "taxableAmount": 3.50
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val ruleCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "overseasPensionContributions": [
          |      {
          |         "customerReference": "PENSIONINCOME245",
          |         "exemptEmployersPensionContribs": 200.23,
          |         "migrantMemReliefQopsRefNo": "QOPS000000",
          |         "dblTaxationRelief": 4.23,
          |         "dblTaxationCountryCode": "PUR",
          |         "dblTaxationArticle": "AB3211-1",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "SF74-123456"
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.50,
          |         "migrantMemReliefQopsRefNo": "QOPS000245",
          |         "dblTaxationRelief": 5.50,
          |         "dblTaxationCountryCode": "SBT",
          |         "dblTaxationArticle": "AB3477-5",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "SF74-1235"
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val nonsenseRequestBody: JsValue = Json.parse(
        """
          |{
          |  "field": "value"
          |}
        """.stripMargin
      )

      val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
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
          |      },
          |      {
          |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
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

      val invalidQOPSRefRequestBodyJson: JsValue = Json.parse(
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
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.50,
          |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
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

      val invalidDoubleTaxationArticleRequestBodyJson: JsValue = Json.parse(
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
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.50,
          |         "migrantMemReliefQopsRefNo": "QOPS000245",
          |         "dblTaxationRelief": 5.50,
          |         "dblTaxationCountryCode": "NGA",
          |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "SF74-1235"
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val invalidDoubleTaxationTreatyRequestBodyJson: JsValue = Json.parse(
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
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.50,
          |         "migrantMemReliefQopsRefNo": "QOPS000245",
          |         "dblTaxationRelief": 5.50,
          |         "dblTaxationCountryCode": "NGA",
          |         "dblTaxationArticle": "AB3477-5",
          |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
          |         "sf74reference": "SF74-1235"
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val invalidSF74RefRequestBodyJson: JsValue = Json.parse(
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
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.50,
          |         "migrantMemReliefQopsRefNo": "QOPS000245",
          |         "dblTaxationRelief": 5.50,
          |         "dblTaxationCountryCode": "NGA",
          |         "dblTaxationArticle": "AB3477-5",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignPensions": [
          |      {
          |         "countryCode": "DEU",
          |         "amountBeforeTax": -100.23,
          |         "taxTakenOff": 1.237,
          |         "specialWithholdingTax": 2.234,
          |         "foreignTaxCreditRelief": false,
          |         "taxableAmount": -3.23
          |      },
          |      {
          |         "countryCode": "FRA",
          |         "amountBeforeTax": -200.25,
          |         "taxTakenOff": 1.279,
          |         "specialWithholdingTax": -2.50,
          |         "foreignTaxCreditRelief": true,
          |         "taxableAmount": 3.525
          |      }
          |   ],
          |   "overseasPensionContributions": [
          |      {
          |         "customerReference": "PENSIONINCOME245",
          |         "exemptEmployersPensionContribs": -200.23,
          |         "migrantMemReliefQopsRefNo": "QOPS000000",
          |         "dblTaxationRelief": 4.234,
          |         "dblTaxationCountryCode": "FRA",
          |         "dblTaxationArticle": "AB3211-1",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "SF74-123456"
          |      },
          |      {
          |         "customerReference": "PENSIONINCOME275",
          |         "exemptEmployersPensionContribs": 270.559,
          |         "migrantMemReliefQopsRefNo": "QOPS000245",
          |         "dblTaxationRelief": -5.50,
          |         "dblTaxationCountryCode": "NGA",
          |         "dblTaxationArticle": "AB3477-5",
          |         "dblTaxationTreaty": "Treaty",
          |         "sf74reference": "SF74-1235"
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
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

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignPensions": [
          |     {
          |       "countryCode": "DEU",
          |       "amountBeforeTax": 100.23,
          |       "taxTakenOff": 1.23,
          |       "specialWithholdingTax": 2.23,
          |       "foreignTaxCreditRelief": false
          |     }
          |   ]
          |}
        """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(
          Seq(
            "/foreignPensions/0/countryCode",
            "/foreignPensions/1/countryCode"
          ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/dblTaxationCountryCode",
            "/overseasPensionContributions/1/dblTaxationCountryCode"
          ))
      )

      val customerRefError: MtdError = CustomerRefFormatError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/customerReference",
            "/overseasPensionContributions/1/customerReference"
          ))
      )

      val qopsRefError: MtdError = QOPSRefFormatError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/migrantMemReliefQopsRefNo",
            "/overseasPensionContributions/1/migrantMemReliefQopsRefNo"
          ))
      )

      val dblTaxationArticleError: MtdError = DoubleTaxationArticleFormatError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/dblTaxationArticle",
            "/overseasPensionContributions/1/dblTaxationArticle"
          ))
      )

      val dblTaxationTreatyError: MtdError = DoubleTaxationTreatyFormatError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/dblTaxationTreaty",
            "/overseasPensionContributions/1/dblTaxationTreaty"
          ))
      )

      val sf74RefError: MtdError = SF74RefFormatError.copy(
        paths = Some(
          Seq(
            "/overseasPensionContributions/0/sf74reference",
            "/overseasPensionContributions/1/sf74reference"
          ))
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "The value must be between 0 and 99999999999.99",
        paths = Some(
          List(
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
          ))
      )

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/foreignPensions/0/taxTakenOff"))
      )

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/foreignPensions/0/taxableAmount"))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new NonTysTest {

            override val nino: String             = requestNino
            override val mtdTaxYear: String       = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "20177", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2019-21", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2019-20", invalidCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeError, None),
          ("AA123456A", "2019-20", ruleCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeRuleError, None),
          ("AA123456A", "2019-20", nonsenseRequestBody, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2019-20", invalidCustomerRefRequestBodyJson, BAD_REQUEST, customerRefError, None),
          ("AA123456A", "2019-20", invalidQOPSRefRequestBodyJson, BAD_REQUEST, qopsRefError, None),
          ("AA123456A", "2019-20", invalidDoubleTaxationArticleRequestBodyJson, BAD_REQUEST, dblTaxationArticleError, None),
          ("AA123456A", "2019-20", invalidDoubleTaxationTreatyRequestBodyJson, BAD_REQUEST, dblTaxationTreatyError, None),
          ("AA123456A", "2019-20", invalidSF74RefRequestBodyJson, BAD_REQUEST, sf74RefError, None),
          ("AA123456A", "2019-20", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError, None),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, nonValidRequestBodyErrors, Some("(invalid request body format)")),
          ("AA123456A", "2019-20", missingFieldRequestBodyJson, BAD_REQUEST, missingFieldRequestBodyErrors, Some("(missing mandatory fields)"))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "downstream message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
