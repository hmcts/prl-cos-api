package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.languagecontext.UserLanguage;
import uk.gov.hmcts.reform.prl.models.wa.AdditionalProperties;
import uk.gov.hmcts.reform.prl.models.wa.ClientContext;
import uk.gov.hmcts.reform.prl.models.wa.TaskData;
import uk.gov.hmcts.reform.prl.models.wa.UserTask;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.enums.Event.EDIT_AND_APPROVE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.Event.REVIEW_ADDITIONAL_APPLICATION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReviewAdditionalApplicationServiceTest {

    @InjectMocks
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    @Mock
    private ElementUtils elementUtils;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testPopulateReviewAdditionalApplication() {
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicantName("test")
                                         .applicationType(
                                             OtherApplicationType
                                                 .D89_COURT_BAILIFF)
                                         .build())
            .build();

        Element<AdditionalApplicationsBundle> additionalApplicationsBundleElement = customElement(additionalApplicationsBundle);
        List<Element<AdditionalApplicationsBundle>> elementList = new ArrayList<>();
        elementList.add(additionalApplicationsBundleElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .additionalApplicationsBundle(elementList)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getAdditionalApplicationsBundle(), objectMapper)).thenReturn(additionalApplicationsBundleElement.getId());
        Map<String, Object> caseDataMap = reviewAdditionalApplicationService.populateReviewAdditionalApplication(
            caseData, new HashMap<>(), null, REVIEW_ADDITIONAL_APPLICATION.getId());
        assertNotNull(caseDataMap.get("selectedAdditionalApplicationsBundle"));
    }

    @Test
    public void testPopulateReviewAdditionalApplicationWhenClientContextIsNotNull() throws Exception {
        UUID applicationUId = UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b");
        WaMapper waMapper = WaMapper.builder()
            .clientContext(ClientContext.builder()
                .userLanguage(UserLanguage.builder().language("en").build())
                .userTask(UserTask.builder()
                    .completeTask(true)
                    .taskData(TaskData.builder()
                        .id("test")
                        .name("test")
                        .additionalProperties(AdditionalProperties.builder()
                            .orderId(UUID.randomUUID().toString())
                            .additionalApplicationId(applicationUId.toString())
                            .build())
                        .build())
                    .build())
                .build())
            .build();
        String json = new ObjectMapper().writeValueAsString(waMapper);
        String clientContextCoded = Base64.getEncoder().encodeToString(json.getBytes());
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicantName("test")
                                         .applicationType(
                                             OtherApplicationType
                                                 .D89_COURT_BAILIFF)
                                         .build())
            .build();

        Element<AdditionalApplicationsBundle> additionalApplicationsBundleElement = customElement(additionalApplicationsBundle);
        List<Element<AdditionalApplicationsBundle>> elementList = new ArrayList<>();
        elementList.add(additionalApplicationsBundleElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .additionalApplicationsBundle(elementList)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getAdditionalApplicationsBundle(), objectMapper)).thenReturn(additionalApplicationsBundleElement.getId());
        Map<String, Object> caseDataMap = reviewAdditionalApplicationService.populateReviewAdditionalApplication(
            caseData, new HashMap<>(), clientContextCoded,
            REVIEW_ADDITIONAL_APPLICATION.getId());

        assertNotNull(caseDataMap.get("selectedAdditionalApplicationsBundle"));
    }

    @Test
    public void testPopulateReviewAdditionalApplicationWhenEventIsNotReviewAdditionalApplication() {
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicantName("test")
                                         .applicationType(
                                             OtherApplicationType
                                                 .D89_COURT_BAILIFF)
                                         .build())
            .build();

        Element<AdditionalApplicationsBundle> additionalApplicationsBundleElement = customElement(additionalApplicationsBundle);
        List<Element<AdditionalApplicationsBundle>> elementList = new ArrayList<>();
        elementList.add(additionalApplicationsBundleElement);
        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondents = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .additionalApplicationsBundle(elementList)
            .previewOrderDoc(Document.builder().documentFileName("abc.pdf").build())
            .respondents(List.of(respondents))
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getAdditionalApplicationsBundle(), objectMapper)).thenReturn(additionalApplicationsBundleElement.getId());
        Map<String, Object> caseDataMap = reviewAdditionalApplicationService.populateReviewAdditionalApplication(
            caseData, new HashMap<>(), null, EDIT_AND_APPROVE_ORDER.getId());
        assertNotNull(caseDataMap.get("selectedAdditionalApplicationsBundle"));
    }

    @Test
    public void testGetOtherApplicationBundleDynamicCode() {
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicantName("test")
                                         .applicationType(
                                             OtherApplicationType
                                                 .D89_COURT_BAILIFF)
                                         .applicationStatus(AWP_STATUS_SUBMITTED)
                                         .uploadedDateTime("21-May-2025 10:19:50 am")
                                         .build())
            .build();
        String applicationBundleDynamicCode = reviewAdditionalApplicationService.getApplicationBundleDynamicCode(
            additionalApplicationsBundle);
        assertEquals("OT_21-May-2025 10:19:50 am", applicationBundleDynamicCode);
    }

    @Test
    public void testGetC2ApplicationBundleDynamicCode() {
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                                         .applicantName("test")
                                         .applicationStatus(AWP_STATUS_SUBMITTED)
                                         .uploadedDateTime("21-May-2025 10:19:50 am")
                                         .build())
            .build();
        String applicationBundleDynamicCode = reviewAdditionalApplicationService.getApplicationBundleDynamicCode(
            additionalApplicationsBundle);
        assertEquals("C2_21-May-2025 10:19:50 am", applicationBundleDynamicCode);
    }

    @Test
    public void testGetC2OrOtherApplicationBundleDynamicCodeShouldReturnNull() {
        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                                  .applicantName("test")
                                  .uploadedDateTime("21-May-2025 10:19:50 am")
                                  .build())
            .build();
        String applicationBundleDynamicCode = reviewAdditionalApplicationService.getApplicationBundleDynamicCode(
            additionalApplicationsBundle);
        assertNull(applicationBundleDynamicCode);
    }


    private static <T> Element<T> customElement(T element) {
        return Element.<T>builder()
            .id(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .value(element)
            .build();
    }
}
