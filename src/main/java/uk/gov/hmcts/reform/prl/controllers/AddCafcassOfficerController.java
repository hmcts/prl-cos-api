package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AddCafcassOfficerController {

    private final ObjectMapper objectMapper;

    private final ApplicationsTabService applicationsTabService;

    @PostMapping(path = "/add-cafcass-officer/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse prePopulateChildDetails(@RequestBody CallbackRequest callbackRequest) {
        log.info("in prePopulateChildDetails ------>");
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("ChildAndCafcassOfficers before ------>" + caseData.getChildAndCafcassOfficers());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = null;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (caseData.getChildren() != null && !caseData.getChildren().isEmpty()) {
                childAndCafcassOfficers = new ArrayList<>();
                for (Element<Child> childElement : caseData.getChildren()) {
                    ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                        .childId(childElement.getId().toString())
                        .childName(childElement.getValue().getFirstName() + " " + childElement.getValue().getLastName()).build();
                    childAndCafcassOfficers.add(element(childAndCafcassOfficer));
                }
            }
        } else {
            if (YesOrNo.Yes.equals(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren())) {
                childAndCafcassOfficers = new ArrayList<>();
                for (Element<ApplicantChild> applicantChildElement : caseData.getApplicantChildDetails()) {
                    ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                        .childId(applicantChildElement.getId().toString())
                        .childName(applicantChildElement.getValue().getFullName()).build();
                    childAndCafcassOfficers.add(element(childAndCafcassOfficer));
                }
            }
        }
        caseDataUpdated.put("childAndCafcassOfficers", childAndCafcassOfficers);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/add-cafcass-officer/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse populateChildDetailsWithCafcassOfficer(@RequestBody CallbackRequest callbackRequest) {
        log.info("in populateChildDetailsWithCafcassOfficer ------>");
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("caseData ------>" + caseData);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = caseData.getChildAndCafcassOfficers();

        for (Element<ChildAndCafcassOfficer> cafcassOfficer : childAndCafcassOfficers) {
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
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
                caseDataUpdated.put("childDetailsTable", applicationsTabService.getChildDetails(caseData));
                log.info("Children C100------>" + caseData.getChildren());
            } else {
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
                    caseDataUpdated.put("applicantFamilyTable", applicantFamilyMap);
                    if (("Yes").equals(applicantFamilyMap.get("doesApplicantHaveChildren"))) {
                        caseDataUpdated.put("fl401ChildDetailsTable", applicantFamilyMap.get("applicantChild"));
                    }
                    log.info("Children FL401------>" + caseData.getApplicantChildDetails());
                }
            }
        }


        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
