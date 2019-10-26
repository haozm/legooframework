package com.legooframework.model.upload.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;

public abstract class QiniuClientUtils {

    private static final String PUBLIC_KEY = ConfigUtil.getConfig("publicKey");

    private static final String SECRET_KEY = ConfigUtil.getConfig("privateKey");

    private static final String BUCKETS_URL = ConfigUtil.getConfig("backets.url");

    private static final String DOMAIN_URL = ConfigUtil.getConfig("domain.url");

    private static Gson gson = new Gson();
    private static Type GSON_TYPE = new TypeToken<List<String>>() {
    }.getType();

    private static Mac createMac() {
        byte[] sk = StringUtils.utf8Bytes(SECRET_KEY);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sk, "HmacSHA1");
        Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(secretKeySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return mac;
    }

    private static String signRequest(String urlString, byte[] body, String contentType) {
        URI uri = URI.create(urlString);
        String path = uri.getRawPath();
        String query = uri.getRawQuery();

        Mac mac = createMac();

        mac.update(StringUtils.utf8Bytes(path));

        if (query != null && query.length() != 0) {
            mac.update((byte) ('?'));
            mac.update(StringUtils.utf8Bytes(query));
        }
        mac.update((byte) '\n');
        if (body != null && Client.FormMime.equalsIgnoreCase(contentType)) {
            mac.update(body);
        }
        return String.format("%s:%s", PUBLIC_KEY, UrlSafeBase64.encodeToString(mac.doFinal()));
    }

    public static List<String> getBuckets() throws Exception {
        OkHttpClient client = new OkHttpClient();
        String authorization = signRequest(BUCKETS_URL, null, "application/x-www-form-urlencoded");
        Request request = new Request.Builder().url(BUCKETS_URL)
                .header("Authorization", "QBox " + authorization)
                .addHeader("X-Reqid", UUID.randomUUID().toString())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        Preconditions.checkState(response.isSuccessful(), "请求返回异常...");
        String list_content_str = StringUtils.utf8String(response.body().bytes());
        return gson.fromJson(list_content_str, GSON_TYPE);
    }

    public static List<String> getDomain(String bucketName) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("%s?tbl=%s", DOMAIN_URL, bucketName);
        String authorization = signRequest(url, null, "application/x-www-form-urlencoded");
        Request request = new Request.Builder().url(url)
                .header("Authorization", "QBox " + authorization)
                .addHeader("X-Reqid", UUID.randomUUID().toString())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        Preconditions.checkState(response.isSuccessful(), "请求返回异常...");
        String list_content_str = StringUtils.utf8String(response.body().bytes());
        return gson.fromJson(list_content_str, GSON_TYPE);
    }

    public static String getDomainForCsosm(String bucketName) throws Exception {
        List<String> list = getDomain(bucketName);
        String domainSuffix = ".cdn.csosm.com";
        String result = null;
        if (list.isEmpty())
            return result;
        for (String domain : list) {
            if (domain.length() <= domainSuffix.length())
                continue;
            if (domain.contains(domainSuffix)) {
                result = domain;
                break;
            }
        }
        if (Strings.isNullOrEmpty(result))
            result = list.get(0);
        return result;
    }

    public static void createBucket(String bucketName) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("http://rs.qiniu.com/mkbucketv2/%s/region/z0",
                Base64.encodeBase64String(bucketName.getBytes()));
        String authorization = signRequest(url, null, "application/x-www-form-urlencoded");
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder().url(url)
                .header("Authorization", "QBox " + authorization)
                .addHeader("X-Reqid", UUID.randomUUID().toString())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        Preconditions.checkState(response.isSuccessful(), "请求返回异常...");
    }

    public static void deleteBucket(String bucketName) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("http://rs.qiniu.com/drop/%s", bucketName);
        String authorization = signRequest(url, null, "application/x-www-form-urlencoded");
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder().url(url)
                .header("Authorization", "QBox " + authorization)
                .addHeader("X-Reqid", UUID.randomUUID().toString())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        Preconditions.checkState(response.isSuccessful(), "请求返回异常...");
    }

}
