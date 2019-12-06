package com.legooframework.model.hmdata.entity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RSAUtils {

    private static final Logger logger = LoggerFactory.getLogger(RSAUtils.class);

    /**
     * 使用公钥加密
     */
    public static String encryptByPublicKey(String data, String publicKey) {
        // 加密
        String str = "";
        int outNum = 2048 / 8 - 11;
        try {
            byte[] pubbyte = base64decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubbyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);
            Cipher c1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c1.init(Cipher.ENCRYPT_MODE, rsaPubKey);
            byte[] dataBytes = data.getBytes();
            int inputLen = data.getBytes().length;
            int offSet = 0;
            byte[] cache;
            byte[] bigByte = new byte[0];
            int i = 0;
            if (inputLen > outNum) {
                while (inputLen - offSet > 0) {
                    if (inputLen - offSet >= outNum) {
                        cache = subBytes(dataBytes, offSet, outNum);
                        bigByte = byteMerger(bigByte, c1.doFinal(cache));
                    } else {
                        cache = subBytes(dataBytes, offSet, inputLen - offSet);
                        bigByte = byteMerger(bigByte, c1.doFinal(cache));
                    }
                    i++;
                    offSet = i * outNum;
                }
                str = base64encode(bigByte);
            } else {
                str = base64encode(c1.doFinal(dataBytes));
            }
        } catch (Exception e) {
            logger.error("签名报文发生异常...", e);
        }
        return str;
    }

    /**
     * 截取byte数组
     *
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    /**
     * 合并byte[]数组 （不改变原数组）
     *
     * @param byte_1
     * @param byte_2
     * @return 合并后的数组
     */
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /**
     * base64解密
     *
     * @param str
     * @return byte[]
     */
    @SuppressWarnings("restriction")
    private static byte[] base64decode(String str) {
        byte[] bt = null;
        try {
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            bt = decoder.decodeBuffer(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bt;
    }

    /**
     * base64加密
     *
     * @param bstr
     * @return
     */
    @SuppressWarnings("restriction")
    private static String base64encode(byte[] bstr) {
        String str = new BASE64Encoder().encode(bstr);
        str = str.replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "");
        return str;
    }

    /**
     * 本方法使用SHA1withRSA签名算法产生签名
     *
     * @param src 签名的原字符串
     * @return String 签名的返回结果(16进制编码)。当产生签名出错的时候，返回null。
     */
    public static String signByPrivateKey(String src, String priKey) {
        try {
            Signature sigEng = Signature.getInstance("SHA1withRSA");
            byte[] pribyte = base64decode(priKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
            sigEng.initSign(privateKey);
            sigEng.update(src.getBytes());
            byte[] signature = sigEng.sign();
            return base64encode(signature);
        } catch (Exception e) {
            System.out.println("=====签名生成失败");
            return null;
        }
    }

    /**
     * 使用私钥解密
     *
     * @see
     */
    public static String decryptByPrivateKey(String data, String priKey) {
        try {
            int outNum = 2048 / 8;
            byte[] pribyte = base64decode(priKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
            Cipher c1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c1.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytes = base64decode(data);
            int inputLen = bytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > outNum) {
                    cache = c1.doFinal(bytes, offSet, outNum);
                } else {
                    cache = c1.doFinal(bytes, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * outNum;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            String res = new String(decryptedData, StandardCharsets.UTF_8);
            return unicodeToString(res);
        } catch (Exception e) {
            logger.error("解密失败", e);
            return null;
        }

    }

    /**
     * unicode转字符串
     *
     * @param str
     * @return
     */
    public static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

    /**
     * 使用公钥验证签名
     *
     * @param sign
     * @param src
     * @return
     */
    public static boolean verifyByPublicKey(String sign, String src, String pubKey) {
        try {
            Signature sigEng = Signature.getInstance("SHA1withRSA");
            byte[] pubbyte = base64decode(pubKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubbyte);
            KeyFactory fac = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);
            sigEng.initVerify(rsaPubKey);
            sigEng.update(src.getBytes());
            byte[] sign1 = base64decode(sign);
            return sigEng.verify(sign1);
        } catch (Exception e) {
            logger.error("验签失败", e);
            return false;
        }
    }

    /**
     * JSON字符串排序 升序
     *
     * @param str
     * @return
     */
    public static String sortString(String str) {
        //判断是否为多值
        if (str.contains(",")) {
            //如果为多值，去掉串的 {} ，解析为数组，再排序
            str = str.substring(1, str.length() - 1);
            String[] strs = str.split(",");
            Arrays.sort(strs);
            //调用开源包的方法，将数组整合成以","分隔的字符串，然后再加{}形成JSON格式
            str = StringUtils.join(strs, ",");
            str = "{" + str + "}";
        }
        return str;
    }
}
