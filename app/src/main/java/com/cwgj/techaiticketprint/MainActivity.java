package com.cwgj.techaiticketprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cwgj.techticketprint.Constant;
import com.cwgj.techticketprint.HuaruiPrintUtils;
import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;



@SuppressLint("SimpleDateFormat") 
@SuppressWarnings("unused")
public class MainActivity extends Activity{

	private static final String TAG = "MainActivity";
	
	private static final int FILE_SELECT_CODE = 0;  
	private Context mContext;
	UsbDriver mUsbDriver;
	UsbDevice mUsbDev1;		//打印机1
	UsbDevice mUsbDev2;		//打印机2
	// Control definition 控件定义
	private Button mPrintSelfPage,mPrintTest,mPrintTicket,mClear;
	private Button mBmpLoad,mBmpPrint,mClearPath,mAddBmpFilePath,mAddImgFilePath;
	private EditText etWrite,editRecDisp,mBmpPath_et,mTestTimes,mImgPath_et;
	private CheckBox IsAddLoadBmpPath;
	// 代表性的支持安卓USB口打印机QrCode函数调用：MS-D347、MS-D245（N58V）、T500II
	private Button D347QrBtn,D245QrBtn,T500IIQrBtn; 
	private Button Print_1DBar_Btn;
	private Button Print_Imgfile_Btn,PrintSeatBtn;
	private static ImageView printImg;
	private EditText HTStr1,HTStr2,HTStr3,HTColumn1,HTColumn2; // 水平制表输入内容与列信息
	private TextView mHTSeatStr; // 水平制表结果显示
	// Common variables 常用变量全局
	private int rotate = 0;       // 默认为:0, 0 正常、1 90度旋转 
	private int align = 0;        // 默认为:1, 0 靠左、1  居中、2:靠右
	private int underLine = 0;    // 默认为:0, 0 取消、   1 下划1、 2 下划2 
	private int linespace = 40;   // 默认40, 常用：30 40 50 60
	private int cutter = 0;       // 默认0，  0 全切、1 半切
	static int Number = 1000;
	private int QrSize = 1;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 国际化标志时间格式类
	SimpleDateFormat m_sdfDate = new SimpleDateFormat("HH:mm:ss ");     // 国际化标志时间格式类
	private String title = "", strData = "", num = "",codeStr = "";
	
	private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";
	private UsbManager mUsbManager;
	private UsbDevice m_Device;
	private boolean shareFlag = false;
	private int clickFlag = 1; // 1：BMP；0：IMG
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        init();// 初始化
    }
    /**
     * 初始化
     */
    private void init() {
    	findView();
    	getUsbDriverService();
    	setListener();
    	getMsgByLanguage();
	}
	// 绑定控件
	private void findView() {
		// 20170518 路径选择监听及输入框控件Add
		mAddBmpFilePath = (Button) findViewById(R.id.SelectBmpFile);
		mBmpPath_et = (EditText) findViewById(R.id.Nvbmp_path_et);
		mClearPath = (Button) findViewById(R.id.Clear_Path_Btn);
		mAddImgFilePath = (Button) findViewById(R.id.SelectImgFile);
		mImgPath_et = (EditText) findViewById(R.id.Img_path_et);
		// 二维码内容 + 一维码类型选择输入框
		etWrite = (EditText) findViewById(R.id.InputContent_et);
		mTestTimes =  (EditText) findViewById(R.id.TestTimes_et);
		mPrintSelfPage = (Button) findViewById(R.id.PrintSelfPage_btn);// 20170614自检页打印
		mPrintTest = (Button) findViewById(R.id.PrintTest_btn);
		mPrintTicket = (Button) findViewById(R.id.PrintTicket_btn);
		editRecDisp = (EditText) findViewById(R.id.Get_State_et); // 打印机状态显示框
		mClear = (Button) findViewById(R.id.Clear_btn);           // 打印机状态清除按钮
		QrSize = Integer.valueOf(mTestTimes.getText().toString().trim());
		etWrite.setText(Constant.WebAddress);
		// 20160826 Add
		mBmpLoad = (Button) findViewById(R.id.Load_nvbmp_btn);
		mBmpPrint = (Button) findViewById(R.id.Print_nvbmp_btn);
		IsAddLoadBmpPath = (CheckBox) findViewById(R.id.Is_AddLoadBmpPath);
		IsAddLoadBmpPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){ 
					mBmpLoad.setVisibility(View.VISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Close));
                }else{ 
                	mBmpLoad.setVisibility(View.INVISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Open));
                } 
			}
		});
		D347QrBtn = (Button)findViewById(R.id.D347QrBtn);
		D245QrBtn = (Button)findViewById(R.id.D245QrBtn);
		T500IIQrBtn = (Button)findViewById(R.id.T500IIQrBtn);
		Print_1DBar_Btn =  (Button)findViewById(R.id.Print_1DBar_Btn);
		Print_Imgfile_Btn = (Button)findViewById(R.id.Print_Imgfile_Btn);
		// 水平制表
		HTStr1 =  (EditText)findViewById(R.id.SeatContent1_et);
		HTColumn1 =  (EditText)findViewById(R.id.SetColumn1_et);
		HTStr2 =  (EditText)findViewById(R.id.SeatContent2_et);
		HTColumn2 =  (EditText)findViewById(R.id.SetColumn2_et);
		HTStr3 =  (EditText)findViewById(R.id.SeatContent3_et);
		PrintSeatBtn = (Button)findViewById(R.id.Print_Seat_Btn);
		mHTSeatStr = (TextView)findViewById(R.id.HTSeat_tv);
	}
	
	
	private void getUsbDriverService(){
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mUsbDriver = new UsbDriver(mUsbManager, this);
		PendingIntent permissionIntent1 = PendingIntent.getBroadcast(this, 0,
				new Intent(ACTION_USB_PERMISSION), 0);
		mUsbDriver.setPermissionIntent(permissionIntent1);
		
		// Broadcast listen for new devices
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
	}

	// 设置监听
	private void setListener() {
		mPrintSelfPage.setOnClickListener(new PrintClickListener());
		mPrintTest.setOnClickListener(new PrintClickListener());
		mPrintTicket.setOnClickListener(new PrintClickListener());
		mClear.setOnClickListener(new PrintClickListener());
		mBmpLoad.setOnClickListener(new PrintClickListener());
		mBmpPrint.setOnClickListener(new PrintClickListener());
		// 打印机各型号QrCode二维码
		D347QrBtn.setOnClickListener(new PrintClickListener());
		D245QrBtn.setOnClickListener(new PrintClickListener());
		T500IIQrBtn.setOnClickListener(new PrintClickListener());
		// 打印机各型号一维码各种类型
		Print_1DBar_Btn.setOnClickListener(new PrintClickListener());
		Print_Imgfile_Btn.setOnClickListener(new PrintClickListener());
		PrintSeatBtn.setOnClickListener(new PrintClickListener());
		mAddBmpFilePath.setOnClickListener(new BmpBrowerClickListener());
		mAddImgFilePath.setOnClickListener(new BmpBrowerClickListener());
		mClearPath.setOnClickListener(new BmpBrowerClickListener());
	}
	
	// Get UsbDriver(UsbManager) service
	private boolean PrintConnStatus() {

		boolean blnRtn = false;
		try {
			if (!mUsbDriver.isConnected()) {
//				UsbManager m_UsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
				// USB线已经连接
				for (UsbDevice device : mUsbManager.getDeviceList().values()) {
					if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
							|| (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
						blnRtn = mUsbDriver.usbAttached(device);
						if (blnRtn == false)
							break;
						blnRtn = mUsbDriver.openUsbDevice(device);
						// 打开设备
						if (blnRtn) {
							if (device.getProductId() == 8211)
								mUsbDev1 = device;
							else
								mUsbDev2 = device;
							Toast.makeText(this,getString(R.string.USB_Driver_Success),
									Toast.LENGTH_SHORT).show();
							break;
						} else {
							Toast.makeText(this,getString(R.string.USB_Driver_Failed),Toast.LENGTH_SHORT).show();
							break;
						}
					}
				}
			} else {
				blnRtn = true;
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		return blnRtn;
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
					if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
							|| (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
						if(mUsbDriver.openUsbDevice(device)) {
							Log.d(TAG, "onReceive: 打印机已连接1" );
		 					if(device.getProductId()==8211)
		 						mUsbDev1 = device;
		 					else
		 						mUsbDev2 = device;
	 					 }
					}
 				}
 			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
							|| (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
		 				mUsbDriver.closeUsbDevice(device);
						Log.d(TAG, "onReceive: 打印机已移除" );
						if(device.getProductId()==8211)
							mUsbDev1 = null;
						else
							mUsbDev2 = null;
					}
 			} else if (ACTION_USB_PERMISSION.equals(action)) {
 	             synchronized (this) {
 	                 UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 	                 if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
 								|| (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
							Log.d(TAG, "onReceive: 打印机已连接2" );
	 		 				 if(mUsbDriver.openUsbDevice(device)) {
	 		 					if(device.getProductId()==8211)
	 		 						mUsbDev1 = device;
	 		 					else
	 		 						mUsbDev2 = device;
	 		 				 }
 						}
 	                 }
 	                 else {
 						Toast.makeText(MainActivity.this,"permission denied for device",
								Toast.LENGTH_SHORT).show();
 	                     //Log.d(TAG, "permission denied for device " + device);
 	                 }
 	             }
 	         } 
 		}
 	};
 	class BmpBrowerClickListener implements OnClickListener{

		@Override
		public void onClick(View view) {
			try {
				switch (view.getId()) {
				case R.id.SelectBmpFile:
					clickFlag = 1;
					showFileChooser();
					HuaruiPrintUtils.putValue(MainActivity.this, "path", mBmpPath_et
							.getText().toString().trim());
					break;
				case R.id.SelectImgFile:
					clickFlag = 0;
					showFileChooser();
					break;
				case R.id.Clear_Path_Btn:
					mBmpPath_et.setText("");
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
 	}
 	
	class PrintClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			QrSize = Integer.valueOf(mTestTimes.getText().toString().trim());
//			System.out.println("二维码QrSize:" + QrSize);
			if(!PrintConnStatus()){
				return;
			}
			try {
				switch (view.getId()) { 
				case R.id.PrintSelfPage_btn:
					PrintSelfPageTest();
					break;
				case R.id.PrintTest_btn:
					getPrintTestData(mUsbDev1);
					break;
				case R.id.PrintTicket_btn:
					getPrintTicketData(mUsbDev1);
					break;
				case R.id.Clear_btn:        // 清除状态按钮
					editRecDisp.setText("");
					break;
				case R.id.Load_nvbmp_btn:   // 下载指定路径的位图保存到打印机
					DownloadNvBmp();
					break;
				case R.id.Print_nvbmp_btn:    // 打印位图
					setPrintNvBmp();	
					break;
				case R.id.Print_Imgfile_Btn:  // 打印其他格式图片（jpg / png / bmp等）
					PrintDiskImgFile();
					break;
				case R.id.Print_Seat_Btn:    // 打印水平制表
					PrintSeat(mUsbDev1);
					break;
				case R.id.D347QrBtn:       // D347QrCode
					D347QrCode();	
					break;
				case R.id.D245QrBtn:       // D245QrCode
					D245QrCode();
					break;
				case R.id.T500IIQrBtn:     // T500IIQrCode
					T500IIQrCode();	
					break;
				case R.id.Print_1DBar_Btn:  // Print1DBar
					Print1DBarByType(QrSize);
					break;
				default:
					break;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// 打印水平制表
	private void PrintSeat(UsbDevice usbDev) {
		// 获取输入数据
		System.out.println("-----PrintSeat-----");
		String ColumnStr1 = HTColumn1.getText().toString();
		String ColumnStr2 = HTColumn2.getText().toString();
		String HTSeatStr1 = HTStr1.getText().toString();
		String HTSeatStr2 = HTStr2.getText().toString();
		String HTSeatStr3 = HTStr3.getText().toString();
		String Colstr1 = HuaruiPrintUtils.intToHexString(Integer.valueOf(ColumnStr1), 1)+ " ";// 转换
		String Colstr2 = HuaruiPrintUtils.intToHexString(Integer.valueOf(ColumnStr2), 1)+ " ";
		String htstr1 = HuaruiPrintUtils.stringTo16Hex(HTSeatStr1);
		String htstr2 = HuaruiPrintUtils.stringTo16Hex(HTSeatStr2);
		String htstr3 = HuaruiPrintUtils.stringTo16Hex(HTSeatStr3);
		// 1.根据客户输入需求【可用，只支持英文与数字，中文乱码】
		mHTSeatStr.setText(Colstr1 + Colstr2 + "00 " + htstr1 + "09 " + htstr2
				+ "09 " + htstr3 + "0A 0A");
		String etstring = mHTSeatStr.getText().toString();
		byte[] seat = HuaruiPrintUtils.hexStr2Bytesnoenter(etstring);
		if (etstring != null && !"".equals(etstring)) {
			mUsbDriver.write(PrintCmd.SetClean(),usbDev);
			mUsbDriver.write(PrintCmd.SetHTseat(seat, seat.length),
					seat.length,usbDev);
			SetFeedCutClean(cutter,usbDev);
		}
	}
	
	// 打印图片文件（png/jpg/bmp）
	private void PrintDiskImgFile(){
		String imgPath = mImgPath_et.getText().toString().trim();
		int[] data1 = getBitmapParamsData(imgPath);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data1,width,heigh));
		SetFeedCutClean(cutter);
	}
	
	int width, heigh;
	private int[] getBitmapParamsData(String imgPath) {
		Bitmap bm = HuaruiPrintUtils.getBitmapData(imgPath);      // 1.彩色图片转换成Bitmap
//		Bitmap bm = HuruiPrintUtils.convertToBlackWhite(bitmap); // 2.彩色转黑白图 ----20170615
		width = bm.getWidth();
		heigh = bm.getHeight();
		int iDataLen = width * heigh;
		int[] pixels = new int[iDataLen];
		bm.getPixels(pixels, 0, width, 0, 0, width, heigh);
		return pixels;
	}
	
	// 通过类型打印一维码
	private void Print1DBarByType(int iBarType) {
		mUsbDriver.write(PrintCmd.SetAlignment(align));
		// CODE39:14809966841053 测试数据  k >= 1 
		// 类型 0 （11 =< k <= 12） 、 1(11 =< k <= 12) 、2(12 =< k <= 13) 、7(12 =< k <= 13) 字符个数 k = 12均适用 
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, iBarType, "012345678900"));
		System.out.println("一维码类型：" + iBarType);
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		SetFeedCutClean(cutter);
	}

	// 【1】MS-D347,13 52指令二维码接口，环绕模式1
	private void D347QrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 25, 6, 1));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));// 走纸换行
		SetFeedCutClean(cutter);
	}

	// 【2】MS-D245|MS-N58V|MSP-100 二维码，左边距、size、环绕模式0
	private void D245QrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 12, 2, 0));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		SetFeedCutClean(cutter);
	}

	// 【3】MS-532II+T500II二维码接口
	private void T500IIQrCode() {
		getStrDataByLanguage();	
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrCodeT500II(QrSize,Constant.WebAddress_zh));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		SetFeedCutClean(cutter);
	}
	
	// 打印位图【指定下载位图的索引】
	private void setPrintNvBmp() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] { getString(R.string.bmp_1).toString(),
				getString(R.string.bmp_2).toString(),
				getString(R.string.bmp_3).toString(),
				getString(R.string.bmp_4).toString(),
				getString(R.string.bmp_5).toString(),
				getString(R.string.bmp_6).toString() }; // 对齐方式数组
		Builder b = new Builder(this);
		b.setTitle(getString(R.string.Print_Bmp_btn));
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							getPrintNvBmp(1);
							break;
						case 1:
							getPrintNvBmp(2);
							break;
						case 2:
							getPrintNvBmp(3);
							break;
						case 3:
							getPrintNvBmp(4);
							break;
						case 4:
							getPrintNvBmp(5);
							break;
						case 5:
							getPrintNvBmp(6);
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}
	
	// 打印下载位图
	private void getPrintNvBmp(int iNums) {
		byte[] etBytes = PrintCmd.PrintNvbmp(iNums, 48);
		mUsbDriver.write(etBytes);
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		SetFeedCutClean(cutter);
	}
	// 设置下载位图
	private void DownloadNvBmp() {
		String loadPath = mBmpPath_et.getText().toString().trim();
		if(!"".equalsIgnoreCase(loadPath)){
			int inums = HuaruiPrintUtils.count(loadPath, ";");
			byte[] bValue = PrintCmd.SetNvbmp(inums,loadPath);
			if(bValue != null){
				mUsbDriver.write(bValue, bValue.length);
			}
		}
	}
	
	// 显示文件选择路径
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*.bin");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "Select a BIN file"),FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				String path = HuaruiPrintUtils.getPath(MainActivity.this, uri);
				if(clickFlag==1){
					String sharePath = HuaruiPrintUtils.getValue(MainActivity.this, "path", "").toString().trim();
					if(!"".equalsIgnoreCase(sharePath)){
						mBmpPath_et.setText(sharePath + path + ";");
					}else{
						mBmpPath_et.setText(path + ";");
					}
				} else {
					mImgPath_et.setText(path);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 1.测试文本打印
	 * @throws UnsupportedEncodingException 
	 */
	private void getPrintTestData(UsbDevice usbDev) throws UnsupportedEncodingException{
		String etstring = "";
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return; 
		if(HuaruiPrintUtils.isZh(this))
			etstring = Constant.TESTDATA_CN;
		else
			etstring = Constant.TESTDATA_US;
		if (etstring != null && !"".equals(etstring)) {
			getCommonSettings(usbDev); // 常规设置
			byte[] etBytes = PrintCmd.PrintString(etstring, 0);
			mUsbDriver.write(etBytes, etBytes.length,usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);
			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "AB1-CD2-EF3"),usbDev);// * 10 CODE128 / *4 CODE39
			SetFeedCutClean(cutter,usbDev);
		}
	}
	
	/**
	 * 2.小票打印
	 */
	private void getPrintTicketData(UsbDevice usbDev) {
		getStrDataByLanguage();		
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return; 
		try{
			mUsbDriver.write(PrintCmd.SetClean(),usbDev);  // 初始化，清理缓存
			// 小票标题
			mUsbDriver.write(PrintCmd.SetBold(0),usbDev);  // 0 不加粗， 1加粗
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev); // 0 左、1 居中、2 右
		/*	int iHeight：放大高度，取值(1-8)
			int iWidth：放大宽度，取值(1-8)*/
			mUsbDriver.write(PrintCmd.SetSizetext(1, 1),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(title, 0),usbDev); // 是否加换行指令 0x0a： 0 加换行指令 1 不加换行指令(等到下一个换行指令才打
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(0, 0),usbDev);
			// 小票号码
			mUsbDriver.write(PrintCmd.SetBold(1),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(1, 1),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(num, 0),usbDev);
			mUsbDriver.write(PrintCmd.SetBold(0),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			mUsbDriver.write(PrintCmd.SetSizetext(0, 0),usbDev);
			// 小票主要内容
			mUsbDriver.write(PrintCmd.PrintString(strData, 0),usbDev);
			mUsbDriver.write(PrintCmd.PrintFeedline(2),usbDev); // 打印走纸2行
			// 二维码
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);   //test 二维码靠左？？？
			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr+"1234567890232543654756474868", 15, 8, 1),usbDev);           // 【1】MS-D347,13 52指令二维码接口，环绕模式1
//			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 12, 2, 0),usbDev);           // 【2】MS-D245,MSP-100二维码，左边距、size、环绕模式0
//			mUsbDriver.write(PrintCmd.PrintQrCodeT500II(QrSize,Constant.WebAddress_zh),usbDev);// 【3】MS-532II+T500II二维码接口

			mUsbDriver.write(PrintCmd.PrintFeedline(2),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			// 日期时间
			mUsbDriver.write(PrintCmd.SetAlignment(2),usbDev);
			mUsbDriver.write(PrintCmd.PrintString(sdf.format(new Date()).toString()
					+ "\n\n", 1),usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
			// 一维条码
			mUsbDriver.write(PrintCmd.SetAlignment(1),usbDev);
			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "A12345678Z"),usbDev);// 一维条码打印
			mUsbDriver.write(PrintCmd.SetAlignment(0),usbDev);
//			 20170804 特殊测试
//			mUsbDriver.write(PrintCmd.SetWhitemodel(1), usbDev);
//			byte[] etBytes = PrintCmd.PrintString(Constant.m_PrintDataCN, 0);
//			mUsbDriver.write(etBytes, usbDev);
			// 走纸换行、切纸、清理缓存
			SetFeedCutClean(cutter,usbDev);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	
	// 根据系统语言获取测试文本
	private void getStrDataByLanguage(){
		codeStr = etWrite.getText().toString().trim();
		if("".equalsIgnoreCase(codeStr))
			codeStr = Constant.WebAddress;
		if(HuaruiPrintUtils.isZh(this)){
			title = Constant.TITLE_CN;
			strData = Constant.STRDATA_CN;
		}else {
			title = Constant.TITLE_US;
			strData = Constant.STRDATA_US;
		}
		num = String.valueOf(Number) + "\n\n";
		Number++;
	}
	// 走纸换行、切纸、清理缓存
	private void SetFeedCutClean(int iMode,UsbDevice usbDev) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5),usbDev);      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode),usbDev);  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean(),usbDev);            // 清理缓存
	}
	// 走纸换行、切纸、清理缓存
	private void SetFeedCutClean(int iMode) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5));      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode));  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());            // 清理缓存
	}
	// 常规设置
	private void getCommonSettings(UsbDevice usbDev){
		mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);    // 对齐方式
		mUsbDriver.write(PrintCmd.SetRotate(rotate),usbDev);      // 字体旋转
		mUsbDriver.write(PrintCmd.SetUnderline(underLine),usbDev);// 下划线
		mUsbDriver.write(PrintCmd.SetLinespace(linespace),usbDev);// 行间距

	}
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.font_0:// 文字方向
			rotate = 0;
			break;
		case R.id.font_1:
			rotate = 1;
			break;
		case R.id.align_0:// 对齐方式
			align = 0;
			break;
		case R.id.align_1:
			align = 1;
			break;
		case R.id.align_2:
			align = 2;
			break;
		case R.id.under_0:// 下划线
			underLine = 0;
			break;
		case R.id.under_1:
			underLine = 1;
			break;
		case R.id.under_2:
			underLine = 2;
			break;
		case R.id.linespace_30:// 下划线
			linespace = 30;
			break;
		case R.id.linespace_40:
			linespace = 40;
			break;
		case R.id.linespace_50:
			linespace = 50;
			break;
		case R.id.linespace_60:
			linespace = 60;
			break;
		case R.id.linespace_70:
			linespace = 70;
			break;
		case R.id.cutter_0:// 切刀
			cutter = 0;
			break;
		case R.id.cutter_1:
			cutter = 1;
			break;
		case R.id.system_0:// 退出系统
			System.exit(0);
			break;
		case R.id.system_1:// 系统版本
			Toast.makeText(MainActivity.this, "当前版本USB2.2", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// -------------------显示消息-----------------------
	private void ShowMessage(String sMsg) {
		StringBuilder sbMsg = new StringBuilder();
		sbMsg.append(editRecDisp.getText());
		sbMsg.append(m_sdfDate.format(new Date()));
		sbMsg.append(sMsg);
		sbMsg.append("\r\n");
		editRecDisp.setText(sbMsg);
		editRecDisp.setSelection(sbMsg.length(), sbMsg.length());
	}
	
	// 检测打印机状态  触控
	private int getPrinterStatus(UsbDevice usbDev) {
		int iRet = -1;

		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = PrintCmd.GetStatus1();
		if(mUsbDriver.read(bRead1,bWrite1,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus1(bRead1[0]);
			Log.d("2222", "getPrinterStatus1: "+ new String(bRead1) +"  "+ iRet);
		}
		
		if(iRet!=0)
			return iRet;
		
		byte[] bRead2 = new byte[1];
		byte[] bWrite2 = PrintCmd.GetStatus2();
		if(mUsbDriver.read(bRead2,bWrite2,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus2(bRead2[0]);
			Log.d("2222", "getPrinterStatus2: "+ new String(bRead2) +"  "+ iRet);
		}

		if(iRet!=0)
			return iRet;
		
		byte[] bRead3 = new byte[1];
		byte[] bWrite3 = PrintCmd.GetStatus3();
		if(mUsbDriver.read(bRead3,bWrite3,usbDev)>0)
		{

			iRet = PrintCmd.CheckStatus3(bRead3[0]);
			Log.d("2222", "getPrinterStatus3: "+ new String(bRead3) +"  "+ iRet);
		}

		if(iRet!=0)
			return iRet;

		byte[] bRead4 = new byte[1];
		byte[] bWrite4 = PrintCmd.GetStatus4();
		if(mUsbDriver.read(bRead4,bWrite4,usbDev)>0)
		{

//			iRet = PrintCmd.CheckStatus4(bRead4[0]);
			int x = bRead4[0];
			Log.d("2222", "getPrinterStatus4: "+ Arrays.toString(bRead4) +"  -  "+ x);
		}

		return iRet;
	}
	

	private int checkStatus(int iStatus)
	{ 
		int iRet = -1;

		StringBuilder sMsg = new StringBuilder();
  
		//0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配 
		//3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
		switch (iStatus) {
			case 0: 
				sMsg.append(normal);       // 正常
				iRet = 0;
				Log.d("1111111111", "打印机正常: ");
				break;
			case 8:
				sMsg.append(paperWillExh); // 纸将尽
				Log.d("1111111111", "纸将尽: ");
				iRet = 0;
				break;			
			case 3:
				sMsg.append(printerHeadOpen); //打印头打开
				Log.d("1111111111", "打印头打开: ");
				break;   
			case 4:
				sMsg.append(cutterNotReset);
				Log.d("1111111111", "切刀未复位: ");
				break;
			case 5:
				sMsg.append(printHeadOverheated);
				Log.d("1111111111", "打印头过热: ");
				break;
			case 6:
				sMsg.append(blackMarkError);
				Log.d("1111111111", "黑标错误: ");
				break;			
			case 7:
				sMsg.append(paperExh);     // 纸尽==缺纸
				Log.d("1111111111", "纸尽: ");
				break;
			case 1:
				sMsg.append(notConnectedOrNotPopwer);
				Log.d("1111111111", "打印机未连接或未上电: ");
				break;
			default:
				sMsg.append(abnormal);     // 异常
				Log.d("1111111111", "异常: ");
				break;
		} 
				
		ShowMessage(sMsg.toString());
		return iRet;
		
	}
 
	public synchronized void sleep(long msec) {
		try {
			wait(msec);
		} catch (InterruptedException e) {
		}
	}
	// 通过系统语言判断Message显示
	String receive = "", state = ""; // 接收提示、状态类型
	String normal = "",notConnectedOrNotPopwer = "",notMatch = "",
			printerHeadOpen = "", cutterNotReset = "", printHeadOverheated = "", 
			blackMarkError = "",paperExh = "",paperWillExh = "",abnormal = "";
	private void getMsgByLanguage() {
		if (HuaruiPrintUtils.isZh(this)) {
			receive = Constant.Receive_CN;
			state = Constant.State_CN;
			normal = Constant.Normal_CN;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_CN;
			notMatch = Constant.PrinterAndLibraryNotMatch_CN;
			printerHeadOpen = Constant.PrintHeadOpen_CN;
			cutterNotReset = Constant.CutterNotReset_CN;
			printHeadOverheated = Constant.PrintHeadOverheated_CN;
			blackMarkError = Constant.BlackMarkError_CN;
			paperExh = Constant.PaperExhausted_CN;
			paperWillExh = Constant.PaperWillExhausted_CN;
			abnormal = Constant.Abnormal_CN;
		} else {
			receive = Constant.Receive_US;
			state = Constant.State_US;
			normal = Constant.Normal_US;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_US;
			notMatch = Constant.PrinterAndLibraryNotMatch_US;
			printerHeadOpen = Constant.PrintHeadOpen_US;
			cutterNotReset = Constant.CutterNotReset_US;
			printHeadOverheated = Constant.PrintHeadOverheated_US;
			blackMarkError = Constant.BlackMarkError_US;
			paperExh = Constant.PaperExhausted_US;
			paperWillExh = Constant.PaperWillExhausted_US;
			abnormal = Constant.Abnormal_US;
		}
	}
	/**
	 * 自检页打印
	 */
	private void PrintSelfPageTest() {
		try {
			byte[] bSend = PrintCmd.PrintSelfcheck();
			if (bSend != null) {
				mUsbDriver.write(bSend);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 自检页指令1D 28 41 02 00 00 02
	public static byte[] getSelfPageCmd() {
		byte[] bCmd = new byte[7];
		int iIndex = 0;
		bCmd[iIndex++] = 0x1D;
		bCmd[iIndex++] = 0x28;
		bCmd[iIndex++] = 0x41;
		bCmd[iIndex++] = 0x02;
		bCmd[iIndex++] = 0x00;
		bCmd[iIndex++] = 0x00;
		bCmd[iIndex++] = 0x02;
		return bCmd;
	}
	// CodePage 发送指令1B 74 3F
//	public static byte[] CodePage() {
//		byte[] bCmd = new byte[3];
//		int iIndex = 0;
//		bCmd[iIndex++] = 0x1B;
//		bCmd[iIndex++] = 0x74;
//		bCmd[iIndex++] = 0x3F;
//		return bCmd;
//	}
	
	/** -------------------------------1D 72 01 指令-----------------------------------*/
    // 获取打印完成状态 1D 72 01发送指令
	public static byte[] getPrintStatus() {
		byte[] bCmd = new byte[3];
		int iIndex = 0;
		bCmd[iIndex++] = 0x1D;
		bCmd[iIndex++] = 0x72;
		bCmd[iIndex++] = 0x01;
		return bCmd;
	}
	
	// 打印状态
	private int printStatus(UsbDevice usbDev) {
		int iRet = -1;
		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = getPrintStatus();
		if (mUsbDriver.read(bRead1, bWrite1) > 0) {
			iRet = checkPrintStatu(bRead1[0]);
		}
		if (iRet != 0)
			return 1;
		return 0;
	}
	// 解析
	private int checkPrintStatu(byte bStatu){
		if ((bStatu & 0x00) == 0x00) 
			return 0;
		return 1;
	}
	/** ------------------------------- END -------------------------------------------*/
	
}
