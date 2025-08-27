package uk.gov.hmcts.reform.prl.enums.noticeofchange;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100RESPONDENTBARRISTER1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100RESPONDENTBARRISTER2;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100RESPONDENTBARRISTER3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100RESPONDENTBARRISTER4;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100RESPONDENTBARRISTER5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401APPLICANTBARRISTER;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401RESPONDENTBARRISTER;
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

class BarristerRoleTest {

    @Test
    void testGetAllBarristerRolesForC100() {
        BarristerRole[] values = Arrays.stream(BarristerRole.values())
            .filter(barristerRole ->  barristerRole.getRepresenting().equals(BarristerRole.Representing.CAAPPLICANT)
                || barristerRole.getRepresenting().equals(BarristerRole.Representing.CARESPONDENT))
            .toArray(BarristerRole[]::new);

        assertThat(values)
            .containsExactly(C100APPLICANTBARRISTER1,
                      C100APPLICANTBARRISTER2,
                      C100APPLICANTBARRISTER3,
                      C100APPLICANTBARRISTER4,
                      C100APPLICANTBARRISTER5,
                      C100RESPONDENTBARRISTER1,
                      C100RESPONDENTBARRISTER2,
                      C100RESPONDENTBARRISTER3,
                      C100RESPONDENTBARRISTER4,
                      C100RESPONDENTBARRISTER5);
    }

    static Stream<Arguments> parameterRoles() {
        return Stream.of(
            of(C100APPLICANTSOLICITOR1.getCaseRoleLabel(),
               C100APPLICANTBARRISTER1.getCaseRoleLabel()),
            of(C100APPLICANTSOLICITOR2.getCaseRoleLabel(),
               C100APPLICANTBARRISTER2.getCaseRoleLabel()),
            of(C100APPLICANTSOLICITOR3.getCaseRoleLabel(),
               C100APPLICANTBARRISTER3.getCaseRoleLabel()),
            of(C100APPLICANTSOLICITOR4.getCaseRoleLabel(),
               C100APPLICANTBARRISTER4.getCaseRoleLabel()),
            of(C100APPLICANTSOLICITOR5.getCaseRoleLabel(),
               C100APPLICANTBARRISTER5.getCaseRoleLabel()),
            of(C100RESPONDENTSOLICITOR1.getCaseRoleLabel(),
               C100RESPONDENTBARRISTER1.getCaseRoleLabel()),
            of(C100RESPONDENTSOLICITOR2.getCaseRoleLabel(),
               C100RESPONDENTBARRISTER2.getCaseRoleLabel()),
            of(C100RESPONDENTSOLICITOR3.getCaseRoleLabel(),
               C100RESPONDENTBARRISTER3.getCaseRoleLabel()),
            of(C100RESPONDENTSOLICITOR4.getCaseRoleLabel(),
               C100RESPONDENTBARRISTER4.getCaseRoleLabel()),
            of(C100RESPONDENTSOLICITOR5.getCaseRoleLabel(),
               C100RESPONDENTBARRISTER5.getCaseRoleLabel()),
            of(FL401APPLICANTSOLICITOR.getCaseRoleLabel(),
               FL401APPLICANTBARRISTER.getCaseRoleLabel()),
            of(FL401RESPONDENTSOLICITOR.getCaseRoleLabel(),
               FL401RESPONDENTBARRISTER.getCaseRoleLabel())
        );
    }

    @ParameterizedTest
    @MethodSource("parameterRoles")
    void testFindMappingBarristerRole(String solicitorRole, String expectedBarristerRole) {

        Optional<String> derivedBarristerRole = Arrays.stream(BarristerRole.values())
            .filter(barristerRole -> barristerRole.getSolicitorCaseRole().equals(solicitorRole))
            .map(BarristerRole::getCaseRoleLabel)
            .findFirst();

        assertThat(derivedBarristerRole)
            .hasValue(expectedBarristerRole);
    }
}
