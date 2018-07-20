package com.cwgj.techticketprint;

public class Constant {
    public static String mBarCodeData = "123456789";
    public static String mBmpPathData = "/storage/sdcard0/DCIM/test.bmp";
    public static String mBmpPath = "/storage/sdcard0/DCIM/";
    //	public static String WebAddress = "www.oteinternational.com";
//	public static String WebAddress_zh = "你好";
    public static String WebAddress = "Welcome";
    public static String WebAddress_zh = "欢迎使用";

    public static String m_strCN1 = "正常";
    public static String m_strCN2 = "缺纸";
    public static String m_strCN3 = "异常";
    public static String m_strCN4 = "获取状态失败";

    public static String m_strUS1 = "Normal";
    public static String m_strUS2 = "Out of paper";
    public static String m_strUS3 = "Abnormal";
    public static String m_strUS4 = "Gets status failure";

    public static String TESTDATA_CN = "您好中国,欢迎光临!" + "\n"
            + "您好中国,欢迎光临!" + "\n"
            + "您好中国,欢迎光临!" + "\n"
            + "您好中国,欢迎光临!" + "\n\n";
    public static String TESTDATA_US = "Hello China, Welcome！" + "\n\n"
            + "Hello China, Welcome！" + "\n\n"
            + "Hello China, Welcome！" + "\n\n"
            + "Hello China, Welcome！" + "\n\n";
    public static String TESTDATA_US2 = "Shenzhen Meisong Technology Co., Ltd. was founded in 2006, "
            + "2013 by the state as a high-tech enterprises, through the "
            + "ISO9001 quality system certification.Headquartered in "
            + "Shenzhen (Shenzheng), the establishment of branches in Beijing,"
            + "(India), Iran, Britain and other places have agents." + "\n";

    public static String m_PrintDataCN = "1,结构一体化：传统80mm热敏方案都是主板及打印头切刀分开，用户需自行设计"
            + "结构件自行安装，该模组结构小巧彻底解决客户设计难度，周期"
            + "长安装麻烦等问题，只需固定原装打印头定位即可，实现一体化可直接安装到客户"
            + "设备中；\n"
            + "2,打印速度得到质的提升，目前我们设计的速度最高可实现170mm/s;\n"
            + "3,超大装纸支架，纸卷直径可达180mm\n"
            + "4,减少空间位置及布线的繁琐，有效提升产品组装过程的合格率，可选择完全兼容epsonM-T532打印头的安装尺寸，无需做任何改动；\n"
            + "5,接口多样化：串口，USB，并口可供选择，并提供全面的软件指令及简介的驱动程序；\n"
            + "6,具有强大的防堵纸，防拽纸及出纸提示闪灯功能，让您省心省事！\n"
            + "7,具有自动送纸，自动收纸回收票据功能，让一切变的人性化！\n\n\n";
    public static String m_PrintDataUS = "Small panel structure, using the latest ARM design "
            + " and printers movement in the same institution, reduce the space of the installation, the "
            + " installation of improve product reliability; The design is exquisite, stable performance "
            + " and fashion appearance. With automatic feed, automatic paper cutting, paper detection, "
            + " and other functions, to make the end user to use more simple and smooth\n"
            + "Interface:Serial port(RS232 or TTL),USB\n\n\n";

    // 小票打印文字中英文对照
    public static int ADD_NUM = 1000;
    public static String TITLE_CN = "中国农业银行\n\n" + "办理业务(一)\n\n";
    public static String TITLE_US = "Agricultural Bank China\n\n" + "Transact business (1)\n\n";
    public static String QUEUE_NUMBER = String.valueOf(ADD_NUM) + "\n\n";
    public static String STRDATA_CN = "您前面有 10 人等候，请注意叫号\n\n"
            + "欢迎光临！我们将竭诚为你服务。\n";
    public static String STRDATA_US = "There are 10 people waiting in front of you, please note the number\n\n"
            + "Welcome! We will serve you wholeheartedly.\n";

    /*
     *  0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
     *  3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
     */
    public static String Receive_CN = "接收[十六进制]：";
    public static String Receive_US = "Receive[Hex]:";
    public static String State_CN = "\r\n状态：";
    public static String State_US = "\r\nState:";
    public static String Normal_CN = "  正常;";
    public static String Normal_US = "  Normal;";
    public static String NoConnectedOrNoOnPower_CN = "  打印机未连接或未上电;";
    public static String NoConnectedOrNoOnPower_US = "  Printer is not connected or not on power;";
    public static String PrinterAndLibraryNotMatch_CN = "  打印机和调用库不匹配;";
    public static String PrinterAndLibraryNotMatch_US = "  Printer and library does not match;";
    public static String PrintHeadOpen_CN = "  打印头打开;";
    public static String PrintHeadOpen_US = "  Print head open;";
    public static String CutterNotReset_CN = "  切刀未复位;";
    public static String CutterNotReset_US = "  Cutter not reset;";
    public static String PrintHeadOverheated_CN = "  打印头过热;";
    public static String PrintHeadOverheated_US = "  Print head overheated;";
    public static String BlackMarkError_CN = "  黑标错误;";
    public static String BlackMarkError_US = "  Black mark error;";
    public static String PaperExhausted_CN = "  纸尽;";
    public static String PaperExhausted_US = "  PaperExhausted;";
    public static String PaperWillExhausted_CN = "  纸将尽;";
    public static String PaperWillExhausted_US = "  Paper will exhausted;";
    public static String Abnormal_CN = "  异常;";
    public static String Abnormal_US = "  Abnormal;";

    public static String LoadBmpPath = "/storage/sdcard0/DCIM/test1.bmp;" +
            "/storage/sdcard0/DCIM/test2.bmp;" +
            "/storage/sdcard0/DCIM/test3.bmp;" +
            "/storage/sdcard0/DCIM/small2v.bmp;" +
            "/storage/sdcard0/DCIM/logo2.bmp;" +
            "/storage/sdcard0/DCIM/logo3.bmp;";

    public static String LoadBmpPath2 = "/storage/sdcard0/DCIM/logo2.bmp;" +
            "/storage/sdcard0/DCIM/logo3.bmp;" +
            "/storage/sdcard0/DCIM/small2v.bmp;" +
            "/storage/sdcard0/DCIM/test3.bmp;" +
            "/storage/sdcard0/DCIM/test1.bmp;" +
            "/storage/sdcard0/DCIM/test2.bmp;";
    public static String LoadBmpPath3 = "/mnt/sdcard/DCIM/BMP/logo1.bmp;" +
            "/mnt/sdcard/DCIM/BMP/logo2.bmp;" +
            "/mnt/sdcard/DCIM/BMP/logo3.bmp;";
}
