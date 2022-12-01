package uk.gov.hmcts.reform.prl.filter.cafcaas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.PostcodeLookupService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CafCassFilterTest {
    @Mock
    private PostcodeLookupService postcodeLookupService;

    @InjectMocks
    private CafCassFilter cafCassFilter;
    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    @org.junit.Test
    public void filterTest() throws IOException {
        List<String> caseTypeList = new ArrayList<>();
        caseTypeList.add("C100");
        ReflectionTestUtils.setField(cafCassFilter, "caseTypeList", caseTypeList);
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("SUBMITTED_PAID");
        ReflectionTestUtils.setField(cafCassFilter, "caseStateList", caseStateList);
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        CafCassResponse cafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );
        cafCassFilter.filter(cafCassResponse);
    }
}
