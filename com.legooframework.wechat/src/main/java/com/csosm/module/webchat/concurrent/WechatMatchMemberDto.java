package com.csosm.module.webchat.concurrent;

import com.csosm.module.webchat.entity.Distance;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.Optional;

public class WechatMatchMemberDto implements Cloneable, Distance {

    private final WebChatUserEntity webChatUser;
    private double jaccardDistance = 99D;
    private double jaroWinklerDistance = 0D;
    private double levenshteinDistance = Integer.MAX_VALUE;

    public Optional<String> getNickName() {
        return webChatUser.getNickName();
    }

    public WebChatUserEntity getWebChatUser() {
        return webChatUser;
    }

    public Optional<String> getConRemark() {
        return webChatUser.getConRemark();
    }

    public WechatMatchMemberDto(WebChatUserEntity webChatUser) {
        this.webChatUser = webChatUser;
    }

    void setJaccardDistance(double value) {
        this.jaccardDistance = value;
    }

    void setLevenshteinDistance(double value) {
        this.levenshteinDistance = value;
    }

    void setJaroWinklerDistance(double value) {
        this.jaroWinklerDistance = value;
    }

    @Override
    public double getJaccardDistance() {
        return jaccardDistance;
    }

    @Override
    public double getJaroWinklerDistance() {
        return jaroWinklerDistance;
    }

    @Override
    public double getLevenshteinDistance() {
        return levenshteinDistance;
    }

    public double getDistance() {
        return getJaroWinklerDistance();
    }

}
