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
    SOLICITORA("[SOLICITORA]", 0, Representing.RESPONDENT, "A"),
    SOLICITORB("[SOLICITORB]", 1, Representing.RESPONDENT, "B"),
    SOLICITORC("[SOLICITORC]", 2, Representing.RESPONDENT, "C"),
    SOLICITORD("[SOLICITORD]", 3, Representing.RESPONDENT,"D"),
    SOLICITORE("[SOLICITORE]", 4, Representing.RESPONDENT,"E"),
    SOLICITORF("[SOLICITORF]", 5, Representing.RESPONDENT,"F"),
    SOLICITORG("[SOLICITORG]", 6, Representing.RESPONDENT,"G"),
    SOLICITORH("[SOLICITORH]", 7, Representing.RESPONDENT, "H"),
    SOLICITORI("[SOLICITORI]", 8, Representing.RESPONDENT, "I"),
    SOLICITORJ("[SOLICITORJ]", 9, Representing.RESPONDENT, "J");

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;
    private final String eventId;

    public static Optional<SolicitorRole> from(String eventId) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.eventId.equals(eventId))
            .findFirst();
    }

    public static Optional<SolicitorRole> fromCaseRoleLabel(String caseRoleLabel) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(caseRoleLabel))
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
