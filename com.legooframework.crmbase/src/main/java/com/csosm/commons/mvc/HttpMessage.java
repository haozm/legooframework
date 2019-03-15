package com.csosm.commons.mvc;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;

public class HttpMessage {
    //// {"code":"9998","msg":"pinCode \u003d 216014 已经被激活使用...","errDetail":"pinCode \u003d 216014 已经被激活使用...","header":{}}
    private final String code, msg, errDetail;

    public HttpMessage(String code, String msg, String errDetail) {
        this.code = code;
        this.msg = msg;
        this.errDetail = errDetail;
    }

    public boolean isOK() {
        return StringUtils.equals(code, "0000");
    }

    public String getMsg() {
        return msg;
    }

    public String getErrDetail() {
        return errDetail;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("msg", msg)
                .add("errDetail", errDetail)
                .toString();
    }
}
