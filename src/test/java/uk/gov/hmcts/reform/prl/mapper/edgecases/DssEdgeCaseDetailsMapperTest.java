package uk.gov.hmcts.reform.prl.mapper.edgecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DssCaseDetails;
import uk.gov.hmcts.reform.prl.models.edgecases.DssCaseData;

import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class DssEdgeCaseDetailsMapperTest {
    private final DssEdgeCaseDetailsMapper dssEdgeCaseDetailsMapper = new DssEdgeCaseDetailsMapper();
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUpdateDssCaseData() {
        CaseData caseData1 = CaseData.builder().dssCaseDetails(DssCaseDetails.builder().dssCaseData("dssCaseDataObject").build()).build();
        Assert.assertEquals(
            "dssCaseDataObject",
            dssEdgeCaseDetailsMapper.updateDssCaseData(caseData1).get("dssCaseData")
        );

    }

    @Test
    public void testUpdateDssCaseDataWhenDssCaseDetailsIsNull() throws JsonProcessingException  {
        Assert.assertNotNull(dssEdgeCaseDetailsMapper.mapDssCaseData(CaseData.builder().build(), null));
    }

    @Test
    public void testMapCaseDataC100() throws JsonProcessingException {
        Assert.assertNotNull(dssEdgeCaseDetailsMapper.mapDssCaseData(
            CaseData.builder().dssCaseDetails(
                DssCaseDetails.builder().build()).caseTypeOfApplication("C100").build(),
            DssCaseDetails.builder().dssCaseData(mapper.writeValueAsString(buildDssCaseData(
                "refNum",
                "applicantEmailAdd",
                "FGM",
                "self"
            ))).build()
        ));
    }

    @Test
    public void testMapCaseData() throws JsonProcessingException {
        Assert.assertNotNull(dssEdgeCaseDetailsMapper.mapDssCaseData(
            CaseData.builder().caseTypeOfApplication("C100").dssCaseDetails(
                DssCaseDetails.builder().build()).caseTypeOfApplication("C100").build(),
            DssCaseDetails.builder().dssCaseData(mapper.writeValueAsString(buildDssCaseData(
                "refNum",
                "applicantEmailAdd",
                "FGM",
                "family"
            ))).build()
        ));
    }

    @Test
    public void testMapCaseDataForFl401() throws JsonProcessingException {
        Assert.assertNotNull(dssEdgeCaseDetailsMapper.mapDssCaseData(
            CaseData.builder().caseTypeOfApplication("FL401").dssCaseDetails(
                DssCaseDetails.builder().build()).caseTypeOfApplication("FL401").build(),
            DssCaseDetails.builder().dssCaseData(mapper.writeValueAsString(buildDssCaseData(
                null,
                null,
                "FMPO",
                null
            ))).build()
        ));
    }


    private DssCaseData buildDssCaseData(String helpWithFeesReferenceNumber, String applicantEmailAddress,
                                         String edgeCaseTypeOfApp, String applyingFor) {
        return DssCaseData.builder().edgeCaseTypeOfApplication(edgeCaseTypeOfApp)
            .helpWithFeesReferenceNumber(helpWithFeesReferenceNumber).selectedCourtId("courtId").applicantApplicationFormDocuments(
                List.of(
                    Document.builder().build())).applicantAdditionalDocuments(List.of(
                Document.builder().build())).applicantFirstName("applicantFirstName").applicantLastName(
                "applicantLastName")
            .applicantDateOfBirth(DateofBirth.builder().year("1999").day("1").month("2").build())
            .applicantEmailAddress(applicantEmailAddress)
            .applicantPhoneNumber("1234567890")
            .applicantAddress1("address1")
            .applicantAddress2("address2")
            .applicantAddressTown("Town")
            .applicantAddressCountry("Country")
            .applicantAddressPostcode("PostCode")
            .applicantAddressCounty("Country")
            .whomYouAreApplying(applyingFor)
            .build();
    }

}
