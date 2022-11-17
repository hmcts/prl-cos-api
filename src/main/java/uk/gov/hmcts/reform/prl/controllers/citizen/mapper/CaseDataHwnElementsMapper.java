package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper.COMMA_SEPARATOR;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataHwnElementsMapper {

    private CaseDataHwnElementsMapper() {
    }

    private static final String DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING = "Details of without notice "
            + "hearing because the other person or people may do something that would obstruct the order";

    public static void updateHearingWithoutNoticeElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                               C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        caseDataBuilder
                .doYouNeedAWithoutNoticeHearing(c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNotice())
                .reasonsForApplicationWithoutNotice(buildReasonsForApplicationWithoutNotice(
                        c100RebuildHearingWithoutNoticeElements))
                .doYouRequireAHearingWithReducedNotice(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNotice())
                .setOutReasonsBelow(c100RebuildHearingWithoutNoticeElements
                        .getDoYouNeedHearingWithoutNoticeWithoutReducedNoticeDetails());
    }

    private static String buildReasonsForApplicationWithoutNotice(
            C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements) {
        if (c100RebuildHearingWithoutNoticeElements.getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomething().equals(Yes)) {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice()
                    + COMMA_SEPARATOR + DETAILS_OF_NOTICE_OTHER_PEOPLE_WILL_DO_SOMETHING
                    + HYPHEN_SEPARATOR +  c100RebuildHearingWithoutNoticeElements
                    .getDoYouNeedHearingWithoutNoticeAsOtherPplDoSomethingDetails();
        } else {
            return c100RebuildHearingWithoutNoticeElements.getReasonsOfHearingWithoutNotice();
        }
    }

}
