/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-17
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.model;

import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.AcqIssuerRelation;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.CardRange;
import com.pax.pay.base.DocumentBase;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlDomManager {

    private static XmlDomManager xmlDocManager;
    private DocumentBase file;
    private HashMap<String, Acquirer> acqMap;
    private HashMap<String, Issuer> issuerMap;
    private List<CardRange> cardRangeList;
    private List<AcqIssuerRelation> relationList;

    public static synchronized XmlDomManager getInstance() {
        if (xmlDocManager == null) {
            xmlDocManager = new XmlDomManager();
        }
        //xmlDocManager.initfile();
        return xmlDocManager;
    }

    public XmlDomManager() {
    }

    public void ParseFile() {
        ParseAcquire();
        ParseIssuer();
        ParseCardRange();
        ParseRelation();
    }

    public void saveToDb() {
        AcqManager acqManager = AcqManager.getInstance();

        for (Map.Entry<String, Acquirer> entry : acqMap.entrySet()) {
            DbManager.getAcqDao().insertAcquirer(entry.getValue());
//            if (tmp == true){
//                Log.d("log", "insert success");
//            }else{
//                Log.d("log", "insert fail");
//                acqManager.updateAcquirer(entry.getValue());
//            }
        }

        for (Map.Entry<String, Issuer> entry : issuerMap.entrySet()) {
            DbManager.getAcqDao().insertIssuer(entry.getValue());
        }

        for (CardRange cardRange : cardRangeList) {
            DbManager.getAcqDao().insertCardRange(cardRange);
        }

        for (AcqIssuerRelation relation : relationList) {
            DbManager.getAcqDao().bind(relation.getAcquirer(), relation.getIssuer());
        }
    }

    private void ParseAcquire() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(this.file.getAcqFilePath()));
            NodeList root = document.getChildNodes();
            Node param = root.item(0);
            NodeList node = param.getChildNodes();
            acqMap = new HashMap<>();
            for (int i = 1; i < node.getLength(); ) {
                //set name
                String text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                Acquirer acq = new Acquirer(text);

                //set nii
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setNii(text);

                //set terminal id
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setTerminalId(text);

                //set merchant id
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setMerchantId(text);

                //set batch no
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setCurrBatchNo(Integer.parseInt(text));

                //set ip
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setIp(text);

                //set port
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setPort(Integer.parseInt(text));

                //not necessary
                while (true) {
                    //set ip bak1
                    String name = node.item(i).getNodeName();
                    if (name.equals("acq1.TCP.ipbak1") || name.equals("acq2.TCP.ipbak1")) {
                        text = node.item(i).getTextContent();
                        if (text == null) {
                            return;
                        }
                        acq.setIpBak1(text);
                        i += 2;
                    } else if (name.equals("acq1.TCP.portbak1") || name.equals("acq2.TCP.portbak1")) {
                        text = node.item(i).getTextContent();
                        if (text == null) {
                            return;
                        }
                        acq.setPortBak1(Short.parseShort(text));
                        i += 2;
                    } else if (name.equals("acq1.TCP.ipbak2") || name.equals("acq2.TCP.ipbak2")) {
                        text = node.item(i).getTextContent();
                        if (text == null) {
                            return;
                        }
                        acq.setIpBak2(text);
                        i += 2;
                    } else if (name.equals("acq1.TCP.portbak1") || name.equals("acq2.TCP.portbak1")) {
                        text = node.item(i).getTextContent();
                        if (text == null) {
                            return;
                        }
                        acq.setPortBak2(Short.parseShort(text));
                        i += 2;
                    } else {
                        break;
                    }
                }

                //set tcp timeout
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setTcpTimeOut(Integer.parseInt(text));

                //set wireless timeout
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                acq.setWirelessTimeOut(Integer.parseInt(text));

                //set isDisableTrickFeed
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    acq.setDisableTrickFeed(true);
                } else {
                    acq.setDisableTrickFeed(false);
                }

                acqMap.put(acq.getName(), acq);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private void ParseIssuer() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(this.file.getIssuerFilePath()));
            NodeList root = document.getChildNodes();
            Node param = root.item(0);
            NodeList node = param.getChildNodes();
            issuerMap = new HashMap<>();
            for (int i = 1; i < node.getLength(); ) {
                //set name
                String text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                Issuer issuer = new Issuer(text);

                //set FloorLimit
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                issuer.setFloorLimit(Long.parseLong(text));

                //set AdjustPercent
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                issuer.setAdjustPercent(Float.parseFloat(text));

                //set panMaskPattern
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                //FIXME LAIYI
                if (text.equals("6-4")) {
                    issuer.setPanMaskPattern(Constants.DEF_PAN_MASK_PATTERN);
                }

                //set isEnableAdjust
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setEnableAdjust(true);
                } else {
                    issuer.setEnableAdjust(false);
                }

                //set isEnableOffline
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setEnableOffline(true);
                } else {
                    issuer.setEnableOffline(false);
                }

                //set isAllowExpiry
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setAllowExpiry(true);
                } else {
                    issuer.setAllowExpiry(false);
                }

                //set isAllowManualPan
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setAllowManualPan(true);
                } else {
                    issuer.setAllowManualPan(false);
                }

                //set isAllowCheckExpiry
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setAllowCheckExpiry(true);
                } else {
                    issuer.setAllowCheckExpiry(false);
                }

                //set isAllowPrint
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setAllowPrint(true);
                } else {
                    issuer.setAllowPrint(false);
                }

                //set isAllowCheckPanMod10
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setAllowCheckPanMod10(true);
                } else {
                    issuer.setAllowCheckPanMod10(false);
                }

                //set isRequirePIN
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setRequirePIN(true);
                } else {
                    issuer.setRequirePIN(false);
                }

                //set isRequireMaskExpiry
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                if (text.equals("Y")) {
                    issuer.setRequireMaskExpiry(true);
                } else {
                    issuer.setRequireMaskExpiry(false);
                }

                issuerMap.put(issuer.getName(), issuer);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ParseCardRange() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(this.file.getCardRangeFilePath()));
            NodeList root = document.getChildNodes();
            Node param = root.item(0);
            NodeList node = param.getChildNodes();
            cardRangeList = new ArrayList<>();
            for (int i = 1; i < node.getLength(); ) {
                CardRange cardRange = new CardRange();
                //set name
                String text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                cardRange.setName(text);

                //set PanLength
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                cardRange.setPanLength(Integer.parseInt(text));

                //set panRangeHigh
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                cardRange.setPanRangeHigh(text);

                //set panRangeLow
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                cardRange.setPanRangeLow(text);

                //set issuer
                cardRange.setIssuer(issuerMap.get(cardRange.getName()));

                cardRangeList.add(cardRange);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void ParseRelation() {

        File file = new File(this.file.getRelationFilePath());
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(this.file.getRelationFilePath()));
            NodeList root = document.getChildNodes();
            Node param = root.item(0);
            NodeList node = param.getChildNodes();
            relationList = new ArrayList<>();
            for (int i = 1; i < node.getLength(); ) {
                AcqIssuerRelation relation = new AcqIssuerRelation();
                //set acq
                String text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                relation.setAcquirer(acqMap.get(text));

                //set issuer
                text = node.item(i).getTextContent();
                if (text == null) {
                    return;
                }
                i += 2;
                relation.setIssuer(issuerMap.get(text));

                relationList.add(relation);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFile(DocumentBase file) {
        this.file = file;
    }

}
