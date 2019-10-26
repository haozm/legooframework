package com.csosm.module.webchat.concurrent;


import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.webchat.entity.MatchWebChatComparator;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class MatchWechatFuture implements Callable<List<WechatMatchMemberDto>> {

    private static final Logger logger = LoggerFactory.getLogger(MatchWechatFuture.class);

    private List<WechatMatchMemberDto> wechatMatchMemberDtos;
    private final MemberEntity member;

    public MatchWechatFuture(MemberEntity member, List<WechatMatchMemberDto> wechatMatchMemberDtos) {
        this.member = member;
        this.wechatMatchMemberDtos = wechatMatchMemberDtos;
    }

    @Override
    public List<WechatMatchMemberDto> call() throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("member= %s match weichat size %s", member.getName(), wechatMatchMemberDtos.size()));
        //LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        //JaccardDistance jaccardDistance = new JaccardDistance();
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        //distance(member, wechatMatchMemberDtos, jaccardDistance);
        distance(member, wechatMatchMemberDtos, jaroWinklerDistance);
        //distance(member, wechatMatchMemberDtos, levenshteinDistance);
        Collections.sort(wechatMatchMemberDtos, MatchWebChatComparator.orderDistance());
        return wechatMatchMemberDtos;
    }

    private void distance(MemberEntity member, List<WechatMatchMemberDto> mms, JaroWinklerDistance score) {
        for (WechatMatchMemberDto $mbr : mms) {
            double a = 0D, c = 0D, d = 0D, e = 0D;
            Optional<String> nameOpt = member.getName() != null ?
                    Optional.of(StringUtils.replace(member.getName(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = member.getMobilephone() != null ?
                    Optional.of(StringUtils.replace(member.getMobilephone(), " ", "")) :
                    Optional.<String>absent();
            if ($mbr.getNickName().isPresent() && nameOpt.isPresent()) {
                a = score.apply($mbr.getNickName().get(), nameOpt.get());
            }
            if ($mbr.getConRemark().isPresent() && nameOpt.isPresent()) {
                c = score.apply($mbr.getConRemark().get(), nameOpt.get());
            }
            if ($mbr.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom($mbr.getConRemark().get());
                c = Strings.isNullOrEmpty(_temp) ? 0D : score.apply(_temp, phoneOpt.get());
            }
            if (member.getMemberCard().getMemberCardNum() != null && $mbr.getConRemark().isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom($mbr.getConRemark().get());
                e = Strings.isNullOrEmpty(_temp) ? 0D : score.apply($mbr.getConRemark().get(), member.getMemberCard().getMemberCardNum());
            }
            $mbr.setJaroWinklerDistance(Doubles.max(a, c, d, e));
        }
    }

    private void distance(MemberEntity member, List<WechatMatchMemberDto> mms, LevenshteinDistance score) {
        for (WechatMatchMemberDto $mbr : mms) {
            double a = 99D, b = 99D, c = 99D, d = 99D, e = 99D;

            Optional<String> nameOpt = member.getName() != null ?
                    Optional.of(StringUtils.replace(member.getName(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = member.getMobilephone() != null ?
                    Optional.of(StringUtils.replace(member.getMobilephone(), " ", "")) :
                    Optional.<String>absent();

            if ($mbr.getNickName().isPresent() && nameOpt.isPresent()) {
                a = score.apply($mbr.getNickName().get(), nameOpt.get());
            }

            if ($mbr.getConRemark().isPresent() && nameOpt.isPresent()) {
                b = score.apply($mbr.getConRemark().get(), nameOpt.get());
            }

            if ($mbr.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom($mbr.getConRemark().get());
                c = Strings.isNullOrEmpty(_temp) ? 99D : score.apply(_temp, phoneOpt.get());
            }

//            if ($mbr.getConRemark().isPresent() && phoneOpt.isPresent()) {
//                String _temp = CharMatcher.JAVA_DIGIT.retainFrom($mbr.getConRemark().get());
//                d = Strings.isNullOrEmpty(_temp) ? 99D : score.apply(_temp, member.getPhone());
//            }

            if (member.hasMemberCard() && $mbr.getConRemark().isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom($mbr.getConRemark().get());
                e = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, member.getMemberCardNum().get());
            }

            $mbr.setLevenshteinDistance(Doubles.min(a, b, c, d, e));
        }
    }

    private void distance(MemberEntity member, List<WechatMatchMemberDto> mms, JaccardDistance score) {
        for (WechatMatchMemberDto $mbr : mms) {
            double a = 99D, c = 99D, d = 99D, e = 99D;

            Optional<String> nameOpt = member.getName() != null ?
                    Optional.of(StringUtils.replace(member.getName(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = member.getMobilephone() != null ?
                    Optional.of(StringUtils.replace(member.getMobilephone(), " ", "")) :
                    Optional.<String>absent();

            if (nameOpt.isPresent() && $mbr.getNickName().isPresent()) {
                a = score.apply($mbr.getNickName().get(), nameOpt.get());
            }

            if ($mbr.getConRemark().isPresent() && nameOpt.isPresent()) {
                c = score.apply($mbr.getConRemark().get(), nameOpt.get());
            }

            if ($mbr.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom($mbr.getConRemark().get());
                d = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, phoneOpt.get());
            }

            if (member.hasMemberCard() && $mbr.getConRemark().isPresent()) {
                e = score.apply($mbr.getConRemark().get(), member.getMemberCardNum().get());
            }
            $mbr.setJaccardDistance(Doubles.min(a, c, d, e));
        }
    }
}
