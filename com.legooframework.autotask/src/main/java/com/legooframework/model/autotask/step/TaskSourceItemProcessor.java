package com.legooframework.model.autotask.step;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.autotask.entity.*;
import com.legooframework.model.core.utils.AppCtxSupport;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.EmployeeAgg;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.covariant.service.WxUserAgg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskSourceItemProcessor extends AppCtxSupport implements ItemProcessor<TaskSourceEntity, TaskProcessAgg> {

    private static final Logger logger = LoggerFactory.getLogger(TaskSourceItemProcessor.class);

    public TaskSourceItemProcessor() {
    }

    @Override
    public TaskProcessAgg process(TaskSourceEntity item) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("-------------- Process(%s) start ---------------------", item.toString()));
        List<TaskProcessTemp> sub_list = null;
        Optional<List<TaskRuleEntity>> ruleOpt;
        try {
            OrgEntity company = getCompanyAction().loadComById(item.getCompanyId());
            ruleOpt = getTaskRuleAction().findStoreByTaskSource(item);
            if (!ruleOpt.isPresent()) ruleOpt = getTaskRuleAction().findCompanyByTaskSource(item);
            if (ruleOpt.isPresent()) {
                sub_list = handle(item, ruleOpt.get(), company);
            } else {
                item.error4NoRule();
            }
        } catch (Exception e) {
            logger.error(String.format("process(%s) 发生未知异常....", item), e);
            item.error(e.getMessage());
        }
        TaskProcessAgg agg = new TaskProcessAgg(item, sub_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("-------------- Process(%s) end ---------------------", agg.toString()));
        return agg;
    }

    /**
     * @param taskSource OO
     * @param taskRules  XX
     * @param company    OOXX
     * @return OOXX
     */
    private List<TaskProcessTemp> handle(TaskSourceEntity taskSource, List<TaskRuleEntity> taskRules, OrgEntity company) {
        Map<String, Object> params = company.toReplaceMap();
        List<TaskProcessTemp> list = Lists.newArrayListWithCapacity(taskRules.size());
        for (TaskRuleEntity taskRule : taskRules) {
            if (RoleType.Member == taskRule.getSendTarget()) {
                TaskProcessTemp _temp = handle4Member(taskSource, taskRule, params);
                if (_temp != null) list.add(_temp);
            } else if (RoleType.ShoppingGuide == taskRule.getSendTarget()) {
                TaskProcessTemp _temp = handle4ShoppingGuide(taskSource, taskRule, params);
                if (_temp != null) list.add(_temp);
            } else if (RoleType.StoreManager == taskRule.getSendTarget()) {
                TaskProcessTemp _temp = handle4ShoppingGuide(taskSource, taskRule, params);
                if (_temp != null) list.add(_temp);
            } else if (RoleType.Manager == taskRule.getSendTarget()) {
                TaskProcessTemp _temp = handle4ShoppingGuide(taskSource, taskRule, params);
                if (_temp != null) list.add(_temp);
            } else if (RoleType.Boss == taskRule.getSendTarget()) {
                TaskProcessTemp _temp = handle4ShoppingGuide(taskSource, taskRule, params);
                if (_temp != null) list.add(_temp);
            }
        }
        return list;
    }

    /**
     * 处理导购发送信息
     *
     * @param taskSource OO
     * @param taskRule   XX
     * @param params     OOXX
     * @return OXOX
     */
    TaskProcessTemp handle4ShoppingGuide(TaskSourceEntity taskSource, TaskRuleEntity taskRule, Map<String, Object> params) {
        Map<String, Object> _temp_map = Maps.newHashMap(params);
        if (SendChannel.SMS == taskRule.getSendChannel()) {
            Optional<Integer> employeeId = taskSource.getEmployeeId();
            if (!employeeId.isPresent())
                return TaskProcessTemp.creatSmsError4Employee(taskRule, String.format("taskSourceId=%s 无职员信息", taskSource.getId()), null);
            EmployeeAgg employeeAgg = getCovariantService().loadEmployeeAgg(employeeId.get());
            _temp_map.putAll(employeeAgg.toReplaceMap());
            try {
                String template = taskRule.replace(_temp_map);
                return TaskProcessTemp.creatSms4Employee(taskRule, template, employeeAgg.getEmployee());
            } catch (TemplateReplaceException e) {
                logger.error(String.format("replace(%s) has error", taskRule.getTemplate()));
                return TaskProcessTemp.creatSmsError4Employee(taskRule, e.getMessage(), employeeAgg.getEmployee());
            }
        } else if (SendChannel.WECHAT == taskRule.getSendChannel()) {
            return TaskProcessTemp.creatSmsError4Employee(taskRule, "该角色不支持发送微信", null);
        } else if (SendChannel.WECHAT_GZH == taskRule.getSendChannel()) {
            return TaskProcessTemp.creatSmsError4Employee(taskRule, "该角色不支持发送公众号", null);
        }
        return TaskProcessTemp.creatSmsError4Employee(taskRule, "未知错误", null);
    }

    /**
     * 处理 会员信息
     *
     * @param taskSource OO
     * @param taskRule   XX
     * @param params     OOXX
     * @return OXOX
     */
    TaskProcessTemp handle4Member(TaskSourceEntity taskSource, TaskRuleEntity taskRule, Map<String, Object> params) {
        Map<String, Object> _temp_map = Maps.newHashMap(params);
        if (SendChannel.SMS == taskRule.getSendChannel()) {
            Optional<Integer> memberId = taskSource.getMemberId();
            if (!memberId.isPresent())
                return TaskProcessTemp.creatSmsError4Member(taskRule, String.format("taskSourceId=%s 无会员ID", taskSource.getId()), null);
            MemberAgg memberAgg = getCovariantService().loadMemberAgg(memberId.get());
            _temp_map.putAll(memberAgg.toReplaceMap());
            try {
                String template = taskRule.replace(_temp_map);
                return TaskProcessTemp.creatSms4Member(taskRule, template, memberAgg.getMember());
            } catch (TemplateReplaceException e) {
                logger.error(String.format("replace(%s) has error", taskRule.getTemplate()));
                return TaskProcessTemp.creatSmsError4Member(taskRule, e.getMessage(), memberAgg.getMember());
            }
        } else if (SendChannel.WECHAT == taskRule.getSendChannel()) {
            return sendWxMsgByWechat(taskSource, taskRule, params);
        } else if (SendChannel.WECHAT_GZH == taskRule.getSendChannel()) {
            return TaskProcessTemp.creatSmsError4Member(taskRule, String.format("taskSourceId=%s 无粉丝信息", taskSource.getId()), null);
        }
        return TaskProcessTemp.creatSmsError4Member(taskRule, "未知异常", null);
    }

    /**
     * 发送微信
     *
     * @param taskSource OO
     * @param taskRule   XX
     * @param params     OOXX
     * @return OXOX
     */
    private TaskProcessTemp sendWxMsgByWechat(TaskSourceEntity taskSource, TaskRuleEntity taskRule, Map<String, Object> params) {
        Map<String, Object> _temp_map = Maps.newHashMap(params);
        Optional<String> weixinId = taskSource.getWeixinId();
        Optional<Integer> storeId = taskSource.getStoreId();
        if (!weixinId.isPresent() || !storeId.isPresent())
            return TaskProcessTemp.creatSmsError4Member(taskRule, String.format("taskSourceId=%s 无微信信息", taskSource.getId()), null);
        WxUserAgg wxUserAgg = getCovariantService().loadWxUserAgg(storeId.get(), weixinId.get());
        _temp_map.putAll(wxUserAgg.toReplaceMap());
        try {
            String template = taskRule.replace(_temp_map);
            return TaskProcessTemp.creatWx4Wechat(taskRule, template, wxUserAgg.getWxUser());
        } catch (TemplateReplaceException e) {
            logger.error(String.format("replace(%s) has error", taskRule.getTemplate()));
            return TaskProcessTemp.creatErrorWx4Wechat(taskRule, e.getMessage(), wxUserAgg.getWxUser());
        }
    }

    // ---------------------------------- setter ----------------------------------
    private TaskRuleEntityAction getTaskRuleAction() {
        return this.getBean(TaskRuleEntityAction.class);
    }

    private CovariantService getCovariantService() {
        return this.getBean(CovariantService.class);
    }

    private OrgEntityAction getCompanyAction() {
        return this.getBean(OrgEntityAction.class);
    }


}
