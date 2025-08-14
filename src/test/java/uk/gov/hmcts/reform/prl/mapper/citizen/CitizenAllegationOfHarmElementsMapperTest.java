package uk.gov.hmcts.reform.prl.mapper.citizen;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class CitizenAllegationOfHarmElementsMapperTest {

    @InjectMocks
    CitizenRespondentAohElementsMapper citizenAllegationOfHarmElementsMapper;

    List<Element<ChildDetailsRevised>> childrenList;

    @BeforeEach
    public void setup() {
        childrenList  = new ArrayList<>();
        childrenList.add(Element.<ChildDetailsRevised>builder()
                                  .id(UUID.fromString("ccd99bd3-29b8-4df5-93d6-b0a622ce033a"))
                                  .value(ChildDetailsRevised.builder()
                                             .firstName("test")
                                             .lastName("test")
                                             .build())
                                  .build());
        childrenList.add(Element.<ChildDetailsRevised>builder()
                                  .id(UUID.fromString("cbb66702-223f-42eb-93a0-b2146bc039e0"))
                                  .value(ChildDetailsRevised.builder()
                                             .firstName("test")
                                             .lastName("test")
                                             .build())
                                  .build());
        childrenList.add(Element.<ChildDetailsRevised>builder()
                                  .id(UUID.fromString("55af15c9-d969-4a1a-8a72-657dcbe56d27"))
                                  .value(ChildDetailsRevised.builder()
                                             .firstName("test")
                                             .lastName("test")
                                             .build())
                                  .build());
        childrenList.add(Element.<ChildDetailsRevised>builder()
                                  .id(UUID.fromString("2a147297-0dfc-467a-99d5-68db0e7e9411"))
                                  .value(ChildDetailsRevised.builder()
                                             .firstName("test")
                                             .lastName("test")
                                             .build())
                                  .build());
        childrenList.add(Element.<ChildDetailsRevised>builder()
                                  .id(UUID.fromString("fe50ccdc-b7fd-48ce-9853-454e8c484a78"))
                                  .value(ChildDetailsRevised.builder()
                                             .firstName("test")
                                             .lastName("test")
                                             .build())
                                  .build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"classpath:./respondentaohdata.json", "classpath:./aohwithabuductiondata.json",
        "classpath:./respondentaohdatanoad.json"})
    void shouldMapAllegationOfHarmDataWithYesOption(String fileName) throws Exception {
        String aohData = TestUtil.readFileFrom(fileName);
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData =
            citizenAllegationOfHarmElementsMapper.map(aohData, childrenList);
        Assert.assertNotNull(respondentAllegationsOfHarmData);
        Assert.assertEquals("Yes", respondentAllegationsOfHarmData.getRespAohYesOrNo().getDisplayedValue());
    }

    @Test
    public void shouldMapAllegationOfHarmDataWithNoOption() {
        String aohData = "{\"c1A_haveSafetyConcerns\":\"No\"}";
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData =
            citizenAllegationOfHarmElementsMapper.map(aohData, childrenList);
        Assert.assertNotNull(respondentAllegationsOfHarmData);
        Assert.assertEquals("No", respondentAllegationsOfHarmData.getRespAohYesOrNo().getDisplayedValue());
    }

    @Test
    public void shouldReturnEmptyDataWhenAohDataNotPresent() {
        String aohData = " ";
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData =
            citizenAllegationOfHarmElementsMapper.map(aohData, childrenList);
        Assert.assertNotNull(respondentAllegationsOfHarmData);
        Assert.assertNull(respondentAllegationsOfHarmData.getRespAohYesOrNo());
    }

}
