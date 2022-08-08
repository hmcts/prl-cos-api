package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationService {

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    @Autowired
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Autowired
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Autowired
    private final CaseInviteManager caseInviteManager;

    @Autowired
    private final ObjectMapper objectMapper;

    public String getCollapsableOfSentDocuments() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add("Documents that will be sent out (if applicable to the case):");
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add("Documents that will be sent out (if applicable to the case):<br/>");
        collapsible.add("<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass and Local Authority only)</li>");
        collapsible.add("<li>Annex Z</li><li>Privacy notice</li><li>Any orders and"
                            + " hearing notices created at the initial gatekeeping stage</li></ul>");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Map<String,Object> getOrderSelectionsEnumValues(List<String> orderList, Map<String,Object> caseData) {
        for (String s : orderList) {
            caseData.put(CreateSelectOrderOptionsEnum.mapOptionFromDisplayedValue(s),"1");
        }
        return caseData;
    }

    public CaseData sendEmail(CaseDetails caseDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseData = caseInviteManager.generatePinAndSendNotificationEmail(caseData);
        log.info("Sending service of application email notifications");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            serviceOfApplicationEmailService.sendEmailC100(caseDetails);
        } else {
            serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
        }
        return caseInviteManager.generatePinAndSendNotificationEmail(caseData);
    }

    public CaseData sendPost(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info(" Sending post to the parties involved ");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            serviceOfApplicationPostService.sendDocs(caseData,authorization);
        }
        return caseData;
    }
}
