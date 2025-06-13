package uk.gov.hmcts.reform.prl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;

public class Test {

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String s = mapper.writeValueAsString(HearingRequest.class);
        System.out.println(s);
    }
}
