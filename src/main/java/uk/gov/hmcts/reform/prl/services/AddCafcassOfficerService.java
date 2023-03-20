package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_AND_CAFCASS_OFFICER_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_DETAILS_TABLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerService {

    private final ObjectMapper objectMapper;

    private final ApplicationsTabService applicationsTabService;

    public Map<String, Object> populateCafcassOfficerDetails(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = caseData.getChildAndCafcassOfficers();
        resetExistingCafcassOfficerDetails(caseData);
        for (Element<ChildAndCafcassOfficer> cafcassOfficer : childAndCafcassOfficers) {
            caseDataUpdated.putAll(populateCafcassOfficerForCA(caseData, cafcassOfficer));
        }
        caseDataUpdated.put(CHILD_AND_CAFCASS_OFFICER_DETAILS, applicationsTabService.prePopulateChildAndCafcassOfficerDetails(caseData));
        return caseDataUpdated;
    }

    private static void resetExistingCafcassOfficerDetails(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren();
        if (children != null) {
            caseData.getChildren().stream().forEach(childElement -> {
                Child amendedChild = childElement.getValue().toBuilder()
                    .cafcassOfficerName(BLANK_STRING)
                    .cafcassOfficerPosition(null)
                    .cafcassOfficerOtherPosition(BLANK_STRING)
                    .cafcassOfficerEmailAddress(BLANK_STRING)
                    .cafcassOfficerPhoneNo(BLANK_STRING)
                    .build();
                children.set(children.indexOf(childElement), element(childElement.getId(), amendedChild));
            });
        }
    }

    private Map<String, Object> populateCafcassOfficerForCA(CaseData caseData,
                                            Element<ChildAndCafcassOfficer> cafcassOfficer) {
        Map<String, Object> childDetailsMap = new HashMap<>();
        List<Element<Child>> children = caseData.getChildren();
        children.stream()
            .filter(child -> Objects.equals(child.getId().toString(), cafcassOfficer.getValue().getChildId()))
            .findFirst()
            .ifPresent(child -> {
                Child amendedChild = child.getValue().toBuilder()
                    .cafcassOfficerName(cafcassOfficer.getValue().getCafcassOfficerName())
                    .cafcassOfficerPosition(cafcassOfficer.getValue().getCafcassOfficerPosition())
                    .cafcassOfficerOtherPosition(cafcassOfficer.getValue().getCafcassOfficerOtherPosition())
                    .cafcassOfficerEmailAddress(cafcassOfficer.getValue().getCafcassOfficerEmailAddress())
                    .cafcassOfficerPhoneNo(cafcassOfficer.getValue().getCafcassOfficerPhoneNo())
                    .build();
                children.set(children.indexOf(child), element(child.getId(), amendedChild));
            });
        childDetailsMap.put(CHILDREN, children);
        childDetailsMap.put(CHILD_DETAILS_TABLE, applicationsTabService.getChildDetails(caseData));

        return childDetailsMap;
    }
}
