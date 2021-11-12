package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.*;


import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class CaseData {

    @JsonProperty("LanguagePreferenceWelsh")
    private final YesOrNo languagePreferenceWelsh;
    private final String applicantName;
    private final String childName;
    private final List<ContactOrderDocument> contactOrderDocumentsUploaded;
    private final List<C8Document> c8FormDocumentsUploaded;
    private final List<OtherDocument> otherDocumentsUploaded;
    private final YesOrNo isWelshNeeded;
    private final List<WelshNeeds> welshNeeds;
    private final YesOrNo isInterpreterNeeded;
    private final List<InterpreterNeeds> interpreterNeeds;
    private final YesOrNo isDisabilityPresent;
    private final String adjustmentsRequired;
    private final YesOrNo isSpecialArrangementsRequired;
    private final String specialArrangementsRequired;
    private final YesOrNo isIntermediaryNeeded;
    private final String reasonsForIntermediary;
    private final List<OrderTypeEnum> ordersApplyingFor;
    private final String natureOfOrder;
    private final PermissionRequiredEnum applicationPermissionRequired;
    private final String applicationPermissionRequiredReason;
    private final String applicationDetails;
    private final YesOrNo isApplicationUrgent;
    private final YesOrNo isApplicationConsideredWithoutNotice;
    private final YesOrNo isHearingWithoutNoticeRequiredNotPossible;
    private final YesOrNo isHearingWithoutNoticeRequiredRespondentWillFrustrate;
    private final String applicationUrgencyOrders;
    private final String applicationReasonsForUrgency;
    private final DaysAndHours applicationConsideredInDaysAndHours;
    private final String applicationNoticeEfforts;
    private final String applicationWithoutNoticeReasons;
    private final String applicationWithoutNoticeNotPossibleReasons;
    private final String applicationWithoutNoticeRespondentWillFrustrateReasons;
    private final YesOrNo domesticAbuse;
    private final YesOrNo childAbduction;
    private final YesOrNo childAbuse;
    private final YesOrNo drugsAlcoholSubstanceAbuse;
    private final YesOrNo safetyWelfareConcerns;
    private final YesOrNo childAtRiskOfAbduction;
    private final YesOrNo policeNotified;
    private final YesOrNo childHasPassport;
    private final YesOrNo childAbductedBefore;
    private final YesOrNo childHasMultiplePassports;
    private final List<PassportPossessionEnum> childPassportPossession;
    private final String childPassportPossessionOtherDetails;
    private final String childAbductionDetails;
    private final YesOrNo abductionPoliceInvolved;
    private final String abductionPoliceInvolvedDetails;
    private final String childAtRiskOfAbductionReason;
    private final String childWhereabouts;
    private final YesOrNo childAbuseSexually;
    private final String childAbuseSexuallyDetails;
    private final LocalDate childAbuseSexuallyStartDate;
    private final YesOrNo childAbuseSexuallyOngoing;
    private final String childAbuseSexuallyHelpSought;
    private final YesOrNo childAbusePhysically;
    private final String childAbusePhysicallyDetails;
    private final LocalDate childAbusePhysicallyStartDate;
    private final YesOrNo childAbusePhysicallyOngoing;
    private final String childAbusePhysicallyHelpSought;
    private final YesOrNo childAbuseFinancially;
    private final String childAbuseFinanciallyDetails;
    private final LocalDate childAbuseFinanciallyStartDate;
    private final YesOrNo childAbuseFinanciallyOngoing;
    private final String childAbuseFinanciallyHelpSought;
    private final YesOrNo childAbuseDomestic;
    private final String childAbuseDomesticDetails;
    private final LocalDate childAbuseDomesticStartDate;
    private final YesOrNo childAbuseDomesticOngoing;
    private final String childAbuseDomesticHelpSought;
    private final YesOrNo childDrugsAlcoholSubstanceAbuse;
    private final String childDrugsAlcoholSubstanceAbuseDetails;
    private final LocalDate childDrugsAlcoholSubstanceAbuseStartDate;
    private final YesOrNo childDrugsAlcoholSubstanceAbuseOngoing;
    private final String childDrugsAlcoholSubstanceAbuseHelpSought;
    private final YesOrNo otherSafetyOrWelfareConcerns;
    private final String otherSafetyOrWelfareConcernsDetails;
    private final Address childrenAddress;
    private final List<Child> children;
    private final YesNoDontKnow isChildrenKnownToAuthority;
    private final String childAndLocalAuthority;
    private final YesNoDontKnow isChildrenUnderChildProtection;
    private final YesNoDontKnow isChildrenWithSameParents;
    private final String parentsAndTheirChildren;
    private final String parentalResponsibilities;
    private final WhoChildrenLiveWith whoChildrenLiveWith;
    private final String childAddressAndAdultsLivingWith;
    private final YesOrNo isExistingProceedings;
    private final String childrenInProceeding;
    private final List<ProceedingDetails> existingProceedings;
    private final List<PartyDetails> applicants;
    private final List<PartyDetails> respondents;
    private final List<PartyDetails> othersToNotify;
    private final List<Child> otherChildren;
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
    private final YesOrNo consentOrder;
    private final ConsentOrderDocument draftConsentOrderFile;

}

