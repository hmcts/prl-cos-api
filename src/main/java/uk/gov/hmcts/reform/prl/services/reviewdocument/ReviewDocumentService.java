package uk.gov.hmcts.reform.prl.services.reviewdocument;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP13_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP14_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP15_HINT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.C7_NOTIFICATION_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    public static final String LEGAL_PROF_QUARANTINE_DOCS_LIST = "legalProfQuarantineDocsList";
    public static final String CAFCASS_QUARANTINE_DOCS_LIST = "cafcassQuarantineDocsList";
    public static final String COURT_STAFF_QUARANTINE_DOCS_LIST = "courtStaffQuarantineDocsList";
    public static final String CITIZEN_QUARANTINE_DOCS_LIST = "citizenQuarantineDocsList";
    public static final String RESPONDENT_WELSH = "Mae’r atebydd";
    public static final String RESPONDENT_NAME = "respondentName";
    public static final String ID = "id";
    public static final String APPLICANT_ADDRESS = "applicantAddress";
    public static final String APPLICANT_NAME = "applicantName";
    public static final String DATE = "date";
    public static final String DAT_FORMAT = "dd MMM yyyy";
    public static final String RESPONDENT = "The respondent";

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    public static final String COURTNAV_QUARANTINE_DOCUMENT_LIST = "courtNavQuarantineDocumentList";

    private final AllTabServiceImpl allTabService;
    private final SystemUserService systemUserService;
    private final ManageDocumentsService manageDocumentsService;
    private final DocumentLanguageService documentLanguageService;
    private final SendgridService sendgridService;

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff and the judiciary. "
        + "You can view it in case file view and the confidential details tab.";
    private static final String REVIEW_NO = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + " This document is visible to all parties and can be viewed in the case documents tab.";
    private static final String REVIEW_NOT_SURE = "### You need to confirm if the uploaded document needs to be restricted"
        + System.lineSeparator()
        + "If you are not sure, you can use %s to get further information about whether "
        + "the document needs to be restricted.";
    public static final String LABEL_WITH_HINT =
        "<h3 class='govuk-heading-s'>Document</h3><label class='govuk-label' for='more-detail'>"
            + "<div id='more-detail-hint' class='govuk-hint'>The document will open in a new tab.</div></label>";
    public static final String GOVUK_LIST_BULLET_LABEL = "<ul class='govuk-list govuk-list--bullet'><li>%s</li></ul></label>";
    public static final String SUBMITTED_BY_LABEL =
        "<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String DOCUMENT_CATEGORY_LABEL =
        "<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String DOCUMENT_COMMENTS_LABEL =
        "<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'>"
            + "<ul class='govuk-list govuk-list--bullet'> <li>%s</li></ul></label>";
    public static final String CONFIDENTIAL_INFO_LABEL =
        "<h3 class='govuk-heading-s'>Confidential information included</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String RESTRICTED_INFO_LABEL =
        "<h3 class='govuk-heading-s'>Request to restrict access</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String RESTRICTION_REASON_LABEL =
        "<h3 class='govuk-heading-s'>Reasons to restrict access</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String BULK_SCAN_TYPE_LABEL =
        "<h3 class='govuk-heading-s'>Type</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String BULK_SCAN_SUB_TYPE_LABEL =
        "<h3 class='govuk-heading-s'>Sub type</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String BULK_SCAN_EXCEPTION_RECORD_REF_LABEL =
        "<h3 class='govuk-heading-s'>Exception record reference</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String BULK_SCAN_SCANNED_DATE_LABEL =
        "<h3 class='govuk-heading-s'>Scanned date</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String BULK_SCAN_DELIVERY_DATE_LABEL =
        "<h3 class='govuk-heading-s'>Delivery date</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String DOC_TO_BE_REVIEWED = "docToBeReviewed";
    public static final String DOC_LABEL = "docLabel";
    public static final String REVIEW_DOC = "reviewDoc";
    public static final String CASE_DETAILS_URL = "/cases/case-details/";
    public static final String SEND_AND_REPLY_URL = "/trigger/sendOrReplyToMessages/sendOrReplyToMessages1";
    public static final String SEND_AND_REPLY_MESSAGE_LABEL = "\">Send and reply to messages</a>";

    private static final String LETTER_TYPE = "responsePack";

    public static final String ENG = "eng";
    public static final String WEL = "wel";
    public static final String IS_WELSH = "isWelsh";
    public static final String IS_ENGLISH = "isEnglish";
    private final EmailService emailService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final BulkPrintService bulkPrintService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;

    public List<DynamicListElement> fetchDocumentDynamicListElements(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        //solcitor
        if (isNotEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList());
        }
        //Cafcass
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList());
        }
        //court staff
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList());
        }
        //citizen
        if (CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList());
        }
        //bulkscan
        if (isNotEmpty(caseData.getScannedDocuments())) {
            dynamicListElements.addAll(caseData.getScannedDocuments().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getFileName()
                                                          + HYPHEN_SEPARATOR
                                                          + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getScannedDate().toLocalDate()
                                               ))
                                               .build()).toList());
            tempQuarantineDocumentList.addAll(convertScannedDocumentsToQuarantineDocList(caseData.getScannedDocuments()));
        }
        //Courtnav uploaded docs
        if (CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList())) {
            log.info("inside prepare for courtnav uploaded docs");
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList());
            log.info("dynamicListElements " + dynamicListElements);
            log.info("tempQuarantineDocumentList " + tempQuarantineDocumentList);
            log.info("exit prepare for courtnav uploaded docs");
        }
        caseDataUpdated.put("tempQuarantineDocumentList", tempQuarantineDocumentList);
        return dynamicListElements;
    }

    public void getReviewedDocumentDetailsNew(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList()
            && null != caseData.getReviewDocuments().getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
            List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = caseData.getDocumentManagementDetails().getTempQuarantineDocumentList();

            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement =
                getQuarantineDocumentById(tempQuarantineDocumentList, uuid);
            quarantineLegalDocElement = resetUploaderRoleForCourtNavUploadedDocs(quarantineLegalDocElement);

            quarantineLegalDocElement.ifPresent(legalDocElement -> updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                legalDocElement,
                legalDocElement.getValue().getUploaderRole()
            ));
        }
    }

    private static Optional<Element<QuarantineLegalDoc>> resetUploaderRoleForCourtNavUploadedDocs(Optional<Element<QuarantineLegalDoc>>
                                                                                                      quarantineLegalDocElement) {
        if (quarantineLegalDocElement.isPresent() && COURTNAV.equals(quarantineLegalDocElement.get().getValue().getUploadedBy())) {
            quarantineLegalDocElement = Optional.of(element(
                quarantineLegalDocElement.get().getId(),
                quarantineLegalDocElement.get().getValue().toBuilder().uploaderRole(
                    COURTNAV).build()
            ));
        }
        return quarantineLegalDocElement;
    }

    private void updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(Map<String, Object> caseDataUpdated,
                                                                      Element<QuarantineLegalDoc> quarantineDocElement,
                                                                      String submittedBy) {
        log.info("submittedBy " + submittedBy);
        QuarantineLegalDoc quarantineLegalDoc = quarantineDocElement.getValue();

        String docTobeReviewed = formatDocumentTobeReviewed(submittedBy, quarantineLegalDoc);

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
        caseDataUpdated.put(DOC_LABEL, LABEL_WITH_HINT);

        Document documentTobeReviewed = manageDocumentsService.getQuarantineDocumentForUploader(
            submittedBy, quarantineLegalDoc);
        caseDataUpdated.put(REVIEW_DOC, documentTobeReviewed);
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        boolean isDocumentFound = false;
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())
            || YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            //solicitor uploaded docs
            if (null != caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData,
                                                        caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                                                        uuid,
                                                        UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).build(),
                                                        SOLICITOR,
                                                        LEGAL_PROF_QUARANTINE_DOCS_LIST
                );

            }
            //cafcass uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                                                        uuid, UserDetails.builder().roles(List.of(CAFCASS)).build(),
                                                        CAFCASS, CAFCASS_QUARANTINE_DOCS_LIST
                );

            }
            //court staff uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData,
                                                        caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                                                        uuid,
                                                        UserDetails.builder().roles(List.of(Roles.COURT_ADMIN.getValue())).build(),
                                                        COURT_STAFF,
                                                        COURT_STAFF_QUARANTINE_DOCS_LIST
                );

            }
            //citizen uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData,
                                                        caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList(),
                                                        uuid,
                                                        UserDetails.builder().roles(List.of(Roles.CITIZEN.getValue())).build(),
                                                        CITIZEN,
                                                        CITIZEN_QUARANTINE_DOCS_LIST
                );

            }
            //courtnav uploaded docs
            isDocumentFound = processCourtNavDocument(caseDataUpdated, caseData, uuid, isDocumentFound);
            //Bulk scan
            processBulkScanDocument(caseDataUpdated, caseData, uuid, isDocumentFound);
        }
    }

    private boolean processReviewDocument(CaseData caseData,
                                          Map<String, Object> caseDataUpdated,
                                          List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                          UUID uuid,
                                          UserDetails userDetails,
                                          String userRole,
                                          String quarantineDocsListToBeModified) {
        boolean isDocumentFound = false;
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional = getQuarantineDocumentById(
            quarantineDocsList,
            uuid
        );
        if (quarantineLegalDocElementOptional.isPresent()) {
            isDocumentFound = true;
            processDocumentsAfterReviewNew(
                caseData,
                caseDataUpdated,
                quarantineLegalDocElementOptional.get(),
                userDetails,
                userRole
            );

            sendNotifications(caseData, quarantineLegalDocElementOptional.get(),
                              quarantineDocsListToBeModified);
            //remove document from quarantine
            quarantineDocsList.remove(quarantineLegalDocElementOptional.get());
            caseDataUpdated.put(quarantineDocsListToBeModified, quarantineDocsList);
        }
        return isDocumentFound;
    }

    private boolean processCourtNavDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid, boolean isDocumentFound) {
        if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList()) {
            isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                    caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList(),
                                                    uuid, UserDetails.builder().roles(List.of(Roles.COURTNAV.getValue())).build(),
                                                    COURTNAV, COURTNAV_QUARANTINE_DOCUMENT_LIST);

        }
        return isDocumentFound;

    }

    private void sendNotifications(CaseData caseData, Element<QuarantineLegalDoc> quarantineLegalDocElementOptional,
                                   String quarantineDocsListToBeModified) {
        //PRL-5846 - notifications to cafacass cymru
        sendNotificationToCafCass(caseData,quarantineLegalDocElementOptional,quarantineDocsListToBeModified);

        //Epic-PRL-5842 - notifications to applicants lip or solicitors
        sendNotificationToApplicantsLipOrSolicitor(caseData,
                                                   quarantineLegalDocElementOptional,
                                                   quarantineDocsListToBeModified);
    }

    private void sendNotificationToCafCass(CaseData caseData, Element<QuarantineLegalDoc> quarantineLegalDocElementOptional,
                                           String quarantineDocsListToBeModified) {
        String cafcassEmail = null;
        String respondantName = null;

        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())
            && Optional.ofNullable(caseData.getServiceOfApplication()).isPresent()) {
            cafcassEmail = caseData.getServiceOfApplication().getSoaCafcassCymruEmail();
            respondantName = getNameOfRespondent(
                quarantineLegalDocElementOptional,
                quarantineDocsListToBeModified
            );

            if (Optional.ofNullable(cafcassEmail).isPresent()
                && Optional.ofNullable(respondantName).isPresent()) {
                if (quarantineLegalDocElementOptional.getValue().getCategoryId().equalsIgnoreCase(RESPONDENT_APPLICATION)) {
                    sendEmailToCafcassCymru(caseData, cafcassEmail,
                                            respondantName,
                                            EmailTemplateNames.RESPONDENT_RESPONDED_CAFCASS
                    );
                }
                if (quarantineLegalDocElementOptional.getValue().getCategoryId().equalsIgnoreCase(
                    RESPONDENT_C1A_APPLICATION)) {
                    sendEmailToCafcassCymru(caseData, cafcassEmail,
                                            respondantName,
                                            EmailTemplateNames.RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS
                    );

                }
                if (quarantineLegalDocElementOptional.getValue().getCategoryId().equalsIgnoreCase(
                    RESPONDENT_C1A_RESPONSE)) {
                    sendEmailToCafcassCymru(caseData, cafcassEmail,
                                            respondantName,
                                            EmailTemplateNames.RESPONDENT_RESPONDED_ALLEGATIONS_OF_HARM_CAFCASS
                    );
                }
            }
        }
    }

    private void sendEmailToCafcassCymru(CaseData caseData,
                                         String cafcassCymruEmailId,
                                         String respondentName,
                                         EmailTemplateNames template) {
        String dashboardUrl = manageCaseUrl + "/" + caseData.getId() + "#Case%20documents";
        emailService.send(
            cafcassCymruEmailId,
            template,
            buildEmailData(caseData, null, respondentName, dashboardUrl),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
    }

    private void sendNotificationToApplicantsLipOrSolicitor(CaseData caseData,
                                                            Element<QuarantineLegalDoc> quarantineLegalDocElement,
                                                            String quarantineDocsListToBeModified) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo()))) {
            String respondentName = getNameOfRespondent(quarantineLegalDocElement, quarantineDocsListToBeModified);
            Document responseDocument = manageDocumentsService.getQuarantineDocumentForUploader(
                quarantineLegalDocElement.getValue().getUploaderRole(), quarantineLegalDocElement.getValue());

            if (RESPONDENT_APPLICATION.equalsIgnoreCase(quarantineLegalDocElement.getValue().getCategoryId())) {
                log.info("*** Sending respondent C7 response documents to applicants ***");
                //C7 response
                sendNotificationToApplicants(caseData,
                                             C7_NOTIFICATION_APPLICANT,
                                             SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
                                             AP13_HINT,
                                             null,
                                             respondentName,
                                             responseDocument);
            } else if (RESPONDENT_C1A_APPLICATION.equalsIgnoreCase(quarantineLegalDocElement.getValue().getCategoryId())) {
                log.info("*** Sending respondent C1A documents to applicants/solicitor ***");
                //C1A
                sendNotificationToApplicants(caseData,
                                             C1A_NOTIFICATION_APPLICANT,
                                             SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
                                             AP14_HINT,
                                             C1A_NOTIFICATION_APPLICANT_SOLICITOR,
                                             respondentName,
                                             responseDocument);
            } else if (RESPONDENT_C1A_RESPONSE.equalsIgnoreCase(quarantineLegalDocElement.getValue().getCategoryId())) {
                log.info("*** Sending respondent response to C1A documents to applicants/solicitor ***");
                //C1A response
                sendNotificationToApplicants(caseData,
                                             C1A_RESPONSE_NOTIFICATION_APPLICANT,
                                             SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT,
                                             AP15_HINT,
                                             C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR,
                                             respondentName,
                                             responseDocument);
            }
        }
    }

    private void sendNotificationToApplicants(CaseData caseData,
                                              EmailTemplateNames partyGovNotifyTemplate,
                                              SendgridEmailTemplateNames partySendgridTemplate,
                                              String coverLetterTemplateHint,
                                              SendgridEmailTemplateNames solicitorSendgridTemplate,
                                              String respondentName,
                                              Document responseDocument) {
        caseData.getApplicants().forEach(partyDataEle -> {
            PartyDetails partyData = partyDataEle.getValue();
            Map<String, Object> dynamicData = getEmailDynamicData(caseData, partyData, respondentName);
            if (CommonUtils.isNotEmpty(partyData.getSolicitorEmail())
                && null != solicitorSendgridTemplate) {
                sendEmailViaSendGrid(systemUserService.getSysUserToken(),
                                     responseDocument,
                                     dynamicData,
                                     partyData.getSolicitorEmail(),
                                     solicitorSendgridTemplate);
            } else {

                if (CommonUtils.isNotEmpty(partyData.getEmail())
                    && ContactPreferences.email.equals(partyData.getContactPreferences())) {
                    if (hasDashboardAccess(element(partyData))) {
                        sendEmailToParty(caseData,
                                         partyData,
                                         respondentName,
                                         partyGovNotifyTemplate);
                    } else {
                        sendEmailViaSendGrid(systemUserService.getSysUserToken(),
                                             responseDocument,
                                             dynamicData,
                                             partyData.getEmail(),
                                             partySendgridTemplate);
                    }
                } else {
                    //Bulk print
                    generateAndSendPostNotification(caseData,
                                                    partyDataEle,
                                                    respondentName,
                                                    responseDocument,
                                                    coverLetterTemplateHint);
                }
            }
        });
    }

    private void generateAndSendPostNotification(CaseData caseData,
                                                 Element<PartyDetails> applicant,
                                                 String respondentName,
                                                 Document responseDocument,
                                                 String coverLetterTemplateHint) {
        if (ObjectUtils.isNotEmpty(applicant.getValue())
            && ObjectUtils.isNotEmpty(applicant.getValue().getAddress())
            && ObjectUtils.isNotEmpty(applicant.getValue().getAddress().getAddressLine1())) {
            try {
                //cover letters
                Map<String, Object> dataMap = fetchApplicantResponseDataMap(caseData,
                                                                            applicant.getValue().getAddress(),
                                                                            applicant.getValue().getLabelForDynamicList(),
                                                                            respondentName);
                List<Document> responseDocuments = new ArrayList<>(serviceOfApplicationService
                                                                       .getCoverLetters(
                                                                           systemUserService.getSysUserToken(),
                                                                           caseData,
                                                                           coverLetterTemplateHint,
                                                                           dataMap
                                                                       ));
                //response document
                responseDocuments.add(responseDocument);

                // Add coversheet and send it to bulk print
                UUID bulkPrintId = sendResponseDocumentViaPost(
                    caseData,
                    applicant.getValue().getAddress(),
                    applicant.getValue().getLabelForDynamicList(),
                    systemUserService.getSysUserToken(),
                    responseDocuments
                );
                log.info("Response documents are sent to applicant {} in the case{} - via post {}", applicant.getId(), caseData.getId(), bulkPrintId);
            } catch (Exception e) {
                log.error("Failed to send response documents to applicant {} in the case {}", applicant.getId(), caseData.getId(), e);
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Couldn't post response documents - address is null/empty for applicant {} in the case {}", applicant.getId(), caseData.getId());
        }
    }

    private Map<String, Object> getEmailDynamicData(CaseData caseData,
                                                    PartyDetails applicant,
                                                    String respondentName) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("applicantName", applicant.getLabelForDynamicList());
        dynamicData.put("solicitorName", applicant.getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());
        dynamicData.put("respondentName",  null == respondentName ? (documentLanguage.isGenWelsh()
            ? RESPONDENT_WELSH : RESPONDENT) : respondentName);
        return dynamicData;
    }

    private void sendEmailToParty(CaseData caseData,
                                  PartyDetails partyData,
                                  String respondentName,
                                  EmailTemplateNames emailTemplate) {
        EmailTemplateVars emailData = buildEmailData(caseData,
                                                        partyData.getLabelForDynamicList(),
                                                        respondentName,
                                                        citizenDashboardUrl);
        emailService.send(
            partyData.getEmail(),
            emailTemplate,
            emailData,
            LanguagePreference.getPreferenceLanguage(caseData)
        );
    }

    private EmailTemplateVars buildEmailData(CaseData caseData,
                                             String applicantName,
                                             String respondentName,
                                             String link) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantName)
            .respondentName(null == respondentName ? (documentLanguageService.docGenerateLang(caseData).isGenWelsh()
                ? RESPONDENT_WELSH : RESPONDENT) : respondentName)
            .caseLink(link)
            .build();
    }

    private void sendEmailViaSendGrid(String authorisation,
                                      Document responseDocument,
                                      Map<String, Object> dynamicDataForEmail,
                                      String emailAddress,
                                      SendgridEmailTemplateNames sendgridEmailTemplateName) {
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(
                sendgridEmailTemplateName,
                authorisation,
                SendgridEmailConfig.builder()
                    .toEmailAddress(emailAddress)
                    .dynamicTemplateData(dynamicDataForEmail)
                    .listOfAttachments(List.of(responseDocument))
                    .languagePreference(LanguagePreference.english)
                    .build()
            );
        } catch (IOException e) {
            log.error("There is a failure in sending email to {} with exception {}", emailAddress, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private  String getNameOfRespondent(Element<QuarantineLegalDoc> quarantineLegalDocElementOptional, String quarantineDocsListToBeModified) {
        if (LEGAL_PROF_QUARANTINE_DOCS_LIST.equalsIgnoreCase(quarantineDocsListToBeModified)) {
            return quarantineLegalDocElementOptional.getValue().getSolicitorRepresentedPartyName();
        } else if (CITIZEN_QUARANTINE_DOCS_LIST.equalsIgnoreCase(quarantineDocsListToBeModified)) {
            return quarantineLegalDocElementOptional.getValue().getUploadedBy();
        }
        return null;
    }

    private UUID sendResponseDocumentViaPost(CaseData caseData,
                                             Address address,
                                             String name,
                                             String authorisation,
                                             List<Document> responseDocuments) throws Exception {
        List<Document> documents = new ArrayList<>();
        //generate cover sheet
        List<Document> coverSheets = serviceOfApplicationPostService.getCoverSheets(
            caseData,
            authorisation,
            address,
            name,
            DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
        );
        if (CollectionUtils.isNotEmpty(coverSheets)) {
            documents.addAll(coverSheets);
        }

        //cover should be the first doc in the list, append all order docs
        documents.addAll(responseDocuments);

        return bulkPrintService.send(
            String.valueOf(caseData.getId()),
            authorisation,
            LETTER_TYPE,
            documents,
            name
        );
    }

    private Map<String, Object> fetchApplicantResponseDataMap(CaseData caseData,
                                                              Address address,
                                                              String applicantName,
                                                              String respondentName) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(APPLICANT_NAME, null != applicantName ? applicantName : " ");
        dataMap.put(APPLICANT_ADDRESS, address);
        dataMap.put(ID, String.valueOf(caseData.getId()));
        dataMap.put(RESPONDENT_NAME,
                    null == respondentName ? (documentLanguageService.docGenerateLang(caseData).isGenWelsh()
                        ? RESPONDENT_WELSH : RESPONDENT) : respondentName
        );
        dataMap.put(DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DAT_FORMAT)));
        return dataMap;
    }

    private void processDocumentsAfterReviewNew(CaseData caseData,
                                                Map<String, Object> caseDataUpdated,
                                                Element<QuarantineLegalDoc> quarantineLegalDocElement,
                                                UserDetails userDetails,
                                                String userRole) {
        QuarantineLegalDoc tempQuarantineDoe = quarantineLegalDocElement.getValue();
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            tempQuarantineDoe = tempQuarantineDoe.toBuilder()
                .isConfidential(null)
                .isRestricted(null)
                .restrictedDetails(null)
                .build();
        }
        manageDocumentsService.moveDocumentsToRespectiveCategoriesNew(
            tempQuarantineDoe,
            userDetails,
            caseData,
            caseDataUpdated,
            userRole
        );
    }

    private void processBulkScanDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid, boolean isDocumentFound) {
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional;
        if (!isDocumentFound && isNotEmpty(caseData.getScannedDocuments())) {
            quarantineLegalDocElementOptional = getQuarantineBulkScanDocElement(caseData, uuid);
            if (quarantineLegalDocElementOptional.isPresent()) {
                processDocumentsAfterReviewNew(
                    caseData,
                    caseDataUpdated,
                    quarantineLegalDocElementOptional.get(),
                    UserDetails.builder().roles(List.of(Roles.BULK_SCAN.getValue())).build(),
                    BULK_SCAN
                );
                removeFromScannedDocumentListAfterReview(caseDataUpdated, caseData, uuid);
            }
        }
    }

    private void removeFromScannedDocumentListAfterReview(Map<String, Object> caseDataUpdated,
                                                          CaseData caseData, UUID uuid) {
        log.info("UUID is {}", uuid);
        Optional<Element<ScannedDocument>> scannedDocumentElement = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
        if (scannedDocumentElement.isPresent()) {
            log.info("removing document from scanned docs");
            caseData.getScannedDocuments().remove(scannedDocumentElement.get());
            caseDataUpdated.put("scannedDocuments", caseData.getScannedDocuments());
        }
    }

    private List<Element<QuarantineLegalDoc>> convertScannedDocumentsToQuarantineDocList(
        List<Element<ScannedDocument>> scannedDocumentElements) {
        return scannedDocumentElements.stream().map(
            scannedDocumentElement -> {
                ScannedDocument scannedDocument = scannedDocumentElement.getValue();
                return element(
                    scannedDocumentElement.getId(),
                    getBulkScanQuarantineDoc(scannedDocument)
                        .build()
                );
            }).toList();
    }

    private static Optional<Element<QuarantineLegalDoc>> getQuarantineBulkScanDocElement(CaseData caseData, UUID uuid) {

        ScannedDocument scannedDocument = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid))
            .findFirst()
            .map(Element::getValue)
            .orElse(null);

        if (null != scannedDocument) {
            return Optional.of(element(uuid, getBulkScanQuarantineDoc(scannedDocument).build()));
        }
        return Optional.empty();
    }

    private static QuarantineLegalDoc.QuarantineLegalDocBuilder getBulkScanQuarantineDoc(ScannedDocument scannedDocument) {
        return QuarantineLegalDoc.builder()
            .fileName(scannedDocument.getFileName())
            .controlNumber(scannedDocument.getControlNumber())
            .type(scannedDocument.getType())
            .subtype(scannedDocument.getSubtype())
            .exceptionRecordReference(scannedDocument.getExceptionRecordReference())
            .url(scannedDocument.getUrl())
            .scannedDate(scannedDocument.getScannedDate())
            .deliveryDate(scannedDocument.getDeliveryDate())
            .documentParty(BULK_SCAN)
            .uploadedBy(BULK_SCAN)
            .documentUploadedDate(scannedDocument.getScannedDate())
            .isConfidential(YesOrNo.Yes) //bulk scan docs always go to confidential if decision is Yes
            .isRestricted(YesOrNo.No) //fix to getRestrictedOrConfidentialKey=confidential
            .uploaderRole(BULK_SCAN);
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())
            && (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()))
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList())
            && CollectionUtils.isEmpty(caseData.getScannedDocuments())) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(
                    caseData.getId()),
                C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                    ? CaseEvent.C100_ALL_DOCS_REVIEWED.getValue() : CaseEvent.FL401_ALL_DOCS_REVIEWED.getValue()
            );
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(caseData.getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataUpdated
            );

        }
        if (YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_YES).build());
        } else if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_NO).build());
        } else {
            String sendReplyLink = "<a href=\"" + CASE_DETAILS_URL
                + caseData.getId()
                + SEND_AND_REPLY_URL
                + SEND_AND_REPLY_MESSAGE_LABEL;
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_IN_REVIEW)
                                         .confirmationBody(String.format(REVIEW_NOT_SURE, sendReplyLink))
                                         .build());
        }
    }

    private Optional<Element<QuarantineLegalDoc>> getQuarantineDocumentById(
        List<Element<QuarantineLegalDoc>> quarantineDocsList, UUID uuid) {
        return quarantineDocsList.stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
    }

    private String formatDocumentTobeReviewed(String submittedBy,
                                              QuarantineLegalDoc quarantineDoc) {
        StringBuilder reviewDetailsBuilder = new StringBuilder();
        reviewDetailsBuilder.append(format(SUBMITTED_BY_LABEL, submittedBy));
        //append quarantine document details for solicitor, cafcass & court staff
        appendQuarantineDocumentDetails(reviewDetailsBuilder, quarantineDoc, submittedBy);

        //PRL-5006 bulk scan fields
        if (BULK_SCAN.equals(submittedBy)) {
            appendBulkScanDocumentDetails(reviewDetailsBuilder, quarantineDoc);
        }
        reviewDetailsBuilder.append("<br/>");
        return reviewDetailsBuilder.toString();
    }

    private void appendQuarantineDocumentDetails(StringBuilder reviewDetailsBuilder,
                                                 QuarantineLegalDoc quarantineDoc,
                                                 String submittedBy) {
        if (CommonUtils.isNotEmpty(quarantineDoc.getCategoryName())) {
            reviewDetailsBuilder.append(format(
                DOCUMENT_CATEGORY_LABEL,
                quarantineDoc.getCategoryName()
            ));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getNotes())) {
            reviewDetailsBuilder.append(format(
                DOCUMENT_COMMENTS_LABEL,
                quarantineDoc.getNotes()
            ));
        }
        if (null != quarantineDoc.getIsConfidential() && !BULK_SCAN.equals(submittedBy)) {
            reviewDetailsBuilder.append(format(
                CONFIDENTIAL_INFO_LABEL,
                quarantineDoc.getIsConfidential().getDisplayedValue()
            ));
        }
        if (null != quarantineDoc.getIsRestricted() && !BULK_SCAN.equals(submittedBy)) {
            reviewDetailsBuilder.append(format(
                RESTRICTED_INFO_LABEL,
                quarantineDoc.getIsRestricted().getDisplayedValue()
            ));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getRestrictedDetails())) {
            reviewDetailsBuilder.append(format(
                RESTRICTION_REASON_LABEL,
                quarantineDoc.getRestrictedDetails()
            ));
        }
    }

    private void appendBulkScanDocumentDetails(StringBuilder reviewDetailsBuilder,
                                               QuarantineLegalDoc quarantineDoc) {
        if (CommonUtils.isNotEmpty(quarantineDoc.getType())) {
            reviewDetailsBuilder.append(format(
                BULK_SCAN_TYPE_LABEL,
                quarantineDoc.getType()
            ));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getSubtype())) {
            reviewDetailsBuilder.append(format(
                BULK_SCAN_SUB_TYPE_LABEL,
                quarantineDoc.getSubtype()
            ));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getExceptionRecordReference())) {
            reviewDetailsBuilder.append(format(
                BULK_SCAN_EXCEPTION_RECORD_REF_LABEL,
                quarantineDoc.getExceptionRecordReference()
            ));
        }
        if (null != quarantineDoc.getScannedDate()) {
            reviewDetailsBuilder.append(format(
                BULK_SCAN_SCANNED_DATE_LABEL,
                formatDateTime(
                    DATE_TIME_PATTERN,
                    quarantineDoc.getScannedDate()
                )
            ));
        }
        if (null != quarantineDoc.getDeliveryDate()) {
            reviewDetailsBuilder.append(format(
                BULK_SCAN_DELIVERY_DATE_LABEL,
                formatDateTime(
                    DATE_TIME_PATTERN,
                    quarantineDoc.getDeliveryDate()
                )
            ));
        }
    }

}
