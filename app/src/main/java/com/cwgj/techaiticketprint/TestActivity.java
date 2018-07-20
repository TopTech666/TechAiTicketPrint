package com.cwgj.techaiticketprint;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.cwgj.gpio_lib.RkGpioManager;
import com.cwgj.techticketprint.TicketPrintManager;

import rxbus.ecaray.com.rxbuslib.rxbus.RxBus;
import rxbus.ecaray.com.rxbuslib.rxbus.RxBusReact;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：
 * +----------------------------------------------------------------------
 * | 创建者   :  ldw
 * +----------------------------------------------------------------------
 * | 时　　间 ：2018/7/18 09:49
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/
public class TestActivity extends Activity {

    TextView tv_print_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tv_print_state = findViewById(R.id.tv_print_state);
        RxBus.getDefault().register(this);
        TicketPrintManager.getInstance().init(getApplication());

        try {
            RkGpioManager.getInstance().initData(30, 5000, new RkGpioManager.onGpioReceiver() {
                @Override
                public void onGpioReceiver(int i) {
                    try {
                        TicketPrintManager.getInstance().printInParkTicket("粤美特停车场", "2018-01-01 12:00:00", "xxxxxxxxxxxxxxxxxxxxx");
                        TicketPrintManager.getInstance().printOutParkTicket("深圳南山科技园粤美特停车场", "2018-01-01 12:00:00", "2018-01-01 14:00:00",
                                "2小时", "20.00元", "微信支付", "aaaaaaaaaaaaaaaaa");
                    } catch (Exception e) {
                        Toast.makeText(TestActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }, 4, 5);
            RkGpioManager.getInstance().startGPIOScan();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @RxBusReact(tag = TicketPrintManager.TAG_TEAR_UP_PAPER)
    public void tearUpPaper() {
        Toast.makeText(TestActivity.this, "纸票被撕下了，啦啦啦啦啦啦啦", Toast.LENGTH_LONG).show();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getDefault().unregister(this);
    }
}
