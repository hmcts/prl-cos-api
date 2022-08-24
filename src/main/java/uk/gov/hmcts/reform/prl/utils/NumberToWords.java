package uk.gov.hmcts.reform.prl.utils;

public class NumberToWords {
    private static final String[] NUMBERS = {
        "first",
        "second",
        "third",
        "fourth",
        "fifth",
        "sixth",
        "seventh",
        "eighth",
        "nineth",
        "tenth",
        "eleventh",
        "twelveth",
        "thirteenth",
        "fourteenth",
        "fifteenth",
        "sixteenth",
        "seventeenth",
        "eighteenth",
        "nineteenth",
        "twentieth"
    };

    public static String convertNumberToWords(int number) {
        return NUMBERS[number];
    }
}
