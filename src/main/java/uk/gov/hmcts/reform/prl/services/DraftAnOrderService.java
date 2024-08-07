package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.exception.ManageOrderRuntimeException;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.MiamAttendingPersonName;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.DioApplicationToApplyPermission;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.SdoDioProvideOtherDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.AddNewPreamble;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoDisclosureOfPapersCaseNumber;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoLanguageDialect;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoFurtherDirections;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfApplicant;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfRespondent;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AFTER_SECOND_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_NEXT_STEPS_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_EX740;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_PROHIBITION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CROSS_EXAMINATION_QUALIFIED_LEGAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_ORDER_FOR_CA_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_APPLICATION_TO_APPLY_PERMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_CASE_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PARTICIPATION_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PERMISSION_HEARING_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_POSITION_STATEMENT_DIRECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_RIGHT_TO_ASK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARDING_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_SAFEGUARING_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGS_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_NOT_NEEDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_SCREEN_ERRORS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JOINING_INSTRUCTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORUTY_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OCCUPATIONAL_SCREEN_ERRORS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARENT_WITHCARE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTICIPATION_DIRECTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RIGHT_TO_ASK_COURT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SAFE_GUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_CROSS_EXAMINATION_EX741;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_DIRECTIONS_FOR_FACT_FINDING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_DRA_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_FHDRA_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_PERMISSION_HEARING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_PERMISSION_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_SECOND_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_SETTLEMENT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SDO_URGENT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION7_EDIT_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_7_DA_OCCURED_EDIT_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SECTION_7_FACTS_EDIT_CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPECIFIED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SPIP_ATTENDANCE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SWANSEA_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_SOLICITOR_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WHO_MADE_ALLEGATIONS_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WHO_NEEDS_TO_RESPOND_ALLEGATIONS_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.Event.DRAFT_AN_ORDER;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.partyToProvideDetailsCmyru;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.partyToProvideDetailsOnly;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.safeguardingCafcassCymru;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.safeguardingCafcassOnly;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum.section7Report;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum.specifiedDocuments;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum.spipAttendance;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.factFindingHearing;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.hearingNotNeeded;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.joiningInstructions;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.nextStepsAfterGateKeeping;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.participationDirections;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.updateContactDetails;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum.localAuthorityLetter;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum.parentWithCare;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum.addNewPreamble;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum.afterSecondGateKeeping;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum.rightToAskCourt;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.updateCurrentOrderId;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getErrorForOccupationScreen;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getErrorsForOrdersProhibitedForC100FL401;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getHearingScreenValidations;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.getHearingScreenValidationsForSdo;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class DraftAnOrderService {

    private static final String ORDERS_HEARING_DETAILS = "ordersHearingDetails";
    public static final String SDO_INSTRUCTIONS_FILING_PARTIES_DYNAMIC_LIST = "sdoInstructionsFilingPartiesDynamicList";
    public static final String SDO_NEW_PARTNER_PARTIES_CAFCASS = "sdoNewPartnerPartiesCafcass";
    public static final String SDO_NEW_PARTNER_PARTIES_CAFCASS_CYMRU = "sdoNewPartnerPartiesCafcassCymru";
    private static final String DATE_ORDER_MADE = "dateOrderMade";
    private static final String SELECTED_ORDER = "selectedOrder";
    public static final String LEGAL_REP_INSTRUCTIONS_PLACE_HOLDER = "legalRepInstructionsPlaceHolder";
    public static final String DRAFT_ORDERS_DYNAMIC_LIST = "draftOrdersDynamicList";
    public static final String SDO_WHO_MADE_ALLEGATIONS_TEXT_FIELD = "sdoWhoMadeAllegationsText";
    public static final String SDO_WHO_NEEDS_TO_RESPOND_ALLEGATIONS_TEXT_FIELD = "sdoWhoNeedsToRespondAllegationsText";
    public static final String SDO_WHO_MADE_ALLEGATIONS_LIST = "sdoWhoMadeAllegationsList";
    public static final String SDO_WHO_NEEDS_TO_RESPOND_ALLEGATIONS_LIST = "sdoWhoNeedsToRespondAllegationsList";
    public static final String ORDER_UPLOADED_AS_DRAFT_FLAG = "orderUploadedAsDraftFlag";
    public static final String SDO_RIGHT_TO_ASK_COURT = "sdoRightToAskCourt";
    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private final ManageOrderService manageOrderService;
    private final DgsService dgsService;
    private final UserService userService;
    private final DocumentLanguageService documentLanguageService;
    private final LocationRefDataService locationRefDataService;
    private final PartiesListGenerator partiesListGenerator;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final HearingDataService hearingDataService;
    private final HearingService hearingService;
    private final RoleAssignmentService roleAssignmentService;

    private static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    private static final String ORDER_NAME = "orderName";
    private static final String MANAGE_ORDER_SDO_FAILURE
        = "Failed to update SDO order details";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";
    private static final String BOLD_BEGIN = "<span class='heading-h3'>";
    private static final String BOLD_END = "</span>";

    private final WelshCourtEmail welshCourtEmail;

    public List<Element<DraftOrder>> generateDraftOrderCollection(CaseData caseData, String authorisation) {
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> orderDetails = element(getCurrentOrderDetails(caseData, loggedInUserType, authorisation));
        //By default all the hearing will be option 1 (dateReservedWithListAssit) as per ticket PRL-4766
        if (DraftOrderOptionsEnum.draftAnOrder.equals(caseData.getDraftOrderOptions())
            && Yes.equals(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())) {
            defaultHearingOptionToDateReservedWithListAssist(orderDetails.getValue());
        }
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(orderDetails);
        } else {
            draftOrderList.add(orderDetails);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return draftOrderList;
    }

    private static void defaultHearingOptionToDateReservedWithListAssist(DraftOrder draftOrder) {
        draftOrder.getManageOrderHearingDetails()
            .forEach(hearingDataElement -> hearingDataElement.getValue()
                .setHearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit));
    }

    public DraftOrder getCurrentOrderDetails(CaseData caseData, String loggedInUserType, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        if (DraftOrderOptionsEnum.uploadAnOrder.equals(caseData.getDraftOrderOptions())) {
            return manageOrderService.getCurrentUploadDraftOrderDetails(caseData, loggedInUserType, userDetails);
        }
        return manageOrderService.getCurrentCreateDraftOrderDetails(caseData, loggedInUserType, userDetails);
    }

    public Map<String, Object> getDraftOrderDynamicList(CaseData caseData,
                                                        String eventId,
                                                        String authorisation) {
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                if (ObjectUtils.isNotEmpty(draftOrderElement.getValue().getOtherDetails().getIsJudgeApprovalNeeded())) {
                    filterDraftOrderForNewCases(eventId, supportedDraftOrderList, draftOrderElement, loggedInUserType);
                } else {
                    filterDraftOrderForExistingCases(eventId, supportedDraftOrderList, draftOrderElement);
                }
            }
        );
        caseDataMap.put(DRAFT_ORDERS_DYNAMIC_LIST, ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));

        String cafcassCymruEmailAddress = welshCourtEmail
            .populateCafcassCymruEmailInManageOrders(caseData);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseDataMap.put(PrlAppsConstants.CAFCASS_OR_CYMRU_NEED_TO_PROVIDE_REPORT, Yes);
            caseDataMap.put(
                "isInHearingState",
                (PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getState())
                    || DECISION_OUTCOME.equals(caseData.getState())) ? Yes : No
            );
            if (Yes.equals(caseData.getIsCafcass())) {
                caseDataMap.put(PrlAppsConstants.CAFCASS_SERVED_OPTIONS, caseData.getManageOrders().getCafcassServedOptions());
            }
        }
        if (null != cafcassCymruEmailAddress) {
            caseDataMap.put("cafcassCymruEmail", cafcassCymruEmailAddress);
        }
        return caseDataMap;
    }

    private static void filterDraftOrderForExistingCases(String eventId, List<Element<DraftOrder>> supportedDraftOrderList,
                                                         Element<DraftOrder> draftOrderElement) {
        String orderStatus = draftOrderElement.getValue().getOtherDetails().getStatus();
        if ((Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && !OrderStatusEnum.reviewedByJudge.getDisplayedValue().equals(orderStatus)
            && !OrderStatusEnum.rejectedByJudge.getDisplayedValue().equals(orderStatus))
            || (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && OrderStatusEnum.reviewedByJudge.getDisplayedValue().equals(orderStatus))) {
            supportedDraftOrderList.add(draftOrderElement);
        }
    }

    private static void filterDraftOrderForNewCases(String eventId, List<Element<DraftOrder>> supportedDraftOrderList,
                                                    Element<DraftOrder> draftOrderElement, String loggedInUserType) {
        if (isJudgeReviewRequested(loggedInUserType, eventId, draftOrderElement.getValue())
            || isManagerReviewRequested(loggedInUserType, eventId, draftOrderElement.getValue())
            || isAdminEditAndApproveOrder(loggedInUserType, eventId, draftOrderElement.getValue())) {
            supportedDraftOrderList.add(draftOrderElement);
        }
    }

    private static boolean isJudgeReviewRequested(String loggedInUserType,
                                                  String eventId,
                                                  DraftOrder draftOrder) {
        return Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && UserRoles.JUDGE.name().equals(loggedInUserType)
            && !OrderStatusEnum.rejectedByJudge.getDisplayedValue().equals(draftOrder.getOtherDetails().getStatus())
            && Yes.equals(draftOrder.getOtherDetails().getIsJudgeApprovalNeeded());
    }

    private static boolean isManagerReviewRequested(String loggedInUserType,
                                                    String eventId,
                                                    DraftOrder draftOrder) {
        return Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && !UserRoles.JUDGE.name().equals(loggedInUserType)
            && AmendOrderCheckEnum.managerCheck.equals(draftOrder.getOtherDetails().getReviewRequiredBy());
    }

    private static boolean isAdminEditAndApproveOrder(String loggedInUserType,
                                                      String eventId,
                                                      DraftOrder draftOrder) {
        return Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            && UserRoles.COURT_ADMIN.name().equals(loggedInUserType)
            && (AmendOrderCheckEnum.noCheck.equals(draftOrder.getOtherDetails().getReviewRequiredBy())
            || OrderStatusEnum.reviewedByManager.getDisplayedValue().equals(draftOrder.getOtherDetails().getStatus())
            || OrderStatusEnum.reviewedByJudge.getDisplayedValue().equals(draftOrder.getOtherDetails().getStatus())
            || OrderStatusEnum.createdByJudge.getDisplayedValue().equals(draftOrder.getOtherDetails().getStatus()));
    }

    public Map<String, Object> removeDraftOrderAndAddToFinalOrder(String authorisation, CaseData caseData, String eventId) {
        Map<String, Object> updatedCaseData = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        updatedCaseData.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (e.getId().equals(selectedOrderId)) {
                updatedCaseData.put(ORDER_UPLOADED_AS_DRAFT_FLAG, draftOrder.getIsOrderUploadedByJudgeOrAdmin());
                if (YesOrNo.Yes.equals(caseData.getDoYouWantToEditTheOrder()) || (caseData.getManageOrders() != null
                    && Yes.equals(caseData.getManageOrders().getMakeChangesToUploadedOrder()))) {
                    Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                    if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getOrdersHearingDetails())) {
                        caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                                                                               .getHearingDataForSelectedHearing(caseData,
                                                                                                                 hearings,
                                                                                                                 authorisation));
                    } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
                        caseData = manageOrderService.setHearingDataForSdo(caseData, hearings, authorisation);
                    }
                    draftOrder = getUpdatedDraftOrder(draftOrder, caseData, loggedInUserType, eventId);
                } else {
                    draftOrder = getDraftOrderWithUpdatedStatus(caseData, eventId, loggedInUserType, draftOrder);
                    caseData = updateCaseDataForFinalOrderDocument(
                        caseData,
                        authorisation,
                        draftOrder.getOrderType(),
                        eventId
                    );
                }

                updatedCaseData.put(
                    ORDER_COLLECTION,
                    getFinalOrderCollection(authorisation, caseData, draftOrder, eventId)
                );
                draftOrderCollection.remove(
                    draftOrderCollection.indexOf(e)
                );
                break;
            }
        }

        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        updatedCaseData.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        return updatedCaseData;

    }

    private CaseData updateCaseDataForFinalOrderDocument(CaseData caseData, String authorisation, CreateSelectOrderOptionsEnum orderType,
                                                         String eventId) {
        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, Map.class);
        Object dynamicList = caseData.getDraftOrdersDynamicList();
        if (Event.EDIT_RETURNED_ORDER.getId().equals(eventId)) {
            dynamicList = caseData.getManageOrders().getRejectedOrdersDynamicList();
        }
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), dynamicList);
        caseDataMap.putAll(populateCommonDraftOrderFields(authorisation, caseData, selectedOrder));
        StandardDirectionOrder standardDirectionOrder = null;
        if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(orderType)) {
            Map<String, Object> standardDirectionOrderMap = populateStandardDirectionOrder(
                authorisation,
                caseData,
                false
            );
            standardDirectionOrder = objectMapper.convertValue(standardDirectionOrderMap, StandardDirectionOrder.class);
        } else if (!(CreateSelectOrderOptionsEnum.noticeOfProceedings.equals(orderType)
            || CreateSelectOrderOptionsEnum.noticeOfProceedingsParties.equals(orderType)
            || CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties.equals(orderType))) {
            caseDataMap.putAll(populateDraftOrderCustomFields(caseData, selectedOrder));
        }
        caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
        caseData = caseData.toBuilder().standardDirectionOrder(null != standardDirectionOrder ? standardDirectionOrder : null).build();
        return caseData;
    }

    private List<Element<OrderDetails>> getFinalOrderCollection(String auth, CaseData caseData, DraftOrder draftOrder, String eventId) {

        List<Element<OrderDetails>> orderCollection;
        if (caseData.getOrderCollection() != null) {
            orderCollection = caseData.getOrderCollection();
        } else {
            orderCollection = new ArrayList<>();
        }
        List<Element<OrderDetails>> newOrderDetails = new ArrayList<>();
        newOrderDetails.add(convertDraftOrderToFinal(auth, caseData, draftOrder, eventId));
        if (isNotEmpty(caseData.getManageOrders().getServeOrderDynamicList())
            && CollectionUtils.isNotEmpty(caseData.getManageOrders().getServeOrderDynamicList().getValue())
            && Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
            updateCurrentOrderId(
                caseData.getManageOrders().getServeOrderDynamicList(),
                orderCollection,
                newOrderDetails
            );
        }

        orderCollection.addAll(newOrderDetails);
        orderCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
        return orderCollection;
    }

    private Element<OrderDetails> convertDraftOrderToFinal(String auth, CaseData caseData, DraftOrder draftOrder, String eventId) {
        String loggedInUserType = manageOrderService.getLoggedInUserType(auth);
        OrderDetails orderDetails = getOrderDetails(
            caseData,
            draftOrder,
            eventId,
            loggedInUserType
        );
        orderDetails = generateFinalOrderDocument(
            auth,
            caseData,
            draftOrder,
            orderDetails
        );
        return element(orderDetails);

    }

    private OrderDetails generateFinalOrderDocument(String auth, CaseData caseData, DraftOrder draftOrder, OrderDetails orderDetails) {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        GeneratedDocumentInfo generatedDocumentInfoWelsh = null;
        if (Yes.equals(draftOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            orderDetails = orderDetails.toBuilder()
                .orderDocument(draftOrder.getOrderDocument())
                .build();
        } else {
            caseData = updateCaseDataForDocmosis(caseData, draftOrder);
            caseData = caseData.toBuilder().manageOrders(
                caseData.getManageOrders().toBuilder()
                    .ordersHearingDetails(draftOrder.getManageOrderHearingDetails())
                    .build()
            ).build();
            if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
                caseData = manageOrderService.filterEmptyHearingDetails(caseData);
            }
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            Map<String, String> fieldMap = manageOrderService.getOrderTemplateAndFile(draftOrder.getOrderType());
            if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData)) || ManageOrdersUtils.isDaOrderSelectedForCaCase(
                String.valueOf(caseData.getCreateSelectOrderOptions()),
                caseData)) {
                FL404 fl404CustomFields = getFl404CustomFields(caseData);
                caseData = caseData.toBuilder().manageOrders(caseData.getManageOrders().toBuilder().fl404CustomFields(
                    fl404CustomFields).build()).build();
            }
            try {
                if (documentLanguage.isGenEng()) {
                    generatedDocumentInfo = dgsService.generateDocument(
                        auth,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
                    );
                }
                if (documentLanguage.isGenWelsh() && fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_WELSH) != null) {
                    generatedDocumentInfoWelsh = dgsService.generateWelshDocument(
                        auth,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_WELSH)
                    );
                }
                orderDetails = orderDetails.toBuilder()
                    .orderDocument(manageOrderService.getGeneratedDocument(generatedDocumentInfo, false, fieldMap))
                    .orderDocumentWelsh(manageOrderService.getGeneratedDocument(
                        generatedDocumentInfoWelsh,
                        documentLanguage.isGenWelsh(),
                        fieldMap
                    ))
                    .build();
            } catch (Exception e) {
                log.error(
                    "Error while generating the final document for case {} and  order {}",
                    caseData.getId(),
                    draftOrder.getOrderType(),
                    e
                );
            }
        }
        return orderDetails;
    }

    private CaseData updateCaseDataForDocmosis(CaseData caseData, DraftOrder draftOrder) {
        manageOrderService.populateChildrenListForDocmosis(caseData);
        if ((C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData)))
            && CreateSelectOrderOptionsEnum.appointmentOfGuardian.equals(draftOrder.getOrderType())) {
            caseData = manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData);
        }
        if ((FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            || ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(draftOrder.getOrderType()),
                                                            caseData))
            && CreateSelectOrderOptionsEnum.generalForm.equals(draftOrder.getOrderType())) {
            boolean isDaOrderSelectedForCaCase = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && ManageOrdersUtils.isDaOrderSelectedForCaCase(
                String.valueOf(draftOrder.getOrderType()), caseData);
            caseData = caseData.toBuilder().manageOrders(caseData.getManageOrders().toBuilder()
                                                             .manageOrdersApplicant(isDaOrderSelectedForCaCase
                                                                                        ? CaseUtils
                                                                 .getApplicantNameForDaOrderSelectedForCaCase(caseData)
                                                                                        : CaseUtils.getApplicant(caseData))
                                                             .manageOrdersApplicantReference(isDaOrderSelectedForCaCase
                                                                                                 ? CaseUtils
                                                                 .getApplicantReferenceForDaOrderSelectedForCaCase(caseData)
                                                                                                 : CaseUtils.getApplicantReference(caseData))
                                                             .manageOrdersRespondent(isDaOrderSelectedForCaCase
                                                                                         ? CaseUtils
                                                                 .getRespondentForDaOrderSelectedForCaCase(caseData)
                                                                                         : CaseUtils.getRespondent(caseData))
                                                             .manageOrdersRespondentReference(
                                                                 caseData.getRespondentsFL401() != null
                                                                     ? caseData.getRespondentsFL401().getSolicitorReference() != null
                                                                     ? caseData.getRespondentsFL401().getSolicitorReference() : "" : null)
                                                             .manageOrdersRespondentDob(isDaOrderSelectedForCaCase
                                                                                            ? CaseUtils
                                                                 .getRespondentDobForDaOrderSelectedForCaCase(caseData)
                                                                                            : null != caseData.getRespondentsFL401().getDateOfBirth()
                                                                                            ? caseData.getRespondentsFL401().getDateOfBirth() : null)
                                                             .build())
                .build();
        }
        if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
            caseData = manageOrderService.populateJudgeNames(caseData);
            caseData = manageOrderService.populatePartyDetailsOfNewParterForDocmosis(caseData);
            if (isNotEmpty(caseData.getStandardDirectionOrder())
                && CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
                && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)) {
                caseData = manageOrderService.populateDirectionOfFactFindingHearingFieldsForDocmosis(caseData);
            }
        }
        return caseData;
    }

    private OrderDetails getOrderDetails(CaseData caseData, DraftOrder draftOrder, String eventId, String loggedInUserType) {
        ServeOrderData serveOrderData = CaseUtils.getServeOrderData(caseData);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        StandardDirectionOrder standardDirectionOrder = null;
        if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
            try {
                standardDirectionOrder = copyPropertiesToStandardDirectionOrder(draftOrder.getSdoDetails());
            } catch (JsonProcessingException exception) {
                throw new ManageOrderRuntimeException(MANAGE_ORDER_SDO_FAILURE, exception);
            }
        }
        return OrderDetails.builder()
            .orderType(String.valueOf(draftOrder.getOrderType()))
            .orderTypeId(draftOrder.getOrderTypeId())
            .typeOfOrder(typeOfOrder != null
                             ? typeOfOrder.getDisplayedValue() : null)
            .doesOrderClosesCase(caseData.getDoesOrderClosesCase())
            .serveOrderDetails(ManageOrderService.buildServeOrderDetails(serveOrderData))
            .adminNotes(caseData.getCourtAdminNotes())
            .dateCreated(draftOrder.getOtherDetails().getDateCreated())
            .judgeNotes(draftOrder.getJudgeNotes())
            .otherDetails(
                OtherOrderDetails.builder().createdBy(draftOrder.getOtherDetails().getCreatedBy())
                    .orderCreatedBy(draftOrder.getOtherDetails().getOrderCreatedBy())
                    .orderCreatedByEmailId(draftOrder.getOtherDetails().getOrderCreatedByEmailId())
                    .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                        PrlAppsConstants.D_MMM_YYYY,
                        Locale.ENGLISH
                    )))
                    .orderMadeDate(draftOrder.getDateOrderMade() != null ? draftOrder.getDateOrderMade().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMM_YYYY,
                            Locale.ENGLISH
                        )) : null)
                    .approvalDate(draftOrder.getApprovalDate() != null ? draftOrder.getApprovalDate().format(
                        DateTimeFormatter.ofPattern(
                            PrlAppsConstants.D_MMM_YYYY,
                            Locale.ENGLISH
                        )) : null)
                    .orderRecipients(manageOrderService.getAllRecipients(caseData))
                    .status(manageOrderService.getOrderStatus(
                        draftOrder.getOrderSelectionType(),
                        loggedInUserType,
                        eventId,
                        draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                    ))
                    .additionalRequirementsForHearingReq(
                        manageOrderService.getAdditionalRequirementsForHearingReq(
                            draftOrder.getManageOrderHearingDetails(),
                            false,
                            standardDirectionOrder,
                            draftOrder.getOrderType(),
                            draftOrder.getC21OrderOptions()
                        ))
                    .build())
            .isTheOrderAboutChildren(draftOrder.getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(draftOrder.getIsTheOrderAboutAllChildren())
            .childrenList(draftOrder.getChildrenList())
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())
                            ? draftOrder.getSdoDetails() : null)
            .selectedHearingType(null != draftOrder.getHearingsType() ? draftOrder.getHearingsType().getValueCode() : null)
            .isOrderCreatedBySolicitor(draftOrder.getIsOrderCreatedBySolicitor())
            .manageOrderHearingDetails(draftOrder.getManageOrderHearingDetails())
            .c21OrderOptions(draftOrder.getC21OrderOptions())
            .childArrangementsOrdersToIssue(draftOrder.getChildArrangementsOrdersToIssue())
            .selectChildArrangementsOrder(draftOrder.getSelectChildArrangementsOrder())
            .childOption(draftOrder.getChildOption())
            .isOrderUploaded(draftOrder.getIsOrderUploadedByJudgeOrAdmin())
            .build();
    }

    public CaseData populateCustomFields(CaseData caseData) {
        switch (caseData.getCreateSelectOrderOptions()) {
            case blankOrderOrDirections:
                return null;
            case nonMolestation:
                return null;
            default:
                return caseData;

        }
    }

    public Map<String, Object> populateDraftOrderDocument(CaseData caseData, String authorization) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(
            caseData.getDraftOrderCollection(),
            caseData.getDraftOrdersDynamicList()
        );
        caseDataMap.put(ORDER_NAME, ManageOrdersUtils.getOrderName(selectedOrder));
        caseDataMap.put("previewUploadedOrder", selectedOrder.getOrderDocument());
        if (!StringUtils.isEmpty(selectedOrder.getJudgeNotes())) {
            caseDataMap.put("uploadOrAmendDirectionsFromJudge", selectedOrder.getJudgeNotes());
        } else {
            caseDataMap.put("judgeNotesEmptyUploadJourney", YES);
        }
        caseDataMap.put(ORDER_UPLOADED_AS_DRAFT_FLAG, selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        caseDataMap.put("manageOrderOptionType", selectedOrder.getOrderSelectionType());
        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
        DocumentLanguage language = documentLanguageService.docGenerateLang(caseData);
        if (language.isGenEng()) {
            caseDataMap.put("previewDraftOrder", selectedOrder.getOrderDocument());
        }
        if (language.isGenWelsh()) {
            caseDataMap.put("previewDraftOrderWelsh", selectedOrder.getOrderDocumentWelsh());
        }
        if (selectedOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", selectedOrder.getJudgeNotes());
        } else {
            caseDataMap.put("judgeNotesEmptyDraftJourney", YES);
        }
        if (null != selectedOrder.getOtherDetails()) {
            caseDataMap.put(
                LEGAL_REP_INSTRUCTIONS_PLACE_HOLDER,
                selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative()
            );
        }
        caseDataMap.put(
            IS_HEARING_PAGE_NEEDED,
            isHearingPageNeeded(selectedOrder.getOrderType(), selectedOrder.getC21OrderOptions()) ? Yes : No
        );
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        //PRL-4854 - upload order
        updateHearingsType(caseData, caseDataMap, selectedOrder, authorization);
        caseDataMap.put(ORDER_UPLOADED_AS_DRAFT_FLAG, selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        caseDataMap.put("wasTheOrderApprovedAtHearing", selectedOrder.getWasTheOrderApprovedAtHearing());

        return caseDataMap;
    }

    public Map<String, Object> populateDraftOrderCustomFields(CaseData caseData, DraftOrder selectedOrder) {
        Map<String, Object> caseDataMap = new HashMap<>();
        if (!CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(selectedOrder.getOrderType())) {
            caseDataMap.put("fl404CustomFields", selectedOrder.getFl404CustomFields());
            caseDataMap.put("parentName", selectedOrder.getParentName());
            caseDataMap.put("childArrangementsOrdersToIssue", selectedOrder.getChildArrangementsOrdersToIssue());
            caseDataMap.put("selectChildArrangementsOrder", selectedOrder.getSelectChildArrangementsOrder());
            caseDataMap.put("cafcassOfficeDetails", selectedOrder.getCafcassOfficeDetails());
            caseDataMap.put("appointedGuardianName", selectedOrder.getAppointedGuardianName());
            caseDataMap.put("manageOrdersFl402CourtName", selectedOrder.getManageOrdersFl402CourtName());
            caseDataMap.put("manageOrdersFl402CourtAddress", selectedOrder.getManageOrdersFl402CourtAddress());
            caseDataMap.put("manageOrdersFl402CaseNo", selectedOrder.getManageOrdersFl402CaseNo());
            caseDataMap.put("manageOrdersFl402Applicant", selectedOrder.getManageOrdersFl402Applicant());
            caseDataMap.put("manageOrdersFl402ApplicantRef", selectedOrder.getManageOrdersFl402ApplicantRef());
            caseDataMap.put("fl402HearingCourtname", selectedOrder.getFl402HearingCourtname());
            caseDataMap.put("fl402HearingCourtAddress", selectedOrder.getFl402HearingCourtAddress());
            caseDataMap.put("manageOrdersDateOfhearing", selectedOrder.getManageOrdersDateOfhearing());
            caseDataMap.put("dateOfHearingTime", selectedOrder.getDateOfHearingTime());
            caseDataMap.put("dateOfHearingTimeEstimate", selectedOrder.getDateOfHearingTimeEstimate());
            caseDataMap.put("manageOrdersCourtName", selectedOrder.getManageOrdersCourtName());
            caseDataMap.put("manageOrdersCourtAddress", selectedOrder.getManageOrdersCourtAddress());
            caseDataMap.put("manageOrdersCaseNo", selectedOrder.getManageOrdersCaseNo());
            caseDataMap.put(
                "manageOrdersApplicant",
                StringUtils.isEmpty(selectedOrder.getManageOrdersApplicant()) ? CaseUtils.getApplicant(
                    caseData) : selectedOrder.getManageOrdersApplicant()
            );
            caseDataMap.put(
                "manageOrdersApplicantReference",
                StringUtils.isEmpty(selectedOrder.getManageOrdersApplicantReference()) ? CaseUtils.getApplicantReference(
                    caseData) : selectedOrder.getManageOrdersApplicantReference()
            );
            caseDataMap.put(
                "manageOrdersRespondent",
                StringUtils.isEmpty(selectedOrder.getManageOrdersRespondent()) ? CaseUtils.getRespondent(
                    caseData) : selectedOrder.getManageOrdersRespondent()
            );
            caseDataMap.put("manageOrdersRespondentReference", selectedOrder.getManageOrdersRespondentReference());
            caseDataMap.put("manageOrdersRespondentDob", selectedOrder.getManageOrdersRespondentDob());
            caseDataMap.put("manageOrdersRespondentAddress", selectedOrder.getManageOrdersRespondentAddress());
            caseDataMap.put("manageOrdersUnderTakingRepr", selectedOrder.getManageOrdersUnderTakingRepr());
            caseDataMap.put("underTakingSolicitorCounsel", selectedOrder.getUnderTakingSolicitorCounsel());
            caseDataMap.put("manageOrdersUnderTakingPerson", selectedOrder.getManageOrdersUnderTakingPerson());
            caseDataMap.put("manageOrdersUnderTakingAddress", selectedOrder.getManageOrdersUnderTakingAddress());
            caseDataMap.put("manageOrdersUnderTakingTerms", selectedOrder.getManageOrdersUnderTakingTerms());
            caseDataMap.put("manageOrdersDateOfUnderTaking", selectedOrder.getManageOrdersDateOfUnderTaking());
            caseDataMap.put("underTakingDateExpiry", selectedOrder.getUnderTakingDateExpiry());
            caseDataMap.put("underTakingExpiryTime", selectedOrder.getUnderTakingExpiryTime());
            caseDataMap.put("underTakingExpiryDateTime", selectedOrder.getUnderTakingExpiryDateTime());
            caseDataMap.put("underTakingFormSign", selectedOrder.getUnderTakingFormSign());
            caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());
            caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
            caseDataMap.put("hasJudgeProvidedHearingDetails", selectedOrder.getHasJudgeProvidedHearingDetails());
            caseDataMap.put(
                IS_HEARING_PAGE_NEEDED,
                isHearingPageNeeded(
                    selectedOrder.getOrderType(),
                    selectedOrder.getC21OrderOptions()
                ) ? Yes : No
            );
        } else {
            caseDataMap.putAll(objectMapper.convertValue(selectedOrder.getSdoDetails(), Map.class));
            manageOrderService.populateWarningMessageIfRequiredForFactFindingHearing(caseData, caseDataMap);
        }

        return caseDataMap;
    }

    public Map<String, Object> populateStandardDirectionOrder(String authorisation, CaseData caseData, boolean editOrder) {
        Map<String, Object> standardDirectionOrderMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(
            caseData.getDraftOrderCollection(),
            caseData.getDraftOrdersDynamicList()
        );
        if (null != selectedOrder.getSdoDetails()) {
            StandardDirectionOrder standardDirectionOrder = null;
            try {
                SdoDetails updatedSdoDetails = selectedOrder.getSdoDetails().toBuilder()
                    .sdoPreamblesList(editOrder ? caseData.getStandardDirectionOrder().getSdoPreamblesList()
                                          : selectedOrder.getSdoDetails().getSdoPreamblesList())
                    .sdoHearingsAndNextStepsList(editOrder ? caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList()
                                                     : selectedOrder.getSdoDetails().getSdoHearingsAndNextStepsList())
                    .sdoCafcassOrCymruList(editOrder ? caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList()
                                               : selectedOrder.getSdoDetails().getSdoCafcassOrCymruList())
                    .sdoLocalAuthorityList(editOrder ? caseData.getStandardDirectionOrder().getSdoLocalAuthorityList()
                                               : selectedOrder.getSdoDetails().getSdoLocalAuthorityList())
                    .sdoCourtList(editOrder ? caseData.getStandardDirectionOrder().getSdoCourtList()
                                      : selectedOrder.getSdoDetails().getSdoCourtList())
                    .sdoDocumentationAndEvidenceList(editOrder ? caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList()
                                                         : selectedOrder.getSdoDetails().getSdoDocumentationAndEvidenceList())
                    .sdoFurtherList(editOrder ? caseData.getStandardDirectionOrder().getSdoFurtherList()
                                        : selectedOrder.getSdoDetails().getSdoFurtherList())
                    .sdoOtherList(editOrder ? caseData.getStandardDirectionOrder().getSdoOtherList()
                                      : selectedOrder.getSdoDetails().getSdoOtherList())
                    .build();
                standardDirectionOrder = copyPropertiesToStandardDirectionOrder(updatedSdoDetails);
                Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                    hearingDataService.populateHearingDynamicLists(
                        authorisation,
                        String.valueOf(caseData.getId()),
                        caseData,
                        hearings
                    );
                standardDirectionOrder = standardDirectionOrder.toBuilder()
                    .sdoUrgentHearingDetails(isNotEmpty(standardDirectionOrder.getSdoUrgentHearingDetails())
                                                 ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoUrgentHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoPermissionHearingDetails(isNotEmpty(standardDirectionOrder.getSdoPermissionHearingDetails())
                                                     ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoPermissionHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoSecondHearingDetails(isNotEmpty(standardDirectionOrder.getSdoSecondHearingDetails())
                                                 ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoSecondHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoFhdraHearingDetails(isNotEmpty(standardDirectionOrder.getSdoFhdraHearingDetails())
                                                ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoFhdraHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoDraHearingDetails(isNotEmpty(standardDirectionOrder.getSdoDraHearingDetails())
                                              ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoDraHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoSettlementHearingDetails(isNotEmpty(standardDirectionOrder.getSdoSettlementHearingDetails())
                                                     ? hearingDataService.getHearingDataForSdo(
                        standardDirectionOrder.getSdoSettlementHearingDetails(),
                        hearingDataPrePopulatedDynamicLists,
                        caseData
                    ) : null)
                    .sdoDirectionsForFactFindingHearingDetails(
                        isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails())
                            && isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails()
                                              .getHearingDateConfirmOptionEnum())
                            ? hearingDataService.getHearingDataForSdo(
                            standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails(),
                            hearingDataPrePopulatedDynamicLists,
                            caseData
                        ) : null)
                    .editedOrderHasDefaultCaseFields(Yes)
                    .sdoPreamblesTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoPreamblesList()
                                              : selectedOrder.getSdoDetails().getSdoPreamblesList())
                    .sdoHearingsAndNextStepsTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList()
                                                         : selectedOrder.getSdoDetails().getSdoHearingsAndNextStepsList())
                    .sdoCafcassOrCymruTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList()
                                                   : selectedOrder.getSdoDetails().getSdoCafcassOrCymruList())
                    .sdoLocalAuthorityTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoLocalAuthorityList()
                                                   : selectedOrder.getSdoDetails().getSdoLocalAuthorityList())
                    .sdoCourtTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoCourtList()
                                          : selectedOrder.getSdoDetails().getSdoCourtList())
                    .sdoDocumentationAndEvidenceTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList()
                                                             : selectedOrder.getSdoDetails().getSdoDocumentationAndEvidenceList())
                    .sdoOtherTempList(editOrder ? caseData.getStandardDirectionOrder().getSdoOtherList()
                                          : selectedOrder.getSdoDetails().getSdoOtherList())
                    .build();
                caseData = caseData.toBuilder().standardDirectionOrder(standardDirectionOrder).build();
                standardDirectionOrderMap = objectMapper.convertValue(standardDirectionOrder, Map.class);
                populateStandardDirectionOrderDefaultFields(authorisation, caseData, standardDirectionOrderMap);
            } catch (JsonProcessingException exception) {
                throw new ManageOrderRuntimeException(MANAGE_ORDER_SDO_FAILURE, exception);
            }
        }
        return standardDirectionOrderMap;
    }

    public StandardDirectionOrder copyPropertiesToStandardDirectionOrder(SdoDetails updatedSdoDetails) throws JsonProcessingException {
        StandardDirectionOrder standardDirectionOrder;
        String sdoDetailsJson = objectMapper.writeValueAsString(updatedSdoDetails);
        standardDirectionOrder = objectMapper.readValue(sdoDetailsJson, StandardDirectionOrder.class);
        return standardDirectionOrder;
    }

    public Map<String, Object> populateCommonDraftOrderFields(String authorization, CaseData caseData, DraftOrder selectedOrder) {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(ORDER_NAME, ManageOrdersUtils.getOrderName(selectedOrder));
        caseDataMap.put(DRAFT_ORDERS_DYNAMIC_LIST, caseData.getDraftOrdersDynamicList());
        caseDataMap.put("orderType", selectedOrder.getOrderType());
        caseDataMap.put("isTheOrderByConsent", selectedOrder.getIsTheOrderByConsent());
        caseDataMap.put(DATE_ORDER_MADE, selectedOrder.getDateOrderMade());
        caseDataMap.put("wasTheOrderApprovedAtHearing", selectedOrder.getWasTheOrderApprovedAtHearing());
        caseDataMap.put("judgeOrMagistrateTitle", selectedOrder.getJudgeOrMagistrateTitle());
        if (!StringUtils.isEmpty(selectedOrder.getJudgeNotes())) {
            caseDataMap.put("uploadOrAmendDirectionsFromJudge", selectedOrder.getJudgeNotes());
        } else {
            caseDataMap.put("judgeNotesEmptyUploadJourney", YES);
        }
        caseDataMap.put("judgeOrMagistratesLastName", selectedOrder.getJudgeOrMagistratesLastName());
        caseDataMap.put("justiceLegalAdviserFullName", selectedOrder.getJusticeLegalAdviserFullName());
        caseDataMap.put("magistrateLastName", selectedOrder.getMagistrateLastName());
        caseDataMap.put("isTheOrderAboutAllChildren", selectedOrder.getIsTheOrderAboutAllChildren());
        caseDataMap.put("isTheOrderAboutChildren", selectedOrder.getIsTheOrderAboutChildren());
        caseDataMap.put("childOption", (Yes.equals(selectedOrder.getIsTheOrderAboutChildren())
            || No.equals(selectedOrder.getIsTheOrderAboutAllChildren()))
            ? selectedOrder.getChildOption() : DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        caseDataMap.put(DA_ORDER_FOR_CA_CASE, ManageOrdersUtils.isDaOrderSelectedForCaCase(
                String.valueOf(selectedOrder.getOrderType()),
                caseData) ? Yes : No);
        caseDataMap.put("recitalsOrPreamble", selectedOrder.getRecitalsOrPreamble());
        caseDataMap.put("orderDirections", selectedOrder.getOrderDirections());
        caseDataMap.put("c21OrderOptions", selectedOrder.getC21OrderOptions());
        caseDataMap.put("furtherDirectionsIfRequired", selectedOrder.getFurtherDirectionsIfRequired());
        caseDataMap.put("furtherInformationIfRequired", selectedOrder.getFurtherInformationIfRequired());
        caseDataMap.put("childArrangementsOrdersToIssue", selectedOrder.getChildArrangementsOrdersToIssue());
        caseDataMap.put("selectChildArrangementsOrder", selectedOrder.getSelectChildArrangementsOrder());
        caseDataMap.put("cafcassOfficeDetails", selectedOrder.getCafcassOfficeDetails());
        if (selectedOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", selectedOrder.getJudgeNotes());
        } else {
            caseDataMap.put("judgeNotesEmptyDraftJourney", YES);
        }

        populateOrderHearingDetails(
            authorization,
            caseData,
            caseDataMap,
            selectedOrder.getManageOrderHearingDetails()
        );

        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
        caseDataMap.put("hasJudgeProvidedHearingDetails", selectedOrder.getHasJudgeProvidedHearingDetails());
        caseDataMap.put(
            IS_HEARING_PAGE_NEEDED,
            isHearingPageNeeded(selectedOrder.getOrderType(), selectedOrder.getC21OrderOptions()) ? Yes : No
        );
        caseDataMap.put("doYouWantToEditTheOrder", caseData.getDoYouWantToEditTheOrder());
        if (caseData.getManageOrders() != null) {
            if (Yes.equals(selectedOrder.getIsOrderCreatedBySolicitor())) {
                caseDataMap.put(
                    "whatToDoWithOrderSolicitor",
                    caseData.getManageOrders().getWhatToDoWithOrderSolicitor()
                );
            } else if (No.equals(selectedOrder.getIsOrderCreatedBySolicitor())) {
                caseDataMap.put(
                    "whatToDoWithOrderCourtAdmin",
                    caseData.getManageOrders().getWhatToDoWithOrderCourtAdmin()
                );
            }
        }
        //refactored to a private method
        updateHearingsType(caseData, caseDataMap, selectedOrder, authorization);
        caseDataMap.put(ORDER_UPLOADED_AS_DRAFT_FLAG, selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        return caseDataMap;
    }

    private void updateHearingsType(CaseData caseData,
                                    Map<String, Object> caseDataMap,
                                    DraftOrder selectedOrder,
                                    String authorization) {
        //Set existing hearingsType from draft order
        ManageOrders manageOrders = null != caseData.getManageOrders()
            ? caseData.getManageOrders().toBuilder().hearingsType(selectedOrder.getHearingsType()).build()
            : ManageOrders.builder().hearingsType(selectedOrder.getHearingsType()).build();
        caseData = caseData.toBuilder()
            .manageOrders(manageOrders)
            .build();
        //PRL-3319 - Fetch hearings dropdown
        DynamicList hearingsDynamicList = manageOrderService.populateHearingsDropdown(authorization, caseData);
        caseDataMap.put(HEARINGS_TYPE, hearingsDynamicList);
    }

    public void populateOrderHearingDetails(String authorization, CaseData caseData, Map<String, Object> caseDataMap,
                                            List<Element<HearingData>> manageOrderHearingDetail) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        Hearings hearings = hearingService.getHearings(authorization, caseReferenceNumber);
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorization, caseReferenceNumber, caseData, hearings);
        if (CollectionUtils.isEmpty(manageOrderHearingDetail)) {
            HearingData hearingData = hearingDataService.generateHearingData(
                hearingDataPrePopulatedDynamicLists, caseData);
            manageOrderHearingDetail = ElementUtils.wrapElements(hearingData);
        } else {
            List<Element<HearingData>> updatedManageOrderHearingDetail = new ArrayList<>();
            for (Element<HearingData> hearingDataElement : manageOrderHearingDetail) {
                hearingDataElement = Element.<HearingData>builder()
                    .value(resetHearingConfirmedDatesAndLinkedCases(
                        hearingDataPrePopulatedDynamicLists,
                        hearingDataElement.getValue()
                    ))
                    .id(hearingDataElement.getId())
                    .build();
                updatedManageOrderHearingDetail.add(hearingDataElement);
            }
            manageOrderHearingDetail = updatedManageOrderHearingDetail;
        }

        caseDataMap.put(ORDERS_HEARING_DETAILS, manageOrderHearingDetail);
        //add hearing screen field show params
        ManageOrdersUtils.addHearingScreenFieldShowParams(null, caseDataMap, caseData);
    }

    private static HearingData resetHearingConfirmedDatesAndLinkedCases(
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
        HearingData hearingData) {
        return hearingData.toBuilder()
            .confirmedHearingDates(isNotEmpty(hearingData.getConfirmedHearingDates())
                                       ? hearingData.getConfirmedHearingDates().toBuilder()
                .listItems(
                    hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates()
                        .getListItems())
                .build() : DynamicList.builder().value(DynamicListElement.EMPTY).listItems(List.of(DynamicListElement.EMPTY)).build())
            .hearingListedLinkedCases(isNotEmpty(hearingData.getHearingListedLinkedCases())
                                          ? hearingData.getHearingListedLinkedCases().toBuilder()
                .listItems(
                    hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases()
                        .getListItems())
                .build() : DynamicList.builder().value(DynamicListElement.EMPTY).listItems(List.of(DynamicListElement.EMPTY)).build())
            .build();
    }

    public DraftOrder getSelectedDraftOrderDetails(List<Element<DraftOrder>> draftOrderCollection, Object dynamicList) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper);
        return draftOrderCollection.stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Could not find order"));
    }

    public Map<String, Object> updateDraftOrderCollection(CaseData caseData, String authorisation, String eventId) {
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        UUID selectedOrderId;
        if (Event.EDIT_RETURNED_ORDER.getId().equalsIgnoreCase(eventId)) {
            selectedOrderId = elementUtils.getDynamicListSelectedValue(
                caseData.getManageOrders().getRejectedOrdersDynamicList(), objectMapper);
        } else {
            selectedOrderId = elementUtils.getDynamicListSelectedValue(
                caseData.getDraftOrdersDynamicList(), objectMapper);
        }
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            if (e.getId().equals(selectedOrderId)) {
                DraftOrder draftOrder = e.getValue();
                if (ManageOrdersUtils.isOrderEdited(caseData, eventId)) {
                    Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
                    if (isHearingPageNeeded(draftOrder.getOrderType(), draftOrder.getC21OrderOptions())) {
                        caseData.getManageOrders().setOrdersHearingDetails(hearingDataService
                                                                               .getHearingDataForSelectedHearing(
                                                                                   caseData,
                                                                                   hearings,
                                                                                   authorisation
                                                                               ));
                        log.info("Updated order hearing details for docmosis");
                    } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
                        caseData = manageOrderService.setHearingDataForSdo(caseData, hearings, authorisation);
                        log.info("Updated sdo order hearing details for docmosis");
                    }
                    draftOrder = getUpdatedDraftOrder(draftOrder, caseData, loggedInUserType, eventId);
                    //Default hearing option to 1 for edit returned
                    if (Event.EDIT_RETURNED_ORDER.getId().equalsIgnoreCase(eventId)
                        && Yes.equals(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())) {
                        defaultHearingOptionToDateReservedWithListAssist(draftOrder);
                    }
                } else {
                    draftOrder = getDraftOrderWithUpdatedStatus(caseData, eventId, loggedInUserType, draftOrder);
                }
                draftOrderCollection.set(
                    draftOrderCollection.indexOf(e),
                    element(selectedOrderId, draftOrder)
                );
                break;
            }
        }
        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of(DRAFT_ORDER_COLLECTION, draftOrderCollection);
    }

    private DraftOrder getDraftOrderWithUpdatedStatus(CaseData caseData, String eventId, String loggedInUserType, DraftOrder draftOrder) {
        String status = manageOrderService.getOrderStatus(
            draftOrder.getOrderSelectionType(),
            loggedInUserType,
            eventId,
            draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
        );
        YesOrNo isJudgeApprovalNeeded = draftOrder.getOtherDetails().getIsJudgeApprovalNeeded();
        String instructionssToLegalRep = caseData.getManageOrders().getInstructionsToLegalRepresentative();
        if (Event.EDIT_AND_APPROVE_ORDER.getId().equals(eventId)) {
            if (OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges
                .equals(caseData.getManageOrders().getWhatToDoWithOrderSolicitor())) {
                status = OrderStatusEnum.rejectedByJudge.getDisplayedValue();
                isJudgeApprovalNeeded = Yes;
            } else {
                isJudgeApprovalNeeded = No;
                instructionssToLegalRep = "";
            }
        }
        return draftOrder.toBuilder()
            .judgeNotes(!StringUtils.isEmpty(draftOrder.getJudgeNotes()) ? draftOrder.getJudgeNotes() : caseData.getJudgeDirectionsToAdmin())
            .adminNotes(caseData.getCourtAdminNotes())
            .otherDetails(draftOrder.getOtherDetails().toBuilder()
                              .status(status)
                              .instructionsToLegalRepresentative(instructionssToLegalRep)
                              .isJudgeApprovalNeeded(isJudgeApprovalNeeded)
                              //PRL-4857 - clear this field as order is reviewed by judge/manager already.
                              .reviewRequiredBy(null)
                              .build())
            .build();
    }

    private DraftOrder getUpdatedDraftOrder(DraftOrder draftOrder, CaseData caseData, String loggedInUserType, String eventId) {
        Document orderDocumentEng = null;
        Document orderDocumentWelsh = null;
        if (ManageOrdersUtils.isOrderEdited(caseData, eventId)) {
            //PRL-4854 - uploaded order
            if (Yes.equals(draftOrder.getIsOrderUploadedByJudgeOrAdmin())) {
                orderDocumentEng = caseData.getManageOrders().getEditedUploadOrderDoc();
            } else {
                orderDocumentEng = caseData.getPreviewOrderDoc();
                orderDocumentWelsh = caseData.getPreviewOrderDocWelsh();
            }
        } else {
            orderDocumentEng = draftOrder.getOrderDocument();
            orderDocumentWelsh = draftOrder.getOrderDocumentWelsh();
        }
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);

        if (Yes.equals(draftOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            return draftOrder.toBuilder()
                .orderDocument(orderDocumentEng)
                .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
                .hearingsType(caseData.getManageOrders().getHearingsType())
                .otherDetails(draftOrder.getOtherDetails().toBuilder()
                                  .status(manageOrderService.getOrderStatus(
                                      draftOrder.getOrderSelectionType(),
                                      loggedInUserType,
                                      eventId,
                                      draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                                  ))
                                  .isJudgeApprovalNeeded(Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
                                                             ? No : draftOrder.getOtherDetails().getIsJudgeApprovalNeeded())
                                  .build())
                .adminNotes(!StringUtils.isEmpty(draftOrder.getAdminNotes()) ? draftOrder.getAdminNotes() : caseData.getCourtAdminNotes())
                .judgeNotes(!StringUtils.isEmpty(draftOrder.getJudgeNotes()) ? draftOrder.getJudgeNotes() : caseData.getJudgeDirectionsToAdmin())
                .build();

        } else {
            boolean isDaOrderSelectedForCaCase = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && ManageOrdersUtils.isDaOrderSelectedForCaCase(
                String.valueOf(draftOrder.getOrderType()), caseData);
            return draftOrder.toBuilder()
                .typeOfOrder(typeOfOrder != null ? typeOfOrder.getDisplayedValue() : null)
                .orderDocument(orderDocumentEng)
                .orderDocumentWelsh(orderDocumentWelsh)
                .otherDetails(draftOrder.getOtherDetails().toBuilder()
                                  .createdBy(caseData.getJudgeOrMagistratesLastName())
                                  .dateCreated(draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getDateCreated() : dateTime.now())
                                  .status(manageOrderService.getOrderStatus(
                                      draftOrder.getOrderSelectionType(),
                                      loggedInUserType,
                                      eventId,
                                      draftOrder.getOtherDetails() != null ? draftOrder.getOtherDetails().getStatus() : null
                                  ))
                                  .isJudgeApprovalNeeded(Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
                                                             ? No : draftOrder.getOtherDetails().getIsJudgeApprovalNeeded())
                                  .additionalRequirementsForHearingReq(manageOrderService.getAdditionalRequirementsForHearingReq(
                                      caseData.getManageOrders().getOrdersHearingDetails(),
                                      true,
                                      caseData.getStandardDirectionOrder(),
                                      draftOrder.getOrderType(),
                                      draftOrder.getC21OrderOptions()
                                  ))
                                  .instructionsToLegalRepresentative(Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(
                                      eventId) ? null : draftOrder.getOtherDetails().getInstructionsToLegalRepresentative())
                                  .build())
                .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
                .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
                .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
                .judgeOrMagistratesLastName(caseData.getJudgeOrMagistratesLastName())
                .justiceLegalAdviserFullName(caseData.getJusticeLegalAdviserFullName())
                .magistrateLastName(caseData.getMagistrateLastName())
                .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
                .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
                .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
                .childOption(manageOrderService.getChildOption(caseData))
                .orderDirections(caseData.getManageOrders().getOrderDirections())
                .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
                .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
                .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
                .manageOrdersCourtName(caseData.getManageOrders().getManageOrdersCourtName())
                .manageOrdersCourtAddress(caseData.getManageOrders().getManageOrdersCourtAddress())
                .manageOrdersCaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
                .manageOrdersApplicant(isDaOrderSelectedForCaCase ? CaseUtils.getApplicantNameForDaOrderSelectedForCaCase(caseData)
                                           : CaseUtils.getApplicant(caseData))
                .manageOrdersApplicantReference(isDaOrderSelectedForCaCase ? CaseUtils.getApplicantReferenceForDaOrderSelectedForCaCase(caseData)
                                                    : CaseUtils.getApplicantReference(caseData))
                .manageOrdersRespondent(isDaOrderSelectedForCaCase
                                            ? CaseUtils.getRespondentForDaOrderSelectedForCaCase(caseData)
                                            : caseData.getManageOrders().getManageOrdersRespondent())
                .manageOrdersRespondentReference(caseData.getManageOrders().getManageOrdersRespondentReference())
                .manageOrdersRespondentDob(isDaOrderSelectedForCaCase ? CaseUtils.getRespondentDobForDaOrderSelectedForCaCase(caseData)
                                               : caseData.getManageOrders().getManageOrdersRespondentDob())
                .manageOrdersRespondentAddress(caseData.getManageOrders().getManageOrdersRespondentAddress())
                .manageOrdersUnderTakingRepr(caseData.getManageOrders().getManageOrdersUnderTakingRepr())
                .underTakingSolicitorCounsel(caseData.getManageOrders().getUnderTakingSolicitorCounsel())
                .manageOrdersUnderTakingPerson(caseData.getManageOrders().getManageOrdersUnderTakingPerson())
                .manageOrdersUnderTakingAddress(caseData.getManageOrders().getManageOrdersUnderTakingAddress())
                .manageOrdersUnderTakingTerms(caseData.getManageOrders().getManageOrdersUnderTakingTerms())
                .manageOrdersDateOfUnderTaking(caseData.getManageOrders().getManageOrdersDateOfUnderTaking())
                .underTakingDateExpiry(caseData.getManageOrders().getUnderTakingDateExpiry())
                .underTakingExpiryTime(caseData.getManageOrders().getUnderTakingExpiryTime())
                .underTakingExpiryDateTime(caseData.getManageOrders().getUnderTakingExpiryDateTime())
                .underTakingFormSign(caseData.getManageOrders().getUnderTakingFormSign())
                .adminNotes(!StringUtils.isEmpty(draftOrder.getAdminNotes()) ? draftOrder.getAdminNotes() : caseData.getCourtAdminNotes())
                .judgeNotes(!StringUtils.isEmpty(draftOrder.getJudgeNotes()) ? draftOrder.getJudgeNotes() : caseData.getJudgeDirectionsToAdmin())
                .parentName(caseData.getManageOrders().getParentName())
                .dateOrderMade(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade() : draftOrder.getDateOrderMade())
                .childArrangementsOrdersToIssue(caseData.getManageOrders().getChildArrangementsOrdersToIssue())
                .selectChildArrangementsOrder(caseData.getManageOrders().getSelectChildArrangementsOrder())
                .cafcassOfficeDetails(caseData.getManageOrders().getCafcassOfficeDetails())
                .appointedGuardianName(caseData.getAppointedGuardianName())
                .manageOrdersDateOfhearing(caseData.getManageOrders().getManageOrdersDateOfhearing())
                .manageOrdersFl402CaseNo(caseData.getManageOrders().getManageOrdersFl402CaseNo())
                .manageOrdersFl402ApplicantRef(caseData.getManageOrders().getManageOrdersFl402ApplicantRef())
                .manageOrdersFl402Applicant(caseData.getManageOrders().getManageOrdersFl402Applicant())
                .manageOrdersFl402CourtAddress(caseData.getManageOrders().getManageOrdersFl402CourtAddress())
                .manageOrdersFl402CourtName(caseData.getManageOrders().getManageOrdersFl402CourtName())
                .dateOfHearingTime(caseData.getManageOrders().getDateOfHearingTime())
                .dateOfHearingTimeEstimate(caseData.getManageOrders().getDateOfHearingTimeEstimate())
                .fl402HearingCourtname(caseData.getManageOrders().getFl402HearingCourtname())
                .fl402HearingCourtAddress(caseData.getManageOrders().getFl402HearingCourtAddress())
                .orderCreatedBy(loggedInUserType)
                .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                .childrenList(manageOrderService.getSelectedChildInfoFromMangeOrder(caseData))
                .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())
                                ? manageOrderService.copyPropertiesToSdoDetails(caseData) : null)
                .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
                .hearingsType(caseData.getManageOrders().getHearingsType())
                .build();
        }
    }

    public CaseData updateCustomFieldsWithApplicantRespondentDetails(@RequestBody CallbackRequest callbackRequest, CaseData caseData) {

        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            || ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(caseData.getCreateSelectOrderOptions()),
                                                            caseData)) {
            caseData = caseData.toBuilder()
                .manageOrders(caseData.getManageOrders().toBuilder()
                                  .manageOrdersFl402CaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
                                  .childOption(manageOrderService.getChildOption(caseData))
                                  .typeOfC21Order(null != caseData.getManageOrders().getC21OrderOptions()
                                                      ? BOLD_BEGIN + caseData.getManageOrders().getC21OrderOptions()
                                      .getDisplayedValue() + BOLD_END : null)
                                  .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
                                  .build()).build();
            caseData = manageOrderService.populateCustomOrderFields(caseData, getOrderType(callbackRequest, caseData));
        } else {
            caseData = caseData.toBuilder()
                .appointedGuardianName(caseData.getAppointedGuardianName())
                .dateOrderMade(caseData.getDateOrderMade())
                .standardDirectionOrder(caseData.getStandardDirectionOrder())
                .manageOrders(caseData.getManageOrders().toBuilder()
                                  .typeOfC21Order(caseData.getManageOrders().getC21OrderOptions() != null
                                                      ? BOLD_BEGIN + caseData.getManageOrders().getC21OrderOptions()
                                      .getDisplayedValue() + BOLD_END : null)
                                  .childOption(manageOrderService.getChildOption(caseData))
                                  .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
                                  .build()).build();
            CreateSelectOrderOptionsEnum selectedOrder = getOrderType(callbackRequest, caseData);
            if (ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(selectedOrder), caseData)) {
                caseData = manageOrderService.populateCustomOrderFields(caseData, selectedOrder);
            }

        }
        return caseData;
    }

    private CreateSelectOrderOptionsEnum getOrderType(CallbackRequest callbackRequest, CaseData caseData) {
        CreateSelectOrderOptionsEnum orderType = caseData.getCreateSelectOrderOptions();
        if (ObjectUtils.isEmpty(orderType) && !Event.DRAFT_AN_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            DraftOrder draftOrder;
            if (Event.EDIT_RETURNED_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                draftOrder = getSelectedDraftOrderDetails(
                    caseData.getDraftOrderCollection(),
                    caseData.getManageOrders()
                        .getRejectedOrdersDynamicList()
                );
            } else {
                draftOrder = getSelectedDraftOrderDetails(
                    caseData.getDraftOrderCollection(),
                    caseData.getDraftOrdersDynamicList()
                );
            }
            orderType = draftOrder.getOrderType();
        }
        return orderType;
    }

    private static FL404 getFl404CustomFields(CaseData caseData) {
        FL404 fl404CustomFields = caseData.getManageOrders().getFl404CustomFields();
        if (fl404CustomFields != null) {
            PartyDetails applicant1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                ? caseData.getApplicants().get(0).getValue() : caseData.getApplicantsFL401();
            PartyDetails respondent1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                ? caseData.getRespondents().get(0).getValue() : caseData.getRespondentsFL401();
            fl404CustomFields = fl404CustomFields.toBuilder().fl404bApplicantName(String.format(
                    PrlAppsConstants.FORMAT,
                    applicant1.getFirstName(),
                    applicant1.getLastName()
                ))
                .fl404bApplicantReference(applicant1.getSolicitorReference() != null
                                              ? applicant1.getSolicitorReference() : "")
                .fl404bCourtName(caseData.getCourtName())
                .fl404bRespondentName(String.format(
                    PrlAppsConstants.FORMAT,
                    respondent1.getFirstName(),
                    respondent1.getLastName()
                )).build();
            if (ofNullable(respondent1.getAddress()).isPresent()) {
                fl404CustomFields = fl404CustomFields.toBuilder()
                    .fl404bRespondentAddress(respondent1.getAddress()).build();
            }
            if (ofNullable(respondent1.getDateOfBirth()).isPresent()) {
                fl404CustomFields = fl404CustomFields.toBuilder()
                    .fl404bRespondentDob(respondent1.getDateOfBirth()).build();
            }
        }
        return fl404CustomFields;
    }

    public static boolean checkStandingOrderOptionsSelected(CaseData caseData, List<String> errorList) {
        if (caseData.getStandardDirectionOrder() != null
            && caseData.getStandardDirectionOrder().getSdoPreamblesList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoLocalAuthorityList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoCourtList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoOtherList().isEmpty()
            && caseData.getStandardDirectionOrder().getSdoFurtherList().isEmpty()) {
            errorList.add(
                "Please select at least one options from below");
            return false;
        } else {
            return true;
        }
    }

    public static boolean validationIfDirectionForFactFindingSelected(CaseData caseData, List<String> errorList) {
        if (caseData.getStandardDirectionOrder() != null
            && CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
            && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)
            && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && (caseData.getApplicants().size() > 1 || caseData.getRespondents().size() > 1)) {
            errorList.add(
                "You cannot add directions for a fact-finding hearing. Upload the order in manage orders");
            return false;
        } else {
            return true;
        }
    }

    public void populateStandardDirectionOrderDefaultFields(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {

        List<DynamicMultiselectListElement> applicantRespondentList = manageOrderService.getPartyDynamicMultiselectList(
            caseData);
        if (CollectionUtils.isNotEmpty(
            caseData.getStandardDirectionOrder().getSdoPreamblesList())) {
            populatePreambles(caseData, caseDataUpdated);
        }
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())) {
            populateHearingAndNextStepsText(caseData, caseDataUpdated, applicantRespondentList);
        }
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList())) {
            populateCafcassNextSteps(caseData, caseDataUpdated);
            populateCafcassCymruNextSteps(caseData, caseDataUpdated);
            populateSection7ChildImpactAnalysis(caseData, caseDataUpdated);
            if (caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().contains(partyToProvideDetailsOnly)
                && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruTempList()).contains(
                partyToProvideDetailsOnly)) {
                caseDataUpdated.put(
                    SDO_NEW_PARTNER_PARTIES_CAFCASS,
                    DynamicMultiSelectList.builder()
                        .listItems(applicantRespondentList)
                        .build()
                );
            }
            if (caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().contains(partyToProvideDetailsCmyru)
                && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruTempList()).contains(
                partyToProvideDetailsCmyru)) {
                caseDataUpdated.put(
                    SDO_NEW_PARTNER_PARTIES_CAFCASS_CYMRU,
                    DynamicMultiSelectList.builder()
                        .listItems(applicantRespondentList)
                        .build()
                );
            }
        }
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoCourtList())) {
            populateCourtText(caseData, caseDataUpdated);
        }
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList())) {
            populateDocumentAndEvidenceText(caseData, caseDataUpdated);
        }
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoOtherList())) {
            populateParentWithCare(caseData, caseDataUpdated);
        }
        List<DynamicListElement> courtList = getCourtDynamicList(authorisation);
        if (CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoLocalAuthorityList())) {
            populateLocalAuthorityDetails(caseData, caseDataUpdated);
        }
        if (!Yes.equals(caseData.getStandardDirectionOrder().getListElementsSetToDefaultValue())) {
            populateCourtDynamicList(courtList, caseDataUpdated, caseData);
            populateSdoDioProvideOtherDetails(caseData, caseDataUpdated);
            populateSdoInterpreterDialectRequired(caseData, caseDataUpdated);
            populateSdoPartiesRaisedAbuseCollection(caseData, caseDataUpdated);
            populateSdoLsApplicantRespondentNameCollection(caseData, caseDataUpdated);
            populateSdoMiamAttendingPerson(caseData, caseDataUpdated);
            populateSdoFurtherDirectionDetails(caseData, caseDataUpdated);
            if (caseData.getStandardDirectionOrder().getSdoInstructionsFilingPartiesDynamicList() == null
                || CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoInstructionsFilingPartiesDynamicList().getListItems())) {
                DynamicList partiesList = partiesListGenerator.buildPartiesList(caseData, courtList);
                caseDataUpdated.put(SDO_INSTRUCTIONS_FILING_PARTIES_DYNAMIC_LIST, partiesList);
            }
            populateSdoOrderHearingDetails(authorisation, caseData, caseDataUpdated);
            caseDataUpdated.put("listElementsSetToDefaultValue", Yes.getDisplayedValue());
        }

        setTemporaryPreambleValues(caseData, caseDataUpdated);
    }


    private void setTemporaryPreambleValues(CaseData caseData, Map<String, Object> caseDataUpdated) {
        caseDataUpdated.put("sdoPreamblesTempList", caseData.getStandardDirectionOrder().getSdoPreamblesList());
        caseDataUpdated.put("sdoHearingsAndNextStepsTempList",
                            caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList());
        caseDataUpdated.put("sdoCafcassOrCymruTempList",
                            caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList());
        caseDataUpdated.put("sdoLocalAuthorityTempList",
                            caseData.getStandardDirectionOrder().getSdoLocalAuthorityList());
        caseDataUpdated.put("sdoDocumentationAndEvidenceTempList",
                            caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList());
        caseDataUpdated.put("sdoOtherTempList", caseData.getStandardDirectionOrder().getSdoOtherList());
        caseDataUpdated.put("sdoCourtTempList", caseData.getStandardDirectionOrder().getSdoCourtList());
    }

    private static void populateSdoSection7FactsEditContent(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSection7FactsEditContent())) {
            caseDataUpdated.put("sdoSection7FactsEditContent", SECTION_7_FACTS_EDIT_CONTENT);
        }
    }

    private static void populateSdoSection7daOccuredEditContent(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoSection7daOccuredEditContent())) {
            caseDataUpdated.put("sdoSection7daOccuredEditContent", SECTION_7_DA_OCCURED_EDIT_CONTENT);
        }
    }

    private static void populateSdoLsApplicantRespondentNameCollection(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<SdoNameOfApplicant>> sdoApplicantName = new ArrayList<>();
        sdoApplicantName.add(element(SdoNameOfApplicant.builder().build()));
        List<Element<SdoNameOfRespondent>> sdoRespondentName = new ArrayList<>();
        sdoRespondentName.add(element(SdoNameOfRespondent.builder().build()));

        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoLsApplicantName())) {
            caseDataUpdated.put("sdoLsApplicantName", sdoApplicantName);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoMedicalDiscApplicantName())) {
            caseDataUpdated.put("sdoMedicalDiscApplicantName", sdoApplicantName);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoGpApplicantName())) {
            caseDataUpdated.put("sdoGpApplicantName", sdoApplicantName);
        }

        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoLsRespondentName())) {
            caseDataUpdated.put("sdoLsRespondentName", sdoRespondentName);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoMedicalDiscRespondentName())) {
            caseDataUpdated.put("sdoMedicalDiscRespondentName", sdoRespondentName);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoGpRespondentName())) {
            caseDataUpdated.put("sdoGpRespondentName", sdoRespondentName);
        }
    }

    private static void populateSdoPartiesRaisedAbuseCollection(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection = new ArrayList<>();
        sdoPartiesRaisedAbuseCollection.add(element(PartyNameDA.builder().build()));
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoPartiesRaisedAbuseCollection())) {
            caseDataUpdated.put("sdoPartiesRaisedAbuseCollection", sdoPartiesRaisedAbuseCollection);
        }
    }

    private static void populateSdoInterpreterDialectRequired(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequiredList = new ArrayList<>();
        sdoInterpreterDialectRequiredList.add(element(SdoLanguageDialect.builder().build()));
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoInterpreterDialectRequired())) {
            caseDataUpdated.put("sdoInterpreterDialectRequired", sdoInterpreterDialectRequiredList);
        }
    }

    private static void populateSdoMiamAttendingPerson(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<MiamAttendingPersonName>> sdoMiamAttendingPersons = new ArrayList<>();
        sdoMiamAttendingPersons.add(element(MiamAttendingPersonName.builder().build()));
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoMiamAttendingPerson())) {
            caseDataUpdated.put("sdoMiamAttendingPerson", sdoMiamAttendingPersons);
        }
    }

    private static void populateSdoFurtherDirectionDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<SdoFurtherDirections>> sdoFurtherDirectionDetails = new ArrayList<>();
        sdoFurtherDirectionDetails.add(element(SdoFurtherDirections.builder().build()));
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoFurtherDirectionDetails())) {
            caseDataUpdated.put("sdoFurtherDirectionDetails", sdoFurtherDirectionDetails);
        }
    }

    private static void populateSdoDioProvideOtherDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<SdoDioProvideOtherDetails>> sdoDioProvideOtherDetailList = new ArrayList<>();
        sdoDioProvideOtherDetailList.add(element(SdoDioProvideOtherDetails.builder().build()));
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoPositionStatementOtherDetails())) {
            caseDataUpdated.put("sdoPositionStatementOtherDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoMiamOtherDetails())) {
            caseDataUpdated.put("sdoMiamOtherDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoFactFindingOtherDetails())) {
            caseDataUpdated.put("sdoFactFindingOtherDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoInterpreterOtherDetails())) {
            caseDataUpdated.put("sdoInterpreterOtherDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoCafcassFileAndServeDetails())) {
            caseDataUpdated.put("sdoCafcassFileAndServeDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSafeguardingCafcassCymruDetails())) {
            caseDataUpdated.put("safeguardingCafcassCymruDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoPartyToProvideDetails())) {
            caseDataUpdated.put("sdoPartyToProvideDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoNewPartnersToCafcassDetails())) {
            caseDataUpdated.put("sdoNewPartnersToCafcassDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoSection7CheckDetails())) {
            caseDataUpdated.put("sdoSection7CheckDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoLocalAuthorityDetails())) {
            caseDataUpdated.put("sdoLocalAuthorityDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoTransferCourtDetails())) {
            caseDataUpdated.put("sdoTransferCourtDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoCrossExaminationCourtDetails())) {
            caseDataUpdated.put("sdoCrossExaminationCourtDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoWitnessStatementsCheckDetails())) {
            caseDataUpdated.put("sdoWitnessStatementsCheckDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoInstructionsFilingDetails())) {
            caseDataUpdated.put("sdoInstructionsFilingDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoMedicalDiscFilingDetails())) {
            caseDataUpdated.put("sdoMedicalDiscFilingDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoLetterFromGpDetails())) {
            caseDataUpdated.put("sdoLetterFromGpDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoLetterFromSchoolDetails())) {
            caseDataUpdated.put("sdoLetterFromSchoolDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoScheduleOfAllegationsDetails())) {
            caseDataUpdated.put("sdoScheduleOfAllegationsDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoDisClosureProceedingDetails())) {
            caseDataUpdated.put("sdoDisClosureProceedingDetails", sdoDioProvideOtherDetailList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoFactFindingOtherDetails())) {
            caseDataUpdated.put("sdoFactFindingOtherDetails", sdoDioProvideOtherDetailList);
        }
    }

    private void populateLocalAuthorityDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoLocalAuthorityList().contains(localAuthorityLetter)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoLocalAuthorityTempList()).contains(localAuthorityLetter)) {
            if (SWANSEA_COURT_NAME.equalsIgnoreCase(caseData.getCourtName())) {
                caseDataUpdated.put("sdoLocalAuthorityName", "City and County of Swansea");
            }
            if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoLocalAuthorityTextArea())) {
                caseDataUpdated.put(
                    "sdoLocalAuthorityTextArea",
                    LOCAL_AUTHORUTY_LETTER
                );
            }
        }
    }

    private static void populateSection7ChildImpactAnalysis(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().contains(section7Report)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruTempList()).contains(section7Report)) {
            caseDataUpdated.put(
                "sdoSection7EditContent",
                SECTION7_EDIT_CONTENT
            );
        }
        populateSdoSection7FactsEditContent(caseData, caseDataUpdated);
        populateSdoSection7daOccuredEditContent(caseData, caseDataUpdated);
    }

    private static void populateCafcassNextSteps(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().contains(safeguardingCafcassOnly)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruTempList()).contains(safeguardingCafcassOnly)) {
            caseDataUpdated.put(
                "sdoCafcassNextStepEditContent",
                CAFCASS_NEXT_STEPS_CONTENT
            );
        }
    }

    private static void populateCafcassCymruNextSteps(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoCafcassOrCymruList().contains(safeguardingCafcassCymru)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCafcassOrCymruTempList()).contains(safeguardingCafcassCymru)) {
            caseDataUpdated.put(
                "sdoCafcassCymruNextStepEditContent",
                CAFCASS_NEXT_STEPS_CONTENT
            );
        }
    }

    private static void populatePreambles(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoPreamblesList().contains(rightToAskCourt)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoPreamblesTempList()).contains(
            rightToAskCourt)
        ) {
            caseDataUpdated.put(
                SDO_RIGHT_TO_ASK_COURT,
                RIGHT_TO_ASK_COURT
            );
        }
        if (caseData.getStandardDirectionOrder().getSdoPreamblesList().contains(afterSecondGateKeeping)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoPreamblesTempList()).contains(afterSecondGateKeeping)) {
            caseDataUpdated.put(
                "sdoAfterSecondGatekeeping",
                AFTER_SECOND_GATEKEEPING
            );
        }
        List<Element<AddNewPreamble>> sdoAddNewPreambleCollection = new ArrayList<>();
        sdoAddNewPreambleCollection.add(element(AddNewPreamble.builder().build()));
        if (caseData.getStandardDirectionOrder().getSdoPreamblesList().contains(addNewPreamble)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoPreamblesTempList()).contains(addNewPreamble)) {
            caseDataUpdated.put("sdoAddNewPreambleCollection", sdoAddNewPreambleCollection);
        }
    }

    private static void populateParentWithCare(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoOtherList().contains(parentWithCare)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoOtherTempList()).contains(parentWithCare)) {
            caseDataUpdated.put(
                "sdoParentWithCare",
                PARENT_WITHCARE
            );
        }
    }

    private void populateSdoOrderHearingDetails(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {
        Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(
                authorisation,
                String.valueOf(caseData.getId()),
                caseData,
                hearings
            );
        HearingData hearingData = hearingDataService.generateHearingData(
            hearingDataPrePopulatedDynamicLists, caseData);
        //add hearing screen field show params
        ManageOrdersUtils.addHearingScreenFieldShowParams(hearingData, caseDataUpdated, caseData);

        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoPermissionHearingDetails(),
            SDO_PERMISSION_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoSecondHearingDetails(),
            SDO_SECOND_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoUrgentHearingDetails(),
            SDO_URGENT_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoFhdraHearingDetails(),
            SDO_FHDRA_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoDraHearingDetails(),
            SDO_DRA_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoSettlementHearingDetails(),
            SDO_SETTLEMENT_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
        populateHearingData(
            caseDataUpdated,
            hearingData,
            caseData.getStandardDirectionOrder().getSdoDirectionsForFactFindingHearingDetails(),
            SDO_DIRECTIONS_FOR_FACT_FINDING_HEARING_DETAILS,
            hearingDataPrePopulatedDynamicLists
        );
    }

    private static void populateHearingData(Map<String, Object> caseDataUpdated, HearingData hearingData,
                                            HearingData existingHearingData, String hearingKey,
                                            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (existingHearingData == null
            || existingHearingData.getHearingDateConfirmOptionEnum() == null) {
            caseDataUpdated.put(hearingKey, hearingData);
        } else {
            existingHearingData = resetHearingConfirmedDatesAndLinkedCases(
                hearingDataPrePopulatedDynamicLists,
                existingHearingData
            );
            caseDataUpdated.put(hearingKey, existingHearingData);
        }
    }

    private static void populateDocumentAndEvidenceText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(specifiedDocuments)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceTempList()).contains(
            specifiedDocuments)) {
            caseDataUpdated.put(
                "sdoSpecifiedDocuments",
                SPECIFIED_DOCUMENTS
            );
        }
        if (caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceList().contains(spipAttendance)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoDocumentationAndEvidenceTempList()).contains(
            spipAttendance)) {
            caseDataUpdated.put(
                "sdoSpipAttendance",
                SPIP_ATTENDANCE
            );
        }
    }

    private static void populateCourtText(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (caseData.getStandardDirectionOrder().getSdoCourtList().contains(SdoCourtEnum.crossExaminationEx740)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCourtTempList()).contains(
            SdoCourtEnum.crossExaminationEx740)) {
            caseDataUpdated.put("sdoCrossExaminationEx740", CROSS_EXAMINATION_EX740);
        }
        if (caseData.getStandardDirectionOrder().getSdoCourtList().contains(SdoCourtEnum.crossExaminationQualifiedLegal)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCourtTempList()).contains(
            SdoCourtEnum.crossExaminationQualifiedLegal)) {
            caseDataUpdated.put("sdoCrossExaminationQualifiedLegal", CROSS_EXAMINATION_QUALIFIED_LEGAL);
        }
        if (caseData.getStandardDirectionOrder().getSdoCourtList().contains(SdoCourtEnum.crossExaminationEx741)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCourtTempList()).contains(
            SdoCourtEnum.crossExaminationEx741)) {
            caseDataUpdated.put("sdoCrossExaminationEx741", SDO_CROSS_EXAMINATION_EX741);
        }
        if (caseData.getStandardDirectionOrder().getSdoCourtList().contains(SdoCourtEnum.crossExaminationProhibition)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoCourtTempList()).contains(
            SdoCourtEnum.crossExaminationProhibition)) {
            caseDataUpdated.put(
                "sdoCrossExaminationEditContent",
                CROSS_EXAMINATION_PROHIBITION
            );
        }
    }

    private static void populateHearingAndNextStepsText(CaseData caseData, Map<String, Object> caseDataUpdated,
                                                        List<DynamicMultiselectListElement> applicantRespondentList) {
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(nextStepsAfterGateKeeping)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(
            nextStepsAfterGateKeeping)) {
            caseDataUpdated.put("sdoNextStepsAfterSecondGK", SAFE_GUARDING_LETTER);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(hearingNotNeeded)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(hearingNotNeeded)) {
            caseDataUpdated.put("sdoHearingNotNeeded", HEARING_NOT_NEEDED);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(participationDirections)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(
            participationDirections)) {
            caseDataUpdated.put("sdoParticipationDirections", PARTICIPATION_DIRECTIONS);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(joiningInstructions)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(joiningInstructions)) {
            caseDataUpdated.put("sdoJoiningInstructionsForRH", JOINING_INSTRUCTIONS);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(updateContactDetails)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(updateContactDetails)) {
            caseDataUpdated.put("sdoUpdateContactDetails", UPDATE_CONTACT_DETAILS);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(updateContactDetails)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(updateContactDetails)) {
            caseDataUpdated.put("sdoPermissionHearingDirections", SDO_PERMISSION_HEARING);
        }
        if (caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)
            && !ElementUtils.nullSafeList(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsTempList()).contains(factFindingHearing)) {
            populateDirectionFactFindingsHearingDetails(caseData, caseDataUpdated, applicantRespondentList);
        }
    }

    private static void populateDirectionFactFindingsHearingDetails(CaseData caseData, Map<String, Object> caseDataUpdated,
                                                                    List<DynamicMultiselectListElement> applicantRespondentList) {
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoWhoMadeAllegationsText())) {
            caseDataUpdated.put(SDO_WHO_MADE_ALLEGATIONS_TEXT_FIELD, WHO_MADE_ALLEGATIONS_TEXT);
        }
        if (StringUtils.isBlank(caseData.getStandardDirectionOrder().getSdoWhoNeedsToRespondAllegationsText())) {
            caseDataUpdated.put(SDO_WHO_NEEDS_TO_RESPOND_ALLEGATIONS_TEXT_FIELD, WHO_NEEDS_TO_RESPOND_ALLEGATIONS_TEXT);
        }
        if (isEmpty(caseData.getStandardDirectionOrder().getSdoWhoMadeAllegationsList())) {
            caseDataUpdated.put(
                SDO_WHO_MADE_ALLEGATIONS_LIST,
                DynamicMultiSelectList.builder()
                    .listItems(applicantRespondentList)
                    .build()
            );
        }
        if (isEmpty(caseData.getStandardDirectionOrder().getSdoWhoNeedsToRespondAllegationsList())) {
            caseDataUpdated.put(
                SDO_WHO_NEEDS_TO_RESPOND_ALLEGATIONS_LIST,
                DynamicMultiSelectList.builder()
                    .listItems(applicantRespondentList)
                    .build()
            );
        }
    }

    private void populateCourtDynamicList(List<DynamicListElement> courtList, Map<String, Object> caseDataUpdated, CaseData caseData) {
        DynamicList courtDynamicList = DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        if (caseData.getStandardDirectionOrder().getSdoTransferApplicationCourtDynamicList() == null
            || CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoTransferApplicationCourtDynamicList().getListItems())) {
            caseDataUpdated.put(
                "sdoTransferApplicationCourtDynamicList", courtDynamicList);
        }
        if (CollectionUtils.isEmpty(caseData.getStandardDirectionOrder().getSdoDisclosureOfPapersCaseNumbers())) {
            List<Element<SdoDisclosureOfPapersCaseNumber>> elementList = new ArrayList<>();
            SdoDisclosureOfPapersCaseNumber sdoDisclosureOfPapersCaseNumbers = SdoDisclosureOfPapersCaseNumber.builder()
                .sdoDisclosureCourtList(courtDynamicList)
                .build();
            elementList.add(element(sdoDisclosureOfPapersCaseNumbers));
            caseDataUpdated.put(
                "sdoDisclosureOfPapersCaseNumbers", elementList);
        }
    }

    private List<DynamicListElement> getCourtDynamicList(String authorisation) {
        return locationRefDataService.getCourtLocations(authorisation);
    }

    public static boolean checkDirectionOnIssueOptionsSelected(CaseData caseData) {
        return !(caseData.getDirectionOnIssue().getDioPreamblesList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioLocalAuthorityList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCourtList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().isEmpty());
    }

    public void populateDirectionOnIssueFields(String authorisation, CaseData caseData, Map<String, Object> caseDataUpdated) {

        if (!caseData.getDirectionOnIssue().getDioPreamblesList().isEmpty()
            && caseData.getDirectionOnIssue().getDioPreamblesList().contains(DioPreamblesEnum.rightToAskCourt)) {
            caseDataUpdated.put("dioRightToAskCourt", DIO_RIGHT_TO_ASK);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.caseReviewAtSecondGateKeeping)) {
            caseDataUpdated.put("dioCaseReviewAtSecondGateKeeping", DIO_CASE_REVIEW);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.updateContactDetails)) {
            caseDataUpdated.put("dioUpdateContactDetails", DIO_UPDATE_CONTACT_DETAILS);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.participationDirections)) {
            caseDataUpdated.put("dioParticipationDirections", DIO_PARTICIPATION_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.permissionHearing)) {
            caseDataUpdated.put("dioPermissionHearingDirections", DIO_PERMISSION_HEARING_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().isEmpty()
            && caseData.getDirectionOnIssue().getDioHearingsAndNextStepsList().contains(
            DioHearingsAndNextStepsEnum.positionStatement)) {
            caseDataUpdated.put("dioPositionStatementDetails", DIO_POSITION_STATEMENT_DIRECTION);
        }
        if (!caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().contains(
            DioCafcassOrCymruEnum.cafcassSafeguarding)) {
            caseDataUpdated.put("dioCafcassSafeguardingIssue", DIO_SAFEGUARDING_CAFCASS);
        }
        if (!caseData.getDirectionOnIssue().getDioCafcassOrCymruList().isEmpty()
            && caseData.getDirectionOnIssue().getDioCafcassOrCymruList().contains(
            DioCafcassOrCymruEnum.cafcassCymruSafeguarding)) {
            caseDataUpdated.put("dioCafcassCymruSafeguardingIssue", DIO_SAFEGUARING_CAFCASS_CYMRU);
        }
        if (!caseData.getDirectionOnIssue().getDioOtherList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().contains(
            DioOtherEnum.parentWithCare)) {
            caseDataUpdated.put(
                "dioParentWithCare", DIO_PARENT_WITHCARE);
        }
        if (!caseData.getDirectionOnIssue().getDioOtherList().isEmpty()
            && caseData.getDirectionOnIssue().getDioOtherList().contains(
            DioOtherEnum.applicationToApplyPermission)) {

            List<Element<DioApplicationToApplyPermission>> dioApplicationToApplyPermissionList = new ArrayList<>();
            DioApplicationToApplyPermission dioApplicationToApplyPermission = DioApplicationToApplyPermission.builder()
                .applyPermissionToEditSection(DIO_APPLICATION_TO_APPLY_PERMISSION)
                .build();

            dioApplicationToApplyPermissionList.add(element(dioApplicationToApplyPermission));
            caseDataUpdated.put(
                "dioApplicationToApplyPermission", dioApplicationToApplyPermissionList);

        }

        List<DynamicListElement> courtList = getCourtDynamicList(authorisation);
        populateDioCourtDynamicList(courtList, caseDataUpdated);

    }

    private void populateDioCourtDynamicList(List<DynamicListElement> courtList, Map<String, Object> caseDataUpdated) {
        DynamicList courtDynamicList = DynamicList.builder().value(DynamicListElement.EMPTY).listItems(courtList)
            .build();
        caseDataUpdated.put(
            "dioFhdraCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioPermissionHearingCourtDynamicList", courtDynamicList);
        caseDataUpdated.put(
            "dioTransferApplicationCourtDynamicList", courtDynamicList);

        List<Element<SdoDisclosureOfPapersCaseNumber>> elementList = new ArrayList<>();
        SdoDisclosureOfPapersCaseNumber sdoDisclosureOfPapersCaseNumbers = SdoDisclosureOfPapersCaseNumber.builder()
            .sdoDisclosureCourtList(courtDynamicList)
            .build();
        elementList.add(element(sdoDisclosureOfPapersCaseNumbers));
        caseDataUpdated.put(
            "dioDisclosureOfPapersCaseNumbers", elementList);
    }

    public Map<String, Object> getDraftOrderInfo(String authorisation, CaseData caseData, DraftOrder draftOrder) throws Exception {

        Map<String, Object> caseDataMap = getDraftOrderData(authorisation, caseData, draftOrder.getOrderType());
        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, draftOrder.getIsOrderCreatedBySolicitor());
        caseDataMap.put(
            IS_HEARING_PAGE_NEEDED,
            isHearingPageNeeded(draftOrder.getOrderType(), draftOrder.getC21OrderOptions()) ? Yes : No
        );

        return caseDataMap;
    }

    private Map<String, Object> getDraftOrderData(String authorisation, CaseData caseData, CreateSelectOrderOptionsEnum orderType) throws Exception {
        return manageOrderService.generateOrderDocumentFromDocmosis(
            authorisation,
            caseData,
            orderType
        );
    }

    public Map<String, Object> getEligibleServeOrderDetails(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String eventId = callbackRequest.getEventId();
        caseDataUpdated.putAll(removeDraftOrderAndAddToFinalOrder(authorisation, caseData, eventId));
        CaseData modifiedCaseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        manageOrderService.populateServeOrderDetails(modifiedCaseData, caseDataUpdated);
        return caseDataUpdated;
    }

    public Map<String, Object> adminEditAndServeAboutToSubmit(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String eventId = callbackRequest.getEventId();
        if (WhatToDoWithOrderEnum.finalizeSaveToServeLater
            .equals(caseData.getServeOrderData().getWhatDoWithOrder())
            || YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
            caseDataUpdated.putAll(removeDraftOrderAndAddToFinalOrder(
                authorisation,
                caseData, eventId
            ));
            if (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
                CaseData modifiedCaseData = objectMapper.convertValue(
                    caseDataUpdated,
                    CaseData.class
                );
                List<Element<OrderDetails>> orderCollection = modifiedCaseData.getOrderCollection();
                caseDataUpdated.put(
                    ORDER_COLLECTION,
                    manageOrderService.serveOrder(modifiedCaseData, orderCollection)
                );
            }
        } else if (WhatToDoWithOrderEnum.saveAsDraft.equals(caseData.getServeOrderData().getWhatDoWithOrder())) {
            caseDataUpdated.putAll(updateDraftOrderCollection(caseData, authorisation, eventId));
        }
        manageOrderService.setMarkedToServeEmailNotification(caseData, caseDataUpdated);
        //PRL-4216 - save server order additional documents if any
        manageOrderService.saveAdditionalOrderDocuments(authorisation, caseData, caseDataUpdated);
        return caseDataUpdated;
    }

    public Map<String, Object> generateOrderDocumentPostValidations(String authorisation,
                                                                    CallbackRequest callbackRequest,
                                                                    List<Element<HearingData>> ordersHearingDetails,
                                                                    boolean isOrderEdited,
                                                                    CreateSelectOrderOptionsEnum orderType) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseData = updateOrderSpecificFields(caseData, callbackRequest);

        if (isNotEmpty(ordersHearingDetails)) {
            caseData.getManageOrders().setOrdersHearingDetails(
                hearingDataService.setHearingDataForSelectedHearing(authorisation, caseData, orderType));
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(orderType)) {
            Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
            caseData = manageOrderService.setHearingDataForSdo(caseData, hearings, authorisation);
        }
        if (isOrderEdited) {
            Object dynamicList = caseData.getDraftOrdersDynamicList();
            if (Event.EDIT_RETURNED_ORDER.getId().equals(callbackRequest.getEventId())) {
                dynamicList = caseData.getManageOrders().getRejectedOrdersDynamicList();
            }
            DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), dynamicList);
            caseDataUpdated.putAll(getDraftOrderInfo(authorisation, caseData, selectedOrder));
        } else {
            caseDataUpdated.putAll(getDraftOrderData(authorisation, caseData, orderType));
        }


        return caseDataUpdated;
    }

    public Map<String, Object> generateOrderDocument(String authorisation, CallbackRequest callbackRequest,
                                                     List<Element<HearingData>> ordersHearingDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData = updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
        }
        Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
        if (ordersHearingDetails != null) {
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(
                    authorisation,
                    String.valueOf(caseData.getId()),
                    caseData,
                    hearings
                );
            List<Element<HearingData>> hearingData = hearingDataService.getHearingDataForOtherOrders(
                ordersHearingDetails,
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            //PRL-4260 - hearing screen changes
            caseDataUpdated.put(ORDER_HEARING_DETAILS, hearingData);
            caseData.getManageOrders().setOrdersHearingDetails(hearingData);

            caseData.getManageOrders().setOrdersHearingDetails(
                hearingDataService.getHearingDataForSelectedHearing(caseData, hearings, authorisation));
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
            caseData = manageOrderService.setHearingDataForSdo(caseData, hearings, authorisation);
        }
        if (Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())
            || Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            DraftOrder selectedOrder = getSelectedDraftOrderDetails(
                caseData.getDraftOrderCollection(),
                caseData.getDraftOrdersDynamicList()
            );
            caseDataUpdated.putAll(getDraftOrderInfo(authorisation, caseData, selectedOrder));
        } else {
            caseDataUpdated.putAll(getDraftOrderData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        }
        return caseDataUpdated;
    }

    public Map<String, Object> prepareDraftOrderCollection(String authorisation, CallbackRequest callbackRequest) {
        manageOrderService.resetChildOptions(callbackRequest);
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseData = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
        if (caseData.getDraftOrderOptions().equals(DraftOrderOptionsEnum.draftAnOrder)
            && isHearingPageNeeded(
            caseData.getCreateSelectOrderOptions(),
            caseData.getManageOrders().getC21OrderOptions()
        )) {
            Hearings hearings = hearingService.getHearings(authorisation, String.valueOf(caseData.getId()));
            caseData.getManageOrders().setSolicitorOrdersHearingDetails(hearingDataService
                                                                            .getHearingDataForSelectedHearing(
                                                                                caseData,
                                                                                hearings,
                                                                                authorisation
                                                                            ));

            Optional<Element<HearingData>> hearingDataElement = caseData.getManageOrders()
                .getSolicitorOrdersHearingDetails()
                .stream()
                .filter(
                    e -> e.getValue().getHearingJudgeNameAndEmail() != null
                )
                .findFirst();
            JudicialUser judicialUser = null;
            if (hearingDataElement.isPresent()) {
                judicialUser = hearingDataElement.get().getValue().getHearingJudgeNameAndEmail();
            }

            RoleAssignmentDto roleAssignmentDto = RoleAssignmentDto.builder()
                .judicialUser(judicialUser)
                .build();
            roleAssignmentService.createRoleAssignment(
                authorisation,
                callbackRequest.getCaseDetails(),
                roleAssignmentDto,
                DRAFT_AN_ORDER.getName(),
                false,
                HEARING_JUDGE_ROLE
            );

        }
        List<Element<DraftOrder>> draftOrderCollection = generateDraftOrderCollection(caseData, authorisation);
        caseDataUpdated.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        caseDataUpdated.put(
            WA_ORDER_NAME_SOLICITOR_CREATED,
            getDraftOrderNameForWA(
                null != draftOrderCollection && !draftOrderCollection.isEmpty() ? draftOrderCollection.get(0).getValue() : null,
                callbackRequest.getEventId()
            )
        );
        CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
        ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
        return caseDataUpdated;
    }

    private String getDraftOrderNameForWA(DraftOrder draftOrder, String eventId) {
        if (DRAFT_AN_ORDER.getId().equalsIgnoreCase(eventId) && null != draftOrder) {
            return draftOrder.getLabelForOrdersDynamicList();
        } else {
            log.error("Error while fetching the order name for WA");
            return "";
        }
    }

    public String getApprovedDraftOrderNameForWA(CaseData caseData) {
        if (!Objects.isNull(caseData.getDraftOrdersDynamicList())) {
            return getSelectedDraftOrderDetails(
                caseData.getDraftOrderCollection(),
                caseData.getDraftOrdersDynamicList()
            )
                .getLabelForOrdersDynamicList();
        }
        return null;
    }

    public Map<String, Object> resetFields(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataUpdated;
    }

    public Map<String, Object> handlePopulateDraftOrderFields(CallbackRequest callbackRequest, String authorisation) throws Exception {

        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (DraftOrderOptionsEnum.uploadAnOrder.equals(caseData.getDraftOrderOptions())) {
            return caseDataUpdated;
        }
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));

        //PRL-4212 - Fetch & populate hearing data only in case order needs
        if (isHearingPageNeeded(
            caseData.getCreateSelectOrderOptions(),
            caseData.getManageOrders().getC21OrderOptions()
        )) {
            HearingData hearingData = manageOrderService.getHearingData(authorisation, caseData);
            caseDataUpdated.put(ORDER_HEARING_DETAILS, ElementUtils.wrapElements(hearingData));
            //add hearing screen field show params
            ManageOrdersUtils.addHearingScreenFieldShowParams(hearingData, caseDataUpdated, caseData);
        }
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            caseData.setAppointedGuardianName(manageOrderService.addGuardianDetails(caseData));
        }
        if (!(CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions()))
            && (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            || ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(caseData.getCreateSelectOrderOptions()),
            caseData))) {
            if (Objects.nonNull(caseData.getCreateSelectOrderOptions())) {
                caseData = manageOrderService.populateCustomOrderFields(
                    caseData,
                    caseData.getCreateSelectOrderOptions()
                );
            }
            if (Objects.nonNull(caseData.getManageOrders())) {
                caseDataUpdated.putAll(caseData.getManageOrders().toMap(CcdObjectMapper.getObjectMapper()));
            }
            if (Objects.nonNull(caseData.getSelectedOrder())) {
                caseDataUpdated.put(SELECTED_ORDER, BOLD_BEGIN + caseData.getSelectedOrder() + BOLD_END);
            }
            if (Objects.nonNull(caseData.getStandardDirectionOrder())) {
                caseDataUpdated.putAll(caseData.getStandardDirectionOrder().toMap(CcdObjectMapper.getObjectMapper()));
            }
        } else {
            ManageOrders manageOrders = caseData.getManageOrders();
            if (manageOrders.getC21OrderOptions() != null) {
                manageOrders = manageOrders.toBuilder().typeOfC21Order(BOLD_BEGIN + manageOrders
                        .getC21OrderOptions().getDisplayedValue() + BOLD_END)
                    .build();
                caseData = caseData.toBuilder().manageOrders(manageOrders).build();
            }
            caseData = updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
            if (Objects.nonNull(caseData.getStandardDirectionOrder())) {
                caseDataUpdated.putAll(caseData.getStandardDirectionOrder().toMap(CcdObjectMapper.getObjectMapper()));
            }
            if (Objects.nonNull(caseData.getManageOrders())) {
                caseDataUpdated.putAll(caseData.getManageOrders().toMap(CcdObjectMapper.getObjectMapper()));

            }
            caseDataUpdated.put("appointedGuardianName", caseData.getAppointedGuardianName());
            caseDataUpdated.put(DATE_ORDER_MADE, caseData.getDateOrderMade());
            CaseData caseData1 = caseData.toBuilder().build();
            if (ObjectUtils.isNotEmpty(caseData.getCreateSelectOrderOptions())) {
                caseDataUpdated.putAll(manageOrderService.getCaseData(
                    authorisation,
                    caseData1,
                    caseData.getCreateSelectOrderOptions()
                ));
            }
        }

        return caseDataUpdated;
    }

    public AboutToStartOrSubmitCallbackResponse handleSelectedOrder(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put("childOption", DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        caseDataUpdated.put(DA_ORDER_FOR_CA_CASE,
                            ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(caseData.getCreateSelectOrderOptions()),
                                                                         caseData) ? Yes : No);
        List<String> errorList = new ArrayList<>();
        if (DraftOrderOptionsEnum.uploadAnOrder.equals(caseData.getDraftOrderOptions())) {
            if ((null != caseData.getChildArrangementOrders()
                && ChildArrangementOrdersEnum.standardDirectionsOrder
                .name()
                .equalsIgnoreCase(caseData
                                      .getChildArrangementOrders()
                                      .toString()))) {
                return prohibitedOrdersForSolicitor(errorList);
            }

            caseDataUpdated.put(SELECTED_ORDER, manageOrderService.getSelectedOrderInfoForUpload(caseData));
            //PRL-4854 - Populate hearings dropdown for upload order
            caseDataUpdated.put(HEARINGS_TYPE, manageOrderService.populateHearingsDropdown(authorisation, caseData));

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        }

        if (Arrays.stream(ManageOrdersUtils.PROHIBITED_ORDER_IDS_FOR_SOLICITORS)
            .anyMatch(orderId -> orderId.equalsIgnoreCase(caseData.getCreateSelectOrderOptions().toString()))) {
            return prohibitedOrdersForSolicitor(errorList);
        }

        if (getErrorsForOrdersProhibitedForC100FL401(
            caseData,
            caseData.getCreateSelectOrderOptions(),
            errorList
        )) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(errorList).build();
        } else {
            //PRL-3254 - Populate hearing details dropdown for create order
            caseDataUpdated.put(HEARINGS_TYPE, manageOrderService.populateHearingsDropdown(authorisation, caseData));

            if (null != caseData.getManageOrders()
                && null != caseData.getManageOrders().getC21OrderOptions()) {
                caseDataUpdated.put("typeOfC21Order", BOLD_BEGIN + caseData.getManageOrders()
                    .getC21OrderOptions().getDisplayedValue() + BOLD_END);
            }

            caseDataUpdated.put(SELECTED_ORDER, null != caseData.getCreateSelectOrderOptions()
                ? BOLD_BEGIN + caseData.getCreateSelectOrderOptions().getDisplayedValue() + BOLD_END : "");
            caseDataUpdated.put(DATE_ORDER_MADE, LocalDate.now());
            caseDataUpdated.put("magistrateLastName", CollectionUtils.isNotEmpty(caseData.getMagistrateLastName())
                ? caseData.getMagistrateLastName() : Arrays.asList(element(MagistrateLastName.builder().build())));

            //PRL-4212 - Fetch & populate hearing data only in case order needs
            if (isHearingPageNeeded(
                caseData.getCreateSelectOrderOptions(),
                caseData.getManageOrders().getC21OrderOptions()
            )) {
                HearingData hearingData = manageOrderService.getHearingData(authorisation, caseData);
                caseDataUpdated.put(ORDER_HEARING_DETAILS, ElementUtils.wrapElements(hearingData));
                //add hearing screen field show params
                ManageOrdersUtils.addHearingScreenFieldShowParams(hearingData, caseDataUpdated, caseData);
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        }
    }

    private AboutToStartOrSubmitCallbackResponse prohibitedOrdersForSolicitor(List<String> errorList) {
        errorList.add("This order is not available to be drafted");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errorList)
            .build();
    }

    private List<String> validateDraftOrderDetails(CaseData caseData) {
        List<String> errorList = new ArrayList<>();
        if (ManageOrdersUtils.isHearingPageNeeded(
            caseData.getCreateSelectOrderOptions(),
            caseData.getManageOrders().getC21OrderOptions()
        )
            &&
            (Yes.equals(caseData.getManageOrders().getHasJudgeProvidedHearingDetails()))) {
            errorList.addAll(getHearingScreenValidations(
                caseData.getManageOrders().getOrdersHearingDetails(),
                caseData.getCreateSelectOrderOptions(),
                true
            ));
        }
        if (CreateSelectOrderOptionsEnum.occupation.equals(caseData.getCreateSelectOrderOptions())
            && null != caseData.getManageOrders().getFl404CustomFields()) {
            errorList.addAll(getErrorForOccupationScreen(caseData, caseData.getCreateSelectOrderOptions()));
        }

        return errorList;
    }

    private List<String> validateEditedOrderDetails(CaseData caseData, DraftOrder draftOrder) {
        List<String> errorList = new ArrayList<>();
        if (CreateSelectOrderOptionsEnum.occupation.equals(caseData.getCreateSelectOrderOptions())
            && null != caseData.getManageOrders().getFl404CustomFields()) {
            errorList.addAll(getErrorForOccupationScreen(caseData, caseData.getCreateSelectOrderOptions()));
        }
        if (isHearingPageNeeded(draftOrder.getOrderType(), draftOrder.getC21OrderOptions())) {
            //PRL-4260 - hearing screen validations
            errorList.addAll(getHearingScreenValidations(
                caseData.getManageOrders().getOrdersHearingDetails(),
                draftOrder.getOrderType(),
                (Yes.equals(draftOrder.getIsOrderCreatedBySolicitor())
                    && Yes.equals(caseData.getManageOrders().getHasJudgeProvidedHearingDetails()))
            ));
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
            errorList.addAll(getHearingScreenValidationsForSdo(caseData.getStandardDirectionOrder()));
        }
        return errorList;
    }

    private CaseData updateOrderSpecificFields(CaseData caseData, CallbackRequest callbackRequest) {
        if (caseData.getCreateSelectOrderOptions() != null
            && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
            List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
            manageOrderService.updateCaseDataWithAppointedGuardianNames(callbackRequest.getCaseDetails(), namesList);
            caseData.setAppointedGuardianName(namesList);
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
            caseData = manageOrderService.populateJudgeNames(caseData);
        }
        return caseData;
    }

    public Map<String, Object> handleDocumentGeneration(String authorisation, CallbackRequest callbackRequest) throws Exception {
        List<String> errorList = null;
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        if (DraftOrderOptionsEnum.draftAnOrder.equals(caseData.getDraftOrderOptions())
            && DRAFT_AN_ORDER.getId().equals(callbackRequest.getEventId())) {
            errorList = validateDraftOrderDetails(caseData);
            if (!errorList.isEmpty()) {
                return Map.of("errorList", errorList);
            }
            return generateOrderDocumentPostValidations(
                authorisation,
                callbackRequest,
                caseData.getManageOrders().getOrdersHearingDetails(),
                false,
                caseData.getCreateSelectOrderOptions()
            );
        } else {
            DraftOrder draftOrder = null;
            if (Event.EDIT_RETURNED_ORDER.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                draftOrder = getSelectedDraftOrderDetails(
                    caseData.getDraftOrderCollection(),
                    caseData.getManageOrders()
                        .getRejectedOrdersDynamicList()
                );
            } else {
                draftOrder = getSelectedDraftOrderDetails(
                    caseData.getDraftOrderCollection(),
                    caseData.getDraftOrdersDynamicList()
                );
            }

            if (ManageOrdersUtils.isOrderEdited(caseData, callbackRequest.getEventId())) {
                errorList = validateEditedOrderDetails(caseData, draftOrder);
                if (!errorList.isEmpty()) {
                    return Map.of("errorList", errorList);
                }
                return generateOrderDocumentPostValidations(
                    authorisation,
                    callbackRequest,
                    draftOrder.getManageOrderHearingDetails(),
                    true,
                    draftOrder.getOrderType()
                );

            } else {
                return generateOrderDocumentPostValidations(
                    authorisation,
                    callbackRequest,
                    draftOrder.getManageOrderHearingDetails(),
                    false,
                    draftOrder.getOrderType()
                );

            }
        }
    }

    public Map<String, Object> handleDocumentGenerationForaDraftOrder(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        List<Element<HearingData>> existingOrderHearingDetails = null;
        List<String> errorList = null;
        boolean isSolicitorOrdersHearings = false;
        List<String> occupationErrorList = new ArrayList<>();
        if (DraftOrderOptionsEnum.draftAnOrder.equals(caseData.getDraftOrderOptions())
            && DRAFT_AN_ORDER.getId().equals(callbackRequest.getEventId())) {
            if (isHearingPageNeeded(
                caseData.getCreateSelectOrderOptions(),
                caseData.getManageOrders().getC21OrderOptions()
            )) {
                existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
                //PRL-4335 - hearing screen validations
                //PRL-4589 - fix, validate only when hearings are available
                if (Yes.equals(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())) {
                    errorList = getHearingScreenValidations(
                        existingOrderHearingDetails,
                        caseData.getCreateSelectOrderOptions(),
                        true
                    );
                }
            }
            occupationErrorList.addAll(getErrorForOccupationScreen(caseData, caseData.getCreateSelectOrderOptions()));
        } else if ((Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
            .equalsIgnoreCase(callbackRequest.getEventId()) || Event.EDIT_AND_APPROVE_ORDER.getId()
            .equalsIgnoreCase(callbackRequest.getEventId()))) {
            DraftOrder draftOrder = getSelectedDraftOrderDetails(
                caseData.getDraftOrderCollection(),
                caseData.getDraftOrdersDynamicList()
            );
            occupationErrorList.addAll(getErrorForOccupationScreen(caseData, draftOrder.getOrderType()));
            if (isHearingPageNeeded(draftOrder.getOrderType(), draftOrder.getC21OrderOptions())) {
                if (ManageOrdersUtils.isOrderEdited(caseData, callbackRequest.getEventId())) {
                    existingOrderHearingDetails = caseData.getManageOrders().getOrdersHearingDetails();

                    //PRL-4260 - hearing screen validations
                    errorList = getHearingScreenValidations(
                        existingOrderHearingDetails,
                        draftOrder.getOrderType(),
                        isSolicitorOrdersHearings
                    );
                } else {
                    existingOrderHearingDetails = draftOrder.getManageOrderHearingDetails();
                }
            }
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(draftOrder.getOrderType())) {
                errorList = getHearingScreenValidationsForSdo(
                    caseData.getStandardDirectionOrder()
                );
            }
        }
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (populateAndReturnIfErrors(errorList, caseDataUpdated, HEARING_SCREEN_ERRORS)
            || populateAndReturnIfErrors(occupationErrorList, caseDataUpdated, OCCUPATIONAL_SCREEN_ERRORS)) {
            return caseDataUpdated;
        }
        return generateOrderDocument(
            authorisation,
            callbackRequest,
            existingOrderHearingDetails
        );
    }

    private boolean populateAndReturnIfErrors(List<String> errorList,
                                              Map<String, Object> caseDataUpdated,
                                              String errorsField) {
        if (CollectionUtils.isNotEmpty(errorList)) {
            caseDataUpdated.put(errorsField, errorList);
            return true;
        } else {
            caseDataUpdated.remove(errorsField);
            return false;
        }
    }
}
