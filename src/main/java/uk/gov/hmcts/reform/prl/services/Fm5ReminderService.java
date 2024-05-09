package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Filter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_FM5_COUNT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_FM5_COUNT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5ReminderService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingApiClient hearingApiClient;
    private final Fm5NotificationService fm5NotificationService;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;



    public void sendFm5ReminderNotifications() {
        log.info("*** FM5 reminder notifications ***");
        long startTime = System.currentTimeMillis();
        //Fetch all cases in Hearing state
        List<CaseDetails> caseDetailsList = retrieveCasesInHearingState();


        if (isNotEmpty(caseDetailsList)) {
            HashMap<String, FmPendingParty> qualifiedCasesAndPartiesBeforeHearing = new HashMap<>();

            qualifiedCasesAndPartiesBeforeHearing = getQualifiedCasesAndHearingsForNotifications(caseDetailsList);

            qualifiedCasesAndPartiesBeforeHearing.forEach(
                (key, value) -> {
                    StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                        = allTabService.getStartAllTabsUpdate(String.valueOf(key));
                    Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
                    if (value.equals(FmPendingParty.NOTIFICATION_NOT_REQUIRED)) {
                        caseDataUpdated.put("fm5RemindersSent", "NOT_REQUIRED");
                    } else {
                        List<Element<NotificationDetails>> fm5ReminderNotifications = fm5NotificationService.sendFm5ReminderNotifications(
                            startAllTabsUpdateDataContent.caseData(),
                            value
                        );
                        if (isNotEmpty(fm5ReminderNotifications)) {
                            caseDataUpdated.put("fm5ReminderNotifications", fm5ReminderNotifications);
                            caseDataUpdated.put("fm5RemindersSent", "YES");
                        }
                    }

                    allTabService.submitAllTabsUpdate(
                        startAllTabsUpdateDataContent.authorisation(),
                        String.valueOf(key),
                        startAllTabsUpdateDataContent.startEventResponse(),
                        startAllTabsUpdateDataContent.eventRequestData(),
                        caseDataUpdated
                    );

                }
            );
        }

        log.info(
            "*** Time taken to send fm5 reminders - {}s ***",
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
        );
    }


    private HashMap<String, FmPendingParty> getQualifiedCasesAndHearingsForNotifications(List<CaseDetails> caseDetailsList) {

        List<String> caseIdsForHearing = new ArrayList<>();
        HashMap<String, FmPendingParty> qualifiedCasesAndPartiesBeforeHearing = new HashMap<>();
        HashMap<String, FmPendingParty> filteredCaseAndParties = new HashMap<>();

        for (CaseDetails caseDetails : caseDetailsList) {
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            log.info("Retrieved case from database, caseId {}", caseData.getId());

            filteredCaseAndParties.putAll(validateNonHearingSystemRules(caseData));

            if (!filteredCaseAndParties.get(String.valueOf(caseData.getId())).equals(FmPendingParty.NOTIFICATION_NOT_REQUIRED)) {
                caseIdsForHearing.add(String.valueOf(caseData.getId()));
            } else {
                qualifiedCasesAndPartiesBeforeHearing.put(
                    String.valueOf(caseData.getId()),
                    FmPendingParty.NOTIFICATION_NOT_REQUIRED
                );
            }
        }

        List<Hearings> hearingsForAllCaseIdsWithCourtVenue = hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(),
            caseIdsForHearing
        );
        hearingsForAllCaseIdsWithCourtVenue.stream().forEach(
            hearing -> {
                if (isFirstListedHearingAwayForDays(hearing, 19)) {
                    qualifiedCasesAndPartiesBeforeHearing.put(
                        hearing.getCaseRef(),
                        filteredCaseAndParties.get(hearing.getCaseRef())
                    );
                }
            }
        );
        return qualifiedCasesAndPartiesBeforeHearing;
    }

    private HashMap<String, FmPendingParty> validateNonHearingSystemRules(CaseData caseData) {
        HashMap<String, FmPendingParty> caseIdPendingPartyMapping = new HashMap<>();
        //if consent order is present, none to remind
        if (null != caseData.getDraftConsentOrderFile()) {
            caseIdPendingPartyMapping.put(String.valueOf(caseData.getId()), FmPendingParty.NOTIFICATION_NOT_REQUIRED);
            return caseIdPendingPartyMapping;
        }

        //if no emergency care proceedings, none to remind
        if (null != caseData.getMiamPolicyUpgradeDetails()
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuChildInvolvedInMiam())) {
            caseIdPendingPartyMapping.put(String.valueOf(caseData.getId()), FmPendingParty.NOTIFICATION_NOT_REQUIRED);
            return caseIdPendingPartyMapping;
        }

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsElemList = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsElemList = new ArrayList<>();

        if (null != caseData.getDocumentManagementDetails()) {
            legalProfQuarantineDocsElemList = caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList();
            courtStaffQuarantineDocsElemList = caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList();
        }

        List<Element<QuarantineLegalDoc>> legalProfQuarantineUploadedDocsElemList = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> courtStaffQuarantineUploadedDocsElemList = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> restrictedDocumentsElemList = new ArrayList<>();

        if (null != caseData.getReviewDocuments()) {
            legalProfQuarantineUploadedDocsElemList = caseData.getReviewDocuments().getLegalProfUploadDocListDocTab();
            courtStaffQuarantineUploadedDocsElemList = caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab();
            restrictedDocumentsElemList = caseData.getReviewDocuments().getRestrictedDocuments();
        }

        //if AOH/C1A document is available, none to remind
        if (isAohAvailable(
            caseData,
            legalProfQuarantineDocsElemList,
            legalProfQuarantineUploadedDocsElemList,
            restrictedDocumentsElemList
        )) {
            caseIdPendingPartyMapping.put(String.valueOf(caseData.getId()), FmPendingParty.NOTIFICATION_NOT_REQUIRED);
            return caseIdPendingPartyMapping;
        }

        //check & evaluate whom to send fm5 reminders
        caseIdPendingPartyMapping.put(String.valueOf(caseData.getId()), fetchFm5DocsSubmissionPendingParties(
            caseData,
            legalProfQuarantineDocsElemList,
            courtStaffQuarantineDocsElemList,
            legalProfQuarantineUploadedDocsElemList,
            courtStaffQuarantineUploadedDocsElemList
        ));
        return caseIdPendingPartyMapping;

    }

    private List<CaseDetails> retrieveCasesInHearingState() {

        SearchResultResponse response = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);

            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken,
                s2sToken,
                CASE_TYPE,
                searchString
            );

            response = objectMapper.convertValue(
                searchResult,
                SearchResultResponse.class
            );
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param ", e);
        }

        if (null != response) {
            log.info("Total no. of cases retrieved {}", response.getTotal());
            return response.getCases();
        }
        return Collections.emptyList();
    }

    private QueryParam buildCcdQueryParam() {
        //C100 cases where fm5 reminders are not sent already
        List<Should> shoulds = List.of(Should.builder()
                                             .match(Match.builder()
                                                        .caseTypeOfApplication("C100")
                                                        //.fm5RemindersSent(No)
                                                        .build())
                                             .build());

        //Hearing state
        StateFilter stateFilter = StateFilter.builder()
            .should(List.of(Should.builder().match(Match.builder()
                                                       .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
                                                       .build())
                                .build()))
            .build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        LastModified lastModified = LastModified.builder().gte(LocalDateTime.now().minusDays(10).toString()).build();
        Range range = Range.builder().lastModified(lastModified).build();
        Filter filter = Filter.builder().range(range).build();

        Bool finalFilter = Bool.builder()
            .filter(filter)
            .should(shoulds)
            .minimumShouldMatch(1)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }

    public boolean isFirstListedHearingAwayForDays(Hearings hearings,
                                                   long days) {
        if (null != hearings) {
            List<HearingDaySchedule> sortedHearingDaySchedules = nullSafeCollection(hearings.getCaseHearings()).stream()
                .filter(eachHearing -> eachHearing.getHmcStatus().equals(LISTED)
                    && null != eachHearing.getHearingDaySchedule())
                .map(CaseHearing::getHearingDaySchedule)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(
                    HearingDaySchedule::getHearingStartDateTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

            if (CollectionUtils.isNotEmpty(sortedHearingDaySchedules)) {
                return LocalDate.from(LocalDateTime.now()).plusDays(days)
                    .equals(LocalDate.from(sortedHearingDaySchedules.get(0).getHearingStartDateTime()));
            }
        }
        return false;
    }

    private  boolean isAohAvailable(CaseData caseData,
                                    List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsElemList,
                                    List<Element<QuarantineLegalDoc>> legalProfQuarantineUploadedDocsElemList,
                                    List<Element<QuarantineLegalDoc>> restrictedDocumentsElemList) {

        if (null != caseData.getC1ADocument() || null != caseData.getC1AWelshDocument()) {
            return true;
        }

        if (isNotEmpty(legalProfQuarantineDocsElemList)
            && checkByCategoryRespondentC1AApplication(legalProfQuarantineDocsElemList)) {
            return true;
        }

        if (isNotEmpty(legalProfQuarantineUploadedDocsElemList)
            && checkByCategoryRespondentC1AApplication(legalProfQuarantineUploadedDocsElemList)) {
            return true;
        }

        return isNotEmpty(restrictedDocumentsElemList)
            && checkByCategoryRespondentC1AApplication(restrictedDocumentsElemList);
    }

    private boolean checkByCategoryRespondentC1AApplication(List<Element<QuarantineLegalDoc>> quarantineDocsElemList) {
        return quarantineDocsElemList.stream()
            .map(Element::getValue)
            .anyMatch(doc -> RESPONDENT_C1A_APPLICATION.equals(doc.getCategoryId()));
    }

    private FmPendingParty fetchFm5DocsSubmissionPendingParties(CaseData caseData,
                                                                 List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsElemList,
                                                                 List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsElemList,
                                                                 List<Element<QuarantineLegalDoc>> legalProfQuarantineUploadedDocsElemList,
                                                                 List<Element<QuarantineLegalDoc>> courtStaffQuarantineUploadedDocsElemList) {

        Map<String,Long> countMap = new HashMap<>();
        countMap.put(APPLICANT_FM5_COUNT,0L);
        countMap.put(RESPONDENT_FM5_COUNT,0L);

        if (isNotEmpty(legalProfQuarantineDocsElemList)) {
            checkByCategoryFm5StatementsAndParty(legalProfQuarantineDocsElemList, countMap);
        }

        if (null != courtStaffQuarantineDocsElemList && !courtStaffQuarantineDocsElemList.isEmpty()) {
            checkByCategoryFm5StatementsAndParty(courtStaffQuarantineDocsElemList, countMap);
        }

        if (null != legalProfQuarantineUploadedDocsElemList && !legalProfQuarantineUploadedDocsElemList.isEmpty()) {
            checkByCategoryFm5StatementsAndParty(legalProfQuarantineUploadedDocsElemList, countMap);
        }

        if (null != courtStaffQuarantineUploadedDocsElemList && !courtStaffQuarantineUploadedDocsElemList.isEmpty()) {
            checkByCategoryFm5StatementsAndParty(courtStaffQuarantineUploadedDocsElemList, countMap);
        }

        if (countMap.get(APPLICANT_FM5_COUNT) < caseData.getApplicants().size()
            && countMap.get(RESPONDENT_FM5_COUNT) < caseData.getRespondents().size()) {
            return FmPendingParty.BOTH;
        } else if (countMap.get(APPLICANT_FM5_COUNT) < caseData.getApplicants().size()
            && countMap.get(RESPONDENT_FM5_COUNT) >= caseData.getRespondents().size()) {
            return FmPendingParty.APPLICANT;
        } else if (countMap.get(APPLICANT_FM5_COUNT) >= caseData.getApplicants().size()
            && countMap.get(RESPONDENT_FM5_COUNT) < caseData.getRespondents().size()) {
            return FmPendingParty.RESPONDENT;
        }

        return FmPendingParty.NOTIFICATION_NOT_REQUIRED;
    }

    private void checkByCategoryFm5StatementsAndParty(List<Element<QuarantineLegalDoc>> quarantineDocsElemList,
                                                      Map<String,Long> countMap) {
        long applicantCount =  quarantineDocsElemList.stream()
            .map(Element::getValue)
            .filter(doc -> FM5_STATEMENTS.equalsIgnoreCase(doc.getCategoryId())
                && DocumentPartyEnum.APPLICANT.getDisplayedValue().equals(doc.getDocumentParty()))
            .count();
        countMap.put(APPLICANT_FM5_COUNT, countMap.get(APPLICANT_FM5_COUNT) + applicantCount);

        long respondentCount = quarantineDocsElemList.stream()
            .map(Element::getValue)
            .filter(doc -> FM5_STATEMENTS.equalsIgnoreCase(doc.getCategoryId())
                && DocumentPartyEnum.RESPONDENT.getDisplayedValue().equals(doc.getDocumentParty()))
            .count();
        countMap.put(RESPONDENT_FM5_COUNT, countMap.get(RESPONDENT_FM5_COUNT) + respondentCount);
    }
}
