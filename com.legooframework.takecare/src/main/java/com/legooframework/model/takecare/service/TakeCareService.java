package com.legooframework.model.takecare.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.takecare.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TakeCareService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(TakeCareService.class);

    /**
     * 批量发送
     *
     * @param taskIds
     * @param channels
     * @param followUpContent
     * @param imgUrls
     * @param careNinetyAggs
     * @param user
     */
    public void batchNinetyCare(Collection<Integer> taskIds, Collection<SendChannel> channels, String followUpContent,
                                String[] imgUrls, List<CareNinetyTaskAgg> careNinetyAggs, UserAuthorEntity user) {
        StoEntity store = getBean(StoEntityAction.class).loadById(user.getStoreId().orElse(0));
        List<CareNinetyTaskAgg> _tempAgg;
        if (CollectionUtils.isNotEmpty(careNinetyAggs)) {
            _tempAgg = Lists.newArrayList(careNinetyAggs);
        } else {
            _tempAgg = previewNinetySmsConent(taskIds, followUpContent, user);
        }
        Preconditions.checkState(CollectionUtils.isNotEmpty(_tempAgg), "待处理的 task 不存在...");
        List<CareNinetyTaskAgg> careAggs = Lists.newArrayList();
        // 处理节点信息
        for (CareNinetyTaskAgg agg : _tempAgg) {
            if (channels.contains(SendChannel.CANCEL)) {
                CareNinetyTaskEntity task = getBean(CareNinetyEntityAction.class).processTask(agg.getTaskId(), true);
                agg.setTask(task);
                agg.setChannels(channels);
                agg.setImgs(imgUrls);
                careAggs.add(agg);
            } else if (agg.isOnlyWx()) {
                if (agg.getWxUser().isPresent()) {
                    CareNinetyTaskEntity task = getBean(CareNinetyEntityAction.class).processTask(agg.getTaskId(), false);
                    agg.setTask(task);
                    agg.setChannels(channels);
                    agg.setImgs(imgUrls);
                    careAggs.add(agg);
                }
            } else {
                CareNinetyTaskEntity task = getBean(CareNinetyEntityAction.class).processTask(agg.getTaskId(), false);
                agg.setTask(task);
                agg.setChannels(channels);
                agg.setImgs(imgUrls);
                careAggs.add(agg);
            }
        } // end_for

        List<CareRecordEntity> all_care_logs = Lists.newArrayList();
        List<CareHisRecordEntity> all_care_his_logs = Lists.newArrayList();
        careAggs.forEach(agg -> {
            all_care_logs.addAll(agg.getCareRecord(user));
            all_care_his_logs.addAll(agg.getCareHisRecord(user));
        });

        if (CollectionUtils.isNotEmpty(all_care_logs)) {
            getBean(CareRecordEntityAction.class).batchInsert(all_care_logs);
        }
        if (CollectionUtils.isNotEmpty(all_care_his_logs)) {
            getBean(CareHisRecordEntityAction.class).batchInert(all_care_his_logs);
        }
        final String batchNo = String.format("NCP-%s", LocalDateTime.now().toString("yyyyMMddHHmmss"));
        sendSms4Care(store, all_care_logs, batchNo);
        sendWxMsg4Care(all_care_logs, batchNo);
    }

    public void batchBirthdayCare(Collection<Integer> memberIds, Collection<SendChannel> channels,
                                  String followUpContent, String[] imgUrls, UserAuthorEntity user) {
        if (CollectionUtils.isEmpty(memberIds)) return;
        Optional<MsgTemplateEnity> msg_tempate = Optional.empty();
        if (Strings.isNullOrEmpty(followUpContent)) {
            StoEntity store = getBean(StoEntityAction.class).loadById(user.getStoreId().orElse(0));
            msg_tempate = getBean(MsgTemplateEnityAction.class).findOneBirthCareTemplet4Store(store);
        }
        StoEntity store = getBean(StoEntityAction.class).loadById(user.getStoreId().orElse(0));
        EmpEntity employee = getEmployeeAction().findById(user.getId()).orElse(null);
        List<CareBirthdayAgg> birthday_care_aggs = Lists.newArrayList();
        List<CareBirthdayAgg> birthday_care_err_aggs = Lists.newArrayList();
        // 短信全追
        String sms_prefix = getBean(SendSmsEntityAction.class).getSmsPrefix(store);

        for (Integer $it : memberIds) {
            String send_ctx = !Strings.isNullOrEmpty(followUpContent) ? followUpContent
                    : msg_tempate.flatMap(MsgTemplateEnity::getContent).orElse(null);
            MemberAgg memberAgg = getCovariantService().loadMemberAgg($it);
            Map<String, Object> replaceMap = memberAgg.toReplaceMap();
            if (!Strings.isNullOrEmpty(send_ctx)) {
                send_ctx = replace(send_ctx, replaceMap);
                send_ctx = String.format("%s%s", sms_prefix, send_ctx);
            }
            // 聚合会员生日
            CareBirthdayAgg careAgg = getBean(CareBirthdayEntityAction.class)
                    .careMember4ThisYear(employee, memberAgg, channels, send_ctx, imgUrls);
            if (careAgg.getBirthdayCare().hasError()) {
                birthday_care_err_aggs.add(careAgg);
            } else if (careAgg.hasCareLog()) {
                // 无记录的生日排除掉
                careAgg.finished();
                birthday_care_aggs.add(careAgg);
            }
        }

        // 过滤异常生日情况
        if (CollectionUtils.isNotEmpty(birthday_care_err_aggs)) {
            List<CareBirthdayEntity> un_saved_care = birthday_care_err_aggs.stream().filter(x -> !x.hasSavedCare())
                    .map(CareBirthdayAgg::getBirthdayCare).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(un_saved_care)) {
                getBean(CareBirthdayEntityAction.class).batchInsertCare(un_saved_care);
            }
        }
        if (CollectionUtils.isEmpty(birthday_care_aggs)) return;

        List<CareBirthdayAgg> saved_care_aggs = birthday_care_aggs.stream().filter(CareBirthdayAgg::hasSavedCare)
                .collect(Collectors.toList());
        List<CareBirthdayAgg> update_care_aggs = birthday_care_aggs.stream().filter(CareBirthdayAgg::hasChangeState)
                .collect(Collectors.toList());
        List<CareBirthdayAgg> un_saved_care_aggs = birthday_care_aggs.stream().filter(x -> !x.hasSavedCare())
                .collect(Collectors.toList());
        List<CareRecordEntity> all_care_logs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(saved_care_aggs)) {
            List<CareRecordEntity> careLogs = Lists.newArrayList();
            saved_care_aggs.stream().map(CareBirthdayAgg::getTakeCareRecords).forEach(careLogs::addAll);
            getBean(CareRecordEntityAction.class).batchInsert(careLogs);
            all_care_logs.addAll(careLogs);
        }
        if (CollectionUtils.isNotEmpty(update_care_aggs)) {
            List<CareBirthdayEntity> cares = update_care_aggs.stream().map(CareBirthdayAgg::getBirthdayCare)
                    .collect(Collectors.toList());
            getBean(CareBirthdayEntityAction.class).batchUpdateCare(cares);

            List<CareRecordEntity> careLogs = Lists.newArrayList();
            for (CareBirthdayAgg $agg : un_saved_care_aggs) {
                getBean(CareBirthdayEntityAction.class).singleInsertCare($agg.getBirthdayCare());
                careLogs.addAll($agg.getTakeCareRecords());
            }
            getBean(CareRecordEntityAction.class).batchInsert(careLogs);
            all_care_logs.addAll(careLogs);
        }
        if (CollectionUtils.isNotEmpty(un_saved_care_aggs)) {
            List<CareRecordEntity> careLogs = Lists.newArrayList();
            for (CareBirthdayAgg $agg : un_saved_care_aggs) {
                getBean(CareBirthdayEntityAction.class).singleInsertCare($agg.getBirthdayCare());
                careLogs.addAll($agg.getTakeCareRecords());
            }
            getBean(CareRecordEntityAction.class).batchInsert(careLogs);
            all_care_logs.addAll(careLogs);
        }
        final String batchNo = String.format("BCP-%s", LocalDateTime.now().toString("yyyyMMddHHmmss"));
        // 适配元系统记录历史
        try {
            List<CareHisRecordEntity> his_rec_logs = Lists.newArrayList();
            birthday_care_aggs.forEach(x -> his_rec_logs.addAll(x.getHisCareRecords()));
            getBean(CareHisRecordEntityAction.class).batchInert(his_rec_logs);
        } catch (Exception e) {
            logger.error("Save crm_membercarerec has error ", e);
        }
        sendSms4Care(store, all_care_logs, batchNo);
        sendWxMsg4Care(all_care_logs, batchNo);
    }

    private void sendSms4Care(StoEntity store, List<CareRecordEntity> careRecords, String batchNo) {
        if (CollectionUtils.isEmpty(careRecords)) return;
        List<CareRecordEntity> to_send_sms_list = careRecords.stream().filter(x -> !x.isError())
                .filter(CareRecordEntity::isSmsChannel).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(to_send_sms_list)) return;
        List<SendSmsEntity> sendsmses = to_send_sms_list.stream().map(care -> care.createSendSms(batchNo))
                .collect(Collectors.toList());
        getBean(CovariantService.class).sendSmsesByStore(store.getId(), sendsmses);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s insert BirthdayCare sms size is %d", batchNo, sendsmses.size()));
    }

    private void sendWxMsg4Care(List<CareRecordEntity> careRecords, String batchNo) {
        if (CollectionUtils.isEmpty(careRecords)) return;
        List<CareRecordEntity> to_send_wx_list = careRecords.stream().filter(x -> !x.isError())
                .filter(CareRecordEntity::isWxChannel).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(to_send_wx_list)) return;
        List<SendWechatEntity> send_msgs = to_send_wx_list.stream().map(care -> care.createSendWxMsg(batchNo))
                .collect(Collectors.toList());
        getBean(SendWechatEntityAction.class).batchSendMsg(send_msgs);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s insert BirthdayCare wxmsg size is %d", batchNo, send_msgs.size()));
    }

    private String replace(String content, Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params) || Strings.isNullOrEmpty(content)) return content;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(content);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", content, params), e);
        }
    }

    /**
     * 预览一批短信信息
     *
     * @param taskIds
     * @param user
     * @return
     */
    public List<CareNinetyTaskAgg> previewNinetySmsConent(Collection<Integer> taskIds, String followUpContent, UserAuthorEntity user) {
        StoEntity store = getBean(StoEntityAction.class).loadById(user.getStoreId().orElse(0));
        // 短信全追
        String sms_prefix = getBean(SendSmsEntityAction.class).getSmsPrefix(store);

        Optional<List<CareNinetyTaskEntity>> tasks = getBean(CareNinetyEntityAction.class).findTaskByIds(taskIds);
        if (taskIds.isEmpty()) return null;
        List<CareNinetyTaskAgg> careNinetyTaskAggs = Lists.newArrayListWithCapacity(taskIds.size());
        for (CareNinetyTaskEntity task : tasks.get()) {
            CareNinetyTaskAgg agg = new CareNinetyTaskAgg(task);
            MemberAgg mm_agg = null;
            try {
                mm_agg = getBean(CovariantService.class).loadMemberAgg(task.getMemberId());
                agg.setMemberAgg(mm_agg);
            } catch (Exception e) {
                logger.error(String.format("无法匹配 memberId = %d 对应的用户信息", task.getMemberId()), e);
                agg.setErrorMsg(String.format("ERROR:%d 无队员信息", task.getMemberId()));
                continue;
            }
            careNinetyTaskAggs.add(agg);
            if (Strings.isNullOrEmpty(followUpContent)) {
                Optional<Integer> msgTempId = task.getMessageTempletId();
                if (msgTempId.isPresent()) {
                    Optional<MsgTemplateEnity> template = getBean(MsgTemplateEnityAction.class).findById(msgTempId.get());
                    if (template.isPresent() && template.get().getContent().isPresent()) {
                        agg.setSrcMsgTemp(template.get().getContent().get());
                    } else {
                        agg.setErrorMsg(String.format("ERROR:MsgId=%d 无有效模板", msgTempId.get()));
                        continue;
                    }
                } else {
                    agg.setErrorMsg("ERROR:未设置模板Id");
                    continue;
                }
            } else {
                agg.setSrcMsgTemp(followUpContent);
            }

            Map<String, Object> replaces = mm_agg.toReplaceMap();
            String target = replace(agg.getSrcMsgTemp(), replaces);
            agg.setTargetContent(String.format("%s%s退订回T", sms_prefix, target));
        }
        return careNinetyTaskAggs;
    }

    /**
     * 临时解决方案 适配源程序的
     */
    public void tempJob() {
        String query = "SELECT count(cb.id) from acp.crm_birthdaycareplan cb where (cb.company_id is null  OR cb.calendarType is null OR cb.birthday is null)";
        Long count = Objects.requireNonNull(getJdbcQuerySupport().getJdbcTemplate()).queryForObject(query, Long.class);
        if (count == null || count == 0L) return;
        String sql_01 = "update acp.crm_birthdaycareplan set company_id= (select cm.company_id from acp.crm_member cm where cm.id= member_id) where company_id is null";
        String sql_02 = "update acp.crm_birthdaycareplan set calendarType= (select IFNULL(cm.calendarType,1) from acp.crm_member cm where cm.id= member_id) where calendarType is null";
        String sql_03 = "update acp.crm_birthdaycareplan set birthday = IFNULL((select CASE WHEN IFNULL(cm.calendarType,1) = 1 THEN cm.birthday ELSE cm.lunarBirthday END  from acp.crm_member cm where cm.id= member_id),NOW()) where birthday is null";
        Objects.requireNonNull(getJdbcQuerySupport().getJdbcTemplate()).execute(sql_01);
        Objects.requireNonNull(getJdbcQuerySupport().getJdbcTemplate()).execute(sql_02);
        Objects.requireNonNull(getJdbcQuerySupport().getJdbcTemplate()).execute(sql_03);
        if (logger.isDebugEnabled())
            logger.debug("tempJob()  acp.crm_birthdaycareplan job finished ");
    }
}
