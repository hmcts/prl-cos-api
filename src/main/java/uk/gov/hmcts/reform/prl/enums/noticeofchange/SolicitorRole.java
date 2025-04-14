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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    C100APPLICANTSOLICITOR1("[C100APPLICANTSOLICITOR1]", 1, Representing.CAAPPLICANT, BLANK_STRING),
    C100APPLICANTSOLICITOR2("[C100APPLICANTSOLICITOR2]", 2, Representing.CAAPPLICANT, BLANK_STRING),
    C100APPLICANTSOLICITOR3("[C100APPLICANTSOLICITOR3]", 3, Representing.CAAPPLICANT, BLANK_STRING),
    C100APPLICANTSOLICITOR4("[C100APPLICANTSOLICITOR4]", 4, Representing.CAAPPLICANT, BLANK_STRING),
    C100APPLICANTSOLICITOR5("[C100APPLICANTSOLICITOR5]", 5, Representing.CAAPPLICANT, BLANK_STRING),
    C100RESPONDENTSOLICITOR1("[C100RESPONDENTSOLICITOR1]", 1, Representing.CARESPONDENT, "A"),
    C100RESPONDENTSOLICITOR2("[C100RESPONDENTSOLICITOR2]", 2, Representing.CARESPONDENT, "B"),
    C100RESPONDENTSOLICITOR3("[C100RESPONDENTSOLICITOR3]", 3, Representing.CARESPONDENT, "C"),
    C100RESPONDENTSOLICITOR4("[C100RESPONDENTSOLICITOR4]", 4, Representing.CARESPONDENT, "D"),
    C100RESPONDENTSOLICITOR5("[C100RESPONDENTSOLICITOR5]", 5, Representing.CARESPONDENT, "E"),
    FL401APPLICANTSOLICITOR("[APPLICANTSOLICITOR]", 1, Representing.DAAPPLICANT, BLANK_STRING),
    FL401RESPONDENTSOLICITOR("[FL401RESPONDENTSOLICITOR]", 1, Representing.DARESPONDENT, BLANK_STRING);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;
    private final String eventId;

    public int getIndex() {
        return index - 1;
    }

    public static Optional<SolicitorRole> fromRepresentingAndIndex(Representing representing, int index) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.representing.equals(representing) && role.index == index)
            .findFirst();
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
            .toList();
    }

    public static List<SolicitorRole> notMatchingRoles(Representing representing) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.values())
            .filter(role -> role.representing != representing)
            .toList();
    }

    public enum Representing {
        CAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.CA_APPLICANT_POLICY,
            Constants.CA_APPLICANT,
            Constants.CA_APPLICANTS
        ),
        CARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.CA_RESPONDENT_POLICY,
            Constants.CA_RESPONDENT,
            Constants.CA_RESPONDENTS
        ),
        DAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.DA_APPLICANT_POLICY,
            Constants.DA_APPLICANT,
            Constants.DA_APPLICANTS
        ),
        DARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.DA_RESPONDENT_POLICY,
            Constants.DA_RESPONDENT,
            Constants.DA_RESPONDENTS
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

        private static class Constants {
            public static final String CA_APPLICANT_POLICY = "caApplicant%dPolicy";
            public static final String CA_APPLICANT = "caApplicant%d";
            public static final String CA_APPLICANTS = "caApplicants";
            public static final String CA_RESPONDENT_POLICY = "caRespondent%dPolicy";
            public static final String CA_RESPONDENT = "caRespondent%d";
            public static final String CA_RESPONDENTS = "caRespondents";
            public static final String DA_APPLICANT_POLICY = "applicantOrganisationPolicy";
            public static final String DA_APPLICANT = "daApplicant";
            public static final String DA_APPLICANTS = "daApplicants";
            public static final String DA_RESPONDENT_POLICY = "daRespondentPolicy";
            public static final String DA_RESPONDENT = "daRespondent";
            public static final String DA_RESPONDENTS = "daRespondents";
        }
    }
}
