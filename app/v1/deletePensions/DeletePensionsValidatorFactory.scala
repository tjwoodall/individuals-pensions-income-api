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

package v1.deletePensions

import shared.config.AppConfig
import shared.controllers.validators.Validator
import v1.deletePensions.def1.Def1_DeletePensionsValidator
import v1.deletePensions.model.request.DeletePensionsRequestData

import javax.inject.Inject

class DeletePensionsValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, taxYear: String): Validator[DeletePensionsRequestData] =
    new Def1_DeletePensionsValidator(nino, taxYear)(appConfig)

}
