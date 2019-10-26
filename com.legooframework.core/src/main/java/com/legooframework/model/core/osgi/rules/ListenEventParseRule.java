package com.legooframework.model.core.osgi.rules;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.osgi.DefaultBundleBuilder;
import com.legooframework.model.core.utils.AttributesUtil;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Optional;

class ListenEventParseRule extends BaseParseRule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        if (StringUtils.equals("listen", name)) {
            Optional<String> global_channel = AttributesUtil.getIfPresent(attributes, "channel");
            getDigester().push("channel", global_channel);
        } else if (StringUtils.equals("event", name)) {
            String event_name = AttributesUtil.getValue(name, attributes, "name");
            Optional<String> global_channel = getDigester().peek("channel");
            Optional<String> custom_channel = AttributesUtil.getIfPresent(attributes, "channel");
            String channl_name = custom_channel.orElseGet(() -> global_channel.orElse(null));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(channl_name),
                    "事件 %s 对应的 通道不可以为空", event_name);
            DefaultBundleBuilder builder = getDigester().peek();
            builder.setListenEvent(event_name, channl_name);
        }

    }

    @Override
    public void end(String namespace, String name) throws Exception {
        if (StringUtils.equals("listen", name)) {
            getDigester().pop("channel");
        }
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"model/evevts/listen", "model/evevts/listen/event"};
    }
}
