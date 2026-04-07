/*
 * Copyright 2026 HM Revenue & Customs
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

package v2.deletePensions.def1

import config.{MockPensionsIncomeConfig, PensionsIncomeConfig}
import shared.config.MockAppConfig
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v2.deletePensions.DeletePensionsValidatorFactory
import v2.deletePensions.model.request.{Def1_DeletePensionsRequestData, DeletePensionsRequestData}

class Def1_DeletePensionsValidatorSpec extends UnitSpec with MockAppConfig with MockPensionsIncomeConfig {

  class Test extends MockPensionsIncomeConfig {
    implicit val correlationId: String = "1234"
    val validNino: String              = "AA123456A"
    val validTaxYear: String           = "2021-22"

    val parsedNino: Nino       = Nino(validNino)
    val parsedTaxYear: TaxYear = TaxYear.fromMtd(validTaxYear)

    implicit val appConfig: PensionsIncomeConfig = mockPensionsIncomeConfig

    MockedPensionsIncomeConfig
      .minimumPermittedTaxYear()
      .returns(2020)
      .anyNumberOfTimes()

    val validatorFactory: DeletePensionsValidatorFactory = new DeletePensionsValidatorFactory()

    def validator(nino: String, taxYear: String): Validator[DeletePensionsRequestData] =
      validatorFactory.validator(nino, taxYear)

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_DeletePensionsRequestData(parsedNino, parsedTaxYear))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator("A12344A", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator(validNino, "201718").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator(validNino, "2016-17").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator(validNino, "2017-19").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {

        val result: Either[ErrorWrapper, DeletePensionsRequestData] =
          validator("not-a-nino", "2017-19").validateAndWrapResult()

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
