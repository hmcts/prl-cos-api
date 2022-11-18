package uk.gov.hmcts.reform.prl.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.InterlocutoryApplicant;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DynamicListService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantsListGenerator {

    private static final String SEPARATOR = ", ";
    private final DynamicListService dynamicLists;

    public DynamicList buildApplicantsList(CaseData caseData) {

        List<InterlocutoryApplicant> applicantsFullNames = new ArrayList<>();

        applicantsFullNames.addAll(buildApplicantNameElements(caseData));
        applicantsFullNames.addAll(buildRespondentNameElements(caseData));
        applicantsFullNames.addAll(buildChildNameElements(caseData));


        return dynamicLists.asDynamicList(
            applicantsFullNames,
            InterlocutoryApplicant::getCode,
            InterlocutoryApplicant::getName
        );
    }

    private List<InterlocutoryApplicant> buildApplicantNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> parties = new ArrayList<>();

        if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)) {
            List<Element<PartyDetails>> applicants = caseData.getRespondents();

            applicants.forEach(applicant -> parties.add(
                InterlocutoryApplicant.builder().code(applicant.getId().toString())
                    .name(applicant.getValue().getFirstName() + " " + applicant.getValue().getLastName() + ", Applicant " + i.getAndIncrement())
                    .build())
            );
        } else {
            PartyDetails fl401Applicant = caseData.getApplicantsFL401();
            parties.add(
                InterlocutoryApplicant.builder().code("applicant")
                    .name(fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName() + ", Applicant " + i.getAndIncrement())
                    .build());
        }

        return parties;
    }

    private List<InterlocutoryApplicant> buildRespondentNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> parties = new ArrayList<>();

        if (caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();

            respondents.forEach(respondent -> parties.add(
                InterlocutoryApplicant.builder().code(respondent.getId().toString())
                    .name(respondent.getValue().getFirstName() + " " + respondent.getValue().getLastName() + ", Respondent " + i.getAndIncrement())
                    .build())
            );
        } else {
            PartyDetails fl401Respondent = caseData.getRespondentsFL401();
            parties.add(
                InterlocutoryApplicant.builder().code("respondent")
                    .name(fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName() + ", Respondent " + i.getAndIncrement())
                    .build());
        }

        return parties;
    }

    private List<InterlocutoryApplicant> buildChildNameElements(CaseData caseData) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<InterlocutoryApplicant> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<Child>> children = caseData.getChildren();
            children.forEach(child -> parties.add(
                InterlocutoryApplicant.builder().code(child.getId().toString())
                    .name(child.getValue().getFirstName() + " " + child.getValue().getLastName() + ", Child " + i.getAndIncrement())
                    .build())
            );
        } else {
            Optional<List<Element<ApplicantChild>>> applicantChildDetails =
                ofNullable(caseData.getApplicantChildDetails());
            if (applicantChildDetails.isPresent()) {
                List<ApplicantChild> children = applicantChildDetails.get().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                children.forEach(child -> parties.add(
                    InterlocutoryApplicant.builder().code("Child " + i.getAndIncrement())
                        .name(child.getFullName() + ", Child " + i.getAndIncrement())
                        .build())
                );
            }
        }
        return parties;
    }

}
