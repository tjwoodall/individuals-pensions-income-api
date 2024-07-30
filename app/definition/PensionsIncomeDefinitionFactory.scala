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

package definition

import shared.config.AppConfig
import shared.definition._
import shared.routing.Version1

import javax.inject.{Inject, Singleton}

@Singleton
class PensionsIncomeDefinitionFactory @Inject()(protected val appConfig: AppConfig) extends ApiDefinitionFactory {

  lazy val definition: Definition =
    Definition(
      scopes = scopes,
      api = APIDefinition(
        name = "Individuals Pensions Income (MTD)",
        description = "An API for providing individual pensions income data",
        context = appConfig.apiGatewayContext,
        categories = List(mtdCategory),
        versions = List(
          APIVersion(
            version = Version1,
            status = buildAPIStatus(Version1),
            endpointsEnabled = appConfig.endpointsEnabled(Version1)
          )
        ),
        requiresTrust = None
      )
    )

}
