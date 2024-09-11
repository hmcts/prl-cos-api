package uk.gov.hmcts.reform.prl.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PartiesListGenerator {
    private final ElementUtils elementUtils;

    public DynamicList buildPartiesList(CaseData caseData, List<DynamicListElement> courtList) {

        List<DynamicListElement> partiesList = new ArrayList<>();

        partiesList.addAll(buildApplicantRepresentativeList(caseData));
        partiesList.addAll(buildRespondentRepresentativeList(caseData));
        partiesList.addAll(courtList);

        return DynamicList.builder().value(DynamicListElement.EMPTY).listItems(partiesList)
            .build();
    }

    private List<DynamicListElement> buildApplicantRepresentativeList(CaseData caseData) {

        List<DynamicListElement> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();
            if (applicants != null && !applicants.isEmpty()) {
                Map<String, String> applicantSolicitors = applicants.stream()
                    .collect(
                    Collectors.toMap(
                        party -> party.getId().toString(),
                        party -> party.getValue().getRepresentativeFullNameForCaseFlags()
                    ));

                for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {
                    parties.add(DynamicListElement.builder().code(appSols.getKey()).label(appSols.getValue()).build());
                }
            }
        } else {
            PartyDetails fl401Applicant = caseData.getApplicantsFL401();
            if (fl401Applicant != null && !fl401Applicant.getSolicitorEmail().isEmpty() && !fl401Applicant.getRepresentativeFirstName().isEmpty()
                && !fl401Applicant.getRepresentativeLastName().isEmpty()) {
                String solicitorName = fl401Applicant.getRepresentativeFirstName() + " " + fl401Applicant.getRepresentativeLastName();
                String solicitorEmailAddress = fl401Applicant.getSolicitorEmail();
                parties.add(DynamicListElement.builder().code(solicitorEmailAddress).label(solicitorName).build());
            }

        }
        return parties;
    }

    private List<DynamicListElement> buildRespondentRepresentativeList(CaseData caseData) {
        List<DynamicListElement> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();
            if (respondents != null && !respondents.isEmpty()) {
                Map<String, String> respondentSolicitors = respondents
                    .stream()
                    .filter(party -> YesNoDontKnow.yes.equals(party.getValue().getDoTheyHaveLegalRepresentation()))
                    .collect(
                        Collectors.toMap(
                            party -> party.getId().toString(),
                            party -> party.getValue().getRepresentativeFullNameForCaseFlags()
                        ));

                for (Map.Entry<String, String> appSols : respondentSolicitors.entrySet()) {
                    parties.add(DynamicListElement.builder().code(appSols.getKey()).label(appSols.getValue()).build());
                }
            }
        } else {
            PartyDetails fl401Respondent = caseData.getRespondentsFL401();
            if (fl401Respondent != null && YesNoDontKnow.yes.equals(fl401Respondent.getDoTheyHaveLegalRepresentation())) {
                String solicitorName = fl401Respondent.getRepresentativeFirstName() + " " + fl401Respondent.getRepresentativeLastName();
                String solicitorEmailAddress = fl401Respondent.getSolicitorEmail();
                parties.add(DynamicListElement.builder().code(solicitorEmailAddress).label(solicitorName).build());
            }
        }
        return parties;
    }
}
