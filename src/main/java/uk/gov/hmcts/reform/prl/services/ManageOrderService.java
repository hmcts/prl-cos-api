package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("manageOrderHeader1", getHeaderInfo(caseData));
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return CaseData.builder().childrenList(getChildInfoFromCaseData(caseData))
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    private String getSelectedOrderInfo(CaseData caseData) {
        StringBuilder selectedOrder = new StringBuilder();
        selectedOrder.append(caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
                                 ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
                                 : caseData.getChildArrangementOrders().getDisplayedValue());
        selectedOrder.append("\n\n");
        return selectedOrder.toString();
    }

    private String getHeaderInfo(CaseData caseData) {
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("Case Name: " + caseData.getApplicantCaseName());
        headerInfo.append("\n\n");
        headerInfo.append(caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)
                              ? "Family Man ID: " + caseData.getFl401FamilymanCaseNumber()
                              : "Family Man ID: " + caseData.getFamilymanCaseNumber());
        headerInfo.append("\n\n");
        return headerInfo.toString();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {
        List<Child> children = caseData.getChildren().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
            builder.append("\n");
        }
        return builder.toString();
    }
}
