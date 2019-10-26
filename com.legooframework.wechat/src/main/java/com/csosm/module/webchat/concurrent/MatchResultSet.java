package com.csosm.module.webchat.concurrent;


import com.csosm.module.member.entity.Member4MatchWebChatDto;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchResultSet {

    private WebChatUserEntity webChatUser;
    private Set<Member4MatchWebChatDto> members;

    public MatchResultSet(WebChatUserEntity webChatUser) {
        this.webChatUser = webChatUser;
        this.members = Sets.newLinkedHashSet();
    }

    public WebChatUserEntity getWebChatUser() {
        return webChatUser;
    }

    public Optional<Set<Member4MatchWebChatDto>> getMembers() {
        return Optional.fromNullable(CollectionUtils.isEmpty(members) ? null : members);
    }

    public void setMembers(List<Member4MatchWebChatDto> members) {
        if (CollectionUtils.isNotEmpty(members))
            this.members.addAll(members);
    }

    public Map<String, Object> toBatchMaps() {
        Map<String, Object> resmap = getWebChatUser().toViewMap();
        List<Map<String, Object>> item = Lists.newArrayList();
        Iterator<Member4MatchWebChatDto> $cur = members.iterator();
        int index = 0;
        while ($cur.hasNext()) {
            if (index >= 2) break;
            item.add($cur.next().toMap());
            index++;
        }
        resmap.put("members", item);
        resmap.put("rowNum", item.size());
        return resmap;
    }

    public Map<String, Object> toSignleMaps() {
        Map<String, Object> all_map = Maps.newHashMap();
        all_map.putAll(getWebChatUser().toViewMap());
        List<Map<String, Object>> list = Lists.newArrayList();
        int i = 0;
        for (Member4MatchWebChatDto $mm : members) {
            if (i >= 6) break;
            list.add($mm.toMap());
            i++;
        }
        all_map.put("members", list);
        all_map.put("rowNum", list.size());
        return all_map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("webChatUser", webChatUser.toSimple())
                .add("members", members)
                .toString();
    }
}
