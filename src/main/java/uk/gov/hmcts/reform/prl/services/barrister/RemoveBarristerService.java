package uk.gov.hmcts.reform.prl.services.barrister;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemoveBarristerService {
    public static final String NO_BARRISTER_FOUND_ERROR = "You do not barrister anyone in this case.";
    public static final String CASE_NOT_REPRESENTED_BY_BARRISTER_ERROR = "This case is not represented by barrister anymore.";
    public static final String REMOVE_LEGAL_REPRESENTATIVE_AND_PARTIES_LIST = "removeLegalRepAndPartiesList";

    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> populateAboutToStartAdminRemoveBarrister(CallbackRequest callbackRequest,
                                                                        List<String> errorList) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        DynamicMultiSelectList removeLegalRepAndPartiesList
            = dynamicMultiSelectListService.getRemoveLegalRepAndPartiesList(caseData);

        if (removeLegalRepAndPartiesList.getListItems().isEmpty()) {
            errorList.add(CASE_NOT_REPRESENTED_BY_BARRISTER_ERROR);
        } else {
            caseDataUpdated.put(REMOVE_LEGAL_REPRESENTATIVE_AND_PARTIES_LIST, removeLegalRepAndPartiesList);
        }
        return caseDataUpdated;
    }
}
