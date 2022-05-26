package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationService {

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    @Autowired
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Autowired
    private final CaseInviteManager caseInviteManager;

    @Autowired
    private final ObjectMapper objectMapper;

    public Map<String, Object> populateHeader(CaseData caseData, Map<String,Object> caseDataUpdated) {
        caseDataUpdated.put("serviceOfApplicationHeader", getHeaderInfo(caseData));
        return caseDataUpdated;
    }

    public String getCollapsableOfSentDocuments() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add("Documents that will be sent out (if applicable to the case):");
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add("<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass and Local Authority only)</li>");
        collapsible.add("<li>Annex Z</li><li>Privacy notice</li><li>Any orders and"
                            + " hearing notices created at the initial gatekeeping stage</li></ul>");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Map<String,Object> getOrderSelectionsEnumValues(List<String> orderList, Map<String,Object> caseData) {
        for (int i = 0;i < orderList.size();i++) {
            caseData = getUpdatedCaseData(orderList.get(i),caseData);
        }
        return caseData;
    }

    private Map<String,Object> getUpdatedCaseData(String selectedOrder, Map<String,Object> caseData) {

        switch (selectedOrder) {

            case "Standard directions order":
                caseData.put("option1","1");
                break;
            case "Blank order or directions (C21)":
                caseData.put("option2","1");
                break;
            case "Blank order or directions (C21) - to withdraw application":
                caseData.put("option3","1");
                break;
            case "Child arrangements, specific issue or prohibited steps order (C43)":
                caseData.put("option4","1");
                break;
            case "Parental responsibility order (C45A)":
                caseData.put("option5","1");
                break;
            case "Special guardianship order (C43A)":
                caseData.put("option6","1");
                break;
            case "Notice of proceedings (C6) (Notice to parties)":
                caseData.put("option7","1");
                break;
            case "Notice of proceedings (C6a) (Notice to non-parties)":
                caseData.put("option8","1");
                break;
            case "Transfer of case to another court (C49)":
                caseData.put("option9","1");
                break;
            case "Appointment of a guardian (C47A)":
                caseData.put("option10","1");
                break;
            case "Non-molestation order (FL404A)":
                caseData.put("option11","1");
                break;
            case "Occupation order (FL404)":
                caseData.put("option12","1");
                break;
            case "Power of arrest (FL406)":
                caseData.put("option13","1");
                break;
            case "Amended, discharged or varied order (FL404B)":
                caseData.put("option14","1");
                break;
            case "General form of undertaking (N117)":
                caseData.put("option15","1");
                break;
            case "Notice of proceedings (FL402)":
                caseData.put("option16","1");
                break;
            case "Other (upload an order)":
                caseData.put("option17","1");
                break;
            case "Blank order (FL404B)":
                caseData.put("option18","1");
                break;
            default:
                break;
        }
        return caseData;
    }

    private String getHeaderInfo(CaseData caseData) {
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("Case Name: " + caseData.getApplicantCaseName());
        headerInfo.append("\n\n");
        headerInfo.append(getFamilyManNumber(caseData));
        headerInfo.append("\n\n");
        return headerInfo.toString();
    }

    private String getFamilyManNumber(CaseData caseData) {
        if (caseData.getFl401FamilymanCaseNumber() == null && caseData.getFamilymanCaseNumber() == null) {
            return FAMILY_MAN_ID;
        }
        return FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            ? FAMILY_MAN_ID + caseData.getFl401FamilymanCaseNumber()
            : FAMILY_MAN_ID + caseData.getFamilymanCaseNumber();
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
        return caseData;
    }
}
