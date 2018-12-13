package com.legooframework.model.imchat.dto;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.legooframework.model.imchat.entity.ChatKeywordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordsDto {

    private static final Logger logger = LoggerFactory.getLogger(KeywordsDto.class);

    private static Joiner joiner = Joiner.on('|');

    private final Long tenantId;

    private Set<String> keywords;

    private Pattern pattern;

    public KeywordsDto(Long tenantId, List<ChatKeywordEntity> keywordEntities) {
        this.tenantId = tenantId;
        this.keywords = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(keywordEntities)) {
            keywordEntities.stream().filter(ChatKeywordEntity::isEnabled)
                    .forEach(x -> this.keywords.add(x.getKeywords()));
            pattern = Pattern.compile(joiner.join(keywords));
        }
    }

    private boolean exitsKeywords() {
        return CollectionUtils.isNotEmpty(keywords);
    }

    public Optional<List<String>> matcher(String text) {
        if (Strings.isNullOrEmpty(text) || !exitsKeywords()) return Optional.empty();
        Matcher matcher = pattern.matcher(text);
        boolean matched = matcher.find();
        if (matched && logger.isTraceEnabled())
            logger.trace(String.format("[%s] matched keys [%s]", text, joiner.join(keywords)));

        if (matched) {
            List<String> list = Lists.newArrayList();
            keywords.forEach(x -> {
                if (StringUtils.contains(text, x)) list.add(x);
            });
            return Optional.of(list);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tenantId", tenantId)
                .add("keywords", keywords)
                .toString();
    }
}
