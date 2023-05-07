package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.exception.RespondentSolicitorException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorAbilityToParticipateInProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
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

    @Autowired
    private final RespondentSolicitorMiamService miamService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final DocumentGenService documentGenService;

    @Autowired
    private final ResponseSubmitChecker responseSubmitChecker;

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Element<PartyDetails> solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(
            callbackRequest
        );
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
                    String[] keepDetailsPrivateFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        keepDetailsPrivateFields[0],
                        solicitorRepresentedRespondent.getValue().getResponse()
                            .getSolicitorKeepDetailsPriate().getRespKeepDetailsPrivate()
                    );
                    caseDataUpdated.put(
                        keepDetailsPrivateFields[1],
                        solicitorRepresentedRespondent.getValue().getResponse()
                            .getSolicitorKeepDetailsPriate().getRespKeepDetailsPrivateConfidentiality()
                    );
                    break;
                case CONFIRM_EDIT_CONTACT_DETAILS:
                    CitizenDetails citizenDetails = solicitorRepresentedRespondent.getValue().getResponse().getCitizenDetails();
                    PartyDetails partyDetails = solicitorRepresentedRespondent.getValue();
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        CitizenDetails.builder()
                            .address(Optional.ofNullable(citizenDetails.getAddress()).orElse(partyDetails.getAddress()))
                            .addressHistory(Optional.ofNullable(citizenDetails.getAddressHistory()).orElse(
                                AddressHistory.builder().isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years())
                                    .build()
                            ))
                            .contact(Optional.ofNullable(citizenDetails.getContact()).orElse(Contact.builder()
                                                                                                 .phoneNumber(
                                                                                                     partyDetails
                                                                                                         .getPhoneNumber())
                                                                                                 .email(partyDetails.getEmail())
                                                                                                 .build()))
                            .dateOfBirth(Optional.ofNullable(citizenDetails.getDateOfBirth()).orElse(partyDetails.getDateOfBirth()))
                            .firstName(Optional.ofNullable(citizenDetails.getFirstName()).orElse(partyDetails.getFirstName()))
                            .lastName(Optional.ofNullable(citizenDetails.getLastName()).orElse(partyDetails.getLastName()))
                            .placeOfBirth(Optional.ofNullable(citizenDetails.getPlaceOfBirth()).orElse(partyDetails.getPlaceOfBirth()))
                            .previousName(Optional.ofNullable(citizenDetails.getPreviousName()).orElse(partyDetails.getPreviousName()))
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
                        solicitorRepresentedRespondent.getValue().getResponse().getSolicitorMiam().getRespSolHaveYouAttendedMiam()
                    );
                    caseDataUpdated.put(
                        miamFields[1],
                        solicitorRepresentedRespondent.getValue().getResponse().getSolicitorMiam().getRespSolWillingnessToAttendMiam()
                    );
                    caseDataUpdated.put(miamFields[2], miamService.getCollapsableOfWhatIsMiamPlaceHolder());
                    caseDataUpdated.put(
                        miamFields[3],
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
                        solicitorRepresentedRespondent.getValue().getResponse().getResSolInternationalElements().getInternationalElementChildInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[1],
                        solicitorRepresentedRespondent.getValue().getResponse().getResSolInternationalElements().getInternationalElementParentInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[2],
                        solicitorRepresentedRespondent.getValue().getResponse()
                            .getResSolInternationalElements().getInternationalElementJurisdictionInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[3],
                        solicitorRepresentedRespondent.getValue().getResponse().getResSolInternationalElements().getInternationalElementRequestInfo()
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
        Element<PartyDetails> solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest);

        String invokingEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        RespondentSolicitorEvents.getCaseFieldName(invokingEvent)
            .ifPresent(event -> buildResponseForRespondent(
                caseData,
                respondents,
                solicitorRepresentedRespondent,
                event
            ));

        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    private void buildResponseForRespondent(CaseData caseData,
                                            List<Element<PartyDetails>> respondents,
                                            Element<PartyDetails> party,
                                            RespondentSolicitorEvents event) {
        Response buildResponseForRespondent = party.getValue().getResponse();
        switch (event) {
            case CONSENT:
                Consent respondentConsentToApplication = caseData.getRespondentConsentToApplication();
                respondentConsentToApplication = optimiseConsent(respondentConsentToApplication);
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .consent(respondentConsentToApplication).build();
                break;
            case KEEP_DETAILS_PRIVATE:
                buildResponseForRespondent = buildKeepYourDetailsPrivateResponse(caseData, buildResponseForRespondent);
                break;
            case CONFIRM_EDIT_CONTACT_DETAILS:
                buildResponseForRespondent = buildCitizenDetailsResponse(caseData, buildResponseForRespondent);
                break;
            case ATTENDING_THE_COURT:
                AttendToCourt attendToCourt = optimiseAttendingcourt(caseData.getRespondentAttendingTheCourt());
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .attendToCourt(attendToCourt)
                    .build();
                break;
            case MIAM:
                buildResponseForRespondent = buildMiamResponse(caseData, buildResponseForRespondent);
                break;
            case CURRENT_OR_PREVIOUS_PROCEEDINGS:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .currentOrPastProceedingsForChildren(caseData.getCurrentOrPastProceedingsForChildren())
                    .respondentExistingProceedings(YesNoDontKnow.yes.equals(caseData.getCurrentOrPastProceedingsForChildren())
                                                       ? caseData.getRespondentExistingProceedings() : null)
                    .build();
                break;
            case ALLEGATION_OF_HARM:
                buildResponseForRespondent = buildAoHResponse(caseData, buildResponseForRespondent);
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
        respondents.set(respondents.indexOf(party), element(party.getId(), amended));
    }

    private Response buildAbilityToParticipateResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .abilityToParticipate(SolicitorAbilityToParticipateInProceedings.builder()
                                      .factorsAffectingAbilityToParticipate(caseData.getAbilityToParticipateInProceedings()
                                                                                .getFactorsAffectingAbilityToParticipate())
                                      .provideDetailsForFactorsAffectingAbilityToParticipate(YesNoDontKnow.yes
                                                                                                 .equals(
                                                                                                     caseData.getAbilityToParticipateInProceedings()
                                                                                                         .getFactorsAffectingAbilityToParticipate())
                                                                                                 ? caseData.getAbilityToParticipateInProceedings()
                                          .getProvideDetailsForFactorsAffectingAbilityToParticipate()
                                                                                                 : null)
                                      .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildInternationalElementResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .resSolInternationalElements(ResSolInternationalElements.builder()
                                             .internationalElementChildInfo(SolicitorInternationalElement.builder()
                                                                                .reasonForChild(caseData.getInternationalElementChild()
                                                                                                    .getReasonForChild())
                                                                                .reasonForChildDetails(YesOrNo.No.equals(
                                                                                    caseData.getInternationalElementChild()
                                                                                        .getReasonForChild())
                                                                                                           ? null
                                                                                                           : caseData.getInternationalElementChild()
                                                                                    .getReasonForChildDetails()).build())
                                             .internationalElementParentInfo(SolicitorInternationalElement.builder()
                                                                                 .reasonForParent(caseData.getInternationalElementParent()
                                                                                                      .getReasonForParent())
                                                                                 .reasonForParentDetails(YesOrNo.No.equals(
                                                                                     caseData.getInternationalElementParent()
                                                                                         .getReasonForParent())
                                                                                                             ? null
                                                                                                             : caseData.getInternationalElementParent()
                                                                                     .getReasonForParentDetails()).build())
                                             .internationalElementJurisdictionInfo(SolicitorInternationalElement.builder()
                                                                                       .reasonForJurisdiction(
                                                                                           caseData.getInternationalElementJurisdiction()
                                                                                               .getReasonForJurisdiction())
                                                                                       .reasonForJurisdictionDetails(
                                                                                           YesOrNo.No.equals(
                                                                                               caseData
                                                                                                   .getInternationalElementJurisdiction()
                                                                                                   .getReasonForJurisdiction())
                                                                                               ? null
                                                                                               : caseData.getInternationalElementJurisdiction()
                                                                                               .getReasonForJurisdictionDetails()).build())
                                             .internationalElementRequestInfo(SolicitorInternationalElement.builder()
                                                                                  .requestToAuthority(caseData.getInternationalElementRequest()
                                                                                                          .getReasonForJurisdiction())
                                                                                  .requestToAuthorityDetails(
                                                                                      YesOrNo.No.equals(caseData
                                                                                                            .getInternationalElementRequest()
                                                                                                            .getRequestToAuthority())
                                                                                          ? null
                                                                                          : caseData.getInternationalElementRequest()
                                                                                          .getRequestToAuthorityDetails()).build())
                                             .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildAoHResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder()
                                                 .respAohYesOrNo(caseData.getRespondentAohYesNo())
                                                 .respAllegationsOfHarmInfo(caseData.getRespondentAllegationsOfHarm())
                                                 .respDomesticAbuseInfo(caseData.getRespondentDomesticAbuseBehaviour())
                                                 .respChildAbuseInfo(caseData.getRespondentChildAbuseBehaviour())
                                                 .respChildAbductionInfo(caseData.getRespondentChildAbduction())
                                                 .respOtherConcernsInfo(caseData.getRespondentOtherConcerns())
                                                 .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildMiamResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .solicitorMiam(SolicitorMiam.builder()
                               .respSolHaveYouAttendedMiam(caseData.getRespondentSolicitorHaveYouAttendedMiam())
                               .respSolWillingnessToAttendMiam(Miam.builder()
                                                                   .reasonNotAttendingMiam(
                                                                       YesOrNo.Yes.equals(caseData.getRespondentSolicitorWillingnessToAttendMiam()
                                                                                              .getWillingToAttendMiam()) ? null
                                                                           : caseData.getRespondentSolicitorWillingnessToAttendMiam()
                                                                           .getReasonNotAttendingMiam())
                                                                   .build())
                               .build())
            .build();
        return buildResponseForRespondent;
    }

    private Response buildCitizenDetailsResponse(CaseData caseData, Response buildResponseForRespondent) {
        CitizenDetails citizenDetails = caseData.getResSolConfirmEditContactDetails();
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .citizenDetails(buildResponseForRespondent.getCitizenDetails().toBuilder()
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

    private Response buildKeepYourDetailsPrivateResponse(CaseData caseData, Response buildResponseForRespondent) {
        List<ConfidentialityListEnum> confList = caseData.getKeepContactDetailsPrivateOther().getConfidentialityList();
        if (null != caseData.getKeepContactDetailsPrivateOther()
            && YesOrNo.No.equals(caseData.getKeepContactDetailsPrivateOther().getConfidentiality())) {
            confList = null;
        }

        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .solicitorKeepDetailsPriate(SolicitorKeepDetailsPrivate.builder()
                                            .respKeepDetailsPrivate(caseData.getKeepContactDetailsPrivate())
                                            .respKeepDetailsPrivateConfidentiality(KeepDetailsPrivate.builder()
                                                                                       .confidentiality(caseData
                                                                                                            .getKeepContactDetailsPrivateOther()
                                                                                                            .getConfidentiality())
                                                                                       .confidentialityList(confList)
                                                                                       .build())
                                            .build())
            .build();
        return buildResponseForRespondent;
    }

    private AttendToCourt optimiseAttendingcourt(AttendToCourt attendToCourt) {
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

    private Element<PartyDetails> findSolicitorRepresentedRespondents(CallbackRequest callbackRequest) {
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (callbackRequest.getEventId().isEmpty()) {
            throw new RespondentSolicitorException(TECH_ERROR);
        } else {
            String invokingSolicitor = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);

            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingSolicitor);

            if (solicitorRole.isPresent()) {
                solicitorRepresentedRespondent = caseData.getRespondents().get(solicitorRole.get().getIndex());
                if (solicitorRepresentedRespondent.getValue().getResponse() != null
                    && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getC7ResponseSubmitted())) {
                    throw new RespondentSolicitorException(
                        RESPONSE_ALREADY_SUBMITTED_ERROR);
                }
            }
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
        for (ConfidentialityListEnum confidentiality : caseData.getKeepContactDetailsPrivateOther()
            .getConfidentialityList()) {
            selectedList.append("<li>");
            selectedList.append(confidentiality.getDisplayedValue());
            selectedList.append("</li>");
        }
        selectedList.append("</ul>");

        Map<String, Object> keepDetailsPrivateList = new HashMap<>();
        keepDetailsPrivateList.put("confidentialListDetails", selectedList);
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
        } else {
            Document document = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C7_FINAL_DOCUMENT,
                false
            );
            caseDataUpdated.put("finalC7ResponseDoc", document);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> submitC7ResponseForActiveRespondent(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        Element<PartyDetails> representedRespondent = findSolicitorRepresentedRespondents(callbackRequest);

        if (representedRespondent != null && representedRespondent.getValue() != null && PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(
            caseData.getCaseTypeOfApplication())) {
            PartyDetails amended = representedRespondent.getValue().toBuilder()
                .response(representedRespondent.getValue().getResponse().toBuilder().c7ResponseSubmitted(Yes).build())
                .build();

            caseData.getRespondents().set(
                caseData.getRespondents().indexOf(representedRespondent),
                element(representedRespondent.getId(), amended)
            );

            updatedCaseData.put(RESPONDENTS, caseData.getRespondents());
        }
        return updatedCaseData;
    }

    public Map<String, Object> generateDraftDocumentsForRespondent(CallbackRequest callbackRequest, String authorisation) throws Exception {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        setActiveRespondent(callbackRequest, caseData);
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            SOLICITOR_C7_DRAFT_DOCUMENT,
            false
        );
        caseDataUpdated.put("draftC7ResponseDoc", document);

        if (Yes.equals(caseData.getRespondentAohYesNo())) {
            Document documentForC1A = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false
            );
            caseDataUpdated.put("draftC1ADoc", documentForC1A);
        }

        return caseDataUpdated;
    }

    private static void setActiveRespondent(CallbackRequest callbackRequest, CaseData caseData) {
        String invokingRespondent = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);
        if (!caseData.getRespondents().isEmpty()) {
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingRespondent);
            if (solicitorRole.isPresent() && caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                int activeRespondentIndex = solicitorRole.get().getIndex();
                Element<PartyDetails> respondingParty = caseData.getRespondents().get(activeRespondentIndex);
                Response response = respondingParty.getValue().getResponse();
                PartyDetails respondent = respondingParty.getValue().toBuilder().response(response.toBuilder().activeRespondent(
                    Yes).build()).build();
                Element<PartyDetails> updatedRepresentedRespondentElement = ElementUtils
                    .element(respondingParty.getId(), respondent);
                caseData.getRespondents().set(activeRespondentIndex, updatedRepresentedRespondentElement);
            }
        }
    }
}
