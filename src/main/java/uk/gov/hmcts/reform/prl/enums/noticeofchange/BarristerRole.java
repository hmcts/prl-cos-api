package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100APPLICANTSOLICITOR1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100APPLICANTSOLICITOR2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100APPLICANTSOLICITOR3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100APPLICANTSOLICITOR4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100APPLICANTSOLICITOR5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100RESPONDENTSOLICITOR1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100RESPONDENTSOLICITOR2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100RESPONDENTSOLICITOR3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100RESPONDENTSOLICITOR4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.C100RESPONDENTSOLICITOR5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.FL401APPLICANTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.FL401RESPONDENTSOLICITOR;

@RequiredArgsConstructor
@Getter
public enum BarristerRole {
    C100APPLICANTBARRISTER1("[C100APPLICANTBARRISTER1]",
                            C100APPLICANTSOLICITOR1.getCaseRoleLabel(),
                            CAAPPLICANT,
                            CAAPPLICANT1),
    C100APPLICANTBARRISTER2("[C100APPLICANTBARRISTER2]",
                            C100APPLICANTSOLICITOR2.getCaseRoleLabel(),
                            CAAPPLICANT,
                            CAAPPLICANT2),
    C100APPLICANTBARRISTER3("[C100APPLICANTBARRISTER3]",
                            C100APPLICANTSOLICITOR3.getCaseRoleLabel(),
                            CAAPPLICANT,
                            CAAPPLICANT3),
    C100APPLICANTBARRISTER4("[C100APPLICANTBARRISTER4]",
                            C100APPLICANTSOLICITOR4.getCaseRoleLabel(),
                            CAAPPLICANT,
                            CAAPPLICANT4),
    C100APPLICANTBARRISTER5("[C100APPLICANTBARRISTER5]",
                            C100APPLICANTSOLICITOR5.getCaseRoleLabel(),
                            CAAPPLICANT,
                            CAAPPLICANT5),
    C100RESPONDENTBARRISTER1("[C100RESPONDENTBARRISTER1]",
                             C100RESPONDENTSOLICITOR1.getCaseRoleLabel(),
                             CARESPONDENT,
                             CARESPONDENT1),
    C100RESPONDENTBARRISTER2("[C100RESPONDENTBARRISTER2]",
                             C100RESPONDENTSOLICITOR2.getCaseRoleLabel(),
                             CARESPONDENT,
                             CARESPONDENT2),
    C100RESPONDENTBARRISTER3("[C100RESPONDENTBARRISTER3]",
                             C100RESPONDENTSOLICITOR3.getCaseRoleLabel(),
                             CARESPONDENT, CARESPONDENT3),
    C100RESPONDENTBARRISTER4("[C100RESPONDENTBARRISTER4]",
                             C100RESPONDENTSOLICITOR4.getCaseRoleLabel(),
                             CARESPONDENT,
                             CARESPONDENT4),
    C100RESPONDENTBARRISTER5("[C100RESPONDENTBARRISTER5]",
                             C100RESPONDENTSOLICITOR5.getCaseRoleLabel(),
                             CARESPONDENT,
                             CARESPONDENT5),
    FL401APPLICANTBARRISTER("[FL401APPLICANTBARRISTER]",
                            FL401APPLICANTSOLICITOR.getCaseRoleLabel(),
                            DAAPPLICANT,
                            RoleMapping.DAAPPLICANT),
    FL401RESPONDENTBARRISTER("[FL401RESPONDENTBARRISTER]",
                             FL401RESPONDENTSOLICITOR.getCaseRoleLabel(),
                             DARESPONDENT,
                             RoleMapping.DARESPONDENT);

    private final String caseRoleLabel;
    private final String solicitorCaseRole;
    private final Representing representing;
    private final RoleMapping roleMapping;

    public enum Representing {
        CAAPPLICANT,
        CARESPONDENT,
        DAAPPLICANT,
        DARESPONDENT
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
}
