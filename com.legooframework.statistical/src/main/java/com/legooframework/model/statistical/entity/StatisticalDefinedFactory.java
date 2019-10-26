package com.legooframework.model.statistical.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.config.FileReloadSupport;
import com.legooframework.model.statistical.entity.rules.StatisticalEntityBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatisticalDefinedFactory extends FileReloadSupport<File> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalDefinedFactory.class);

    private final List<StatisticalEntity> statisticals;
    private final RulesModule rulesModule;

    StatisticalDefinedFactory(List<String> patterns, RulesModule rulesModule) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.statisticals = Lists.newArrayList();
    }

    public List<Map<String, Object>> getSummaryMetas(Collection<String> summeryIds) {
        List<Map<String, Object>> summaryMetas = Lists.newArrayList();
        for (String $it : summeryIds) {
            String[] args = StringUtils.split($it, '.');
            final Optional<StatisticalEntity> statistical = findById(args[0]);
            if (!statistical.isPresent()) continue;
            statistical.get().getSummaryMetaIfExits(args[1]).ifPresent(x -> {
                summaryMetas.add(x.toViewMap(statistical.get()));
            });
        }
        return summaryMetas;
    }

    // summnerId:sql
    public Optional<List<String>> getSummerySqls(Collection<String> summeryIds) {
        List<String> list = Lists.newArrayList();
        summeryIds.forEach(fullId -> {
            String[] args = StringUtils.split(fullId, '.');
            Optional<StatisticalEntity> statistical = findById(args[0]);
            statistical.ifPresent(sc -> sc.getSummaryMetaIfExits(args[1]).ifPresent(sm -> {
                list.add(String.format("%s:%s", fullId, sm.getSql()));
            }));
        });
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

    public Optional<String> getSubSummerySql(String sid) {
        Optional<StatisticalEntity> statistical = findById(sid);
        return statistical.map(StatisticalEntity::getSubSummarySql);
    }

    public Optional<Map<String, Object>> getSubSummaryMeta(String subId) {
        String[] args = StringUtils.split(subId, '.');
        Optional<StatisticalEntity> statistical = findById(args[0]);
        if (!statistical.isPresent()) return Optional.empty();
        Optional<SummaryMetaEntity> exits = statistical.get().getSubSummaryMeta();
        return exits.map(x -> x.toViewMap(statistical.get()));
    }

    public Optional<String> getDetailSql(String sid, String detailId) {
        Optional<StatisticalEntity> statistical = findById(sid);
        if (!statistical.isPresent()) return Optional.empty();
        return statistical.get().getTableIfExits(detailId).map(TableMetaEntity::getSql);
    }

    Optional<Map<String, Object>> getEchartById(String echartId) {
        String[] args = StringUtils.split(echartId, '.');
        final Optional<StatisticalEntity> statistical = findById(args[0]);
        if (!statistical.isPresent()) return Optional.empty();
        Optional<EchartMetaEntity> echartMeta = statistical.get().findEchartMetaById(args[1]);
        return echartMeta.map(echartMetaEntity -> echartMetaEntity.toViewMap(statistical.get()));
    }

    Optional<Map<String, Object>> getTableById(String tableFullName) {
        String[] args = StringUtils.split(tableFullName, '.');
        final Optional<StatisticalEntity> statistical = findById(args[0]);
        if (!statistical.isPresent()) return Optional.empty();
        return statistical.get().getTableMapIfExits(args[1]);
    }

    Optional<StatisticalEntity> findById(String id) {
        if (CollectionUtils.isEmpty(statisticals)) return Optional.empty();
        return statisticals.stream().filter(x -> StringUtils.equals(x.getId(), id)).findFirst();
    }

    @Override
    public void addConfig(File file, File config) {
        super.addConfig(file, config);
    }

    @Override
    protected Optional<File> parseFile(File file) {
        if (!isSupported(file)) return Optional.empty();
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        List<StatisticalEntityBuilder> builders = Lists.newArrayList();
        boolean finished = false;
        try {
            digester.push(builders);
            digester.parse(file);
            finished = true;
            if (logger.isInfoEnabled())
                logger.info(String.format("parse file=%s finshed", file));
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
        } finally {
            digester.clear();
        }

        if (finished) {
            List<StatisticalEntity> _temps = builders.stream().map(StatisticalEntityBuilder::building)
                    .collect(Collectors.toList());
            this.statisticals.clear();
            this.statisticals.addAll(_temps);
            if (logger.isDebugEnabled())
                logger.debug(String.format("Resufues StatisticalDefinedFactory By file=%s ,size = %s finshed", file,
                        _temps.size()));
        }
        return Optional.of(file);
    }
}
