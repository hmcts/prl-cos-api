package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DARESPONDENT;

@RequiredArgsConstructor
@Getter
public enum BarristerRole {
    C100APPLICANTBARRISTER1("[C100APPLICANTBARRISTER1]", CAAPPLICANT),
    C100APPLICANTBARRISTER2("[C100APPLICANTBARRISTER2]", CAAPPLICANT),
    C100APPLICANTBARRISTER3("[C100APPLICANTBARRISTER3]", CAAPPLICANT),
    C100APPLICANTBARRISTER4("[C100APPLICANTBARRISTER4]", CAAPPLICANT),
    C100APPLICANTBARRISTER5("[C100APPLICANTBARRISTER5]", CAAPPLICANT),
    C100RESPONDENTBARRISTER1("[C100RESPONDENTBARRISTER1]", CARESPONDENT),
    C100RESPONDENTBARRISTER2("[C100RESPONDENTBARRISTER2]", CARESPONDENT),
    C100RESPONDENTBARRISTER3("[C100RESPONDENTBARRISTER3]", CARESPONDENT),
    C100RESPONDENTBARRISTER4("[C100RESPONDENTBARRISTER4]", CARESPONDENT),
    C100RESPONDENTBARRISTER5("[C100RESPONDENTBARRISTER5]", CARESPONDENT),
    FL401APPLICANTBARRISTER("[APPLICANTBARRISTER]", DAAPPLICANT),
    FL401RESPONDENTBARRISTER("[FL401RESPONDENTBARRISTER]", DARESPONDENT);

    private final String caseRoleLabel;
    private final Representing representing;

    public enum Representing {
        CAAPPLICANT,
        CARESPONDENT,
        DAAPPLICANT,
        DARESPONDENT
    }
}
