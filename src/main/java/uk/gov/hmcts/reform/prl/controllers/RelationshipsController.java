package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RelationshipsController {

    private final ObjectMapper objectMapper;

    @PostMapping(path = "/pre-populate-applicant-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "pre populates applicant and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateApplicantToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildrenAndApplicantRelation>> applicantChildRelationsList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData.getApplicants().forEach(eachApplicant ->
                caseData.getNewChildDetails().forEach(eachChild -> {
                    ChildrenAndApplicantRelation applicantChildRelations = ChildrenAndApplicantRelation.builder()
                            .childFullName(String.format(PrlAppsConstants.FORMAT, eachChild.getValue().getFirstName(),
                                    eachChild.getValue().getLastName()))
                            .childId(eachChild.getId().toString())
                            .applicantId(eachApplicant.getId().toString())
                            .applicantFullName(String.format(PrlAppsConstants.FORMAT, eachApplicant.getValue().getFirstName(),
                                    eachApplicant.getValue().getLastName())).build();
                    applicantChildRelationsList.add(Element.<ChildrenAndApplicantRelation>builder().value(applicantChildRelations).build());
                })
        );
        caseDataUpdated.put("buffChildAndApplicantRelations", applicantChildRelationsList);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/populate-applicant-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "populates applicant and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateApplicantToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("buffChildAndApplicantRelations", null);
        caseDataUpdated.put("childAndApplicantRelations", caseData.getRelations().getBuffChildAndApplicantRelations());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/pre-populate-respondent-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "pre populates respondent and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateRespondentToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildrenAndRespondentRelation>> applicantChildRelationsList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData.getRespondents().forEach(eachRespondent ->
                caseData.getNewChildDetails().forEach(eachChild -> {
                    ChildrenAndRespondentRelation applicantChildRelations = ChildrenAndRespondentRelation.builder()
                            .childFullName(String.format(PrlAppsConstants.FORMAT, eachChild.getValue().getFirstName(),
                                    eachChild.getValue().getLastName()))
                            .childId(eachChild.getId().toString())
                            .respondentId(eachRespondent.getId().toString())
                            .respondentFullName(String.format(PrlAppsConstants.FORMAT, eachRespondent.getValue().getFirstName(),
                                    eachRespondent.getValue().getLastName())).build();
                    applicantChildRelationsList.add(Element.<ChildrenAndRespondentRelation>builder().value(applicantChildRelations).build());
                })
        );
        caseDataUpdated.put("buffChildAndRespondentRelations", applicantChildRelationsList);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/populate-respondent-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "populates respondent and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateRespondentToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("buffChildAndRespondentRelations", null);
        caseDataUpdated.put("childAndRespondentRelations", caseData.getRelations().getBuffChildAndRespondentRelations());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/pre-populate-other-people-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "pre populates other people and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateOtherPeopleToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildrenAndOtherPeopleRelation>> otherPeopleChildRelationsList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData.getOtherPartyInTheCaseRevised().forEach(eachPeople ->
                caseData.getNewChildDetails().forEach(eachChild -> {
                    ChildrenAndOtherPeopleRelation otherPeopleChildRelations = ChildrenAndOtherPeopleRelation.builder()
                            .childFullName(String.format(PrlAppsConstants.FORMAT, eachChild.getValue().getFirstName(),
                                    eachChild.getValue().getLastName()))
                            .childId(eachChild.getId().toString())
                            .otherPeopleId(eachPeople.getId().toString())
                            .otherPeopleFullName(String.format(PrlAppsConstants.FORMAT, eachPeople.getValue().getFirstName(),
                                    eachPeople.getValue().getLastName())).build();
                    otherPeopleChildRelationsList.add(Element.<ChildrenAndOtherPeopleRelation>builder().value(otherPeopleChildRelations).build());
                })
        );
        caseDataUpdated.put(PrlAppsConstants.BUFF_CHILD_AND_OTHER_PEOPLE_RELATIONS, otherPeopleChildRelationsList);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/pre-populate-amend-other-people-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "pre populates amend other people and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse prePopulateAmendOtherPeopleToChildRelation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<Element<ChildrenAndOtherPeopleRelation>> otherPeopleChildRelationsList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<ChildrenAndOtherPeopleRelation>> existingOtherPeopleChildRelations = caseData.getRelations().getChildAndOtherPeopleRelations();
        caseData.getOtherPartyInTheCaseRevised().forEach(eachPeople ->
             caseData.getNewChildDetails().forEach(eachChild -> {
                 ChildrenAndOtherPeopleRelation existingRelation = getExistingChildrenAndOtherPeopleRelation(existingOtherPeopleChildRelations, eachPeople, eachChild);

                 ChildrenAndOtherPeopleRelation otherPeopleChildRelation = ChildrenAndOtherPeopleRelation.builder()
                     .childFullName(String.format(PrlAppsConstants.FORMAT, eachChild.getValue().getFirstName(),
                                                  eachChild.getValue().getLastName()))
                     .childId(eachChild.getId().toString())
                     .otherPeopleId(eachPeople.getId().toString())
                     .otherPeopleFullName(String.format(PrlAppsConstants.FORMAT, eachPeople.getValue().getFirstName(),
                                                        eachPeople.getValue().getLastName()))
                     .childAndOtherPeopleRelation(Objects.nonNull(existingRelation) ? existingRelation.getChildAndOtherPeopleRelation() : null)
                     .childLivesWith(Objects.nonNull(existingRelation) ? existingRelation.getChildLivesWith() : null)
                     .childAndOtherPeopleRelationOtherDetails(Objects.nonNull(existingRelation)
                                                                  ? existingRelation.getChildAndOtherPeopleRelationOtherDetails() : null)
                     .isChildLivesWithPersonConfidential(Objects.nonNull(existingRelation)
                                                             ? existingRelation.getIsChildLivesWithPersonConfidential() : null)
                     .build();
                 otherPeopleChildRelationsList.add(Element.<ChildrenAndOtherPeopleRelation>builder().value(otherPeopleChildRelation).build());
             })
        );
        caseDataUpdated.put(PrlAppsConstants.BUFF_CHILD_AND_OTHER_PEOPLE_RELATIONS, otherPeopleChildRelationsList);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private static ChildrenAndOtherPeopleRelation getExistingChildrenAndOtherPeopleRelation(
        List<Element<ChildrenAndOtherPeopleRelation>> existingOtherPeopleChildRelations,
        Element<PartyDetails> eachPeople,
        Element<ChildDetailsRevised> eachChild
    ) {
        return CollectionUtils.isNotEmpty(existingOtherPeopleChildRelations)
            ? existingOtherPeopleChildRelations.stream().filter(childrenAndOtherPeopleRelationElement ->
               StringUtils.equals(childrenAndOtherPeopleRelationElement.getValue().getOtherPeopleId(),
                                  String.valueOf(eachPeople.getId()))
               && StringUtils.equals(childrenAndOtherPeopleRelationElement.getValue().getChildId(),
                                     String.valueOf(eachChild.getId())))
            .findFirst().map(Element::getValue).orElse(null) : null;
    }

    @PostMapping(path = "/populate-other-people-to-child-relation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "populates other people and child relations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateOtherPeopleToChildRelation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<ChildrenAndOtherPeopleRelation>> buffChildAndOtherPeopleRelations = caseData.getRelations()
                                                                                            .getBuffChildAndOtherPeopleRelations();
        List<Element<ChildrenAndOtherPeopleRelation>> updatedChildAndOtherPeopleRelations = new ArrayList<>();
        buffChildAndOtherPeopleRelations.stream().forEach(relationElement -> {
            ChildrenAndOtherPeopleRelation relation = relationElement.getValue();
            updatedChildAndOtherPeopleRelations.add(Element.<ChildrenAndOtherPeopleRelation>builder()
                .value(relation.toBuilder()
                           .childAndOtherPeopleRelationOtherDetails(
                               StringUtils.equals(relation.getChildAndOtherPeopleRelation().getId(), RelationshipsEnum.other.getId())
                                   ? relation.getChildAndOtherPeopleRelationOtherDetails() : null)
                           .isChildLivesWithPersonConfidential(
                               relation.getChildLivesWith().equals(YesOrNo.Yes) ? relation.getIsChildLivesWithPersonConfidential() : null)
                           .build())
                .id(relationElement.getId())
                .build());
        });
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(PrlAppsConstants.BUFF_CHILD_AND_OTHER_PEOPLE_RELATIONS, null);
        caseDataUpdated.put("childAndOtherPeopleRelations", updatedChildAndOtherPeopleRelations);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}

