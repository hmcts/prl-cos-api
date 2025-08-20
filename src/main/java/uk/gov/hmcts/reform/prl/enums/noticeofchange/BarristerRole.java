package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CAAPPLICANT1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CAAPPLICANT2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CAAPPLICANT3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CAAPPLICANT4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CAAPPLICANT5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CARESPONDENT1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CARESPONDENT2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CARESPONDENT3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CARESPONDENT4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.RoleMapping.CARESPONDENT5;

@RequiredArgsConstructor
@Getter
public enum BarristerRole {
    C100APPLICANTBARRISTER1("[C100APPLICANTBARRISTER1]", 1, CAAPPLICANT, CAAPPLICANT1),
    C100APPLICANTBARRISTER2("[C100APPLICANTBARRISTER2]", 2, CAAPPLICANT, CAAPPLICANT2),
    C100APPLICANTBARRISTER3("[C100APPLICANTBARRISTER3]", 3, CAAPPLICANT, CAAPPLICANT3),
    C100APPLICANTBARRISTER4("[C100APPLICANTBARRISTER4]", 4, CAAPPLICANT, CAAPPLICANT4),
    C100APPLICANTBARRISTER5("[C100APPLICANTBARRISTER5]", 5, CAAPPLICANT, CAAPPLICANT5),
    C100RESPONDENTBARRISTER1("[C100RESPONDENTBARRISTER1]", 1, CARESPONDENT, CARESPONDENT1),
    C100RESPONDENTBARRISTER2("[C100RESPONDENTBARRISTER2]", 2, CARESPONDENT, CARESPONDENT2),
    C100RESPONDENTBARRISTER3("[C100RESPONDENTBARRISTER3]", 3, CARESPONDENT, CARESPONDENT3),
    C100RESPONDENTBARRISTER4("[C100RESPONDENTBARRISTER4]", 4, CARESPONDENT, CARESPONDENT4),
    C100RESPONDENTBARRISTER5("[C100RESPONDENTBARRISTER5]", 5, CARESPONDENT, CARESPONDENT5),
    FL401APPLICANTBARRISTER("[FL401APPLICANTBARRISTER]", 1, DAAPPLICANT, RoleMapping.DAAPPLICANT),
    FL401RESPONDENTBARRISTER("[FL401RESPONDENTBARRISTER]", 1, DARESPONDENT, RoleMapping.DARESPONDENT);

    private final String caseRoleLabel;
    private final int index;
    private final Representing representing;
    private final RoleMapping roleMapping;

    public enum Representing {
        CAAPPLICANT,
        CARESPONDENT,
        DAAPPLICANT,
        DARESPONDENT
    }

    public int getIndex() {
        return index - 1;
    }

    @Getter
    public enum RoleMapping {
        CAAPPLICANT1("caApplicant1",  CAAPPLICANT),
        CAAPPLICANT2("caApplicant2", CAAPPLICANT),
        CAAPPLICANT3("caApplicant3", CAAPPLICANT),
        CAAPPLICANT4("caApplicant4",  CAAPPLICANT),
        CAAPPLICANT5("caApplicant5", CAAPPLICANT),
        CARESPONDENT1("caRespondent1",CARESPONDENT),
        CARESPONDENT2("caRespondent2",CARESPONDENT),
        CARESPONDENT3("caRespondent3",CARESPONDENT),
        CARESPONDENT4("caRespondent4",CARESPONDENT),
        CARESPONDENT5("caRespondent5", CARESPONDENT),
        DAAPPLICANT("daApplicant", Representing.DAAPPLICANT),
        DARESPONDENT("daRespondent",  Representing.DARESPONDENT);

        private final String party;
        private final Representing representing;

        RoleMapping(String party, Representing representing) {
            this.party = party;
            this.representing = representing;
        }
    }

    public static Optional<BarristerRole> fromCaseRoleLabel(String caseRoleLabel) {
        return Arrays.stream(uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.values())
            .filter(role -> role.caseRoleLabel.equals(caseRoleLabel))
            .findFirst();
    }
}
