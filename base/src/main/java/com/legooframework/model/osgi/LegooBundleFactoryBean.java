package com.legooframework.model.osgi;

import com.google.common.base.Preconditions;
import com.legooframework.model.osgi.rules.RulesModule;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

public class LegooBundleFactoryBean extends AbstractFactoryBean<Bundle> {

    @Override
    public Class<Bundle> getObjectType() {
        return Bundle.class;
    }

    @Override
    protected Bundle createInstance() throws Exception {
        Preconditions.checkNotNull(resource);
        Preconditions.checkState(resource.exists(), "%s 对应的配置not exits...", resource);
        Digester digester = DigesterLoader.newLoader(new RulesModule()).newDigester();
        try {
            DefaultBundleBuilder bundle_builder = digester.parse(resource.getFile());
            Bundle bundle = bundle_builder.building();
            if (logger.isDebugEnabled())
                logger.debug(String.format("finish parse bundle-cfg: %s", bundle));
            return bundle;
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", resource.getFile()), e);
            throw new RuntimeException(e);
        } finally {
            digester.clear();
        }
    }

    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
