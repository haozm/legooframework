package com.legooframework.model.utils;

import com.google.common.base.Throwables;
import com.legooframework.model.base.exception.BaseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ExceptionUtil {

    public static RuntimeException handleException(Exception e, String msg, Logger logger) {
        Throwables.propagateIfPossible(e, BaseException.class);
        Throwables.propagateIfPossible(e, RuntimeException.class);
        logger.error(msg, e);
        return new RuntimeException(msg, e);
    }

    /**
     * @param
     * @return
     */
    public static String getExceptionStrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        e.printStackTrace(writer);
        writer.flush();
        sw.flush();
        try {
            sw.close();
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sw.toString();
    }

}
