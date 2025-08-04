package uk.gov.hmcts.reform.prl.enums.noticeofchange;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.Arrays;
import java.util.Map;

class BarristerRoleTest {

    @Test
    void test() {
        BarristerRole[] values = Arrays.stream(BarristerRole.values())
            .filter(barristerRole ->  barristerRole.getRepresenting().equals(BarristerRole.Representing.CAAPPLICANT))
            .toArray(BarristerRole[]::new);

        System.out.println(Arrays.toString(values));
        System.out.println(values[0].getCaseRoleLabel());
    }

    @Test
    void testSeralized() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Barrister.builder().build());
        System.out.println(s);
        CaseData caseData = CaseData.builder().applicants(
            ElementUtils.wrapElements(
                PartyDetails.builder()
                    .barrister(Barrister.builder().build())
                    .build()
            )
        ).build();

        CaseDetails build = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .build();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(mapper);
        CaseData caseData1 = caseDetailsConverter.extractCase(build);


        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(caseData1));
    }
    /**
     {
     "barristerOrg" : null,
     "barristerId" : null,
     "barristerName" : null,
     "barristerRole" : null,
     "barristerEmail" : null
     }

     { }
     */
}
