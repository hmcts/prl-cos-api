package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;

import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_10;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_11;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_12;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_13;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_15;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_16;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_17;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_18;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_19;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_2;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_20;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_21;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_22;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_3;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_5;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_6;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_7;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_8;
import static uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_9;

@Getter
@RequiredArgsConstructor
public enum DomesticAbuseMapperEnum {
    evidenceOfSomeoneArrest(miamDomesticViolenceChecklistEnum_Value_1),
    evidenceOfPolice(miamDomesticViolenceChecklistEnum_Value_2),
    evidenceOfOnGoingCriminalProceeding(miamDomesticViolenceChecklistEnum_Value_3),
    evidenceOfConviction(miamDomesticViolenceChecklistEnum_Value_4),
    evidenceOFProtectionNotice(miamDomesticViolenceChecklistEnum_Value_6),
    boundedByCourtAction(miamDomesticViolenceChecklistEnum_Value_5),
    protectionInjuction(miamDomesticViolenceChecklistEnum_Value_7),
    fmlAct1996(miamDomesticViolenceChecklistEnum_Value_8),
    ukdomesticVoilcenceUK(miamDomesticViolenceChecklistEnum_Value_9),
    ukPotentialVictim(miamDomesticViolenceChecklistEnum_Value_10),
    letterFromHealthProfessional(miamDomesticViolenceChecklistEnum_Value_11),
    letterFromHPfromPerspectiveParty(miamDomesticViolenceChecklistEnum_Value_12),
    letterFromMultiAgencyMember(miamDomesticViolenceChecklistEnum_Value_13),
    letterFromOfficer(miamDomesticViolenceChecklistEnum_Value_15),
    letterFromPublicAuthority(miamDomesticViolenceChecklistEnum_Value_20),
    letterFromDomesticViolenceAdvisor(miamDomesticViolenceChecklistEnum_Value_17),
    letterFromSexualViolenceAdvisor(miamDomesticViolenceChecklistEnum_Value_16),
    letterFromOrgDomesticViolenceSupport(miamDomesticViolenceChecklistEnum_Value_18),
    letterFromOrgDomesticViolenceInUk(miamDomesticViolenceChecklistEnum_Value_19),
    ILRDuetoDomesticAbuse(miamDomesticViolenceChecklistEnum_Value_21),
    financiallyAbuse(miamDomesticViolenceChecklistEnum_Value_22);

    public MiamDomesticViolenceChecklistEnum getValue() {
        return value;
    }

    private final MiamDomesticViolenceChecklistEnum value;
}
