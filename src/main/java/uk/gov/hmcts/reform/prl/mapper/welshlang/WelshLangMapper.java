package uk.gov.hmcts.reform.prl.mapper.welshlang;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Slf4j
public class WelshLangMapper {

    public static final String WELSH_NO = "Nac ydy";
    public static final String WELSH_NO_NAC_YDW = "Nac ydw";
    public static final String WELSH_YES_GALLAF = "Gallaf";
    public static final String WELSH_NO_NA_ALLAF = "Na allaf";
    public static final String WELSH_YES_OES = "Oes";
    public static final String WELSH_NO_NAC_OES = "Nac oes";
    public static final String WELSH_NO_NA_FYDD = "Na fydd";
    public static final String WELSH_OTHER = "Arall";
    public static final String WELSH_APPLICANT = "Ceisydd";
    public static final String WELSH_RESPONDENT = "Atebydd";
    public static final String CAN_YOU_PROVIDE_EMAIL_ADDRESS_YES = "canYouProvideEmailAddress_Yes";
    public static final String CAN_YOU_PROVIDE_EMAIL_ADDRESS_NO = "canYouProvideEmailAddress_No";

    private WelshLangMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * C100 Welsh Lang Map.
     */
    protected static final Map<String, String> CA_WELSH_MAP = getCaWelshLangMap();
    protected static final List<String> CA_WELSH_CONDITONAL_MAP = getCaConditionalFieldWelshLangMap();

    /**
     * FL401 Welsh Lang Map.
     */
    protected static final Map<String, String> DA_WELSH_MAP = getDaWelshLangMap();
    protected static final List<String> DA_WELSH_CONDITONAL_MAP = getDaConditionalFieldWelshLangMap();

    /**
     * Recursive mapper for replacing the English to Welsh.
     */
    public static Object applyWelshTranslation(Object key, Object obj, boolean isCA) {
        if (key.toString().equalsIgnoreCase("responseToAllegationsOfHarmYesOrNoResponse")) {
            log.info("got responseToAllegationsOfHarmYesOrNoResponse");
        }
        if (obj instanceof String) {
            obj = getValueFromMap(key, obj, isCA);
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            for (int i = 0; i < list.size(); i++) {
                Object eachObj = list.get(i);
                list.set(i, applyWelshTranslation(null, eachObj, isCA));
            }
        } else if (obj instanceof Map) {
            Map<String, Object> innerMap = (Map<String, Object>) obj;
            innerMap.forEach((k, v) -> {
                if (v != null) {
                    innerMap.put(k, applyWelshTranslation(k, v, isCA));
                }
            });
        }
        return obj;
    }

    private static Object getValueFromMap(Object key, Object obj, boolean isCA) {
        if (validateConditionalKey(key, isCA)) {
            if (validateMappingKey(key, obj, isCA)) {
                obj = (isCA ? CA_WELSH_MAP : DA_WELSH_MAP).get(key + "_" + obj);
            } else if (validateObject(obj, isCA)) {
                obj = (isCA ? CA_WELSH_MAP : DA_WELSH_MAP).get(obj);
            }
        }
        return obj;
    }

    private static boolean validateObject(Object obj, boolean isCA) {
        return (isCA ? CA_WELSH_MAP : DA_WELSH_MAP).containsKey(obj);
    }

    private static boolean validateMappingKey(Object key, Object obj, boolean isCA) {
        return key != null && (isCA ? CA_WELSH_MAP : DA_WELSH_MAP).containsKey(key + "_" + obj);
    }

    private static boolean validateConditionalKey(Object key, boolean isCA) {
        return !(isCA ? CA_WELSH_CONDITONAL_MAP : DA_WELSH_CONDITONAL_MAP).contains(key);
    }

    /**
     * Excluding conditional fields for changing english to welsh.
     */
    private static List<String> getCaConditionalFieldWelshLangMap() {
        return Arrays.asList(
            "isAtAddressLessThan5Years",
            "typeOfChildArrangementsOrder",
            "canYouProvideEmailAddress",
            "childrenKnownToLocalAuthority",
            "isDateOfBirthKnown",
            "isPlaceOfBirthKnown",
            "isCurrentAddressKnown",
            "isAtAddressLessThan5YearsWithDontKnow",
            "canYouProvideEmailAddress",
            "canYouProvidePhoneNumber",
            "doTheyHaveLegalRepresentation",
            "ordersNonMolestation",
            "ordersOccupation",
            "ordersForcedMarriageProtection",
            "ordersRestraining",
            "ordersOtherInjunctive",
            "ordersUndertakingInPlace",
            "childrenKnownToLocalAuthority",
            "childrenSubjectOfChildProtectionPlan",
            "typeOfChildArrangementsOrder"
        );
    }

    private static Map<String, String> getCaWelshLangMap() {

        Map<String, String> welshMap = new WeakHashMap<>();

        /**
         * Common Utils - Yes,No,Information is to be kept confidential,Gender,Don't know.
         */
        welshMap.put(YesOrNo.Yes.toString(), "Ydy");
        welshMap.put(YesOrNo.No.toString(), WELSH_NO);
        welshMap.put("confidential_mask", "Rhaid cadw’r wybodaeth hon yn gyfrinachol");
        welshMap.put(DontKnow.dontKnow.getDisplayedValue(), "Ddim yn gwybod");
        welshMap.put(Gender.female.getDisplayedValue(), "Benyw");     //will need as a condition
        welshMap.put(Gender.male.getDisplayedValue(), "Gwryw");
        welshMap.put(Gender.other.getDisplayedValue(), "Maent yn uniaethu mewn rhyw ffordd arall");

        /**
         *  Type of Application - What order(s) are you applying for?.
         */
        welshMap.put(OrderTypeEnum.childArrangementsOrder.getDisplayedValue(), "Trefniadau Plant");
        welshMap.put(OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue(), "Camau Gwaharddedig");
        welshMap.put(OrderTypeEnum.specificIssueOrder.getDisplayedValue(), "Materion Penodol");

        /**
         *  Type of Application - Type of child arrangements order.
         */
        welshMap.put(
            ChildArrangementOrderTypeEnum.spendTimeWithOrder.getDisplayedValue(),
            "Gorchymyn Treulio Amser Gyda"
        );
        welshMap.put(ChildArrangementOrderTypeEnum.liveWithOrder.getDisplayedValue(), "Gorchymyn Byw Gyda");
        welshMap.put(
            ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder.getDisplayedValue(),
            "Gorchymyn Byw Gyda a Threulio Amser Gyda"
        );

        /**
         * Type of Application - Have you applied to the court for permission to make this application?.
         */
        welshMap.put("applicationPermissionRequired_Yes", "Do");
        welshMap.put(PermissionRequiredEnum.noNowSought.getDisplayedValue(), "Naddo, gwneir cais am ganiatâd nawr");
        welshMap.put(PermissionRequiredEnum.noNotRequired.getDisplayedValue(), "Naddo, nid oes rhaid cael caniatâd");

        /**
         * Hearing urgency - Do you need a without notice hearing?.
         */
        welshMap.put("doYouNeedAWithoutNoticeHearing_Yes", "Ydw");
        welshMap.put("doYouNeedAWithoutNoticeHearing_No", WELSH_NO_NAC_YDW);
        welshMap.put("doYouRequireAHearingWithReducedNotice_Yes", "Ydw");
        welshMap.put("doYouRequireAHearingWithReducedNotice_No", WELSH_NO_NAC_YDW);


        /**
         * Hearing urgency - Are respondents aware of proceedings?.
         */
        welshMap.put("awareOfProceeding_Yes", "Ydyn");
        welshMap.put("awareOfProceeding_No", "Nac ydyn");


        /**
         * Applicant -Details - Do you need to keep the address confidential?.
         */
        welshMap.put("isAddressConfidential_Yes", "Ydw");
        welshMap.put("isAddressConfidential_No", WELSH_NO_NAC_YDW);

        /**
         * Applicant -Details - Can you provide email address?.
         */
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_YES, WELSH_YES_GALLAF);
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_NO, WELSH_NO_NA_ALLAF);

        /**
         * Applicant -Details - Do you need to keep the contact number confidential?.
         */
        welshMap.put("isPhoneNumberConfidential_Yes", "Ydw");
        welshMap.put("isPhoneNumberConfidential_No", WELSH_NO_NAC_YDW);

        /**
         * Child Details - What is the applicant's relationship to child?
         * What is the respondent’s relationship to Child 1?.
         */
        welshMap.put(RelationshipsEnum.father.getDisplayedValue(), "Tad");
        welshMap.put(RelationshipsEnum.mother.getDisplayedValue(), "Mam");
        welshMap.put(RelationshipsEnum.stepFather.getDisplayedValue(), "Llystad");
        welshMap.put(RelationshipsEnum.stepMother.getDisplayedValue(), "Llysfam");
        welshMap.put(RelationshipsEnum.grandParent.getDisplayedValue(), "Nain/Taid");
        welshMap.put(RelationshipsEnum.guardian.getDisplayedValue(), "Gwarcheidwad");
        welshMap.put(RelationshipsEnum.specialGuardian.getDisplayedValue(), "Gwarcheidwad Arbennig");
        welshMap.put(RelationshipsEnum.other.getDisplayedValue(), WELSH_OTHER);

        /**
         * Child Details - Who does the child live with? (childLiveWith).
         */
        welshMap.put(LiveWithEnum.applicant.getDisplayedValue(), WELSH_APPLICANT);
        welshMap.put(LiveWithEnum.respondent.getDisplayedValue(), WELSH_RESPONDENT);
        welshMap.put(LiveWithEnum.anotherPerson.getDisplayedValue(), "Unigolyn arall nad yw wedi’i restru");

        /**
         * Child Details - Do you need to keep the identity of the person that the child lives with confidential?
         * (isPersonIdentityConfidential).
         */
        welshMap.put("isPersonIdentityConfidential_Yes", "Ydw");
        welshMap.put("isPersonIdentityConfidential_No", WELSH_NO_NAC_YDW);

        /**
         * Respondent - Can you provide email address?.
         */
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_YES, WELSH_YES_GALLAF);
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_NO, WELSH_NO_NA_ALLAF);

        /**
         * Respondent - Can you provide a contact number?.
         */
        welshMap.put("canYouProvidePhoneNumber_Yes", WELSH_YES_GALLAF);
        welshMap.put("canYouProvidePhoneNumber_No", WELSH_NO_NA_ALLAF);

        /**
         * Respondent - Do they have legal representation?.
         */
        welshMap.put("doTheyHaveLegalRepresentation_Yes", WELSH_YES_OES);
        welshMap.put("doTheyHaveLegalRepresentation_No", WELSH_NO_NAC_OES);

        /**
         * MIAM.
         */
        welshMap.put(MiamExemptionsChecklistEnum.domesticViolence.getDisplayedValue(), "Trais domestig");
        welshMap.put(
            MiamExemptionsChecklistEnum.childProtectionConcern.getDisplayedValue(),
            "Pryderon amddiffyn plant"
        );
        welshMap.put(MiamExemptionsChecklistEnum.urgency.getDisplayedValue(), "Cais brys");
        welshMap.put(
            MiamExemptionsChecklistEnum.previousMIAMattendance.getDisplayedValue(),
            "Eisoes wedi mynychu MIAM neu esemptiad MIAM blaenorol"
        );
        welshMap.put(MiamExemptionsChecklistEnum.other.getDisplayedValue(), WELSH_OTHER);
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_22.getDisplayedValue(),
            "Tystiolaeth sy’n dangos bod darpar barti wedi bod,"
                + " neu mewn risg o fod, yn ddioddefwr trais domestig gan ddarpar barti "
                + "arall ar ffurf camdriniaeth mewn perthynas â materion ariannol."
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_21.getDisplayedValue(),
            "Llythyr gan Ysgrifennydd Gwladol y Swyddfa Gartref yn cadarnhau "
                + "bod darpar barti wedi cael caniatâd i aros yn y Deyrnas Unedig"
                + " o dan baragraff 289B Rheolau’r Ysgrifennydd Gwladol a wnaed dan Adran 3(2) "
                + "Deddf Mewnfudo 1971, a gellir eu gweld yn "
                + "https://www. gov.uk/guidance/immigration-rules/immigration-rules-index;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_20.getDisplayedValue(),
            "Llythyr gan awdurdod cyhoeddus yn cadarnhau bod unigolyn sydd, "
                + "neu a oedd mewn perthynas teuluol â darpar barti, wedi’i asesu fel ei fod yn dioddef,"
                + " neu mewn risg o ddioddef trais domestig gan y darpar barti hwnnw (neu gopi o’r asesiad hwnnw);"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_19.getDisplayedValue(),
            "Llythyr neu adroddiad gan sefydliad sy’n darparu gwasanaethau cefnogi trais domestig yn y Deyrnas"
                + " Unedig yn cadarnhau- (i) bod cais am loches i unigolyn sydd, neu a oedd mewn perthynas teuluol"
                + " â darpar barti, "
                + "wedi cael ei wrthod; (ii) y dyddiad y cafodd y cais am loches ei wrthod; a’u "
                + "(iii) bod wedi gwneud cais am loches oherwydd honiadau o drais domestig gan y "
                + "darpar barti a gyfeiriwyd atynt ym mharagraff (i);"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_18.getDisplayedValue(),
            "Llythyr- (i) gan sefydliad sy’n darparu gwasanaethau cefnogi trais domestig, "
                + "neu elusen gofrestredig, a bod y llythyr yn cadarnhau bod y gwasanaeth- (a) wedi’i leoli yng Nghymru a Lloegr, "
                + "(b) wedi bod yn gweithredu am gyfnod di-dor o chwe mis neu hirach; a’i "
                + "(c) fod wedi darparu cefnogaeth i ddarpar barti mewn perthynas ag anghenion yr unigolyn hwnnw fel dioddefwr,"
                + " neu unigolyn sydd mewn risg o drais domestig; "
                + "ac (ii) mae’n cynnwys- (a) datganiad sy’n nodi, ym marn broffesiynol resymol awdur y llythyr,"
                + " bod y darpar barti yn, neu mewn risg o fod yn ddioddefwr trais domestig; "
                + "(b) disgrifiad o’r materion penodol y gellir dibynnu arnynt i gefnogi’r farn honno; "
                + "(c) disgrifiad o’r gefnogaeth a roddwyd i’r darpar barti; "
                + "a (d) datganiad o’r rhesymau pam bod y darpar barti angen y gefnogaeth honno;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_17.getDisplayedValue(),
            "Llythyr gan swyddog a gyflogir gan awdurdod lleol neu gymdeithas tai (neu sefydliad cyfwerth yn "
                + "Yr Alban neu yng Ngogledd Iwerddon) yn cefnogi tenantiaid sy’n cynnwys- "
                + "(i) datganiad sy’n nodi, yn eu barn broffesiynol resymol nhw, bod unigolyn y mae darpar barti yn,"
                + " neu wedi bod mewn perthynas deuluol ag o/â hi yn, neu mewn risg o ddioddef trais domestig gan y darpar barti hwnnw; "
                + "(ii) disgrifiad o’r materion penodol y gellir dibynnu arnynt i gefnogi’r farn honno; a "
                + "(iii) disgrifiad o’r gefnogaeth a roddwyd ganddynt i’r dioddefwr trais domestig neu’r "
                + "unigolyn sydd mewn risg o drais domestig gan y darpar barti hwnnw;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_16.getDisplayedValue(),
            "Llythyr gan ymgynghorydd trais rhywiol annibynnol yn cadarnhau eu bod yn "
                + "darparu cefnogaeth i ddarpar barti mewn perthynas â thrais rhywiol gan ddarpar barti arall;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_15.getDisplayedValue(),
            "Llythyr gan ymgynghorydd trais domestig annibynnol yn cadarnhau eu bod yn darparu cefnogaeth i ddarpar barti;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_13.getDisplayedValue(),
            "Llythyr gan unrhyw unigolyn sy’n aelod o gynhadledd asesu risg amlasiantaeth (neu fforwm diogelu lleol addas arall) "
                + "yn cadarnhau bod darpar barti, neu unigolyn y mae y darpar barti mewn perthynas deuluol ag o/â hi, "
                + "yn neu wedi bod mewn risg o ddioddef trais domestig gan ddarpar barti arall;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_12.getDisplayedValue(),
            "Llythyr neu adroddiad gan- (i) y gweithiwr iechyd proffesiynol priodol a wnaeth yr atgyfeiriad a ddisgrifir isod; "
                + "(ii) gweithiwr iechyd proffesiynol priodol sydd â mynediad at gofnodion meddygol y darpar barti y cyfeiriwyd ato isod; "
                + "neu (iii) yr unigolyn y gwnaethpwyd yr atgyfeiriad a ddisgrifir isod iddo; "
                + "yn cadarnhau bod darpar barti wedi cael ei gyfeirio gan weithiwr iechyd proffesiynol priodol at unigolyn sy’n darparu "
                + "cefnogaeth neu gymorth arbenigol i ddioddefwyr, neu’r rhai hynny sydd mewn risg o ddioddef trais domestig;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_10.getDisplayedValue(),
            "Adroddiad arbenigwr a gyflwynwyd fel tystiolaeth mewn achos yn y Deyrnas Unedig er mwyn i lys neu dribiwnlys allu cadarnhau "
                + "bod unigolyn sydd, neu a oedd mewn perthynas teuluol â darpar barti, wedi’i asesu fel bod, "
                + "neu mewn risg o fod, yn ddioddefwr trais domestig gan y darpar barti hwnnw;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_9.getDisplayedValue(),
            "Copi o ganfyddiad ffeithiol, a wnaed mewn achos yn y Deyrnas Unedig, bod trais domestig wedi’i gyflawni gan ddarpar barti;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_8.getDisplayedValue(),
            "Ymgymeriad a roddwyd yng Nghymru a Lloegr dan adran 46 neu 63E Deddf Cyfraith Teulu 1996 (neu a roddwyd yn Yr Alban neu yng "
                + "Ngogledd Iwerddon yn lle gwaharddeb gwarchod) gan ddarpar barti, ar yr amod na roddwyd traws-ymgymeriad mewn perthynas "
                + "â thrais domestig gan ddarpar barti arall;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_7.getDisplayedValue(),
            "Gwaharddeb gwarchod perthnasol;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_6.getDisplayedValue(),
            "Hysbysiad diogelu rhag trais domestig a roddwyd dan adran 24 Deddf Troseddau a Diogelwch 2010 yn erbyn darpar barti;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_5.getDisplayedValue(),
            "Gorchymyn llys yn rhwymo darpar barti mewn perthynas â throsedd trais domestig;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4.getDisplayedValue(),
            "Tystiolaeth o euogfarn berthnasol am drosedd trais domestig;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_3.getDisplayedValue(),
            "Tystiolaeth o achos troseddol perthnasol am drosedd trais domestig sydd heb ddod i ben;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_2.getDisplayedValue(),
            "Tystiolaeth o rybuddiad heddlu perthnasol am drosedd trais domestig;"
        );
        welshMap.put(
            MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_1.getDisplayedValue(),
            "Tystiolaeth bod darpar barti wedi cael ei arestio am drosedd trais domestig perthnasol;"
        );
        welshMap.put(
            MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1.getDisplayedValue(),
            "Mae awdurdod lleol yn cynnal ymholiadau dan Adran 47 Deddf Plant 1989"
        );
        welshMap.put(
            MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_2.getDisplayedValue(),
            "Mae awdurdod lleol wedi rhoi cynllun amddiffyn plant mewn lle"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_5.getDisplayedValue(),
            "Mae yna risg sylweddol yn y cyfnod a gymerir i drefnu a mynychu MIAM, "
                + "y bydd achos mewn perthynas â’r anghydfod yn cael ei ddwyn gerbron llys mewn gwlad arall, "
                + "lle y gall hawliad dilys i awdurdodaeth fodoli, megis y gall llys yn y wlad arall honno wrando "
                + "ar yr anghydfod cyn llys yng Nghymru a Lloegr"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_4.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri problemau anadferadwy wrth ddelio â’r anghydfod "
                + "(gan gynnwys colled anadferadwy o dystiolaeth sylweddol)"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_3.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri caledi afresymol i’r ceisydd arfaethedig"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_2.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir drwy fynychu MIAM yn peri risg sylweddol o gamwedd cyfiawnder"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_1.getDisplayedValue(),
            "Mae yna risg i fywyd, rhyddid neu ddiogelwch corfforol y ceisydd arfaethedig neu ei deulu/theulu neu ei gartref/chartref; neu"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_6.getDisplayedValue(),
            "Mae yna risg y byddai plentyn yn cael ei gludo o’r Deyrnas Unedig yn anghyfreithlon, "
                + "neu risg y byddai plentyn sydd y tu allan i Gymru a Lloegr ar hyn o bryd yn cael ei gadw’n anghyfreithlon"
        );
        welshMap.put(
            MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_7.getDisplayedValue(),
            "Mae yna risg o niwed i blentyn"
        );
        welshMap.put(
            MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_5.getDisplayedValue(),
            "Byddai’r cais yn cael ei wneud ynghylch achos presennol sy’n parhau ac roedd esemptiad MIAM yn "
                + "berthnasol pan wnaed y cais ynghylch yr achos hwnnw"
        );
        welshMap.put(
            MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_4.getDisplayedValue(),
            "Byddai’r cais yn cael ei wneud ynghylch achos presennol sy’n parhau ac mi wnaeth y "
                + "darpar geisydd fynychu MIAM cyn cychwyn yr achos hwnnw"
        );
        welshMap.put(
            MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_3.getDisplayedValue(),
            "Yn y 4 mis cyn gwneud y cais, bod yr unigolyn wedi ffeilio cais perthnasol i’r llys teulu yn "
                + "cadarnhau bod esemptiad MIAM yn berthnasol"
                + " a bod y cais yn ymwneud â’r un anghydfod neu yr un anghydfod i raddau helaeth"
        );
        welshMap.put(
            MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_2.getDisplayedValue(),
            "Adeg gwneud y cais, mae’r unigolyn yn cymryd rhan mewn math arall o broses i ddatrys anghydfod y "
                + "tu allan i’r llys yn ymwneud â’r un "
                + "anghydfod neu yr un anghydfod i raddau helaeth"
        );
        welshMap.put(
            MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1.getDisplayedValue(),
            "Yn y 4 mis cyn gwneud y cais, bod yr unigolyn wedi mynychu MIAM neu wedi cymryd rhan mewn"
                + " math arall o broses i ddatrys anghydfod y "
                + "tu allan i’r llys yn ymwneud â’r un anghydfod neu yr un anghydfod i raddau helaeth"
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_11.getDisplayedValue(),
            "Nid oes gan yr un cyfryngwr teuluol awdurdodedig swyddfa o fewn pymtheg milltir i gartref y darpar geisydd."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_10.getDisplayedValue(),
            "(i) mae’r darpar geisydd wedi cysylltu â’r holl gyfryngwyr teuluol awdurdodedig sydd â swyddfa o fewn pymtheg milltir "
                + "i’w gartref neu ei chartref (neu dri ohonynt os oes tri neu fwy), ac mae pob un ohonynt wedi dweud na allant gynnal MIAM "
                + "o fewn pymtheg diwrnod busnes; a (ii) gellir darparu enwau, "
                + "cyfeiriadau a rhifau ffôn neu gyfeiriadau e-bost y cyfryngwyr "
                + "teuluol awdurdodedig hynny a’r dyddiadau cysylltu i’r llys ar gais."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_9.getDisplayedValue(),
            "Mae un o’r darpar bartïon yn blentyn yn rhinwedd Rheol 12.3(1)"
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_8.getDisplayedValue(),
            "Nid yw’r darpar geisydd neu’r holl ddarpar atebwyr yn preswylio’n arferol yng Nghymru neu Lloegr."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_7.getDisplayedValue(),
            "ni all y darpar geisydd neu’r holl ddarpar atebwyr fynychu MIAM oherwydd y mae ef neu hi, "
                + "neu y mae nhw (i) yn y carchar neu mewn unrhyw sefydliad arall ac y mae’n rhaid ei g/chadw neu eu cadw yno; "
                + "(ii) yn destun amodau mechnïaeth sy’n eu hatal rhag cysylltu â’r unigolyn arall; "
                + "neu (iii) yn destun trwydded gyda gofyniad i beidio â chysylltu â’r unigolyn arall."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_6.getDisplayedValue(),
            "(i) mae gan y darpar geisydd neu’r holl ddarpar atebwyr anabledd neu analluogrwydd a fyddai’n golygu na all neu "
                + "na allant fynychu MIAM oni bai bod y cyfryngwr awdurdodedig yn gallu cynnig cyfleusterau addas; "
                + "mae’r darpar geisydd wedi cysylltu â’r holl gyfryngwyr teuluol awdurdodedig sydd â swyddfa o fewn pymtheg "
                + "milltir i’w gartref neu ei chartref (neu dri ohonynt os oes tri neu fwy), "
                + "ac mae pob un ohonynt wedi dweud na allant ddarparu cyfleusterau o’r fath; "
                + "a (iii)gellir darparu enwau, cyfeiriadau a rhifau ffôn neu gyfeiriadau e-bost y cyfryngwyr teuluol "
                + "awdurdodedig hynny a’r dyddiadau cysylltu i’r llys ar gais."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_5.getDisplayedValue(),
            "Byddai’r cais yn cael ei wneud heb hysbysiad (Mae paragraff 5.1 o Gyfarwyddyd Ymarfer 18A yn nodi’r "
                + "amgylchiadau pan ellir gwneud cais heb hysbysiad.)"
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_4.getDisplayedValue(),
            "Nid oes gan y darpar geisydd fanylion cyswllt digonol ar gyfer unrhyw un o’r darpar atebwyr "
                + "i alluogi cyfryngwr teuluol gysylltu ag unrhyw un o’r darpar atebwyr i drefnu’r MIAM."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_3.getDisplayedValue(),
            "Mae’r ceisydd yn fethdalwr ac mae ganddo/i orchymyn methdalu ar gyfer y darpar geisydd."
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_2.getDisplayedValue(),
            "Mae’r ceisydd yn fethdalwr ac mae ganddo/i ddeiseb gan gredydwr y darpar geisydd am orchymyn methdalu"
        );
        welshMap.put(
            MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_1.getDisplayedValue(),
            "Mae’r ceisydd yn fethdalwr ac mae ganddo/i gais gan y darpar geisydd am orchymyn methdalu;"
        );

        /**
         * Allegations of harm.
         */
        welshMap.put("allegationsOfHarmYesNo_Yes", WELSH_YES_OES);
        welshMap.put("allegationsOfHarmYesNo_No", WELSH_NO_NAC_OES);
        welshMap.put("allegationsOfHarmDomesticAbuseYesNo_Yes", WELSH_YES_OES);
        welshMap.put("allegationsOfHarmDomesticAbuseYesNo_No", WELSH_NO_NAC_OES);
        welshMap.put(ApplicantOrChildren.applicants.getDisplayedValue(), "Ceisydd/Ceiswyr");
        welshMap.put(ApplicantOrChildren.children.getDisplayedValue(), "Plentyn/Plant");
        welshMap.put(AbductionChildPassportPossessionEnum.mother.getDisplayedValue(), "Mam");
        welshMap.put(AbductionChildPassportPossessionEnum.father.getDisplayedValue(), "Tad");
        welshMap.put(AbductionChildPassportPossessionEnum.other.getDisplayedValue(), WELSH_OTHER);
        welshMap.put("allegationsOfHarmChildAbductionYesNo_Yes", WELSH_YES_OES);
        welshMap.put("allegationsOfHarmChildAbductionYesNo_No", WELSH_NO_NAC_OES);

        welshMap.put("allegationsOfHarmChildAbuseYesNo_No", WELSH_NO_NAC_OES);
        welshMap.put("allegationsOfHarmChildAbuseYesNo_Yes", WELSH_YES_OES);

        welshMap.put("allegationsOfHarmSubstanceAbuseYesNo_Yes", WELSH_YES_OES);
        welshMap.put("allegationsOfHarmOtherConcernsYesNo_Yes", WELSH_YES_OES);
        welshMap.put("allegationsOfHarmSubstanceAbuseYesNo_No", WELSH_NO_NAC_OES);
        welshMap.put("allegationsOfHarmOtherConcernsYesNo_No", WELSH_NO_NAC_OES);

        welshMap.put("ordersNonMolestation_No", WELSH_NO_NAC_OES);
        welshMap.put("ordersOccupation_Yes", WELSH_YES_OES);
        welshMap.put("ordersOccupation_No", WELSH_NO_NAC_OES);
        welshMap.put("ordersNonMolestation_Yes", WELSH_YES_OES);

        welshMap.put("ordersForcedMarriageProtection_Yes", WELSH_YES_OES);
        welshMap.put("ordersRestraining_Yes", WELSH_YES_OES);
        welshMap.put("ordersOtherInjunctive_Yes", WELSH_YES_OES);
        welshMap.put("ordersForcedMarriageProtection_No", WELSH_NO_NAC_OES);
        welshMap.put("ordersRestraining_No", WELSH_NO_NAC_OES);
        welshMap.put("ordersOtherInjunctive_No", WELSH_NO_NAC_OES);

        welshMap.put("ordersUndertakingInPlace_Yes", WELSH_YES_OES);
        welshMap.put("ordersUndertakingInPlace_No", WELSH_NO_NAC_OES);
        welshMap.put("abductionChildHasPassport_No", WELSH_NO_NAC_OES);
        welshMap.put("abductionChildHasPassport_Yes", WELSH_YES_OES);


        /**
         * Other Proceedings.
         */
        welshMap.put(ProceedingsEnum.ongoing.getDisplayedValue(), "achosion sy’n mynd ymlaen ar hyn o bryd");
        welshMap.put(ProceedingsEnum.previous.getDisplayedValue(), "achosion blaenorol");
        welshMap.put(TypeOfOrderEnum.emergencyProtectionOrder.getDisplayedValue(), "Gorchymyn Diogelu Brys");
        welshMap.put(TypeOfOrderEnum.supervisionOrder.getDisplayedValue(), "Gorchymyn Goruchwylio");
        welshMap.put(TypeOfOrderEnum.careOrder.getDisplayedValue(), "Gorchymyn Gofal");
        welshMap.put(TypeOfOrderEnum.childAbduction.getDisplayedValue(), "Herwgydio plentyn");
        welshMap.put(TypeOfOrderEnum.familyLaw1996Part4.getDisplayedValue(), "Deddf Cyfraith Teulu 1996 Rhan 4");
        welshMap.put(
            TypeOfOrderEnum.contactOrResidenceOrder.getDisplayedValue(),
            "Gorchymyn cyswllt neu orchymyn preswylio a wnaed fel rhan o achos ysgaru neu achos diddymu partneriaeth sifil"
        );
        welshMap.put(
            TypeOfOrderEnum.contactOrResidenceOrderWithAdoption.getDisplayedValue(),
            "Gorchymyn cyswllt neu orchymyn preswylio a wnaed mewn perthynas â Gorchymyn Mabwysiadu"
        );
        welshMap.put(
            TypeOfOrderEnum.orderRelatingToChildMaintainance.getDisplayedValue(),
            "Gorchymyn yn ymwneud â chynhaliaeth plant"
        );
        welshMap.put(TypeOfOrderEnum.childArrangementsOrder.getDisplayedValue(), "Gorchymyn trefniadau plant");
        welshMap.put(TypeOfOrderEnum.otherOrder.getDisplayedValue(), "Gorchmynion eraill");

        /**
         * Attending the hearing.*/
        welshMap.put("isWelshNeeded_yes", "Bydd");
        welshMap.put("isWelshNeeded_no", WELSH_NO_NA_FYDD);
        welshMap.put(SpokenOrWrittenWelshEnum.spoken.getDisplayedValue(), "Byddant eisiau siarad Cymraeg");
        welshMap.put(
            SpokenOrWrittenWelshEnum.written.getDisplayedValue(),
            "Byddant eisiau darllen ac ysgrifennu yn Gymraeg"
        );
        welshMap.put(SpokenOrWrittenWelshEnum.both.getDisplayedValue(), "Y ddau");
        welshMap.put("isInterpreterNeeded_yes", "Bydd");
        welshMap.put("isInterpreterNeeded_no", WELSH_NO_NA_FYDD);
        welshMap.put(PartyEnum.applicant.getDisplayedValue(), WELSH_APPLICANT);
        welshMap.put(PartyEnum.respondent.getDisplayedValue(), WELSH_RESPONDENT);
        welshMap.put(PartyEnum.other.getDisplayedValue(), "Pobl eraill yn yr achos");
        welshMap.put("isDisabilityPresent_Yes", WELSH_YES_OES);
        welshMap.put("isDisabilityPresent_No", WELSH_NO_NAC_OES);
        welshMap.put("isSpecialArrangementsRequired_Yes", "Bydd");
        welshMap.put("isSpecialArrangementsRequired_No", WELSH_NO_NA_FYDD);

        /**
         * International Element.
         */
        welshMap.put("habitualResidentInOtherState_Yes", WELSH_YES_OES);
        welshMap.put("habitualResidentInOtherState_No", WELSH_NO_NAC_OES);
        welshMap.put("jurisdictionIssue_Yes", WELSH_YES_OES);
        welshMap.put("jurisdictionIssue_No", WELSH_NO_NAC_OES);
        welshMap.put("requestToForeignAuthority_Yes", WELSH_YES_OES);
        welshMap.put("requestToForeignAuthority_No", WELSH_NO_NAC_OES);

        /**
         * Litigation capacity.
         */
        welshMap.put("litigationCapacityOtherFactors_Yes", "Ydw");
        welshMap.put("litigationCapacityOtherFactors_No", WELSH_NO_NAC_YDW);

        /**
         * Welsh language requirement.
         */
        welshMap.put(LanguagePreference.english.getDisplayedValue(), "Saesneg");
        welshMap.put(LanguagePreference.welsh.getDisplayedValue(), "Cymraeg");

        welshMap.put(
            SdoHearingUrgentCheckListEnum.immediateRisk.getDisplayedValue(),
            "Mae tystiolaeth o risg uniongyrchol o niwed i'r plentyn(plant)"
        );
        welshMap.put(
            SdoHearingUrgentCheckListEnum.applicantsCare.getDisplayedValue(),
            "Mae tystiolaeth i awgrymu bod yr atebydd yn ceisio tynnu'r plentyn(plant) o ofal y ceisydd"
        );
        welshMap.put(
            SdoHearingUrgentCheckListEnum.seekToFrustrate.getDisplayedValue(),
            "Mae tystiolaeth i awgrymu y byddai'r atebydd yn ceisio rhwystro'r broses os na wrandewir y cais ar frys"
        );
        welshMap.put(
            SdoHearingUrgentCheckListEnum.leaveTheJurisdiction.getDisplayedValue(),
            "Mae tystiolaeth i awgrymu y gall yr atebydd geisio gadael yr awdurdodaeth gyda'r plentyn (plant) os na wrandewir y cais ar frys"
        );
        welshMap.put(
            SdoHearingUrgentCheckListEnum.other.getDisplayedValue(),
            "Rheswm arall nad yw wedi’i restru"
        );

        welshMap.put(
            SdoTransferApplicationReasonEnum.courtInAreaChildLives.getDisplayedValue(),
            "Mae llys arall yn yr ardal lle mae'r plentyn fel arfer yn byw"
        );
        welshMap.put(
            SdoTransferApplicationReasonEnum.ongoingProceedings.getDisplayedValue(),
            "Mae achosion parhaus mewn llys arall"
        );

        welshMap.put(
            HearingChannelsEnum.VID.getDisplayedValue(),
            "Drwy fideo"
        );
        welshMap.put(
            HearingChannelsEnum.TEL.getDisplayedValue(),
            "Dros y ffôn"
        );
        welshMap.put(
            HearingChannelsEnum.ONPPRS.getDisplayedValue(),
            "Ar bapur"
        );

        //For MIAM Policy Upgrade
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse.getDisplayedValue(),
            "Cam-drin domestig");

        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance.getDisplayedValue(),
            "Wedi mynychu MIAM yn flaenorol neu ddatrys anghydfod y tu allan i’r llys");

        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1.getDisplayedValue(),
            "Tystiolaeth bod darpar barti wedi cael ei arestio am drosedd cam-drin domestig perthnasol.");

        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_2.getDisplayedValue(),
            "Tystiolaeth o rybudd heddlu perthnasol am drosedd cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_3.getDisplayedValue(),
            "Tystiolaeth o achos troseddol perthnasol am drosedd cam-drin domestig sydd heb ddod i ben."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_4.getDisplayedValue(),
            "Tystiolaeth o euogfarn berthnasol am drosedd cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_5.getDisplayedValue(),
            "Gorchymyn llys yn rhwymo darpar barti mewn perthynas â throsedd cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_6.getDisplayedValue(),
            "Hysbysiad amddiffyn rhag trais domestig a roddwyd dan adran 24 Deddf Troseddau a Diogelwch 2010 yn erbyn darpar barti."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_7.getDisplayedValue(),
            "Hysbysiad amddiffyn rhag cam-drin domestig a roddwyd dan adran 22 Deddf Troseddau a Diogelwch 2021 yn erbyn darpar barti."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_8.getDisplayedValue(),
            "Gwaharddeb gwarchod perthnasol."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_9.getDisplayedValue(),
            "Ymgymeriad a roddwyd yng Nghymru a Lloegr dan adran 46 neu 63E Deddf Cyfraith Teulu 1996 (neu a roddwyd yn Yr Alban neu "
                + "yng Ngogledd Iwerddon yn lle gwaharddeb gwarchod) gan ddarpar barti, ar yr amod na roddwyd traws-ymgymeriad mewn perthynas"
                + " â thrais domestig neu gam-drin domestig gan ddarpar barti arall."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_10.getDisplayedValue(),
            "Copi o ganfyddiad ffeithiol, a wnaed mewn achos yn y Deyrnas Unedig, bod cam-drin domestig wedi’i gyflawni gan ddarpar barti."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_11.getDisplayedValue(),
            "Adroddiad arbenigwr a gyflwynwyd fel tystiolaeth mewn achos yn y Deyrnas Unedig er mwyn i lys neu dribiwnlys allu "
                + "cadarnhau bod unigolyn sydd neu a oedd yn ymwneud yn bersonol â darpar barti, wedi’i asesu fel bod, neu mewn risg o fod, "
                + "yn ddioddefwr cam-drin domestig gan y darpar barti hwnnw."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_12.getDisplayedValue(),
            "Llythyr neu adroddiad gan weithiwr iechyd proffesiynol priodol yn cadarnhau - (i) bod gweithiwr proffesiynol,"
                + " neu weithiwr iechyd proffesiynol priodol arall, wedi archwilio darpar barti yn bersonol, dros y ffôn neu drwy"
                + " gynhadledd fideo; ac (ii) ym marn broffesiynol resymol yr awdur neu’r gweithiwr iechyd proffesiynol priodol  sy’n "
                + "archwilio, mae gan y darpar barti hwnnw/honno anafiadau neu gyflwr, neu mae’r darpar barti wedi cael anafiadau neu "
                + "gyflwr, sy’n gyson â bod yn ddioddefwr cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_13.getDisplayedValue(),
            "Llythyr neu adroddiad gan (i) y gweithiwr iechyd proffesiynol priodol a wnaeth yr atgyfeiriad a ddisgrifir isod;"
                + " (ii) weithiwr iechyd proffesiynol priodol sydd â mynediad i gofnodion meddygol y darpar barti a gyfeiriwyd ato/i isod; neu "
                + "(iii) yr unigolyn y gwnaethpwyd yr atgyfeiriad a ddisgrifir isod iddo; yn cadarnhau bod darpar barti wedi cael ei gyfeirio gan "
                + "weithiwr iechyd proffesiynol priodol at unigolyn sy’n darparu cefnogaeth neu gymorth arbenigol i ddioddefwyr, neu’r rhai hynny "
                + "sydd mewn risg o ddioddef cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_14.getDisplayedValue(),
            "Llythyr gan unrhyw unigolyn sy’n aelod o gynhadledd asesu risg amlasiantaeth (neu fforwm diogelu lleol addas arall) yn cadarnhau bod "
                + "darpar barti, neu unigolyn y mae’r darpar barti hwnnw/honno yn ymwneud ag ef/â hi yn bersonol, yn neu wedi bod mewn risg o "
                + "niwed o ganlyniad i gam-drin domestig gan ddarpar barti arall."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_15.getDisplayedValue(),
            "Llythyr gan ymgynghorydd trais domestig annibynnol (IDVA) yn cadarnhau eu bod yn darparu neu eu bod wedi "
                + "darparu cefnogaeth i ddarpar barti."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_16.getDisplayedValue(),
            "Llythyr gan ymgynghorydd trais rhywiol annibynnol (ISVA) yn cadarnhau eu bod yn darparu neu eu bod wedi darparu cefnogaeth "
                + "i ddarpar barti o ganlyniad i drais rhywiol gan ddarpar barti arall."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_17.getDisplayedValue(),
            "Llythyr gan swyddog a gyflogir gan awdurdod lleol neu gymdeithas tai (neu sefydliad cyfwerth yn yr Alban neu yng Ngogledd Iwerddon)"
                + " yn cefnogi tenantiaid sy’n cynnwys- (i) datganiad sy’n nodi, yn eu barn broffesiynol resymol nhw."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_18.getDisplayedValue(),
            "Llythyr- (i) gan sefydliad sy’n darparu gwasanaethau cefnogi cam-drin domestig, a bod y llythyr yn cadarnhau ei fod- (a) wedi’i "
                + "leoli yn y Deyrnas Unedig, (b) wedi bod yn gweithredu am gyfnod di-dor o chwe mis neu hirach; a’i (c) fod wedi darparu "
                + "cefnogaeth i ddarpar barti mewn perthynas ag anghenion yr unigolyn hwnnw fel dioddefwr, neu unigolyn sydd mewn risg "
                + "o gam-drin domestig; ac (ii) mae’n cynnwys- (a) datganiad sy’n nodi, ym marn broffesiynol resymol awdur y llythyr,"
                + " bod y darpar barti yn, neu mewn risg o fod yn ddioddefwr cam-drin domestig; (b) disgrifiad o’r materion penodol y "
                + "gellir dibynnu arnynt i gefnogi’r farn honno; (c) disgrifiad o’r gefnogaeth a roddwyd i’r darpar barti; a (d) datganiad"
                + " o’r rhesymau pam bod y darpar barti angen y gefnogaeth honno."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_19.getDisplayedValue(),
            "Llythyr neu adroddiad gan sefydliad sy’n darparu gwasanaethau cefnogi cam-drin domestig yn y Deyrnas Unedig yn cadarnhau-"
                + " (i) bod cais am loches i unigolyn sydd, neu a oedd yn ymwneud yn bersonol â darpar barti, wedi cael ei wrthod; (ii) "
                + "y dyddiad y cafodd y cais am loches ei wrthod; a’u (iii) bod wedi gwneud cais am loches oherwydd honiadau o gam-drin "
                + "domestig gan y darpar barti a gyfeiriwyd ato/i ym mharagraff (i)."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_20.getDisplayedValue(),
            "Llythyr gan awdurdod cyhoeddus yn cadarnhau bod unigolyn sydd, neu a oedd yn ymwneud yn bersonol â darpar barti,"
                + " wedi’i asesu fel ei fod yn ddioddefwr, neu mewn risg o fod yn ddioddefwr cam-drin domestig gan y darpar "
                + "barti hwnnw/o (neu gopi o’r asesiad hwnnw)."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_21.getDisplayedValue(),
            "Llythyr gan Ysgrifennydd Gwladol yr Adran Gartref yn cadarnhau bod darpar barti wedi cael caniatâd i aros yn"
                + " y Deyrnas Unedig fel dioddefwr cam-drin domestig."
        );
        welshMap.put(
            MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_22.getDisplayedValue(),
            "Tystiolaeth sy’n dangos bod darpar barti wedi bod, neu mewn risg o fod, yn ddioddefwr cam-drin domestig gan ddarpar barti arall ar "
                + "ffurf camdriniaeth sy’n ymwneud â materion ariannol."
        );

        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1.getDisplayedValue(),
            "Mae yna risg i fywyd, rhyddid neu ddiogelwch corfforol y darpar geisydd neu ei deulu/theulu neu ei gartref/chartref."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_2.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir trwy fynychu MIAM yn achosi risg o niwed i blentyn."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_3.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir trwy fynychu MIAM yn achosi risg y byddai plentyn yn cael ei gludo o’r Deyrnas Unedig yn anghyfreithlon, "
                + "neu risg y byddai plentyn sydd y tu allan i Gymru a Lloegr ar hyn o bryd yn cael ei gadw’n anghyfreithlon."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_4.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir trwy fynychu MIAM yn achosi risg sylweddol o gamwedd cyfiawnder."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_5.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir trwy fynychu MIAM yn achosi caledi ariannol sylweddol i’r darpar geisydd."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_6.getDisplayedValue(),
            "Byddai unrhyw oedi a achosir trwy fynychu MIAM yn achosi problemau anadferadwy wrth ddelio â’r anghydfod (gan "
                + "gynnwys colled anadferadwy o dystiolaeth sylweddol)."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_7.getDisplayedValue(),
            "Mae yna risg sylweddol yn y cyfnod a gymerir i drefnu a mynychu MIAM, y bydd achos mewn perthynas â’r anghydfod yn"
                + " cael ei ddwyn gerbron llys mewn gwlad arall, lle y gall hawliad dilys i awdurdodaeth fodoli, ac y bydd llys "
                + "yn y wlad arall honno yn gwrando ar yr anghydfod cyn llys yng Nghymru a Lloegr."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1
                .getDisplayedValue(),
            "Yn y 4 mis cyn gwneud y cais, mae’r unigolyn wedi mynychu MIAM neu wedi cymryd rhan mewn proses datrys anghydfod y "
                + "tu allan i’r llys yn ymwneud â’r un anghydfod neu’r un anghydfod i raddau helaeth; a phan fu i’r ceisydd gymryd rhan mewn "
                + "proses datrys anghydfod y tu allan i’r llys, mae yna dystiolaeth o hynny ar ffurf cadarnhad ysgrifenedig gan y darparwr"
                + " datrys anghydfod."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_2
                .getDisplayedValue(),
            "Byddai’r cais yn cael ei wneud ynghylch achos presennol sy’n parhau ac mi wnaeth y darpar geisydd fynychu MIAM cyn cychwyn yr "
                + "achos hwnnw. Bydd angen i chi uwchlwytho tystysgrif y cyfryngwr. Os mai chi yw’r atebydd yn yr achos sydd ar y gweill, "
                + "rhowch ddyddiad y MIAM yn ogystal ag enw a manylion cyswllt y darparwr MIAM yn y blwch testun."
        );

        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_1
                .getDisplayedValue(),
            "Byddai’r cais yn cael ei wneud heb rybudd (Mae paragraff 5.1 o Gyfarwyddyd Ymarfer 18A yn nodi’r amgylchiadau"
                + " pan ellir gwneud ceisiadau heb rybudd)."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_2
                .getDisplayedValue(),
            "Mae un o’r darpar bartïon yn blentyn."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_3
                .getDisplayedValue(),
            "(i) Ni all y darpar geisydd fynychu MIAM ar-lein neu drwy gyswllt fideo a rhoddir esboniad am hyn i’r llys trwy"
                + " ddefnyddio’r blwch testun a ddarparwyd; ac (ii) mae’r darpar geisydd wedi cysylltu â phob cyfryngwr teuluol"
                + " awdurdodedig sydd â swyddfa o fewn 15 milltir i’w gartref neu ei chartref (neu 5 ohonynt os oes 5 neu fwy o gyfryngwyr),"
                + " ac mae pob un ohonynt wedi datgan nad ydynt ar gael i gynnal MIAM o fewn 15 diwrnod busnes i’r dyddiad cysylltu."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_4
                .getDisplayedValue(),
            "(i) Ni all y darpar geisydd fynychu MIAM ar-lein neu drwy gyswllt fideo a rhoddir esboniad am hyn i’r llys "
                + "trwy ddefnyddio’r blwch testun a ddarparwyd; ac\n"
                + "(ii) mae gan y darpar geisydd anabledd neu analluogrwydd arall a fyddai’n ei atal rhag mynychu MIAM yn "
                + "bersonol oni bai y gellir cynnig cyfleusterau priodol gan gyfryngwr awdurdodedig, ac (iii) mae’r darpar "
                + "geisydd wedi cysylltu â phob cyfryngwr teuluol awdurdodedig sydd â swyddfa o fewn 15 milltir i’w gartref "
                + "neu ei chartref (neu 5 ohonynt os oes yna 5 neu fwy o gyfryngwyr), ac mae pob un ohonynt wedi datgan nad ydynt"
                + " yn gallu darparu cyfleusterau o’r fath.");
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_5
                .getDisplayedValue(),
            "(i) Ni all y darpar geisydd fynychu MIAM ar-lein neu drwy gyswllt fideo; ac (ii) "
                + "nid oes yna gyfryngwr teuluol awdurdodedig sydd â swyddfa o fewn 15 milltir i gartref y darpar geisydd."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_6
                .getDisplayedValue(),
            "Ni all y darpar geisydd fynychu MIAM oherwydd y mae’r darpar geisydd (i) yn y carchar neu mewn unrhyw sefydliad arall"
                + " ac y mae’n rhaid cadw’r darpar geisydd yno ac ni ellir gwneud trefniadau iddynt fynychu MIAM ar-lein neu drwy"
                + " gyswllt fideo; neu (ii) yn destun amodau mechnïaeth sy’n eu hatal rhag cysylltu â’r unigolyn arall; neu (iii)"
                + " yn destun trwydded gyda gofyniad i beidio â chysylltu â’r unigolyn arall."
        );
        welshMap.put(
            uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuChildProtectionConcern.getDisplayedValue(),
            "Pryderon amddiffyn plant"
        );

        return welshMap;
    }

    /**
     * FL401 Welsh Lang Map.
     * */

    /**
     * Excluding conditional fields for changing english to welsh.
     */
    private static List<String> getDaConditionalFieldWelshLangMap() {
        return Arrays.asList("");
    }

    private static Map<String, String> getDaWelshLangMap() {

        Map<String, String> welshMap = new WeakHashMap<>();

        /**
         * Common Utils Yes, No, Don't know.
         */
        welshMap.put(YesOrNo.Yes.toString(), "Ydy");
        welshMap.put(YesOrNo.No.toString(), WELSH_NO);
        welshMap.put(DontKnow.dontKnow.getDisplayedValue(), "Ddim yn gwybod");
        welshMap.put(Gender.female.getDisplayedValue(), "Benywaidd");
        welshMap.put(Gender.male.getDisplayedValue(), "Gwrywaidd");
        welshMap.put(Gender.other.getDisplayedValue(), "Hunaniaeth ryweddol arall");

        /**
         * FL401 Type of Application.
         */
        welshMap.put(FL401OrderTypeEnum.nonMolestationOrder.getDisplayedValue(), "Gorchymyn rhag molestu");
        welshMap.put(FL401OrderTypeEnum.occupationOrder.getDisplayedValue(), "Gorchymyn anheddu");

        /**
         *  Type of Application - What order(s) are you applying for?.
         */
        welshMap.put(OrderTypeEnum.childArrangementsOrder.getDisplayedValue(), "Trefniadau Plant");
        welshMap.put(OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue(), "Camau Gwaharddedig");
        welshMap.put(OrderTypeEnum.specificIssueOrder.getDisplayedValue(), "Materion Penodol");

        /**
         * Without notice order. reasonForOrderWithoutGivingNotice.
         */
        welshMap.put("orderWithoutGivingNotice_Yes", "Ydw");
        welshMap.put("orderWithoutGivingNotice_No", WELSH_NO_NAC_YDW);

        /**
         * Without notice order - Why do you want to apply without giving notice to the respondent?.
         */
        welshMap.put(
            ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild.getDisplayedValue(),
            "Mae risg o niwed sylweddol i'r ceisydd neu i blentyn perthnasol, y gellir ei "
                + "phriodoli i ymddygiad yr atebydd, os na wneir y gorchymyn ar unwaith"
        );
        welshMap.put(
            ReasonForOrderWithoutGivingNoticeEnum.deferringApplicationIfNotImmediate.getDisplayedValue(),
            "Mae'n debygol y bydd y ceisydd yn cael ei rwystro neu ei atal rhag bwrw ymlaen â'r "
                + "cais os na wneir gorchymyn ar unwaith"
        );
        welshMap.put(
            ReasonForOrderWithoutGivingNoticeEnum.prejudiced.getDisplayedValue(),
            "Mae'r ceisydd yn credu bod yr atebydd yn ymwybodol o'r gweithrediadau ond ei fod "
                + "yn osgoi'r cyflwyno yn fwriadol ac y bydd y ceisydd neu blentyn perthnasol "
                + "yn cael ei niweidio'n ddifrifol gan yr oedi cyn cyflawni cyflwyniad dirprwyol"
        );

        /**
         * Applicant's Details. applicantsFL401.
         */
        welshMap.put("isAddressConfidential_Yes", WELSH_YES_OES);
        welshMap.put("isAddressConfidential_No", WELSH_NO_NAC_OES);
        welshMap.put("isEmailAddressConfidential_Yes", WELSH_YES_OES);
        welshMap.put("isEmailAddressConfidential_No", WELSH_NO_NAC_OES);
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_YES, WELSH_YES_GALLAF);
        welshMap.put(CAN_YOU_PROVIDE_EMAIL_ADDRESS_NO, WELSH_NO_NA_ALLAF);
        welshMap.put("isPhoneNumberConfidential_Yes", WELSH_YES_OES);
        welshMap.put("isPhoneNumberConfidential_No", WELSH_NO_NAC_OES);

        /**
         * Respondent Details. respondentsFL401.
         */
        welshMap.put("isCurrentAddressKnown_Yes", "Ydw");
        welshMap.put("isCurrentAddressKnown_No", WELSH_NO_NAC_YDW);
        welshMap.put("canYouProvidePhoneNumber_Yes", WELSH_YES_GALLAF);
        welshMap.put("canYouProvidePhoneNumber_No", WELSH_NO_NA_ALLAF);
        welshMap.put("doTheyHaveLegalRepresentation_Yes", WELSH_YES_OES);
        welshMap.put("doTheyHaveLegalRepresentation_No", WELSH_NO_NAC_OES);

        /**
         * Relationship to respondent.
         */
        welshMap.put(
            ApplicantRelationshipEnum.marriedOrCivil.getDisplayedValue(),
            "Yn briod neu mewn partneriaeth sifil"
        );
        welshMap.put(
            ApplicantRelationshipEnum.formerlyMarriedOrCivil.getDisplayedValue(),
            "Yn briod neu mewn partneriaeth sifil o'r blaen"
        );
        welshMap.put(
            ApplicantRelationshipEnum.engagedOrProposed.getDisplayedValue(),
            "Wedi dyweddïo neu wedi cynnig partneriaeth sifil"
        );
        welshMap.put(
            ApplicantRelationshipEnum.formerlyEngagedOrProposed.getDisplayedValue(),
            "Wedi dyweddïo neu wedi cynnig partneriaeth sifil o'r blaen"
        );
        welshMap.put(
            ApplicantRelationshipEnum.liveTogether.getDisplayedValue(),
            "Yn cyd-fyw fel cwpl"
        );
        welshMap.put(
            ApplicantRelationshipEnum.foremerlyLivedTogether.getDisplayedValue(),
            "Yn cyd-fyw fel cwpl o'r blaen"
        );
        welshMap.put(
            ApplicantRelationshipEnum.bfGfOrPartnerNotLivedTogether.getDisplayedValue(),
            "Cariad neu bartner nad yw'n byw gydag ef/hi"
        );
        welshMap.put(
            ApplicantRelationshipEnum.formerBfGfOrPartnerNotLivedTogether.getDisplayedValue(),
            "Cariad neu bartner o'r blaen nad yw wedi byw gydag ef/hi"
        );
        welshMap.put(
            ApplicantRelationshipEnum.noneOfTheAbove.getDisplayedValue(),
            "Dim un o'r uchod"
        );

        /**
         * Relationship to respondent. - Question followed None of the above.
         */
        welshMap.put(ApplicantRelationshipOptionsEnum.father.getDisplayedValue(), "Tad");
        welshMap.put(ApplicantRelationshipOptionsEnum.mother.getDisplayedValue(), "Mam");
        welshMap.put(ApplicantRelationshipOptionsEnum.son.getDisplayedValue(), "Mab");
        welshMap.put(ApplicantRelationshipOptionsEnum.daughter.getDisplayedValue(), "Merch");
        welshMap.put(ApplicantRelationshipOptionsEnum.brother.getDisplayedValue(), "Brawd");
        welshMap.put(ApplicantRelationshipOptionsEnum.sister.getDisplayedValue(), "Chwaer");
        welshMap.put(ApplicantRelationshipOptionsEnum.grandfather.getDisplayedValue(), "Taid/Tad-cu");
        welshMap.put(ApplicantRelationshipOptionsEnum.grandmother.getDisplayedValue(), "Nain/Mam-gu");
        welshMap.put(ApplicantRelationshipOptionsEnum.uncle.getDisplayedValue(), "Ewythr");
        welshMap.put(ApplicantRelationshipOptionsEnum.aunt.getDisplayedValue(), "Modryb");
        welshMap.put(ApplicantRelationshipOptionsEnum.nephew.getDisplayedValue(), "Nai");
        welshMap.put(ApplicantRelationshipOptionsEnum.niece.getDisplayedValue(), "Nith");
        welshMap.put(ApplicantRelationshipOptionsEnum.cousin.getDisplayedValue(), "Cefnder/Cyfnither");
        welshMap.put(ApplicantRelationshipOptionsEnum.other.getDisplayedValue(), WELSH_OTHER);

        /**
         * Applicant's Family 2 questions with answer yes no is missing in execel.
         */

        /**
         * Respondent's Behaviour.
         */
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1.getDisplayedValue(),
            "Bod yn dreisgar neu fygythiol tuag ato ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_2.getDisplayedValue(),
            "Bygwth neu aflonyddu arno ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_3.getDisplayedValue(),
            "Postio neu gyhoeddi unrhyw beth amdano ef/hi mewn print neu'n ddigidol"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_4.getDisplayedValue(),
            "Cysylltu ag ef/hi yn uniongyrchol"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_5.getDisplayedValue(),
            "Achosi difrod i'w eiddo ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_6.getDisplayedValue(),
            "Achosi difrod i'w gartref ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_7.getDisplayedValue(),
            "Mynd at ei gartref ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_8.getDisplayedValue(),
            "Mynd yn agos i'w gartref ef/hi"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_9.getDisplayedValue(),
            "Mynd yn agos i'w weithle ef/hi"
        );

        welshMap.put(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1.getDisplayedValue(),
            "Bod yn dreisgar neu fygythiol tuag at y plentyn neu blant"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_2.getDisplayedValue(),
            "Bygwth neu aflonyddu ar y plentyn neu blant"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_3.getDisplayedValue(),
            "Postio neu gyhoeddi unrhyw beth am y plentyn neu blant mewn print neu'n ddigidol"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_4.getDisplayedValue(),
            "Cysylltu â'r plentyn neu blant yn uniongyrchol heb gydsyniad y ceisydd"
        );
        welshMap.put(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_5.getDisplayedValue(),
            "Mynd at neu'n agos i ysgol y plentyn neu blant"
        );

        /**
         * The home.
         */
        welshMap.put(PeopleLivingAtThisAddressEnum.applicant.getDisplayedValue(), "Y ceisydd");
        welshMap.put(PeopleLivingAtThisAddressEnum.respondent.getDisplayedValue(), "Yr atebydd");
        welshMap.put(PeopleLivingAtThisAddressEnum.applicantChildren.getDisplayedValue(),
                     "Plentyn neu blant y ceisydd");
        welshMap.put(PeopleLivingAtThisAddressEnum.someoneElse.getDisplayedValue(), "Rhywun arall - noder");
        welshMap.put(YesNoBothEnum.yesBothOfThem.getDisplayedValue(), "Ydynt, y ddau ohonynt");
        welshMap.put(YesNoBothEnum.yesApplicant.getDisplayedValue(), "Ydy, y ceisydd");
        welshMap.put(YesNoBothEnum.yesRespondent.getDisplayedValue(), "Ydy, yr atebydd");
        welshMap.put(YesNoBothEnum.No.getDisplayedValue(), WELSH_NO);
        welshMap.put("doAnyChildrenLiveAtAddress_Yes", WELSH_YES_OES);
        welshMap.put("doAnyChildrenLiveAtAddress_No", WELSH_NO_NAC_OES);
        welshMap.put("keepChildrenInfoConfidential_Yes", WELSH_YES_OES);
        welshMap.put("isRespondentResponsibleForChild_Yes", WELSH_YES_OES);
        welshMap.put("keepChildrenInfoConfidential_No", WELSH_NO_NAC_OES);
        welshMap.put("isRespondentResponsibleForChild_No", WELSH_NO_NAC_OES);
        welshMap.put("isPropertyAdapted_Yes", WELSH_YES_OES);
        welshMap.put("isPropertyAdapted_No", WELSH_NO_NAC_OES);
        welshMap.put("isThereMortgageOnProperty_Yes", WELSH_YES_OES);
        welshMap.put("isThereMortgageOnProperty_No", WELSH_NO_NAC_OES);
        welshMap.put(MortgageNamedAfterEnum.applicant.getDisplayedValue(), "Y ceisydd");
        welshMap.put(MortgageNamedAfterEnum.respondent.getDisplayedValue(), "Yr atebydd");
        welshMap.put(MortgageNamedAfterEnum.someoneElse.getDisplayedValue(), "Rhywun arall - noder");
        welshMap.put("doesApplicantHaveHomeRights_Yes", WELSH_YES_OES);
        welshMap.put("doesApplicantHaveHomeRights_No", WELSH_NO_NAC_OES);
        welshMap.put(LivingSituationEnum.ableToStayInHome.getDisplayedValue(),
                     "Mae'r ceisydd am allu aros yn ei gartref");
        welshMap.put(LivingSituationEnum.ableToReturnHome.getDisplayedValue(),
                     "Mae'r ceisydd am allu dychwelyd i'w gartref");
        welshMap.put(
            LivingSituationEnum.restrictFromEnteringHome.getDisplayedValue(),
            "Nid yw'r ceisydd am i'r atebydd allu dod i mewn i'r cartref"
        );
        welshMap.put(
            LivingSituationEnum.awayFromHome.getDisplayedValue(),
            "Mae'r ceisydd am gadw'r atebydd i ffwrdd o'r ardal sydd o gwmpas ei gartref"
        );
        welshMap.put(
            LivingSituationEnum.limitRespondentInHome.getDisplayedValue(),
            "Mae'r ceisydd am gyfyngu ar ble mae'r atebydd yn gallu mynd yn ei gartref"
        );
        welshMap.put(
            FamilyHomeEnum.payForRepairs.getDisplayedValue(),
            "Mae ar y ceisydd angen i'r atebydd dalu am neu gyfrannu at gostau trwsio neu gynnal y cartref"
        );
        welshMap.put(
            FamilyHomeEnum.payOrContributeRent.getDisplayedValue(),
            "Mae ar y ceisydd angen i'r atebydd dalu am neu gyfrannu at y rhent neu'r morgais"
        );
        welshMap.put(
            FamilyHomeEnum.useHouseholdContents.getDisplayedValue(),
            "Mae angen i'r ceisydd gael y defnydd o'r dodrefn neu gynnwys arall y cartref"
        );

        /**
         * Other Proceedings. fl401OtherProceedingDetails.
         */
        welshMap.put("hasPrevOrOngoingOtherProceeding_Yes", WELSH_YES_OES);
        welshMap.put("hasPrevOrOngoingOtherProceeding_No", WELSH_NO_NAC_OES);

        /**
         * Attending the hearing.
         */
        welshMap.put("isWelshNeeded_yes", "Bydd");
        welshMap.put("isWelshNeeded_no", WELSH_NO_NA_FYDD);
        welshMap.put(SpokenOrWrittenWelshEnum.spoken.getDisplayedValue(), "Byddant eisiau siarad Cymraeg");
        welshMap.put(
            SpokenOrWrittenWelshEnum.written.getDisplayedValue(),
            "Byddant eisiau darllen ac ysgrifennu yn Gymraeg"
        );
        welshMap.put("isInterpreterNeeded_yes", "Ydw");
        welshMap.put("isInterpreterNeeded_no", WELSH_NO_NAC_YDW);
        welshMap.put(PartyEnum.applicant.getDisplayedValue(), WELSH_APPLICANT);
        welshMap.put(PartyEnum.respondent.getDisplayedValue(), WELSH_RESPONDENT);
        welshMap.put(PartyEnum.other.getDisplayedValue(), "Pobl eraill yn yr achos");
        welshMap.put("isDisabilityPresent_Yes", WELSH_YES_OES);
        welshMap.put("isDisabilityPresent_No", WELSH_NO_NAC_OES);
        welshMap.put("isSpecialArrangementsRequired_Yes", "Bydd");
        welshMap.put("isSpecialArrangementsRequired_No", WELSH_NO_NA_FYDD);

        /**
         * Welsh language requirement.
         */
        welshMap.put("welshLanguageRequirement_Yes", WELSH_YES_OES);
        welshMap.put("welshLanguageRequirement_No", WELSH_NO_NAC_OES);
        welshMap.put(LanguagePreference.english.getDisplayedValue(), "Saesneg");
        welshMap.put(LanguagePreference.welsh.getDisplayedValue(), "Cymraeg");
        welshMap.put("welshLanguageRequirementApplicationNeedEnglish_Yes", WELSH_YES_OES);
        welshMap.put("welshLanguageRequirementApplicationNeedEnglish_No", WELSH_NO_NAC_OES);
        welshMap.put("languageRequirementApplicationNeedWelsh_Yes", WELSH_YES_OES);
        welshMap.put("languageRequirementApplicationNeedWelsh_No", WELSH_NO_NAC_OES);

        return welshMap;
    }
}
