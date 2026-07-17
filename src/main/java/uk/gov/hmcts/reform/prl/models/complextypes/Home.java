package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class Home {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = "*Who currently lives at the above address (please select all that apply)?", searchable = false)
    private final List<PeopleLivingAtThisAddressEnum> peopleLivingAtThisAddress;
    @CCD(
            label = " ",
            hint = "Provide the details in the box below",
            showCondition = "peopleLivingAtThisAddress CONTAINS \"someoneElse\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String textAreaSomethingElse;
    @CCD(
            label = "*Has the applicant or the respondent ever lived at the above address but don’t live there currently?",
            searchable = false
    )
    private final YesNoBothEnum everLivedAtTheAddress;
    @CCD(
            label = "*Did the applicant or the respondent ever intend to live in the address?",
            showCondition = "everLivedAtTheAddress=\"No\"",
            searchable = false
    )
    private final YesNoBothEnum intendToLiveAtTheAddress;
    @CCD(
            label = "*Do any children live at the above address that the applicant is responsible for?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo doAnyChildrenLiveAtAddress;
    @CCD(label = "Child", showCondition = "doAnyChildrenLiveAtAddress=\"Yes\"", searchable = false)
    private final List<Element<ChildrenLiveAtAddress>> children;
    @CCD(
            label = "*Does the property have any features or adaptations that make it more accessible for the applicant, their children or anyone else living there?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPropertyAdapted;
    @CCD(
            label = " ",
            hint = "*Please provide details of how the property is specially adapted",
            showCondition = "isPropertyAdapted = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String howIsThePropertyAdapted;
    @CCD(label = "*Is there a mortgage on the property?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isThereMortgageOnProperty;
    @CCD(label = " ", showCondition = "isThereMortgageOnProperty=\"Yes\"", searchable = false)
    private final Mortgage mortgages;
    @CCD(label = "*Is the property rented?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPropertyRented;
    @CCD(label = " ", showCondition = "isPropertyRented=\"Yes\"", searchable = false)
    private final Landlord landlords;
    @CCD(label = "*Does the applicant have any home rights?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo doesApplicantHaveHomeRights;
    @CCD(label = "*What exactly does the applicant want to happen with their living situation?", searchable = false)
    private final List<LivingSituationEnum> livingSituation;
    @CCD(label = "*Is there anything else the applicant wants to happen with their family home?", searchable = false)
    private final List<FamilyHomeEnum> familyHome;
    @CCD(
            label = "Is there anything else the applicant would want to be considered by the court?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String furtherInformation;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "*To what address do you want the occupation order to apply?",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String addressLabel;
  // ==== end synthesised definition-only fields ====
}
