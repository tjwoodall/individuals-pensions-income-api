/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import shared.models.errors.MtdError
import v1.models.SF74RefFormatError

object SF74RefValidation {

  def validateOptional(sf74Ref: Option[String], path: String): List[MtdError] = sf74Ref.fold(NoValidationErrors: List[MtdError]) { ref =>
    if (ref.matches("^[0-9a-zA-Z{À-˿'}\\- _&`():.'^]{1,90}$")) NoValidationErrors else List(SF74RefFormatError.copy(paths = Some(Seq(path))))
  }

}
