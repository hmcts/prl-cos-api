package uk.gov.hmcts.reform.prl.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PartiesListGenerator {
    private final ElementUtils elementUtils;

    public static final String APPLICANT_SOLICITOR = " (Applicant Solicitor)";
    public static final String RESPONDENT_SOLICITOR = " (Respondent Solicitor)";

    Predicate<Element<PartyDetails>> notNullSolicitorsNamePredicate = party -> !party.getValue()
        .getRepresentativeFullNameForCaseFlags().isBlank();
    Predicate<Element<PartyDetails>> solicitorPresentPredicate = party -> YesNoDontKnow.yes
        .equals(party.getValue().getDoTheyHaveLegalRepresentation());

    public DynamicList buildPartiesList(CaseData caseData, List<DynamicListElement> courtList) {

        List<DynamicListElement> partiesList = new ArrayList<>();

        Optional<DynamicListElement> court = courtList.stream()
            .filter(element -> element.getCode().equalsIgnoreCase(caseData.getCourtName()))
            .findFirst();

        partiesList.addAll(buildApplicantRepresentativeList(caseData));
        partiesList.addAll(buildRespondentRepresentativeList(caseData));
        partiesList.add(DynamicListElement.builder()
                            .label(court.isPresent() ? court.get().getLabel() : caseData.getCourtName())
                            .code(court.isPresent() ? court.get().getCode() : caseData.getCourtName())
                            .build());

        return DynamicList.builder().value(DynamicListElement.EMPTY).listItems(partiesList)
            .build();
    }

    private List<DynamicListElement> buildApplicantRepresentativeList(CaseData caseData) {

        List<DynamicListElement> parties = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();
            if (applicants != null && !applicants.isEmpty()) {
                Map<String, String> applicantSolicitors = applicants.stream()
                    .filter(notNullSolicitorsNamePredicate)
                    .collect(
                    Collectors.toMap(
                        party -> party.getId().toString(),
                        party -> party.getValue().getRepresentativeFullNameForCaseFlags().concat(APPLICANT_SOLICITOR)
                    ));

                log.info("Applicant solicitors Map<<<<<<<<>>>>>>> {}", applicantSolicitors);
                for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {
                    parties.add(DynamicListElement.builder().code(appSols.getKey()).label(appSols.getValue()).build());
                }
            }
        } else {
            PartyDetails fl401Applicant = caseData.getApplicantsFL401();
            if (fl401Applicant != null && !fl401Applicant.getSolicitorEmail().isEmpty() && !fl401Applicant.getRepresentativeFirstName().isEmpty()
                && !fl401Applicant.getRepresentativeLastName().isEmpty()) {
                String solicitorName = fl401Applicant.getRepresentativeFirstName()
                    + " " + fl401Applicant.getRepresentativeLastName() + APPLICANT_SOLICITOR;
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
                    .filter(solicitorPresentPredicate)
                    .filter(notNullSolicitorsNamePredicate)
                    .collect(
                        Collectors.toMap(
                            party -> party.getId().toString(),
                            party -> party.getValue().getRepresentativeFullNameForCaseFlags().concat(RESPONDENT_SOLICITOR)
                        ));
                log.info("Respondent solicitors Map<<<<<<<<>>>>>>> {}", respondentSolicitors);
                for (Map.Entry<String, String> appSols : respondentSolicitors.entrySet()) {
                    parties.add(DynamicListElement.builder().code(appSols.getKey()).label(appSols.getValue()).build());
                }
            }
        } else {
            PartyDetails fl401Respondent = caseData.getRespondentsFL401();
            if (fl401Respondent != null && YesNoDontKnow.yes.equals(fl401Respondent.getDoTheyHaveLegalRepresentation())) {
                String solicitorName = fl401Respondent.getRepresentativeFirstName() + " "
                    + fl401Respondent.getRepresentativeLastName() + RESPONDENT_SOLICITOR;
                String solicitorEmailAddress = fl401Respondent.getSolicitorEmail();
                parties.add(DynamicListElement.builder().code(solicitorEmailAddress).label(solicitorName).build());
            }
        }
        return parties;
    }
}
