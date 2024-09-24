package uk.gov.hmcts.reform.prl.config.templates;


import java.util.Map;

public class Templates {
    //Sonar fix
    private Templates() {
    }

    //Docmosis templates
    public static final String PRL_LET_ENG_AP1 = "PRL-LET-ENG-FL401-AP1.docx";
    public static final String PRL_LET_ENG_AP2 = "PRL-LET-ENG-FL401-AP2.docx";
    public static final String PRL_LET_ENG_AP6 = "PRL-LET-ENG-C100-AP6.docx";
    public static final String PRL_LET_ENG_AP7 = "PRL-LET-ENG-C100-AP7.docx";
    public static final String PRL_LET_ENG_AP8 = "PRL-LET-ENG-C100-AP8.docx";

    public static final String PRL_LET_WEL_AP1 = "PRL-LET-WEL-FL401-AP1.docx";
    public static final String PRL_LET_WEL_AP2 = "PRL-LET-WEL-FL401-AP2.docx";
    public static final String PRL_LET_WEL_AP6 = "PRL-LET-WEL-C100-AP6.docx";
    public static final String PRL_LET_WEL_AP7 = "PRL-LET-WEL-C100-AP7.docx";
    public static final String PRL_LET_WEL_AP8 = "PRL-LET-WEL-C100-AP8.docx";

    public static final String PRL_LET_ENG_FL401_RE1 = "PRL-LET-ENG-FL401-RE1.docx";
    public static final String PRL_LET_ENG_FL401_RE2 = "PRL-LET-ENG-FL401-RE2.docx";
    public static final String PRL_LET_ENG_FL401_RE3 = "PRL-LET-ENG-FL401-RE3.docx";
    public static final String PRL_LET_ENG_FL401_RE4 = "PRL-LET-ENG-FL401-RE4.docx";
    public static final String PRL_LET_ENG_RE5 = "PRL-LET-ENG-C100-RE5.docx";
    public static final String PRL_LET_ENG_C100_RE6 = "PRL-LET-ENG-C100-RE6.docx";

    public static final String PRL_LET_ENG_C100_RE7 = "PRL-LET-ENG-C100-RE7.docx";
    public static final String PRL_LET_WEL_C100_RE7 = "PRL-LET-WEL-C100-RE7.docx";
    public static final String PRL_LET_ENG_FL401_RE8 = "PRL-LET-ENG-FL401-RE8.docx";
    public static final String PRL_LET_WEL_FL401_RE8 = "PRL-LET-WEL-FL401-RE8.docx";
    public static final String PRL_LTR_ENG_C100_FM5 = "PRL-LTR-ENG-C100-FM5.docx";

    public static final String PRL_LET_ENG_LIST_WITHOUT_NOTICE = "PRL-LET-ENG-LIST-WITHOUT-NOTICE.docx";

    public static final String PRL_LET_ENG_C100_AP13 = "PRL-LET-ENG-C100-AP13.docx";
    public static final String PRL_LET_WEL_C100_AP13 = "PRL-LET-WEL-C100-AP13.docx";
    public static final String PRL_LET_ENG_C100_AP14 = "PRL-LET-ENG-C100-AP14.docx";
    public static final String PRL_LET_WEL_C100_AP14 = "PRL-LET-WEL-C100-AP14.docx";
    public static final String PRL_LET_ENG_C100_AP15 = "PRL-LET-ENG-C100-AP15.docx";
    public static final String PRL_LET_WEL_C100_AP15 = "PRL-LET-WEL-C100-AP15.docx";

    public static final String RE7_HINT = "RE7";
    public static final String RE8_HINT = "RE8";
    public static final String AP13_HINT = "AP13";
    public static final String AP14_HINT = "AP14";
    public static final String AP15_HINT = "AP15";
    private static final Map<String, String> welshTemplateMapper = Map.ofEntries(
        Map.entry(PRL_LET_ENG_AP1, PRL_LET_WEL_AP1),
        Map.entry(PRL_LET_ENG_AP2, PRL_LET_WEL_AP2),
        Map.entry(PRL_LET_ENG_AP6, PRL_LET_WEL_AP6),
        Map.entry(PRL_LET_ENG_AP7, PRL_LET_WEL_AP7),
        Map.entry(PRL_LET_ENG_AP8, PRL_LET_WEL_AP8),
        Map.entry(PRL_LET_ENG_C100_RE7, PRL_LET_WEL_C100_RE7),
        Map.entry(PRL_LET_ENG_FL401_RE8, PRL_LET_WEL_FL401_RE8),
        Map.entry(PRL_LET_ENG_C100_AP13, PRL_LET_WEL_C100_AP13),
        Map.entry(PRL_LET_ENG_C100_AP14, PRL_LET_WEL_C100_AP14),
        Map.entry(PRL_LET_ENG_C100_AP15, PRL_LET_WEL_C100_AP15)
    );

    public static String getWelshTemplate(String template) {
        return welshTemplateMapper.get(template);
    }
}
