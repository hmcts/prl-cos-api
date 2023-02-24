package uk.gov.hmcts.reform.prl.services;

import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingPrePopulateServiceTest {

    @InjectMocks
    HearingPrePopulateService hearingPrePopulateService;

    @Mock
    RefDataUserService refDataUserService;

    public static final String authToken = "Bearer TestAuthToken";

    @Test()
    public void testPrePopulateHearingType() {
        List<DynamicListElement> listOfHearingType = new ArrayList<>();
        listOfHearingType.add(DynamicListElement.builder().code("HearingTypeKey").label("HearingType")
                                  .build());

        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE)).thenReturn(listOfHearingType);
        List<DynamicListElement> expectedResponse = hearingPrePopulateService.prePopulateHearingType(authToken);
        assertEquals(expectedResponse.get(0).getCode(),"HearingTypeKey");
        assertEquals(expectedResponse.get(0).getLabel(),"HearingType");
    }


}





