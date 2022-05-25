package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabsService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Api
@RestController
@RequestMapping("/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TaskListController extends AbstractCallbackController {

    @Autowired
    @Qualifier("allTabsService")
    AllTabsService tabService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        updateSearchCaseMetaInfo(caseData);
        publishEvent(new CaseDataChanged(caseData));
        tabService.updateAllTabs(caseData);
    }

    private void updateSearchCaseMetaInfo(CaseData caseData) {


        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();

            if (Objects.nonNull(fl401Applicant)) {
                log.info("adding applicant name in casedata: ");
                caseData.setApplicantName(fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
                log.info("Applicant case name in case data is: {}",caseData.getApplicantName());
            }
        } else {
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                log.info("adding applicant name in casedata: ");
                if (Objects.nonNull(applicant1)) {
                    caseData.setApplicantName(applicant1.getFirstName() + " " + applicant1.getLastName());
                    log.info("Applicant case name in case data is: {}",caseData.getApplicantName());
                }

            }
            Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());
            if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
                List<Child> children = childrenWrapped.get().stream().map(Element::getValue).collect(Collectors.toList());
                Child child = children.get(0);
                if (Objects.nonNull(child)) {
                    caseData.setChildName(child.getFirstName() + " " + child.getLastName());
                    log.info("child case name in case data is: {}",caseData.getChildName());
                }

            }
        }

        log.info("applicant Name : {}",caseData.getApplicantName());
        log.info("child Name : {}",caseData.getChildName());


    }
}
