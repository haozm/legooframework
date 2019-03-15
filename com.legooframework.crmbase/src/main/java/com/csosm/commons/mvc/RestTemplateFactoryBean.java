package com.csosm.commons.mvc;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateFactoryBean extends AbstractFactoryBean<RestTemplate> {
    @Override
    public Class<RestTemplate> getObjectType() {
        return RestTemplate.class;
    }

    @Override
    protected RestTemplate createInstance() throws Exception {
        SimpleClientHttpRequestFactory httpClient = new SimpleClientHttpRequestFactory();
        httpClient.setConnectTimeout(5000);
        httpClient.setReadTimeout(5000);
        return new RestTemplate(httpClient);
    }
}
