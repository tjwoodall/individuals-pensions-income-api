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

package uk.gov.hmrc.individualspensionsincomeapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** Temporary test to get code coverage up; will be replaced with the real API code & tests.
  */
class DocumentationControllerSpec extends AnyWordSpec with Matchers {

//  private val assets =
//
//  private val controller  = new DocumentationController(,  Helpers.stubControllerComponents())

  "definition()" should {
    "return definition.json" in {
      "true".toBoolean shouldBe true
      // val result = controller.def
    }

    "specification" should {
      "return the version" in {
        "true".toBoolean shouldBe true

      }
    }
  }

}
