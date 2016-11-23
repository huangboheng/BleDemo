package fantasy.rqg.sdk.util;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by tongwanglin on 13-11-23.
 * <p>
 * <p>
 * <p>
 * //          * AlarmSettings 属性如下
 * //     * private boolean isOn; 是否开启闹钟
 * //                * private int index; 闹钟id 1-5
 * //                * private int remindBefore; 浅睡眠提醒 (分钟)
 * //                * private Integer time;  闹钟时间 (分钟)
 * //                * private boolean day1On; 周一到周日 是否开 true - > 开 false -> 关
 * //                * private boolean day2On;
 * //                * private boolean day3On;
 * //                * private boolean day4On;
 * //                * private boolean day5On;
 * //                * private boolean day6On;
 * //                * private boolean day7On;
 * //                * private int lazyMode;//0：关闭 1:开启 懒人模式 (2s 设置无意义,但这个字段需要)
 */
public class AlarmSettings {
    private static final String TAG = "AlarmSettings";

    public boolean isSelected;
    private boolean isOn;
    private int index;
    private int remindBefore;
    private Integer time;
    private boolean isUpload;
    private boolean day1On;
    private boolean day2On;
    private boolean day3On;
    private boolean day4On;
    private boolean day5On;
    private boolean day6On;
    private boolean day7On;
    private boolean isDelete;

    private int lazyMode;//0：关闭 1:开启 懒人模式

    private String name;

    public AlarmSettings() {
    }

    public AlarmSettings(AlarmSettings cs) {
        this.isSelected = cs.isSelected();
        this.isOn = cs.isOn();
        this.index = cs.getIndex();
        this.remindBefore = cs.getRemindBefore();
        this.time = cs.getTime();
        this.isUpload = cs.isUpload();
        this.day1On = cs.isDay1On();
        this.day2On = cs.isDay2On();
        this.day3On = cs.isDay3On();
        this.day4On = cs.isDay4On();
        this.day5On = cs.isDay5On();
        this.day6On = cs.isDay6On();
        this.day7On = cs.isDay7On();
        this.isDelete = cs.isDelete();
        this.name = cs.getName();
        this.lazyMode = cs.getLazyMode();
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getRemindBefore() {
        return remindBefore;
    }

    public void setRemindBefore(int remindBefore) {
        this.remindBefore = remindBefore;
    }

    public Integer getTime() {
        if (time == null) time = 480;
        return time;
    }

    public String getTimeString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, (getTime() / 60));
        calendar.set(Calendar.MINUTE, (getTime() % 60));
        return format.format(calendar.getTime());
    }

    public String getBeforeTimeString() {//用于兼容旧版本
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, ((getTime() - remindBefore) / 60));
        calendar.set(Calendar.MINUTE, ((getTime() - remindBefore) % 60));
        return format.format(calendar.getTime());
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isDay1On() {
        return day1On;
    }

    public void setDay1On(boolean day1On) {
        this.day1On = day1On;
    }

    public boolean isDay2On() {
        return day2On;
    }

    public void setDay2On(boolean day2On) {
        this.day2On = day2On;
    }

    public boolean isDay3On() {
        return day3On;
    }

    public void setDay3On(boolean day3On) {
        this.day3On = day3On;
    }

    public boolean isDay4On() {
        return day4On;
    }

    public void setDay4On(boolean day4On) {
        this.day4On = day4On;
    }

    public boolean isDay5On() {
        return day5On;
    }

    public void setDay5On(boolean day5On) {
        this.day5On = day5On;
    }

    public boolean isDay6On() {
        return day6On;
    }

    public void setDay6On(boolean day6On) {
        this.day6On = day6On;
    }

    public boolean isDay7On() {
        return day7On;
    }

    public void setDay7On(boolean day7On) {
        this.day7On = day7On;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public String buildWeekString() {
        // 从周7开始，到周一到周6。
        StringBuilder stringBuilder = new StringBuilder();
        if (isDay1On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay2On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay3On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay4On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay5On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay6On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isDay7On())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        if (isOn())
            stringBuilder.append("1");
        else
            stringBuilder.append("0");
        return stringBuilder.toString();
    }

    public void parseWeekString(String week) {
        char[] c = week.toCharArray();
        if (c.length == 8) {
            setDay1On(c[0] == '1');
            setDay2On(c[1] == '1');
            setDay3On(c[2] == '1');
            setDay4On(c[3] == '1');
            setDay5On(c[4] == '1');
            setDay6On(c[5] == '1');
            setDay7On(c[6] == '1');
            setOn(c[7] == '1');
        } else {
            Log.i(TAG, "闹钟星期数据有误");
        }
    }

    public int getLazyMode() {
        return lazyMode;
    }

    public void setLazyMode(int lazyMode) {
        this.lazyMode = lazyMode;
    }

    @Override
    public String toString() {
        return "ClockSettings{" +
                "isSelected=" + isSelected +
                ", isOn=" + isOn +
                ", index=" + index +
                ", remindBefore=" + remindBefore +
                ", time=" + time +
                ", isUpload=" + isUpload +
                ", day1On=" + day1On +
                ", day2On=" + day2On +
                ", day3On=" + day3On +
                ", day4On=" + day4On +
                ", day5On=" + day5On +
                ", day6On=" + day6On +
                ", day7On=" + day7On +
                ", isDelete=" + isDelete +
                ", lazyMode=" + lazyMode +
                '}';
    }
}
