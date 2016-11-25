package fantasy.rqg.sdk.util;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fantasy.rqg.blemodule.x.BongUtil;
import fantasy.rqg.sdk.BuildConfig;


/**
 * 和bong通信
 */
public class BongCoder {
    private static final String TAG = "BongCoder";

    /**
     * *************************************
     * 解码从BLE设备收到的数据
     * **************************************
     */

    public static DecodeDataType getBongDataType(byte[] _data) {
        if (_data == null || _data.length == 0) {
            return DecodeDataType.DATA_UNKNOWN;
        }

        StringBuilder dataByte = new StringBuilder(_data.length);
        BongUtil.byteToBit(_data, dataByte);
        int month = Integer.parseInt(dataByte.substring(0, 4), 2);
        if (month > 0 && month < 13) {
            return DecodeDataType.DATA_BONG;
        } else if (HexUtil.encodeHexStr(_data).startsWith("e0")) {
            return DecodeDataType.DATA_SENSOR;
        }
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "Data Other:" + HexUtil.encodeHexStr(_data));
        }
        return DecodeDataType.DATA_UNKNOWN;
    }


    /**
     * 身体测试开关
     *
     * @param isOn true 获取打开身体测试命令, false 获取关闭身体测试命令
     * @return 身体测试命令
     */
    public static byte[] encodeBodyTest(boolean isOn) {
        if (isOn) {
            return BongUtil.hexStringToBytes(BongHex.bodyTestOn);
        } else {
            return BongUtil.hexStringToBytes(BongHex.bodyTestOff);
        }
    }


    public static byte[] encodeLanguage() {
        Locale aDefault = Locale.getDefault();
        int l = 0;
        if (TextUtils.equals(Locale.CHINESE.getLanguage(), aDefault.getLanguage())) {
            l = 0;
        } else {
            l = 1;
        }

        Log.d(TAG, "encodeLanguage: " + aDefault.getLanguage() + " " + l + " " + Locale.CHINESE.getLanguage());

        String format = String.format(Locale.ENGLISH, "2900000020%02X", l);

        return BongUtil.hexStringToBytes(format);
    }


    /**
     * 点亮 bong
     *
     * @return
     */
    public static byte[] encodeLightBong() {
        return BongUtil.hexStringToBytes(BongHex.lightBong);
    }

    /**
     * 显示绑定成功
     *
     * @return
     */
    public static byte[] encodeBindSuccess() {
        return BongUtil.hexStringToBytes(BongHex.bindSuccess);
    }


    public static byte[] encodeExerciseReminder(boolean isOn, Iterable<? extends Boolean> dayonList, int startHour, int startMin, int endHour, int endMin) {

        byte week = 0;

        byte tmp = 0x01;

        for (Boolean b : dayonList) {
            if (b) {
                week = (byte) (week | tmp);
            }

            tmp = (byte) (tmp << 1);
        }

        String string = String.format(Locale.ENGLISH, "2900000012%02X%02X%02X%02X%02X%02X"
                , isOn ? 1 : 0,
                startHour,
                startMin,
                endHour,
                endMin,
                week
        );


        return BongUtil.hexStringToBytes(string);
    }

    public static byte[] encodeBong3Screen(boolean step, boolean distance, boolean cal, boolean weather, boolean heart) {


        String string = String.format(Locale.ENGLISH, "290000001A%02X%02X%02X%02X%02X"
                , step ? 1 : 0
                , distance ? 1 : 0
                , cal ? 1 : 0
                , weather ? 1 : 0
                , heart ? 1 : 0
        );


        return BongUtil.hexStringToBytes(string);
    }

    public static byte[] encodeWeather(long second, int weatherCode, int temp, int pm25) {
        String string = String.format(Locale.ENGLISH, "290000001B%016X%02X%02X%04X"
                , second
                , weatherCode
                , (byte) temp
                , pm25
        );

        return BongUtil.hexStringToBytes(string);
    }


    public static byte[] encodeUserInfo(int sex, int height, int weight, int birth) {
        String str = String.format(Locale.ENGLISH, "2900000016%02X%08X%08X%08X", sex, height, weight, birth);

        return BongUtil.hexStringToBytes(str);
    }

    public static byte[] encodeScreenStyle(boolean vertical, boolean auto, boolean right, boolean highlight) {


        String str = String.format(Locale.ENGLISH, "2900000013%02X%02X%02X%02X",
                vertical ? 1 : 0,
                auto ? 1 : 0,
                right ? 1 : 0,
                highlight ? 1 : 0
        );

        return BongUtil.hexStringToBytes(str);
    }


    public static byte[] encodeAppMsgSwitch(boolean call, boolean wechat, boolean qq, boolean sms) {
        String string = String.format(Locale.ENGLISH, "2900000014%02X%02X%02X%02X",
                call ? 1 : 0,
                wechat ? 1 : 0,
                qq ? 1 : 0,
                sms ? 1 : 0
        );


        return BongUtil.hexStringToBytes(string);
    }

    public static byte[] encodeStartAppSport(int state, int type, int startTime, int distance) {
        String str = String.format(Locale.ENGLISH, "2900000015%02X%02X%08X%08X", state, type, startTime, distance);


        return BongUtil.hexStringToBytes(str);
    }

    public static byte[] encodePostSummery(int energy, int steps, int distance) {
        String str = String.format(Locale.ENGLISH, "2900000019%08X%08X%08X", energy, steps, distance);

        return BongUtil.hexStringToBytes(str);
    }

    /**
     * 写时间
     */
    public static byte[] encodeTimeSync() {
        return BongUtil.hexStringToBytes(encodeTimeSyncString());
    }


    public static byte[] encodeSmartHeart(boolean open) {
        return BongUtil.hexStringToBytes(encode2sHeartSwitch(open));
    }

    /**
     * 写时间
     */
    public static String encodeTimeSyncString() {
        Calendar calendar = Calendar.getInstance();
        String timeSyncString = "100000%04X%02X%02X%02X%02X%02X%02X%02X";

        TimeZone timeZone = TimeZone.getDefault();

        int offset = timeZone.getOffset(System.currentTimeMillis());

        int zoneH = offset / 3600000;
        int zoneM = (offset / 60000) % 60;


//        zoneM *= (zoneH > 0 ? 1 : -1);


        Log.d(TAG, "zoneH:" + zoneH + " zoneM:" + zoneM);
        return String.format(timeSyncString,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                (byte) zoneH,
                (byte) zoneM
        );
//
    }

    /**
     * AlarmSettings 属性如下
     * private boolean isOn; 是否开启闹钟
     * private int index; 闹钟id
     * private int remindBefore; 浅睡眠提醒 (分钟)
     * private Integer time;  闹钟时间 (分钟)
     * private boolean day1On; 周一到周日 是否开 true - > 开 false -> 关
     * private boolean day2On;
     * private boolean day3On;
     * private boolean day4On;
     * private boolean day5On;
     * private boolean day6On;
     * private boolean day7On;
     * private int lazyMode;//0：关闭 1:开启 懒人模式 (2s 设置无意义,但这个字段需要)
     *
     * @param list
     * @return
     */
    public static String encodeAlaramListString(List<AlarmSettings> list) {
        // 写闹钟
        StringBuilder data = new StringBuilder();
        data.append("23");
        // 0~30min 转化为 0~F(*2)
        for (AlarmSettings cs : list) {
            String remind_time_1 = Integer.toHexString((cs.getRemindBefore() + 1) / 2);
            data.append(remind_time_1);
        }
        int left = 6 - list.size();
        for (int i = 0; i < left; i++) {
            data.append("0");//补足，无意义
        }

        //闹钟的重复日期
        for (AlarmSettings settings : list) {
            StringBuilder binary_data = new StringBuilder();
            if (settings.isOn()) {
                //懒人模式
                if (settings.getLazyMode() == 1) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay6On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay5On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay4On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay3On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay2On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay1On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }

                if (settings.isDay7On()) {
                    binary_data.append("1");
                } else {
                    binary_data.append("0");
                }
            } else {
                binary_data.append("00000000");
            }
            data.append(BongUtil.binaryString2hexString(binary_data.toString()));

            int Day1_Clock_Time_H = settings.getTime() / 60;
            int Day1_Clock_Time_M = settings.getTime() % 60;
            String Day1_Clock_Time_H_String = Integer.toHexString(Day1_Clock_Time_H);
            String Day1_Clock_Time_M_String = Integer.toHexString(Day1_Clock_Time_M);

            data.append(String.format("%2s", Day1_Clock_Time_H_String).replace(' ', '0'));
            data.append(String.format("%2s", Day1_Clock_Time_M_String).replace(' ', '0'));
        }

        left = 5 - list.size();
        if (left > 0) {
            StringBuilder binary_data = new StringBuilder();
            for (int i = 0; i < left; i++) {
                //补足，无意义
                binary_data.append("00000000");// 星期
                binary_data.append("00000000");// 小时
                binary_data.append("00000000");// 分钟
            }
            data.append(BongUtil.binaryString2hexString(binary_data.toString()));
        }
        Log.i(TAG, "clock : " + data);
        return data.toString();
    }

    /**
     * 写入ReadData命令 读取更新数据
     * data[0]=0x20；
     * data[1]~data[3]：保留，无意义；
     * data[4]：
     * 0x10：读取更新数据；
     * 0x11：读取flash数据，参数见data[5]~data[8]；
     * 0x20：停止数据输出，对所有读取数据指令有效；
     * 0x30：读取当前指针；
     * 0x31：设置当前指针；
     * data[5]：要读取的起始页,0~39；
     * data[6]：要读取的起始指针，0~63；
     * data[7]：要读取的结束页，0~39；
     * data[8]：要读取的结束指针，0~63；
     * 写入ReadData命令 读取更新数据
     *
     * @return
     */
//    public static byte[] encodeDataUpdateRead() {
//        return BongUtil.hexStringToBytes(encodeDataUpdateReadString());
//    }

//    public static String encodeDataUpdateReadString() {
//        return BongHex.hi1;
//    }

    /**
     * 写入ReadData命令 读取时间段数据
     * data[0]=0x20；
     * data[1]~data[2]：保留，无意义；
     * data[3]：13；
     * 开始时间:
     * data[4]：年
     * data[5]：月；
     * data[6]：日；
     * data[7]：时；
     * data[8]：分；
     * 结束时间:
     * data[9]：年
     * data[10]：月；
     * data[11]：日；
     * data[12]：时；
     * data[13]：分；
     * 写入ReadData命令 读取时间段数据
     *
     * @return
     */
    public static byte[] encodeDataByTimeRead(long beginTime, long endTime) {
        return BongUtil.hexStringToBytes(encodeDataByTimeReadString(beginTime, endTime));
    }

    private static String encodeDataByTimeReadString(long beginTime, long endTime) {
        return BongHex.hehe3 + BongHexUtils.getStrTimeForHex(beginTime) + BongHexUtils.getStrTimeForHex(endTime);
    }


    public static byte[] encodeHeartDataByTimeRead(long beginTime, long endTime) {
        return BongUtil.hexStringToBytes(encodeHeartDataByTimeReadString(beginTime, endTime));
    }


    private static String encodeHeartDataByTimeReadString(long beginTime, long endTime) {
        return BongHex.heartSync + BongHexUtils.getStrTimeForHex(beginTime) + BongHexUtils.getStrTimeForHex(endTime);
    }


    public static byte[] encodeReadBattery() {
        return BongUtil.hexStringToBytes("2600000010");
    }

    public static byte[] encodeDfuMode() {
        return BongUtil.hexStringToBytes(BongHex.dfu);
    }

    /**
     * 写入ReadData命令 读取全量数据
     * data[0]=0x20；
     * data[1]~data[3]：保留，无意义；
     * data[4]：
     * 0x10：读取更新数据；
     * 0x11：读取flash数据，参数见data[5]~data[8]；
     * 0x20：停止数据输出，对所有读取数据指令有效；
     * 0x30：读取当前指针；
     * 0x31：设置当前指针；
     * data[5]：要读取的起始页,0~39；
     * data[6]：要读取的起始指针，0~63；
     * data[7]：要读取的结束页，0~39；
     * data[8]：要读取的结束指针，0~63；
     *
     * @return
     */
    public static byte[] encodeDataAllRead() {
        return BongUtil.hexStringToBytes(encodeDataAllReadString());
    }


    public static byte[] encodeStartOnceHeartTest() {
        return BongUtil.hexStringToBytes(BongHex.onceHeartStart);
    }

    public static byte[] encodeStopHeartOrKeepFit() {
        return BongUtil.hexStringToBytes(BongHex.heartRateOrKeepFitStop);
    }

    public static byte[] encodeReadOnceHeartRate() {
        return BongUtil.hexStringToBytes(BongHex.onceReadHeartRate);
    }

    public static byte[] encodeStartKeepFit() {
        return BongUtil.hexStringToBytes(BongHex.keepFitStart);
    }

    public static String encodeDataAllReadString() {
        return BongHex.hi2;
    }

    /**
     * 写入读取SensorDataRead设置
     *
     * @return
     */
    public static byte[] encodeSensorDataRead() {
        return BongUtil.hexStringToBytes(BongHex.hello1);
    }

    /**
     * 输出源数据(传感器 xyz 值);
     */
    public static byte[] encodeGetXYZ() {
        return BongUtil.hexStringToBytes(BongHex.fuck10);
    }

    /**
     * 输出校准后源数据(传感器 xyz 平方值);
     */
    public static byte[] encodeGetCalibrateXYZ() {
        return BongUtil.hexStringToBytes(BongHex.fuck11);
    }

    /**
     * 停止输出源数据,返回正常模式;
     */
    public static byte[] encodeStopXYZ() {
        return BongUtil.hexStringToBytes(BongHex.hello1);
    }


    /**
     * 震动指定次数，最多20
     *
     * @return
     */
    public static byte[] encodeVibrate(int times) {
        return BongUtil.hexStringToBytes(encodeVibrateString(times));
    }

    public static String encodeVibrateString(int times) {
        if (times < 0) {
            times = 1;
        }
        if (times > 20) {
            times = 20;
        }
        String l = BongHex.hehe1;
        if (times <= 15) {
            l += "0";
        }
        l += Integer.toHexString(times);
        return l;
    }

    /**
     * LED闪烁的编码 闪烁指定次数，最多20
     *
     * @return
     */
    public static byte[] encodeLight(int times) {
        if (times < 0) {
            times = 1;
        }
        if (times > 20) {
            times = 20;
        }
        String l = BongHex.hehe2;
        if (times <= 15) {
            l += "0";
        }
        l += Integer.toHexString(times);
        return BongUtil.hexStringToBytes(l);
    }


    /**
     * 震动一次手环的编码
     *
     * @return
     */
    public static byte[] encodeVibrate() {
        return BongUtil.hexStringToBytes(BongHex.hello2);
    }


    /**
     * 震动一次手环的编码
     *
     * @return
     */
    public static String encodeVibrateString() {
        return BongHex.hello2;
    }

    /**
     * 获取bongx相关信息
     *
     * @return
     */
    public static String encodeGetBongxInfoString() {
        return "2500000004";
    }

    /**
     * LED闪烁的编码 闪烁5次
     *
     * @return
     */
    public static byte[] encodeLight() {
        return BongUtil.hexStringToBytes(BongHex.hello3);
    }

    /**
     * LED闪烁的编码 闪烁5次
     *
     * @return
     */
    public static String encodeLightString() {
        return BongHex.hello3;
    }

    public static String encodeBongXBindLightString() {
        return "26000000210000000000006A";
    }

    public static String encodeBongXBindSmileString() {
        return "260000002100000000000065";
    }


    /**
     * 震动报时开关设置
     *
     * @return
     */
    public static byte[] encodeVibrateClock(boolean open) {
        return open ? BongUtil.hexStringToBytes(BongHex.ope2)
                : BongUtil.hexStringToBytes(BongHex.ope3);
    }

    /**
     * 设置 bongin 和 bongout
     *
     * @return
     */
    public static byte[] encodeBongInfo(int bongIn, int bongOut) {
        return BongUtil.hexStringToBytes(BongHex.wri1 + Integer.toHexString(bongIn) + Integer
                .toHexString(bongOut));
    }

    public static byte[] encodeStopOutput() {
        return BongUtil.hexStringToBytes("2100000020");
    }


    /**
     * 获取固件版本
     *
     * @return
     */
    public static String encodegetFirmwearString() {
        return BongHex.hello4;
    }

    /**
     * 获取SN
     *
     * @return
     */
    public static byte[] encodeBongSNInfo() {
        return BongUtil.hexStringToBytes(encodeBongSNInfoString());
    }

    /**
     * 获取SN
     *
     * @return
     */
    public static String encodeBongSNInfoString() {
        return BongHex.hello5;
    }


    /**
     * 开始升级命令
     *
     * @return
     */
    public static byte[] encodeUpdate() {
        return BongUtil.hexStringToBytes(encodeUpdateString());
    }

    public static String encodeUpdateString() {
        return BongHex.wri2;
    }


    /**
     * 震动报时开关设置
     *
     * @return
     */
    public static byte[] encodeLightClock(boolean open) {
        return open ? BongUtil.hexStringToBytes(BongHex.hehe4)
                : BongUtil.hexStringToBytes(BongHex.hehe5);
    }

    /**
     * 广播包开关设置
     *
     * @return
     */
    public static byte[] encodeBongBroadcast(boolean open) {
        return open ? BongUtil.hexStringToBytes(BongHex.hehe6)
                : BongUtil.hexStringToBytes(BongHex.hehe7);
    }

    /**
     * 恢复出厂设置
     *
     * @return
     */
    public static byte[] encodeBongRecoveryFactory() {
        return BongUtil.hexStringToBytes(BongHex.hehe8);
    }

    /**
     * 有哪些类型的数据需要解码
     */
    public enum DecodeDataType {
        DATA_BONG,  // 1~12月份 1分钟bongdata
        DATA_SENSOR,  // 传感器数据、电池电量
        DATA_RAW_BONG, // xyz数据
        DATA_UNKNOWN
    }


    public static byte[] encodePhoneCallVirbate(int times) {
        return BongUtil.hexStringToBytes(encodePhoneCallVirbateString(times));
    }

    private static String encodePhoneCallVirbateString(int times) {
        if (times > 20)
            times = 20;
        return String.format("260000002100%02X05140515", times);
    }

    /**
     * 手环 震动
     */
    public static String encodeBongXWriteVibrate() {
        //0x2600000021000305140514
        //return BongUtil.hexStringToBytes(BongHex.hehe8);
        //return BongUtil.hexStringToBytes("2600000021000305140514");
        return "2600000021000305140514";
    }

    /**
     * 手环 震动 和 动画
     * 000305140514 + 动画id
     */
//    public static String encodeBongXWriteVibrateAndAnimation(int vibrateTimes, SparseIntArray dataList, int animationId) {
//        //拿到震动list
//        StringBuilder sb = new StringBuilder("260000002100");//默认 0槽
//        String hex = Integer.toHexString(0xFF & vibrateTimes);
//        if (hex.length() == 1) {
//            sb.append('0');
//        }
//        sb.append(hex);
//        for (int i = 0; i < 4; i++) {
//            int v = dataList.get(i);
//            switch (v) {
//                case 0:
//                    sb.append(Const.BONGX_VIBRATE_EMPTY);
//                    break;
//                case 1:
//                    sb.append(Const.BONGX_VIBRATE_SHORT);
//                    break;
//                case 2:
//                    sb.append(Const.BONGX_VIBRATE_LONG);
//                    break;
//            }
//        }
//        //动画 存储编号
//        sb.append(String.format("%02x", animationCodeForX(animationId)));
//        return sb.toString();
//
////        return "2600000021000305140514";//00,03,05140514 0槽 震动3次 震动4帧
//
//
//        //0x2600000021000305140514
//        //return BongUtil.hexStringToBytes(BongHex.hehe8);
//        //return BongUtil.hexStringToBytes("2600000021000305140514");
////        return "2600000021000305140514";
//    }

//    public static String encodeBongXXWriteVibrateAndAnimation(int vibrateTimes, SparseIntArray dataList, int animationId) {
//        //拿到震动list
//        StringBuilder sb = new StringBuilder("260000002100");//默认 0槽
//        String hex = Integer.toHexString(0xFF & vibrateTimes);
//        if (hex.length() == 1) {
//            sb.append('0');
//        }
//        sb.append(hex);
//        for (int i = 0; i < 4; i++) {
//            int v = dataList.get(i);
//            switch (v) {
//                case 0:
//                    sb.append(Const.BONGX_VIBRATE_EMPTY);
//                    break;
//                case 1:
//                    sb.append(Const.BONGX_VIBRATE_SHORT);
//                    break;
//                case 2:
//                    sb.append(Const.BONGX_VIBRATE_LONG);
//                    break;
//            }
//        }
//        //动画 存储编号
//        sb.append(String.format("%02x", animationCodeForXX(animationId)));
//        return sb.toString();
//
////        return "2600000021000305140514";//00,03,05140514 0槽 震动3次 震动4帧
//
//
//        //0x2600000021000305140514
//        //return BongUtil.hexStringToBytes(BongHex.hehe8);
//        //return BongUtil.hexStringToBytes("2600000021000305140514");
////        return "2600000021000305140514";
//    }
    private static int animationCodeForX(int id) {
        switch (id) {
            case 1:
                return 105;//来电
            case 2:
                return 106;//bong
            case 3:
                return 103;//sms
            case 4:
                return 102;//we chat
            case 5:
                return 104;//qq
            case 6:
                return 101;//smile
            case 7:
                return 100;//hi

            case 101:
                return 101;//smile
            case 100:
                return 100;//hi
        }

        return 106;
    }

    private static int animationCodeForXX(int id) {
        switch (id) {
            case 1:
                return 100;//来电 64
            case 2:
                return 104;//bong 68
            case 3:
                return 101;//sms
            case 4:
                return 102;//we chat
            case 5:
                return 103;//qq 67
            case 6:
                return 105;//smile 0x69
            case 7:
                return 100;//hi
            case 8:
                return 106;//love 中性1-爱心
            case 9:
                return 107;//sun 中性2-太阳


            case 105:
                return 105;//smile 69
            case 106:
                return 106;//love 中性1-爱心
            case 107:
                return 107;//sun 中性2-太阳
            case 100:
                return 100;//hi
        }

        return 104;
    }

    /**
     * 手环 闪烁
     */
    public static String encodeBongXWriteAnimation() {
        //return BongUtil.hexStringToBytes(BongHex.hehe8);
        //0x2600000031000301040506010203040506010203
        //return BongUtil.hexStringToBytes("2600000031000301040506010203040506010203");
        return "2600000031000301040506010203040506010203";
    }

    public static String encodeCountDownString(int time, int triggerId, int vibrateId, int animationId) {
        StringBuilder sb = new StringBuilder("2900000002");//
        String hex = String.format("%04x", (0xFFFF & time));
        sb.append(hex);


        hex = Integer.toHexString(0xFF & triggerId);
        if (hex.length() == 1) {
            sb.append('0');
        }
        sb.append(hex);

        hex = Integer.toHexString(0xFF & vibrateId);
        if (hex.length() == 1) {
            sb.append('0');
        }
        sb.append(hex);

        hex = Integer.toHexString(0xFF & animationId);
        if (hex.length() == 1) {
            sb.append('0');
        }
        sb.append(hex);

        return sb.toString();
    }

    /**
     * 设置勿扰模式, mode == 0 其他参数无效
     *
     * @param mode 勿扰开关 1:开，0:关
     * @param sH   开始 小时
     * @param sM   开始 分钟
     * @param eH   结束 小时
     * @param eM   结束 分钟
     * @return
     */
    public static String encodeNoDisturb(int mode, int sH, int sM, int eH, int eM) {
        if (mode == 1) {
            return String.format("290000001001%02x%02x%02x%02x", sH, sM, eH, eM);
        } else {
            return "290000001000";
        }
    }

    /**
     * 设置目标步数
     *
     * @param steps
     * @return
     */
    public static String encodeTargetStepString(int steps) {
        StringBuilder sb = new StringBuilder("2900000001");//
        String hex = String.format("%08x", steps);
        sb.append(hex);

        return sb.toString();
    }

    /**
     * 设置目标防久坐目标
     *
     * @param open
     * @param time
     * @return
     */
    public static String encodeLongQuietTimeString(int open, int time) {

        return "2900000003" + String.format("%02x", open) + String.format("%04x", time);
    }

    /**
     * 设置动画库
     *
     * @param position
     * @param index
     * @param colors
     * @return
     */
    public static String encodeSaveAnimationString(int position, int index, SparseIntArray colors) {
        StringBuilder sb = new StringBuilder("2600000031");//

        //存储编号
        switch (index) {
            case 1:
            case 2:
            case 3:
                sb.append("ff");
                break;
            case 4:
                sb.append(String.format("%02x", position));
                break;
        }

        //重复次数
        sb.append("02");

        //帧的编号
        sb.append(String.format("%02x", index));


        for (int i = 0; i < 12; i++) {
            sb.append(String.format("%02x", colors.get(i)));
        }

        return sb.toString();
    }

    /**
     * 预览动画库
     *
     * @param position
     * @return
     */
    public static String encodePreviewAnimationString(int position) {
        String sb = "2600000022" + "00" +
                String.format("%02x", position);

        //震动 存储编号

        //动画 存储编号

        return sb;
    }

    /**
     * 读取动画库
     *
     * @return
     */
    public static String encodeReadAnimationString() {
        return "2500000006";
    }

    /**
     * 读取硬件信息
     *
     * @return notified: <00650003 00010108 000a07df 030c0000 00000000>
     */
    public static String encodeReadFirmwareString() {
        return "2500000004";
    }

    /**
     * 读取电量信息
     *
     * @return notified: <00650003 00010108 000a07df 030c0000 00000000>
     */
    private static String encodeReadBongXBatteryString() {
        return "2600000010";
    }

    /**
     * 支持亮任意图标的指令
     *
     * @return notified: <00650003 00010108 000a07df 030c0000 00000000>
     */
    public static String encodeLightBongXAnimationString(int id) {
        return encodeLightBongXAnimationString(id, 10);
    }

    public static String encodeLightBongXAnimationString(int id, int duration) {
        return "2600000032" + String.format("%02x", id) + String.format("%02x", duration);
    }

    /**
     * 恢复出厂设置
     *
     * @return
     */
    public static String encodeBongRecoveryFactoryString() {
        return BongHex.hehe8;
    }


    /**
     * @param openStatus
     * @return
     */
    public static String encodePlayAirPlanOpen(int openStatus) {
        return "2900000008" + String.format("%02x", openStatus);//00 关  01 开
    }

    /**
     * @param hiNick
     * @return
     */
    public static String encodeHiFriend(int id, String hiNick) {
        return "2900000006" + String.format("%02x", id) + HexUtil.encodeHexStr(hiNick.getBytes());
    }

    /**
     * @return
     */
    public static String encodeDeleteHiFriend(int id) {
        return "2900000006" + String.format("%02x", id) + "00";
    }

    /**
     * @param hiNick
     * @return
     */
    public static String encodeShowHiFriend(String hiNick) {
        hiNick = TextUtils.isEmpty(hiNick) ? "hi" : hiNick;
        return "2900000009" + HexUtil.encodeHexStr(hiNick.getBytes());
    }

    /**
     * @param cal
     * @return
     */
    public static String encodeEnergyCal(int cal) {
        return "290000000A" + String.format("%08x", cal);
    }

    /**
     * @param mins
     * @return
     */
    public static String encodeStandUpTime(int mins) {
        return "290000000B" + String.format("%04x", mins);
    }

    /**
     * @param gender
     * @param height
     * @param birthday
     * @return
     */
    public static String encodeBongUserInfo(int gender, int height, int birthday) {
        return "290000000C" + String.format("%02x", gender) + String.format("%04x", height) + String.format("%08x", birthday);
    }


    /**
     * 向精灵灯添加用户指令 20 00 ＋Mac ＋01 ＋55AA
     *
     * @param mac
     * @return
     */
    public static String encodeAddLightUser(String mac) {
        if (mac != null) {
            return "2000" + exChangePos(mac) + "0155AA";
        }
        return "2000" + mac + "0155AA";
    }


    /**
     * 删除用户
     * 指令：20 01+MAC +55AA
     *
     * @param mac
     * @return
     */
    public static String encodeDeleteuser(String mac) {
        if (mac != null) {

            return "2001" + exChangePos(mac) + "55AA";
        }
        return "2001" + mac + "55AA";
    }

    /**
     * 转化mac地址，让其反过来
     *
     * @return
     */

    private static String exChangePos(String mac) {
        if (mac != null) {
            String mac_no = mac.replace(":", "");
            String mac_yes = getRealMac(mac_no);
            String[] maclist = mac_yes.split(":");
            if (maclist == null || maclist.length != 6) {
                return mac_no;
            } else {
                StringBuffer buffer = new StringBuffer();
                for (int i = 5; i >= 0; i--) {
                    buffer.append(maclist[i]);
                }
                return buffer.toString();
            }
        }
        return mac;
    }

    private static String getRealMac(String mac) {
        if (mac != null) {
            StringBuffer macBuffer = new StringBuffer(mac);
            StringBuffer newBuffer = new StringBuffer();
            int length = macBuffer.length();
            for (int i = 0; i < length; i++) {
                newBuffer.append(macBuffer.charAt(i));
                if (i % 2 == 1 && i != length - 1) {
                    newBuffer.append(":");
                }
            }
            return newBuffer.toString();
        }
        return mac;
    }

    /**
     * 获取用户列表
     *
     * @return
     */
    public static String encodeUserList() {
        return "208055AA";
    }

    /**
     * 重置精灵灯（清空用户）
     *
     * @return
     */
    public static String encodeResetUser() {
        return "208155AA";
    }

    /**
     * 获取用户数量
     *
     * @return
     */
    public static String encodeUserCount() {
        return "208255AA";
    }

    /**
     * 开启精灵灯指令
     *
     * @return
     */
    public static String encodeOpenLight() {
        return "AA02";
    }

    /**
     * 关闭精灵灯指令
     *
     * @return
     */
    public static String encodeCloseLight() {
        return "AA03";
    }

    /**
     * 改变灯颜色
     *
     * @return
     */
    public static String encodeChangeColor() {
        return "AA01";
    }

    /**
     * 重启命令
     *
     * @return
     */
    public static String encodeRestartBong() {
        return "01";
    }

    /**
     * 获取bong2s固件信息
     *
     * @return
     */
    private static String encodeGetBong2sInfoString() {
        return "2500000004";
    }

    public static byte[] encodeGetBong2sInfoCode() {
        return BongUtil.hexStringToBytes(encodeGetBong2sInfoString());
    }

    /**
     * 2600000054+开关（00关，01开）
     *
     * @return
     */
    public static String encode2sHeartSwitch(boolean open) {
        String dif = open ? "01" : "00";
        return "2600000054" + dif;
    }


    /**
     * 2600000055+开关（00关，01开）
     * 久坐提醒
     *
     * @return
     */

    public static String encode2sSitReminder(boolean open) {
        String dif = open ? "01" : "00";
        return "2600000055" + dif;
    }

    /**
     * 电话提醒震动  长短交替震动
     *
     * @return
     */
    public static String encodeTelShock(int times) {

        String order = "2600000021" + "00";//00 代表立刻执行

        if (times <= 15) {
            order += "0";
        }
        order += Integer.toHexString(times);

        String suffix = "14001400";//短长短长 一个震动周期

        return order + suffix;
    }

}
