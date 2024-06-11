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

package v1.deletePensions.def1

import shared.UnitSpec
import shared.config.{AppConfig, MockAppConfig}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.deletePensions.model.request.Def1_DeletePensionsRequestData

class Def1_DeletePensionsValidatorSpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "1234"
  private val validNino                      = "AA123456A"
  private val validTaxYear                   = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  implicit val appConfig: AppConfig = mockAppConfig

  private def validator(nino: String, taxYear: String) = new Def1_DeletePensionsValidator(nino, taxYear)(mockAppConfig)

  private def setupMocks() = {
    MockAppConfig.minimumPermittedTaxYear
      .returns(2020)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        setupMocks()
        val result = validator(validNino, validTaxYear).validateAndWrapResult()
        result shouldBe Right(Def1_DeletePensionsRequestData(parsedNino, parsedTaxYear))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        setupMocks()
        val result = validator("A12344A", validTaxYear).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        setupMocks()
        val result = validator(validNino, "201718").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        setupMocks()
        val result = validator(validNino, "2016-17").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in {
        setupMocks()
        val result = validator(validNino, "2017-19").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        setupMocks()
        val result = validator("not-a-nino", "2017-19").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, RuleTaxYearRangeInvalidError))
          )
        )
      }
    }
  }

}
