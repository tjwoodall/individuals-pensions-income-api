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

package v2.createAmendPensions.model.request

import api.utils.JsonWritesUtil
import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, OWrites, Reads}
import v2.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}

sealed trait CreateAmendPensionsRequestBody

object CreateAmendPensionsRequestBody extends JsonWritesUtil {

  implicit val writes: OWrites[CreateAmendPensionsRequestBody] = writesFrom { case a: Def1_CreateAmendPensionsRequestBody =>
    implicitly[OWrites[Def1_CreateAmendPensionsRequestBody]].writes(a)
  }

}

case class Def1_CreateAmendPensionsRequestBody(foreignPensions: Option[Seq[CreateAmendForeignPensionsItem]],
                                               overseasPensionContributions: Option[Seq[CreateAmendOverseasPensionContributions]])
    extends CreateAmendPensionsRequestBody

object Def1_CreateAmendPensionsRequestBody {

  implicit val reads: Reads[Def1_CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPensions").readNullable[Seq[CreateAmendForeignPensionsItem]] and
      (JsPath \ "overseasPensionContributions").readNullable[Seq[CreateAmendOverseasPensionContributions]]
  )(Def1_CreateAmendPensionsRequestBody.apply)

  implicit val writes: OWrites[Def1_CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPension").writeNullable[Seq[CreateAmendForeignPensionsItem]] and
      (JsPath \ "overseasPensionContribution").writeNullable[Seq[CreateAmendOverseasPensionContributions]]
  )(w => Tuple.fromProductTyped(w))

}
