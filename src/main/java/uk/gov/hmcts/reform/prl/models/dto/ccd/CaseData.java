package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityStatementDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.CourtDetailsPilotEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
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
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ConfidentialityDisclaimer;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.GatekeeperEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
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
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CaseData implements MappableObject {

    private final long id;

    private final State state;

    @JsonIgnore
    private final LocalDateTime createdDate;

    @JsonIgnore
    private final LocalDateTime lastModifiedDate;

    private final String dateSubmitted;
    private final String dateSubmittedAndTime;

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
     * C100 Confidential Disclaimer.
     */
    private final List<ConfidentialityStatementDisclaimerEnum> c100ConfidentialityStatementDisclaimer;
    private final ConfidentialityDisclaimer confidentialityDisclaimer;


    /**
     * Upload documents.
     */

    private final List<Document> contactOrderDocumentsUploaded;
    private final List<Document> c8FormDocumentsUploaded;
    private final List<Document> otherDocumentsUploaded;

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
    private final Document draftConsentOrderFile;
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
    private final Document miamCertificationDocumentUpload;
    private final String mediatorRegistrationNumber1;
    private final String familyMediatorServiceName1;
    private final String soleTraderName1;
    private final Document miamCertificationDocumentUpload1;

    @JsonUnwrapped
    private final AllegationOfHarm allegationOfHarm;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate issueDate;

    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("feeAmount")
    private final String feeAmount;
    @JsonProperty("feeCode")
    private final String feeCode;
    @JsonProperty("draftOrderDoc")
    private final Document draftOrderDoc;
    @JsonProperty("draftOrderDocWelsh")
    private final Document draftOrderDocWelsh;
    @JsonProperty("c8Document")
    private final Document c8Document;
    @JsonProperty("c8WelshDocument")
    private final Document c8WelshDocument;
    @JsonProperty("c1ADocument")
    private final Document c1ADocument;
    @JsonProperty("c1AWelshDocument")
    private final Document c1AWelshDocument;

    @JsonProperty("isEngDocGen")
    private final String isEngDocGen;
    @JsonProperty("isWelshDocGen")
    private final String isWelshDocGen;

    @JsonProperty("submitAndPayDownloadApplicationLink")
    private final Document submitAndPayDownloadApplicationLink;
    @JsonProperty("submitAndPayDownloadApplicationWelshLink")
    private final Document submitAndPayDownloadApplicationWelshLink;

    /**
     * Add case number.
     */
    private final String familymanCaseNumber;
    private final String fl401FamilymanCaseNumber;

    /**
     * Manage Documents.
     */
    private final DocumentCategoryEnum documentCategory;
    private final List<Element<FurtherEvidence>> furtherEvidences;
    @JsonProperty("giveDetails")
    private final String giveDetails;

    private final List<Element<Correspondence>> correspondence;
    private final List<Element<OtherDocuments>> otherDocuments;

    private final List<Element<FurtherEvidence>> mainAppDocForTabDisplay;
    private final List<Element<Correspondence>> correspondenceForTabDisplay;
    private final List<Element<OtherDocuments>> otherDocumentsForTabDisplay;

    private final List<Element<UserInfo>> userInfo;

    /**
     * Return Application.
     */
    private final List<RejectReasonEnum> rejectReason;
    private final List<FL401RejectReasonEnum> fl401RejectReason;
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
     * Home Situation DA.
     */
    private final Home home;

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
    private final List<Element<LocalCourtAdminEmail>> localCourtAdmin;

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
    private String courtEmailAddress;
    private String reasonForAmendCourtDetails;

    /**
     * Final document.
     */

    @JsonProperty("finalDocument")
    private final Document finalDocument;

    /**
     * Send and reply to messages.
     */
    @JsonProperty("openMessages")
    private final List<Element<Message>> openMessages;

    @JsonProperty("closedMessages")
    private final List<Element<Message>> closedMessages;

    @JsonProperty("messageObject")
    MessageMetaData messageMetaData;
    String messageContent;
    Object replyMessageDynamicList;
    Message messageReply;
    SendOrReply chooseSendOrReply;

    public static String[] temporaryFields() {
        return new String[]{
            "replyMessageDynamicList", "messageReply", "messageContent",
            "messageReply", "messageMetaData"
        };
    }


    @JsonProperty("finalWelshDocument")
    private final Document finalWelshDocument;

    /**
     * Confidentiality details.
     */
    private final List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;
    private final List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails;

    private final Map<String, Object> typeOfApplicationTable;

    /**
     *  Withdraw Application.
     */
    private final WithdrawApplication withDrawApplicationData;

    /**
     * FL401 Upload Documents.
     */
    private final List<Element<Document>> fl401UploadWitnessDocuments;
    private final List<Element<Document>> fl401UploadSupportDocuments;

    /**
     * Send to Gatekeeper.
     */
    private final List<Element<GatekeeperEmail>> gatekeeper;

    /**
     * FL401 Other Proceedings.
     */
    private final FL401OtherProceedingDetails fl401OtherProceedingDetails;

    /**
     *  FL401 Statement Of truth and submit.
     */
    @JsonProperty("fl401StmtOfTruth")
    private final StatementOfTruth fl401StmtOfTruth;

    @JsonProperty("viewPDFlinkLabelText")
    private String viewPdfLinkLabelText;

    private List<Element<CaseInvite>> respondentCaseInvites;


    /**
     *  FL401 submit status flags.
     */
    private String isCourtEmailFound;
    private String isDocumentGenerated;
    private String isNotificationSent;

    private ChildArrangementOrdersEnum childArrangementOrders;

    /**
     *  Manage Orders.
     */
    //upload orders flow

    /*
    private DomesticAbuseOrdersEnum domesticAbuseOrders;
    private FcOrdersEnum fcOrders;
    private OtherOrdersOptionEnum otherOrdersOption;
    private String nameOfOrder;
    private YesOrNo isTheOrderUploadedByConsent;*/
    private LocalDate approvalDate;
    private Document appointmentOfGuardian;
    private Document previewOrderDoc;

    private final ManageOrdersOptionsEnum manageOrdersOptions;
    private final CreateSelectOrderOptionsEnum createSelectOrderOptions;

    /*Commented below as getiing too many parameters error for @AllArgsConstructor
    * */
    //create orders flow
    //private final CafcassEnum cafcassRecipient;

    //private final HearingTypeEnum hearingType;
    //private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;

    //private final OrderRecipientsEnum orderRecipients;
    //private final OtherEnum otherRecipient;
    //private final OtherOrderRecipientsEnum otherOrderRecipients;
    //private final SelectTypeOfOrderEnum selectTypeOfOrder;
    //private final YesNoNotRequiredEnum isTheOrderAboutAllChildren;


    private final YesOrNo doesOrderClosesCase;
    private final YesOrNo isTheOrderByConsent;
    private final YesOrNo wasTheOrderApprovedAtHearing;
    private final String judgeOrMagistratesLastName;
    private final String justiceLegalAdviserFullName;
    private final String dateOrderMade;
    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;
    private final List<String> cafcassEmailAddress;
    private final List<String> otherEmailAddress;
    private final String childrenList;

    /**
     * Solicitor Details.
     */
    private String caseSolicitorName;
    private String caseSolicitorOrgName;
    private String selectedOrder;


    /**
     * FL401 Court details for Pilot.
     */
    private final CourtDetailsPilotEnum submitCountyCourtSelection;

    public CaseData setDateSubmittedDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        this.toBuilder()
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .dateSubmittedAndTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime))
            .build();

        return this;
    }

    public CaseData setIssueDate() {
        this.toBuilder()
            .issueDate(LocalDate.now())
            .build();

        return this;
    }

    /**
     *  Withdraw request flag.
     */
    private String isWithdrawRequestSent;
}
