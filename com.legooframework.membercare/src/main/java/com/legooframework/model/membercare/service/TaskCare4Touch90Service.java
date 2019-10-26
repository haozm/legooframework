package com.legooframework.model.membercare.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.core.web.TreeUtil;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.membercare.entity.*;
import com.legooframework.model.templatemgs.entity.SimpleMsgTemplateList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

import java.util.*;
import java.util.stream.Collectors;

public class TaskCare4Touch90Service extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(TaskCare4Touch90Service.class);

    void runTouch90JobByCompany(final CrmOrganizationEntity company) {
        List<CrmStoreEntity> stores = getBean(CrmStoreEntityAction.class).loadAllByCompany(company);
        if (CollectionUtils.isEmpty(stores)) {
            if (logger.isWarnEnabled())
                logger.warn(String.format("公司 %s 无可支配的门店信息,忽略....", company.getId()));
            return;
        }

        List<JobParameters> jobParameters = Lists.newArrayList();
        for (CrmStoreEntity store : stores) {
            Optional<List<TaskCareRule4Touch90Entity>> store_rule_list = getBean(TaskCareRule4Touch90EntityAction.class)
                    .loadEnabledTouch90RuleByStore(store, false);
            if (!store_rule_list.isPresent()) continue;
            store_rule_list.get().forEach(rule -> {
                Optional<JobExecution> jobExecution = getJobInstanceAction().loadLastJobExecution
                        (BusinessType.TOUCHED90.getJobName(), store.getCompanyId(), store.getId(), rule.getCategories());
                if (jobExecution.isPresent()) {
                    Touch90JobEntity touch90Job = Touch90JobEntity.getInstance(jobExecution.get().getJobParameters());
                    Optional<Touch90JobEntity> next = touch90Job.nextJobParameters(5);
                    next.ifPresent(z -> jobParameters.add(z.currentJobParameters()));
                } else {
                    Touch90JobEntity touch90Job = Touch90JobEntity.init(store, rule.getCategories());
                    jobParameters.add(touch90Job.currentJobParameters());
                }
            });
        } // end_for

        if (logger.isDebugEnabled()) {
            jobParameters.forEach(param -> logger.debug("[touch90]:" + param.toString()));
        }
        // 多线程并发执行 执行按照门店为单位
        jobParameters.forEach(params -> getLegooJobService().runJob(BusinessType.TOUCHED90, params));
    }

    /**
     * AUTO RUN 计算机任务自动执行
     */
    public void runTouch90Job() {
        LoginContextHolder.setAnonymousCtx();
        try {
            Optional<List<CrmOrganizationEntity>> com_opts = getBean(CrmOrganizationEntityAction.class).loadAllCompanys();
            com_opts.ifPresent(com -> com.forEach(this::runTouch90JobByCompany));
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 授权用户 获取公司指定的 90 节点设置树
     * 该功能有问题 需要进一步优化
     *
     * @param authenticationor 授权用户
     * @return TreeNode 授权用户
     */
    public TreeNode buildTouch90TemplateTree(Authenticationor authenticationor) {
        List<TreeNode> treeNodes = Lists.newArrayList();
        Optional<Set<String>> categories = getBean(TaskCareRule4Touch90EntityAction.class)
                .loadAll90Categories(authenticationor.getCompany());
        Optional<List<Touch90TemplateEntity>> templates = getBean(Touch90TemplateEntityAction.class)
                .loadByCompany(authenticationor.getCompany());
        categories.ifPresent(styles -> styles.forEach(style -> {
            treeNodes.add(new TreeNode(style, "0000", String.format("%s类型90服务", style), null));
        }));
        if (templates.isPresent()) {
            List<TreeNode> _temps = Lists.newArrayList(treeNodes);
            for (TreeNode _temp : _temps) {
                List<Touch90TemplateEntity> sub_list = templates.get().stream()
                        .filter(x -> StringUtils.equals((String) _temp.getId(), x.getCategories())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sub_list)) {
                    sub_list.forEach(temp -> treeNodes.add(new TreeNode(temp.getId(), temp.getCategories(), temp.getNodeName(), null)));
                }
            }
        }
        final TreeNode root = new TreeNode("0000", "0000", "90模板设定", null);
        TreeUtil.buildTree(root, treeNodes);
        return root;
    }

    /**
     * 自動執行90 自動節點任務，沒有入參
     * 默默的服務于後臺程序
     */
    public void autoRunTouch90Jobs() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<List<UpcomingTaskDetailEntity>> undo_deitals = getBean(UpcomingTaskDetailEntityAction.class)
                    .loadAutoRunJobDetails(BusinessType.TOUCHED90);
            if (!undo_deitals.isPresent()) {
                if (logger.isInfoEnabled())
                    logger.info("loadAutoRunJobDetails(BusinessType.TOUCHED90) 暫無可執行的90自動節任務...");
                return;
            }
            ArrayListMultimap<String, UpcomingTaskDetailEntity> multimap = ArrayListMultimap.create();
            undo_deitals.get().forEach(x -> multimap.put(x.getRuleId(), x));
            List<UpcomingTaskAutoRunDto> autoRunJobList = Lists.newArrayList();
            List<UpcomingTaskDetailEntity> error_job_list = Lists.newArrayList();
            multimap.keySet().forEach(x -> {
                Optional<TaskCareRule4Touch90Entity> ruleOpt = getBean(TaskCareRule4Touch90EntityAction.class).findById(x);
                autoRunJobList.add(new UpcomingTaskAutoRunDto(ruleOpt.orElse(null), multimap.get(x)));
            });
            autoRunJobList.forEach(x -> x.selfTest().ifPresent(error_job_list::addAll));
            List<UpcomingTaskAutoRunDto> run_job_filter = autoRunJobList.stream().filter(x -> !x.isError()).collect(Collectors.toList());
            run_job_filter.stream().filter(UpcomingTaskAutoRunDto::isCumulativeSalesRange).forEach(job -> {
                Map<String, Object> params = job.getCumulativeSalesParams();
                Optional<List<Map<String, Object>>> list_opt = getJdbcQuery().queryForList("MemberCare", "findMemberInfoByIds", params);
                list_opt.ifPresent(job::addMemberInfos);
            });
            run_job_filter.stream().filter(UpcomingTaskAutoRunDto::isSingleSalesRange).forEach(job -> {
                Optional<List<UpcomingTaskEntity>> list_opt = getBean(UpcomingTaskEntityAction.class).loadByIds(job.getTaskIdsParams());
                list_opt.ifPresent(job::addUpcomingTaskEntitys);
            });
            List<UpcomingTaskDetailExecDto> exit_job_list = Lists.newArrayList();
            run_job_filter.forEach(job -> job.filterRunEnabledList().ifPresent(exit_job_list::addAll));
            if (CollectionUtils.isEmpty(exit_job_list)) return;
            Set<String> classfies = exit_job_list.stream().map(UpcomingTaskDetailExecDto::getTemplateClassifies)
                    .collect(Collectors.toSet());
            SimpleMsgTemplateList simpleMsgTemplateList = getBean(MsgTemplateProxyAction.class).readDefTemplateByClassfies(classfies);
            exit_job_list.forEach(execJob -> {
                Optional<String> template = simpleMsgTemplateList.getDefTempateByRuleId(execJob.getTemplateClassifies(), execJob.getStoreId());
                template.ifPresent(execJob::setTemplate);
            });
            List<UpcomingTaskDetailExecDto> end_job_list = Lists.newArrayList();
            exit_job_list.forEach(x -> {
                if (x.isExitsTemplate()) {
                    end_job_list.add(x);
                } else {
                    error_job_list.add(x.getTaskDetail().makeException("节点对应模板缺失..."));
                }
            });
            if (CollectionUtils.isNotEmpty(error_job_list)) {
                //TODO  处理异常节点信息
            }
            if (CollectionUtils.isEmpty(end_job_list)) return;
            sendByStore(end_job_list);
        } catch (Exception e) {
            logger.error("autoRunTouch90Jobs() has error", e);
        } finally {
            LoginContextHolder.clear();
        }
    }

    private void sendByStore(List<UpcomingTaskDetailExecDto> todoJobList) {
        ArrayListMultimap<String, UpcomingTaskDetailExecDto> multimap = ArrayListMultimap.create();
        todoJobList.forEach(x -> multimap.put(x.getCompanyAndStoreIds(), x));
        for (String key : multimap.keySet()) {
            List<UpcomingTaskDetailExecDto> taskDetails = multimap.get(key);
            List<UpcomingTaskDetailEntity> res_list = Lists.newArrayList();
            Integer companyId = null, storeId = null;
            try {
                String[] args = StringUtils.split(key, '_');
                companyId = Integer.valueOf(args[0]);
                storeId = Integer.valueOf(args[1]);
                List<String> payloads = taskDetails.stream().map(UpcomingTaskDetailExecDto::toStringWithEncoding)
                        .collect(Collectors.toList());
                getBean(SmsGatewayProxyAction.class)
                        .sendMessageProxy(companyId, storeId, -1, payloads, null, BusinessType.TOUCHED90, true, null);
                taskDetails.forEach(x -> x.getTaskDetail().makeFinished().ifPresent(res_list::add));
            } catch (Exception e) {
                logger.error("sendByStore(%s,%s) has error", companyId, storeId);
                taskDetails.forEach(x -> res_list.add(x.getTaskDetail().makeException("自动执行失败...")));
            } finally {
                getBean(UpcomingTaskDetailEntityAction.class).batchUpdateByEntity(res_list);
            }
        } // end_for
    }

}
