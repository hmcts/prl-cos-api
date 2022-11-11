package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.Gender.male;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.childProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.domesticViolence;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.other;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.previousMIAMattendance;
import static uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum.urgency;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
import static uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum.careOrder;
import static uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum.superviosionOrder;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
                .c100RebuildInternationalElements("{\"ie_internationalStart\":\"Yes\",\"ie_internationalParents\":\"Yes\""
                        + ",\"ie_provideDetailsParents\":\"Child'sParentslifeoutsideUK\",\"ie_internationalJurisdiction\":\"Yes\""
                        + ",\"ie_provideDetailsJurisdiction\":\"AnotherpersoncanapplyoutsideUK\",\"ie_internationalRequest\":\"Yes\""
                        + ",\"ie_provideDetailsRequest\":\"Anothercountryorjurisdictiondetails\"}")
                .c100RebuildHearingWithoutNotice("{\"hwn_hearingPart1\":\"No\",\"hwn_reasonsForApplicationWithoutNotice\":\"WNH Details\""
                        + "," + "\"hwn_doYouNeedAWithoutNoticeHearing\":\"Yes\",\"hwn_doYouNeedAWithoutNoticeHearingDetails\":"
                        + "\"Other people will do something\",\"hwn_doYouRequireAHearingWithReducedNotice\":\"Yes\","
                        + "\"hwn_doYouRequireAHearingWithReducedNoticeDetails\":\""
                        + "No time to give notice\"}")
                .c100RebuildTypeOfOrder("{\"too_courtOrder\":[\"whoChildLiveWith\",\"childTimeSpent\","
                        + "\"stopOtherPeopleDoingSomething\"" + ",\"resolveSpecificIssue\"],\"too_stopOtherPeopleDoingSomethingSubField"
                        + "\":[\"changeChildrenNameSurname\",\"allowMedicalTreatment\",\"takingChildOnHoliday\","
                        + "\"relocateChildrenDifferentUkArea\",\"relocateChildrenOutsideUk\"],\"too_resolveSpecificIssueSubField"
                        + "\":[\"specificHoliday\",\"whatSchoolChildrenWillGoTo\",\"religiousIssue\",\"changeChildrenNameSurnameA"
                        + "\",\"medicalTreatment\",\"relocateChildrenDifferentUkAreaA\",\"relocateChildrenOutsideUkA\","
                        + "\"returningChildrenToYourCare\"],\"too_shortStatement\":\"shortStatementFieldValue\"}")
                .c100RebuildOtherProceedings("{\n   \"op_childrenInvolvedCourtCase\": \"No\",\n\"op_courtOrderProtection\": "
                        + "\"Yes\",\n   \"op_courtProceedingsOrders\": [\n\"childArrangementOrder\",\n\"emergencyProtectionOrder"
                        + "\",\n\"supervisionOrder\",\n\"careOrder\",\n\"childAbductionOrder\",\n\"contactOrderForDivorce\","
                        + "\n\"contactOrderForAdoption\",\n\"childMaintenanceOrder\",\n\"financialOrder\",\n\"nonMolestationOrder"
                        + "\",\n\"occupationOrder\",\n\"forcedMarriageProtectionOrder\",\n\"restrainingOrder\","
                        + "\n\"otherInjuctionOrder\",\n\"undertakingOrder\",\n\"otherOrder\"\n],\n   \"op_otherProceedings\": "
                        + "{\n\"order\": {\n\"childArrangementOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"CO1\","
                        + "\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},"
                        + "\n\"currentOrder\": \"Yes\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},"
                        + "\n\"orderCopy\": \"Yes\",\n\"orderDocument\": {\n\"id\": \"36c5d12c-22ed-4a62-8625-b8b102b8d4a2\","
                        + "\n\"url\": \"http://dm-store-aat.service.core-compute-aat.internal/documents/36c5d12c-22ed"
                        + "-4a62-8625-b8b102b8d4a2\",\n\"filename\": \"applicant__supervision_order__10102022.pdf\","
                        + "\n\"binaryUrl\": \"http://dm-store-aat.service.core-compute-aat.internal/documents"
                        + "/36c5d12c-22ed-4a62-8625-b8b102b8d4a2/binary\"\n}\n}\n],\n\"emergencyProtectionOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"EO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"No\"\n}\n],\n\"supervisionOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"SO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"careOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": "
                        + "\"CO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"2021\",\n\"month\": \"11\",\n\"day\": "
                        + "\"11\"\n},\n\"currentOrder\": \"Yes\",\n\"orderEndDate\": {\n\"year\": \"2021\",\n\"month\": \"12\","
                        + "\n\"day\": \"11\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"childAbductionOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"AO\",\n\"caseNo\": \"BS19F99999\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"contactOrdersForDivorce\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"COD\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"contactOrdersForAdoption\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"COA\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"childMaintenanceOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"CMO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"financialOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"FO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"nonMolestationOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"NMO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"occupationOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"OO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"forcedMarriageProtectionOrders\": "
                        + "[\n{\n\"id\": \"1\",\n\"orderDetail\": \"FMPO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": "
                        + "\"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": "
                        + "{\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],"
                        + "\n\"restrainingOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"RO\",\n\"caseNo\": \"\","
                        + "\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"otherInjuctionOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"OIO\",\n\"caseNo\": "
                        + "\"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"undertakingOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"UO\",\n\"caseNo\": "
                        + "\"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"otherOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"OTO\",\n\"caseNo\": \"\","
                        + "\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\","
                        + "\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n]\n}\n}\n},\n   }\n}")
                .c100RebuildMaim("{\n  \"miam_otherProceedings\": \"No\",\n\"miam_consent\": \"s\",\n\"miam_attendance\": "
                        + "\"No\",\n\"miam_mediatorDocument\": \"No\",\n\"miam_validReason\": \"Yes\","
                        + "\n\"miam_nonAttendanceReasons\": [\n\"domesticViolence\",\n\"childProtection\",\n\"urgentHearing\","
                        + "\n\"previousMIAMOrExempt\",\n\"validExemption\"\n],\n\"miam_domesticAbuse\": [\n\"policeInvolvement"
                        + "\"\n],\n\"miam_domesticabuse_involvement_subfields\": [\n\"\",\n\"\",\n\"\",\n\"\",\n\"\","
                        + "\n\"evidenceOfSomeoneArrest\",\n\"evidenceOfPolice\",\n\"evidenceOfOnGoingCriminalProceeding\","
                        + "\n\"evidenceOfConviction\",\n\"evidenceOFProtectionNotice\"\n],"
                        + "\n\"miam_domesticabuse_courtInvolvement_subfields\": [\n\"\",\n\"\",\n\"\",\n\"\",\n\"\"\n],"
                        + "\n\"miam_domesticabuse_letterOfBeingVictim_subfields\": [\n\"\",\n\"\"\n],\n"
                        + "\"miam_domesticabuse_letterFromAuthority_subfields\": [\n\"\",\n\"\",\n\"\"\n],"
                        + "\n\"miam_domesticabuse_letterFromSupportService_subfields\": [],\n\"miam_childProtectionEvidence\": "
                        + "[\n\"localAuthority\",\n\"childProtectionPlan\"\n],\n\"miam_urgency\": "
                        + "[\n\"freedomPhysicalSafety\",\n\"freedomPhysicalSafetyInFamily\"\n],\n\"miam_previousAttendance\": "
                        + "[\n\"fourMonthsPriorAttended\",\n\"onTimeParticipation\"\n],\n\"miam_notAttendingReasons\": "
                        + "[\n\"noSufficientContactDetails\",\n\"applyingForWithoutNoticeHearing\"\n],"
                        + "\n\"miam_noMediatorAccessSubfields\": []\n}")
                .c100RebuildHearingUrgency("{\n  \"hu_urgentHearingReasons\": \"Yes\",\n\"hu_reasonOfUrgentHearing\":"
                        + " [\n\"risk of safety\",\n\"risk of child abduction\",\n\"overseas legal proceeding\","
                        + "\n\"other risks\"\n],\n\"hu_otherRiskDetails\": \"test\",\n\"hu_timeOfHearingDetails\": "
                        + "\"24 hours\",\n\"hu_hearingWithNext48HrsDetails\": \"Yes\",\n\"hu_hearingWithNext48HrsMsg\": "
                        + "\"48 hours\"\n}")
                .c100RebuildChildDetails("{\"cd_children\":[{\"id\":\"6c2505da-dae5-4541-9df5-5f4045f0ad4a\",\"firstName\":\""
                         + "c1\",\"lastName\":\"c11\",\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2021\",\"month\":\""
                         + "10\",\"day\":\"10\"},\"isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month"
                         + "\":\"\",\"year\":\"\"},\"gender\":\"Female\",\"otherGenderDetails\":\"\"},\"childMatters\":{\""
                         + "needsResolution\":[\"whoChildLiveWith\"]},\"parentialResponsibility\":{\"statement\":\"test11\""
                         + "}},{\"id\":\"ce9a93c4-8d7d-4aeb-8ac5-619de4d91a8c\",\"firstName\":\"c2\",\"lastName\":\"c22\","
                         + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\""
                         + "isDateOfBirthUnknown\":\"Yes\",\"approxDateOfBirth\":{\"year\":\"2000\",\"month\":\"10\",\"day\""
                         + ":\"20\"},\"gender\":\"Other\",\"otherGenderDetails\":\"TestOther\"},\"childMatters\":{"
                         + "\"needsResolution\":[\"childTimeSpent\"]},\"parentialResponsibility\":{\"statement\":\"test22"
                        + "\"}}],\"cd_childrenKnownToSocialServices\":\"Yes\",\"cd_childrenKnownToSocialServicesDetails\""
                        + ":\"Testchild\",\"cd_childrenSubjectOfProtectionPlan\":\"Dontknow\"}")
                .c100RebuildApplicantDetails("{\n   \"appl_allApplicants\":[\n      {\n         "
                        + "\"id\":\"f468e727-5691-45b0-b456-0d6156fb55d7\",\n         \"applicantFirstName\":\"c1\",\n         "
                        + "\"applicantLastName\":\"c1\",\n         \"detailsKnown\":\"Yes\",\n         "
                        + "\"startAlternative\":\"\",\n         \"start\":\"No\",\n         "
                        + "\"contactDetailsPrivate\":[\n            \n         ],\n         \"contactDetailsPrivateAlternative\""
                        + ":[\n            \n         ],\n         \"relationshipDetails\":{\n            "
                        + "\"relationshipToChildren\":[\n               {\n                  \"childId\":"
                        + "\"c4befd68-8cc2-4582-b4f5-630d41bda1c3\",\n                  \"relationshipType\":"
                        + "\"Father\",\n                  \"otherRelationshipTypeDetails\":\"\"\n               "
                        + "}\n            ]\n         },\n         \"personalDetails\":{\n            "
                        + "\"haveYouChangeName\":\"Yes\",\n            \"applPreviousName\":\"test\",\n            "
                        + "\"dateOfBirth\":{\n               \"day\":\"12\",\n               "
                        + "\"month\":\"12\",\n               \"year\":\"1990\"\n            },\n            "
                        + "\"gender\":\"Male\",\n            \"otherGenderDetails\":\"\",\n            "
                        + "\"applicantPlaceOfBirth\":\"India\"\n         },\n         \"applicantContactDetail\":{\n            "
                        + "\"canProvideEmail\":\"Yes\",\n            \"emailAddress\":\"test@gmail.com\",\n            "
                        + "\"canNotProvideEmailReason\":\"\",\n            \"canProvideTelephoneNumber\":\"Yes\",\n            "
                        + "\"telephoneNumber\":\"09999999999\",\n            \"canNotProvideTelephoneNumberReason\":\"\","
                        + "\n            \"canLeaveVoiceMail\":\"No\"\n         },\n         "
                        + "\"applicantAddressPostcode\":\"XXX XXX\",\n         \"applicantAddress1\":\"10\",\n         "
                        + "\"applicantAddress2\":\"Test Road\",\n         \"applicantAddressTown\":\"Harror\",\n         "
                        + "\"applicantAddressCounty\":\"Brent\",\n         \"applicantAddressHistory\":\"No\",\n         "
                        + "\"applicantProvideDetailsOfPreviousAddresses\":\"test\"\n      }\n   ]\n}")
                .c100RebuildOtherChildrenDetails("{\"ocd_hasOtherChildren\":\"Yes\",\"ocd_otherChildren\":[{\"id\":"
                         + "\"a6c3e7f1-ce2f-42a7-b60e-82b80f8f36ab\","
                         + "\"firstName\":\"test1\",\"lastName\":\"test11\",\"personalDetails\":{\"dateOfBirth\":"
                         + "{\"year\":\"2000\",\"month\":\"12\",\"day\":\"7\"},\"isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":"
                         + "{\"day\":\"\",\"month\":\"\",\"year\":\"\"},\"gender\":\"Male\",\"otherGenderDetails\":\"\"},"
                         + "\"childMatters\":{\"needsResolution\":[]},\"parentialResponsibility\":{\"statement\":\"\"}},"
                         + "{\"id\":\"498bbf69-f8ab-45bb-a762-1810a339566f\",\"firstName\":\"test2\",\"lastName\":\"test22\","
                         + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},"
                         + "\"isDateOfBirthUnknown\":\"Yes\",\"approxDateOfBirth\":{\"year\":\"2012\",\"month\":\"8\",\"day\":\"8\"},"
                         + "\"gender\":\"Other\",\"otherGenderDetails\":\"test\"},\"childMatters\":{\"needsResolution\":[]},"
                         + "\"parentialResponsibility\":{\"statement\":\"\"}}]}")
                .c100RebuildReasonableAdjustments("{\n  \"ra_typeOfHearing\": [\n\"videoHearing\",\n\"phoneHearing\"\n],"
                        + "\n\"ra_languageNeeds\": [\n\"speakInWelsh\",\n\"readAndWriteInWelsh\","
                        + "\n\"needInterpreterInCertainLanguage\"\n],\n\"ra_needInterpreterInCertainLanguageDetails\": "
                        + "\"test\",\n\"ra_specialArrangements\": [\n\"separateWaitingRoom\",\n\"separateExitEntrance\","
                        + "\n\"screenWithOtherPeople\",\n\"separateToilets\",\n\"visitCourtBeforeHearing\",\n\"videoLinks\","
                        + "\n\"specialArrangementsOther\"\n],\n\"ra_specialArrangementsOtherSubField\": \"test\","
                        + "\n\"ra_disabilityRequirements\": [\n\"documentsHelp\",\n\"communicationHelp\",\n\"extraSupport\","
                        + "\n\"feelComfortableSupport\",\n\"helpTravellingMovingBuildingSupport\"\n],"
                        + "\n\"ra_documentInformation\": "
                        + "[\n\"specifiedColorDocuments\",\n\"easyReadFormatDocuments\",\n\"brailleDocuments\","
                        + "\n\"largePrintDocuments\",\n\"audioTranslationDocuments\",\n\"readOutDocuments\","
                        + "\n\"emailInformation\",\n\"other\"\n],\n\"ra_specifiedColorDocumentsDetails\": \"test\","
                        + "\n\"ra_largePrintDocumentsDetails\": \"test\",\n\"ra_otherDetails\": \"test\",\n"
                        + "\"ra_communicationHelp\": [\n\"hearingLoop\",\n\"infraredReceiver\",\n\"needToBeClosedWithSpeaker\","
                        + "\n\"lipSpeaker\",\n\"signLanguageInterpreter\",\n\"speechToTextReporter\",\n\"needExtraTime\","
                        + "\n\"visitCourtBeforeHearing\",\n\"explanationOfCourt\",\n\"intermediary\",\n\"other\"\n],"
                        + "\n\"ra_signLanguageInterpreterDetails\": \"test\",\n\"ra_communicationHelpOtherDetails\": "
                        + "\"test\",\n\"ra_supportCourt\": [\n\"supportWorkerCarer\",\n\"friendFamilyMember\","
                        + "\n\"assistanceGuideDog\",\n\"therapyAnimal\",\n\"supportCourtOther\"\n],"
                        + "\n\"ra_supportWorkerCarerSubField\": \"test\",\n\"ra_friendFamilyMemberSubField\": "
                        + "\"test\",\n\"ra_therapyAnimalSubField\": \"test\",\n\"ra_supportCourtOtherSubField\": "
                        + "\"test\",\n\"ra_feelComportable\": [\n\"appropriateLighting\",\n\"regularBreaks\","
                        + "\n\"spaceUpAndMoveAround\",\n\"feelComportableOther\"\n],\n\"ra_appropriateLightingSubField\": "
                        + "\"test\",\n\"ra_feelComportableOtherSubField\": \"test\",\n\"ra_travellingCourt\": "
                        + "[\n\"parkingSpace\",\n\"wheelchairAccess\",\n\"venueWheelchair\",\n\"accessToilet\","
                        + "\n\"helpUsingLift\",\n\"differentTypeChair\",\n\"guideBuilding\",\n\"travellingCourtOther\"\n],"
                        + "\n\"ra_parkingSpaceSubField\": \"test\",\n\"ra_differentTypeChairSubField\": "
                        + "\"test\",\n\"ra_travellingCourtOtherSubField\": \"test\"\n}")
                .c100RebuildOtherPersonsDetails("{\"oprs_otherPersons\":[{\"id\":\"530b66b8-b718-4aca-bc29-09cca1c0429f\","
                        + "\"firstName\":\"c1\",\"lastName\":\"c1\",\"personalDetails\":{\"dateOfBirth\":"
                        + "{\"year\":\"1990\",\"month\":\"12\",\"day\":\"12\"},\"isDateOfBirthUnknown\":\"\","
                        + "\"isNameChanged\":\"yes\",\"previousFullName\":\"previous name\",\"approxDateOfBirth\":"
                        + "{\"day\":\"\",\"month\":\"\",\"year\":\"\"},\"gender\":\"Male\",\"otherGenderDetails\":\"\"},"
                        + "\"relationshipDetails\":{\"relationshipToChildren\":[{\"childId\":"
                        + "\"4a9f3ec0-c359-4dc0-9e94-e4fc868f0341\",\"relationshipType\":\"Mother\","
                        + "\"otherRelationshipTypeDetails\":\"\"}]},\"address\":{\"AddressLine1\":\"\","
                        + "\"AddressLine2\":\"\",\"AddressLine3\":\"\",\"PostTown\":\"\",\"County\":\"\","
                        + "\"PostCode\":\"\",\"Country\":\"\"}}]}")
                .c100RebuildRespondentDetails(" {\"resp_Respondents\":[{\"id\":\"5739186d-e782-4e49-9f0e-dc62729fdbf2\","
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
    }

    @Test
    public void testCaseDataMapper() throws JsonProcessingException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                updatedCaseData.getOrdersApplyingFor());
        assertEquals(bothLiveWithAndSpendTimeWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
        assertEquals("Changing the children's names or surname, Allowing medical treatment to be carried "
                        + "out on the children, Taking the children on holiday, Relocating the children to a different "
                        + "area in England and Wales, Relocating the children outside of England and Wales (including Scotland "
                        + "and Northern Ireland), A specific holiday or arrangement, What school the children will go to, "
                        + "A religious issue, Changing the children's names or surname, Medical treatment, Relocating the "
                        + "children to a different area in England and Wales, Relocating the children outside of England and "
                        + "Wales (including Scotland and Northern Ireland), "
                        + "Returning the children to your care, Short Statement Information - shortStatementFieldValue",
                updatedCaseData.getNatureOfOrder());
        assertEquals(No, updatedCaseData.getDoYouNeedAWithoutNoticeHearing());
        assertEquals("WNH Details, Details of without notice hearing because the other "
                        + "person or people may do something that would obstruct the order - Other people will do something",
                updatedCaseData.getReasonsForApplicationWithoutNotice());
        assertEquals(Yes, updatedCaseData.getDoYouRequireAHearingWithReducedNotice());
        assertEquals("No time to give notice", updatedCaseData.getSetOutReasonsBelow());
        assertEquals(Yes, updatedCaseData.getHabitualResidentInOtherState());
        assertEquals("Child'sParentslifeoutsideUK", updatedCaseData.getHabitualResidentInOtherStateGiveReason());
        assertEquals(Yes, updatedCaseData.getJurisdictionIssue());
        assertEquals("AnotherpersoncanapplyoutsideUK", updatedCaseData.getJurisdictionIssueGiveReason());
        assertEquals(Yes, updatedCaseData.getRequestToForeignAuthority());
        assertEquals("Anothercountryorjurisdictiondetails", updatedCaseData.getRequestToForeignAuthorityGiveReason());
        assertEquals(yes, updatedCaseData.getPreviousOrOngoingProceedingsForChildren());
        List<Element<ProceedingDetails>> proceedingDetails = updatedCaseData.getExistingProceedings();
        assertEquals(16, proceedingDetails.size());
        assertEquals(List.of(superviosionOrder), proceedingDetails.get(0).getValue().getTypeOfOrder());
        assertEquals(List.of(careOrder), proceedingDetails.get(1).getValue().getTypeOfOrder());
        assertEquals(No, updatedCaseData.getApplicantAttendedMiam());
        assertEquals(No, updatedCaseData.getOtherProceedingsMiam());
        assertEquals(No, updatedCaseData.getFamilyMediatorMiam());
        assertEquals("s", updatedCaseData.getApplicantConsentMiam());
        assertTrue(updatedCaseData.getMiamExemptionsChecklist().containsAll(List.of(domesticViolence,
                urgency, previousMIAMattendance, other, childProtectionConcern)));
        assertEquals(Yes, updatedCaseData.getIsCaseUrgent());
        assertEquals("Case Urgency Time - 24 hours Case Urgency Reasons - Risk to my safety or the "
                + "children's safety, Risk that the children will be abducted, Legal proceedings taking place overseas, "
                + "Other risks, test", updatedCaseData.getCaseUrgencyTimeAndReason());
        assertEquals("48 hours", updatedCaseData.getEffortsMadeWithRespondents());

        assertEquals(1, updatedCaseData.getApplicants().size());
        PartyDetails partyDetails = updatedCaseData.getApplicants().get(0).getValue();
        assertEquals("c1", partyDetails.getFirstName());
        assertEquals("c1", partyDetails.getLastName());
        assertEquals(LocalDate.of(1990, 12, 12), partyDetails.getDateOfBirth());
        assertEquals(male, partyDetails.getGender());

        assertEquals(Yes, updatedCaseData.getIsWelshNeeded());
        assertEquals(Yes, updatedCaseData.getIsIntermediaryNeeded());
        assertEquals(Yes, updatedCaseData.getIsInterpreterNeeded());
        assertEquals(Yes, updatedCaseData.getIsDisabilityPresent());
        assertEquals(Yes, updatedCaseData.getIsSpecialArrangementsRequired());
        assertEquals("I need documents in an alternative format: Documents in a specified colour(test), "
                + "Documents in Easy Read format, Braille documents, Documents in large print(test), Audio translation of "
                + "documents, Documents read out to me, Information emailed to me, Other(test), I need help communicating "
                + "and understanding: Hearing loop (hearing enhancement system), Infrared receiver (hearing enhancement system), "
                + "Need to be close to who is speaking, Lip speaker, Sign Language interpreter(test), Speech to text reporter "
                + "(palantypist), Extra time to think and explain myself, Visit to court before the hearing, "
                + "Explanation of the court and who's in the room at the hearing, Intermediary, Other(test), "
                + "I need to bring support with me to a hearing: A support worker or carer(test), "
                + "A friend or family member(test), Assistance / guide dog, Therapy animal(test), Other(test), "
                + "I need something to feel comfortable during a hearing: Appropriate lighting(test), Regular breaks, "
                + "Space to be able to get up and move around, Other(test), I need help travelling to, or moving around "
                + "court buildings: Parking space close to the venue(test), Step free / wheelchair access, Use of "
                + "venue wheelchair, Accessible toilet, Help using a lift, A different type of chair(test), "
                + "Guiding in the building, Other(test)", updatedCaseData.getAdjustmentsRequired());
        assertEquals("Separate waiting room, Separate exits and entrances, Screens so you and the other "
                + "people in the case cannot see each other, Separate toilets, Visit to court before the hearing, "
                + "Video links, Other(test)", updatedCaseData.getSpecialArrangementsRequired());
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
                        + "\n\"needInterpreterInCertainLanguage\"\n],\n\"ra_needInterpreterInCertainLanguageDetails\": "
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
