package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MemberServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(MemberServer.class);

    public MemberEntity modifyMember(StoreEntity store, Integer memberId, String name, Integer memberType, Integer sex, Integer serviceLevel,
                                     String phone, Integer memberCardType, String memberCardNum, Date createCardTime, String qqNum,
                                     String weixinId, String weiboNum, String email, Integer marryStatus, String detailAddress, String idols,
                                     String carePeople, Integer zodiac, Integer characterType, String jobType, Integer faithType,
                                     Integer likeContact, Date gregorianBirthday, Date lunarBirthday, String iconUrl, Integer calendarType) {
        Objects.requireNonNull(memberId);
        MemberEntity member = getBean(MemberEntityAction.class).loadById(store, memberId);
        return getBean(MemberEntityAction.class).modifyMember(member, name, memberType, sex, serviceLevel, phone,
                memberCardType, memberCardNum, createCardTime, qqNum, weixinId, weiboNum, email, marryStatus,
                detailAddress, idols, carePeople, zodiac, characterType, jobType, faithType, likeContact,
                gregorianBirthday, lunarBirthday, iconUrl, calendarType);
    }

    public MemberEntity saveMember(String name, Integer memberType, Integer sex, Integer serviceLevel, String phone,
                                   Integer memberCardType, String memberCardNum, Date createCardTime, Integer createCardStoreId,
                                   String createCardStoreName, String weixinId, String qqNum, String weiboNum, String email,
                                   Date gregorianBirthday, Date lunarBirthday, Integer calendarType, String iconUrl, Integer marryStatus,
                                   String detailAddress, String idols, String carePeople, Integer zodiac, Integer characterType,
                                   String jobType, Integer faithType, Integer likeContact, LoginUserContext userContext) {
        Preconditions.checkArgument(userContext.getStore().isPresent(), "登录用户门店信息不能为空");
        Preconditions.checkArgument(!existPhone(userContext.getExitsStore(), phone), "会员电话号码 %s 已存在.", phone);
        getBean(MemberEntityAction.class).saveMember(name, memberType, sex, serviceLevel, phone, memberCardType,
                memberCardNum, createCardTime, createCardStoreId, createCardStoreName, weixinId, qqNum, weiboNum, email,
                gregorianBirthday, lunarBirthday, calendarType, iconUrl, marryStatus, detailAddress, idols, carePeople,
                zodiac, characterType, jobType, faithType, likeContact, userContext.getStore().get());
        Optional<MemberEntity> optional = getBean(MemberEntityAction.class).findByPhone(userContext.getExitsStore(), phone);
        Preconditions.checkArgument(optional.isPresent(), String.format("无法找到电话为[%s]会员信息", phone));
        getBean(BaseModelServer.class).cleanCache("adapterCache");
        return optional.get();
    }

    private boolean existPhone(StoreEntity store, String phone) {
        Optional<MemberEntity> optional = getBean(MemberEntityAction.class).findByPhone(store, phone);
        return optional.isPresent();
    }

    /**
     * 会员绑定导购
     *
     * @param employeeId
     * @param memberIds
     */
    public void buildMembersToShopping(Integer employeeId, Collection<Integer> memberIds, boolean isBuilding,
                                       LoginUserContext user) {
        Preconditions.checkNotNull(user);
        Preconditions.checkState(user.getStore().isPresent());
        Preconditions.checkState(CollectionUtils.isNotEmpty(memberIds));
        if (isBuilding)
            Preconditions.checkNotNull(employeeId, "职员ID不可以为空值...");
        Optional<StoreEntity> storeOpt = user.getStore();
        Preconditions.checkState(storeOpt.isPresent(), "Id = %s 对应的门店不存在...");
        Optional<List<MemberEntity>> members = getBean(MemberEntityAction.class).findByIds(storeOpt.get(), memberIds,
                false, false);
        if (!members.isPresent()) return;
        getBean(EmployeeEntityAction.class).buildMembers(employeeId, members.get(), storeOpt.get(), isBuilding,
                user.getCompany().get());
        if (isBuilding) {
            EmployeeEntity employee = getBean(EmployeeEntityAction.class).loadById(employeeId);
            getBean(MemberEntityAction.class).addShoppingGuide(employee, members.get());
        } else {
            EmployeeEntity employee = null;
            if (employeeId != null) {
                employee = getBean(EmployeeEntityAction.class).loadById(employeeId);
            }
            getBean(MemberEntityAction.class).removeShoppingGuide(employee, members.get());
        }
    }

}
