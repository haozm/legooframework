package com.legooframework.model.osgi;

import com.legooframework.model.event.LegooEvent;
import org.springframework.integration.core.MessageSelector;

import java.util.List;
import java.util.Optional;

public interface Bundle extends MessageSelector {

    String getName();

    String getVersion();

    Optional<String> getChannelByEvent(LegooEvent event);

    List<DependsItem> getDepends();

    boolean exitsDepends();

}
