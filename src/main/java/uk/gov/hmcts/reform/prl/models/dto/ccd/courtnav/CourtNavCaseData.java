package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SignatureEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CourtNavCaseData implements MappableObject {

    private final ApplicantAge applicantHowOld;

    private final String courtNavCaseName;


    /**
     * type of application.
     */

    private final List<FL401OrderTypeEnum> ordersAppliedFor;

    /**
     * without notice order.
     */
    private final boolean ordersAppliedWithoutNotice;
    private final List<WithoutNoticeReasonEnum> ordersAppliedWithoutNoticeReason;
    private final String ordersAppliedWithoutNoticeReasonDetails;
    private final boolean bailConditionsOnRespondent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate bailConditionsEndDate;
    private final String additionalDetailsForCourt;

    /**
     * Applicant details.
     */
    private final ApplicantsDetails applicantDetails;

    /**
     * Respondent Details.
     */
    private final RespondentDetails respondentDetails;

    /**
     * Applicant's Family.
     */
    private final YesOrNo whoApplicationIsFor;
    private final List<Element<ProtectedChild>> protectedChildren;

    /**
     * Relationship to Respondent.
     */
    private final ApplicantRelationshipDescriptionEnum relationshipDescription;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipStartDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate relationshipEndDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ceremonyDate;
    private final ApplicantRelationshipOptionsEnum respondentsRelationshipToApplicant;
    private final String relationshipToApplicantOther;
    private final boolean anyChildren;

    /**
     * Respondent's Behaviour.
     */
    private final boolean applyingForMonMolestationOrder;
    private final List<BehaviourTowardsApplicantEnum> stopBehaviourTowardsApplicant;
    private final List<BehaviourTowardsChildrenEnum> stopBehaviourTowardsChildren;
    private final String stopBehaviourAnythingElse;

    /**
     * Home.
     */
    private final boolean applyingForOccupationOrder;
    private final Address occupationOrderAddress;
    private final List<CurrentResidentAtAddressEnum> currentlyLivesAtAddress;
    private final String currentlyLivesAtAddressOther;
    private final PreviousOrIntendedResidentAtAddressEnum previouslyLivedAtAddress;
    private final PreviousOrIntendedResidentAtAddressEnum intendedToLiveAtAddress;
    private final List<Element<ChildAtAddress>> childrenApplicantResponsibility;
    private final boolean propertySpeciallyAdapted;
    private final String propertySpeciallyAdaptedDetails;
    private final boolean propertyHasMortgage;
    private final List<ContractEnum> namedOnMortgage;
    private final String namedOnMortgageOther;
    private final String mortgageNumber;
    private final String mortgageLenderName;
    private final Address mortgageLenderAddress;
    private final boolean propertyIsRented;
    private final List<ContractEnum> namedOnRentalAgreement;
    private final String namedOnRentalAgreementOther;
    private final String landlordName;
    private final Address landlordAddress;
    private final boolean haveHomeRights;
    private final List<LivingSituationOutcomeEnum> wantToHappenWithLivingSituation;
    private final List<FamilyHomeOutcomeEnum> wantToHappenWithFamilyHome;
    private final String anythingElseForCourtToConsider;

    /**
     * Statement of truth.
     */
    private final FL401Consent declaration;
    private final SignatureEnum signature;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate signatureDate;
    private final String signatureFullName;
    private final String representativeFirmName;
    private final String representativePositionHeld;

    /**
     * Ongoing proceedings.
     */

    private final boolean anyOngoingCourtProceedings;
    private final List<Element<CourtProceedings>> ongoingCourtProceedings;

    /**
     * Going to court.
     */
    private final boolean isInterpreterRequired;
    private final String interpreterLanguage;
    private final String interpreterDialect;
    private final boolean anyDisabilityNeeds;
    private final String disabilityNeedsDetails;
    private final List<SpecialMeasuresEnum> anySpecialMeasures;
    private final String courtSpecialRequirements;

}
