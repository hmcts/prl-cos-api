package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RefugeConfidentialityService {

    private final ObjectMapper objectMapper;

    public Map<String, Object> updateConfidentialDetailsForRefuge(CallbackRequest callbackRequest) {

        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        if (Event.AMEND_APPLICANTS_DETAILS.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && CollectionUtils.isNotEmpty(caseData.getApplicants())) {
                List<Element<PartyDetails>> updatedApplicants = new ArrayList<>();
                caseData.getApplicants().forEach(eachApplicant ->
                                                     updatedApplicants.add(element(
                                                         eachApplicant.getId(),
                                                         resetPartyConfidentialDetailsForRefuge(eachApplicant.getValue())
                                                     )));
                updatedCaseData.put(APPLICANTS, updatedApplicants);
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                && isNotEmpty(caseData.getApplicantsFL401())) {
                PartyDetails updatedApplicant = resetPartyConfidentialDetailsForRefuge(caseData.getApplicantsFL401());
                updatedCaseData.put(FL401_APPLICANTS, updatedApplicant);
            }
        }
        return updatedCaseData;
    }

    public PartyDetails resetPartyConfidentialDetailsForRefuge(PartyDetails partyDetails) {
        log.info("*** Inside resetPartyConfidentialDetailsForRefuge ***");
        PartyDetails updatedPartyDetails = partyDetails.toBuilder()
            .liveInRefuge(isEmpty(partyDetails.getLiveInRefuge()) ? YesOrNo.No : partyDetails.getLiveInRefuge())
            .isAddressConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                       ? null : partyDetails.getIsAddressConfidential())
            .isAtAddressLessThan5Years(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                           ? null : partyDetails.getIsAtAddressLessThan5Years())
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                            && YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? null : partyDetails.getIsEmailAddressConfidential())
            .isPhoneNumberConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                           ? null : partyDetails.getIsPhoneNumberConfidential())
            .build();
        log.info("Exit resetPartyConfidentialDetailsForRefuge with party {}", updatedPartyDetails);
        return updatedPartyDetails;
    }

    public void setConfidentialFlagForPartiesLiveInRefuge(Map<String, Object> updatedCaseData) {
        log.info("*** Inside setConfidentialFlagForPartiesLiveInRefuge ***");
        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && CollectionUtils.isNotEmpty(caseData.getApplicants())) {
            List<Element<PartyDetails>> updatedApplicants = new ArrayList<>();
            caseData.getApplicants().forEach(eachApplicant ->
                                                 updatedApplicants.add(element(
                                                     eachApplicant.getId(),
                                                     YesOrNo.Yes.equals(eachApplicant.getValue().getLiveInRefuge())
                                                         ? markPersonalDetailsAsConfidentialForPartiesLiveInRefuge(
                                                         eachApplicant.getValue())
                                                         : eachApplicant.getValue()
                                                 ))
            );
            updatedCaseData.put(APPLICANTS, updatedApplicants);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            && isNotEmpty(caseData.getApplicantsFL401())) {
            PartyDetails updatedApplicant = YesOrNo.Yes.equals(caseData.getApplicantsFL401().getLiveInRefuge())
                ? markPersonalDetailsAsConfidentialForPartiesLiveInRefuge(
                caseData.getApplicantsFL401())
                : caseData.getApplicantsFL401();
            updatedCaseData.put(FL401_APPLICANTS, updatedApplicant);
        }
    }


    private PartyDetails markPersonalDetailsAsConfidentialForPartiesLiveInRefuge(PartyDetails partyDetails) {
        log.info("*** Inside markPersonalDetailsAsConfidentialForPartiesLiveInRefuge ***");
        PartyDetails updatedPartyDetails = partyDetails.toBuilder()
            .isAddressConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                       ? YesOrNo.Yes : partyDetails.getIsAddressConfidential())
            .isAtAddressLessThan5Years(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                           ? YesOrNo.Yes : partyDetails.getIsAtAddressLessThan5Years())
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                            && YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? YesOrNo.Yes : partyDetails.getIsEmailAddressConfidential())
            .isPhoneNumberConfidential(YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
                                           ? YesOrNo.Yes : partyDetails.getIsPhoneNumberConfidential())
            .build();
        log.info("Exit markPersonalDetailsAsConfidentialForPartiesLiveInRefuge with party {}", updatedPartyDetails);
        return updatedPartyDetails;

    }

}
