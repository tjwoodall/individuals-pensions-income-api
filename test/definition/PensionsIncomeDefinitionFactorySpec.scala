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

package definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.{APIAccessType, Definition}
import api.routing.Version2
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

class PensionsIncomeDefinitionFactorySpec extends UnitSpec with MockAppConfig {

  "PensionsIncomeDefinitionFactory" when {

    "the access level is set" when {
      "the controlled access flag is enabled" should {
        "return CONTROLLED" in {

          MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
          MockedAppConfig.endpointsEnabled(Version2)
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()

          MockedAppConfig.controlledAccessEnabled returns true

          val definition: Definition = new PensionsIncomeDefinitionFactory(mockAppConfig).definition

          definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
        }
      }

      "the controlled access flag is disabled" should {
        "return PUBLIC" in {

          MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
          MockedAppConfig.endpointsEnabled(Version2)
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()

          MockedAppConfig.controlledAccessEnabled returns false

          val definition: Definition = new PensionsIncomeDefinitionFactory(mockAppConfig).definition

          definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
        }
      }
    }
  }

}
