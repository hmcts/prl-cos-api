package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DISPLAY_LEGAL_REP_OPTION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServiceOfDocumentsService {

    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    public Map<String, Object> aboutToStart(String authorisation,
                                            CallbackRequest callbackRequest) {

        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataMap.put(DISPLAY_LEGAL_REP_OPTION, CaseUtils.isCitizenCase(caseData) ? "No" : "Yes");
        caseDataMap.put(
            MISSING_ADDRESS_WARNING_TEXT,
            serviceOfApplicationService.checkIfPostalAddressMissedForRespondentAndOtherParties(caseData)
        );
        caseDataMap.put("sodDocumentsList", List.of(element(DocumentsDynamicList.builder()
                                                                .documentsList(sendAndReplyService.getCategoriesAndDocuments(
                                                                    authorisation,
                                                                    String.valueOf(caseData.getId())
                                                                )).build())));
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService.getOtherPeopleMultiSelectList(
            caseData);
        caseDataMap.put(SOA_OTHER_PARTIES, DynamicMultiSelectList.builder().listItems(otherPeopleList).build());
        caseDataMap.put(
            SOA_OTHER_PEOPLE_PRESENT_IN_CASE,
            CollectionUtils.isNotEmpty(otherPeopleList) ? YesOrNo.Yes : YesOrNo.No
        );
        caseDataMap.put(SOA_RECIPIENT_OPTIONS, serviceOfApplicationService.getCombinedRecipients(caseData));

        return caseDataMap;
    }
}
