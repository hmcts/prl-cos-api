package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

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
        headerInfo.append(getFamilyManNumber(caseData));
        headerInfo.append("\n\n");
        return headerInfo.toString();
    }

    private String getFamilyManNumber(CaseData caseData) {
        if (caseData.getFl401FamilymanCaseNumber() == null && caseData.getFamilymanCaseNumber() == null) {
            return FAMILY_MAN_ID;
        }
        return caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)
            ? FAMILY_MAN_ID + caseData.getFl401FamilymanCaseNumber()
            : FAMILY_MAN_ID + caseData.getFamilymanCaseNumber();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {

        StringBuilder builder = new StringBuilder();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Child> children = caseData.getChildren().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (int i = 0; i < children.size(); i++) {
                Child child = children.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
                builder.append("\n");
            }

        } else {
            builder.append(getFl401ChildrenString(caseData));
        }
        return builder.toString();
    }

    private String getFl401ChildrenString(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        if (ofNullable(caseData.getApplicantChildDetails()).isPresent()) {
            List<ApplicantChild> children = unwrapElements(caseData.getApplicantChildDetails());
            for (int i = 0; i < children.size(); i++) {
                ApplicantChild child = children.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getFullName()));
                builder.append("\n");
            }
        }
        if (ofNullable(caseData.getHome()).isPresent() && ofNullable(caseData.getHome().getChildren()).isPresent()) {
            List<ChildrenLiveAtAddress> childrenInHome = caseData.getHome().getChildren().stream()
                .map(Element::getValue).collect(Collectors.toList());

            for (int i = 0; i < childrenInHome.size(); i++) {
                ChildrenLiveAtAddress child = childrenInHome.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getChildFullName()));
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
