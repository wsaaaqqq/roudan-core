package org.xht.xdb.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;

@Slf4j
public class BlobUtil {

    private static String toBinaryString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                sb.append((b >> i) & 1);
            }
        }
        return sb.toString();
    }

    public static String toBinaryString(Blob blob) {
        if (blob == null) {
            return null;
        }
        InputStream is = null;
        try {
            is = blob.getBinaryStream(); // 获取Blob对象的输入流
            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 循环读取输入流中的数据
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            // 将字节数组转换为二进制字符串
            return toBinaryString(baos.toByteArray());
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("blobToString error", e);
        } finally {
            CloseUtil.close(is); // 关闭输入流
        }
    }
}
