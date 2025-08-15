package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;


class PartyDetailsTest {

    @Test
    void testBarrister() throws JsonProcessingException {
        Barrister barrister = null;
        PartyDetails partyDetails = PartyDetails.builder().build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(partyDetails);
        System.out.println(s);


    }
}
