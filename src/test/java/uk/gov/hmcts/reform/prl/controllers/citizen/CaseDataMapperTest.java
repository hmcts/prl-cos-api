package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @Before
    public void setUp() throws IOException {
        mapper.registerModule(new JSR310Module());
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
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
                .build();
    }

    @Test
    public void testCaseDataMapper() throws IOException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        JSONAssert.assertEquals(TestUtil.readFileFrom("classpath:c100-rebuild/response.json"),
                mapper.writeValueAsString(updatedCaseData), false);
    }

    @Test
    public void testCaseDataMapperForOrderTypeExtraFields() throws JsonProcessingException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildTypeOfOrder("{\"too_courtOrder\":[\"whoChildLiveWith\","
                        + "\"stopOtherPeopleDoingSomething\"" + ",\"resolveSpecificIssue\"],\"too_stopOtherPeopleDoingSomethingSubField"
                        + "\":[\"changeChildrenNameSurname\",\"allowMedicalTreatment\",\"takingChildOnHoliday\","
                        + "\"relocateChildrenDifferentUkArea\",\"relocateChildrenOutsideUk\"],\"too_resolveSpecificIssueSubField"
                        + "\":[\"specificHoliday\",\"whatSchoolChildrenWillGoTo\",\"religiousIssue\",\"changeChildrenNameSurnameA"
                        + "\",\"medicalTreatment\",\"relocateChildrenDifferentUkAreaA\",\"relocateChildrenOutsideUkA\","
                        + "\"returningChildrenToYourCare\"]}")
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
    public void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws JsonProcessingException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildOtherProceedings("{\n   \"op_childrenInvolvedCourtCase\": \"No\",\n\"op_courtOrderProtection\": "
                        + "\"No\",\n   \"op_courtProceedingsOrders\": []\n}")
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNull(updatedCaseData.getExistingProceedings());
    }

    @Test
    public void testCaseDataMapperForMiamExtraFields() throws JsonProcessingException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildMaim("{\n  \"miam_otherProceedings\": \"No\",\n\"miam_consent\": \"s\",\n\"miam_attendance\": "
                        + "\"No\",\n\"miam_haveDocSigned\": \"Yes\",\n\"miam_mediatorDocument\": \"No\",\n\"miam_validReason\": "
                        + "\"Yes\",\n\"miam_nonAttendanceReasons\": [\n\"none\"\n],\n\"miam_certificate\": {\n  \"id\": "
                        + "\"test\",\n  \"url\": \"test\",\n  \"filename\": \"test\",\n  \"binaryUrl\": \"test\"\n}\n}")
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getMiamExemptionsChecklist());
    }

    @Test
    public void testCaseDataMapperForChildDetail() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildChildDetails("{\"cd_children\":"
                  + "[{\"id\":\"6c2505da-dae5-4541-9df5-5f4045f0ad4a\",\"firstName\":\"c1\",\"lastName\":\"c11\","
                 + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2021\",\"month\":\"10\",\"day\":\"10\"},\""
                 + "isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month\":\"\",\"year\":\"\"},\""
                 + "gender\":\"Female\",\"otherGenderDetails\":\"\"},\"childMatters\":{\"needsResolution\":"
                 + "[\"whoChildLiveWith\"]},\"parentialResponsibility\":{\"statement\":\"test11\"}},{\"id\":\""
                 + "ce9a93c4-8d7d-4aeb-8ac5-619de4d91a8c\",\"firstName\":\"c2\",\"lastName\":\"c22\",\"personalDetails\""
                 + ":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\"isDateOfBirthUnknown\":\"Yes\","
                  + "\"approxDateOfBirth\":{\"year\":\"2000\",\"month\":\"10\",\"day\":\"20\"},\"gender\":\"Other\",\""
                 + "otherGenderDetails\":\"TestOther\"},\"childMatters\":{\"needsResolution\":[\"childTimeSpent\"]},"
             + "\"parentialResponsibility\":{\"statement\":\"test22\"}}],\"cd_childrenKnownToSocialServices\":\"Yes\","
                 + "\"cd_childrenKnownToSocialServicesDetails\":\"Testchild\",\"cd_childrenSubjectOfProtectionPlan\":\""
                  + "Dontknow\"}").build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getChildren());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetail() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildOtherChildrenDetails("{\"ocd_hasOtherChildren\":\"Yes\","
                      + "\"ocd_otherChildren\":"
                      + "[{\"id\":\"a6c3e7f1-ce2f-42a7-b60e-82b80f8f36ab\",\"firstName\":\"test1\",\"lastName\":\"test11\","
                      + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2000\",\"month\":\"12\",\"day\":\"7\"},"
                      + "\"isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month\":\"\",\"year\":\"\"},"
                      + "\"gender\":\"Male\",\"otherGenderDetails\":\"\"},\"childMatters\":{\"needsResolution\":[]},"
                    + "\"parentialResponsibility\":{\"statement\":\"\"}},{\"id\":\"498bbf69-f8ab-45bb-a762-1810a339566f\","
                  + "\"firstName\":\"test2\",\"lastName\":\"test22\",\"personalDetails\":{\"dateOfBirth\":"
                  + "{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\"isDateOfBirthUnknown\":\"Yes\",\"approxDateOfBirth\":"
                  + "{\"year\":\"2012\",\"month\":\"8\",\"day\":\"8\"},\"gender\":\"Other\",\"otherGenderDetails\":\"test\"},"
              + "\"childMatters\":{\"needsResolution\":[]},\"parentialResponsibility\":{\"statement\":\"\"}}]}")
                                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOtherChildren());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetailNull() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildOtherChildrenDetails("{\"ocd_hasOtherChildren\":\"No\",\"ocd_otherChildren\":"
                  + "[{\"id\":\"a6c3e7f1-ce2f-42a7-b60e-82b80f8f36ab\",\"firstName\":\"test1\",\"lastName\":\"test11\","
                  + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2000\",\"month\":\"12\",\"day\":\"7\"},"
                  + "\"isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month\":\"\",\"year\":\"\"},"
                  + "\"gender\":\"Male\",\"otherGenderDetails\":\"\"},\"childMatters\":{\"needsResolution\":[]},"
                  + "\"parentialResponsibility\":{\"statement\":\"\"}},{\"id\":\"498bbf69-f8ab-45bb-a762-1810a339566f\","
                  + "\"firstName\":\"test2\",\"lastName\":\"test22\",\"personalDetails\":{\"dateOfBirth\":"
                  + "{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\"isDateOfBirthUnknown\":\"Yes\",\"approxDateOfBirth\":"
                  + "{\"year\":\"2012\",\"month\":\"8\",\"day\":\"8\"},\"gender\":\"Other\",\"otherGenderDetails\":\"test\"},"
                  + "\"childMatters\":{\"needsResolution\":[]},\"parentialResponsibility\":{\"statement\":\"\"}}]}")
            .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getOtherChildren());
    }

    @Test
    public void testCaseDataMapperReasonableAdjustmentsExtraFields() throws JsonProcessingException {
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildReasonableAdjustments("{\n  \"ra_typeOfHearing\": [\n\"videoHearing\",\n\"phoneHearing\"\n],"
                        + "\n\"ra_languageNeeds\": [\n\"speakInWelsh\",\n\"readAndWriteInWelsh\","
                        + "\n\"needInterpreterInCertainLanguage\"\n],\n\"ra_needInterpreterInCertainLanguage_subfield\": "
                        + "\"test\",\n\"ra_specialArrangements\": [\n\"noSafetyRequirements\"\n],"
                        + "\n\"ra_disabilityRequirements\": [\n\"noSupport\"\n]\n}")
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetails() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildOtherPersonsDetails("{\"oprs_otherPersons\":[{"
              + "\"id\":\"530b66b8-b718-4aca-bc29-09cca1c0429f\",\"firstName\":\"c1\",\"lastName\":\"c1\","
              + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"1990\",\"month\":\"12\",\"day\":\"12\"},"
              + "\"isDateOfBirthUnknown\":\"\",\"isNameChanged\":\"yes\",\"previousFullName\":\"previous name\","
              + "\"approxDateOfBirth\":{\"day\":\"\",\"month\":\"\",\"year\":\"\"},\"gender\":\"Male\","
              + "\"otherGenderDetails\":\"\"},\"relationshipDetails\":{\"relationshipToChildren\":[{\"childId\":"
              + "\"4a9f3ec0-c359-4dc0-9e94-e4fc868f0341\",\"relationshipType\":\"Mother\","
              + "\"otherRelationshipTypeDetails\":\"\"}]},\"address\":{\"AddressLine1\":\"add1\","
              + "\"AddressLine2\":\"add2\",\"AddressLine3\":\"add3\",\"PostTown\":\"\",\"County\":\"thames\",\"PostCode\":\"tw22tr8\","
              + "\"Country\":\"uk\"}}]}")
            .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetailsUnknownDoB() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildOtherPersonsDetails("{\"oprs_otherPersons\":"
             + "[{\"id\":\"530b66b8-b718-4aca-bc29-09cca1c0429f\",\"firstName\":\"c1\",\"lastName\":\"c1\","
             + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},"
             + "\"isDateOfBirthUnknown\":\"Yes\",\"isNameChanged\":\"yes\",\"previousFullName\":\"previous name\","
             + "\"approxDateOfBirth\":{\"day\":\"12\",\"month\":\"12\",\"year\":\"1990\"},\"gender\":\"Other\","
             + "\"otherGenderDetails\":\"Test\"},\"relationshipDetails\":{\"relationshipToChildren\":[{\"childId\":"
             + "\"4a9f3ec0-c359-4dc0-9e94-e4fc868f0341\",\"relationshipType\":\"Mother\","
             + "\"otherRelationshipTypeDetails\":\"\"}]},\"address\":{\"AddressLine1\":\"address1\","
             + "\"AddressLine2\":\"address2\",\"AddressLine3\":\"address3\",\"PostTown\":\"town\",\"County\":\"sdy\","
             + "\"PostCode\":\"tw23tr9\",\"Country\":\"uk\"}}]}")
            .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForRespondentDetails() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildRespondentDetails("{\"resp_Respondents\""
                                + ":[{\"id\":\"5739186d-e782-4e49-9f0e-dc62729fdbf2\","
                                + "\"firstName\":\"Nir\",\"lastName\":\"Sin\",\"personalDetails\""
                                + ":{\"hasNameChanged\":\"Yes\",\"resPreviousName\":\"\",\"dateOfBirth\""
                                + ":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\"isDateOfBirthUnknown\":"
                                + "\"Yes\",\"approxDateOfBirth\":{\"year\":\"1993\",\"month\":\"11\","
                                + "\"day\":\"22\"},\"gender\":\"Other\",\"otherGenderDetails\":\"Male\","
                                + "\"respondentPlaceOfBirth\":\"London\",\"respondentPlaceOfBirthUnknown\":\"No\"},"
                                + "\"address\":{\"AddressLine1\":\"FLAT23,THAMESVIEW,AXONPLACE\",\"AddressLine2\":"
                                + "\"CENTREWAYAPARTMENTS\",\"PostTown\":\"ILFORD\",\"PostCode\":\"IG11NB\","
                                + "\"selectedAddress\":\"ILFORD\",\"addressHistory\":\"Yes\",\"provideDetailsOfPreviousAddresses\":"
                                + "\"\",\"County\":\"\"},\"relationshipDetails\":{\"relationshipToChildren\":"
                                + "[{\"childId\":\"2e665739-0578-46cf-a4c4-bdaaefd61b0a\",\"relationshipType\":"
                                + "\"Other\",\"otherRelationshipTypeDetails\":\"others\"}]},\"contactDetails\""
                                + ":{\"emailAddress\":\"abc@gmail.com\",\"telephoneNumber\":\"+447205308786\","
                                + "\"donKnowEmailAddress\":\"No\",\"donKnowTelephoneNumber\":\"No\"}}]}").build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getRespondents());
    }

}
