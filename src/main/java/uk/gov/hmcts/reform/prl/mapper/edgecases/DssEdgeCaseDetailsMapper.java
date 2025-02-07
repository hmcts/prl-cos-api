package uk.gov.hmcts.reform.prl.mapper.edgecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DssCaseDetails;
import uk.gov.hmcts.reform.prl.models.edgecases.DssCaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.CaseUtils.buildDateOfBirth;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DssEdgeCaseDetailsMapper {

    public Map<String, Object> updateDssCaseData(CaseData caseData) {
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (null != caseData.getDssCaseDetails()) {
            caseDataMapToBeUpdated.put("dssCaseData", caseData.getDssCaseDetails().getDssCaseData());
        }

        return caseDataMapToBeUpdated;
    }

    public CaseData mapDssCaseData(CaseData caseData, DssCaseDetails dssCaseDetails) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CaseData.CaseDataBuilder<?,?> caseDataBuilder = caseData.toBuilder();

        //Submit the case data to CCD with data mapped from DSS
        if (null != dssCaseDetails
            && StringUtils.isNotEmpty(dssCaseDetails.getDssCaseData())) {
            DssCaseData dssCaseData = mapper.readValue(dssCaseDetails.getDssCaseData(), DssCaseData.class);

            caseDataBuilder
                .applicants(List.of(getDssApplicantPartyDetails(dssCaseData)))
                .dssCaseDetails(caseDataBuilder.build().getDssCaseDetails().toBuilder()
                                    .edgeCaseTypeOfApplication(dssCaseData.getEdgeCaseTypeOfApplication())
                                    .selectedCourtId(dssCaseData.getSelectedCourtId())
                                    .dssApplicationFormDocuments(
                                        wrapElements(dssCaseData.getApplicantApplicationFormDocuments()))
                                    .dssAdditionalDocuments(
                                        wrapElements(dssCaseData.getApplicantAdditionalDocuments())).build());
        }
        log.info("Case data mapped from DSS: {}", caseDataBuilder.build());
        return caseDataBuilder.build();
    }

    private Element<PartyDetails> getDssApplicantPartyDetails(DssCaseData dssCaseData) {
        return element(PartyDetails.builder()
                .firstName(dssCaseData.getApplicantFirstName())
                .lastName(dssCaseData.getApplicantLastName())
                .dateOfBirth(buildDateOfBirth(dssCaseData.getApplicantDateOfBirth()))
                .email(dssCaseData.getApplicantEmailAddress())
                .phoneNumber(dssCaseData.getApplicantPhoneNumber())
                .landline(dssCaseData.getApplicantHomeNumber()) //recheck
                .address(Address.builder()
                        .addressLine1(dssCaseData.getApplicantAddress1())
                        .addressLine2(dssCaseData.getApplicantAddress2())
                        .postTown(dssCaseData.getApplicantAddressTown())
                        .county(dssCaseData.getApplicantAddressCounty())
                        .postCode(dssCaseData.getApplicantAddressPostCode())
                        .country(dssCaseData.getApplicantAddressCountry())
                        .build())
                .build());
    }
}
