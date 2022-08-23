package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    private CaseDataService caseDataService = new CaseDataService();
    private static final String jsonInString =
        "classpath:Cafca-json-27-June.json";
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void customBuilderShouldWork() throws IOException {
        String searchString = TestResourceUtil.readFileFrom("classpath:request/ccdsearchrequest.json");
        caseDataService.getCaseData(null, null, searchString);

        /*caseDataService.getCaseData("01/01/2022", "01/01/2023");
        CafCassResponse cafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );
        System.out.println(cafCassResponse);*/
    }

}
