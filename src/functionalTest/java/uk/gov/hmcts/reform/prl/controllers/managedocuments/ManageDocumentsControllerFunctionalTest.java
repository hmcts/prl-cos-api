package uk.gov.hmcts.reform.prl.controllers.managedocuments;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.COURT_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_EXPERT_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DRUG_AND_ALCOHOL_TEST;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.HOME_OFFICE_DWP_RESPONSES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.JUDGE_NOTES_FROM_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LETTERS_OF_COMPLAINTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICES_OF_ACTING_DISCHARGE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PRIVACY_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PUBLIC_FUNDING_CERTIFICATES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESULTS_OF_HAIR_STRAND_BLOOD_TESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SAFEGUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SEC37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION7_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SIXTEEN_A_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_GUARDIANSHIP_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPIP_REFERRAL_REQUESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.STANDARD_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.WITNESS_AVAILABILITY;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.DETAILS_ERROR_MESSAGE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String MANAGE_DOCUMENT_REQUEST = "requests/manage-documents-request.json";
    //private static final String MANAGE_DOCUMENT_RESPONSE = "response/MangeDocument.json";

    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED = "requests/manage-documents-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenCaseId_whenAboutToStartEndPoint_thenRespWithDocumentCategories() throws Exception {

        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        //String response = ResourceLoader.loadJson(MANAGE_DOCUMENT_RESPONSE);
        //JSONObject jsObject = new JSONObject(response);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/about-to-start")
            .then()
            // .body( "data", equalTo("<{id=1687442033910904, createdDate=2022-03-15T15:02:33.880000, lastModifiedDate=2022-03-15T15:02:33.880000, c100ConfidentialityStatementDisclaimer=[confidentialityStatementUnderstood], buffChildAndApplicantRelations=null, childAndApplicantRelations=null, buffChildAndRespondentRelations=null, childAndRespondentRelations=null, buffChildAndOtherPeopleRelations=null, childAndOtherPeopleRelations=null, draftConsentOrderFile={document_url=http://dm-store:8080/documents/6bb61ec7-df31-4c14-b11d-48379307aa8c, document_binary_url=http://dm-store:8080/documents/6bb61ec7-df31-4c14-b11d-48379307aa8c/binary, document_filename=C100-Final-Document.docx, document_hash=null, category_id=null, document_creation_date=null}, applicationPermissionRequired=noNotRequired, doYouNeedAWithoutNoticeHearing=No, doYouRequireAHearingWithReducedNotice=No, areRespondentsAwareOfProceedings=No, childrenKnownToLocalAuthority=no, childrenSubjectOfChildProtectionPlan=no, applicantAttendedMiam=No, claimingExemptionMiam=Yes, familyMediatorMiam=null, otherProceedingsMiam=null, applicantConsentMiam=null, miamExemptionsChecklist=[other], miamDomesticViolenceChecklist=null, miamUrgencyReasonChecklist=null, miamChildProtectionConcernList=null, miamPreviousAttendanceChecklist=null, miamPreviousAttendanceChecklist1=null, miamOtherGroundsChecklist=miamOtherGroundsChecklistEnum_Value_1, miamOtherGroundsChecklist1=null, mediatorRegistrationNumber=null, familyMediatorServiceName=null, soleTraderName=null, miamCertificationDocumentUpload=null, mediatorRegistrationNumber1=null, familyMediatorServiceName1=null, soleTraderName1=null, miamCertificationDocumentUpload1=null, allegationsOfHarmYesNo=No, allegationsOfHarmDomesticAbuseYesNo=null, allegationsOfHarmChildAbductionYesNo=null, childAbductionReasons=null, previousAbductionThreats=null, previousAbductionThreatsDetails=null, childrenLocationNow=null, abductionPassportOfficeNotified=null, abductionChildHasPassport=null, abductionChildPassportPosession=null, abductionChildPassportPosessionOtherDetail=null, abductionPreviousPoliceInvolvement=null, abductionPreviousPoliceInvolvementDetails=null, allegationsOfHarmChildAbuseYesNo=null, allegationsOfHarmSubstanceAbuseYesNo=null, allegationsOfHarmOtherConcernsYesNo=null, ordersNonMolestation=null, ordersOccupation=null, ordersForcedMarriageProtection=null, ordersRestraining=null, ordersOtherInjunctive=null, ordersUndertakingInPlace=null, ordersNonMolestationDateIssued=null, ordersNonMolestationEndDate=null, ordersNonMolestationCurrent=null, ordersNonMolestationCourtName=null, ordersNonMolestationDocument=null, ordersOccupationDateIssued=null, ordersOccupationEndDate=null, ordersOccupationCurrent=null, ordersOccupationCourtName=null, ordersOccupationDocument=null, ordersForcedMarriageProtectionDateIssued=null, ordersForcedMarriageProtectionEndDate=null, ordersForcedMarriageProtectionCurrent=null, ordersForcedMarriageProtectionCourtName=null, ordersForcedMarriageProtectionDocument=null, ordersRestrainingDateIssued=null, ordersRestrainingEndDate=null, ordersRestrainingCurrent=null, ordersRestrainingCourtName=null, ordersRestrainingDocument=null, ordersOtherInjunctiveDateIssued=null, ordersOtherInjunctiveEndDate=null, ordersOtherInjunctiveCurrent=null, ordersOtherInjunctiveCourtName=null, ordersOtherInjunctiveDocument=null, ordersUndertakingInPlaceDateIssued=null, ordersUndertakingInPlaceEndDate=null, ordersUndertakingInPlaceCurrent=null, ordersUndertakingInPlaceCourtName=null, ordersUndertakingInPlaceDocument=null, allegationsOfHarmOtherConcerns=null, allegationsOfHarmOtherConcernsDetails=null, allegationsOfHarmOtherConcernsCourtActions=null, agreeChildUnsupervisedTime=null, agreeChildSupervisedTime=null, agreeChildOtherContact=null, physicalAbuseVictim=null, emotionalAbuseVictim=null, psychologicalAbuseVictim=null, sexualAbuseVictim=null, financialAbuseVictim=null, behaviours=null, newAllegationsOfHarmYesNo=null, newAllegationsOfHarmDomesticAbuseYesNo=null, newAllegationsOfHarmChildAbductionYesNo=null, newAllegationsOfHarmChildAbuseYesNo=null, newChildAbductionReasons=null, newPreviousAbductionThreats=null, newPreviousAbductionThreatsDetails=null, newChildrenLocationNow=null, newAbductionPassportOfficeNotified=null, newAbductionChildHasPassport=null, newAbductionPreviousPoliceInvolvement=null, newAbductionPreviousPoliceInvolvementDetails=null, newAllegationsOfHarmSubstanceAbuseYesNo=null, newAllegationsOfHarmSubstanceAbuseDetails=null, newOrdersNonMolestation=null, newOrdersOccupation=null, newOrdersForcedMarriageProtection=null, newOrdersRestraining=null, newOrdersOtherInjunctive=null, newOrdersUndertakingInPlace=null, newOrdersNonMolestationDateIssued=null, newOrdersNonMolestationEndDate=null, newOrdersNonMolestationCurrent=null, newOrdersNonMolestationCourtName=null, newOrdersNonMolestationDocument=null, newOrdersOccupationDateIssued=null, newOrdersOccupationEndDate=null, newOrdersOccupationCurrent=null, newOrdersOccupationCourtName=null, newOrdersOccupationDocument=null, newOrdersForcedMarriageProtectionDateIssued=null, newOrdersForcedMarriageProtectionEndDate=null, newOrdersForcedMarriageProtectionCurrent=null, newOrdersForcedMarriageProtectionCourtName=null, newOrdersForcedMarriageProtectionDocument=null, newOrdersRestrainingDateIssued=null, newOrdersRestrainingEndDate=null, newOrdersNonMolestationCaseNumber=null, newOrdersOccupationCaseNumber=null, newOrdersForcedMarriageProtectionCaseNumber=null, newOrdersRestrainingCaseNumber=null, newOrdersOtherInjunctiveCaseNumber=null, newOrdersUndertakingInPlaceCaseNumber=null, newOrdersRestrainingCurrent=null, newOrdersRestrainingCourtName=null, newOrdersRestrainingDocument=null, newOrdersOtherInjunctiveDateIssued=null, newOrdersOtherInjunctiveEndDate=null, newOrdersOtherInjunctiveCurrent=null, newOrdersOtherInjunctiveCourtName=null, newOrdersOtherInjunctiveDocument=null, newOrdersUndertakingInPlaceDateIssued=null, newOrdersUndertakingInPlaceEndDate=null, newOrdersUndertakingInPlaceCurrent=null, newOrdersUndertakingInPlaceCourtName=null, newOrdersUndertakingInPlaceDocument=null, newAllegationsOfHarmOtherConcerns=null, newAllegationsOfHarmOtherConcernsDetails=null, newAllegationsOfHarmOtherConcernsCourtActions=null, newAgreeChildUnsupervisedTime=null, newAgreeChildSupervisedTime=null, newAgreeChildOtherContact=null, childAbuses=null, childPassportDetails=null, domesticBehaviours=null, childPhysicalAbuse=null, childPsychologicalAbuse=null, childFinancialAbuse=null, childSexualAbuse=null, childEmotionalAbuse=null, allChildrenAreRiskPhysicalAbuse=null, allChildrenAreRiskPsychologicalAbuse=null, allChildrenAreRiskSexualAbuse=null, allChildrenAreRiskEmotionalAbuse=null, allChildrenAreRiskFinancialAbuse=null, whichChildrenAreRiskPhysicalAbuse=null, whichChildrenAreRiskPsychologicalAbuse=null, whichChildrenAreRiskSexualAbuse=null, whichChildrenAreRiskEmotionalAbuse=null, whichChildrenAreRiskFinancialAbuse=null, previousOrOngoingProceedingsForChildren=no, isWelshNeeded=null, welshNeeds=null, isInterpreterNeeded=null, interpreterNeeds=null, isDisabilityPresent=null, adjustmentsRequired=null, isSpecialArrangementsRequired=No, specialArrangementsRequired=null, isIntermediaryNeeded=null, reasonsForIntermediary=null, habitualResidentInOtherState=No, requestToForeignAuthority=No, litigationCapacityFactors=Give details of any factors affecting litigation capacity (Optional), litigationCapacityReferrals=Provide details of any referral to or assessment by the Adult Learning Disability team, and/or any adult health service, where known, together with the outcome (Optional), litigationCapacityOtherFactors=Yes, litigationCapacityOtherFactorsDetails=*Give Details (Optional), welshLanguageRequirement=Yes, welshLanguageRequirementApplication=english, languageRequirementApplicationNeedWelsh=Yes, typeOfApplicationTable={natureOfOrder=asdf, ordersApplyingFor=Prohibited Steps Order, typeOfChildArrangementsOrder=}, childListForSpecialGuardianship=null, courtName1=null, courtAddress=null, caseNumber=null, applicantName1=null, applicantReference=null, respondentReference=null, orderRespondentName=null, respondentDateOfBirth=null, respondentAddress=null, addressTheOrderAppliesTo=null, homeRights=null, applicantInstructions=null, powerOfArrest1=null, respondentDay1=null, respondentDay2=null, respondentStartTime=null, respondentEndTime=null, powerOfArrest2=null, whenTheyLeave=null, powerOfArrest3=null, moreDetails=null, powerOfArrest4=null, instructionRelating=null, powerOfArrest5=null, powerOfArrest6=null, dateOrderMade1=null, dateOrderEnds=null, datePlaceHearing=null, datePlaceHearingTime=null, dateOrderEndsTime=null, courtName2=null, ukPostcode2=null, orderNotice=null, hearingTimeEstimate=null, manageOrdersCourtName=null, manageOrdersCaseNo=null, manageOrdersApplicant=null, manageOrdersApplicantReference=null, manageOrdersRespondent=null, manageOrdersRespondentReference=null, manageOrdersRespondentDob=null, manageOrdersUnderTakingRepr=null, underTakingSolicitorCounsel=null, manageOrdersUnderTakingPerson=null, manageOrdersUnderTakingTerms=null, manageOrdersDateOfUnderTaking=null, underTakingDateExpiry=null, underTakingExpiryTime=null, underTakingFormSign=null, isTheOrderByConsent=null, judgeOrMagistrateTitle=null, manageOrdersDocumentToAmend=null, manageOrdersAmendedOrder=null, amendOrderDynamicList=null, manageOrdersFl402CourtName=null, manageOrdersFl402CourtAddress=null, manageOrdersFl402CaseNo=null, manageOrdersFl402Applicant=null, manageOrdersFl402ApplicantRef=null, manageOrdersDateOfhearing=null, dateOfHearingTime=null, dateOfHearingTimeEstimate=null, fl402HearingCourtname=null, fl402HearingCourtAddress=null, serveToRespondentOptions=null, servingRespondentsOptionsCA=null, recipientsOptions=null, otherParties=null, cafcassServedOptions=null, cafcassEmailId=null, cafcassCymruServedOptions=null, cafcassCymruEmail=null, deliveryByOptionsCA=null, servingRespondentsOptionsDA=null, serveOtherPartiesDA=null, deliveryByOptionsDA=null, currentOrderCreatedDateTime=null, isOnlyC47aOrderSelectedToServe=null, otherPeoplePresentInCaseFlag=null, serveToRespondentOptionsOnlyC47a=null, servingRespondentsOptionsCaOnlyC47a=null, recipientsOptionsOnlyC47a=null, otherPartiesOnlyC47a=null, deliveryByOptionsCaOnlyC47a=null, hearingsType=null, cafcassOfficeDetails=null, cafcassEmailAddress=null, otherEmailAddress=null, isCaseWithdrawn=null, recitalsOrPreamble=null, orderDirections=null, furtherDirectionsIfRequired=null, furtherInformationIfRequired=null, courtDeclares2=null, theRespondent2=null, childArrangementsOrdersToIssue=null, selectChildArrangementsOrder=null, manageOrdersCourtAddress=null, manageOrdersRespondentAddress=null, manageOrdersUnderTakingAddress=null, parentName=null, fl404CustomFields=null, fl404bCustomFields=null, childOption=null, serveOrderDynamicList=null, serveOrderAdditionalDocuments=null, serveOtherPartiesCA=null, emailInformationCA=null, postalInformationCA=null, emailInformationDA=null, postalInformationDA=null, withdrawnOrRefusedOrder=null, ordersNeedToBeServed=null, isTheOrderAboutChildren=null, isTheOrderAboutAllChildren=null, loggedInUserType=null, judgeDirectionsToAdminAmendOrder=null, amendOrderSelectCheckOptions=null, amendOrderSelectJudgeOrLa=null, nameOfJudgeAmendOrder=null, nameOfLaAmendOrder=null, nameOfJudgeToReviewOrder=null, nameOfLaToReviewOrder=null, previewUploadedOrder=null, orderUploadedAsDraftFlag=null, makeChangesToUploadedOrder=null, editedUploadOrderDoc=null, c21OrderOptions=null, typeOfC21Order=null, serveOtherPartiesCaOnlyC47a=null, emailInformationCaOnlyC47a=null, postalInformationCaOnlyC47a=null, hasJudgeProvidedHearingDetails=null, markedToServeEmailNotification=null, additionalOrderDocuments=null, sdoPartiesRaisedAbuseCollection=null, sdoHearingUrgentMustBeServedBy=null, sdoPositionStatementDeadlineDate=null, sdoMiamAttendingPerson=null, sdoAllegationsDeadlineDate=null, sdoWrittenResponseDeadlineDate=null, sdoInterpreterDialectRequired=null, sdoCafcassFileAndServe=null, sdoCafcassNextStepEditContent=null, sdoCafcassCymruFileAndServe=null, sdoCafcassCymruNextStepEditContent=null, sdoNewPartnersToCafcass=null, sdoNewPartnersToCafcassCymru=null, sdoCafcassCymruReportSentByDate=null, sdoLocalAuthorityReportSubmitByDate=null, sdoTransferApplicationCourtDynamicList=null, sdoWitnessStatementsDeadlineDate=null, sdoInstructionsFilingPartiesDynamicList=null, sdoHospitalRecordsDeadlineDate=null, sdoLetterFromGpDeadlineDate=null, sdoLetterFromSchoolDeadlineDate=null, sdoDisclosureOfPapersCaseNumbers=null, sdoFurtherDirectionDetails=null, sdoAfterSecondGatekeeping=null, sdoAddNewPreambleCollection=null, sdoNextStepsAfterGatekeeping=null, sdoPreamblesList=null, sdoHearingsAndNextStepsList=null, sdoCafcassOrCymruList=null, sdoLocalAuthorityList=null, sdoCourtList=null, sdoDocumentationAndEvidenceList=null, sdoFurtherList=null, sdoOtherList=null, sdoRightToAskCourt=null, sdoNextStepsAfterSecondGK=null, sdoNextStepsAllocationTo=null, sdoHearingUrgentCheckList=null, sdoHearingUrgentAnotherReason=null, sdoHearingUrgentCourtConsider=null, sdoHearingUrgentTimeShortened=null, sdoHearingNotNeeded=null, sdoParticipationDirections=null, sdoPositionStatementWritten=null, sdoJoiningInstructionsForRH=null, sdoHearingAllegationsMadeBy=null, sdoHearingCourtHasRequested=null, sdoHearingReportsAlsoSentTo=null, sdoHearingMaximumPages=null, sdoHearingHowManyWitnessEvidence=0, sdoDocsEvidenceWitnessEvidence=0, sdoUpdateContactDetails=null, sdoSection7EditContent=null, sdoSection7ImpactAnalysisOptions=null, sdoSection7FactsEditContent=null, sdoSection7daOccuredEditContent=null, sdoSection7ChildImpactAnalysis=null, sdoNameOfCouncil=null, sdoLocalAuthorityName=null, sdoLocalAuthorityTextArea=null, sdoTransferApplicationReason=null, sdoTransferApplicationSpecificReason=null, sdoCrossExaminationCourtHavingHeard=null, sdoCrossExaminationEx740=null, sdoCrossExaminationEx741=null, sdoCrossExaminationQualifiedLegal=null, sdoWitnessStatementsSentTo=null, sdoWitnessStatementsCopiesSentTo=null, sdoWitnessStatementsMaximumPages=null, sdoSpecifiedDocuments=null, sdoSpipAttendance=null, sdoMedicalDisclosureUploadedBy=null, sdoLetterFromGpUploadedBy=null, sdoLetterFromSchoolUploadedBy=null, sdoScheduleOfAllegationsOption=null, sdoParentWithCare=null, sdoPermissionHearingDirections=null, sdoPermissionHearingDetails=null, sdoSecondHearingDetails=null, sdoNextStepJudgeName=null, sdoAllocateOrReserveJudge=null, sdoAllocateOrReserveJudgeName=null, sdoUrgentHearingDetails=null, sdoFhdraHearingDetails=null, sdoPositionStatementOtherCheckDetails=null, sdoPositionStatementOtherDetails=null, sdoMiamOtherCheckDetails=null, sdoMiamOtherDetails=null, sdoDraHearingDetails=null, sdoSettlementHearingDetails=null, sdoFactFindingOtherCheck=null, sdoFactFindingOtherDetails=null, sdoInterpreterOtherDetailsCheck=null, sdoInterpreterOtherDetails=null, sdoCafcassFileAndServeCheck=null, sdoCafcassFileAndServeDetails=null, safeguardingCafcassCymruCheck=null, safeguardingCafcassCymruDetails=null, sdoPartyToProvideDetailsCheck=null, sdoPartyToProvideDetails=null, sdoNewPartnersToCafcassCheck=null, sdoNewPartnersToCafcassDetails=null, sdoSection7Check=null, sdoSection7CheckDetails=null, sdoLocalAuthorityCheck=null, sdoLocalAuthorityDetails=null, sdoTransferCourtDetailsCheck=null, sdoTransferCourtDetails=null, sdoCrossExaminationCourtCheck=null, sdoCrossExaminationCourtDetails=null, sdoWitnessStatementsCheck=null, sdoWitnessStatementsCheckDetails=null, sdoInstructionsFilingCheck=null, sdoInstructionsFilingDetails=null, sdoMedicalDiscApplicantName=null, sdoMedicalDiscRespondentName=null, sdoMedicalDiscFilingCheck=null, sdoMedicalDiscFilingDetails=null, sdoGpApplicantName=null, sdoGpRespondentName=null, sdoLetterFromDiscGpCheck=null, sdoLetterFromGpDetails=null, sdoLsApplicantName=null, sdoLsRespondentName=null, sdoLetterFromSchoolCheck=null, sdoLetterFromSchoolDetails=null, sdoScheduleOfAllegationsDetails=null, sdoDisClosureProceedingCheck=null, sdoDisClosureProceedingDetails=null, sdoCrossExaminationEditContent=null, sdoNamedJudgeFullName=null, dioPermissionHearingOn=null, dioUrgentFirstHearingDate=null, dioHearingUrgentMustBeServedBy=null, dioFhdraStartDateTime=null, dioFhdraCourtDynamicList=null, dioPositionStatementDeadlineDate=null, dioLocalAuthorityReportSubmitByDate=null, dioPreamblesList=null, dioHearingsAndNextStepsList=null, dioCafcassOrCymruList=null, dioLocalAuthorityList=null, dioCourtList=null, dioOtherList=null, dioRightToAskCourt=null, dioPartiesRaisedAbuseCollection=null, dioCaseReviewAtSecondGateKeeping=null, dioNextStepsAllocationTo=null, dioApplicationIsReservedTo=null, dioPermissionHearingTimeEstimate=null, dioPermissionHearingCourtDynamicList=null, dioPermissionHearingBeforeAList=null, dioHearingUrgentCheckList=null, dioFirstHearingUrgencyDetails=null, dioHearingUrgentCourtConsider=null, dioHearingUrgentTimeShortened=null, dioHearingUrgentDayShortened=null, dioHearingUrgentByWayOf=null, dioUrgentHearingRefusedCheckList=null, dioHearingUrgencyRefusedDetails=null, dioWithoutNoticeFirstHearingCheckList=null, dioWithoutNoticeFirstHearingDetails=null, dioWithoutNoticeHearingRefusedCheckList=null, dioWithoutNoticeHearingRefusedDetails=null, dioFhdraBeforeAList=null, dioFhdraByWayOf=null, dioParticipationDirections=null, dioPositionStatementWritten=null, dioMiamAttendingPerson=null, dioPersonWhoRequiresInterpreter=null, dioInterpreterDialectRequired=null, dioUpdateContactDetails=null, dioCafcassSafeguardingIssue=null, dioCafcassCymruSafeguardingIssue=null, dioTransferApplicationCourtDynamicList=null, dioTransferApplicationReason=null, dioTransferApplicationSpecificReason=null, dioLocalAuthorityName=null, dioDisclosureOfPapersCaseNumbers=null, dioParentWithCare=null, dioApplicationToApplyPermission=null, dioPermissionHearingDirections=null, dioCaseReviewHearingDetails=null, dioFhdraHearingDetails=null, dioPermissionHearingDetails=null, dioUrgentFirstHearingDetails=null, dioUrgentHearingDetails=null, dioWithoutNoticeHearingDetails=null, dioPositionStatementDetails=null, dioPositionStatementOtherCheckDetails=null, dioPositionStatementOtherDetails=null, dioMiamOtherCheckDetails=null, dioMiamOtherDetails=null, dioInterpreterOtherDetailsCheck=null, dioInterpreterOtherDetails=null, dioLocalAuthorityDetailsCheck=null, dioLocalAuthorityDetails=null, dioTransferCourtDetailsCheck=null, dioTransferCourtDetails=null, dioDisclosureOtherDetailsCheck=null, dioDisclosureOtherDetails=null, pd36qLetter=null, specialArrangementsLetter=null, additionalDocuments=null, additionalDocumentsList=null, sentDocumentPlaceHolder=null, noticeOfSafetySupportLetter=null, respondentNameForResponse=null, respondentConsentToApplication=null, respondentSolicitorHaveYouAttendedMiam=null, whatIsMiamPlaceHolder=null, helpMiamCostsExemptionsPlaceHolder=null, keepContactDetailsPrivate=null, confidentialListDetails=null, respondentAttendingTheCourt=null, internationalElementChild=null, respondentAohYesNo=null, respondentAllegationsOfHarm=null, respondentDomesticAbuseBehaviour=null, respondentChildAbuseBehaviour=null, respondentChildAbduction=null, respondentOtherConcerns=null, resSolConfirmEditContactDetails=null, draftC7ResponseDoc=null, finalC7ResponseDoc=null, draftC8ResponseDoc=null, finalC8ResponseDoc=null, respondentAgreeStatement=null, draftC1ADoc=null, finalC1AResponseDoc=null, currentOrPastProceedingsForChildren=null, respondentExistingProceedings=null, abilityToParticipateInProceedings=null, c100RebuildInternationalElements=null, c100RebuildReasonableAdjustments=null, c100RebuildTypeOfOrder=null, c100RebuildHearingWithoutNotice=null, c100RebuildHearingUrgency=null, c100RebuildOtherProceedings=null, c100RebuildReturnUrl=null, c100RebuildMaim=null, c100RebuildChildDetails=null, c100RebuildApplicantDetails=null, c100RebuildOtherChildrenDetails=null, c100RebuildRespondentDetails=null, c100RebuildOtherPersonsDetails=null, c100RebuildSafetyConcerns=null, c100RebuildScreeningQuestions=null, c100RebuildHelpWithFeesDetails=null, c100RebuildStatementOfTruth=null, helpWithFeesReferenceNumber=null, c100RebuildChildPostCode=null, c100RebuildConsentOrderDetails=null, whenReportsMustBeFiled=null, cafcassOrCymruNeedToProvideReport=null, cafcassCymruDocuments=null, orderEndsInvolvementOfCafcassOrCymru=null, doYouWantToServeOrder=null, whatDoWithOrder=null, additionalApplicationsApplyingFor=null, typeOfC2Application=null, additionalApplicantsList=null, temporaryC2Document=null, temporaryOtherApplicationsBundle=null, additionalApplicationFeesToPay=null, additionalApplicationsHelpWithFees=null, additionalApplicationsHelpWithFeesNumber=null, representedPartyType=null, isFl401CaseCreatedForWithOutNotice=null, fl401WithOutNoticeReasonToRespondent=null, additionalDirections=null, reducedNoticePeriodDetails=null, linkedCaCasesList=null, linkedCaCasesFurtherDetails=null, applicantNeedsFurtherInfoDetails=null, respondentNeedsFileStatementDetails=null, fl401ListOnNoticeHearingDetails=null, fl401ListOnNoticeDirectionsToAdmin=null, fl401LonOrderCompleteToServe=null, fl401ListOnNoticeDocument=null, soaServeToRespondentOptions=null, soaServingRespondentsOptionsCA=null, soaServingRespondentsOptionsDA=null, soaCitizenServingRespondentsOptionsCA=null, soaCitizenServingRespondentsOptionsDA=null, soaOtherParties=null, soaCafcassServedOptions=null, soaCafcassEmailId=null, soaCafcassCymruServedOptions=null, soaCafcassCymruEmail=null, proceedToServing=null, soaApplicantsList=null, coverPageAddress=null, coverPagePartyName=null, soaOtherPeoplePresentInCaseFlag=null, soaRecipientsOptions=null, messageReplyDynamicList=null, respondToMessage=null, messageReplyTable=null, sendMessageObject=null, replyMessageObject=null, messages=null, reviewDocsDynamicList=null, reviewDecisionYesOrNo=null, docToBeReviewed=null, reviewDoc=null, citizenUploadDocListConfTab=null, citizenUploadedDocListDocTab=null, legalProfUploadDocListConfTab=null, legalProfUploadDocListDocTab=null, cafcassUploadDocListConfTab=null, cafcassUploadDocListDocTab=null, courtStaffUploadDocListConfTab=null, courtStaffUploadDocListDocTab=null, bulkScannedDocListConfTab=null, bulkScannedDocListDocTab=null, caseTypeOfApplication=C100, manageDocuments=[{id=07f234f6-9de2-40bd-ae89-d6800c877bc2, value={documentParty=null, documentCategories={value={code=null, label=null}, list_items=[{code=applicantApplication, label=Applicant Application}, {code=applicantC1AApplication, label=Applicant C1A Application}, {code=applicantC1AResponse, label=Applicant C1A Response}, {code=applicationsWithinProceedings, label=Applications within proceedings}, {code=MIAMCertificate, label=MIAM Certificate}, {code=previousOrdersSubmittedWithApplication, label=Prev orders submitted with application}, {code=respondentApplication, label=Respondent Application}, {code=respondentC1AApplication, label=Respondent C1A Application}, {code=respondentC1AResponse, label=Respondent C1A Response}, {code=applicationsFromOtherProceedings, label=Applications from other proceedings}, {code=noticeOfHearing, label=Notice of Hearing}, {code=courtBundle, label=Court Bundle}, {code=caseSummary, label=Case Summary}, {code=safeguardingLetter, label=Safeguarding letter}, {code=section7Report, label=Section 7 report}, {code=section37Report, label=Section 37 report}, {code=16aRiskAssessment, label=16a risk assessment}, {code=guardianReport, label=Guardian report}, {code=specialGuardianshipReport, label=Special guardianship report}, {code=otherDocs, label=Other documents}, {code=confidential, label=Confidential}, {code=emailsToCourtToRequestHearingsAdjourned, label=Emails to request hearings adjourned}, {code=publicFundingCertificates, label=Public funding certificates}, {code=noticesOfActingDischarge, label=Notices of acting/discharge}, {code=requestForFASFormsToBeChanged, label=Request for FAS forms to be changed}, {code=witnessAvailability, label=Witness availability}, {code=lettersOfComplaint, label=Letters of complaint}, {code=SPIPReferralRequests, label=SPIP referral requests}, {code=homeOfficeDWPResponses, label=Home office/ DWP responses}, {code=bulkScanQuarantine, label=Bulk scan uploaded}, {code=medicalReports, label=Medical reports}, {code=DNAReports_expertReport, label=DNA reports}, {code=resultsOfHairStrandBloodTests, label=Results of hair strand/blood tests}, {code=policeDisclosures, label=Police disclosures}, {code=medicalRecords, label=Medical Records}, {code=drugAndAlcoholTest(toxicology), label=Drug and alcohol test (toxicology)}, {code=policeReport, label=Police report}, {code=sec37Report, label=Section 37 report}, {code=ordersSubmittedWithApplication, label=Orders Submitted with Application}, {code=approvedOrders, label=Approved orders}, {code=standardDirectionsOrder, label=Standard directions order}, {code=transcriptsOfJudgements, label=Transcripts of judgements}, {code=magistratesFactsAndReasons, label=Magistrates facts and reasons}, {code=judgeNotesFromHearing, label=Judge notes from hearing}, {code=importantInfoAboutAddressAndContact, label=Important info about address and contact}, {code=DNAReports_otherDocs, label=DNA reports}, {code=privacyNotice, label=Privacy Notice}, {code=specialMeasures, label=Special Measures}, {code=anyOtherDoc, label=Any Other Documents}, {code=positionStatements, label=Position statements}, {code=applicantStatements, label=Applicant's statements}, {code=respondentStatements, label=Respondent's statements}, {code=otherWitnessStatements, label=Other witness Statements}]}, document=null, documentDetails=null, documentRestrictCheckbox=null}}]}>"))
            // .body( "data", equalTo(JSON.parse(response))
            .body("data.manageDocuments[0].value.documentParty", equalTo(null),
                  "data.manageDocuments[0].value.document", equalTo(null),
                  "data.manageDocuments[0].value.documentDetails", equalTo(null),
                  "data.manageDocuments[0].value.documentRestrictCheckbox", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.code", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.label",equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.list_items[0].code", equalTo(APPLICANT_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[1].code", equalTo(APPLICANT_C1A_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[2].code", equalTo(APPLICANT_C1A_RESPONSE),
                  "data.manageDocuments[0].value.documentCategories.list_items[3].code", equalTo(APPLICATIONS_WITHIN_PROCEEDINGS),
                  "data.manageDocuments[0].value.documentCategories.list_items[4].code", equalTo(MIAM_CERTIFICATE),
                  "data.manageDocuments[0].value.documentCategories.list_items[5].code", equalTo(PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[6].code", equalTo(RESPONDENT_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[7].code", equalTo(RESPONDENT_C1A_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[8].code", equalTo(RESPONDENT_C1A_RESPONSE),
                  "data.manageDocuments[0].value.documentCategories.list_items[9].code", equalTo(APPLICATIONS_FROM_OTHER_PROCEEDINGS),
                  "data.manageDocuments[0].value.documentCategories.list_items[10].code", equalTo(NOTICE_OF_HEARING),
                  "data.manageDocuments[0].value.documentCategories.list_items[11].code", equalTo(COURT_BUNDLE),
                  "data.manageDocuments[0].value.documentCategories.list_items[12].code", equalTo(CASE_SUMMARY),
                  "data.manageDocuments[0].value.documentCategories.list_items[13].code", equalTo(SAFEGUARDING_LETTER),
                  "data.manageDocuments[0].value.documentCategories.list_items[14].code", equalTo(SECTION7_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[15].code", equalTo(SECTION_37_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[16].code", equalTo(SIXTEEN_A_RISK_ASSESSMENT),
                  "data.manageDocuments[0].value.documentCategories.list_items[17].code", equalTo(GUARDIAN_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[18].code", equalTo(SPECIAL_GUARDIANSHIP_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[19].code", equalTo(OTHER_DOCS),
                  "data.manageDocuments[0].value.documentCategories.list_items[20].code", equalTo(CONFIDENTIAL),
                  "data.manageDocuments[0].value.documentCategories.list_items[21].code", equalTo(EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED),
                  "data.manageDocuments[0].value.documentCategories.list_items[22].code", equalTo(PUBLIC_FUNDING_CERTIFICATES),
                  "data.manageDocuments[0].value.documentCategories.list_items[23].code", equalTo(NOTICES_OF_ACTING_DISCHARGE),
                  "data.manageDocuments[0].value.documentCategories.list_items[24].code", equalTo(REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED),
                  "data.manageDocuments[0].value.documentCategories.list_items[25].code", equalTo(WITNESS_AVAILABILITY),
                  "data.manageDocuments[0].value.documentCategories.list_items[26].code", equalTo(LETTERS_OF_COMPLAINTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[27].code", equalTo(SPIP_REFERRAL_REQUESTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[28].code", equalTo(HOME_OFFICE_DWP_RESPONSES),
                  "data.manageDocuments[0].value.documentCategories.list_items[29].code", equalTo("bulkScanQuarantine"),
                  "data.manageDocuments[0].value.documentCategories.list_items[30].code", equalTo(MEDICAL_REPORTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[31].code", equalTo(DNA_REPORTS_EXPERT_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[32].code", equalTo(RESULTS_OF_HAIR_STRAND_BLOOD_TESTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[33].code", equalTo(POLICE_DISCLOSURES),
                  "data.manageDocuments[0].value.documentCategories.list_items[34].code", equalTo(MEDICAL_RECORDS),
                  "data.manageDocuments[0].value.documentCategories.list_items[35].code", equalTo(DRUG_AND_ALCOHOL_TEST),
                  "data.manageDocuments[0].value.documentCategories.list_items[36].code", equalTo(POLICE_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[37].code", equalTo(SEC37_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[38].code", equalTo(ORDERS_SUBMITTED_WITH_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[39].code", equalTo(APPROVED_ORDERS),
                  "data.manageDocuments[0].value.documentCategories.list_items[40].code", equalTo(STANDARD_DIRECTIONS_ORDER),
                  "data.manageDocuments[0].value.documentCategories.list_items[41].code", equalTo(TRANSCRIPTS_OF_JUDGEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[42].code", equalTo(MAGISTRATES_FACTS_AND_REASONS),
                  "data.manageDocuments[0].value.documentCategories.list_items[43].code", equalTo(JUDGE_NOTES_FROM_HEARING),
                  "data.manageDocuments[0].value.documentCategories.list_items[44].code", equalTo(IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT),
                  "data.manageDocuments[0].value.documentCategories.list_items[45].code", equalTo(DNA_REPORTS_OTHER_DOCS),
                  "data.manageDocuments[0].value.documentCategories.list_items[46].code", equalTo(PRIVACY_NOTICE),
                  "data.manageDocuments[0].value.documentCategories.list_items[47].code", equalTo(SPECIAL_MEASURES),
                  "data.manageDocuments[0].value.documentCategories.list_items[48].code", equalTo(ANY_OTHER_DOC),
                  "data.manageDocuments[0].value.documentCategories.list_items[49].code", equalTo(POSITION_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[50].code", equalTo(APPLICANT_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[51].code", equalTo(RESPONDENT_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[52].code", equalTo(OTHER_WITNESS_STATEMENTS)


            )
            .assertThat().statusCode(200);

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsEndPoint_thenRespWithCopiedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200);
    }

    // ignoring this as managedocument event is working in demo probabaly we need to update the json here
    public void givenCaseId_whenCopy_manage_docsEndPoint_thenRespWithCopiedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsMid_thenCheckDocumentField_WhenNotRestricted() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED);
        AboutToStartOrSubmitCallbackResponse response =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs-mid")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(0,response.getErrors().size());

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsMid_thenCheckDocumentField_WhenRestricted() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        AboutToStartOrSubmitCallbackResponse response =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs-mid")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1,response.getErrors().size());
        Assert.assertEquals(DETAILS_ERROR_MESSAGE,response.getErrors().get(0));
    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);


        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("legalProfQuarantineDocsList");

        Assert.assertNotNull(legalProfQuarantineDocsList);
        Assert.assertEquals(1,legalProfQuarantineDocsList.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(legalProfUploadDocListDocTab);
        Assert.assertEquals(1,legalProfUploadDocListDocTab.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("cafcassQuarantineDocsList");

        Assert.assertNotNull(cafcassQuarantineDocsList);
        Assert.assertEquals(1,cafcassQuarantineDocsList.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("cafcassUploadDocListDocTab");

        Assert.assertNotNull(cafcassUploadDocListDocTab);
        Assert.assertEquals(1,cafcassUploadDocListDocTab.size());

    }


}
