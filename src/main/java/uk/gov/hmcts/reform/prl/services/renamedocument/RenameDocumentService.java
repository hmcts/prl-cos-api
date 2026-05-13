package uk.gov.hmcts.reform.prl.services.renamedocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityCheckService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISPLAY_LEGAL_REP_OPTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RenameDocumentService {

    public static final String GOV_NOTIFY_TEMPLATE = "govNotifyTemplate";
    public static final String SEND_GRID_TEMPLATE = "sendGridTemplate";
    public static final String SERVED_PARTY = "servedParty";
    private static final String LETTER_TYPE = "Documents";
    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final UserService userService;
    private final AllTabServiceImpl allTabService;
    private final DocumentLanguageService documentLanguageService;
    private final EmailService emailService;
    private final BulkPrintService bulkPrintService;
    private final ConfidentialityCheckService confidentialityCheckService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    public Map<String, Object> handleAboutToStart(String authorisation,
                                                  CallbackRequest callbackRequest) {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataMap.put(DISPLAY_LEGAL_REP_OPTION, CaseUtils.isCitizenCase(caseData) ? "No" : "Yes");
        caseDataMap.put(
            MISSING_ADDRESS_WARNING_TEXT,
            serviceOfApplicationService.checkIfPostalAddressMissedForRespondentAndOtherParties(caseData)
        );
        caseDataMap.put(
            "renameDocumentsList", List.of(element(DocumentsDynamicList.builder()
                                                    .documentsList(sendAndReplyService.getCategoriesAndDocuments(
                                                        authorisation,
                                                        String.valueOf(caseData.getId())
                                                    )).build()))
        );
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
