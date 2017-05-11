package com.pax.pay.trans.pack.PackReconcile;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.pack.PackIso8583;

/**
 * Created by huangmuhua on 2017/4/7.
 * 参考文档Terminal Specification for VMJC V1 6.pdf
 * 第5.4节 Reconcilation Messages
 * 包含 Settlement Message和Batch Upload
 */

public abstract class BasePackReconcile extends PackIso8583 {
    public BasePackReconcile(PackListener listener) {
        super(listener);
    }
}
