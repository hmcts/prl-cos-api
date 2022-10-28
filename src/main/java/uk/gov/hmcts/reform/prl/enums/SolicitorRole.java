package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.WithSolicitor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    APPLICANTSOLICITOR("[APPLICANTSOLICITOR]", 0, Representing.RESPONDENT),
    RESPONDENTSOLICITOR("[RESPONDENTSOLICITOR]", 1, Representing.RESPONDENT);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;

    public static Optional<SolicitorRole> from(String label) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst();
    }

    public static List<SolicitorRole> values(Representing representing) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.representing == representing)
            .collect(Collectors.toList());
    }

    public Optional<Element<WithSolicitor>> getRepresentedPerson(CaseData caseData) {
        List<Element<WithSolicitor>> parties = this.representing.target.apply(caseData);
        if (this.index < parties.size()) {
            return Optional.of(parties.get(this.index));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public enum Representing {
        RESPONDENT(caseData -> (List) caseData.getRespondents(),
            "respondentPolicy%d",
            "noticeOfChangeAnswers%d",
            "respondents1");

        private final Function<CaseData, List<Element<WithSolicitor>>> target;
        private final String policyFieldTemplate;
        private final String nocAnswersTemplate;
        private final String caseField;

        Representing(Function<CaseData, List<Element<WithSolicitor>>> target,
                     String policyFieldTemplate, String nocAnswersTemplate, String caseField) {
            this.target = target;
            this.policyFieldTemplate = policyFieldTemplate;
            this.nocAnswersTemplate = nocAnswersTemplate;
            this.caseField = caseField;
        }

        public Function<CaseData, List<Element<WithSolicitor>>> getTarget() {
            return target;
        }

        public String getPolicyFieldTemplate() {
            return policyFieldTemplate;
        }

        public String getNocAnswersTemplate() {
            return nocAnswersTemplate;
        }

        public String getCaseField() {
            return caseField;
        }
    }
}
