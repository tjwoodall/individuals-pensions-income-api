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

import cats.data.Validated
import cats.implicits._
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v1.deletePensions.def1.Def1_DeletePensionsValidator.resolveTaxYear
import v1.deletePensions.model.request.{Def1_DeletePensionsRequestData, DeletePensionsRequestData}

class Def1_DeletePensionsValidator(nino: String, taxYear: String) extends Validator[DeletePensionsRequestData] {

  def validate: Validated[Seq[MtdError], DeletePensionsRequestData] = (
    ResolveNino(nino),
    resolveTaxYear(taxYear)
  ).mapN(Def1_DeletePensionsRequestData)

}

object Def1_DeletePensionsValidator {
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(2020))

}
