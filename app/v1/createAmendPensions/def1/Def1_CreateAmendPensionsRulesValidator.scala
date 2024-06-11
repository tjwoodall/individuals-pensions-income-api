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

package v1.createAmendPensions.def1

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toFoldableOps
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.MtdError
import v1.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}
import v1.createAmendPensions.model.request.Def1_CreateAmendPensionsRequestData
import v1.models._

object Def1_CreateAmendPensionsRulesValidator extends RulesValidator[Def1_CreateAmendPensionsRequestData] {

  private val resolveParsedNumber = ResolveParsedNumber()
  private val stringRegex         = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def validateBusinessRules(parsed: Def1_CreateAmendPensionsRequestData): Validated[Seq[MtdError], Def1_CreateAmendPensionsRequestData] = {
    import parsed.body._
    val validatedForeignPensions = foreignPensions match {
      case Some(foreignPensionsItems) =>
        val foreignPensionsWithIndex = foreignPensionsItems.zipWithIndex.toList
        validateForeignPensions(foreignPensionsWithIndex)
      case None => valid

    }

    val validatedPensionContributions = overseasPensionContributions match {
      case Some(pensionContributions) =>
        val overseasPensionContributionsWithIndex = pensionContributions.zipWithIndex.toList
        validatePensionContributions(overseasPensionContributionsWithIndex)
      case None => valid
    }

    combine(validatedForeignPensions, validatedPensionContributions).onSuccess(parsed)

  }

  private def validateForeignPensions(foreignPensions: Seq[(CreateAmendForeignPensionsItem, Int)]): Validated[Seq[MtdError], Unit] = {
    foreignPensions.traverse_ { case (foreignPensions, i) =>
      validatePensionsItem(foreignPensions, i)
    }
  }

  private def validatePensionContributions(
      pensionContributions: Seq[(CreateAmendOverseasPensionContributions, Int)]): Validated[Seq[MtdError], Unit] = {
    pensionContributions.traverse_ { case (pensionContributions, i) =>
      validatePensionContributions(pensionContributions, i)
    }
  }

  private def resolveNonNegativeNumber(value: Option[BigDecimal], path: String): Validated[Seq[MtdError], Option[BigDecimal]] =
    ResolveParsedNumber(disallowZero = true)(value, path)

  private def validateCustomerRef(ref: String, path: String, error: MtdError = CustomerRefFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validateQopsRef(ref: String, path: String, error: MtdError = QOPSRefFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validatesf74Ref(ref: String, path: String, error: MtdError = SF74RefFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validateDoubleTaxationArticle(ref: String,
                                            path: String,
                                            error: MtdError = DoubleTaxationArticleFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validateDoubleTaxationTreaty(ref: String,
                                           path: String,
                                           error: MtdError = DoubleTaxationTreatyFormatError): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(ref)) valid
    else Invalid(List(error.withPath(path)))

  private def validatePensionsItem(foreignPensionsItem: CreateAmendForeignPensionsItem, index: Int): Validated[Seq[MtdError], Unit] = {

    def path(suffix: String) = s"/foreignPensions/$index/$suffix"

    combine(
      ResolveParsedCountryCode(foreignPensionsItem.countryCode, path("countryCode")),
      resolveNonNegativeNumber(foreignPensionsItem.amountBeforeTax, path("amountBeforeTax")),
      resolveNonNegativeNumber(foreignPensionsItem.taxTakenOff, path("taxTakenOff")),
      resolveNonNegativeNumber(foreignPensionsItem.specialWithholdingTax, path("specialWithholdingTax")),
      resolveParsedNumber(foreignPensionsItem.taxableAmount, path("taxableAmount"))
    )

  }

  private def validatePensionContributions(pensionContributions: CreateAmendOverseasPensionContributions,
                                           index: Int): Validated[Seq[MtdError], Unit] = {

    def path(suffix: String) = s"/overseasPensionContributions/$index/$suffix"

    combine(
      pensionContributions.customerReference.traverse_(validateCustomerRef(_, path("customerReference"))),
      resolveParsedNumber(pensionContributions.exemptEmployersPensionContribs, path("exemptEmployersPensionContribs")),
      pensionContributions.migrantMemReliefQopsRefNo.traverse_(validateQopsRef(_, path("migrantMemReliefQopsRefNo"))),
      resolveNonNegativeNumber(pensionContributions.dblTaxationRelief, path("dblTaxationRelief")),
      ResolveParsedCountryCode(pensionContributions.dblTaxationCountryCode, path("dblTaxationCountryCode")),
      pensionContributions.dblTaxationArticle.traverse_(validateDoubleTaxationArticle(_, path("dblTaxationArticle"))),
      pensionContributions.dblTaxationTreaty.traverse_(validateDoubleTaxationTreaty(_, path("dblTaxationTreaty"))),
      pensionContributions.sf74reference.traverse_(validatesf74Ref(_, path("sf74reference")))
    )

  }

}
