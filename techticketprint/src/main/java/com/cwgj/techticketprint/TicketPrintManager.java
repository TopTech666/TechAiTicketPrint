package com.cwgj.techticketprint;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rxbus.ecaray.com.rxbuslib.rxbus.RxBus;

/**
 * +----------------------------------------------------------------------
 * |  说明     ： 纸票打印机管理
 * +----------------------------------------------------------------------
 * | 创建者   :  ldw
 * +----------------------------------------------------------------------
 * | 时　　间 ：2018/7/18 09:59
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/
public class TicketPrintManager {

    private static final String TAG = "TicketPrintManager";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";

    //1秒轮询一次取纸状态
    public static final int POLL_TIME = 1*1000;

    //纸票被撕下
    public static final String TAG_TEAR_UP_PAPER = "tear_up_paper";
    //usb管理器
    private UsbManager mUsbManager;
    //usb驱动
    private UsbDriver mUsbDriver;
    //打印机1
    UsbDevice mUsbDev1;

    private Handler mHandler;

    private Runnable mRunnable;



    private static TicketPrintManager sTicketPrintManager;

    private TicketPrintManager(){}

    public static TicketPrintManager getInstance(){
        if(sTicketPrintManager == null){
            synchronized (TicketPrintManager.class){
                if(sTicketPrintManager == null){
                    sTicketPrintManager = new TicketPrintManager();
                }
            }
        }
        return sTicketPrintManager;
    }


    //初始化
    public void init(Context context){
        mHandler = new Handler();
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mUsbDriver = new UsbDriver(mUsbManager, context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(pendingIntent);
        // Broadcast listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);
    }


    public void testPrint() throws Exception {
        if(!printerConnStatus()){
            //打印机未连接
            throw new Exception("打印机未连接");
        }
        String msg = getPrinterStatusMsg(mUsbDev1);
        if(!TextUtils.isEmpty(msg)){
            //打印机异常
            throw new Exception(msg);
        }

        mUsbDriver.write(PrintCmd.SetClean(),mUsbDev1);  // 初始化，清理缓存
        // 小票标题
        mUsbDriver.write(PrintCmd.SetBold(0),mUsbDev1);  // 0 不加粗， 1加粗
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1); // 0 左、1 居中、2 右
		/*	int iHeight：放大高度，取值(1-8)
			int iWidth：放大宽度，取值(1-8)*/
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("标题", 0),mUsbDev1); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1);
        // 小票号码
        mUsbDriver.write(PrintCmd.SetBold(1),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("007", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetBold(0),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1);
        // 小票主要内容
        mUsbDriver.write(PrintCmd.PrintString("我就是测试下，呵呵哒", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1); // 打印走纸2行
        // 二维码
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);   //test 二维码靠左？？？
        mUsbDriver.write(PrintCmd.PrintQrcode("1234567890232543654756474868", 15, 8, 1),mUsbDev1);           // 【1】MS-D347,13 52指令二维码接口，环绕模式1
//			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 12, 2, 0),usbDev);           // 【2】MS-D245,MSP-100二维码，左边距、size、环绕模式0
//			mUsbDriver.write(PrintCmd.PrintQrCodeT500II(QrSize,Constant.WebAddress_zh),usbDev);// 【3】MS-532II+T500II二维码接口

        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);
        // 日期时间
        mUsbDriver.write(PrintCmd.SetAlignment(2),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString(sdf.format(new Date()).toString()
                + "\n\n", 1),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);
        // 一维条码
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1);
        mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "A12345678Z"),mUsbDev1);// 一维条码打印
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1);
//			 20170804 特殊测试
//			mUsbDriver.write(PrintCmd.SetWhitemodel(1), usbDev);
//			byte[] etBytes = PrintCmd.PrintString(Constant.m_PrintDataCN, 0);
//			mUsbDriver.write(etBytes, usbDev);
        // 走纸换行、切纸、清理缓存
        setFeedCutClean(mUsbDev1);

        pollingPaperTimeTask();
    }

   //打印进场小票
    public void printInParkTicket( String parkName, String inParkTime, String inParkQrCodeStr) throws Exception {
        if(!printerConnStatus()){
            //打印机未连接
            throw new Exception("打印机未连接");
        }
        String msg = getPrinterStatusMsg(mUsbDev1);
        if(!TextUtils.isEmpty(msg)){
            //打印机异常
            throw new Exception(msg);
        }
        // 停车小票凭证
        mUsbDriver.write(PrintCmd.SetBold(0),mUsbDev1);      // 0 不加粗， 1加粗
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1); // 0 左、1 居中、2 右
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1),mUsbDev1); //  int iHeight：放大高度，取值(1-8)  int iWidth：放大宽度，取值(1-8)
        mUsbDriver.write(PrintCmd.PrintString("停车小票凭证", 0),mUsbDev1); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1); // 打印走纸1行
        // ***停车场欢迎您
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "%s欢迎您", parkName), 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1); // 打印走纸2行
        //进场时间 2018-01-01 12:00:00
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1); //  int iHeight：放大高度，取值(1-8)  int iWidth：放大宽度，取值(1-8)
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "进场时间 %s", inParkTime), 0),mUsbDev1); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1); // 打印走纸2行
        //进场二维码
        mUsbDriver.write(PrintCmd.PrintQrcode(inParkQrCodeStr, 12, 8, 1),mUsbDev1);   //  内容  左边距 单位长度（QR码大小  1- 8）  环绕模式
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1);  // 打印走纸2行
        //微信扫码付费离场
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1),mUsbDev1); //  int iHeight：放大高度，取值(1-8)  int iWidth：放大宽度，取值(1-8)
        mUsbDriver.write(PrintCmd.PrintString("微信扫码付费离场", 0),mUsbDev1); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1);  // 打印走纸2行
        //温馨提示：请在出场时提前扫码支付停车费用，出场时使用出口读票机读取二维码出场，祝您用车愉快，客服电话 400-344-222
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1); //  int iHeight：放大高度，取值(1-8)  int iWidth：放大宽度，取值(1-8)
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1); // 0 左、1 居中、2 右
        mUsbDriver.write(PrintCmd.PrintString("温馨提示：", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("请在出场时提前扫码支付停车费用，出场时使用出口读票机读取二维码出场，祝您用车愉快。", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("客服电话 400-344-222", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1);  // 打印走纸1行
        // 走纸换行、切纸、清理缓存
        setFeedCutClean(mUsbDev1);

        pollingPaperTimeTask();
    }

    //打印进场小票
    public void printOutParkTicket( String parkName, String inParkTime,String outParkTime, String parkingDuration, String parkingMoney, String payMode, String wxQrCode) throws Exception {
        if(!printerConnStatus()){
            //打印机未连接
            throw new Exception("打印机未连接");
        }
        String msg = getPrinterStatusMsg(mUsbDev1);
        if(!TextUtils.isEmpty(msg)){
            //打印机异常
            throw new Exception(msg);
        }
        // 停车收费凭证
        mUsbDriver.write(PrintCmd.SetBold(0),mUsbDev1);      // 0 不加粗， 1加粗
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1); // 0 左、1 居中、2 右
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1),mUsbDev1); //  int iHeight：放大高度，取值(1-8)  int iWidth：放大宽度，取值(1-8)
        mUsbDriver.write(PrintCmd.PrintString("停车收费凭证", 0),mUsbDev1); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1); // 打印走纸1行
        // ***停车场欢迎您
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "%s欢迎您", parkName), 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(2),mUsbDev1); // 打印走纸2行

        mUsbDriver.write(PrintCmd.SetSizetext(0, 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1); // 0 左、1 居中、2 右
        //进场时间 2018-01-01 12:00:00
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "进场时间: %s", inParkTime), 0),mUsbDev1);
        //离场时间 2018-01-01 13:00:00
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "离场时间: %s", outParkTime), 0),mUsbDev1);
        //停车时长：2小时
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "停车时长: %s", parkingDuration), 0),mUsbDev1);
        //停车费用：20元
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "停车费用: %s", parkingMoney), 0),mUsbDev1);
        //支付方式：微信支付
        mUsbDriver.write(PrintCmd.PrintString(String.format(Locale.getDefault(), "支付方式: %s", payMode), 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1); // 打印走纸1行
        //微信公众号二维码
        mUsbDriver.write(PrintCmd.PrintQrcode(wxQrCode, 12, 8, 1),mUsbDev1);   //  内容  左边距 单位长度（QR码大小  1- 8）
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1);  // 打印走纸1行
        //车位管家，扫一扫关注，让停车更智惠
        mUsbDriver.write(PrintCmd.SetAlignment(1),mUsbDev1); // 0 左、1 居中、2 右
        mUsbDriver.write(PrintCmd.PrintString("车位管家,扫一扫关注,让停车更智惠", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1);  // 打印走纸1行
        //温馨提示：本小票仅为停车收费凭证，不作为报销使用，祝您用车愉快。  客服电话 400-344-222
        mUsbDriver.write(PrintCmd.SetAlignment(0),mUsbDev1); // 0 左、1 居中、2 右
        mUsbDriver.write(PrintCmd.PrintString("温馨提示：", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("本小票仅为停车收费凭证，不作为报销使用，祝您用车愉快。", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintString("客服电话 400-344-222", 0),mUsbDev1);
        mUsbDriver.write(PrintCmd.PrintFeedline(1),mUsbDev1);  // 打印走纸1行
        // 走纸换行、切纸、清理缓存
        setFeedCutClean(mUsbDev1);

        pollingPaperTimeTask();
    }



    // 走纸换行、切纸、清理缓存
    private void setFeedCutClean(UsbDevice usbDev) {
        mUsbDriver.write(PrintCmd.PrintFeedline(4),usbDev);      // 走纸换行
        mUsbDriver.write(PrintCmd.PrintCutpaper(1),usbDev);    // 切纸类型  1半切  0全切
        mUsbDriver.write(PrintCmd.SetClean(),usbDev);            // 清理缓存
    }

    //定时任务轮询纸票是否被撕下
    private void pollingPaperTimeTask(){
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mHandler == null){
            mHandler = new Handler();
        }
        if(mRunnable == null){
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    //1秒轮询下取纸的状态
                    Log.d(TAG, "轮询纸票是否撕下");
                    if(getTearUpPaper()){
                        RxBus.getDefault().post(TAG_TEAR_UP_PAPER);
                        if(mHandler!=null)
                            mHandler.removeCallbacksAndMessages(null);
                        return;
                    }
                    mHandler.postDelayed(this, POLL_TIME);
                }
            };
        }
        //考虑到打印纸票的时间，这里延时5s去检测,否则会轮询到上次撕票的状态
        mHandler.postDelayed(mRunnable, POLL_TIME *5);

    }


    //自检页打印
    public void  printSelfPageTest() throws Exception {
        if(!printerConnStatus()){
            //打印机未连接
            throw new Exception("打印机未连接");
        }
        String msg = getPrinterStatusMsg(mUsbDev1);
        if(!TextUtils.isEmpty(msg)){
            //打印机异常
            throw new Exception(msg);
        }
        try {
            byte[] bSend = PrintCmd.PrintSelfcheck();
            if (bSend != null) {
                mUsbDriver.write(bSend);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pollingPaperTimeTask();
    }

    // 检测小票打印机是否连接
    private boolean printerConnStatus() {

        boolean bPrinterConn = false;
        try {
            if (!mUsbDriver.isConnected()) {
                for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                        bPrinterConn = mUsbDriver.usbAttached(device);
                        if (!bPrinterConn){
                            Log.d(TAG, "printConnStatus: 打印机设备usb连接失败");
                            continue;
                        }
                        bPrinterConn = mUsbDriver.openUsbDevice(device);
                        if (bPrinterConn) {
                            if (device.getProductId() == 8211){
                                mUsbDev1 = device;
                                Log.d(TAG, "printConnStatus: 打印机设备1打开成功");
                                break;
                            }
                        }else {
                            Log.d(TAG, "printConnStatus: 打印机设备打开失败");
                        }
                    }
                }
            } else {
                bPrinterConn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            bPrinterConn = false;
        }
        return bPrinterConn;
    }




    //获取打印机的工作状态
    private int getPrinterStatus(UsbDevice usbDev){
        int iRet = -1;
        // 1
        byte[] bRead1 = new byte[1];
        byte[] bWrite1 = PrintCmd.GetStatus1();
        if(mUsbDriver.read(bRead1,bWrite1,usbDev)>0) {
            iRet = PrintCmd.CheckStatus1(bRead1[0]);
        }
        if(iRet!=0)
            return iRet; //打印机异常情况退出
        // 2
        byte[] bRead2 = new byte[1];
        byte[] bWrite2 = PrintCmd.GetStatus2();
        if(mUsbDriver.read(bRead2,bWrite2,usbDev)>0) {
            iRet = PrintCmd.CheckStatus2(bRead2[0]);
        }
        if(iRet!=0)
            return iRet;
        // 3
        byte[] bRead3 = new byte[1];
        byte[] bWrite3 = PrintCmd.GetStatus3();
        if(mUsbDriver.read(bRead3,bWrite3,usbDev)>0) {

            iRet = PrintCmd.CheckStatus3(bRead3[0]);
        }
        if(iRet!=0)
            return iRet;
        // 4
        byte[] bRead4 = new byte[1];
        byte[] bWrite4 = PrintCmd.GetStatus4();
        if(mUsbDriver.read(bRead4,bWrite4,usbDev)>0) {
            iRet = PrintCmd.CheckStatus4(bRead4[0]);
        }
        return iRet;
    }


    /*	0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
        3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽*/
    private String getPrinterStatusMsg(UsbDevice usbDev){
        int status = getPrinterStatus(usbDev);
        String msg ="";
        if(status == 1){
            msg ="打印机未连接或未上电";
        }else if(status == 2){
            msg ="打印机和调用库不匹配";
        }else if(status == 3){
            msg ="打印头打开";
        }else if(status == 4){
            msg ="切刀未复位";
        }else if(status == 5){
            msg ="打印头过热";
        }else if(status == 6){
            msg ="黑标错误";
        }else if(status == 7){
            msg ="纸尽";
        }else if(status == 8){
            msg ="纸将尽";
        }
        return msg;
    }

    //出口无纸 ： 1c  10 出口有纸 ： 1e  12
    public boolean getTearUpPaper(){
        byte[] bRead4 = new byte[1];
        byte[] bWrite4 = PrintCmd.GetStatus4();
        if(mUsbDriver.read(bRead4,bWrite4,mUsbDev1)>0) {
            int x = bRead4[0];
            Log.d(TAG, "getTearUpPaper: " + x);
            if(x == 0x10){
                //小票被撕下
               return true;
            }
        }
        return false;
    }

    /*
    *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
    *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
    */
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if(mUsbDriver.usbAttached(intent)) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device.getProductId() == 8211 && device.getVendorId() == 1305) {
                        if(mUsbDriver.openUsbDevice(device)){
                            Log.d(TAG, "onReceive: 打印机已连接1" );
                            mUsbDev1 = device;
                        }

                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device.getProductId() == 8211 && device.getVendorId() == 1305) {
                    if(mUsbDriver.closeUsbDevice(device)){
                        Log.d(TAG, "onReceive: 打印机已移除" );
                        mUsbDev1 = null;
                    }

                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device.getProductId() == 8211 && device.getVendorId() == 1305) {
                            if(mUsbDriver.openUsbDevice(device)) {
                                Log.d(TAG, "onReceive: 打印机已连接_permiss" );
                                mUsbDev1 = device;
                            }
                        }
                    }
                }
            }
        }
    };

}
