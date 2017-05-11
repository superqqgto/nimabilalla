package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.trans.DccTrans;
import com.pax.pay.trans.Instalment;
import com.pax.pay.trans.VoidRefundTrans;
import com.pax.pay.trans.mototrans.MotoRefundTrans;
import com.pax.pay.trans.mototrans.MotoSaleTrans;
import com.pax.pay.trans.mototrans.MotoSaleVoidTrans;
import com.pax.view.MenuPage;
/**
 * Created by chenzaoyang on 2017/4/14.
 */

public class OtherFunctionMenuActivity extends BaseMenuActivity {

    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(OtherFunctionMenuActivity.this, 6, 3)
                // DCC
                .addTransItem(R.string.trans_dcc, R.drawable.app_dcc,new DccTrans())
                //instalment sale
                .addTransItem(R.string.trans_instalment, R.drawable.app_instalment_sale,new Instalment())
                // Moto Sale
                .addTransItem(R.string.trans_moto_sale, R.drawable.app_sale,new MotoSaleTrans())
                //Moto Refund
                .addTransItem(R.string.trans_moto_refund, R.drawable.app_refund,new MotoRefundTrans())
                //Moto Void
                .addTransItem(R.string.trans_moto_void, R.drawable.app_void,new MotoSaleVoidTrans())
                //void refund
                .addTransItem(R.string.trans_void_refund, R.drawable.app_void_refund,
                new VoidRefundTrans());
        return builder.create();
    }
}
