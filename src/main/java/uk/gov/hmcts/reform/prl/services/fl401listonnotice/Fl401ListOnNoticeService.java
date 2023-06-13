package uk.gov.hmcts.reform.prl.services.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_LIST_ON_NOTICE_FL404B_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_LISTONNOTICE_HEARINGDETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_LIST_ON_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LINKED_CASES_LIST;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
public class Fl401ListOnNoticeService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    HearingDataService hearingDataService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> prePopulateHearingPageDataForFl401ListOnNotice(String authorisation, CaseData caseData) {
        log.info("Casestatus before prePopulateHearingPageDataForFl401ListOnNotice: {} ", caseData.getState());

        Map<String, Object> caseDataUpdated = new HashMap<>();
        List<Element<HearingData>> existingFl401ListOnNoticeHearingDetails = caseData.getFl401ListOnNotice().getFl401ListOnNoticeHearingDetails();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorisation, String.valueOf(caseData.getId()), caseData);
        String isCaseWithOutNotice = String.valueOf(Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent()
                                                                   .getOrderWithoutGivingNotice())
                                                        ? Yes : No);
        caseDataUpdated.put(FL401_CASE_WITHOUT_NOTICE, isCaseWithOutNotice);

        if (caseDataUpdated.containsKey(FL401_LISTONNOTICE_HEARINGDETAILS)) {
            caseDataUpdated.put(
                FL401_LISTONNOTICE_HEARINGDETAILS,
                hearingDataService.getHearingData(existingFl401ListOnNoticeHearingDetails,hearingDataPrePopulatedDynamicLists,caseData));
        } else {
            caseDataUpdated.put(
                FL401_LISTONNOTICE_HEARINGDETAILS,
                ElementUtils.wrapElements(hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists, caseData)));

        }
        List<DynamicListElement> linkedCasesList = hearingDataService.getLinkedCases(authorisation, caseData);
        caseDataUpdated.put(
            LINKED_CASES_LIST,
            hearingDataService.getDynamicList(linkedCasesList));

        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        caseDataUpdated.put(LEGAL_ADVISER_LIST, DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
            .build());
        log.info("Casestatus after end of the prePopulateHearingPageDataForFl401ListOnNotice: {} ", caseData.getState());
        return caseDataUpdated;
    }

    public Map<String, Object> generateFl404bDocument(String authorisation, CaseData caseData) throws Exception {

        log.info("Casestatus before generating doc generateFl404bDocument: {} ", caseData.getState());

        Map<String, Object> caseDataUpdated = new HashMap<>();
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DA_LIST_ON_NOTICE_FL404B_DOCUMENT,
            false
        );
        caseDataUpdated.put(FL401_LIST_ON_NOTICE_DOCUMENT, document);
        log.info("Casestatus after generating doc generateFl404bDocument: {} ", caseData.getState());

        return caseDataUpdated;
    }

    public Map<String, Object> fl401ListOnNoticeSubmission(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        log.info("case type of application::: {}",caseData.getCaseTypeOfApplication());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("Case status before updating the tabs from casedataUpdated: {} ", caseDataUpdated.get("state"));
        caseDataUpdated.put(FL401_LISTONNOTICE_HEARINGDETAILS, null);
        AllocatedJudge allocatedJudge = allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated,
                                                                                       caseData.getLegalAdviserList(), refDataUserService);
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        log.info("Casestatus before updating the tabs: {} ", caseData.getState());
        caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));
        log.info("Casestatus after updating the tabs: {} ", caseData.getState());
        log.info("Casestatus after updating the tabs from casedataUpdated: {} ", caseDataUpdated.get("state"));
        caseDataUpdated.put(FL401_LISTONNOTICE_HEARINGDETAILS, hearingDataService
            .getHearingData(caseData.getFl401ListOnNotice().getFl401ListOnNoticeHearingDetails(),null,caseData));
        caseDataUpdated.put("caseTypeOfApplication", CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataUpdated;
    }
}
