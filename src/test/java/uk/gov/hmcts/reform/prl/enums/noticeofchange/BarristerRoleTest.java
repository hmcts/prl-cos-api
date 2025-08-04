package uk.gov.hmcts.reform.prl.enums.noticeofchange;


import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BarristerRoleTest {

    @Test
    void test() {
        BarristerRole[] values = Arrays.stream(BarristerRole.values())
            .filter(barristerRole ->  barristerRole.getRepresenting().equals(BarristerRole.Representing.CAAPPLICANT))
            .toArray(BarristerRole[]::new);

        System.out.println(Arrays.toString(values));
        System.out.println(values[0].getCaseRoleLabel());
    }
}
