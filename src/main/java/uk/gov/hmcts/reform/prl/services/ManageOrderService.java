package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String c21TDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String c21DraftFile;

    public CaseData getUpdatedCaseData(CaseData caseData) {

        return CaseData.builder().childrenList(getChildInfoFromCaseData(caseData))
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public Map<String,String> getOrderTemplateAndFile(CreateSelectOrderOptionsEnum selectedOrder){
        Map<String,String> fieldsMap =new HashMap();
        switch(selectedOrder){
            case blankOrderOrDirections:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                break;
            case standardDirectionsOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE,"");
                fieldsMap.put(PrlAppsConstants.FILE_NAME, "");
                break;
            default:
                  break;
        }
        return fieldsMap;
    }

    private String getSelectedOrderInfo(CaseData caseData) {
        StringBuilder slectedOrder = new StringBuilder();
        slectedOrder.append(caseData.getApplicantCaseName());
        slectedOrder.append("\n\n");
        slectedOrder.append(caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)
                                ? String.format("Family Man ID: ", caseData.getFl401FamilymanCaseNumber())
                                : String.format("Family Man ID: ", caseData.getFamilymanCaseNumber()));
        slectedOrder.append("\n\n");
        slectedOrder.append(caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
                                ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
                                : caseData.getChildArrangementOrders().getDisplayedValue());
        slectedOrder.append("\n\n");
        return slectedOrder.toString();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {
        List<Child> children = new ArrayList<>();
        if (caseData.getChildren() != null) {
            children = caseData.getChildren().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
            builder.append("\n");
        }
        return builder.toString();
    }
}
