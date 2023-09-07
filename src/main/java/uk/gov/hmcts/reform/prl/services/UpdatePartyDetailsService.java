package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UpdatePartyDetailsService {

    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    private final ObjectMapper objectMapper;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final DocumentGenService documentGenService;

    @Qualifier("caseSummaryTab")
    private final  CaseSummaryTabService caseSummaryTabService;

    public Map<String, Object> updateApplicantRespondentAndChildData(CallbackRequest callbackRequest,
                                                                     String authorisation) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);

        CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
        updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());

        updatedCaseData.putAll(caseSummaryTabService.updateTab(caseData));

        final Flags caseFlags = Flags.builder().build();

        updatedCaseData.put("caseFlags", caseFlags);

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401respondent = caseData
                .getRespondentsFL401();

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
            setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, caseData.getApplicantsFL401());
            try {
                updatedCaseData.putAll(caseSummaryTabService
                                           .updateTab(
                                               generateC8DocumentsForRespondents(callbackRequest, authorisation,
                                                                                 List.of(ElementUtils
                                                                                             .element(fl401respondent)))));
            } catch (Exception e) {
                log.error("Failed to generate C8 document for Fl401 case {}",e.getMessage());
            }
        } else if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
            updatedCaseData.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
            if (applicantsWrapped.isPresent() && !applicantsWrapped.get().isEmpty()) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                PartyDetails applicant1 = applicants.get(0);
                if (Objects.nonNull(applicant1)) {
                    updatedCaseData.put("applicantName",applicant1.getFirstName() + " " + applicant1.getLastName());
                }
            }
            // set applicant and respondent case flag
            setApplicantFlag(caseData, updatedCaseData);
            setRespondentFlag(caseData, updatedCaseData);
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                setApplicantOrganisationPolicyIfOrgEmpty(updatedCaseData, ElementUtils.unwrapElements(applicantList.get()).get(0));
            }
            try {
                updatedCaseData.putAll(caseSummaryTabService
                                           .updateTab(generateC8DocumentsForRespondents(callbackRequest,authorisation,caseData.getRespondents())));
            } catch (Exception e) {
                log.error("Failed to generate C8 document for C100 case {}", e.getMessage());
            }
        }

        return updatedCaseData;
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
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && ObjectUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation())) {
            if (StringUtils.isEmpty(applicantOrganisationPolicy.getOrgPolicyCaseAssignedRole())) {
                roleNotExists = true;
            }
            organisationNotExists = true;
        } else if (ObjectUtils.isNotEmpty(applicantOrganisationPolicy) && ObjectUtils.isNotEmpty(
            applicantOrganisationPolicy.getOrganisation()) && StringUtils.isEmpty(
            applicantOrganisationPolicy.getOrganisation().getOrganisationID())) {
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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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

    private CaseData generateC8DocumentsForRespondents(CallbackRequest callbackRequest,String authorisation,
                                                    List<Element<PartyDetails>> currentRespondents) throws Exception {
        CaseData caseData = objectMapper
            .convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        int respondentIndex = 0;
        for (Element<PartyDetails> respondent: currentRespondents) {
            Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                callbackRequest,
                respondent
            );
            Document c8FinalDocument = null;
            Document c8FinalWelshDocument = null;
            if (dataMap.containsKey(IS_CONFIDENTIAL_DATA_PRESENT)) {
                c8FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    false,
                    dataMap
                );
                c8FinalWelshDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    true,
                    dataMap
                );
            }
            String partyName = respondent.getValue().getFirstName() + "" + respondent.getValue().getLastName();
            populateC8Documents(caseData, partyName, c8FinalDocument, c8FinalWelshDocument, respondentIndex++);
        }
        return  caseData;
    }

    private  void populateC8Documents(CaseData caseData, String partyName,
                                             Document c8FinalDocument,
                                      Document c8WelshDocument,int partyIndex) {
        if (null != c8FinalDocument && partyIndex >= 0) {
            ResponseDocuments c8ResponseDocuments = ResponseDocuments.builder()
                .partyName(partyName)
                .dateCreated(LocalDate.now())
                .build();
            switch (partyIndex) {
                case 0:
                    caseData.setRespondentAc8Documents(getOrCreateC8DocumentList(c8ResponseDocuments,c8FinalDocument,
                                                                                 c8WelshDocument,
                                                                                 caseData.getRespondentAc8Documents()));
                    break;
                case 1:
                    caseData.setRespondentBc8Documents(getOrCreateC8DocumentList(c8ResponseDocuments,c8FinalDocument,
                                                                        c8WelshDocument,
                                                                        caseData.getRespondentBc8Documents()));
                    break;
                case 2:
                    caseData.setRespondentCc8Documents(getOrCreateC8DocumentList(c8ResponseDocuments,c8FinalDocument,
                                                                        c8WelshDocument,
                                                                        caseData.getRespondentCc8Documents()));
                    break;
                case 3:
                    caseData.setRespondentDc8Documents(getOrCreateC8DocumentList(c8ResponseDocuments,c8FinalDocument,
                                                                        c8WelshDocument,
                                                                        caseData.getRespondentDc8Documents()));
                    break;
                case 4:
                    caseData.setRespondentEc8Documents(getOrCreateC8DocumentList(c8ResponseDocuments,c8FinalDocument,
                                                                        c8WelshDocument,
                                                                        caseData.getRespondentEc8Documents()));
                    break;
                default:
                    break;
            }
        }
    }

    private List<Element<ResponseDocuments>> getOrCreateC8DocumentList(ResponseDocuments c8DocumentParam,
                                                                       Document c8Document,
                                                                       Document c8WelshDocument,
                                                                       List<Element<ResponseDocuments>> c8Documents) {
        Element<ResponseDocuments> newC8Document = ElementUtils.element(c8DocumentParam.toBuilder()
                                                                            .respondentC8Document(c8Document)
                                                                            .respondentC8DocumentWelsh(c8WelshDocument)
                                                                            .build());
        List<Element<ResponseDocuments>> newC8Documents = new ArrayList<>();
        if (null != c8Documents) {
            c8Documents.add(newC8Document);
            return c8Documents;
        } else {
            newC8Documents.add(newC8Document);
            return newC8Documents;
        }
    }
}
