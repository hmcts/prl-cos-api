package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FL401_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {

    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    public static final String C_8_OF = "C8 of ";
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final DocumentGenService documentGenService;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Qualifier("caseSummaryTab")
    private final  CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> updateApplicantRespondentAndChildData(CallbackRequest callbackRequest,
                                                                     String authorisation) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());

        final Flags caseFlags = Flags.builder().build();

        updatedCaseData.put("caseFlags", caseFlags);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

            setFl401ApplicantAndRespondent(updatedCaseData, caseData, fl401Applicant, fl401respondent);
            setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, caseData.getApplicantsFL401());
            try {
                generateC8DocumentsForRespondents(updatedCaseData,callbackRequest,authorisation,caseData, List.of(ElementUtils
                                                                                             .element(fl401respondent)));
            } catch (Exception e) {
                log.error("Failed to generate C8 document for Fl401 case {}",e.getMessage());
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            setApplicantName(updatedCaseData, applicantsWrapped);
            // set applicant and respondent case flag
            setApplicantFlag(caseData, updatedCaseData);
            setRespondentFlag(caseData, updatedCaseData);
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, ElementUtils.unwrapElements(applicantList.get()).get(0));
            }
            try {
                generateC8DocumentsForRespondents(updatedCaseData,
                                                  callbackRequest,authorisation,caseData,caseData.getRespondents());
            } catch (Exception e) {
                log.error("Failed to generate C8 document for C100 case {}", e.getMessage());
            }
            // set applicant and respondent case flag
            setApplicantFlag(caseData, updatedCaseData);
            setRespondentFlag(caseData, updatedCaseData);
        }
        cleanUpCaseDataBasedOnYesNoSelection(updatedCaseData, caseData);
        return updatedCaseData;
    }

    private void setFl401ApplicantAndRespondent(Map<String, Object> updatedCaseData,
                                                CaseData caseData, PartyDetails fl401Applicant,
                                                PartyDetails fl401respondent) {
        if (Objects.nonNull(fl401Applicant)) {
            CommonUtils.generatePartyUuidForFL401(caseData);
            updatedCaseData.put("applicantName", fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
            setFL401ApplicantFlag(updatedCaseData, fl401Applicant);

        }

        if (Objects.nonNull(fl401respondent)) {
            CommonUtils.generatePartyUuidForFL401(caseData);
            updatedCaseData.put("respondentName", fl401respondent.getFirstName() + " " + fl401respondent.getLastName());
            setFL401RespondentFlag(updatedCaseData, fl401respondent);
        }
    }

    private void setApplicantName(Map<String, Object> updatedCaseData,
                                  Optional<List<Element<PartyDetails>>> applicantsWrapped) {
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            PartyDetails applicant1 = applicants.get(0);
            if (Objects.nonNull(applicant1)) {
                updatedCaseData.put("applicantName", applicant1.getFirstName() + " " + applicant1.getLastName());
            }
        }
    }

    private void cleanUpCaseDataBasedOnYesNoSelection(Map<String, Object> updatedCaseData, CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (isNotEmpty(caseData.getRespondentsFL401())) {
                PartyDetails updatedRespondent = resetRespondent(caseData.getRespondentsFL401());
                updatedCaseData.put(FL401_RESPONDENTS, updatedRespondent);
            }
            if (isNotEmpty(caseData.getApplicantsFL401())) {
                PartyDetails updatedApplicant = resetApplicant(caseData.getApplicantsFL401());
                updatedCaseData.put(FL401_APPLICANTS, updatedApplicant);
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
                List<Element<PartyDetails>> updatedRespondents = new ArrayList<>();
                caseData.getRespondents().forEach(eachRespondent ->
                    updatedRespondents.add(element(
                        eachRespondent.getId(),
                        resetRespondent(eachRespondent.getValue())
                    ))
                );
                updatedCaseData.put(RESPONDENTS, updatedRespondents);
            }
            if (CollectionUtils.isNotEmpty(caseData.getApplicants())) {
                List<Element<PartyDetails>> updatedApplicants = new ArrayList<>();
                caseData.getApplicants().forEach(eachApplicant ->
                    updatedApplicants.add(element(
                        eachApplicant.getId(),
                        resetApplicant(eachApplicant.getValue())
                    ))
                );
                updatedCaseData.put(APPLICANTS, updatedApplicants);
            }
            if (CollectionUtils.isNotEmpty(caseData.getChildren())
                && YesNoDontKnow.no.equals(caseData.getChildrenKnownToLocalAuthority())) {
                updatedCaseData.put("childrenKnownToLocalAuthorityTextArea", null);
            }
        }
    }

    private PartyDetails resetApplicant(PartyDetails partyDetails) {
        partyDetails = partyDetails.toBuilder()
            .addressLivedLessThan5YearsDetails(YesOrNo.Yes.equals(partyDetails.getIsAtAddressLessThan5Years())
                                                   ? partyDetails.getAddressLivedLessThan5YearsDetails() : null)
            .email(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress()) ? partyDetails.getEmail() : null)
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? partyDetails.getIsEmailAddressConfidential() : null)
            .build();

        return partyDetails;
    }

    private PartyDetails resetRespondent(PartyDetails partyDetails) {
        boolean isRepresented = YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
        partyDetails = partyDetails.toBuilder()
            .dateOfBirth(YesOrNo.Yes.equals(partyDetails.getIsDateOfBirthKnown()) ? partyDetails.getDateOfBirth() : null)
            .placeOfBirth(YesOrNo.Yes.equals(partyDetails.getIsPlaceOfBirthKnown()) ? partyDetails.getPlaceOfBirth() : null)
            .address(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown()) ? partyDetails.getAddress() : null)
            .isAddressConfidential(YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())
                                       ? partyDetails.getIsAddressConfidential() : null)
            .addressLivedLessThan5YearsDetails(YesNoDontKnow.yes.equals(partyDetails.getIsAtAddressLessThan5YearsWithDontKnow())
                                                   ? partyDetails.getAddressLivedLessThan5YearsDetails() : null)
            .email(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress()) ? partyDetails.getEmail() : null)
            .isEmailAddressConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())
                                            ? partyDetails.getIsEmailAddressConfidential() : null)
            .phoneNumber(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber()) ? partyDetails.getPhoneNumber() : null)
            .isPhoneNumberConfidential(YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber())
                                           ? partyDetails.getIsPhoneNumberConfidential() : null)
            .representativeFirstName(isRepresented ? partyDetails.getRepresentativeFirstName() : null)
            .representativeLastName(isRepresented ? partyDetails.getRepresentativeLastName() : null)
            .solicitorEmail(isRepresented ? partyDetails.getSolicitorEmail() : null)
            .dxNumber(isRepresented ? partyDetails.getDxNumber() : null)
            .solicitorAddress(isRepresented ? partyDetails.getSolicitorAddress() : null)
            .solicitorOrg(isRepresented ? partyDetails.getSolicitorOrg() : null)
            .build();

        return partyDetails;
    }

    private void setApplicantOrganisationPolicyIfOrgEmpty(Map<String, Object> updatedCaseData, PartyDetails partyDetails) {
        CaseData caseDataUpdated = objectMapper.convertValue(updatedCaseData, CaseData.class);
        OrganisationPolicy applicantOrganisationPolicy = caseDataUpdated.getApplicantOrganisationPolicy();
        boolean organisationNotExists = false;
        boolean roleNotExists = false;
        log.info("Organisation policy before  override : {}", applicantOrganisationPolicy);
        if (ObjectUtils.isEmpty(applicantOrganisationPolicy)) {
            applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]").build();
            organisationNotExists = true;
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && (ObjectUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation()) || (ObjectUtils.isNotEmpty(
            applicantOrganisationPolicy.getOrganisation()) && StringUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation().getOrganisationID())))
        ) {
            if (StringUtils.isEmpty(applicantOrganisationPolicy.getOrgPolicyCaseAssignedRole())) {
                roleNotExists = true;
            }
            organisationNotExists = true;
        }
        if (organisationNotExists) {
            applicantOrganisationPolicy.setOrganisation(partyDetails.getSolicitorOrg());
        }
        if (roleNotExists) {
            applicantOrganisationPolicy.setOrgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]");
        }
        log.info("Organisation policy after  override : {}", applicantOrganisationPolicy);
        updatedCaseData.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
    }

    private void setApplicantFlag(CaseData caseData, Map<String, Object> caseDetails) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails applicant : applicants) {
                CommonUtils.generatePartyUuidForC100(applicant);
                final String partyName = applicant.getFirstName() + " " + applicant.getLastName();
                final Flags applicantFlag = Flags.builder().partyName(partyName)
                    .roleOnCase(PartyEnum.applicant.getDisplayedValue()).details(Collections.emptyList()).build();
                applicant.setPartyLevelFlag(applicantFlag);
            }

            caseDetails.put("applicants", applicantsWrapped);
        }
    }

    private void setRespondentFlag(CaseData caseData, Map<String, Object> caseDetails) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());
        if (respondentsWrapped.isPresent() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails respondent : respondents) {
                CommonUtils.generatePartyUuidForC100(respondent);
                final String partyName = respondent.getFirstName() + " " + respondent.getLastName();
                final Flags respondentFlag = Flags.builder().partyName(partyName)
                    .roleOnCase(PartyEnum.respondent.getDisplayedValue()).details(Collections.emptyList()).build();
                respondent.setPartyLevelFlag(respondentFlag);
            }
            caseDetails.put("respondents", respondentsWrapped);
        }
    }

    private void setFL401ApplicantFlag(Map<String, Object> caseDetails, PartyDetails fl401Applicant) {
        String partyName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        final Flags applicantFlag = Flags.builder().partyName(partyName)
            .roleOnCase(PartyEnum.applicant.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401Applicant.setPartyLevelFlag(applicantFlag);

        caseDetails.put("applicantsFL401", fl401Applicant);
    }

    private void setFL401RespondentFlag(Map<String, Object> caseDetails, PartyDetails fl401respondent) {
        String partyName = fl401respondent.getFirstName() + " " + fl401respondent.getLastName();
        final Flags respondentFlag = Flags.builder().partyName(partyName)
            .roleOnCase(PartyEnum.respondent.getDisplayedValue()).details(Collections.emptyList()).build();
        fl401respondent.setPartyLevelFlag(respondentFlag);

        caseDetails.put("respondentsFL401", fl401respondent);
    }

    private void generateC8DocumentsForRespondents(Map<String, Object> updatedCaseData, CallbackRequest callbackRequest, String authorisation,
                                                       CaseData caseData, List<Element<PartyDetails>> currentRespondents)
        throws Exception {
        int respondentIndex = 0;
        for (Element<PartyDetails> respondent: currentRespondents) {
            Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                callbackRequest,
                respondent
            );
            populateC8Documents(authorisation,
                        updatedCaseData,
                        caseData,
                        dataMap, checkIfConfidentialityDetailsChangedRespondent(callbackRequest,respondent),
                        respondentIndex,respondent
            );
            respondentIndex++;
        }
    }

    public Boolean checkIfConfidentialityDetailsChangedRespondent(CallbackRequest callbackRequest, Element<PartyDetails> respondent) {
        Map<String, Object> casDataMap = callbackRequest.getCaseDetailsBefore().getData();
        CaseData caseDataBefore = objectMapper.convertValue(casDataMap, CaseData.class);
        List<Element<PartyDetails>> respondentList = null;
        if (caseDataBefore.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            respondentList = caseDataBefore.getRespondents().stream()
                    .filter(resp1 -> resp1.getId().equals(respondent.getId())
                            && (!StringUtils.equals(resp1.getValue().getEmail(),respondent.getValue().getEmail())
                            || checkIfAddressIsChanged(respondent.getValue().getAddress(), resp1.getValue().getAddress())
                            || !StringUtils.equalsIgnoreCase(resp1.getValue().getPhoneNumber(),
                            respondent.getValue().getPhoneNumber())
                            || !StringUtils.equals(resp1.getValue().getLabelForDynamicList(), respondent.getValue()
                            .getLabelForDynamicList()))).toList();
        } else {
            PartyDetails respondentDetailsFL401 = caseDataBefore.getRespondentsFL401();
            if ((!StringUtils.equals(respondentDetailsFL401.getEmail(),respondent.getValue().getEmail()))
                    || checkIfAddressIsChanged(respondent.getValue().getAddress(),respondentDetailsFL401.getAddress())
                    || (!StringUtils.equalsIgnoreCase(respondentDetailsFL401.getPhoneNumber(),
                    respondent.getValue().getPhoneNumber()))
                || !StringUtils.equals(respondent.getValue().getLabelForDynamicList(), respondentDetailsFL401
                .getLabelForDynamicList())) {
                log.info("respondent data changed for fl401");
                return true;
            }
        }
        if (respondentList != null && !respondentList.isEmpty()) {
            log.info("respondent data changed {}", respondent.getValue().getLabelForDynamicList());
            log.info("{}", respondent.getValue().getAddress());
            return true;
        }
        log.info("respondent data not changed");
        return  false;
    }

    private boolean checkIfAddressIsChanged(Address currentAddress, Address previousAddress) {
        log.info("Current address {} ", currentAddress);
        log.info("Previous address {} ", previousAddress);
        return currentAddress != null
            && (!StringUtils.equals(currentAddress.getAddressLine1(),previousAddress.getAddressLine1())
            || !StringUtils.equals(currentAddress.getAddressLine2(),previousAddress.getAddressLine2())
            || !StringUtils.equals(currentAddress.getAddressLine3(),previousAddress.getAddressLine3())
            || !StringUtils.equals(currentAddress.getCountry(),previousAddress.getCountry())
            || !StringUtils.equals(currentAddress.getCounty(),previousAddress.getCounty())
            || !StringUtils.equals(currentAddress.getPostCode(),previousAddress.getPostCode())
            || !StringUtils.equals(currentAddress.getPostTown(),previousAddress.getPostTown()));
    }

    private  void populateC8Documents(String authorisation, Map<String, Object> updatedCaseData, CaseData caseData,
                                      Map<String, Object> dataMap, Boolean isDetailsChanged, int partyIndex,
                                      Element<PartyDetails> respondent) throws Exception {
        if (partyIndex >= 0) {
            switch (partyIndex) {
                case 0:
                    updatedCaseData
                        .put("respondentAc8Documents",getOrCreateC8DocumentList(authorisation, caseData, dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentAc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 1:
                    updatedCaseData
                        .put("respondentBc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentBc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 2:
                    updatedCaseData
                        .put("respondentCc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentCc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 3:
                    updatedCaseData
                        .put("respondentDc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentDc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                case 4:
                    updatedCaseData
                        .put("respondentEc8Documents",getOrCreateC8DocumentList(authorisation, caseData,
                                                                                dataMap,
                                                                                caseData.getRespondentC8Document()
                                                                                    .getRespondentEc8Documents(),
                                                                                isDetailsChanged,
                                                                                respondent));
                    break;
                default:
                    break;
            }
        }
    }

    private List<Element<ResponseDocuments>> getOrCreateC8DocumentList(String authorisation, CaseData caseData,
                                                                       Map<String, Object> dataMap,
                                                                       List<Element<ResponseDocuments>> c8Documents,
                                                                       Boolean isDetailsChanged,
                                                                       Element<PartyDetails> respondent)
        throws  Exception {
        Document c8FinalDocument;
        Document c8FinalWelshDocument;
        String partyName = respondent.getValue().getLabelForDynamicList();
        if (dataMap.containsKey(IS_CONFIDENTIAL_DATA_PRESENT)) {
            if ((isDetailsChanged
                || CollectionUtils.isEmpty(c8Documents))) {
                String fileName = C_8_OF + partyName
                    + " " + LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).format(dateTimeFormatter);
                dataMap.put("dynamic_fileName", fileName + ".pdf");
                c8FinalDocument = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        caseData.getCaseTypeOfApplication()
                                .equals(C100_CASE_TYPE) ? C8_RESP_FINAL_HINT
                                : C8_RESP_FL401_FINAL_HINT,
                        false,
                        dataMap
                );
                dataMap.put("dynamic_fileName", fileName + " welsh" + ".pdf");
                c8FinalWelshDocument = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        caseData.getCaseTypeOfApplication()
                                .equals(C100_CASE_TYPE) ? C8_RESP_FINAL_HINT
                                : C8_RESP_FL401_FINAL_HINT,
                        true,
                        dataMap
                );
                Element<ResponseDocuments> newC8Document = ElementUtils.element(ResponseDocuments.builder()
                                                                                    .dateTimeCreated(LocalDateTime.now())
                                                                                    .respondentC8Document(
                                                                                        c8FinalDocument)
                                                                                    .respondentC8DocumentWelsh(
                                                                                        c8FinalWelshDocument)
                                                                                    .build());
                return getC8DocumentReverseOrderList(c8Documents, newC8Document);
            } else {
                return  c8Documents;
            }
        } else {
            return null;
        }
    }

    private List<Element<ResponseDocuments>> getC8DocumentReverseOrderList(List<Element<ResponseDocuments>> c8Documents,
                                                                           Element<ResponseDocuments> newC8Document) {
        List<Element<ResponseDocuments>> newC8Documents = new ArrayList<>();
        if (null != c8Documents) {
            c8Documents.add(newC8Document);
            c8Documents.sort(Comparator.comparing(
                m -> m.getValue().getDateTimeCreated(),
                Comparator.reverseOrder()
            ));
            return c8Documents;
        } else {
            newC8Documents.add(newC8Document);
            return newC8Documents;
        }
    }
}
