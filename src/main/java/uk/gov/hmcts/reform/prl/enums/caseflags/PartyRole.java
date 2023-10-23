package uk.gov.hmcts.reform.prl.enums.caseflags;

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
public enum PartyRole {

    C100APPLICANT1("Applicant 1", 1, Representing.CAAPPLICANT),
    C100APPLICANT2("Applicant 2", 2, Representing.CAAPPLICANT),
    C100APPLICANT3("Applicant 3", 3, Representing.CAAPPLICANT),
    C100APPLICANT4("Applicant 4", 4, Representing.CAAPPLICANT),
    C100APPLICANT5("Applicant 5", 5, Representing.CAAPPLICANT),
    C100APPLICANTSOLICITOR1("Applicant solicitor 1", 1, Representing.CAAPPLICANTSOLICITOR),
    C100APPLICANTSOLICITOR2("Applicant solicitor 2", 2, Representing.CAAPPLICANTSOLICITOR),
    C100APPLICANTSOLICITOR3("Applicant solicitor 3", 3, Representing.CAAPPLICANTSOLICITOR),
    C100APPLICANTSOLICITOR4("Applicant solicitor 4", 4, Representing.CAAPPLICANTSOLICITOR),
    C100APPLICANTSOLICITOR5("Applicant solicitor 5", 5, Representing.CAAPPLICANTSOLICITOR),
    C100RESPONDENT1("Respondent 1", 1, Representing.CARESPONDENT),
    C100RESPONDENT2("Respondent 2", 2, Representing.CARESPONDENT),
    C100RESPONDENT3("Respondent 3", 3, Representing.CARESPONDENT),
    C100RESPONDENT4("Respondent 4", 4, Representing.CARESPONDENT),
    C100RESPONDENT5("Respondent 5", 5, Representing.CARESPONDENT),
    C100RESPONDENTSOLICITOR1("Respondent solicitor 1", 1, Representing.CARESPONDENTSOLCIITOR),
    C100RESPONDENTSOLICITOR2("Respondent solicitor 2", 2, Representing.CARESPONDENTSOLCIITOR),
    C100RESPONDENTSOLICITOR3("Respondent solicitor 3", 3, Representing.CARESPONDENTSOLCIITOR),
    C100RESPONDENTSOLICITOR4("Respondent solicitor 4", 4, Representing.CARESPONDENTSOLCIITOR),
    C100RESPONDENTSOLICITOR5("Respondent solicitor 5", 5, Representing.CARESPONDENTSOLCIITOR),
    C100OTHERPARTY1("Other people in the case 1", 1, Representing.CAOTHERPARTY),
    C100OTHERPARTY2("Other people in the case 2", 2, Representing.CAOTHERPARTY),
    C100OTHERPARTY3("Other people in the case 3", 3, Representing.CAOTHERPARTY),
    C100OTHERPARTY4("Other people in the case 4", 4, Representing.CAOTHERPARTY),
    C100OTHERPARTY5("Other people in the case 5", 5, Representing.CAOTHERPARTY),
    C100OTHERPARTY6("Other people in the case 6", 6, Representing.CAOTHERPARTY),
    C100OTHERPARTY7("Other people in the case 7", 7, Representing.CAOTHERPARTY),
    C100OTHERPARTY8("Other people in the case 8", 8, Representing.CAOTHERPARTY),
    C100OTHERPARTY9("Other people in the case 9", 9, Representing.CAOTHERPARTY),
    C100OTHERPARTY10("Other people in the case 10", 10, Representing.CAOTHERPARTY),
    FL401APPLICANT("Applicant", 1, Representing.DAAPPLICANT),
    FL401APPLICANTSOLICITOR("Applicant solicitor", 1, Representing.DAAPPLICANTSOLICITOR),
    FL401RESPONDENT("Respondent", 1, Representing.DARESPONDENT),
    FL401RESPONDENTSOLICITOR("Respondent solicitor", 1, Representing.DARESPONDENTSOLCIITOR);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;

    public int getIndex() {
        return index - 1;
    }

    public static Optional<PartyRole> fromRepresentingAndIndex(Representing representing, int index) {
        return Arrays.stream(PartyRole.values())
            .filter(role -> role.representing.equals(representing) && role.index == index)
            .findFirst();
    }

    public static Optional<PartyRole> fromCaseRoleLabel(String caseRoleLabel) {
        return Arrays.stream(PartyRole.values())
            .filter(role -> role.caseRoleLabel.equals(caseRoleLabel))
            .findFirst();
    }

    public static List<PartyRole> matchingRoles(Representing representing) {
        return Arrays.stream(PartyRole.values())
            .filter(role -> role.representing == representing)
            .collect(Collectors.toList());
    }

    public static List<PartyRole> notMatchingRoles(Representing representing) {
        return Arrays.stream(PartyRole.values())
            .filter(role -> role.representing != representing)
            .collect(Collectors.toList());
    }

    public enum Representing {
        CAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.CA_APPLICANT_EXTERNAL,
            Constants.CA_APPLICANT_INTERNAL
        ),
        CAAPPLICANTSOLICITOR(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.CA_APPLICANT_SOLICITOR_EXTERNAL,
            Constants.CA_APPLICANT_SOLICITOR_INTERNAL
        ),
        CARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.CA_RESPONDENT_EXTERNAL,
            Constants.CA_RESPONDENT_INTERNAL
        ),
        CARESPONDENTSOLCIITOR(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.CA_RESPONDENT_SOLICITOR_EXTERNAL,
            Constants.CA_RESPONDENT_SOLICITOR_INTERNAL
        ),
        CAOTHERPARTY(
            CaseData::getOtherPartyInTheCaseRevised,
            null,
            Constants.CA_OTHER_PARTY_EXTERNAL,
            Constants.CA_OTHER_PARTY_INTERNAL
        ),
        DAAPPLICANT(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.DA_APPLICANT_EXTERNAL,
            Constants.DA_APPLICANT_INTERNAL
        ),
        DAAPPLICANTSOLICITOR(
            CaseData::getApplicants,
            CaseData::getApplicantsFL401,
            Constants.DA_APPLICANT_SOLICITOR_EXTERNAL,
            Constants.DA_APPLICANT_SOLICITOR_INTERNAL
        ),
        DARESPONDENT(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.DA_RESPONDENT_EXTERNAL,
            Constants.DA_RESPONDENT_INTERNAL
        ),
        DARESPONDENTSOLCIITOR(
            CaseData::getRespondents,
            CaseData::getRespondentsFL401,
            Constants.DA_RESPONDENT_SOLICITOR_EXTERNAL,
            Constants.DA_RESPONDENT_SOLICITOR_INTERNAL
        );

        private final Function<CaseData, List<Element<PartyDetails>>> caTarget;
        private final Function<CaseData, PartyDetails> daTarget;
        @Getter
        private final String caseDataExternalField;
        @Getter
        private final String caseDataInternalField;

        Representing(Function<CaseData, List<Element<PartyDetails>>> caTarget,
                     Function<CaseData, PartyDetails> daTarget,
                     String caseDataExternalField,
                     String caseDataInternalField) {
            this.caTarget = caTarget;
            this.daTarget = daTarget;
            this.caseDataExternalField = caseDataExternalField;
            this.caseDataInternalField = caseDataInternalField;
        }

        public Function<CaseData, List<Element<PartyDetails>>> getCaTarget() {
            return caTarget;
        }

        public Function<CaseData, PartyDetails> getDaTarget() {
            return daTarget;
        }

        public String getCaseDataExternalField() {
            return caseDataExternalField;
        }

        public String getCaseDataInternalField() {
            return caseDataInternalField;
        }

        private static class Constants {
            public static final String CA_APPLICANT_EXTERNAL = "caApplicant%dExternalFlags";
            public static final String CA_APPLICANT_SOLICITOR_EXTERNAL = "caApplicantSolicitor%dExternalFlags";
            public static final String CA_RESPONDENT_EXTERNAL = "caRespondent%dExternalFlags";
            public static final String CA_RESPONDENT_SOLICITOR_EXTERNAL = "caRespondentSolicitor%dExternalFlags";
            public static final String CA_OTHER_PARTY_EXTERNAL = "caOtherParty%dExternalFlags";
            public static final String DA_APPLICANT_EXTERNAL = "daApplicantExternalFlags";
            public static final String DA_APPLICANT_SOLICITOR_EXTERNAL = "daApplicantSolicitorExternalFlags";
            public static final String DA_RESPONDENT_EXTERNAL = "daRespondentExternalFlags";
            public static final String DA_RESPONDENT_SOLICITOR_EXTERNAL = "daRespondentSolicitorExternalFlags";
            public static final String CA_APPLICANT_INTERNAL = "caApplicant%dInternalFlags";
            public static final String CA_APPLICANT_SOLICITOR_INTERNAL = "caApplicantSolicitor%dInternalFlags";
            public static final String CA_RESPONDENT_INTERNAL = "caRespondent%dInternalFlags";
            public static final String CA_RESPONDENT_SOLICITOR_INTERNAL = "caRespondentSolicitor%dInternalFlags";
            public static final String CA_OTHER_PARTY_INTERNAL = "caOtherParty%dInternalFlags";
            public static final String DA_APPLICANT_INTERNAL = "daApplicantInternalFlags";
            public static final String DA_APPLICANT_SOLICITOR_INTERNAL = "daApplicantSolicitorInternalFlags";
            public static final String DA_RESPONDENT_INTERNAL = "daRespondentInternalFlags";
            public static final String DA_RESPONDENT_SOLICITOR_INTERNAL = "daRespondentSolicitorInternalFlags";
        }
    }
}
