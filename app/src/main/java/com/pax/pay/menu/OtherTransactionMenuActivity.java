package com.pax.pay.menu;

import com.pax.edc.R;
import com.pax.pay.trans.BalanceTrans;
import com.pax.pay.trans.Instalment;
import com.pax.pay.trans.QuasiCashTrans;
import com.pax.pay.trans.VoidRefundTrans;
import com.pax.view.MenuPage;
/**
 * Created by chenzaoyang on 2017/4/14.
 */

public class OtherTransactionMenuActivity extends BaseMenuActivity {

    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(OtherTransactionMenuActivity.this, 9, 3)

                .addTransItem(R.string.trans_instalment, R.drawable.app_instalment_sale,new Instalment())
                //Inquiry Balance
                .addTransItem(R.string.trans_balance, R.drawable.app_balance,new BalanceTrans())
                //void refund
                .addTransItem(R.string.trans_void_refund, R.drawable.app_void_refund,
                new VoidRefundTrans())
                //Quasi Cash
                .addTransItem(R.string.trans_quasi_cash, R.drawable.app_sale,
                new QuasiCashTrans())
                //Cash Advance
                .addTransItem(R.string.trans_cash_advance, R.drawable.app_sale,
                new QuasiCashTrans());
        return builder.create();
    }
}
