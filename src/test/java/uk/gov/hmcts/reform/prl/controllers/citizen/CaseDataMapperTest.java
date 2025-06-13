package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;

//TO BE DELETED - NOT IN USE
@Disabled
@SuppressWarnings({"java:S1607"})
@ExtendWith(MockitoExtension.class)
class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;
    private C100RebuildData c100RebuildData;

    @BeforeEach
    void setUp() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        c100RebuildData = C100RebuildData.builder()
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
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(CASE_TYPE)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(new ArrayList<>())
                                           .build())
            .c100RebuildData(c100RebuildData)
                .build();
    }

    @Test
    @Disabled
    void testCaseDataMapper() throws IOException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        JSONAssert.assertEquals(TestUtil.readFileFrom("classpath:c100-rebuild/mapper-response.json"),
                mapper.writeValueAsString(updatedCaseData), false);
    }

    @Test
    void testCaseDataMapperForOrderTypeExtraFields() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(c100RebuildData
                                     .toBuilder()
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
    @Disabled
    void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildData(c100RebuildData
                                     .toBuilder()
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
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
    void testCaseDataMapperForChildDetail() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildData(c100RebuildData
                                 .toBuilder()
                                     .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd2.json"))
                                     .build())
            .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getOtherChildren());
    }

    @Test
    void testCaseDataMapperReasonableAdjustmentsExtraFields() throws IOException {
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
                                .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra1.json"))
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
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
        CaseData caseData1 = caseData.toBuilder()
                    .c100RebuildData(c100RebuildData
                                 .toBuilder()
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
        CaseData caseData1 = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(new ArrayList<>())
                                           .build())
            .c100RebuildData(C100RebuildData.builder().build())
            .build();
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertEquals(caseData1, updatedCaseData);
    }

    @Test
    void testCaseDataMapperWhenUrgencyDataEmpty() throws IOException {

        //When
        CaseData caseData1 = caseData.toBuilder()
            .c100RebuildData(c100RebuildData
                                 .toBuilder()
                                 .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu1.json"))
                                 .build())
            .build();
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

}
