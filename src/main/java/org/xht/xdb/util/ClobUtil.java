package org.xht.xdb.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;

@Slf4j
public class ClobUtil {

    public static String toString(Clob clob) {
        if (clob == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Reader reader = null;
        BufferedReader br = null;
        try {
            reader = clob.getCharacterStream(); // 获取CLOB数据的字符输入流
            br = new BufferedReader(reader); // 用BufferedReader包装一下，方便按行读取
            String line;
            while ((line = br.readLine()) != null) { // 按行读取数据
                sb.append(line);
                sb.append(System.lineSeparator()); // 添加换行符保持原文本格式
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            CloseUtil.closeNotThrow(br);
            CloseUtil.closeNotThrow(reader);
        }
        return sb.toString().trim(); // 返回结果，并去除首尾多余的空白字符
    }
}
