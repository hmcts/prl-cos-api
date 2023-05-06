package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ParentalResponsibilityType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadApplicationDraftOrder;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class UploadAdditionalApplicationServiceTest {

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UploadAdditionalApplicationService uploadAdditionalApplicationService;


    @Test
    public void testGetAdditionalApplicationElementsForBothC2AndOther() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
                .additionalApplicantsList(DynamicMultiSelectList.builder().build())
                .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order,
                                                           AdditionalApplicationTypeEnum.otherOrder))
                .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
                .temporaryC2Document(C2DocumentBundle.builder().build())
                .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
                .build();
        CaseData caseData = CaseData.builder()
                .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
                .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList
                = uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData);

        assertNotNull(additionalApplicationsElementList);
        assertEquals("test@abc.com", additionalApplicationsElementList.get(0).getValue().getAuthor());
    }

    @Test
    public void testGetAdditionalApplicationElementsForC2() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(Document.builder().build())
            .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
            .c2AdditionalOrdersRequested(List.of(C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT))
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
            .supplementsBundle(List.of(element(Supplement.builder().build())))
            .additionalDraftOrdersBundle(List.of(element(UploadApplicationDraftOrder.builder().build())))
            .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder().build())))
            .build();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicantsList(DynamicMultiSelectList.builder().build())
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.c2Order))
            .typeOfC2Application(C2ApplicationTypeEnum.applicationWithNotice)
            .temporaryC2Document(c2DocumentBundle)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder().build()));
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList
            = uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData);

        assertNotNull(additionalApplicationsElementList);
        assertEquals(2, additionalApplicationsElementList.size());
    }

    @Test
    public void testGetAdditionalApplicationElementsForOthe() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder().email("test@abc.com").build());
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsElementList
            = uploadAdditionalApplicationService.getAdditionalApplicationElements("auth", caseData);

        assertNotNull(additionalApplicationsElementList);
        assertEquals(1, additionalApplicationsElementList.size());
        assertEquals("test@abc.com", additionalApplicationsElementList.get(0).getValue().getAuthor());
    }

}
