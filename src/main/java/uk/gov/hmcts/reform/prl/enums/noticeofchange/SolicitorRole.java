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
    C100APPLICANTSOLICITOR1("[C100APPLICANTSOLICITOR1]", 1, Representing.CAAPPLICANT, ""),
    C100APPLICANTSOLICITOR2("[C100APPLICANTSOLICITOR2]", 2, Representing.CAAPPLICANT, ""),
    C100APPLICANTSOLICITOR3("[C100APPLICANTSOLICITOR3]", 3, Representing.CAAPPLICANT, ""),
    C100APPLICANTSOLICITOR4("[C100APPLICANTSOLICITOR4]", 4, Representing.CAAPPLICANT, ""),
    C100APPLICANTSOLICITOR5("[C100APPLICANTSOLICITOR5]", 5, Representing.CAAPPLICANT, ""),
    C100RESPONDENTSOLICITOR1("[C100RESPONDENTSOLICITOR1]", 1, Representing.CARESPONDENT, "A"),
    C100RESPONDENTSOLICITOR2("[C100RESPONDENTSOLICITOR2]", 2, Representing.CARESPONDENT, "B"),
    C100RESPONDENTSOLICITOR3("[C100RESPONDENTSOLICITOR3]", 3, Representing.CARESPONDENT, "C"),
    C100RESPONDENTSOLICITOR4("[C100RESPONDENTSOLICITOR4]", 4, Representing.CARESPONDENT, "D"),
    C100RESPONDENTSOLICITOR5("[C100RESPONDENTSOLICITOR5]", 5, Representing.CARESPONDENT, "E"),
    FL401APPLICANTSOLICITOR("[FL401APPLICANTSOLICITOR]", 1, Representing.DAAPPLICANT, ""),
    FL401RESPONDENTSOLICITOR("[FL401RESPONDENTSOLICITOR]", 1, Representing.DARESPONDENT, "");

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;
    private final String eventId;

    public int getIndex() {
        return index - 1;
    }

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

    public static List<SolicitorRole> matchingRoles(Representing representing) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.representing == representing)
            .collect(Collectors.toList());
    }

    public static List<SolicitorRole> notMatchingRoles(Representing representing) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.representing != representing)
            .collect(Collectors.toList());
    }

    public enum Representing {
        CAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            "caApplicant%dPolicy",
            "caApplicant%d",
            "caApplicants"
        ),
        CARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            "caRespondent%dPolicy",
            "caRespondent%d",
            "caRespondents"
        ),
        DAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            "daApplicantPolicy",
            "daApplicant",
            "daApplicants"
        ),
        DARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            "daRespondentPolicy",
            "daRespondent",
            "daRespondents"
        );

        private final Function<CaseData, List<Element<PartyDetails>>> caTarget;
        private final Function<CaseData, PartyDetails> daTarget;
        private final String policyFieldTemplate;
        private final String nocAnswersTemplate;
        private final String caseField;

        Representing(Function<CaseData, List<Element<PartyDetails>>> caTarget,
                     Function<CaseData, PartyDetails> daTarget,
                     String policyFieldTemplate, String nocAnswersTemplate, String caseField) {
            this.caTarget = caTarget;
            this.daTarget = daTarget;
            this.policyFieldTemplate = policyFieldTemplate;
            this.nocAnswersTemplate = nocAnswersTemplate;
            this.caseField = caseField;
        }

        public Function<CaseData, List<Element<PartyDetails>>> getCaTarget() {
            return caTarget;
        }

        public Function<CaseData, PartyDetails> getDaTarget() {
            return daTarget;
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
