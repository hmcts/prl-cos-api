package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerService {

    private final ApplicationsTabService applicationsTabService;

    /*public List<Element<ChildAndCafcassOfficer>> prePopulateChildName(CaseData caseData) {
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = null;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            childAndCafcassOfficers = prePopulateChildNameForCA(caseData, childAndCafcassOfficers);
        }
        return childAndCafcassOfficers;
    }*/

    public void populateCafcassOfficerDetails(CaseData caseData, Map<String, Object> caseDataUpdated,
                                              Element<ChildAndCafcassOfficer> cafcassOfficer) {
        if (caseDataUpdated == null) {
            caseDataUpdated = new HashMap<>();
        }
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            populateCafcassOfficerForCA(caseData, caseDataUpdated, cafcassOfficer);
        }
    }

    private void populateCafcassOfficerForCA(CaseData caseData, Map<String, Object> caseDataUpdated,
                                            Element<ChildAndCafcassOfficer> cafcassOfficer) {
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
        caseDataUpdated.put("children", children);
        caseDataUpdated.put("childDetailsTable", applicationsTabService.getChildDetails(caseData));
    }
}
