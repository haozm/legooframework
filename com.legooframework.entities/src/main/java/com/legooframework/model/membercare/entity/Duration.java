package com.legooframework.model.membercare.entity;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class Duration {

    private final long duration;
    private final TimeUnit timeUnit;

    TimeUnit getTimeUnit() {
        return timeUnit;
    }

    Duration(String key, String value) {
        try {
            char lastChar = value.charAt(value.length() - 1);
            TimeUnit timeUnit;
            switch (lastChar) {
                case 'd':
                    timeUnit = TimeUnit.DAYS;
                    break;
                case 'h':
                    timeUnit = TimeUnit.HOURS;
                    break;
                case 'm':
                    timeUnit = TimeUnit.MINUTES;
                    break;
                case 's':
                    timeUnit = TimeUnit.SECONDS;
                    break;
                default:
                    throw new IllegalArgumentException(
                            format("key %s invalid format.  was %s, must end with one of [dDhHmMsS]", key, value));
            }
            this.timeUnit = timeUnit;
            this.duration = Long.parseLong(value.substring(0, value.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    format("key %s value set to %s, must be integer", key, value));
        }
    }

    long getDuration() {
        return duration;
    }

    public long toHours() {
        return TimeUnit.HOURS.convert(duration, timeUnit);
    }


    public String toCnString() {
        String res;
        switch (timeUnit) {
            case DAYS:
                res = "天";
                break;
            case HOURS:
                res = "小时";
                break;
            case MINUTES:
                res = "分钟";
                break;
            case SECONDS:
                res = "秒";
                break;
            default:
                throw new IllegalArgumentException(format("非法的参数....%s ", timeUnit));
        }
        return format("%s%s", duration, res);
    }

    @Override
    public String toString() {
        char res;
        switch (timeUnit) {
            case DAYS:
                res = 'd';
                break;
            case HOURS:
                res = 'h';
                break;
            case MINUTES:
                res = 'm';
                break;
            case SECONDS:
                res = 's';
                break;
            default:
                throw new IllegalArgumentException(format("非法的参数....%s ", timeUnit));
        }
        return format("%s%s", duration, res);
    }
}
