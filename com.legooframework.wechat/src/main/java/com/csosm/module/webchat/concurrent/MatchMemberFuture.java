package com.csosm.module.webchat.concurrent;

import com.csosm.module.member.entity.Member4MatchWebChatDto;
import com.csosm.module.webchat.entity.Distance;
import com.csosm.module.webchat.entity.MatchWebChatComparator;

import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class MatchMemberFuture implements Callable<Multimap<WebChatUserEntity, Member4MatchWebChatDto>> {

    private static final Logger logger = LoggerFactory.getLogger(MatchMemberFuture.class);

    private List<WebChatUserEntity> webChatUser;
    private List<Member4MatchWebChatDto> members;
    private boolean single;

    public MatchMemberFuture(List<WebChatUserEntity> webChatUser, List<Member4MatchWebChatDto> members, boolean single) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(webChatUser));
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(members));
        this.webChatUser = webChatUser;
        this.members = members;
        this.single = single;
    }

    @Override
    public Multimap<WebChatUserEntity, Member4MatchWebChatDto> call() throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("webChatUser size is %s,members size %s , single = %s",
                    webChatUser.size(), members.size(), single));
        Multimap<WebChatUserEntity, Member4MatchWebChatDto> multimap = ArrayListMultimap.create();

        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        JaccardDistance jaccardDistance = new JaccardDistance();
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
        Ordering<Distance> ordering = MatchWebChatComparator.orderDistance();
        for (WebChatUserEntity $user : webChatUser) {
            if ($user.hasMember()) continue;
            List<Member4MatchWebChatDto> new_list = Lists.newArrayListWithCapacity(members.size());
            for (Member4MatchWebChatDto $dto : members) new_list.add($dto.cloneMe());
            distance($user, new_list, jaroWinklerDistance);
            // distance($user, new_list, jaccardDistance);
            // distance($user, new_list, levenshteinDistance);
            Collections.sort(new_list, ordering);
            multimap.putAll($user, new_list.size() < 10 ? new_list : new_list.subList(0, 10));
        }
        return multimap;
    }

    private void distance(WebChatUserEntity webChatUser, List<Member4MatchWebChatDto> mms, JaroWinklerDistance score) {
        for (Member4MatchWebChatDto $mbr : mms) {
            if ($mbr.isNullOrEmpty()) continue;
            Optional<String> nameOpt = $mbr.getName().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getName().get(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = $mbr.getPhone().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getPhone().get(), " ", "")) :
                    Optional.<String>absent();
            double a = 0D, b = 0D, c = 0D, d = 0D, e = 0D;
            if (webChatUser.getNickName().isPresent() && nameOpt.isPresent()) {
                a = score.apply(webChatUser.getNickName().get(), nameOpt.get());
            }
//            if (webChatUser.getNickName().isPresent() && phoneOpt.isPresent()) {
//                String _temp = CharMatcher.JAVA_DIGIT.retainFrom(webChatUser.getNickName().get());
//                b = Strings.isNullOrEmpty(_temp) ? 0D : score.apply(_temp, phoneOpt.get());
//            }

            if (webChatUser.getConRemark().isPresent() && nameOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().trimFrom(webChatUser.getConRemark().get());
                c = Strings.isNullOrEmpty(_temp) ? 0D : score.apply(_temp, nameOpt.get());
            }

            if (webChatUser.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                d = Strings.isNullOrEmpty(_temp) ? 0D : score.apply(_temp, phoneOpt.get());
            }

            if ($mbr.getMemberCardNum() != null && webChatUser.getConRemark().isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                e = Strings.isNullOrEmpty(_temp) ? 0D : score.apply($mbr.getMemberCardNum(), _temp);
            }
            $mbr.setJaroWinklerDistance(Doubles.max(a, b, c, d, e));
        }
    }

    private void distance(WebChatUserEntity webChatUser, List<Member4MatchWebChatDto> mms, LevenshteinDistance score) {
        for (Member4MatchWebChatDto $mbr : mms) {
            if ($mbr.isNullOrEmpty()) continue;
            double a = 99D, b = 99D, c = 99D, d = 99D, e = 99D;
            Optional<String> nameOpt = $mbr.getName().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getName().get(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = $mbr.getPhone().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getPhone().get(), " ", "")) :
                    Optional.<String>absent();

            if (webChatUser.getNickName().isPresent() && nameOpt.isPresent()) {
                a = score.apply(webChatUser.getNickName().get(), nameOpt.get());
            }
            if (webChatUser.getNickName().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getNickName().get());
                b = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, phoneOpt.get());
            }
            if (webChatUser.getConRemark().isPresent() && nameOpt.isPresent()) {
                c = score.apply(webChatUser.getConRemark().get(), nameOpt.get());
            }
            if (webChatUser.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                d = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, phoneOpt.get());
            }

            if ($mbr.getMemberCardNum() != null && webChatUser.getConRemark().isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                e = Strings.isNullOrEmpty(_temp) ? 100D : score.apply($mbr.getMemberCardNum(), _temp);
            }
            $mbr.setLevenshteinDistance(Doubles.min(a, b, c, d, e));
        }
    }

    private void distance(WebChatUserEntity webChatUser, List<Member4MatchWebChatDto> mms, JaccardDistance score) {
        for (Member4MatchWebChatDto $mbr : mms) {
            if ($mbr.isNullOrEmpty()) continue;
            double a = 99D, b = 99D, c = 99D, d = 99D, e = 99D;
            Optional<String> nameOpt = $mbr.getName().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getName().get(), " ", "")) :
                    Optional.<String>absent();
            Optional<String> phoneOpt = $mbr.getPhone().isPresent() ?
                    Optional.of(StringUtils.replace($mbr.getPhone().get(), " ", "")) :
                    Optional.<String>absent();

            if (webChatUser.getNickName().isPresent() && nameOpt.isPresent()) {
                a = score.apply(webChatUser.getNickName().get(), nameOpt.get());
            }

            if (webChatUser.getNickName().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getNickName().get());
                b = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, phoneOpt.get());
            }

            if (webChatUser.getConRemark().isPresent() && nameOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().trimFrom(webChatUser.getConRemark().get());
                c = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, nameOpt.get());
            }

            if (webChatUser.getConRemark().isPresent() && phoneOpt.isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                d = Strings.isNullOrEmpty(_temp) ? 100D : score.apply(_temp, phoneOpt.get());
            }
            if ($mbr.getMemberCardNum() != null && webChatUser.getConRemark().isPresent()) {
                String _temp = CharMatcher.javaDigit().retainFrom(webChatUser.getConRemark().get());
                e = Strings.isNullOrEmpty(_temp) ? 100D : score.apply($mbr.getMemberCardNum(), _temp);
            }
            $mbr.setJaccardDistance(Doubles.min(a, b, c, d, e));
        }
    }

}
