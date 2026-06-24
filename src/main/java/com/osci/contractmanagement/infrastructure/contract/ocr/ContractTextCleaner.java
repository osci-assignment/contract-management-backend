package com.osci.contractmanagement.infrastructure.contract.ocr;
import java.util.regex.Pattern;

public final class ContractTextCleaner {
 
    // 한글 완성형(가-힣), 한글 자모, 숫자만 허용. 그 외 문자는 공백으로 치환.
    private static final Pattern NON_ALLOWED_CHARS =
            Pattern.compile("[^\\uAC00-\\uD7A3\\u3131-\\u318E0-9]");
 
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
 
    private ContractTextCleaner() {
    }
 
    public static String clean(String rawText) {
        if (rawText == null) {
            return "";
        }
 
        String onlyKoreanAndDigits = NON_ALLOWED_CHARS.matcher(rawText).replaceAll(" ");
        return MULTIPLE_SPACES.matcher(onlyKoreanAndDigits).replaceAll(" ").trim();
    }
}