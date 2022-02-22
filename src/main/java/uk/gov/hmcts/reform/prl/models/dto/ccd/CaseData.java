package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityStatementDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.WhoChildrenLiveWith;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.C8Document;
import uk.gov.hmcts.reform.prl.models.documents.ConsentOrderDocument;
import uk.gov.hmcts.reform.prl.models.documents.ContactOrderDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.MiamDocument;
import uk.gov.hmcts.reform.prl.models.documents.OtherDocument;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CaseData implements MappableObject {

    private final long id;

    private final State state;

    private final LocalDateTime createdDate;

    private final LocalDateTime lastModifiedDate;

    private final String dateSubmitted;

    @JsonProperty("LanguagePreferenceWelsh")
    private final YesOrNo languagePreferenceWelsh;

    /**
     * Case Type Of Application.
     */
    private final String caseTypeOfApplication;

    /**
     * Case name.
     */
    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private final String applicantCaseName;

    /**
     * Confidential Disclaimer.
     */
    private final List<ConfidentialityStatementDisclaimerEnum> confidentialityStatementDisclaimer;
    private final List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksDisclaimer;

    /**
     * Upload documents.
     */

    private final List<ContactOrderDocument> contactOrderDocumentsUploaded;
    private final List<C8Document> c8FormDocumentsUploaded;
    private final List<OtherDocument> otherDocumentsUploaded;

    /**
     * People in the case.
     */

    private final Address childrenAddress;
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
    private final List<Element<Child>> otherChildren;


    /**
     * Type of application.
     */
    private final List<OrderTypeEnum> ordersApplyingFor;
    private final ChildArrangementOrderTypeEnum typeOfChildArrangementsOrder;
    private final String natureOfOrder;
    private final YesOrNo consentOrder;
    private final ConsentOrderDocument draftConsentOrderFile;
    private final PermissionRequiredEnum applicationPermissionRequired;
    private final String applicationPermissionRequiredReason;
    private final String applicationDetails;

    /**
     * Hearing urgency.
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
     * Applicant details.
     */
    private final List<Element<PartyDetails>> applicants;
    @JsonProperty("applicantsFL401")
    private final PartyDetails applicantsFL401;

    /**
     * Child details.
     */
    private final List<Element<Child>> children;
    private final YesNoDontKnow childrenKnownToLocalAuthority;
    private final String childrenKnownToLocalAuthorityTextArea;
    private final YesNoDontKnow childrenSubjectOfChildProtectionPlan;

    /**
     * Respondent details.
     */
    private final List<Element<PartyDetails>> respondents;
    @JsonProperty("respondentsFL401")
    private final PartyDetails respondentsFL401;


    /**
     * MIAM.
     */
    private final YesOrNo applicantAttendedMiam;
    private final YesOrNo claimingExemptionMiam;
    private final YesOrNo familyMediatorMiam;
    private final List<MiamExemptionsChecklistEnum> miamExemptionsChecklist;
    private final List<MiamDomesticViolenceChecklistEnum> miamDomesticViolenceChecklist;
    private final List<MiamUrgencyReasonChecklistEnum> miamUrgencyReasonChecklist;
    private final List<MiamChildProtectionConcernChecklistEnum> miamChildProtectionConcernList;
    private final MiamPreviousAttendanceChecklistEnum miamPreviousAttendanceChecklist;
    private final MiamOtherGroundsChecklistEnum miamOtherGroundsChecklist;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    //TODO: refactor to remove duplicated details screen
    private final MiamDocument miamCertificationDocumentUpload;
    private final String mediatorRegistrationNumber1;
    private final String familyMediatorServiceName1;
    private final String soleTraderName1;
    private final MiamDocument miamCertificationDocumentUpload1;

    /**
     * Allegations of harm.
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
    private final YesOrNo abductionChildHasPassport;
    private final AbductionChildPassportPossessionEnum abductionChildPassportPosession;
    private final String abductionChildPassportPosessionOtherDetail;
    private final YesOrNo abductionPreviousPoliceInvolvement;
    private final String abductionPreviousPoliceInvolvementDetails;
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    private final YesOrNo allegationsOfHarmSubstanceAbuseYesNo;
    private final YesOrNo allegationsOfHarmOtherConcernsYesNo;
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
    private final YesOrNo agreeChildUnsupervisedTime;
    private final YesOrNo agreeChildSupervisedTime;
    private final YesOrNo agreeChildOtherContact;


    /**
     * Other people in the case.
     */
    private final List<Element<PartyDetails>> othersToNotify;

    /**
     * Other proceedings.
     */

    private final YesNoDontKnow previousOrOngoingProceedingsForChildren;
    private final List<Element<ProceedingDetails>> existingProceedings;


    /**
     * Attending the hearing.
     */
    private final YesOrNo isWelshNeeded;
    @JsonAlias({"welshNeeds", "fl401WelshNeeds"})
    private final List<Element<WelshNeed>> welshNeeds;
    private final YesOrNo isInterpreterNeeded;
    private final List<Element<InterpreterNeed>> interpreterNeeds;
    private final YesOrNo isDisabilityPresent;
    private final String adjustmentsRequired;
    private final YesOrNo isSpecialArrangementsRequired;
    private final String specialArrangementsRequired;
    private final YesOrNo isIntermediaryNeeded;
    private final String reasonsForIntermediary;

    /**
     * International element.
     */
    private final YesOrNo habitualResidentInOtherState;
    private final String habitualResidentInOtherStateGiveReason;
    private final YesOrNo jurisdictionIssue;
    private final String jurisdictionIssueGiveReason;
    private final YesOrNo requestToForeignAuthority;
    private final String requestToForeignAuthorityGiveReason;

    /**
     * Litigation capacity.
     */
    private final String litigationCapacityFactors;
    private final String litigationCapacityReferrals;
    private final YesOrNo litigationCapacityOtherFactors;
    private final String litigationCapacityOtherFactorsDetails;

    /**
     * Welsh language requirements.
     */
    private final YesOrNo welshLanguageRequirement;
    private final LanguagePreference welshLanguageRequirementApplication;
    private final YesOrNo languageRequirementApplicationNeedWelsh;
    private final YesOrNo welshLanguageRequirementApplicationNeedEnglish;

    private final CcdPaymentServiceRequestUpdate paymentCallbackServiceRequestUpdate;
    @JsonProperty("paymentServiceRequestReferenceNumber")
    private final String paymentServiceRequestReferenceNumber;

    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("feeAmount")
    private final String feeAmount;
    @JsonProperty("feeCode")
    private final String feeCode;
    @JsonProperty("draftOrderDoc")
    private final Document draftOrderDoc;
    @JsonProperty("c8Document")
    private final Document c8Document;
    @JsonProperty("c1ADocument")
    private final Document c1ADocument;



    @JsonProperty("submitAndPayDownloadApplicationLink")
    private final Document submitAndPayDownloadApplicationLink;

    /**
     * Add case number.
     */
    private final String familymanCaseNumber;

    /**
     * Manage Documents.
     */
    private final DocumentCategoryEnum documentCategory;
    private final List<Element<FurtherEvidence>> furtherEvidences;
    @JsonProperty("giveDetails")
    private final String giveDetails;

    private final List<Element<Correspondence>> correspondence;
    private final List<Element<OtherDocuments>> otherDocuments;

    private final List<Element<UserInfo>> userInfo;


    /**
     * Return Application.
     */
    private final List<RejectReasonEnum> rejectReason;
    private final String returnMessage;

    /**
     * Without Notice Order.
     */
    @JsonProperty("orderWithoutGivingNoticeToRespondent")
    private final WithoutNoticeOrderDetails orderWithoutGivingNoticeToRespondent;
    @JsonProperty("reasonForOrderWithoutGivingNotice")
    private final ReasonForWithoutNoticeOrder reasonForOrderWithoutGivingNotice;
    @JsonProperty("bailDetails")
    private final RespondentBailConditionDetails bailDetails;
    @JsonProperty("anyOtherDtailsForWithoutNoticeOrder")
    private final OtherDetailsOfWithoutNoticeOrder anyOtherDtailsForWithoutNoticeOrder;

    /**
     * FL401 Respondents relationship.
     */
    private final RespondentRelationObjectType respondentRelationObject;
    private final RespondentRelationDateInfo respondentRelationDateInfoObject;
    private final RespondentRelationOptionsInfo respondentRelationOptions;

    /**
     * FL401 Type of Application.
     */
    @JsonProperty("typeOfApplicationOrders")
    private final TypeOfApplicationOrders typeOfApplicationOrders;
    @JsonProperty("typeOfApplicationLinkToCA")
    private final LinkToCA typeOfApplicationLinkToCA;

    /**
     * Respondent Behaviour.
     */
    private final RespondentBehaviour respondentBehaviourData;
    @JsonProperty("applicantFamilyDetails")
    private final ApplicantFamilyDetails applicantFamilyDetails;
    @JsonProperty("applicantChildDetails")
    private final List<Element<ApplicantChild>> applicantChildDetails;

    /**
     * Issue and send to local court'.
     */
    private final List<Element<LocalCourtAdminEmail>> localCourtAdminEmail;

    /**
     * This field contains Application Submitter solicitor email address.
     */
    private final String applicantSolicitorEmailAddress;
    private final String respondentSolicitorEmailAddress;
    private final String caseworkerEmailAddress;


    /**
     * Court details.
     */

    private String courtName;
    private String courtId;

    /**
     * Confidentiality details.
     */
    private final List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;
    private final List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails;

    /**
     *  Withdraw Application.
     */
    private final WithdrawApplication withDrawApplicationData;

}
