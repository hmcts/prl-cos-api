package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerService {

    public static final String CHILD_DETAILS_TABLE = "childDetailsTable";
    public static final String FL_401_CHILD_DETAILS_TABLE = "fl401ChildDetailsTable";
    public static final String APPLICANT_FAMILY_TABLE = "applicantFamilyTable";
    public static final String DOES_APPLICANT_HAVE_CHILDREN = "doesApplicantHaveChildren";
    public static final String APPLICANT_CHILD = "applicantChild";
    private final ApplicationsTabService applicationsTabService;

    public List<Element<ChildAndCafcassOfficer>> prePopulateChildName(CaseData caseData) {
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = null;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            childAndCafcassOfficers = prePopulateChildNameForCA(caseData, childAndCafcassOfficers);
        } else {
            childAndCafcassOfficers = prePopulateChildNameForDA(caseData, childAndCafcassOfficers);
        }
        return childAndCafcassOfficers;
    }

    private List<Element<ChildAndCafcassOfficer>> prePopulateChildNameForDA(CaseData caseData,
                                                                            List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers) {
        if (YesOrNo.Yes.equals(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren())) {
            childAndCafcassOfficers = new ArrayList<>();
            for (Element<ApplicantChild> applicantChildElement : caseData.getApplicantChildDetails()) {
                ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                    .childId(applicantChildElement.getId().toString())
                    .childName(applicantChildElement.getValue().getFullName()).build();
                childAndCafcassOfficers.add(element(childAndCafcassOfficer));
            }
        }
        return childAndCafcassOfficers;
    }

    private List<Element<ChildAndCafcassOfficer>> prePopulateChildNameForCA(CaseData caseData,
                                                                            List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers) {
        if (caseData.getChildren() != null && !caseData.getChildren().isEmpty()) {
            childAndCafcassOfficers = new ArrayList<>();
            for (Element<Child> childElement : caseData.getChildren()) {
                ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                    .childId(childElement.getId().toString())
                    .childName(childElement.getValue().getFirstName() + " " + childElement.getValue().getLastName()).build();
                childAndCafcassOfficers.add(element(childAndCafcassOfficer));
            }
        }
        return childAndCafcassOfficers;
    }

    public void populateCafcassOfficerDetails(CaseData caseData, Map<String, Object> caseDataUpdated,
                                              Element<ChildAndCafcassOfficer> cafcassOfficer) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            populateCafcassOfficerForCA(caseData, caseDataUpdated, cafcassOfficer);
        } else {
            populateCafcassOfficerForDA(caseData, caseDataUpdated, cafcassOfficer);
        }
    }

    private void populateCafcassOfficerForDA(CaseData caseData, Map<String, Object> caseDataUpdated,
                                            Element<ChildAndCafcassOfficer> cafcassOfficer) {
        if (YesOrNo.Yes.equals(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren())) {
            List<Element<ApplicantChild>> applicantChildren = caseData.getApplicantChildDetails();
            applicantChildren.stream()
                .filter(applicantChild -> Objects.equals(
                    applicantChild.getId().toString(),
                    cafcassOfficer.getValue().getChildId()
                ))
                .findFirst()
                .ifPresent(applicantChild -> {
                    ApplicantChild amendedApplicantChild = applicantChild.getValue().toBuilder()
                        .cafcassOfficerAdded(cafcassOfficer.getValue().getCafcassOfficerName() != null ? YesOrNo.Yes : YesOrNo.No)
                        .cafcassOfficerName(cafcassOfficer.getValue().getCafcassOfficerName())
                        .cafcassOfficerEmailAddress(cafcassOfficer.getValue().getCafcassOfficerEmailAddress())
                        .cafcassOfficerPhoneNo(cafcassOfficer.getValue().getCafcassOfficerPhoneNo())
                        .build();
                    applicantChildren.set(
                        applicantChildren.indexOf(applicantChild),
                        element(applicantChild.getId(), amendedApplicantChild)
                    );
                });
            Map<String, Object> applicantFamilyMap = applicationsTabService.getApplicantsFamilyDetails(caseData);
            caseDataUpdated.put(APPLICANT_FAMILY_TABLE, applicantFamilyMap);
            if (YesOrNo.Yes.equals(applicantFamilyMap.get(DOES_APPLICANT_HAVE_CHILDREN))) {
                caseDataUpdated.put(FL_401_CHILD_DETAILS_TABLE, applicantFamilyMap.get(APPLICANT_CHILD));
            }
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
                    .cafcassOfficerEmailAddress(cafcassOfficer.getValue().getCafcassOfficerEmailAddress())
                    .cafcassOfficerPhoneNo(cafcassOfficer.getValue().getCafcassOfficerPhoneNo())
                    .build();
                children.set(children.indexOf(child), element(child.getId(), amendedChild));
            });
        caseDataUpdated.put(CHILD_DETAILS_TABLE, applicationsTabService.getChildDetails(caseData));
    }
}
