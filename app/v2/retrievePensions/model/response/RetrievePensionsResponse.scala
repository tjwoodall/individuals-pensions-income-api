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

package v2.retrievePensions.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.models.domain.Timestamp
import shared.utils.JsonWritesUtil
import utils.JsonUtils
import v2.retrievePensions.def1.model.response.{ForeignPensionsItem, OverseasPensionContributions}

sealed trait RetrievePensionsResponse

object RetrievePensionsResponse extends JsonWritesUtil {

  implicit val writes: OWrites[RetrievePensionsResponse] = writesFrom { case a: Def1_RetrievePensionsResponse =>
    implicitly[OWrites[Def1_RetrievePensionsResponse]].writes(a)
  }

}

case class Def1_RetrievePensionsResponse(submittedOn: Timestamp,
                                         foreignPensions: Option[Seq[ForeignPensionsItem]],
                                         overseasPensionContributions: Option[Seq[OverseasPensionContributions]])
    extends RetrievePensionsResponse

object Def1_RetrievePensionsResponse extends JsonUtils {

  implicit val reads: Reads[Def1_RetrievePensionsResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignPension").readNullable[Seq[ForeignPensionsItem]].mapEmptySeqToNone and
      (JsPath \ "overseasPensionContribution").readNullable[Seq[OverseasPensionContributions]].mapEmptySeqToNone
  )(Def1_RetrievePensionsResponse.apply)

  implicit val writes: OWrites[Def1_RetrievePensionsResponse] = Json.writes[Def1_RetrievePensionsResponse]

}
