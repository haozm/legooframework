package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

public class ChatroomActivityAmountEntity extends BaseEntity<String> {

    private final Integer totalSize, addSize, delSize, msgSize, talkSize;
    private final String amountDate;

    ChatroomActivityAmountEntity(String name, Integer totalSize,
                                 Integer addSize, Integer delSize, Integer msgSize, Integer talkSize, String amountDate) {
        super(name);
        this.totalSize = totalSize;
        this.addSize = addSize;
        this.delSize = delSize;
        this.msgSize = msgSize;
        this.talkSize = talkSize;
        this.amountDate = amountDate;
    }


    public Integer getTotalSize() {
        return totalSize;
    }

    public Integer getAddSize() {
        return addSize;
    }

    public Integer getDelSize() {
        return delSize;
    }

    public Integer getMsgSize() {
        return msgSize;
    }

    public Integer getTalkSize() {
        return talkSize;
    }

    public String getAmountDate() {
        return amountDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatroomActivityAmountEntity that = (ChatroomActivityAmountEntity) o;
        return Objects.equal(getId(), that.getId()) &&
                Objects.equal(totalSize, that.totalSize) &&
                Objects.equal(addSize, that.addSize) &&
                Objects.equal(delSize, that.delSize) &&
                Objects.equal(msgSize, that.msgSize) &&
                Objects.equal(talkSize, that.talkSize) &&
                Objects.equal(amountDate, that.amountDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), totalSize, addSize, delSize,
                msgSize, talkSize, amountDate);
    }


	@Override
	public String toString() {
		return "ChatroomActivityAmountEntity [totalSize=" + totalSize + ", addSize=" + addSize + ", delSize=" + delSize
				+ ", msgSize=" + msgSize + ", talkSize=" + talkSize + ", amountDate=" + amountDate + "]";
	}
    
}
