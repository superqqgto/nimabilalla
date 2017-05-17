package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.trans.authtrans.MotoPreAuthCancelTrans;
import com.pax.pay.trans.authtrans.MotoPreAuthCompCancelTrans;
import com.pax.pay.trans.mototrans.MotoRefundTrans;
import com.pax.pay.trans.mototrans.MotoSaleTrans;
import com.pax.pay.trans.mototrans.MotoSaleVoidTrans;
import com.pax.view.MenuPage;
import com.pax.pay.trans.authtrans.MotoPreAuthTrans;
import com.pax.pay.trans.authtrans.MotoPreAuthCompTrans;

/**
 * Created by zhouhong on 2017/5/5.
 */

public class MotoMenuActivity extends BaseMenuActivity {

    /**
     * Moto，
     */
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(MotoMenuActivity.this, 9, 3)
                .addTransItem(R.string.trans_moto_preAuth, R.drawable.app_auth,
                        new MotoPreAuthTrans(true))
                .addTransItem(R.string.trans_moto_preauth_comp, R.drawable.app_auth,
                        new MotoPreAuthCompTrans(true))
                .addTransItem(R.string.trans_moto_preauth_cancel, R.drawable.app_void,
                        new MotoPreAuthCancelTrans(true))
                .addTransItem(R.string.trans_moto_preauth_comp_cancel, R.drawable.app_refund,
                        new MotoPreAuthCompCancelTrans(true))
                // Moto Sale
                .addTransItem(R.string.trans_moto_sale, R.drawable.app_sale,new MotoSaleTrans())
                //Moto Refund
                .addTransItem(R.string.trans_moto_refund, R.drawable.app_refund,new MotoRefundTrans())
                //Moto Void
                .addTransItem(R.string.trans_moto_void, R.drawable.app_void,new MotoSaleVoidTrans());

        return builder.create();
    }

}
