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

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v2.retrievePensions.model.request.RetrievePensionsRequestData

trait MockRetrievePensionsValidatorFactory extends MockFactory {

  val mockRetrievePensionsValidatorFactory: RetrievePensionsValidatorFactory =
    mock[RetrievePensionsValidatorFactory]

  object MockedRetrievePensionsValidatorFactory {

    def validator(): CallHandler[Validator[RetrievePensionsRequestData]] =
      (mockRetrievePensionsValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrievePensionsRequestData]): CallHandler[Validator[RetrievePensionsRequestData]] = {
    MockedRetrievePensionsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrievePensionsRequestData): Validator[RetrievePensionsRequestData] =
    new Validator[RetrievePensionsRequestData] {
      def validate: Validated[Seq[MtdError], RetrievePensionsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrievePensionsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrievePensionsRequestData] =
    new Validator[RetrievePensionsRequestData] {
      def validate: Validated[Seq[MtdError], RetrievePensionsRequestData] = Invalid(result)
    }

}
