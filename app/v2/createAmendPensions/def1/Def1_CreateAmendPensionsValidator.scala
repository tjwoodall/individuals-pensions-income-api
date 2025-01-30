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

import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.createAmendPensions.def1.Def1_CreateAmendPensionsRulesValidator.validateBusinessRules
import v2.createAmendPensions.def1.Def1_CreateAmendPensionsValidator.{resolveJson, resolveTaxYear}
import v2.createAmendPensions.model.request.{CreateAmendPensionsRequestData, Def1_CreateAmendPensionsRequestBody, Def1_CreateAmendPensionsRequestData}
class Def1_CreateAmendPensionsValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendPensionsRequestData] {

  def validate: Validated[Seq[MtdError], CreateAmendPensionsRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendPensionsRequestData) andThen validateBusinessRules

}

object Def1_CreateAmendPensionsValidator{
  private val resolveJson         = new ResolveNonEmptyJsonObject[Def1_CreateAmendPensionsRequestBody]()
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(2020))
}
