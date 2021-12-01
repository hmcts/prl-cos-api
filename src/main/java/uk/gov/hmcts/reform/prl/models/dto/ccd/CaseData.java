package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.*;
import uk.gov.hmcts.reform.prl.models.complextypes.*;
import uk.gov.hmcts.reform.prl.models.documents.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CaseData {

    private final long id;

    @JsonProperty("LanguagePreferenceWelsh")
    private final YesOrNo languagePreferenceWelsh;

    /**
     * Case name
     */
    private final String applicantCaseName;

    /**
     * Type of application
     */
    private final List<OrderTypeEnum> ordersApplyingFor;
    private final childArrangementOrderTypeEnum typeOfChildArrangementsOrder;
    private final String natureOfOrder;
    private final YesOrNo consentOrder;
    private final ConsentOrderDocument draftConsentOrderFile;
    private final PermissionRequiredEnum applicationPermissionRequired;
    private final String applicationPermissionRequiredReason;
    private final String applicationDetails;

    /**
     * Hearing urgency
     */
    private final YesOrNo isCaseUrgent;
    private final String caseUrgencyTimeAndReason;
    private final String effortsMadeWithRespondents;
    private final YesOrNo doYouNeedAWithoutNoticeHearing;
    private final String reasonsForApplicationWithoutNotice;
    private final YesOrNo doYouRequireAHearingWithReducedNotice;
    private final String setOutReasonsBelow;
    private final YesOrNo areRespondentsAwareOfProceedings;

    /**
     * Applicant details
     */
    private final List<Element<PartyDetails>> applicants;

    /**
     * Child details
     */
    private final List<Element<Child>> children;
    private final YesNoDontKnow childrenKnownToLocalAuthority;
    private final String childrenKnownToLocalAuthorityTextArea;
    private final YesNoDontKnow childrenSubjectOfChildProtectionPlan;

    /**
     * Respondent details
     */
    private final List<Element<PartyDetails>> respondents;

    /**
     * MIAM
     */
    private final YesOrNo applicantAttendedMIAM;
    private final YesOrNo claimingExemptionMIAM;
    private final YesOrNo familyMediatorMIAM;
    private final List<MIAMExemptionsChecklistEnum> miamExemptionsChecklist;
    private final List<MIAMDomesticViolenceChecklistEnum> miamDomesticViolenceChecklist;
    private final List<MIAMUrgencyReasonChecklistEnum> miamUrgencyReasonChecklist;
    private final MIAMPreviousAttendanceChecklistEnum miamPreviousAttendanceChecklist;
    private final MIAMOtherGroundsChecklistEnum miamOtherGroundsChecklist;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    //TODO: refactor to remove duplicated details screen
    private final MIAMDocument mIAMCertificationDocumentUpload;
    private final String mediatorRegistrationNumber1;
    private final String familyMediatorServiceName1;
    private final String soleTraderName1;
    private final MIAMDocument miamCertificationDocumentUpload1;

    /**
     * Allegations of harm
     */
    private final YesOrNo allegationsOfHarmYesNo;
    private final YesOrNo allegationsOfHarmDomesticAbuseYesNo;
    private final List<ApplicantOrChildren> physicalAbuseVictim;
    private final List<ApplicantOrChildren> emotionalAbuseVictim;
    private final List<ApplicantOrChildren> psychologicalAbuseVictim;
    private final List<ApplicantOrChildren> sexualAbuseVictim;
    private final List<ApplicantOrChildren> financialAbuseVictim;
    private final YesOrNo allegationsOfHarmChildAbductionYesNo;
    private final String childAbductionReasons;
    private final YesOrNo previousAbductionThreats;
    private final String previousAbductionThreatsDetails;
    private final String childrenLocationNow;
    private final YesOrNo abductionPassportOfficeNotified;
    private final YesOrNo abductionPreviousPoliceInvolvement;
    private final String abductionPreviousPoliceInvolvementDetails;
    private final YesOrNo abductionOtherSafetyConcerns;
    private final String abductionOtherSafetyConcernsDetails;
    private final String abductionCourtStepsRequested;
    private final List<Element<Behaviours>> behaviours;
    private final YesOrNo ordersNonMolestation;
    private final YesOrNo ordersOccupation;
    private final YesOrNo ordersForcedMarriageProtection;
    private final YesOrNo ordersRestraining;
    private final YesOrNo ordersOtherInjunctive;
    private final YesOrNo ordersUndertakingInPlace;
    private final LocalDate ordersNonMolestationDateIssued;
    private final LocalDate ordersNonMolestationEndDate;
    private final YesOrNo ordersNonMolestationCurrent;
    private final String ordersNonMolestationCourtName;
    private final OtherDocument ordersNonMolestationDocument;
    private final LocalDate ordersOccupationDateIssued;
    private final LocalDate ordersOccupationEndDate;
    private final YesOrNo ordersOccupationCurrent;
    private final String ordersOccupationCourtName;
    private final OtherDocument ordersOccupationDocument;
    private final LocalDate ordersForcedMarriageProtectionDateIssued;
    private final LocalDate ordersForcedMarriageProtectionEndDate;
    private final YesOrNo ordersForcedMarriageProtectionCurrent;
    private final String ordersForcedMarriageProtectionCourtName;
    private final OtherDocument ordersForcedMarriageProtectionDocument;
    private final LocalDate ordersRestrainingDateIssued;
    private final LocalDate ordersRestrainingEndDate;
    private final YesOrNo ordersRestrainingCurrent;
    private final String ordersRestrainingCourtName;
    private final OtherDocument ordersRestrainingDocument;
    private final LocalDate ordersOtherInjunctiveDateIssued;
    private final LocalDate ordersOtherInjunctiveEndDate;
    private final YesOrNo ordersOtherInjunctiveCurrent;
    private final String ordersOtherInjunctiveCourtName;
    private final OtherDocument ordersOtherInjunctiveDocument;
    private final LocalDate ordersUndertakingInPlaceDateIssued;
    private final LocalDate ordersUndertakingInPlaceEndDate;
    private final YesOrNo ordersUndertakingInPlaceCurrent;
    private final String ordersUndertakingInPlaceCourtName;
    private final OtherDocument ordersUndertakingInPlaceDocument;
    private final YesOrNo allegationsOfHarmOtherConcerns;
    private final String allegationsOfHarmOtherConcernsDetails;
    private final String allegationsOfHarmOtherConcernsCourtActions;

    /**
     * Other people in the case
     */
    private final List<Element<PartyDetails>> othersToNotify;

    /**
     * Other proceedings
     */

    /**
     * Attending the hearing
     */
    private final YesOrNo isWelshNeeded;
    private final List<Element<WelshNeeds>> welshNeeds;
    private final YesOrNo isInterpreterNeeded;
    private final List<Element<InterpreterNeed>> interpreterNeeds;
    private final YesOrNo isDisabilityPresent;
    private final String adjustmentsRequired;
    private final YesOrNo isSpecialArrangementsRequired;
    private final String specialArrangementsRequired;
    private final YesOrNo isIntermediaryNeeded;
    private final String reasonsForIntermediary;

    /**
     * International element
     */
    private final YesOrNo habitualResidentInOtherState;
    private final YesOrNo jurisdictionIssue;
    private final YesOrNo requestToForeignAuthority;
    private final String habitualResidentInOtherStateGiveReason;
    private final String jurisdictionIssueGiveReason;
    private final String requestToForeignAuthorityGiveReason;

    /**
     * Litigation capacity
     */
    private final String litigationCapacityFactors;
    private final String litigationCapacityReferrals;
    private final YesOrNo litigationCapacityOtherFactors;
    private final String litigationCapacityOtherFactorsDetails;

    /**
     * Welsh language requirements
     */
    private final YesOrNo welshLanguageRequirement;
    private final LanguagePreference welshLanguageRequirementApplication;
    private final YesOrNo languageRequirementApplicationNeedWelsh;
    private final YesOrNo welshLanguageRequirementApplicationNeedEnglish;


    /**
     * Upload documents
     */
    private final List<Element<ContactOrderDocument>> contactOrderDocumentsUploaded;
    private final List<Element<C8Document>> c8FormDocumentsUploaded;
    private final List<Element<OtherDocument>> otherDocumentsUploaded;

}
