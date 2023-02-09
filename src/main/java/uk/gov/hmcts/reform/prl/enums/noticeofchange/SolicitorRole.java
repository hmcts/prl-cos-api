package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[SOLICITORA]", 0, Representing.RESPONDENT),
    SOLICITORB("[SOLICITORB]", 1, Representing.RESPONDENT),
    SOLICITORC("[SOLICITORC]", 2, Representing.RESPONDENT),
    SOLICITORD("[SOLICITORD]", 3, Representing.RESPONDENT),
    SOLICITORE("[SOLICITORE]", 4, Representing.RESPONDENT),
    SOLICITORF("[SOLICITORF]", 5, Representing.RESPONDENT),
    SOLICITORG("[SOLICITORG]", 6, Representing.RESPONDENT),
    SOLICITORH("[SOLICITORH]", 7, Representing.RESPONDENT),
    SOLICITORI("[SOLICITORI]", 8, Representing.RESPONDENT),
    SOLICITORJ("[SOLICITORJ]", 9, Representing.RESPONDENT);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;

    public static Optional<SolicitorRole> from(String label) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst();
    }

    public static List<SolicitorRole> values(Representing representing) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.representing == representing)
            .collect(Collectors.toList());
    }

    public enum Representing {
        RESPONDENT(
            CaseData::getRespondents,
            "respondent%dPolicy",
            "respondent%d",
            "respondents"
        );

        private final Function<CaseData, List<Element<PartyDetails>>> target;
        private final String policyFieldTemplate;
        private final String nocAnswersTemplate;
        private final String caseField;

        Representing(Function<CaseData, List<Element<PartyDetails>>> target,
                     String policyFieldTemplate, String nocAnswersTemplate, String caseField) {
            this.target = target;
            this.policyFieldTemplate = policyFieldTemplate;
            this.nocAnswersTemplate = nocAnswersTemplate;
            this.caseField = caseField;
        }

        public Function<CaseData, List<Element<PartyDetails>>> getTarget() {
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
