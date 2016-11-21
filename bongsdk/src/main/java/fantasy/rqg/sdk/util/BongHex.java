package fantasy.rqg.sdk.util;

/**
 * SN : SN：2700000011
 * <p/>
 * <p/>
 * SN意义：
 * 第一个字节为颜色
 * 01 金
 * 02 灰
 * 03 红
 * 04 蓝
 * 05 黑
 * 后三个为生产年月日，年从2000年算起，当前年-2000
 * 0E 09 0A
 * 2014年9月10生产
 * <p/>
 * sleep:
 * 写入后蓝牙会主动断开，长期处于待机模式。长按触摸按键五秒，返回正常工作模式。
 * <p/>
 * hello4:
 * * 第一个字节为硬件版本02
 * 01为bongI（一代bong）
 * 02为bongII（二代bong）
 * 后三个字节为软件版本1.2.3，每个字节代表一个版本级别
 *
 * @author MaTianyu
 * @date 2014-08-17
 */
public class BongHex {
    /**
     * public String fuck10 = "2100FF0010";//xyz
     * public String fuck11 = "2100FF0011";//校准的xyz
     * public String fuck20 = "2100FF0020";//停止xyz
     * public String hi1    = "2000000010";// 读取 增量数据
     * public String hi2    = "20000000110000ffff";//读取 全量数据
     * public String hello1 = "26FFFFFF10";// 读取 电池 传感器
     * public String hello2 = "26FFFFFF2001";// 震动
     * public String hello3 = "26FFFFFF3005";// 闪灯
     * public String wri1   = "24000000";// bongin bongout
     * public String hello4 = "2500000001";// 固件信息
     * public String wri2   = "27FFFFFF55";// 固件升级
     * public String hello5 = "2700000011";// SN信息
     * public String ope1   = "2700000001";// 休眠
     * public String ope2   = "2700000030";// 震动报时打开
     * public String ope3   = "2700000031";// 震动报时关闭
     * public String hehe1 = "26FFFFFF20";// 震动基础命令
     * public String hehe2 = "26FFFFFF30";// 闪灯基础命令
     * public String hehe3 = "2000000013";// 数据同步
     * <p/>
     * public String hehe4 = "2700000030";// 闪烁打开 27
     * public String hehe5 = "2700000031";// 闪烁关闭 27
     * <p/>
     * public String hehe6 = "2700000041";// 广播打开 27
     * public String hehe7 = "2700000040";// 广播关闭 27
     * <p/>
     * public String hehe8 = "2700000002";// 清零设置
     **/
    private static final long serialVersionUID = -5986054650418210313L;

    // hack 命令
    public static final String fuck10 = "2100FF0010";//xyz
    public static final String fuck11 = "2100FF0011";//校准的xyz
    public static final String fuck20 = "2100FF0020";//停止xyz
    public static final String hi1 = "2000000010";// 读取 增量数据
    public static final String hi2 = "20000000110000ffff";//读取 全量数据
    public static final String hello1 = "26FFFFFF10";// 读取 电池 传感器
    public static final String hello2 = "26FFFFFF2001";// 震动
    public static final String hello3 = "26FFFFFF3005";// 闪灯
    public static final String wri1 = "24000000";// bongin bongout
    public static final String hello4 = "2500000001";// 固件信息
    public static final String wri2 = "27FFFFFF55";// 固件升级
    public static final String hello5 = "2700000011";// SN信息
    public static final String ope1 = "2700000001";// 休眠
    public static final String ope2 = "2700000030";// 震动报时打开
    public static final String ope3 = "2700000031";// 震动报时关闭
    public static final String hehe1 = "2600000020";// 震动基础命令
    public static final String hehe2 = "2600000030";// 闪灯基础命令
    public static final String hehe3 = "2000000013";// 数据同步
    public static final String heartSync = "2000000014";// 数据同步
    public static final String hehe4 = "2700000030";// 闪烁打开 27
    public static final String hehe5 = "2700000031";// 闪烁关闭 27
    public static final String hehe6 = "2700000041";// 广播打开 27
    public static final String hehe7 = "2700000040";// 广播关闭 27
    public static final String hehe8 = "2700000002";// 清零设置
    public static final String dfu = "2700000055";// dfu mode
    public static final String onceHeartStart = "2600000050";// start heart test
    public static final String onceReadHeartRate = "2600000052";
    public static final String keepFitStart = "2600000051";
    public static final String heartRateOrKeepFitStop = "2600000053";
    public static final String bodyTestOn = "2100000012";
    public static final String bodyTestOff = "2100000020";
    public static final String lightBong = "2900000017";
    public static final String bindSuccess = "2900000018";


    //@Override
    //public String toString() {
    //    return "BongHex{" +
    //            "update read ='" + hi1 + '\'' +
    //            ", all read = '" + hi2 + '\'' +
    //            ", sensor ='" + hello1 + '\'' +
    //            ", shake ='" + hello2 + '\'' +
    //            ", light ='" + hello3 + '\'' +
    //            ", bongIn$Out ='" + wri1 + '\'' +
    //            ", firmware ='" + hello4 + '\'' +
    //            ", update ='" + wri2 + '\'' +
    //            ", sn ='" + hello5 + '\'' +
    //            ", sleep='" + ope1 + '\'' +
    //            ", vibrate open ='" + ope2 + '\'' +
    //            ", vibrate close ='" + ope3 + '\'' +
    //            ", xyz ='" + fuck10 + '\'' +
    //            ", cali xyz ='" + fuck11 + '\'' +
    //            ", stop xyz ='" + fuck20 + '\'' +
    //            '}';
    //}
    //
    /**
     public static void main(String[] a) {
     try {
     String json = Json.get().toJson(new BongHex());
     char[] chars = Base64Coder.encode(json.getBytes("utf-8"));
     //String str = new String(chars);
     //chars = Base64Coder.encode(str.getBytes("utf-8"));
     String str = new String(chars);
     str = HexUtil.encodeHexStr(str.getBytes("utf-8"));
     str = HexUtil.encodeHexStr(str.getBytes("utf-8"));
     System.out.println("encode: " + str);

     byte[] bytes = HexUtil.decodeHex(str.toCharArray());
     str = new String(bytes, "utf-8");
     bytes = HexUtil.decodeHex(str.toCharArray());
     str = new String(bytes, "utf-8");
     bytes = Base64Coder.decode(str.toCharArray());
     str = new String(bytes, "utf-8");
     //bytes = Base64Coder.decode(str.toCharArray());
     //str = new String(bytes, "utf-8");

     System.out.println("decode: BongHex" + str);
     System.out.println("json  : " + Json.get().toObject(str, BongHex.class));

     } catch (UnsupportedEncodingException e) {
     e.printStackTrace();
     }
     }
     **/

}
