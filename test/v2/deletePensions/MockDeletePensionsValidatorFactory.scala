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

package v2.deletePensions

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.deletePensions.model.request.DeletePensionsRequestData

trait MockDeletePensionsValidatorFactory extends MockFactory {

  val mockDeletePensionsValidatorFactory: DeletePensionsValidatorFactory =
    mock[DeletePensionsValidatorFactory]

  object MockedDeletePensionsValidatorFactory {

    def validator(): CallHandler[Validator[DeletePensionsRequestData]] =
      (mockDeletePensionsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[DeletePensionsRequestData]): CallHandler[Validator[DeletePensionsRequestData]] = {
    MockedDeletePensionsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeletePensionsRequestData): Validator[DeletePensionsRequestData] =
    new Validator[DeletePensionsRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeletePensionsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeletePensionsRequestData] =
    new Validator[DeletePensionsRequestData] {
      def validate: Validated[Seq[MtdError], DeletePensionsRequestData] = Invalid(result)
    }

}
