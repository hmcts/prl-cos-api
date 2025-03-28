package uk.gov.hmcts.reform.prl.mapper.edgecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.edgecases.EdgeCaseTypeOfApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DssCaseDetails;
import uk.gov.hmcts.reform.prl.models.edgecases.DssCaseData;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails.FULL_NAME_FORMAT;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.buildDateOfBirth;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.generatePartyUuidForFL401;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.isNotEmpty;
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
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE));
            DssCaseData dssCaseData = mapper.readValue(dssCaseDetails.getDssCaseData(), DssCaseData.class);
            log.info("DSS CaseData {}", dssCaseData);
            EdgeCaseTypeOfApplicationEnum edgeCaseType = EdgeCaseTypeOfApplicationEnum.fromKey(dssCaseData.getEdgeCaseTypeOfApplication());
            caseDataBuilder
                .c100RebuildData(C100RebuildData.builder().applicantPcqId(dssCaseData.getApplicantPcqId()).build())
                .helpWithFeesNumber(dssCaseData.getHelpWithFeesReferenceNumber())
                .helpWithFees(isNotEmpty(dssCaseData.getHelpWithFeesReferenceNumber()) ? YesOrNo.Yes : null)
                .dssCaseDetails(caseDataBuilder.build().getDssCaseDetails().toBuilder()
                                    .isEdgeCase(YesOrNo.Yes)
                                    .edgeCaseTypeOfApplication(edgeCaseType)
                                    .edgeCaseTypeOfApplicationDisplayValue(edgeCaseType.getDisplayedValue())
                                    .selectedCourtId(dssCaseData.getSelectedCourtId())
                                    .dssApplicationFormDocuments(
                                        wrapElements(dssCaseData.getApplicantApplicationFormDocuments()))
                                    .dssAdditionalDocuments(
                                        wrapElements(dssCaseData.getApplicantAdditionalDocuments())).build())
                    .selectedCaseTypeID(caseData.getCaseTypeOfApplication())
                    .applicantName(String.format(FULL_NAME_FORMAT,
                            dssCaseData.getApplicantFirstName(), dssCaseData.getApplicantLastName()))
                    .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
                    .caseSubmittedTimeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime));
            if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                caseDataBuilder
                        .applicants(List.of(element(getDssApplicantPartyDetails(dssCaseData))));
            } else if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
                caseDataBuilder
                        .applicantsFL401(getDssApplicantPartyDetails(dssCaseData));
                generatePartyUuidForFL401(caseDataBuilder.build());
            }
        }
        log.info("Case data mapped from DSS: {}", caseDataBuilder.build());
        return caseDataBuilder.build();
    }

    private PartyDetails getDssApplicantPartyDetails(DssCaseData dssCaseData) {
        return PartyDetails.builder()
            .firstName(dssCaseData.getApplicantFirstName())
            .lastName(dssCaseData.getApplicantLastName())
            .dateOfBirth(buildDateOfBirth(dssCaseData.getApplicantDateOfBirth()))
            .email(dssCaseData.getApplicantEmailAddress())
            .canYouProvideEmailAddress(isNotEmpty(dssCaseData.getApplicantEmailAddress()) ? YesOrNo.Yes : null)
            .phoneNumber(dssCaseData.getApplicantPhoneNumber())
            .address(Address.builder()
                         .addressLine1(dssCaseData.getApplicantAddress1())
                         .addressLine2(dssCaseData.getApplicantAddress2())
                         .postTown(dssCaseData.getApplicantAddressTown())
                         .county(dssCaseData.getApplicantAddressCounty())
                         .postCode(dssCaseData.getApplicantAddressPostcode())
                         .country(dssCaseData.getApplicantAddressCountry())
                         .build())
            //Default gender to Female for FGM cases
            .gender("FGM".equals(dssCaseData.getEdgeCaseTypeOfApplication())
                && "self".equalsIgnoreCase(dssCaseData.getWhomYouAreApplying()) ? Gender.female : null)
            .build();
    }
}
