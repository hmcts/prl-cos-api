package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
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

@ExtendWith(MockitoExtension.class)
class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @BeforeEach
    void setUp() throws IOException {
        mapper.registerModule(new JSR310Module());
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
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
                .build())
                .build();
    }

    @Test
    void testCaseDataMapper() throws IOException {

        //When
        CaseData updatedCaseData1 = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        JSONAssert.assertEquals(TestUtil.readFileFrom("classpath:c100-rebuild/mapper-response.json"),
                mapper.writeValueAsString(updatedCaseData1), false);
    }

    @Test
    void testCaseDataMapperForOrderTypeExtraFields() throws IOException {

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
    void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws IOException {

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
    void testCaseDataMapperForMiamExtraFields() throws IOException {

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
        assertThat(updatedCaseData.getMiamExemptionsChecklist()).isEmpty();
    }

    @Test
    void testCaseDataMapperForChildDetail() throws IOException {
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
        assertNotNull(updatedCaseData.getChildren());
    }

    @Test
    void testCaseDataMapperForOtherChildrenDetail() throws IOException {
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
    void testCaseDataMapperForOtherChildrenDetailNull() throws IOException {
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
    @ValueSource(strings = {"ra1.json", "ra2.json", "ra3.json"})
    void testCaseDataMapperReasonableAdjustmentsExtraFields(String fileName) throws IOException {
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(caseData.getC100RebuildData().toBuilder()
                        .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/" + fileName))
                        .build())
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    void testCaseDataMapperForOtherPersonDetails() throws IOException {
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
    void testCaseDataMapperForOtherPersonDetailsUnknownDoB() throws IOException {
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
    void testCaseDataMapperForRespondentDetails() throws IOException {
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
    void testCaseDataMapperWhenAllBlocksEmpty() throws IOException {

        //When
        CaseData caseData1 = CaseData.builder().c100RebuildData(C100RebuildData.builder().build()).build();
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertEquals(caseData1, updatedCaseData);
    }

    @Test
    void testCaseDataMapperWhenUrgencyDataEmpty() throws IOException {

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
    void testCaseDataMapperForHearingWithoutNotice() throws IOException {
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
    void testCaseDataMapperForHearingWithoutNoticeElse() throws IOException {
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
    void testCaseDataMapperForOrderTypeBothLiveWithAndSpendTimeWithOrder() throws IOException {

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
    void testCaseDataMapperForOrderTypeNatureOfOrderShortStatement() throws IOException {

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
    void testCaseDataMapperForChildDetailOrdersAppliedForAll() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                                 .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getChildren());
    }

    @Test
    void testCaseDataMapperConsentOrderDetails() throws IOException {
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(caseData.getC100RebuildData().toBuilder()
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .build()).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getConsentOrder());
        assertNotNull(updatedCaseData.getDraftConsentOrderFile());
    }

}
