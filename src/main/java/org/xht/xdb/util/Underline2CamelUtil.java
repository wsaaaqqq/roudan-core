package org.xht.xdb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 驼峰法-下划线互转
 */
@SuppressWarnings({"unused"})
public class Underline2CamelUtil {

    /**
     * 下划线转驼峰法
     *
     * @param line       源字符串
     * @param smallCamel 大小驼峰,是否为小驼峰
     * @return 转换后的字符串
     */
    public static String underline2Camel(String line, boolean smallCamel) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        if (!line.contains("_")) {
            if (startSmallCharacter(line)) {
                return line;
            } else {
                return line.toLowerCase(Locale.ROOT);
            }
        }
        char[] chars = line.toCharArray();
        int len = chars.length;
        List<Character> list = new ArrayList<>();
        char start = chars[0];
        if (smallCamel) {
            list.add(Character.toLowerCase(start));
        } else {
            list.add(Character.toUpperCase(start));
        }
        addNext(list, chars, 1, start, len);
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : list) {
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }

    private static String firstCharacter2Small(String line) {
        return Character.toLowerCase(line.charAt(0)) + line.substring(1);
    }

    public static boolean startSmallCharacter(String line) {
        return Character.isLowerCase(line.charAt(0));
    }

    private static void addNext(List<Character> list, char[] chars, int idx, char pre, int maxLen) {
        if (idx < maxLen) {
            char current = chars[idx];
            if ('_' == pre) {
                if ('_' == current) {
                    idx++;
                    addNext(list, chars, idx, current, maxLen);
                } else {
                    current = Character.toUpperCase(current);
                    list.add(current);
                    idx++;
                    addNext(list, chars, idx, current, maxLen);
                }
            } else {
                if ('_' == current) {
                    idx++;
                    addNext(list, chars, idx, current, maxLen);
                } else {
                    current = Character.toLowerCase(current);
                    list.add(current);
                    idx++;
                    addNext(list, chars, idx, current, maxLen);
                }
            }
        }
    }

    /**
     * 驼峰法转下划线
     *
     * @param line 源字符串
     * @return 转换后的字符串
     */
    public static String camel2Underline(String line, boolean... upperFormat) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        if (upperFormat == null) upperFormat = new boolean[]{true};
        if (upperFormat[0]) {
            return camel2UnderlineNoUpperLowerCase(line).toUpperCase(Locale.ROOT);
        } else {
            return camel2UnderlineNoUpperLowerCase(line).toLowerCase(Locale.ROOT);
        }
    }

    public static String camel2UnderlineNoUpperLowerCase(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        if (line.length() == 1) return line;
        char char1 = line.charAt(0);
        char char2 = line.charAt(1);
        if (line.length() == 2) {
            if (Character.isDigit(char1)) {
                if (Character.isDigit(char2)) {
                    return line;
                } else {
                    return String.format("%s_%s", char1, char2);
                }
            } else {
                if (Character.isLowerCase(char1)) {
                    if (Character.isDigit(char2)) {
                        return String.format("%s_%s", char1, char2);
                    } else if (Character.isUpperCase(char2)) {
                        return String.format("%s_%s", char1, char2);
                    } else {
                        return String.format("%s%s", char1, char2);
                    }
                } else if (Character.isUpperCase(char1)) {
                    if (Character.isDigit(char2)) {
                        return String.format("%s_%s", char1, char2);
                    } else {
                        return String.format("%s%s", char1, char2);
                    }
                }
            }
        }
        String next = line.substring(1);
        String next_result = camel2UnderlineNoUpperLowerCase(next);
        if (next_result.isEmpty()) return char1 + "";
        char next_char1 = next_result.charAt(0);
        if (Character.isDigit(char1)) {
            if (Character.isDigit(next_char1)) {
                return String.format("%s%s", char1, next_result);
            } else {
                return String.format("%s_%s", char1, next_result);
            }
        } else if (Character.isLowerCase(char1)) {
            if (Character.isDigit(next_char1)) {
                return String.format("%s_%s", char1, next_result);
            } else if (Character.isUpperCase(next_char1)) {
                return String.format("%s_%s", char1, next_result);
            } else {
                return String.format("%s%s", char1, next_result);
            }
        } else if (Character.isUpperCase(char1)) {
            if (Character.isDigit(next_char1)) {
                return String.format("%s_%s", char1, next_result);
            } else {
                return String.format("%s%s", char1, next_result);
            }
        }
        return String.format("%s%s", char1, next_result);
    }

    public static void main(String[] args) {
        boolean f = true;
//        System.out.println(Character.isUpperCase('a'));
//        System.out.println(Character.isUpperCase('1'));
//        System.out.println(Character.isUpperCase('A'));
//        System.out.println(Character.isUpperCase('_'));
//        System.out.println("----");
//        System.out.println(Character.isLowerCase('a'));
//        System.out.println(Character.isLowerCase('1'));
//        System.out.println(Character.isLowerCase('A'));
//        System.out.println(Character.isLowerCase('_'));
//        System.out.println(Underline2CamelUtil.underline2Camel("RABC_1_A_23_ID_abc", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("R_1_ABC_1_A_23_ID", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("1_RABC_1_A_23_ID_", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("123_RABC_1_A_23_ID_", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("_1_RABC_1_A_23_ID_", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("__1_RABC_1_A_23_ID_", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("__1_RABC_1_AdA_23_ID_", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("__1_RABC_1_AdA_id_23_ID_", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("rabc1a23Id", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("r1abc1a23Id", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("r1abDc1a23Id", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("1rabc1a23Id_", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("123rabc1a23Id_", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("_1rabc1a23Id_", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("__1rabc1a23Id_", f));
//        System.out.println(Underline2CamelUtil.camel2Underline( f));
//        System.out.println(Underline2CamelUtil.camel2Underline("ABC2", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("obj_Id", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("obj_ID", f));
//        System.out.println(Underline2CamelUtil.camel2Underline("obj_ID_iD_Id_id", f));
//
//        System.out.println(Underline2CamelUtil.underline2Camel("ABC2", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("obj_Id", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("obj_ID", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("obj_ID_iD_Id_id", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("MC", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("name2", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("MaC2", f));
//        System.out.println(Underline2CamelUtil.underline2Camel("maC2", f));

        test_camel2Underline("MC");
        test_camel2Underline("name2");
        test_camel2Underline("MCa2");
        test_camel2Underline("MaC2");
        test_camel2Underline("maC2");
        test_camel2Underline("SBID");
        test_camel2Underline("SB2ID");
        test_camel2Underline("SBId");
        test_camel2Underline("sbId");
        test_camel2Underline("sb_Id");
        test_camel2Underline("sB_Id");
        test_camel2Underline("sB_id");
        test_camel2Underline("sB_iD");
        test_camel2Underline("SB2ID");
        test_camel2Underline("createUser");
        test_camel2Underline("serverName61850");
        test_camel2Underline("sfzc61850");
    }

    private static void test_camel2Underline(String from) {
        System.out.printf("%s   %s   %s%n", "to underline", from, Underline2CamelUtil.camel2Underline(from, true));
    }

    private static void test_underline2Camel(String method, String from) {
        System.out.println(from + "   to camel   " + Underline2CamelUtil.underline2Camel(from, true));
    }

    public static boolean isSmallCamelCase(String text) {
        return text != null && text.equals(underline2Camel(text, true));
    }
}
