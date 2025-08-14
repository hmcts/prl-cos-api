package uk.gov.hmcts.reform.prl.services.extendedcasedataservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.ccd.ExtendedCaseDataApi;
import uk.gov.hmcts.reform.prl.models.extendedcasedetails.ExtendedCaseDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExtendedCaseDataServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private ExtendedCaseDataApi caseDataApi;

    @InjectMocks
    private ExtendedCaseDataService extendedCaseDataService;


    @Test
    public void testGetDataClassification() {
        Map<String, Object> dataClassification = new HashMap<>();
        dataClassification.put("draftConsentOrderFile", "PUBLIC");
        dataClassification.put("effortsMadeWithRespondents", "PUBLIC");
        dataClassification.put("effortsMadeWithRespondents", "PUBLIC");

        ExtendedCaseDetails caseDetails = ExtendedCaseDetails.builder()
            .dataClassification(dataClassification)
            .build();

        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(authTokenGenerator.generate()).thenReturn("test");
        when(caseDataApi.getExtendedCaseDetails("test", "test", "test")).thenReturn(caseDetails);

        assertEquals(dataClassification, extendedCaseDataService.getDataClassification("test"));

    }
}
