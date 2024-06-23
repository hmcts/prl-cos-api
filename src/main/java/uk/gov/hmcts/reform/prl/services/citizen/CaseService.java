package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DssCaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String YES = "Yes";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataMapper caseDataMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final HearingService hearingService;
    private final LocationRefDataApi locationRefDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${courts.edgeCaseCourtList}")
    protected String edgeCaseCourtList;

    public CaseDetails updateCase(CaseData caseData, String authToken,
                                  String caseId, String eventId) throws JsonProcessingException {
        if (CITIZEN_CASE_SUBMIT.getValue().equalsIgnoreCase(eventId)
            || CITIZEN_CASE_SUBMIT_WITH_HWF.getValue().equalsIgnoreCase(eventId)) {
            UserDetails userDetails = idamClient.getUserDetails(authToken);
            UserInfo userInfo = UserInfo
                .builder()
                .idamId(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(null))
                .emailAddress(userDetails.getEmail())
                .build();

            CaseData updatedCaseData = caseDataMapper
                .buildUpdatedCaseData(caseData.toBuilder().userInfo(wrapElements(userInfo))
                                          .courtName(C100_DEFAULT_COURT_NAME)
                                          .taskListVersion(TASK_LIST_VERSION_V2)
                                          .build());
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }
        if (CITIZEN_CASE_UPDATE.getValue().equalsIgnoreCase(eventId)
            && isEmpty(caseData.getApplicantCaseName())) {
            caseData = caseData.toBuilder()
                .applicantCaseName(buildApplicantAndRespondentForCaseName(caseData))
                .build();
        }

        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    public String buildApplicantAndRespondentForCaseName(CaseData caseData) throws JsonProcessingException {
        C100RebuildData c100RebuildData = caseData.getC100RebuildData();
        ObjectMapper mapper = new ObjectMapper();
        C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = null;
        C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = null;
        if (null != c100RebuildData) {
            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
                c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            }

            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
                c100RebuildRespondentDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            }
        }
        return buildCaseName(c100RebuildApplicantDetailsElements, c100RebuildRespondentDetailsElements);
    }


    private String buildCaseName(C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements,
                                 C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {
        String caseName = null;
        if (null != c100RebuildApplicantDetailsElements
            && null != c100RebuildRespondentDetailsElements.getRespondentDetails()) {
            caseName = c100RebuildApplicantDetailsElements.getApplicants().get(0).getApplicantLastName() + " V "
                + c100RebuildRespondentDetailsElements.getRespondentDetails().get(0).getLastName();
        }

        return caseName;
    }

    public List<CaseData> retrieveCases(String authToken, String s2sToken) {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToUser(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesLinkedToUser(String authToken, String s2sToken,
                                                   Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        return caseDetails
            .stream()
            .map(caseDetail -> CaseUtils.getCaseData(caseDetail, objectMapper))
            .toList();
    }

    private List<CaseDetails> performSearch(String authToken, UserDetails user, Map<String, String> searchCriteria,
                                            String serviceAuthToken) {
        List<CaseDetails> result;

        result = coreCaseDataApi.searchForCitizen(
            authToken,
            serviceAuthToken,
            user.getId(),
            JURISDICTION,
            CASE_TYPE,
            searchCriteria
        );

        return result;
    }

    public CaseDetails getCase(String authToken, String caseId) {
        return caseRepository.getCase(authToken, caseId);
    }

    public CaseDetails createCase(CaseData caseData, String authToken) {
        return caseRepository.createCase(authToken, caseData);
    }

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }

    public CaseDataWithHearingResponse getCaseWithHearing(String authorisation, String caseId, String hearingNeeded) {
        CaseDataWithHearingResponse caseDataWithHearingResponse = CaseDataWithHearingResponse.builder().build();
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseData = caseData.toBuilder().noOfDaysRemainingToSubmitCase(
            CaseUtils.getRemainingDaysSubmitCase(caseData)).build();
        caseDataWithHearingResponse = caseDataWithHearingResponse.toBuilder().caseData(caseData).build();
        if ("Yes".equalsIgnoreCase(hearingNeeded)) {
            caseDataWithHearingResponse =
                caseDataWithHearingResponse.toBuilder().hearings(
                    hearingService.getHearings(authorisation, caseId)).build();
        }

        return caseDataWithHearingResponse;
    }

    public String getEdgeCasesCourtList() {
        return edgeCaseCourtList;
    }

    public CaseDetails updateCaseForDss(String authToken, String caseId, String eventId, DssCaseData dssCaseData) throws JsonProcessingException {

        System.out.println("dssCaseDate recieved " + dssCaseData);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Element<Document>> uploadDssDocs = new ArrayList<Element<Document>>();
        List<Element<Document>> uploadAdditionalDssDocs = new ArrayList<Element<Document>>();
        dssCaseData.getApplicantApplicationFormDocuments().stream().forEach(edgeCaseDocumentElement -> {
            uk.gov.hmcts.reform.ccd.client.model.Document document = edgeCaseDocumentElement.getValue().getDocumentLink();
            System.out.println("document---------------" + document);
            uploadDssDocs.add(element(Document.builder().documentUrl(document.getDocumentURL()).documentBinaryUrl(
                document.getDocumentBinaryURL()).documentFileName(document.getDocumentFilename()).build()));
            System.out.println("**************");
            System.out.println("uploadDssDocs ==========" + uploadDssDocs);

        });

        dssCaseData.getApplicantAdditionalDocuments().stream().forEach(edgeCaseDocumentElement -> {
            uk.gov.hmcts.reform.ccd.client.model.Document document = edgeCaseDocumentElement.getValue().getDocumentLink();

            uploadAdditionalDssDocs.add(element(Document.builder().documentUrl(document.getDocumentURL()).documentBinaryUrl(
                document.getDocumentBinaryURL()).documentFileName(document.getDocumentFilename()).build()));
        });

        PartyDetails partyDetails = PartyDetails.builder().firstName(dssCaseData.getApplicantFirstName()).email(
            dssCaseData.getApplicantEmailAddress()).address(Address.builder().addressLine1(dssCaseData.getApplicantAddress1()).addressLine2(
            dssCaseData.getApplicantAddress2()).country("United Kingdom").postCode(dssCaseData.getApplicantAddressPostCode()).build()).dateOfBirth(
            LocalDate.parse(
                dssCaseData.getApplicantDateOfBirth(),
                dateTimeFormatter
            )).lastName(dssCaseData.getApplicantLastName()).phoneNumber(dssCaseData.getApplicantPhoneNumber()).build();
        Element<PartyDetails> partyDetailsElement = element(partyDetails);
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        CaseData updatedCaseData = caseData.toBuilder().id(Long.parseLong(caseId)).applicants(List.of(partyDetailsElement))
            .dssCaseDetails(
            caseData.getDssCaseDetails().toBuilder()
                .dssUploadedDocuments(uploadDssDocs)
                .dssUploadedAdditionalDocuments(uploadAdditionalDssDocs)
                .selectedCourt(dssCaseData.getSelectedCourt())
                .build()).build();
        updatedCaseData = updateCourtDetails(authToken, dssCaseData, updatedCaseData);
        log.info("updatedCaseData --" + updatedCaseData);
        return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));

    }

    private CaseData updateCourtDetails(String authToken, DssCaseData dssCaseData, CaseData updatedCaseData) {
        if (null != updatedCaseData.getDssCaseDetails()
            && ("FGM".equalsIgnoreCase(updatedCaseData.getDssCaseDetails().getEdgeCaseTypeOfApplication())
            || "FMPO".equalsIgnoreCase(updatedCaseData.getDssCaseDetails().getEdgeCaseTypeOfApplication()))
            && null != dssCaseData.getSelectedCourt()) {

            CaseManagementLocation courtLocationByEpmsId = getCourtLocationByEpmsId(
                dssCaseData.getSelectedCourt(),
                authToken
            );
            updatedCaseData = updatedCaseData.toBuilder()
                .caseManagementLocation(courtLocationByEpmsId)
                .courtName(courtLocationByEpmsId.getBaseLocationName())
                .courtId(courtLocationByEpmsId.getBaseLocation())
                .build();
        }
        return updatedCaseData;
    }

    private CaseManagementLocation getCourtLocationByEpmsId(String epmsId, String authorisation) {
        CourtDetails courtDetails = locationRefDataApi.getCourtDetailsByService(
            authorisation,
            authTokenGenerator.generate(),
            SERVICE_ID
        );

        Optional<CourtVenue> courtVenue = courtDetails.getCourtVenues().stream()
            .filter(location -> epmsId.equalsIgnoreCase(location.getCourtEpimmsId()))
            .findFirst();
        if (courtVenue.isPresent()) {
            return CaseManagementLocation.builder()
                .baseLocation(courtVenue.get().getCourtEpimmsId())
                .baseLocationName(courtVenue.get().getCourtName())
                .region(courtVenue.get().getRegionId())
                .regionName(courtVenue.get().getRegion()).build();
        }
        return CaseManagementLocation.builder().build();
    }
}
