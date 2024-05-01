package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityStatementDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.TransferToAnotherCourtReasonDaEnum;
import uk.gov.hmcts.reform.prl.enums.TransferToAnotherCourtReasonEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DomesticAbuseOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.FcOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrdersOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentC8;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ConfidentialityDisclaimer;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.GatekeeperEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
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
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.Fl401ListOnNotice;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder(toBuilder = true)
public class CaseData extends BaseCaseData implements MappableObject {

    @JsonProperty("LanguagePreferenceWelsh")
    private final YesOrNo languagePreferenceWelsh;

    /**
     * Case created by.
     */
    private CaseCreatedBy caseCreatedBy;

    @JsonProperty("isCafcass")
    private YesOrNo isCafcass;

    private String applicantName;

    private String respondentName;


    private String childName;

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
     * C100 Help with Fees.
     */
    private final YesOrNo helpWithFees;
    @JsonProperty("helpWithFeesReferenceNumber")
    private final String helpWithFeesNumber;

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
    private final String childrenInProceeding;
    private final List<Element<Child>> otherChildren;

    // people feature FPET-325
    @JsonUnwrapped
    private final Relations relations;

    /**
     * Type of application.
     */
    private List<OrderTypeEnum> ordersApplyingFor;
    private ChildArrangementOrderTypeEnum typeOfChildArrangementsOrder;
    private String natureOfOrder;
    private final YesOrNo consentOrder;
    private final Document draftConsentOrderFile;
    private final PermissionRequiredEnum applicationPermissionRequired;
    private final String applicationPermissionRequiredReason;
    private final String applicationDetails;

    /**
     * Hearing urgency.
     */
    private YesOrNo isCaseUrgent;
    private String caseUrgencyTimeAndReason;
    private String effortsMadeWithRespondents;
    private YesOrNo doYouNeedAWithoutNoticeHearing;
    private String reasonsForApplicationWithoutNotice;
    private YesOrNo doYouRequireAHearingWithReducedNotice;
    private String setOutReasonsBelow;
    private YesOrNo areRespondentsAwareOfProceedings;

    /**
     * Applicant details.
     */
    private final List<Element<PartyDetails>> applicants;
    @JsonProperty("applicantsFL401")
    private final PartyDetails applicantsFL401;

    /**
     * caseNotes details.
     */
    private List<Element<CaseNoteDetails>> caseNotes;
    //@JsonProperty("caseNoteDetails")
    //private final CaseNoteDetails caseNoteDetails;
    private final String subject;
    private final String caseNote;


    /**
     * Child Details Revised.
     */
    private List<Element<ChildDetailsRevised>> newChildDetails;


    /**
     * Children are not in the case but related to this case.
     */
    private List<Element<OtherChildrenNotInTheCase>> childrenNotInTheCase;

    private YesOrNo childrenNotPartInTheCaseYesNo;

    /**
     * Child details.
     */
    private List<Element<Child>> children;
    private YesNoDontKnow childrenKnownToLocalAuthority;
    private String childrenKnownToLocalAuthorityTextArea;
    private YesNoDontKnow childrenSubjectOfChildProtectionPlan;

    /**
     * Respondent details.
     */
    private final List<Element<PartyDetails>> respondents;
    @JsonProperty("respondentsFL401")
    private final PartyDetails respondentsFL401;


    /**
     * MIAM.
     */
    @JsonUnwrapped
    private final MiamDetails miamDetails;

    /**
     * MIAM.
     */
    @JsonUnwrapped
    private final MiamPolicyUpgradeDetails miamPolicyUpgradeDetails;

    /**
     * Allegations of harm.
     */

    @JsonUnwrapped
    private final AllegationOfHarm allegationOfHarm;


    @JsonUnwrapped
    private final AllegationOfHarmRevised allegationOfHarmRevised;

    /**
     * Other people in the case.
     */
    private final List<Element<PartyDetails>> othersToNotify;


    /**
     * Other people in the case.
     */
    private final List<Element<PartyDetails>> otherPartyInTheCaseRevised;



    /**
     * Other proceedings.
     */

    private final YesNoDontKnow previousOrOngoingProceedingsForChildren;
    private final List<Element<ProceedingDetails>> existingProceedings;

    /**
     * Attending the hearing.
     */
    @JsonUnwrapped
    private final AttendHearing attendHearing;

    /**
     * International element.
     */
    private YesOrNo habitualResidentInOtherState;
    private String habitualResidentInOtherStateGiveReason;
    private YesOrNo jurisdictionIssue;
    private String jurisdictionIssueGiveReason;
    private YesOrNo requestToForeignAuthority;
    private String requestToForeignAuthorityGiveReason;

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

    @JsonProperty("c8DraftDocument")
    private final Document c8DraftDocument;
    @JsonProperty("c8WelshDraftDocument")
    private final Document c8WelshDraftDocument;

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
    @JsonIgnore
    private final String fl401FamilymanCaseNumber; //field is no longer in use

    /**
     * Manage Documents.
     */
    private final DocumentCategoryEnum documentCategoryChecklist;
    private final List<Element<FurtherEvidence>> furtherEvidences;
    @JsonProperty("giveDetails")
    private final String giveDetails;

    private final List<Element<Correspondence>> correspondence;
    private final List<Element<OtherDocuments>> otherDocuments;

    private final List<Element<FurtherEvidence>> mainAppDocForTabDisplay;
    private final List<Element<Correspondence>> correspondenceForTabDisplay;
    private final List<Element<OtherDocuments>> otherDocumentsForTabDisplay;

    private List<Element<UserInfo>> userInfo;

    /**
     * Return Application.
     */
    private final List<RejectReasonEnum> rejectReason;
    private final List<FL401RejectReasonEnum> fl401RejectReason;
    private final String returnMessage;

    @JsonProperty("applicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;

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
    private final DynamicList courtList;
    private final CaseManagementLocation caseManagementLocation;

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
    private List<TransferToAnotherCourtReasonEnum> reasonForTransferToAnotherCourt;
    private List<TransferToAnotherCourtReasonDaEnum> reasonForTransferToAnotherCourtDa;
    private List<CantFindCourtEnum> cantFindCourtCheck;
    private final String anotherCourt;
    private final String transferredCourtFrom;
    private String anotherReasonToTransferDetails;

    /**
     * Final document. (C100)
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
    private final List<Element<ApplicantConfidentialityDetails>> respondentConfidentialDetails;
    private final List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails;

    private final Map<String, Object> typeOfApplicationTable;

    /**
     * Withdraw Application.
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
     * FL401 Statement Of truth and submit.
     */
    @JsonProperty("fl401StmtOfTruth")
    private StatementOfTruth fl401StmtOfTruth;

    @JsonProperty("viewPDFlinkLabelText")
    private String viewPdfLinkLabelText;

    private List<Element<CaseInvite>> caseInvites;


    /**
     * FL401 submit status flags.
     */
    private String isCourtEmailFound;
    private String isDocumentGenerated;
    private String isNotificationSent;


    private ChildArrangementOrdersEnum childArrangementOrders;
    private DomesticAbuseOrdersEnum domesticAbuseOrders;
    private FcOrdersEnum fcOrders;
    private OtherOrdersOptionEnum otherOrdersOption;
    private String nameOfOrder;

    /**
     * Manage Orders.
     */

    private final List<Element<OrderDetails>> orderCollection;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate approvalDate;
    private Document uploadOrderDoc;
    private Document previewOrderDoc;
    private Document previewOrderDocWelsh;

    private final ManageOrdersOptionsEnum manageOrdersOptions;
    private final CreateSelectOrderOptionsEnum createSelectOrderOptions;
    private final List<OrderRecipientsEnum> orderRecipients;
    @JsonProperty("selectTypeOfOrder")
    private final SelectTypeOfOrderEnum selectTypeOfOrder;

    @JsonProperty("doesOrderClosesCase")
    private final YesOrNo doesOrderClosesCase;
    @JsonProperty("wasTheOrderApprovedAtHearing")
    private final YesOrNo wasTheOrderApprovedAtHearing;
    @JsonProperty("judgeOrMagistratesLastName")
    private final String judgeOrMagistratesLastName;
    @JsonProperty("justiceLegalAdviserFullName")
    private final String justiceLegalAdviserFullName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade;

    @JsonProperty("childrenList")
    private final String childrenList;
    @JsonProperty("magistrateLastName")
    private final List<Element<MagistrateLastName>> magistrateLastName;

    private List<Element<AppointedGuardianFullName>> appointedGuardianName;

    @JsonUnwrapped
    private final ManageOrders manageOrders;

    @JsonProperty("childrenListForDocmosis")
    private List<Element<Child>> childrenListForDocmosis;

    @JsonProperty("applicantChildDetailsForDocmosis")
    private List<Element<ApplicantChild>> applicantChildDetailsForDocmosis;

    @JsonUnwrapped
    private final StandardDirectionOrder standardDirectionOrder;

    @JsonUnwrapped
    private final DirectionOnIssue directionOnIssue;

    @JsonUnwrapped
    private final ServiceOfApplicationUploadDocs serviceOfApplicationUploadDocs;


    /**
     * Solicitor Details.
     */
    private String caseSolicitorName;
    private String caseSolicitorOrgName;
    private String selectedOrder;
    private String selectedC21Order;

    /**
     * FL401 Court details for Pilot.
     */
    @JsonProperty("submitCountyCourtSelection")
    private final DynamicList submitCountyCourtSelection;

    public CaseData setDateSubmittedDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        return this.toBuilder()
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .build();
    }

    public CaseData setIssueDate() {
        this.toBuilder()
            .issueDate(LocalDate.now())
            .build();

        return this;
    }


    /**
     * Withdraw request flag.
     */
    private String isWithdrawRequestSent;

    /**
     * Courtnav uploaded files.
     */

    @JsonProperty("courtNavUploadedDocs")
    private final List<Element<UploadedDocuments>> courtNavUploadedDocs;
    private YesOrNo isCourtNavCase;

    /**
     * Service Of Application.
     */
    private DynamicMultiSelectList serviceOfApplicationScreen1;

    @JsonProperty("citizenUploadedDocumentList")
    private final List<Element<UploadedDocuments>> citizenUploadedDocumentList;

    @JsonProperty("citizenResponseC7DocumentList")
    private final List<Element<ResponseDocuments>> citizenResponseC7DocumentList;

    @JsonProperty("scannedDocuments")
    private List<Element<ScannedDocument>> scannedDocuments;

    /**
     * Courtnav.
     */
    @JsonProperty("applicantAge")
    private final ApplicantAge applicantAge;
    private final String specialCourtName;
    private YesOrNo courtNavApproved;
    private YesOrNo hasDraftOrder;
    private String caseOrigin;
    private String numberOfAttachments;

    private String previewDraftAnOrder;

    private String citizenUploadedStatement;
    @JsonProperty("paymentReferenceNumber")
    private final String paymentReferenceNumber;

    /**
     * Respondent Solicitor.
     */
    @JsonUnwrapped
    private final RespondentSolicitorData respondentSolicitorData;

    @JsonProperty("cafcassUploadedDocs")
    private final List<Element<UploadedDocuments>> cafcassUploadedDocs;

    // C100 Rebuild
    @JsonUnwrapped
    private final C100RebuildData c100RebuildData;

    private final List<Element<DraftOrder>> draftOrderCollection;
    private Object draftOrdersDynamicList;

    @JsonUnwrapped
    private final NoticeOfChangeAnswersData noticeOfChangeAnswersData = NoticeOfChangeAnswersData.builder().build();
    @JsonProperty("bundleInformation")
    private BundlingInformation bundleInformation;

    private String judgeDirectionsToAdmin;
    private YesOrNo doYouWantToEditTheOrder;
    private String courtAdminNotes;

    @JsonUnwrapped
    private final ServeOrderData serveOrderData;

    private final List<CaseLinksElement<CaseLink>> caseLinks;

    private Flags caseFlags;

    @JsonUnwrapped
    @Builder.Default
    private UploadAdditionalApplicationData uploadAdditionalApplicationData;
    private final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle;
    private final DraftOrderOptionsEnum draftOrderOptions;

    private final List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers;

    //Added for c100 rebuild
    private Long noOfDaysRemainingToSubmitCase;

    private final DynamicList legalAdviserList;

    private AllocatedJudge allocatedJudge;
    @JsonProperty("gatekeepingDetails")
    private GatekeepingDetails gatekeepingDetails;

    @JsonUnwrapped
    private final ListWithoutNoticeDetails listWithoutNoticeDetails;
    @JsonUnwrapped
    private final Fl401ListOnNotice fl401ListOnNotice;

    private NextHearingDetails nextHearingDetails;

    private final YesOrNo isAddCaseNumberAdded;

    private final ChangeOrganisationRequest changeOrganisationRequestField;

    @JsonUnwrapped
    private final ServiceOfApplication serviceOfApplication;

    @JsonProperty("finalServedApplicationDetailsList")
    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
    private DynamicMultiSelectList solStopRepChooseParties;

    private DynamicMultiSelectList removeLegalRepAndPartiesList;

    private String courtCodeFromFact;

    private String tsPaymentServiceRequestReferenceNumber;
    private String tsPaymentStatus;
    private YesOrNo hwfRequestedForAdditionalApplications;

    private List<Element<RespondentDocs>> respondentDocsList;

    @JsonUnwrapped
    private CitizenResponseDocuments citizenResponseDocuments;

    @JsonUnwrapped
    private RespondentC8Document respondentC8Document;

    @JsonUnwrapped
    private RespondentC8 respondentC8;
    //PRL-3454 - send and reply message enhancements
    @JsonUnwrapped
    private SendOrReplyMessage sendOrReplyMessage;

    @JsonUnwrapped
    private DocumentManagementDetails documentManagementDetails;

    /**
     * Review documents.
     */
    @JsonUnwrapped
    private ReviewDocuments reviewDocuments;

    @JsonUnwrapped
    private StatementOfService statementOfService;

    @JsonUnwrapped
    private final AllPartyFlags allPartyFlags;
    /**
     * PRL-4260,4335,4301 - manage orders hearing screen fields show params.
     */
    @JsonUnwrapped
    public OrdersHearingPageFieldShowParams ordersHearingPageFieldShowParams;


    private List<Element<NotificationDetails>> fm5ReminderNotifications;
    private YesOrNo fm5RemindersSentAlready;

}
