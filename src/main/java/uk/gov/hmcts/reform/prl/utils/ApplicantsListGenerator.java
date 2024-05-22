package uk.gov.hmcts.reform.prl.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.ApplicantOfAdditionalApplication;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantsListGenerator {

    private final ElementUtils elementUtils;

    public DynamicList buildApplicantsList(CaseData caseData) {

        List<Element<ApplicantOfAdditionalApplication>> applicantsFullNames = new ArrayList<>();

        applicantsFullNames.addAll(buildApplicantNameElements(caseData));
        applicantsFullNames.addAll(buildRespondentNameElements(caseData));
        applicantsFullNames.addAll(buildChildNameElements(caseData));


        return elementUtils.asDynamicList(
            applicantsFullNames,
            null,
            ApplicantOfAdditionalApplication::getName
        );
    }

    private List<Element<ApplicantOfAdditionalApplication>> buildApplicantNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<Element<ApplicantOfAdditionalApplication>> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();

            applicants.forEach(applicant -> parties.add(
                element(ApplicantOfAdditionalApplication.builder().code(applicant.getId().toString())
                            .name(applicant.getValue().getFirstName() + " " + applicant.getValue().getLastName()
                                      + ", Applicant " + i.getAndIncrement())
                            .build())));
        } else {
            PartyDetails fl401Applicant = caseData.getApplicantsFL401();
            parties.add(
                element(ApplicantOfAdditionalApplication.builder().code("Applicant")
                            .name(fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName() + ", Applicant " + i.getAndIncrement())
                            .build()));
        }

        return parties;
    }

    private List<Element<ApplicantOfAdditionalApplication>> buildRespondentNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<Element<ApplicantOfAdditionalApplication>> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();

            respondents.forEach(respondent -> parties.add(
                element(ApplicantOfAdditionalApplication.builder().code(respondent.getId().toString())
                            .name(respondent.getValue().getFirstName() + " " + respondent.getValue().getLastName()
                                      + ", Respondent " + i.getAndIncrement())
                            .build()))
            );
        } else {
            PartyDetails fl401Respondent = caseData.getRespondentsFL401();
            parties.add(
                element(ApplicantOfAdditionalApplication.builder().code("Respondent")
                            .name(fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName() + ", Respondent " + i.getAndIncrement())
                            .build()));
        }

        return parties;
    }

    private List<Element<ApplicantOfAdditionalApplication>> buildChildNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<Element<ApplicantOfAdditionalApplication>> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                    || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
                List<Element<ChildDetailsRevised>> children = caseData.getNewChildDetails();
                children.forEach(child -> parties.add(
                    element(ApplicantOfAdditionalApplication.builder().code(child.getId().toString())
                                .name(child.getValue().getFirstName() + " " + child.getValue().getLastName() + ", Child " + i.getAndIncrement())
                                .build()))
                );
            } else {
                List<Element<Child>> children = caseData.getChildren();
                children.forEach(child -> parties.add(
                    element(ApplicantOfAdditionalApplication.builder().code(child.getId().toString())
                                .name(child.getValue().getFirstName() + " " + child.getValue().getLastName() + ", Child " + i.getAndIncrement())
                                .build()))
                );
            }
        } else {
            Optional<List<Element<ApplicantChild>>> applicantChildDetails =
                ofNullable(caseData.getApplicantChildDetails());
            if (applicantChildDetails.isPresent()) {
                List<ApplicantChild> children = applicantChildDetails.get().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                children.forEach(child -> parties.add(
                    element(ApplicantOfAdditionalApplication.builder().code("Child " + i.getAndIncrement())
                                .name(child.getFullName() + ", Child " + i.getAndIncrement())
                                .build()))
                );
            }
        }
        return parties;
    }

}
