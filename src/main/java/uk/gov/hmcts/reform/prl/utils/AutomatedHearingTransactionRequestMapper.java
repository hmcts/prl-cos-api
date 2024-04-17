package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caselink.AutomatedHearingCaseLink;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.complextypes.AutomatedHearingCaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingAttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingDataApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingDataRespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingPartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingCaseData mappingAutomatedHearingTransactionRequest(CaseData caseData, HearingData hearingData) {
        ObjectMapper objectMappers = new ObjectMapper();
        objectMappers.registerModule(new JavaTimeModule());
        try {
            List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails = new ArrayList<>();

            List<Element<AutomatedHearingPartyDetails>> respondentsAutomatedHearingpartyDetails = new ArrayList<>();
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData.getApplicants().forEach(applicants -> getPartyDetailsForRequest(
                    applicants,
                    applicantsAutomatedHearingpartyDetails
                ));
                caseData.getRespondents().forEach(respondents -> getPartyDetailsForRequest(
                    respondents,
                    respondentsAutomatedHearingpartyDetails
                ));
            } else {
                getPartyDetailsForRequest(ElementUtils.element(caseData.getApplicantsFL401().getPartyId(),
                                                               caseData.getApplicantsFL401()), applicantsAutomatedHearingpartyDetails);
                getPartyDetailsForRequest(ElementUtils.element(caseData.getRespondentsFL401().getPartyId(),
                                                               caseData.getRespondentsFL401()), respondentsAutomatedHearingpartyDetails);
            }

            List<Element<AutomatedHearingPartyDetails>> automatedHearingOtherPartyInTheCaseRevised = getAutomatedHearingOtherPartyDetails(
                caseData);

            List<CaseLinksElement<AutomatedHearingCaseLink>> automatedHearingCaseLinks = new ArrayList<>();
            List<CaseLinksElement<CaseLink>> caseLinksList = caseData.getCaseLinks();
            if (caseLinksList != null) {
                caseLinksList.forEach(caseLink -> getCaseLinksDetails(caseLink, automatedHearingCaseLinks));
            }

            AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder()
                .id(caseData.getId())
                .hearingData(getHearingDataForRequest(hearingData))
                .taskListVersion(caseData.getTaskListVersion())
                .createdDate(caseData.getCreatedDate())
                .familymanCaseNumber(caseData.getFamilymanCaseNumber())
                .dateSubmitted(caseData.getDateSubmitted())
                .caseTypeOfApplication(caseData.getCaseTypeOfApplication())
                .applicants(applicantsAutomatedHearingpartyDetails)
                .respondents(respondentsAutomatedHearingpartyDetails)
                .otherPartyInTheCaseRevised(automatedHearingOtherPartyInTheCaseRevised)
                .applicantSolicitorEmailAddress(caseData.getApplicantSolicitorEmailAddress())
                .solicitorName(caseData.getSolicitorName())
                .courtName(caseData.getCourtName())
                .caseManagementLocation(isNull(caseData.getCaseManagementLocation())
                                            ? AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith().build() :
                                            AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith()
                                                .region(caseData.getCaseManagementLocation().getRegion())
                                                .baseLocation(caseData.getCaseManagementLocation().getBaseLocation())
                                                .build())
                .caseLinks(automatedHearingCaseLinks)
                .applicantCaseName(caseData.getApplicantCaseName())
                .allPartyFlags(caseData.getAllPartyFlags())
                .attendHearing(isNull(caseData.getAttendHearing()) ? AutomatedHearingAttendHearing.automatedHearingAttendHearingWith().build() :
                                   AutomatedHearingAttendHearing.automatedHearingAttendHearingWith()
                                       .isWelshNeeded(YesOrNo.Yes.equals(caseData.getAttendHearing().getIsWelshNeeded()))
                                       .isInterpreterNeeded(YesOrNo.Yes.equals(caseData.getAttendHearing().getIsInterpreterNeeded()))
                                       .build())
                .issueDate(caseData.getIssueDate())
                .build();
            String automatedHearingCaseDataJson = objectMappers.writerWithDefaultPrettyPrinter().writeValueAsString(
                automatedHearingCaseData);
            log.info("Automated Hearing Request Mapper: AutomatedHearingCaseData: {}", automatedHearingCaseDataJson);
            return automatedHearingCaseData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Element<AutomatedHearingPartyDetails>> getAutomatedHearingOtherPartyDetails(CaseData caseData) {
        List<Element<AutomatedHearingPartyDetails>> automatedHearingOtherPartyInTheCaseRevised = new ArrayList<>();
        List<Element<PartyDetails>> otherPartyInTheCaseRevisedList = caseData.getOtherPartyInTheCaseRevised();
        if (otherPartyInTheCaseRevisedList != null) {
            otherPartyInTheCaseRevisedList.forEach(otherParty -> getPartyDetailsForRequest(
                otherParty,
                automatedHearingOtherPartyInTheCaseRevised
            ));
        }
        return automatedHearingOtherPartyInTheCaseRevised;
    }

    private static AutomatedHearingData getHearingDataForRequest(HearingData hearingData) {
        if (hearingData != null) {
            return AutomatedHearingData.automatedHearingDataBuilder()
                .hearingTypes(hearingData.getHearingTypes())
                .hearingId(hearingData.getHearingId())
                .confirmedHearingDates(hearingData.getConfirmedHearingDates())
                .hearingChannels(hearingData.getHearingChannels())
                .hearingVideoChannels(hearingData.getHearingVideoChannels())
                .hearingTelephoneChannels(hearingData.getHearingTelephoneChannels())
                .courtList(hearingData.getCourtList())
                .localAuthorityHearingChannel(hearingData.getLocalAuthorityHearingChannel())
                .hearingListedLinkedCases(hearingData.getHearingListedLinkedCases())
                .applicantSolicitorHearingChannel(hearingData.getApplicantSolicitorHearingChannel())
                .respondentHearingChannel(hearingData.getRespondentHearingChannel())
                .respondentSolicitorHearingChannel(hearingData.getRespondentSolicitorHearingChannel())
                .cafcassHearingChannel(hearingData.getCafcassHearingChannel())
                .cafcassCymruHearingChannel(hearingData.getCafcassCymruHearingChannel())
                .applicantHearingChannel(hearingData.getApplicantHearingChannel())
                .hearingDateConfirmOptionEnum(hearingData.getHearingDateConfirmOptionEnum())
                .additionalHearingDetails(hearingData.getAdditionalHearingDetails())
                .instructionsForRemoteHearing(hearingData.getInstructionsForRemoteHearing())
                .hearingDateTimes(hearingData.getHearingDateTimes())
                .hearingEstimatedHours(hearingData.getHearingEstimatedHours())
                .hearingEstimatedMinutes(hearingData.getHearingEstimatedMinutes())
                .hearingEstimatedDays(hearingData.getHearingEstimatedDays())
                .allPartiesAttendHearingSameWayYesOrNo(hearingData.getAllPartiesAttendHearingSameWayYesOrNo())
                .hearingAuthority(hearingData.getHearingAuthority())
                .hearingChannelsEnum(hearingData.getHearingChannelsEnum())
                .hearingJudgeNameAndEmail(hearingData.getHearingJudgeNameAndEmail())
                .hearingJudgePersonalCode(hearingData.getHearingJudgePersonalCode())
                .hearingJudgeLastName(hearingData.getHearingJudgeLastName())
                .hearingJudgeEmailAddress(hearingData.getHearingJudgeEmailAddress())
                .applicantName(hearingData.getApplicantName())
                .applicantSolicitor(hearingData.getApplicantSolicitor())
                .respondentName(hearingData.getRespondentName())
                .respondentSolicitor(hearingData.getRespondentSolicitor())
                .hearingSpecificDatesOptionsEnum(hearingData.getHearingSpecificDatesOptionsEnum())
                .firstDateOfTheHearing(hearingData.getFirstDateOfTheHearing())
                .hearingMustTakePlaceAtHour(hearingData.getHearingMustTakePlaceAtHour())
                .hearingMustTakePlaceAtMinute(hearingData.getHearingMustTakePlaceAtMinute())
                .earliestHearingDate(hearingData.getEarliestHearingDate())
                .latestHearingDate(hearingData.getLatestHearingDate())
                .hearingPriorityTypeEnum(hearingData.getHearingPriorityTypeEnum())
                .customDetails(hearingData.getCustomDetails())
                .isRenderingRequiredFlag(hearingData.getIsRenderingRequiredFlag())
                .fillingFormRenderingInfo(hearingData.getFillingFormRenderingInfo())
                .hearingdataFromHearingTab(hearingData.getHearingdataFromHearingTab())
                .hearingDataApplicantDetails(AutomatedHearingDataApplicantDetails.automatedHearingDataApplicantDetails()
                                                 .applicantHearingChannel1(hearingData.getApplicantHearingChannel1())
                                                 .applicantHearingChannel2(hearingData.getApplicantHearingChannel2())
                                                 .applicantHearingChannel3(hearingData.getApplicantHearingChannel3())
                                                 .applicantHearingChannel4(hearingData.getApplicantHearingChannel4())
                                                 .applicantHearingChannel5(hearingData.getApplicantHearingChannel5())
                                                 .applicantSolicitorHearingChannel1(hearingData.getApplicantSolicitorHearingChannel1())
                                                 .applicantSolicitorHearingChannel2(hearingData.getApplicantSolicitorHearingChannel2())
                                                 .applicantSolicitorHearingChannel3(hearingData.getApplicantSolicitorHearingChannel3())
                                                 .applicantSolicitorHearingChannel4(hearingData.getApplicantSolicitorHearingChannel4())
                                                 .applicantSolicitorHearingChannel5(hearingData.getApplicantSolicitorHearingChannel5())
                                                 .applicantName1(hearingData.getApplicantName1())
                                                 .applicantName2(hearingData.getApplicantName2())
                                                 .applicantName3(hearingData.getApplicantName3())
                                                 .applicantName4(hearingData.getApplicantName4())
                                                 .applicantName5(hearingData.getApplicantName5())
                                                 .applicantSolicitor1(hearingData.getApplicantSolicitor1())
                                                 .applicantSolicitor2(hearingData.getApplicantSolicitor2())
                                                 .applicantSolicitor3(hearingData.getApplicantSolicitor3())
                                                 .applicantSolicitor4(hearingData.getApplicantSolicitor4())
                                                 .applicantSolicitor5(hearingData.getApplicantSolicitor5())
                                                 .build())
                .hearingDataRespondentDetails(AutomatedHearingDataRespondentDetails.automatedHearingDataRespondentDetails()
                                                  .respondentHearingChannel1(hearingData.getRespondentHearingChannel1())
                                                  .respondentHearingChannel2(hearingData.getRespondentHearingChannel2())
                                                  .respondentHearingChannel3(hearingData.getRespondentHearingChannel3())
                                                  .respondentHearingChannel4(hearingData.getRespondentHearingChannel4())
                                                  .respondentHearingChannel5(hearingData.getRespondentHearingChannel5())
                                                  .respondentSolicitorHearingChannel1(hearingData.getRespondentSolicitorHearingChannel1())
                                                  .respondentSolicitorHearingChannel2(hearingData.getRespondentSolicitorHearingChannel2())
                                                  .respondentSolicitorHearingChannel3(hearingData.getRespondentSolicitorHearingChannel3())
                                                  .respondentSolicitorHearingChannel4(hearingData.getRespondentSolicitorHearingChannel4())
                                                  .respondentSolicitorHearingChannel5(hearingData.getRespondentSolicitorHearingChannel5())
                                                  .respondentName1(hearingData.getRespondentName1())
                                                  .respondentName2(hearingData.getRespondentName2())
                                                  .respondentName3(hearingData.getRespondentName3())
                                                  .respondentName4(hearingData.getRespondentName4())
                                                  .respondentName5(hearingData.getRespondentName5())
                                                  .respondentSolicitor1(hearingData.getRespondentSolicitor1())
                                                  .respondentSolicitor2(hearingData.getRespondentSolicitor2())
                                                  .respondentSolicitor3(hearingData.getRespondentSolicitor3())
                                                  .respondentSolicitor4(hearingData.getRespondentSolicitor4())
                                                  .respondentSolicitor5(hearingData.getRespondentSolicitor5())
                                                  .build())
                .isCafcassCymru(hearingData.getIsCafcassCymru())
                .additionalDetailsForHearingDateOptions(hearingData.getAdditionalDetailsForHearingDateOptions())
                .build();
        }
        return AutomatedHearingData.automatedHearingDataBuilder()
            .hearingDataApplicantDetails(AutomatedHearingDataApplicantDetails.automatedHearingDataApplicantDetails().build())
            .hearingDataRespondentDetails(AutomatedHearingDataRespondentDetails.automatedHearingDataRespondentDetails().build())
            .build();
    }

    private static void getPartyDetailsForRequest(Element<PartyDetails> party,
                                                  List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails) {
        PartyDetails partyDetails = party.getValue();
        AutomatedHearingPartyDetails automatedHearingPartyDetails = AutomatedHearingPartyDetails
            .automatedHearingPartyDetailsWith().build();
        if (partyDetails != null) {
            automatedHearingPartyDetails = AutomatedHearingPartyDetails
                .automatedHearingPartyDetailsWith()
                .firstName(partyDetails.getFirstName())
                .lastName(partyDetails.getLastName())
                .previousName(partyDetails.getPreviousName())
                .dateOfBirth(partyDetails.getDateOfBirth())
                .isDateOfBirthUnknown(partyDetails.getIsDateOfBirthUnknown())
                .otherGender(partyDetails.getOtherGender())
                .placeOfBirth(partyDetails.getPlaceOfBirth())
                .isAddressUnknown(partyDetails.getIsAddressUnknown())
                .addressLivedLessThan5YearsDetails(partyDetails.getAddressLivedLessThan5YearsDetails())
                .landline(partyDetails.getLandline())
                .relationshipToChildren(partyDetails.getRelationshipToChildren())
                .otherPersonRelationshipToChildren(partyDetails.getOtherPersonRelationshipToChildren())
                .solicitorOrg(partyDetails.getSolicitorOrg())
                .solicitorAddress(partyDetails.getSolicitorAddress())
                .dxNumber(partyDetails.getDxNumber())
                .solicitorReference(partyDetails.getSolicitorReference())
                .representativeFirstName(partyDetails.getRepresentativeFirstName())
                .representativeLastName(partyDetails.getRepresentativeLastName())
                .sendSignUpLink(partyDetails.getSendSignUpLink())
                .solicitorEmail(partyDetails.getSolicitorEmail())
                .phoneNumber(partyDetails.getPhoneNumber())
                .email(partyDetails.getEmail())
                .address(partyDetails.getAddress())
                .solicitorTelephone(partyDetails.getSolicitorTelephone())
                .caseTypeOfApplication(partyDetails.getCaseTypeOfApplication())
                .partyLevelFlag(partyDetails.getPartyLevelFlag())
                .partyId(partyDetails.getPartyId())
                .solicitorOrgUuid(partyDetails.getSolicitorOrgUuid())
                .solicitorPartyId(partyDetails.getSolicitorPartyId())
                .build();
        }

        Element<AutomatedHearingPartyDetails> autoElement = Element.<AutomatedHearingPartyDetails>builder()
            .id(party.getId())
            .value(automatedHearingPartyDetails)
            .build();
        applicantsAutomatedHearingpartyDetails.add(autoElement);
    }

    private static void getCaseLinksDetails(CaseLinksElement<CaseLink> caseLink,
                                            List<CaseLinksElement<AutomatedHearingCaseLink>> automatedHearingCaseLinksDetails) {
        String ids = caseLink.getId();
        CaseLink partyDetails = caseLink.getValue();
        AutomatedHearingCaseLink automatedHearingCaseLinkDetails = AutomatedHearingCaseLink
            .automatedHearingCaseLinkWith()
            .caseType(partyDetails.getCaseType())
            .build();

        CaseLinksElement<AutomatedHearingCaseLink> autoElement = CaseLinksElement.<AutomatedHearingCaseLink>builder()
            .id(ids)
            .value(automatedHearingCaseLinkDetails)
            .build();
        automatedHearingCaseLinksDetails.add(autoElement);
    }
}
