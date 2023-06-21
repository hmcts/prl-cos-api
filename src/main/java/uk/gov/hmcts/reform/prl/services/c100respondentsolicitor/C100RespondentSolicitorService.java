package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.exception.RespondentSolicitorException;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.DxAddress;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    public static final String RESPONDENTS = "respondents";
    public static final String RESPONDENT_NAME_FOR_RESPONSE = "respondentNameForResponse";
    public static final String TECH_ERROR = "This event cannot be started. Please contact support team";
    public static final String RESPONSE_ALREADY_SUBMITTED_ERROR = "This event cannot be started as the response has already been submitted.";
    public static final String SOLICITOR = " (Solicitor)";

    @Autowired
    private final RespondentSolicitorMiamService miamService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final DocumentGenService documentGenService;

    @Autowired
    private final ResponseSubmitChecker responseSubmitChecker;

    private final ApplicationsTabService applicationsTabService;

    private final SystemUserService systemUserService;

    private final ConfidentialDetailsMapper confidentialDetailsMapper;

    private final OrganisationService organisationService;

    public static final String RESPONSE_SUBMITTED_LABEL = "# Response Submitted";

    public static final String CONTACT_LOCAL_COURT_LABEL = "### Your response is now submitted. \n\n "
        + "You can contact your local court at ";

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        }
        if (solicitorRepresentedRespondent != null && solicitorRepresentedRespondent.getValue() != null) {
            retrieveExistingResponseForSolicitor(
                callbackRequest,
                caseDataUpdated,
                solicitorRepresentedRespondent
            );
            String representedRespondentName = solicitorRepresentedRespondent.getValue().getFirstName() + " "
                + solicitorRepresentedRespondent.getValue().getLastName();

            caseDataUpdated.put(RESPONDENT_NAME_FOR_RESPONSE, representedRespondentName);
        }
        return caseDataUpdated;
    }

    private void retrieveExistingResponseForSolicitor(CallbackRequest callbackRequest, Map<String,
        Object> caseDataUpdated, Element<PartyDetails> solicitorRepresentedRespondent) {
        String invokedEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        RespondentSolicitorEvents.getCaseFieldName(invokedEvent).ifPresent(event -> {
            switch (event) {
                case CONSENT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        solicitorRepresentedRespondent.getValue().getResponse().getConsent()
                    );
                    break;
                case KEEP_DETAILS_PRIVATE:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate()
                    );
                    break;
                case CONFIRM_EDIT_CONTACT_DETAILS:
                    CitizenDetails citizenDetails = solicitorRepresentedRespondent.getValue().getResponse().getCitizenDetails();
                    PartyDetails partyDetails = solicitorRepresentedRespondent.getValue();
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        CitizenDetails.builder()
                            .address(ofNullable(citizenDetails.getAddress()).orElse(partyDetails.getAddress()))
                            .addressHistory(ofNullable(citizenDetails.getAddressHistory()).orElse(
                                AddressHistory.builder().isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years())
                                    .build()
                            ))
                            .contact(ofNullable(citizenDetails.getContact()).orElse(Contact.builder()
                                                                                        .phoneNumber(
                                                                                            partyDetails
                                                                                                .getPhoneNumber())
                                                                                        .email(partyDetails.getEmail())
                                                                                        .build()))
                            .dateOfBirth(ofNullable(citizenDetails.getDateOfBirth()).orElse(partyDetails.getDateOfBirth()))
                            .firstName(ofNullable(citizenDetails.getFirstName()).orElse(partyDetails.getFirstName()))
                            .lastName(ofNullable(citizenDetails.getLastName()).orElse(partyDetails.getLastName()))
                            .placeOfBirth(ofNullable(citizenDetails.getPlaceOfBirth()).orElse(partyDetails.getPlaceOfBirth()))
                            .previousName(ofNullable(citizenDetails.getPreviousName()).orElse(partyDetails.getPreviousName()))
                            .build()
                    );
                    break;
                case ATTENDING_THE_COURT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        solicitorRepresentedRespondent.getValue().getResponse().getAttendToCourt()
                    );
                    break;
                case MIAM:
                    String[] miamFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        miamFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse().getMiam()
                    );
                    caseDataUpdated.put(miamFields[1], miamService.getCollapsableOfWhatIsMiamPlaceHolder());
                    caseDataUpdated.put(
                        miamFields[2],
                        miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder()
                    );
                    break;
                case CURRENT_OR_PREVIOUS_PROCEEDINGS:
                    String[] proceedingsFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        proceedingsFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse().getCurrentOrPastProceedingsForChildren()
                    );
                    caseDataUpdated.put(
                        proceedingsFields[1],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentExistingProceedings()
                    );
                    break;
                case ALLEGATION_OF_HARM:
                    String[] allegationsOfHarmFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        allegationsOfHarmFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAohYesOrNo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[1],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAllegationsOfHarmInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[2],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespDomesticAbuseInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[3],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespChildAbuseInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[4],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespChildAbductionInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[5],
                        solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespOtherConcernsInfo()
                    );
                    break;
                case INTERNATIONAL_ELEMENT:
                    String[] internationalElementFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        internationalElementFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse().getCitizenInternationalElements()
                    );
                    break;
                case ABILITY_TO_PARTICIPATE:
                    String[] abilityToParticipateFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        abilityToParticipateFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse().getAbilityToParticipate()
                    );
                    break;
                case VIEW_DRAFT_RESPONSE:
                    String[] viewDraftDocumentFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        viewDraftDocumentFields[0],
                        " "
                    );
                    break;
                case SUBMIT:
                default:
                    break;
            }
        });
    }

    public Map<String, Object> populateAboutToSubmitCaseData(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        }
        String invokingEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        Element<PartyDetails> finalSolicitorRepresentedRespondent = solicitorRepresentedRespondent;
        RespondentSolicitorEvents.getCaseFieldName(invokingEvent)
            .ifPresent(event -> buildResponseForRespondent(
                caseData,
                respondents,
                finalSolicitorRepresentedRespondent,
                event
            ));

        if (RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS.getEventId().equalsIgnoreCase(invokingEvent)
            || RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE.getEventId().equalsIgnoreCase(invokingEvent)) {
            CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
            updatedCaseData.put("respondentConfidentialDetails", caseDataTemp.getRespondentConfidentialDetails());
        }
        updatedCaseData.put("respondentDocsList", caseData.getRespondentSolicitorData().getRespondentDocsList());
        updatedCaseData.put(C100_RESPONDENT_TABLE, applicationsTabService.getRespondentsTable(caseData));
        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    private void buildResponseForRespondent(CaseData caseData,
                                            List<Element<PartyDetails>> respondents,
                                            Element<PartyDetails> party,
                                            RespondentSolicitorEvents event) {
        Response buildResponseForRespondent = party.getValue().getResponse();
        String solicitor = party.getValue().getRepresentativeLabelForDynamicList();
        switch (event) {
            case CONSENT:
                Consent respondentConsentToApplication = caseData.getRespondentSolicitorData().getRespondentConsentToApplication();
                respondentConsentToApplication = optimiseConsent(respondentConsentToApplication);
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .consent(respondentConsentToApplication).build();
                break;
            case KEEP_DETAILS_PRIVATE:
                buildResponseForRespondent = buildKeepYourDetailsPrivateResponse(
                    caseData,
                    buildResponseForRespondent,
                    party
                );
                break;
            case CONFIRM_EDIT_CONTACT_DETAILS:
                buildResponseForRespondent = buildCitizenDetailsResponse(caseData, buildResponseForRespondent);
                break;
            case ATTENDING_THE_COURT:
                AttendToCourt attendToCourt = optimiseAttendingCourt(caseData.getRespondentSolicitorData()
                                                                         .getRespondentAttendingTheCourt());
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .attendToCourt(attendToCourt)
                    .build();
                break;
            case MIAM:
                buildResponseForRespondent = buildMiamResponse(caseData, buildResponseForRespondent);
                break;
            case CURRENT_OR_PREVIOUS_PROCEEDINGS:
                buildResponseForRespondent = buildOtherProceedingsResponse(
                    caseData,
                    buildResponseForRespondent,
                    solicitor
                );
                break;
            case ALLEGATION_OF_HARM:
                buildResponseForRespondent = buildAoHResponse(caseData, buildResponseForRespondent, solicitor);
                break;
            case INTERNATIONAL_ELEMENT:
                buildResponseForRespondent = buildInternationalElementResponse(caseData, buildResponseForRespondent);
                break;
            case ABILITY_TO_PARTICIPATE:
                buildResponseForRespondent = buildAbilityToParticipateResponse(caseData, buildResponseForRespondent);
                break;
            case VIEW_DRAFT_RESPONSE:
            case SUBMIT:

            default:
                break;
        }
        PartyDetails amended = party.getValue().toBuilder()
            .response(buildResponseForRespondent).build();
        Optional<Element<PartyDetails>> partyElement = respondents.stream().filter(element -> element.getId()
            .equals(party.getId())).findFirst();
        if (partyElement.isPresent()) {
            int index = respondents.indexOf(partyElement.get());
            if (index != -1) {
                respondents.set(index, element(party.getId(), amended));
            }
        }
    }

    private Response buildOtherProceedingsResponse(CaseData caseData, Response buildResponseForRespondent, String solicitor) {
        List<Element<RespondentProceedingDetails>> respondentExistingProceedings
            = YesNoDontKnow.yes.equals(caseData.getRespondentSolicitorData()
                                           .getCurrentOrPastProceedingsForChildren())
            ? caseData.getRespondentSolicitorData()
            .getRespondentExistingProceedings() : null;

        for (Element<RespondentProceedingDetails> proceedings : respondentExistingProceedings) {
            if (null != proceedings.getValue()
                && null != proceedings.getValue().getUploadRelevantOrder()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + " (solicitor)",
                    proceedings.getValue().getUploadRelevantOrder()
                );
            }
        }

        return buildResponseForRespondent.toBuilder()
            .currentOrPastProceedingsForChildren(caseData.getRespondentSolicitorData()
                                                     .getCurrentOrPastProceedingsForChildren())
            .respondentExistingProceedings(respondentExistingProceedings)
            .build();
    }

    private Response buildAbilityToParticipateResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .abilityToParticipate(AbilityToParticipate.builder()
                                      .factorsAffectingAbilityToParticipate(caseData.getRespondentSolicitorData()
                                                                                .getAbilityToParticipateInProceedings()
                                                                                .getFactorsAffectingAbilityToParticipate())
                                      .provideDetailsForFactorsAffectingAbilityToParticipate(
                                          YesNoDontKnow.yes.equals(
                                              caseData.getRespondentSolicitorData()
                                                  .getAbilityToParticipateInProceedings().getFactorsAffectingAbilityToParticipate())
                                              ? caseData.getRespondentSolicitorData()
                                              .getAbilityToParticipateInProceedings()
                                              .getProvideDetailsForFactorsAffectingAbilityToParticipate()
                                              : null)
                                      .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildInternationalElementResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .citizenInternationalElements(CitizenInternationalElements
                                              .builder()
                                              .childrenLiveOutsideOfEnWl(caseData
                                                                             .getRespondentSolicitorData()
                                                                             .getInternationalElementChild()
                                                                             .getChildrenLiveOutsideOfEnWl())
                                              .childrenLiveOutsideOfEnWlDetails(Yes
                                                                                    .equals(caseData
                                                                                                .getRespondentSolicitorData()
                                                                                                .getInternationalElementChild()
                                                                                                .getChildrenLiveOutsideOfEnWl()
                                                                                    ) ? caseData
                                                  .getRespondentSolicitorData()
                                                  .getInternationalElementChild()
                                                  .getChildrenLiveOutsideOfEnWlDetails() : null)
                                              .parentsAnyOneLiveOutsideEnWl(caseData
                                                                                .getRespondentSolicitorData()
                                                                                .getInternationalElementChild()
                                                                                .getParentsAnyOneLiveOutsideEnWl())
                                              .parentsAnyOneLiveOutsideEnWlDetails(Yes
                                                                                       .equals(caseData
                                                                                                   .getRespondentSolicitorData()
                                                                                                   .getInternationalElementChild()
                                                                                                   .getParentsAnyOneLiveOutsideEnWl()
                                                                                       ) ? caseData
                                                  .getRespondentSolicitorData()
                                                  .getInternationalElementChild()
                                                  .getParentsAnyOneLiveOutsideEnWlDetails() : null
                                              )
                                              .anotherPersonOrderOutsideEnWl(caseData
                                                                                 .getRespondentSolicitorData()
                                                                                 .getInternationalElementChild()
                                                                                 .getAnotherPersonOrderOutsideEnWl())
                                              .anotherPersonOrderOutsideEnWlDetails(Yes
                                                                                        .equals(caseData
                                                                                                    .getRespondentSolicitorData()
                                                                                                    .getInternationalElementChild()
                                                                                                    .getAnotherPersonOrderOutsideEnWl()
                                                                                        ) ? caseData
                                                  .getRespondentSolicitorData()
                                                  .getInternationalElementChild()
                                                  .getAnotherPersonOrderOutsideEnWlDetails() : null
                                              )
                                              .anotherCountryAskedInformation(caseData
                                                                                  .getRespondentSolicitorData()
                                                                                  .getInternationalElementChild()
                                                                                  .getAnotherCountryAskedInformation())
                                              .anotherCountryAskedInformationDetaails(Yes
                                                                                          .equals(caseData
                                                                                                      .getRespondentSolicitorData()
                                                                                                      .getInternationalElementChild()
                                                                                                      .getAnotherCountryAskedInformation()
                                                                                          ) ? caseData
                                                  .getRespondentSolicitorData()
                                                  .getInternationalElementChild()
                                                  .getAnotherCountryAskedInformationDetaails() : null)
                                              .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildAoHResponse(CaseData caseData, Response buildResponseForRespondent, String solicitor) {
        RespondentAllegationsOfHarm respondentAllegationsOfHarm
            = caseData.getRespondentSolicitorData().getRespondentAllegationsOfHarm();
        if (null != respondentAllegationsOfHarm.getRespondentUndertakingDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentUndertakingDocument()
            );
        }

        if (null != respondentAllegationsOfHarm.getRespondentForcedMarriageDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentForcedMarriageDocument()
            );
        }
        if (null != respondentAllegationsOfHarm.getRespondentNonMolestationOrderDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentNonMolestationOrderDocument()
            );
        }

        if (null != respondentAllegationsOfHarm.getRespondentOccupationOrderDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentOccupationOrderDocument()
            );
        }

        if (null != respondentAllegationsOfHarm.getRespondentOtherInjunctiveDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentOtherInjunctiveDocument()
            );
        }

        if (null != respondentAllegationsOfHarm.getRespondentRestrainingDocument()) {
            buildRespondentDocs(
                caseData,
                caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                solicitor + " (solicitor)",
                respondentAllegationsOfHarm.getRespondentRestrainingDocument()
            );
        }

        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .respondentAllegationsOfHarmData(
                RespondentAllegationsOfHarmData
                    .builder()
                    .respAohYesOrNo(caseData.getRespondentSolicitorData().getRespondentAohYesNo())
                    .respAllegationsOfHarmInfo(caseData.getRespondentSolicitorData().getRespondentAllegationsOfHarm())
                    .respDomesticAbuseInfo(caseData.getRespondentSolicitorData().getRespondentDomesticAbuseBehaviour())
                    .respChildAbuseInfo(caseData.getRespondentSolicitorData().getRespondentChildAbuseBehaviour())
                    .respChildAbductionInfo(caseData.getRespondentSolicitorData().getRespondentChildAbduction())
                    .respOtherConcernsInfo(caseData.getRespondentSolicitorData().getRespondentOtherConcerns())
                    .build())
            .build();
        return buildResponseForRespondent;
    }

    private void buildRespondentDocs(CaseData caseData, String respondentName, String solicitorName, Document document) {
        RespondentDocs respondentDocs = RespondentDocs.builder()
            .otherDocuments(List.of(element(ResponseDocuments
                                                .builder()
                                                .partyName(respondentName)
                                                .createdBy(solicitorName)
                                                .dateCreated(LocalDate.now())
                                                .citizenDocument(document)
                                                .build())))
            .build();

        if (CollectionUtils.isNotEmpty(caseData.getRespondentSolicitorData().getRespondentDocsList())) {
            caseData.getRespondentSolicitorData().getRespondentDocsList().add(element(respondentDocs));
        } else {
            caseData.getRespondentSolicitorData().setRespondentDocsList(List.of(element(respondentDocs)));
        }
    }

    private Response buildMiamResponse(CaseData caseData, Response buildResponseForRespondent) {
        boolean attendedMiam = Yes.equals(caseData.getRespondentSolicitorData()
                                              .getRespondentSolicitorHaveYouAttendedMiam().getAttendedMiam());
        boolean willingToAttendMiam = attendedMiam && Yes.equals(caseData.getRespondentSolicitorData()
                                                                     .getRespondentSolicitorHaveYouAttendedMiam()
                                                                     .getWillingToAttendMiam());
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .miam(Miam.builder()
                      .attendedMiam(caseData.getRespondentSolicitorData()
                                        .getRespondentSolicitorHaveYouAttendedMiam().getAttendedMiam())
                      .willingToAttendMiam(attendedMiam ? null : caseData.getRespondentSolicitorData()
                          .getRespondentSolicitorHaveYouAttendedMiam().getWillingToAttendMiam())
                      .reasonNotAttendingMiam(
                          willingToAttendMiam ? null : caseData
                              .getRespondentSolicitorData().getRespondentSolicitorHaveYouAttendedMiam()
                              .getReasonNotAttendingMiam()).build()).build();
        return buildResponseForRespondent;
    }

    private Response buildCitizenDetailsResponse(CaseData caseData, Response buildResponseForRespondent) {
        CitizenDetails citizenDetails = caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails();
        buildResponseForRespondent = buildResponseForRespondent
            .toBuilder().citizenDetails(
                buildResponseForRespondent.getCitizenDetails()
                    .toBuilder()
                    .firstName(citizenDetails.getFirstName())
                    .lastName(citizenDetails.getLastName())
                    .dateOfBirth(citizenDetails.getDateOfBirth())
                    .previousName(citizenDetails.getPreviousName())
                    .placeOfBirth(citizenDetails.getPlaceOfBirth())
                    .address(citizenDetails.getAddress())
                    .addressHistory(citizenDetails.getAddressHistory())
                    .contact(citizenDetails.getContact())
                    .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildKeepYourDetailsPrivateResponse(CaseData caseData, Response buildResponseForRespondent,
                                                         Element<PartyDetails> respondent) {
        List<ConfidentialityListEnum> confList = null;
        if (null != caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            && YesOrNo.Yes.equals(caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentiality())) {
            confList = caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentialityList();
            if (confList.contains(ConfidentialityListEnum.address)) {
                respondent.getValue().setIsAddressConfidential(Yes);
            } else {
                respondent.getValue().setIsAddressConfidential(No);
            }
            if (confList.contains(ConfidentialityListEnum.email)) {
                respondent.getValue().setIsEmailAddressConfidential(Yes);
            } else {
                respondent.getValue().setIsEmailAddressConfidential(No);
            }
            if (confList.contains(ConfidentialityListEnum.phoneNumber)) {
                respondent.getValue().setIsPhoneNumberConfidential(Yes);
            } else {
                respondent.getValue().setIsPhoneNumberConfidential(No);
            }
        } else if (null != caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            && YesOrNo.No.equals(caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentiality())) {
            respondent.getValue().setIsAddressConfidential(No);
            respondent.getValue().setIsEmailAddressConfidential(No);
            respondent.getValue().setIsPhoneNumberConfidential(No);
        }

        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .keepDetailsPrivate(KeepDetailsPrivate.builder()
                                    .otherPeopleKnowYourContactDetails(
                                        caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate() != null
                                            ? caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                                            .getOtherPeopleKnowYourContactDetails() : null)
                                    .confidentiality(caseData
                                                         .getRespondentSolicitorData()
                                                         .getKeepContactDetailsPrivate()
                                                         .getConfidentiality())
                                    .confidentialityList(confList)
                                    .build()).build();
        return buildResponseForRespondent;
    }

    private AttendToCourt optimiseAttendingCourt(AttendToCourt attendToCourt) {
        return attendToCourt.toBuilder()
            .respondentWelshNeedsList(YesOrNo.No.equals(attendToCourt.getRespondentWelshNeeds()) ? null
                                          : attendToCourt.getRespondentWelshNeedsList())
            .respondentInterpreterNeeds(YesOrNo.No.equals(attendToCourt.getIsRespondentNeededInterpreter()) ? null
                                            : attendToCourt.getRespondentInterpreterNeeds())
            .disabilityNeeds(YesOrNo.No.equals(attendToCourt.getHaveAnyDisability()) ? null
                                 : attendToCourt.getDisabilityNeeds())
            .respondentSpecialArrangementDetails(YesOrNo.No.equals(attendToCourt.getRespondentSpecialArrangements()) ? null
                                                     : attendToCourt.getRespondentSpecialArrangementDetails())
            .respondentIntermediaryNeedDetails(YesOrNo.No.equals(attendToCourt.getRespondentIntermediaryNeeds()) ? null
                                                   : attendToCourt.getRespondentIntermediaryNeedDetails())
            .build();
    }

    private Consent optimiseConsent(Consent consent) {
        String noConsentReason = consent.getNoConsentReason();
        String courtOrderDetails = consent.getCourtOrderDetails();
        if (YesOrNo.Yes.equals(consent.getConsentToTheApplication())) {
            noConsentReason = null;
        }
        if (YesOrNo.No.equals(consent.getPermissionFromCourt())) {
            courtOrderDetails = null;
        }
        return consent.toBuilder()
            .noConsentReason(noConsentReason)
            .courtOrderDetails(courtOrderDetails)
            .build();
    }

    public Optional<SolicitorRole> getSolicitorRole(CallbackRequest callbackRequest) {
        if (callbackRequest.getEventId().isEmpty()) {
            throw new RespondentSolicitorException(TECH_ERROR);
        } else {
            String invokingSolicitor = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);

            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingSolicitor);

            if (solicitorRole.isPresent()) {
                return solicitorRole;
            }
        }
        return Optional.empty();
    }

    public Element<PartyDetails> findSolicitorRepresentedRespondents(CallbackRequest callbackRequest, SolicitorRole solicitorRole) {
        Element<PartyDetails> solicitorRepresentedRespondent;
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        solicitorRepresentedRespondent = caseData.getRespondents().get(solicitorRole.getIndex());
        if (solicitorRepresentedRespondent.getValue().getResponse() != null
            && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getC7ResponseSubmitted())) {
            throw new RespondentSolicitorException(
                RESPONSE_ALREADY_SUBMITTED_ERROR);
        }
        return solicitorRepresentedRespondent;
    }

    public Map<String, Object> generateConfidentialityDynamicSelectionDisplay(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        StringBuilder selectedList = new StringBuilder();

        selectedList.append("<ul>");
        for (ConfidentialityListEnum confidentiality : caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            .getConfidentialityList()) {
            selectedList.append("<li>");
            selectedList.append(confidentiality.getDisplayedValue());
            selectedList.append("</li>");
        }
        selectedList.append("</ul>");

        Map<String, Object> keepDetailsPrivateList = new HashMap<>();
        keepDetailsPrivateList.put("confidentialListDetails", selectedList);
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            .getConfidentialityList().contains(ConfidentialityListEnum.address)) {
            keepDetailsPrivateList.put("isAddressConfidential", YesOrNo.Yes);
        }
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            .getConfidentialityList().contains(ConfidentialityListEnum.phoneNumber)) {
            keepDetailsPrivateList.put("isPhoneNumberConfidential", YesOrNo.Yes);
        }
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            .getConfidentialityList().contains(ConfidentialityListEnum.email)) {
            keepDetailsPrivateList.put("isEmailAddressConfidential", YesOrNo.Yes);
        }
        return keepDetailsPrivateList;
    }

    public Map<String, Object> validateActiveRespondentResponse(CallbackRequest callbackRequest, List<String> errorList,
                                                                String authorisation) throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        String invokingRespondent = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);
        boolean mandatoryFinished = false;
        generateDraftDocumentsForRespondent(callbackRequest, authorisation);
        if (!caseData.getRespondents().isEmpty()) {
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingRespondent);
            if (solicitorRole.isPresent() && caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                Element<PartyDetails> respondingParty = caseData.getRespondents().get(solicitorRole.get().getIndex());

                if (respondingParty.getValue() != null
                    && respondingParty.getValue().getUser() != null
                    && YesOrNo.Yes.equals(respondingParty.getValue().getUser().getSolicitorRepresented())) {
                    mandatoryFinished = responseSubmitChecker.isFinished(respondingParty.getValue());
                }
            }
        }
        if (!mandatoryFinished) {
            errorList.add(
                "Response submission is not allowed for this case unless you finish all the mandatory information");
        }
        return caseDataUpdated;
    }

    public Map<String, Object> submitC7ResponseForActiveRespondent(String authorisation, CallbackRequest callbackRequest) throws Exception {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> representedRespondent = null;
        if (solicitorRole.isPresent()) {
            representedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        }

        if (representedRespondent != null && representedRespondent.getValue() != null && PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(
            caseData.getCaseTypeOfApplication())) {
            PartyDetails amended = representedRespondent.getValue().toBuilder()
                .response(representedRespondent.getValue().getResponse().toBuilder().c7ResponseSubmitted(Yes).build())
                .build();
            String party = representedRespondent.getValue().getLabelForDynamicList();
            String createdBy = StringUtils.isEmpty(representedRespondent.getValue().getRepresentativeLabelForDynamicList())
                ? party : representedRespondent.getValue().getRepresentativeLabelForDynamicList() + SOLICITOR;

            caseData.getRespondents().set(
                caseData.getRespondents().indexOf(representedRespondent),
                element(representedRespondent.getId(), amended)
            );

            updatedCaseData.put(RESPONDENTS, caseData.getRespondents());

            Document c7FinalDocument = null;
            Map<String, Object> dataMap = populateDataMap(callbackRequest, representedRespondent);
            c7FinalDocument = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C7_FINAL_DOCUMENT,
                false,
                dataMap
            );
            updatedCaseData.put("finalC7ResponseDoc", c7FinalDocument);

            Document c1aFinalDocument = null;
            if (Yes.equals(caseData.getRespondentSolicitorData().getRespondentAohYesNo())) {
                c1aFinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C1A_FINAL_DOCUMENT,
                    false,
                    dataMap
                );
                updatedCaseData.put("finalC1AResponseDoc", c1aFinalDocument);
            }

            Document c8FinalDocument = null;
            c8FinalDocument = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                C8_RESP_FINAL_HINT,
                false,
                dataMap
            );
            updatedCaseData.put("finalC8ResponseDoc", c8FinalDocument);

            RespondentDocs respondentDocs = RespondentDocs.builder().build();
            if (null != c1aFinalDocument) {
                respondentDocs = respondentDocs
                    .toBuilder()
                    .c1aDocument(ResponseDocuments
                                     .builder()
                                     .partyName(party)
                                     .createdBy(createdBy)
                                     .dateCreated(LocalDate.now())
                                     .citizenDocument(c1aFinalDocument)
                                     .build()
                    )
                    .build();
            }
            if (null != c7FinalDocument) {
                respondentDocs = respondentDocs
                    .toBuilder()
                    .c7Document(ResponseDocuments
                                    .builder()
                                    .partyName(party)
                                    .createdBy(createdBy)
                                    .dateCreated(LocalDate.now())
                                    .citizenDocument(c7FinalDocument)
                                    .build()
                    )
                    .build();
            }

            if (null != caseData.getRespondentSolicitorData().getRespondentDocsList()) {
                caseData.getRespondentSolicitorData().getRespondentDocsList().add(element(respondentDocs));
            } else {
                caseData.getRespondentSolicitorData().setRespondentDocsList(List.of(element(respondentDocs)));
            }
            updatedCaseData.put("respondentDocsList", caseData.getRespondentSolicitorData().getRespondentDocsList());

            if (null != c8FinalDocument) {
                updatedCaseData.put(
                    getKeyForDoc(solicitorRole.get()).get(0),
                    ResponseDocuments.builder()
                        .partyName(party)
                        .createdBy(createdBy)
                        .dateCreated(LocalDate.now())
                        .citizenDocument(c8FinalDocument)
                        .build()
                );
            }
        }

        return updatedCaseData;
    }

    private List<String> getKeyForDoc(SolicitorRole solicitorRole) {
        String c8Key;
        switch (solicitorRole.getEventId()) {
            case "A":
                c8Key = "respondentAc8";
                break;
            case "B":
                c8Key = "respondentBc8";
                break;
            case "C":
                c8Key = "respondentCc8";
                break;
            case "D":
                c8Key = "respondentDc8";
                break;
            case "E":
                c8Key = "respondentEc8";
                break;
            default:
                c8Key = "";
                break;
        }
        return List.of(c8Key);
    }

    public Map<String, Object> populateDataMap(CallbackRequest callbackRequest, Element<PartyDetails> solicitorRepresentedRespondent) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("courtName", callbackRequest.getCaseDetails().getData().get(COURT_NAME));
        dataMap.put("id", callbackRequest.getCaseDetails().getId());
        dataMap.put("issueDate", callbackRequest.getCaseDetails().getData().get(ISSUE_DATE_FIELD));
        List<Element<Child>> listOfChildren = (List<Element<Child>>) callbackRequest.getCaseDetails().getData().get(
            CHILDREN);
        dataMap.put("children", listOfChildren);

        if (solicitorRepresentedRespondent == null) {
            Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
            if (solicitorRole.isPresent()) {
                solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(
                    callbackRequest,
                    solicitorRole.get()
                );
            }
        }
        if (null != solicitorRepresentedRespondent.getValue().getSolicitorOrg()) {
            getOrganisationAddress(solicitorRepresentedRespondent, dataMap);
        }

        dataMap.put("respondent", solicitorRepresentedRespondent.getValue());
        Response response = solicitorRepresentedRespondent.getValue().getResponse();

        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsEmailAddressConfidential())) {
            dataMap.put("email", THIS_INFORMATION_IS_CONFIDENTIAL);
        } else if (null != response.getCitizenDetails().getContact()
            && StringUtils.isNoneEmpty(response.getCitizenDetails().getContact().getEmail())) {
            dataMap.put("email", response.getCitizenDetails().getContact().getEmail());
        } else {
            dataMap.put("email", solicitorRepresentedRespondent.getValue().getEmail());
        }
        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsPhoneNumberConfidential())) {
            dataMap.put("phone", THIS_INFORMATION_IS_CONFIDENTIAL);
        } else if (null != response.getCitizenDetails().getContact()
            && StringUtils.isNoneEmpty(response.getCitizenDetails().getContact().getPhoneNumber())) {
            dataMap.put("phone", response.getCitizenDetails().getContact().getPhoneNumber());
        } else {
            dataMap.put("phone", solicitorRepresentedRespondent.getValue().getPhoneNumber());
        }
        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsAddressConfidential())) {
            dataMap.put("address", THIS_INFORMATION_IS_CONFIDENTIAL);
        } else if (null != response.getCitizenDetails().getAddress()) {
            dataMap.put("address", response.getCitizenDetails().getAddress().getAddressLine1());
        } else {
            dataMap.put("address", solicitorRepresentedRespondent.getValue().getAddress().getAddressLine1());
        }
        dataMap.put("gender", solicitorRepresentedRespondent.getValue().getGender());
        dataMap.put("repFirstName", solicitorRepresentedRespondent.getValue().getRepresentativeFirstName());
        dataMap.put("repLastName", solicitorRepresentedRespondent.getValue().getRepresentativeLastName());
        dataMap.put("repFullName", solicitorRepresentedRespondent
            .getValue().getRepresentativeFirstName() + " " + solicitorRepresentedRespondent
            .getValue().getRepresentativeLastName());
        populateAddressMap(solicitorRepresentedRespondent, dataMap);
        dataMap.put("repEmail", solicitorRepresentedRespondent.getValue().getSolicitorEmail());
        dataMap.put("repTelephone", solicitorRepresentedRespondent.getValue().getSolicitorTelephone());
        if (solicitorRepresentedRespondent.getValue().getDxNumber() != null) {
            dataMap.put("dxNumber", solicitorRepresentedRespondent.getValue().getDxNumber());
        } else {
            if (solicitorRepresentedRespondent.getValue().getOrganisations() != null) {
                for (ContactInformation contactInformationLoop : solicitorRepresentedRespondent
                    .getValue().getOrganisations().getContactInformation()) {
                    for (DxAddress dxAddress : contactInformationLoop.getDxAddress()) {
                        dataMap.put("dxNumber", dxAddress.getDxNumber());
                    }
                }
            }
        }
        dataMap.put("repReference", solicitorRepresentedRespondent.getValue().getSolicitorReference());

        dataMap.put("fullName", response.getCitizenDetails()
            .getFirstName() + " " + response.getCitizenDetails()
            .getLastName());

        dataMap.put("dob", response.getCitizenDetails().getDateOfBirth());
        dataMap.put("applicationReceivedDate", response.getConsent().getApplicationReceivedDate());
        List<Element<RespondentProceedingDetails>> proceedingsList = response.getRespondentExistingProceedings();
        dataMap.put("respondentsExistingProceedings", proceedingsList);
        populateAohDataMap(response, dataMap);
        dataMap.put("consentToTheApplication", response.getConsent().getConsentToTheApplication());
        dataMap.put("noConsentReason", response.getConsent().getNoConsentReason());
        dataMap.put("permissionFromCourt", response.getConsent().getPermissionFromCourt());
        dataMap.put("courtOrderDetails", response.getConsent().getCourtOrderDetails());
        dataMap.put("attendedMiam", response.getMiam().getAttendedMiam());
        dataMap.put("willingToAttendMiam", response.getMiam().getWillingToAttendMiam());
        dataMap.put("reasonNotAttendingMiam", response.getMiam().getReasonNotAttendingMiam());
        dataMap.put("currentOrPastProceedingsForChildren", response.getCurrentOrPastProceedingsForChildren());
        dataMap.put("childAbuseInfo", response.getRespondentAllegationsOfHarmData().getRespChildAbuseInfo());
        dataMap.put("reasonForChild", response.getCitizenInternationalElements().getChildrenLiveOutsideOfEnWl());
        dataMap.put(
            "reasonForChildDetails",
            response.getCitizenInternationalElements().getChildrenLiveOutsideOfEnWlDetails()
        );
        dataMap.put("reasonForParent", response.getCitizenInternationalElements().getParentsAnyOneLiveOutsideEnWl());
        dataMap.put(
            "reasonForParentDetails",
            response.getCitizenInternationalElements().getParentsAnyOneLiveOutsideEnWlDetails()
        );
        dataMap.put(
            "reasonForJurisdiction",
            response.getCitizenInternationalElements().getAnotherPersonOrderOutsideEnWl()
        );
        dataMap.put(
            "reasonForJurisdictionDetails",
            response.getCitizenInternationalElements().getAnotherPersonOrderOutsideEnWlDetails()
        );
        dataMap.put(
            "requestToAuthority",
            response.getCitizenInternationalElements().getAnotherCountryAskedInformation()
        );
        dataMap.put(
            "requestToAuthorityDetails",
            response.getCitizenInternationalElements().getAnotherCountryAskedInformationDetaails()
        );
        dataMap.put(
            "solicitorRepresented",
            solicitorRepresentedRespondent.getValue().getUser().getSolicitorRepresented()
        );
        dataMap.put("reasonableAdjustments", response.getSupportYouNeed().getReasonableAdjustments());
        dataMap.put("attendingTheCourt", response.getAttendToCourt());
        log.info("Data map ::>>  {}",dataMap);
        return dataMap;
    }

    private void populateAohDataMap(Response response, Map<String, Object> dataMap) {
        if (response.getRespondentAllegationsOfHarmData().getRespAllegationsOfHarmInfo() != null) {
            dataMap.put("nonMolestationOrderIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrderIssueDate());
            dataMap.put("nonMolestationOrderEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrderEndDate());
            dataMap.put("aoh", response.getRespondentAllegationsOfHarmData().getRespAllegationsOfHarmInfo());
            dataMap.put("nonMolestationOrderIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrderIsCurrent());
            dataMap.put("nonMolestationOrderCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrderCourt());
            dataMap.put("nonMolestationOrderCaseNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrderCaseNumber());
            dataMap.put("occupationOrderIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOccupationOrderIssueDate());
            dataMap.put("occupationOrderEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOccupationOrderEndDate());
            dataMap.put("occupationOrderIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOccupationOrderIsCurrent());
            dataMap.put("occupationOrderCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOccupationOrderCourt());
            dataMap.put("occupationCaseNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOccupationOrderCaseNumber());
            dataMap.put("forcedMarriageIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentForcedMarriageIssueDate());
            dataMap.put("forcedMarriageEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentForcedMarriageEndDate());
            dataMap.put("forcedMarriageIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentForcedMarriageIsCurrent());
            dataMap.put("forcedMarriageCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentForcedMarriageCourt());
            dataMap.put("forcedMarriageNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentForcedMarriageCaseNumber());
            dataMap.put("restrainingIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentRestrainingIssueDate());
            dataMap.put("restrainingEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentRestrainingEndDate());
            dataMap.put("restrainingIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentRestrainingIsCurrent());
            dataMap.put("restrainingCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentRestrainingCourt());
            dataMap.put("restrainingNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentRestrainingCaseNumber());
            dataMap.put("otherInjunctiveIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveIssueDate());
            dataMap.put("otherInjunctiveEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveEndDate());
            dataMap.put("otherInjunctiveIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveIsCurrent());
            dataMap.put("otherInjunctiveCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveCourt());
            dataMap.put("otherInjunctiveNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveCaseNumber());
            dataMap.put("undertakingIssueDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentUndertakingIssueDate());
            dataMap.put("undertakingEndDate", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentUndertakingEndDate());
            dataMap.put("undertakingIsCurrent", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentUndertakingIsCurrent());
            dataMap.put("undertakingCourt", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentUndertakingCourt());
            dataMap.put("undertakingNumber", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentUndertakingCaseNumber());
            dataMap.put("domesticAbuse", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentDomesticAbuse());
            dataMap.put("domesticAbuseInfo", response.getRespondentAllegationsOfHarmData().getRespDomesticAbuseInfo());
            dataMap.put("drugAlcoholAbuse", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentDrugOrAlcoholAbuse());
            dataMap.put("otherConcerns", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentOtherSafetyConcerns());
            dataMap.put("ordersRespondentWantsFromCourt", response.getRespondentAllegationsOfHarmData()
                .getRespOtherConcernsInfo().getOrdersRespondentWantFromCourt());
            dataMap.put("childSpendingUnsupervisedTime", response.getRespondentAllegationsOfHarmData()
                .getRespOtherConcernsInfo().getChildSpendingUnsupervisedTime());
            dataMap.put("childSpendingSupervisedTime", response.getRespondentAllegationsOfHarmData()
                .getRespOtherConcernsInfo().getChildSpendingSupervisedTime());
            dataMap.put("childHavingOtherFormOfContact", response.getRespondentAllegationsOfHarmData()
                .getRespOtherConcernsInfo().getChildHavingOtherFormOfContact());
            dataMap.put("childAbuse", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getRespondentChildAbuse());
            dataMap.put("childAbductionInfo", response.getRespondentAllegationsOfHarmData()
                .getRespAllegationsOfHarmInfo().getIsRespondentChildAbduction());
            dataMap.put("reasonChildAbductionBelief", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getReasonForChildAbductionBelief());
            dataMap.put("previousThreatsForChildAbduction", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getPreviousThreatsForChildAbduction());
            dataMap.put("previousThreatsForChildAbductionDetails", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getPreviousThreatsForChildAbductionDetails());
            dataMap.put("whereIsChild", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getWhereIsChild());
            dataMap.put("passportOfficeBeenNotified", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getHasPassportOfficeNotified());
            dataMap.put("orgInvolvedInPreviousAbductions", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getAnyOrgInvolvedInPreviousAbduction());
            dataMap.put("orgInvolvedInPreviousAbductionsDetails", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getAnyOrgInvolvedInPreviousAbductionDetails());
            dataMap.put(
                "childrenHavePassport",
                response.getRespondentAllegationsOfHarmData().getRespChildAbductionInfo()
                    .getChildrenHavePassport()
            );
            dataMap.put("childrenHaveMoreThanOnePassport", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getChildrenHaveMoreThanOnePassport());
            dataMap.put("whoHasChildrenPassport", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getWhoHasChildPassport());
            dataMap.put("whoHasChildrenPassportOther", response.getRespondentAllegationsOfHarmData()
                .getRespChildAbductionInfo().getWhoHasChildPassportOther());
        }
    }

    private void populateAddressMap(Element<PartyDetails> solicitorRepresentedRespondent, Map<String, Object> dataMap) {
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine1() != null) {
            dataMap.put(
                "repAddressLine1",
                solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine1()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine2() != null) {
            dataMap.put(
                "repAddressLine2",
                solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine2()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine3() != null) {
            dataMap.put(
                "repAddressLine3",
                solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine3()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getPostCode() != null) {
            dataMap.put("repPostcode", solicitorRepresentedRespondent.getValue().getSolicitorAddress().getPostCode());
        }
    }

    public Map<String, Object> generateDraftDocumentsForRespondent(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> dataMap = populateDataMap(callbackRequest, null);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            SOLICITOR_C7_DRAFT_DOCUMENT,
            false,
            dataMap
        );
        caseDataUpdated.put("draftC7ResponseDoc", document);

        if (Yes.equals(caseData.getRespondentSolicitorData().getRespondentAohYesNo())) {
            Document documentForC1A = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false,
                dataMap
            );
            caseDataUpdated.put("draftC1ADoc", documentForC1A);
        }
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
            String representedRespondentName = solicitorRepresentedRespondent.getValue().getFirstName() + " "
                + solicitorRepresentedRespondent.getValue().getLastName();

            caseDataUpdated.put(RESPONDENT_NAME_FOR_RESPONSE, representedRespondentName);
        }
        return caseDataUpdated;
    }

    private void getOrganisationAddress(Element<PartyDetails> respondingParty, Map<String, Object> dataMap) {
        Address address = respondingParty.getValue().getSolicitorAddress();
        String orgName = "";
        String systemAuthorisation = systemUserService.getSysUserToken();
        try {
            Organisations orgDetails = organisationService.getOrganisationDetails(
                systemAuthorisation,
                respondingParty.getValue()
                    .getSolicitorOrg().getOrganisationID()
            );
            if (null != orgDetails && null != orgDetails.getContactInformation()) {
                address = orgDetails.getContactInformation().get(0).toAddress();
                orgName = orgDetails.getName();
            }
        } catch (Exception e) {
            log.error("Error fetching organisation for respondent solicitor {}", e.getMessage());
        }
        dataMap.put("solicitorAddress", address);
        dataMap.put("solicitorOrg", Organisation.builder().organisationName(orgName)
            .build());
    }

    public SubmittedCallbackResponse submittedC7Response(CaseData caseData) {
        return SubmittedCallbackResponse.builder().confirmationHeader(
            RESPONSE_SUBMITTED_LABEL).confirmationBody(CONTACT_LOCAL_COURT_LABEL.concat(null != caseData.getCourtName()
                                                                                            ? caseData.getCourtName() : ""))
            .build();
    }
}
