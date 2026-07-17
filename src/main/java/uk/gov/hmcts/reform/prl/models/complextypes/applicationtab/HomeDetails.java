package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class HomeDetails {
    @CCD(
            label = "Address that the occupation order should apply to",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address address;
    @CCD(label = "Who currently lives at the above address (please select all that apply)?", searchable = false)
    private final String peopleLivingAtThisAddress;
    @CCD(
            label = "Has the applicant or respondent ever lived at the above address, but don’t live there currently?",
            searchable = false
    )
    private final String everLivedAtTheAddress;
    @CCD(label = "Did the applicant or respondent ever intend to live in the address?", searchable = false)
    private final String intendToLiveAtTheAddress;
    @CCD(
            label = "Do any children live at the address that the applicant is responsible for?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo doAnyChildrenLiveAtAddress;
    @CCD(label = "Child", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "ChildInfo")
    private final List<Element<HomeChild>> children;

    @CCD(
            label = "Is the property adapted in any way for the applicant, child, children or anyone else living there?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPropertyAdapted;
    @CCD(
            label = "Details of special adaptations:",
            showCondition = "howIsThePropertyAdapted=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo howIsThePropertyAdapted;
    @CCD(label = "Details of special adaptations:", searchable = false)
    private final String howIsThePropertyAdaptedText;
    @CCD(label = "Is there a mortgage on the property?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isThereMortgageOnProperty;
    @CCD(label = "Who is named on the mortgage?", searchable = false)
    private final String mortgageNamedAfter;
    @CCD(label = "Mortgage number (if known)", searchable = false)
    private final String mortgageNumber;
    @CCD(label = "Mortgage lender’s name", searchable = false)
    private final String mortgageLenderName;
    @CCD(label = "Mortgage lender’s address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address mortgageAddress;
    @CCD(label = "Is the property rented?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPropertyRented;
    @CCD(label = "Who is named in the rental agreement?", searchable = false)
    private final String landLordNamedAfter;
    @CCD(label = "What is the name of the landlord of the rental property?", searchable = false)
    private final String landlordName;
    @CCD(label = "Landlord’s address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address landlordAddress;
    @CCD(label = "Does the applicant have any home rights?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo doesApplicantHaveHomeRights;
    @CCD(label = "What exactly does the applicant want to happen with their living situation?", searchable = false)
    private final String livingSituation;
    @CCD(label = "Is there anything else the applicant wants to happen with their family home?", searchable = false)
    private final String familyHome;
    @CCD(label = "Any further information the applicant would LIKE to be considered by the court:", searchable = false)
    private final String furtherInformation;
}
