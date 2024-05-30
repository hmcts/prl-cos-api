package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.spendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;

@RunWith(MockitoJUnitRunner.class)
 class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @BeforeEach
    public void setUp() throws IOException {
        setValue();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        setValue();
    }

    private void setValue() throws IOException {
        MockitoAnnotations.openMocks(this);
        mapper.registerModule(new JSR310Module());
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
                .documentManagementDetails(DocumentManagementDetails.builder()
                                               .citizenQuarantineDocsList(new ArrayList<>())
                                               .build())
                .taskListVersion("v2")
                .c100RebuildData(C100RebuildData.builder()
                .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
                .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
                .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
                .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
                .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam.json"))
                .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
                .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
                .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
                .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
                .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
                .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
                .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
                .c100RebuildSafetyConcerns(TestUtil.readFileFrom("classpath:c100-rebuild/saftycrns.json"))
                .build())
                .build();
    }


    @Test
    public void testCaseDataMapper() throws IOException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        JSONAssert.assertEquals(TestUtil.readFileFrom("classpath:c100-rebuild/mapper-response.json"),
                mapper.writeValueAsString(updatedCaseData), false);
    }

    @Test
    public void testCaseDataMapperForOrderTypeExtraFields() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too1.json"))
                        .build())
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                updatedCaseData.getOrdersApplyingFor());
        assertEquals(liveWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
    }

    @Test
    public void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op1.json"))
                        .build())
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertThat(updatedCaseData.getExistingProceedings()).isEmpty();
    }

    @Test
    public void testCaseDataMapperForMiamExtraFields() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam1.json"))
                        .build())
                .build();


        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertThat(updatedCaseData.getMiamDetails().getMiamExemptionsChecklist()).isEmpty();
    }

    @Test
    public void testCaseDataMapperForChildDetail() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd1.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getNewChildDetails());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetail() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd1.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOtherChildren());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetailNull() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd2.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getOtherChildren());
    }

    @ParameterizedTest
    @ValueSource(strings = {"classpath:c100-rebuild/ra1.json", "classpath:c100-rebuild/ra2.json", "classpath:c100-rebuild/ra3.json"})
    void testCaseDataMapperReasonableAdjustmentsExtraFields1(String resourcePath) throws IOException {
        CaseData caseData1 = caseData
            .toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                                 .c100RebuildReasonableAdjustments(TestUtil.readFileFrom(resourcePath))
                                 .build())
            .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetails() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs1.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetailsUnknownDoB() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs2.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForRespondentDetails() throws IOException {
        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp1.json"))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getRespondents());
    }

    @Test
    public void testCaseDataMapperWhenAllBlocksEmpty() throws IOException {

        //When
        CaseData caseData1 = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder().citizenQuarantineDocsList(new ArrayList<>())
                                           .build())
            .c100RebuildData(C100RebuildData.builder().build()).build();
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertEquals(caseData1, updatedCaseData);
    }

    @Test
    public void testCaseDataMapperWhenUrgencyDataEmpty() throws IOException {

        //When
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu1.json"))
                        .build())
                .build();

        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperNoUrgencyCase() throws IOException {

        //When
        CaseData caseData1 = caseData
            .toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                                 .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu2.json"))
                                 .build())
            .build();

        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForHearingWithoutNotice() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForHearingWithoutNoticeElse() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn1.json"))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForOrderTypeBothLiveWithAndSpendTimeWithOrder() throws IOException {

        //Given
        CaseData caseData1 = caseData
            .toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .build()).build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                     updatedCaseData.getOrdersApplyingFor());
        assertEquals(bothLiveWithAndSpendTimeWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
    }

    @Test
    public void testCaseDataMapperForOrderTypeNatureOfOrderShortStatement() throws IOException {

        //Given
        CaseData caseData1 = caseData
            .toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too2.json"))
            .build()).build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                     updatedCaseData.getOrdersApplyingFor());
        assertEquals(spendTimeWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
    }

    @Test
    public void testCaseDataMapperForChildDetailOrdersAppliedForAll() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getNewChildDetails());
    }

    @Test
    public void testCaseDataMapperConsentOrderDetails() throws IOException {
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getConsentOrder());
    }

    @ParameterizedTest
    @ValueSource(strings = {"classpath:c100-rebuild/saftycrns.json", "classpath:c100-rebuild/saftycrnsWithoutDomesticAbuse.json",
        "classpath:c100-rebuild/saftycrnsWithoutChildAbuses.json"})
     void testCaseDataMapperForSafetyConcerns(String resourcePath) throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                                 .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                                 .c100RebuildSafetyConcerns(TestUtil.readFileFrom(resourcePath))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);
        //Then
        assertNotNull(updatedCaseData);

    }

    @ParameterizedTest
    @ValueSource(strings = {"classpath:c100-rebuild/saftycrnsForASingleChild.json"})
    void testCaseDataMapperForSafetyConcernsForASingleChild(String resourcePath) throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                                 .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                                 .c100RebuildSafetyConcerns(TestUtil.readFileFrom(resourcePath))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);
        //Then
        assertNotNull(updatedCaseData);

    }

    @Test
    public void testCaseWhen_No_haveSafetyConcerns() throws IOException {

        String whenHaveSafetyConcernsIsNo = "{\"c1A_haveSafetyConcerns\":\"No\"}";

        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                                 .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                                 .c100RebuildSafetyConcerns(whenHaveSafetyConcernsIsNo)
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);
        //Then
        assertNotNull(updatedCaseData);

    }

}
