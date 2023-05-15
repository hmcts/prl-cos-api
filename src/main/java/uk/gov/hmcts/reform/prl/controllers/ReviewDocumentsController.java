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
import uk.gov.hmcts.reform.prl.models.complextypes.QuarentineLegalDoc;
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
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (null != caseData.getLegalProfQuarentineDocsList()) {
            dynamicListElements.addAll(caseData.getLegalProfQuarentineDocsList().stream()
                .map(element -> DynamicListElement.builder().code(element.getId().toString())
                    .label(element.getValue().getDocument().getDocumentFileName())
                    .build()).collect(Collectors.toList()));
        }
        if (null != caseData.getCitizenUploadQuarentineDocsList()) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarentineDocsList().stream()
                .map(element -> DynamicListElement.builder().code(element.getId().toString())
                    .label(element.getValue().getCitizenDocument().getDocumentFileName())
                    .build()).collect(Collectors.toList()));
        }

        if (dynamicListElements.size() == 0) {
            errors = List.of("No documents to review");
        }
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
            Optional<Element<QuarentineLegalDoc>> quarentineLegalDocElement = Optional.empty();
            if (null != caseData.getLegalProfQuarentineDocsList()) {
                quarentineLegalDocElement = caseData.getLegalProfQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }
            Optional<Element<UploadedDocuments>> quarentineCitizenDocElement = Optional.empty();
            if (null != caseData.getCitizenUploadQuarentineDocsList()) {
                quarentineCitizenDocElement = caseData.getCitizenUploadQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }

            if (quarentineLegalDocElement.isPresent()) {
                QuarentineLegalDoc doc = quarentineLegalDocElement.get().getValue();
                log.info("** QuarentineLegalDoc ** {}", doc);

                String doctobereviewed = String
                    .join(format("<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 "Legal professional"),
                          format("<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                                 doc.getCategory()),
                          format("<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>",
                         doc.getNotes()));
                caseDataUpdated.put("docToBeReviewed", doctobereviewed);
                caseDataUpdated.put("reviewDoc", doc.getDocument());
                log.info("** review doc ** {}", doc.getDocument());
            } else if (quarentineCitizenDocElement.isPresent()) {
                UploadedDocuments doc = quarentineCitizenDocElement.get().getValue();
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
            if (null != caseData.getLegalProfQuarentineDocsList()) {
                Optional<Element<QuarentineLegalDoc>> quarentineLegalDocElement = caseData.getLegalProfQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarentineLegalDocElement.isPresent()) {
                    Element<QuarentineLegalDoc> docDetails = caseData.getLegalProfQuarentineDocsList()
                        .remove(caseData.getLegalProfQuarentineDocsList().indexOf(quarentineLegalDocElement.get()));
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().add(docDetails);
                        caseDataUpdated.put("legalProfUploadDocListConfTab", caseData.getReviewDocuments().getLegalProfUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put("legalProfUploadDocListConfTab", List.of(docDetails));
                    }
                }
            }
            if (null != caseData.getCitizenUploadQuarentineDocsList()) {
                Optional<Element<UploadedDocuments>> quarentineCitizenDocElement = caseData.getCitizenUploadQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarentineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarentineDocsList()
                        .remove(caseData.getCitizenUploadQuarentineDocsList().indexOf(quarentineCitizenDocElement.get()));
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
            if (null != caseData.getLegalProfQuarentineDocsList()) {
                Optional<Element<QuarentineLegalDoc>> quarentineLegalDocElement = caseData.getLegalProfQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarentineLegalDocElement.isPresent()) {
                    Element<QuarentineLegalDoc> docDetails = caseData.getLegalProfQuarentineDocsList()
                        .remove(caseData.getLegalProfQuarentineDocsList().indexOf(quarentineLegalDocElement.get()));
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().add(docDetails);
                        caseDataUpdated.put("legalProfUploadDocListDocTab", caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());
                    } else {
                        caseDataUpdated.put("legalProfUploadDocListDocTab", List.of(docDetails));
                    }
                }
            }
            if (null != caseData.getCitizenUploadQuarentineDocsList()) {
                Optional<Element<UploadedDocuments>> quarentineCitizenDocElement = caseData.getCitizenUploadQuarentineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarentineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarentineDocsList()
                        .remove(caseData.getCitizenUploadQuarentineDocsList().indexOf(quarentineCitizenDocElement.get()));
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
        log.info("*** Legal prof docs q ** {}", caseData.getLegalProfQuarentineDocsList());
        log.info("***citizen docs q ** {}", caseData.getCitizenUploadQuarentineDocsList());
        caseDataUpdated.put("legalProfQuarentineDocsList", caseData.getLegalProfQuarentineDocsList());
        caseDataUpdated.put("citizenUploadQuarentineDocsList", caseData.getCitizenUploadQuarentineDocsList());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
