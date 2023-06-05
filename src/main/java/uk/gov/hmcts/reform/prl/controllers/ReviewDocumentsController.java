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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderElements;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewDocumentsController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ManageOrderService manageOrderService;

    private final TaskListRenderElements taskListRenderElements;

    public static final String ORDERS_NEED_TO_BE_SERVED = "ordersNeedToBeServed";

    @PostMapping(path = "/review-documents/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (null != caseData.getLegalProfQuarantineDocsList()) {
            dynamicListElements.addAll(caseData.getLegalProfQuarantineDocsList().stream()
                .map(element -> DynamicListElement.builder().code(element.getId().toString())
                    .label(element.getValue().getDocument().getDocumentFileName())
                    .build()).collect(Collectors.toList()));
        }
        if (null != caseData.getCitizenUploadQuarantineDocsList()) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarantineDocsList().stream()
                .map(element -> DynamicListElement.builder().code(element.getId().toString())
                    .label(element.getValue().getCitizenDocument().getDocumentFileName())
                    .build()).collect(Collectors.toList()));
        }

        if (dynamicListElements.size() == 0) {
            errors = List.of("No documents to review");
        }
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        caseDataUpdated.put("reviewDocsDynamicList", DynamicList.builder().listItems(dynamicListElements).build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).errors(errors).build();
    }

    @PostMapping(path = "/review-documents/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList() && null != caseData.getReviewDocuments()
            .getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
            log.info("** uuid ** {}", uuid);
            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = Optional.empty();
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                quarantineLegalDocElement = caseData.getLegalProfQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }
            Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = Optional.empty();
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }

            if (quarantineLegalDocElement.isPresent()) {
                QuarantineLegalDoc doc = quarantineLegalDocElement.get().getValue();
                log.info("** QuarantineLegalDoc ** {}", doc);

                String doctobereviewed = String
                    .join(format("<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 "Legal professional"),
                          format("<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 doc.getCategory()),
                          format("<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                         doc.getNotes()), "<br/>");
                caseDataUpdated.put("docToBeReviewed", doctobereviewed);
                caseDataUpdated.put("reviewDoc", doc.getDocument());
                log.info("** review doc ** {}", doc.getDocument());
            } else if (quarantineCitizenDocElement.isPresent()) {
                UploadedDocuments doc = quarantineCitizenDocElement.get().getValue();
                String doctobereviewed = String
                    .join(format("<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 doc.getPartyName()),
                          format("<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 doc.getDocumentType()),
                          format("<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 " "));
                caseDataUpdated.put("docToBeReviewed", doctobereviewed);
                caseDataUpdated.put("reviewDoc", doc.getCitizenDocument());
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/review-documents/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
        if (YesNoDontKnow.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = caseData.getLegalProfQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineLegalDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getLegalProfQuarantineDocsList()
                        .remove(caseData.getLegalProfQuarantineDocsList().indexOf(quarantineLegalDocElement.get()));
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().add(docDetails);
                        caseDataUpdated.put("legalProfUploadDocListConfTab", caseData.getReviewDocuments().getLegalProfUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put("legalProfUploadDocListConfTab", List.of(docDetails));
                    }
                }
            }
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarantineDocsList()
                        .remove(caseData.getCitizenUploadQuarantineDocsList().indexOf(quarantineCitizenDocElement.get()));
                    if (null != caseData.getReviewDocuments().getCitizenUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getCitizenUploadDocListConfTab().add(docDetails);
                        caseDataUpdated.put("citizenUploadDocListConfTab", caseData.getReviewDocuments().getCitizenUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put("citizenUploadDocListConfTab", List.of(docDetails));
                    }
                }
            }

            log.info("*** legal prof docs dtab ** {}", caseDataUpdated.get("legalProfUploadDocListDocTab"));

            log.info("*** cit docs dtab ** {}", caseDataUpdated.get("citizenUploadedDocListDocTab"));
        } else if (YesNoDontKnow.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = caseData.getLegalProfQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineLegalDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getLegalProfQuarantineDocsList()
                        .remove(caseData.getLegalProfQuarantineDocsList().indexOf(quarantineLegalDocElement.get()));
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().add(docDetails);
                        caseDataUpdated.put("legalProfUploadDocListDocTab", caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());
                    } else {
                        caseDataUpdated.put("legalProfUploadDocListDocTab", List.of(docDetails));
                    }
                }
            }
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarantineDocsList()
                        .remove(caseData.getCitizenUploadQuarantineDocsList().indexOf(quarantineCitizenDocElement.get()));
                    if (null != caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()) {
                        caseData.getReviewDocuments().getCitizenUploadedDocListDocTab().add(docDetails);
                        caseDataUpdated.put("citizenUploadedDocListDocTab", caseData.getReviewDocuments().getCitizenUploadedDocListDocTab());
                    } else {
                        caseDataUpdated.put("citizenUploadedDocListDocTab", List.of(docDetails));
                    }
                }
            }
            log.info("*** legal prof docs dtab ** {}", caseDataUpdated.get("legalProfUploadDocListDocTab"));
            log.info("*** cit docs dtab ** {}", caseDataUpdated.get("citizenUploadedDocListDocTab"));
        }
        log.info("*** Legal prof docs q ** {}", caseData.getLegalProfQuarantineDocsList());
        log.info("***citizen docs q ** {}", caseData.getCitizenUploadQuarantineDocsList());
        caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        caseDataUpdated.put("citizenUploadQuarantineDocsList", caseData.getCitizenUploadQuarantineDocsList());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
