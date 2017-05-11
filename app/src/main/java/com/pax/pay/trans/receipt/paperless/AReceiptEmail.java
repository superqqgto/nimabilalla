/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-14
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.receipt.paperless;

import android.graphics.Bitmap;

import com.pax.pay.trans.receipt.PrintListener;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.EmailInfo;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

abstract class AReceiptEmail {

    protected PrintListener listener;

    //send email
    public int sendTextEmail(EmailInfo emailInfo, String emailAddress, String subject, String content) {
        try {
            Email email = new SimpleEmail();
            setBaseInfo(emailInfo, email);
            email.setSubject(subject);
            email.setMsg(content);
            email.addTo(emailAddress);
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    public int sendHtmlEmail(EmailInfo emailInfo, String emailAddress, String subject, String content, Bitmap pic) {
        String placeHolder = "<img/>";
        String htmlMsg = "<html><body>" +
                placeHolder + "</body></html>";
        try {
            ImageHtmlEmail email = new ImageHtmlEmail();

            setBaseInfo(emailInfo, email);
            File file = Convert(pic, "receipt_tmp.jpg");
            email.setDataSourceResolver(new DataSourceFileResolver(file));
            String cid = email.embed(file); // 将图片嵌入邮件中，返回cid
            String img = "<img src='cid:" + cid + "'/>"; // 构造img标签，图片源为cid
            htmlMsg = htmlMsg.replace(placeHolder, img); // 替换html邮件正文中的占位符

            email.setSubject(subject);
            email.setTextMsg(content);
            email.setHtmlMsg(htmlMsg);
            email.addTo(emailAddress);
            email.send();
        } catch (EmailException | IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private int setBaseInfo(EmailInfo emailInfo, Email email) {
        try {
            //email.setDebug(true);
            email.setHostName(emailInfo.getHostName());
            email.setSmtpPort(emailInfo.getPort());
            email.setAuthentication(emailInfo.getUserName(), emailInfo.getPassword());
            email.setCharset("UTF-8");
            email.setSSLOnConnect(emailInfo.isSsl());
            if (emailInfo.isSsl())
                email.setSslSmtpPort(String.valueOf(emailInfo.getSslPort()));
            email.setFrom(emailInfo.getFrom());
        } catch (EmailException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private File Convert(Bitmap bm, String fileName) throws IOException {
        String path = ContextUtils.getFilesDir() + "/temp/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
    }
}
