/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Mode Numbers:
PDM_EMAIL=1;
PDM_PRINT=2;
AUTONUM_JOURNALENTRY=0;
AUTONUM_SALESORDER=1;
AUTONUM_INVOICE=2;
AUTONUM_CREDITNOTE=3;
AUTONUM_RECEIPT=4;
AUTONUM_PURCHASEORDER=5;
AUTONUM_GOODSRECEIPT=6;
AUTONUM_DEBITNOTE=7;
AUTONUM_PAYMENT=8;
AUTONUM_CASHSALE=9;
AUTONUM_CASHPURCHASE=10;
AUTONUM_BILLINGINVOICE=11;
AUTONUM_BILLINGRECEIPT=12;
AUTONUM_BILLINGCASHSALE=13;
AUTONUM_BILLINGCASHPURCHASE=14;
AUTONUM_BILLINGGOODSRECEIPT=15;
AUTONUM_BILLINGPAYMENT=16;
AUTONUM_BILLINGSALESORDER=17;
AUTONUM_BILLINGPURCHASEORDER=18;
AUTONUM_BILLINGCREDITNOTE=19;
AUTONUM_BILLINGDEBITNOTE=20;
AUTONUM_BALANCESHEET=21;*/

    
Wtf.account.MailWindow = function(config){
     Wtf.apply(this, config);
     this.rec=(config.rec==undefined?"":config.rec);
     this.userdata=null;
     this.tax1099=(config.tax1099==undefined?false:config.tax1099);
     this.data=this.rec.data;
     this.isinvoice=null;
     this.userrec=null;
     this.mode=null;
     this.label=(config.label==undefined?"":config.label);
     this.otherVendorMails = (this.data.othervendoremails == undefined?"":(this.data.othervendoremails+";"));
     this.isQuotation=config.isQuotation;
     this.isCash=config.isCash;
     this.isOrder=config.isOrder;
     this.isDoOrGr=config.isDoOrGr;
     this.isSecurityGate=config.isSecurityGate;
     this.isCustomer=config.isCustomer;
     this.isConsignment=config.isConsignment;
     this.emailId="";
     this.bccEmailIds="";
     this.sendBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.MailWin.send")  //'Send'
    });
    this.sendBtn.on('click', this.handleSend, this);
     this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);
    this.attach=[];
     Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.MailWin.sendMail")+' '+WtfGlobal.getLocaleText("acc.field.With")+this.label,  //"Send Mail",
        buttons: [this.sendBtn,this.closeBtn]
    },config);

    Wtf.account.MailWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.MailWindow, Wtf.Window, {
   getRecord:function(){
       var mode="";
       var url="";
       if (this.data.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId){ //For Purchase Requisitiom module we are taking Email template from DB
            url="MailNotify/ReplacePlaceholdersofEmailContent.do";
       } else {
            url="ProfileHandler/getAllUserDetails.do";
       }
       
        Wtf.Ajax.requestEx({
//            url:Wtf.req.base+"UserManager.jsp",
        url:url,
            params:{
                personname:this.rec.data.personname,
                moduleid:this.data.moduleid,
                fieldid:Wtf.Email_Button_From_Report,
                billid:this.data.billid,
                lid:Wtf.userid,
                mode:mode
            }
        },this,this.genSuccessResponse,this.genFailureResponse);

    },
    genSuccessResponse:function(response){
        if (this.data.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId){
            this.emailid = response.emailid;
            this.userMailid = response.emailid;
            var a = '<br><br>' + WtfGlobal.getLocaleText("acc.field.Attach") + this.label + WtfGlobal.getLocaleText("acc.field.PDF");//this.sendCopy.show();
            if (this.emailid != undefined && this.emailid != "") {
                a = '<span>' + WtfGlobal.getLocaleText("acc.field.Sendmeemailidacopy") + '</span>';
            } else {
                this.sendCopy.hide();
            }
            this.tplSummary = new Wtf.XTemplate(a);
            this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body, {
                emailid: this.emailid
            });
            this.Subject.setValue(response.subject);
            this.Message.setValue(response.message);
        } else {
        this.userdata=response.data[0];
        var a='<br><br>'+WtfGlobal.getLocaleText("acc.field.Attach")+this.label+ WtfGlobal.getLocaleText("acc.field.PDF");//this.sendCopy.show();
        if(this.userdata!=null&&this.userdata.emailid!=undefined&&this.userdata.emailid!=""){
            
            a='<span>'+WtfGlobal.getLocaleText("acc.field.Sendmeemailidacopy")+'</span>';
        }else{this.sendCopy.hide();}
        this.tplSummary=new Wtf.XTemplate(a);
        this.userdata=response.data[0];
        if(Wtf.account.companyAccountPref.defaultmailsenderFlag==Wtf.UserMail && this.userdata!=null&&this.userdata.emailid!=undefined && this.userdata.emailid!="" ){
            this.emailId=this.userdata.emailid;
        }else{
            this.emailId=Wtf.account.companyAccountPref.companyEmailId;
        }
        if(this.tax1099){
            this.southTpl.hide();
//            this.sendCopy.setValue(false);
//            this.sendPdf.setValue(false);
//            this.sendCopy.hide();
//            this.sendPdf.hide();
            var m = Wtf.DomainPatt.exec(window.location);
            var paramstr="getTaxCode.jsp?personid="+this.data.personid;
            m=window.location.href.replace(m[0],paramstr)
            this.Subject.setValue(WtfGlobal.getLocaleText("acc.MailWin.taxID"));
            this.Message.setValue(WtfGlobal.getLocaleText("acc.MailWin.dear")+" "+this.data.personname+"<br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID1")+" <br/>"+
                "<br/>"+
                "<a href="+m+">"+WtfGlobal.getLocaleText("acc.MailWin.taxID2")+"</a><br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                "<br/>"+
                WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                "<br/>"+
                companyName+"  "+WtfGlobal.getLocaleText("acc.MailWin.taxID5")+" <br/>");
        }else{
            if(this.isinvoice){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{emailid:this.userdata.emailid});
                    this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    ((!this.isCustomer)?(this.isConsignment?WtfGlobal.getLocaleText("acc.MailWin.conpurchaseinvoicemsg"):WtfGlobal.getLocaleText("acc.MailWin.purchaseinvoicemsg")):WtfGlobal.getLocaleText("acc.MailWin.msg1")+" "+this.label+" "+" for ")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg2")+" "+this.rec.data.duedate.format("d-m-Y")+".<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText({key:"acc.MailWin.msg3",params:[this.label]})+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    this.label+" "+ WtfGlobal.getLocaleText("acc.invoice.no")+":"+this.data.billno+"");
             } else if(this.isQuotation && this.isCustomer){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                 this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{emailid:this.userdata.emailid});
                     this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.cqmsg8")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.cqmsg9")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg10")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                     "<br/>"+
                     this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.cqmsg12")+":"+this.data.billno+"");
              } else if(this.isRFQ){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.billno);
                 this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{emailid:this.userdata.emailid});
                     this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+WtfGlobal.getLocaleText("acc.ledger.accAllTransactions")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg22")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg9")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg10")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                     "<br/>"+
                     this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg20")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg21")+":"+this.data.billno+"");
              } else if(this.isQuotation && !this.isCustomer){
                 this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                 this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{emailid:this.userdata.emailid});
                     this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                     "<br/>"+
                     (this.label === "Vendor Quotation"?WtfGlobal.getLocaleText("acc.MailWin.vqmsg8"):WtfGlobal.getLocaleText("acc.MailWin.avqmsg8"))+" "+this.rec.data.date.format("F Y")+". <br/>"+
                     "<br/>"+
                     (this.label === "Vendor Quotation"?WtfGlobal.getLocaleText("acc.MailWin.vqmsg9"):WtfGlobal.getLocaleText("acc.MailWin.avqmsg9"))+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg10")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                     "<br/>"+
                     this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                     "<br/>"+
                     WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                     (this.label === "Vendor Quotation"?WtfGlobal.getLocaleText("acc.MailWin.vqmsg12"):WtfGlobal.getLocaleText("acc.MailWin.avqmsg12"))+":"+this.data.billno+"");
              }else if(this.isCash && !this.isCustomer){
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.cpmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.cpmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.cpmsg7")+":"+this.data.billno+"");
            }else if(this.isCash && this.isCustomer){
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.csmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.csmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.csmsg7")+":"+this.data.billno+"");
            } else if(this.isOrder && !this.isCustomer && this.isFixedAsset){// for vendor<--  purchase order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.fixpomsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.fixpomsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.fixpomsg7")+":"+this.data.billno+"");
            } else if(this.isSecurityGate){
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.sgemsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.sgemsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.sgemsg7")+":"+this.data.billno+"");
            }else if(this.isOrder && !this.isCustomer && !this.isConsignment){// for vendor<--  purchase order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.pomsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.pomsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.pomsg7")+":"+this.data.billno+"");
                 } else if(this.isOrder && !this.isCustomer &&  this.isConsignment){// for vendor<--  Conisignment request
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.convensomsg")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.convensomsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consomsg7")+":"+this.data.billno+"");
            } else if(this.isOrder && this.isCustomer &&  this.isConsignment){// for customer<--  sales order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consomsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consomsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consomsg7")+":"+this.data.billno+"");
            } else if(this.isOrder && this.isCustomer && !this.isConsignment){// for customer<--  sales order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.somsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.somsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.somsg7")+":"+this.data.billno+"");
                } else if(this.isDoOrGr && this.isCustomer && !this.isConsignment){// Delivery Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    (this.label === "Delivery Order"?WtfGlobal.getLocaleText("acc.MailWin.domsg1"):WtfGlobal.getLocaleText("acc.MailWin.adomsg1"))+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    (this.label === "Delivery Order"?WtfGlobal.getLocaleText("acc.MailWin.domsg3"):WtfGlobal.getLocaleText("acc.MailWin.adomsg3"))+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    (this.label === "Delivery Order"?WtfGlobal.getLocaleText("acc.MailWin.domsg7"):WtfGlobal.getLocaleText("acc.MailWin.adomsg7"))+":"+this.data.billno+"");
            } else if(this.isDoOrGr && this.isCustomer && this.isConsignment){//  isConsignment Delivery Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.condomsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.condomsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.condomsg7")+":"+this.data.billno+"");
            } else if(this.isSalesReturn && this.isCustomer && this.isConsignment) {// Delivery Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consrmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consrmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.consrmsg7")+":"+this.data.billno+"");
            } else if(this.isSalesReturn && this.isCustomer && !this.isConsignment) {// Delivery Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.srmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.srmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.srmsg7")+":"+this.data.billno+"");
            } else if(this.isSalesReturn && !this.isCustomer && !this.isConsignment) {//purchase return
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.prmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.prmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.prmsg7")+":"+this.data.billno+"");
                 } else if(this.isSalesReturn && !this.isCustomer && this.isConsignment) {//purchase return
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.conprmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.conprmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.conprmsg7")+":"+this.data.billno+"");
            }else if(this.isDoOrGr && !this.isCustomer && !this.isConsignment){// Goods Receipt Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.gromsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.gromsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.gromsg7")+":"+this.data.billno+"");
                
               } else if(this.isDoOrGr && !this.isCustomer && this.isConsignment) {// Consignment Goods Receipt Order
                this.Subject.setValue(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                });
               this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.data.personname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.congrmsg1")+" "+this.rec.data.date.format("F Y")+". <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.congrmsg3")+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.congrmsg7")+":"+this.data.billno+"");
                }
                    else {
                this.Subject.setValue(unescape(this.label+"-"+companyName+"-"+this.rec.data.personname+"-"+this.rec.data.billno));
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.userdata.emailid
                    });
                this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+unescape(this.data.personname)+"<br/>"+
                    "<br/>"+
                    ((this.label!='Payment Receipt')?WtfGlobal.getLocaleText("acc.MailWin.msgpm13"):WtfGlobal.getLocaleText("acc.MailWin.msg13"))+" "+this.rec.data.billdate.format("F Y")+". <br/>"+
                    "<br/>"+
                     WtfGlobal.getLocaleText({key:"acc.MailWin.msg3",params:[this.label]})+" "+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+this.emailId+" <br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg5")+"<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
                    "<br/>"+
                    this.userdata.fname+" "+this.userdata.lname+"<br/>"+
                    "<br/>"+
                    "<br/>"+
                    WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
                    ((this.label!='Payment Receipt')?WtfGlobal.getLocaleText("acc.MailWin.rmsgpm7"):WtfGlobal.getLocaleText("acc.MailWin.rmsg7"))+":"+this.data.billno+"");
            }
        }
    }
    }, 
                    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],1);
    },
    onRender: function(config){
        this.getRecord();
        this.createForm();
        var image="../../images/accounting_image/bank-reconciliation.jpg";

    this.documentRec = Wtf.data.Record.create ([
        {name:'docname'},
        {name:'docid'},
        {name:'doctypeid'}        
    ]);
    this.documentStoreUrl = "ACCInvoiceCMN/getAttachDocuments.do";    
    this.documentStore = new Wtf.data.Store({
        url:this.documentStoreUrl,
        baseParams:{
            id:this.rec.data.billid
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.documentRec)
    });
        this.documentStore.load();
        /*
         * attached document in grid should not be added in email template as we have given 
         * attach functionality in Purchase Requisition Module
         */
        if (!this.isPurchaseRequisition) {
            this.documentStore.on('load', this.showCheckBox, this);
        }
        this.add({
//            region: 'north',
//            height:75,
//            border: false,
//            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
//            html:getTopHtml('Send Mail','Send Mail',image,false)
//        },{
            region: 'center',
            border: false,
            //autoScroll:true,
            baseCls:'bckgroundcolor',
            //layout: 'fit',
            items:this.Form
        }/*,{
            region: 'south',
            hidden:this.tax1099,
            border: false,
            height:70,//(Wtf.isIE?(this.tax1099?0:53):(this.tax1099?0:70)),
            autoScroll:true,
            baseCls:'bckgroundcolor',
            style: 'padding:0px 0px 0px 10px;',
            layout: 'border',
            items: [{
                region: 'west',
                border: false,
                width:40,
                layout: 'border',
                items:[{
                    region: 'center',
                    border: false,
                    baseCls:'bckgroundcolor',
                    layout: 'fit',
                    items: this.sendCopy= new Wtf.form.Checkbox({
                        name:'emailcopy',
                        checked:!this.tax1099,
                        width: 20
                    })
                },{
                    layout:'fit',
                    height:45,
                    region:'south',
                    baseCls:'bckgroundcolor',          
                    border:false,
                    items:this.sendpdf= new Wtf.form.Checkbox({
                        name:'sendpdf',
                        checked:!this.tax1099,
                        style: 'padding:0px 0px 10px 0px;',
                        width: 20
                    })
                }]
            },
            this.southTpl,this.southDownPanel]
        }*/);
        Wtf.account.ReconciliationWindow.superclass.onRender.call(this, config);                               
    },
    
    createForm:function(){
         this.southTpl=new Wtf.Panel({
            region: 'center',
            border: false,
//            height:30,
            baseCls:'bckgroundcolor',
            style: 'padding-top:8px;',
            layout: 'fit'
         });
         this.southDownPanel=new Wtf.Panel({
            region: 'south',
            border: false,
//            height : 100,
//            bodyStyle : 'background-color:#f1f1f1;',
            baseCls:'bckgroundcolor',                      
            layout: 'fit'
         })
         this.Rec = Wtf.data.Record.create ([
            {name: 'userid'},
            {name: 'username',mapping:'accname'},
            {name: "fullname",mapping:'accname'},
            {name: "emailid",mapping:'email'},
            {name: 'image',mapping:''},
            {name:'accid'},
            {name:'openbalance'},
            {name:'id'},
            {name:'title'},
            {name:'accname'},
            {name:'address'},
            {name:'company'},
            {name:'email'},
            {name:'contactno'},
            {name:'contactno2'},
            {name:'fax'},
            {name:'shippingaddress'},
            {name:'pdm'},
            {name:'pdmname'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'bankaccountno'},
            {name:'termid'},
            {name:'termname'},
            {name:'other'},
            {name: 'leaf'},
            {name: 'currencysymbol'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'deleted'},
            {name: 'creationDate' ,type:'date'},
            {name: 'level'}
        ]);
        this.url=this.isCustomer?"ACCCustomerCMN/getCustomers.do":"ACCVendorCMN/getVendors.do";
        this.baseParams=this.tax1099?{
                deleted:false,
                nondeleted:true
            }:{
                mode:2,
                group:[10]//:[13])
            }
        this.contactStore = new Wtf.data.Store({
            //this.businessPerson+
    //        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            url:this.url,
            baseParams:this.baseParams,
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalcount",
                root: "data"
            },this.Rec)
        });
        this.resultTpl = new Wtf.XTemplate(
            '<tpl for="."><div class="search-item">',
                '<img src="{[this.f(values)]}">',
                '<div><h3><span>{fullname} - ({username})</span></h3><br>',
                '<div class="search-item-email">{emailid}</div></div>',
            '</div></tpl>', {
            f: function(val){
                if(val.image == "")
                    val.image = "../../images/user100.png";
                return val.image;
            },
            scope: this
        });

        this.To = new Wtf.form.ComboBox({
            store: this.contactStore,
            name:"to",
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to")+"*",
         //   defaultValue:this.data.personemail,
            emptyText:';',// WtfGlobal.getLocaleText("acc.MAilWin.msg19"),  //'Type user name and select one from the list',
            tabIndex:1,
            cls: 'search-username-combo',
            listClass:'search-username-combo_list', // this class is used to show the scroll bar in list
            displayField: 'emailid',
            typeAhead: false,
            loadingText: WtfGlobal.getLocaleText("acc.MAilWin.search"),  //'Searching...',
            pageSize:3,
            anchor:'95%',
            hideTrigger:true,
            tpl: this.resultTpl,
            itemSelector: 'div.search-item',
            minChars: 1,
            onSelect: function(record) {
                // override default onSelect to do redirect
                var v = this.getValue().toString();
                if (record.data['emailid'] !== "") {
                    var isAlreadyPresent = false;
                    var currentValuesArr = v.split(';');
                    var selectedValuesArr = record.data['emailid'].split(';');
                    /*
                     * Below if block is used to check the duplicates email in the selected string. //ERP-21905
                     */
                    if (currentValuesArr !== "" && selectedValuesArr !== "" && currentValuesArr.length > 0 && selectedValuesArr.length > 0) {
                        for (var i = 0; i < currentValuesArr.length; i++) {
                            for (var j = 0; j < selectedValuesArr.length; j++) {
                                if (currentValuesArr[i] === selectedValuesArr[j]) {
                                    isAlreadyPresent = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!isAlreadyPresent) {
                        if (v.charAt(v.length) == ';')
                            this.setValue(v + record.data['emailid'] + ';');
                        else {
                            var temp = '';
                            if (v.indexOf(';') !== -1)
                                temp = v.substring(0, v.lastIndexOf(';') + 1);
                            else
                                temp = '';
                            this.reset();
                            this.setValue(temp + record.data['emailid'] + ';');
                        }
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MailWin.msg15")], 2); //Showing alert message when email id is alreday present in receiptatent list.
                    }
                    this.focus();
                }
            }
        });
         this.EmailMessage = new Wtf.Panel({
            border:false,
            xtype:'panel',
            bodyStyle:'padding:0px 0px 10px 65px;',
            html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.MailWin.msg16") +'</font>'
        })
        /*
         * Added to get Purchase Requisition Template in combo box of mail template window  
         * 
         */
        var templateRecord = new Wtf.data.Record.create([
        {
            name: 'templateid'
        },

        {
            name: 'templatename'
        }
        ]);
        this.templateStore = new Wtf.data.Store({
            record : templateRecord
        });
         this.moduleid=this.data.moduleid
         this.allowDDTemplate = (this.moduleid== Wtf.Acc_Purchase_Requisition_ModuleId) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
        this.templateCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDDtemplate.email.select.template")+"*",
            emptyText:"Select Template",
            valueField:'templateid',
            displayField:'templatename',
            store:this.templateStore,
            width:200,
            scope:this,
            labelStyle : "width:150px;",
            hidden: (this.allowDDTemplate)?false:true,
            hideLabel:(this.allowDDTemplate)?false:true,
            typeAhead: true,
            forceSelection: true,
            name:'templateid',
            hiddenName:'templateid',
            allowBlank:(this.allowDDTemplate)?false:true
        });
        
        var colModArray = GlobalCustomTemplateList[this.moduleid];
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            var none  = new templateRecord({
                    templateid :"None",
                    templatename : "None"
                });
            this.templateStore.add(none);
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !addTemplate(id1)){
                    continue;
                }
                var record1  = new templateRecord({
                    templateid :id1,
                    templatename : name1
                });
                this.templateStore.add(record1);
                if(colModArray.length == 1){
                    this.templateCombo.setValue(id1);
            }
        }
        }
        if (this.isPurchaseRequisition || this.isRFQ) {
            this.Name = new Wtf.common.Select({
                fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven"), //this.businessPerson+"*",
                id: "customerbccc",
                store:  Wtf.vendorAccStore,
                valueField: 'accid',
                displayField: 'accname',
                allowBlank: true,
                emptyText: WtfGlobal.getLocaleText("acc.inv.ven"), //'Select a '+this.businessPerson+'...',
                mode: 'local',
                extraFields:['billingEmail'],
                typeAhead: true,
                forceSelection: true,
                selectOnFocus: true,
                anchor: "80%",
                multiSelect: true,
                xtype: 'select',
                triggerAction: 'all'
            });

            this.VendorBccEmail = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.VendorBccEmails"), //
                name: 'othervendoremailsfh',
                emptyText: WtfGlobal.getLocaleText("acc.field.VendorBccEmailsEmptytxt"),
                anchor: '80%',
                scope: this
            });

            this.bccEmailFieldSet = new Wtf.form.FieldSet({
                autoHeight: true,
                width: 600,
                title: "<span wtf:qtip='" + "BCC" + "'>" + "BCC" + "</span>",
                items: [this.Name,this.VendorBccEmail,this.EmailMessage]

            });
            chkvenaccload();
             Wtf.vendorAccStore.on('load', function() {
                if (this.isRFQ) {
                    this.Name.setValue(this.data.personid);
                }
             }, this);
             
             if (this.isRFQ) {
                this.Name.setValue(this.data.personid);
            }
             this.Name.on('change',this.setVendorEmails,this)

        } 
        
         if (this.data.billingEmail != undefined && this.data.billingEmail != "" && this.data.billingEmail != null) {
            var billingEmail = this.data.billingEmail;
            billingEmail = billingEmail.replace(/,/g, ";");
            this.To.setValue(billingEmail + ";")
        }
        if (this.data.personemail != undefined && this.data.personemail != "" && this.data.personemail != null) {
            var personemail = this.data.personemail;
            personemail = personemail.replace(/,/g, ";");
            if (this.otherVendorMails != undefined && this.otherVendorMails != "" && this.otherVendorMails != ";") {
                this.To.setValue(personemail + ";" + this.otherVendorMails)
            } else {
                this.To.setValue(personemail + ";")
            }

        } else if (this.otherVendorMails != "" && this.otherVendorMails != ";") {
            var otherVendorMails = this.otherVendorMails;
            otherVendorMails = otherVendorMails.replace(/,/g, ";");
            this.To.setValue(otherVendorMails)
        }
        this.To.on('beforequery', function(q){
            var qt = q.query.trim();
            var curr_q = qt.substr(qt.lastIndexOf(';')+1);
            curr_q = WtfGlobal.HTMLStripper(curr_q);
            q.query = curr_q;
        }, this)
        this.Subject=new Wtf.form.TextField({
            name:"subject",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.sub"),  //"Subject",
            maxLength:1024,
            anchor:'95%'
        });
        this.attachSupportingDoc = new Wtf.linkPanel({           
            text: WtfGlobal.getLocaleText("acc.mySupportingDocattach.btntxt"),        
            height: 20, 
            hidden:!this.isPurchaseRequisition,
            bodyStyle:'margin-bottom: 5px;margin-left: -5px;margin-top: 5px;'
        });
        this.attachDocFromComputer = new Wtf.linkPanel({           
            text: WtfGlobal.getLocaleText("acc.myCompuerDocattach.btntxt"),
            height: 20, 
            hidden:!this.isPurchaseRequisition,
            bodyStyle:'margin-bottom: 5px;margin-left: -5px;margin-top: 5px;'
        });
        this.count = 1;
        this.attachheight = 45;
        this.attachDocFromComputer.on("linkClicked", this.AttachfromComputer, this);
        this.attachSupportingDoc.on("linkClicked", this.AttachfileWithAttach, this);
        this.Message=new Wtf.newHTMLEditor({
            name:"message",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.message") + "*",  //"Message*",
            xtype:'htmleditor',
            id:'bio',
            anchor:'95%',

            height: 300,
            border: false,
            enableLists: false,
            enableSourceEdit: false,
            enableAlignments: true,
            hideLabel: true
         });
         this.Form=new Wtf.form.FormPanel({
            //region:'north',
            //autoScroll:true,
            height : 'auto',
            //height:(Wtf.isIE?(this.tax1099?512:455):(this.tax1099?495:425)),
            border:false,
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:60,
                //autoScroll:true,
                border:false,
                items:[((this.isPurchaseRequisition || this.isRFQ)?this.bccEmailFieldSet:this.To),
                    ((this.isPurchaseRequisition|| this.isRFQ)?{border:false} : this.EmailMessage),
                    this.Subject, 
                    /*
                     * attachDocFromComputer AND attachSupportingDoc Functionality IS only given For purchase Requisition module only SDP-11016
                     */
                    (this.isPurchaseRequisition?this.attachDocFromComputer:{border:false}),
                    (this.isPurchaseRequisition?this.attachSupportingDoc:{border:false}),
                    this.Message,
                    this.attachPanel = new Wtf.Panel({
                            border : false,
                            height : '50',
                            bodyStyle : 'padding-bottom:5px;padding-top:10px;',
                            items : [{
                                layout : 'table',
                                border : false,
                                items : [this.sendCopy= new Wtf.form.Checkbox({
                                    name:'emailcopy',
                                    checked:!this.tax1099,
                                    width: 20
                                }),{
                                    border : false,
                                    bodyStyle : 'padding-left:5px;',
                                    id : 'sendpadftmp'
                                }
                                ]

                            },{
                                layout : 'table',
                                border : false,
                                items : [this.sendpdf= new Wtf.form.Checkbox({
                                    name:'sendpdf',
                                    checked:(this.allowDDTemplate)?false:((Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink) && (this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId))?false:true,
                                    hidden:(this.allowDDTemplate)?true:false,
                                    style: 'padding:0px 0px 10px 0px;',
                                    width: 20
                                }),{
                                    border : false,
                                    bodyStyle : 'padding-left:5px;',
                                    hidden:(this.allowDDTemplate)?true:false,
                                    html : WtfGlobal.getLocaleText("acc.field.Attach")+" " +this.label+ WtfGlobal.getLocaleText("acc.field.PDF")
                                }]

                            }]
                        }),this.templateCombo] 
            }]
        });
    },
    saveData:function(){ 
        if(!this.Form.getForm().isValid())
                WtfComMsgBox(2,2);
        else{
            var rec=this.Form.getForm().getValues();
            callReconciliationLedger(rec)
            this.close();
        }
    },
     handleClose:function(){
         this.fireEvent('cancel',this)
         this.close();
     },
     AttachfileWithAttach: function () {
        var quotationid = "";
        if (this.data.billid != null & this.data.billid != "") {
            quotationid = this.data.billid;
        }
        var mainGrid = new Wtf.docscomGrid({
            id: 'doc-mydocs',
            border: false,
            treeroot: "My Documents",
            autoWidth: true,
            fromUploadFiles: true,
            treeid: 'doctree-mydocs',
            treeRenderto: 'navareadocs',
            pcid: 1,
            quotationID: quotationid,
            moduleid:this.data.moduleid
        });
        this.win = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.myamails.selectfiles"), //'Select Folder',
            closable: true,
            id: 'selectfolder' + this.id,
            iconCls: "pwnd favwinIcon",
            modal: true,
            height: 410,
            width: 700,
            layout: 'border',
            buttonAlign: 'right',
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.attachDoc.set.btntxt"), //'Ok',
                    handler: function () {
                        var mainArray = [];
                        if (mainGrid.grid1.getSelectionModel().hasSelection()) {
                            var rec = mainGrid.grid1.getSelectionModel().getSelections();
                            for (var recRecord = 0; recRecord < rec.length; recRecord++) {
                                var tmpJson = {};
                                if (rec[recRecord].data.name.substr(rec[recRecord].data.name.lastIndexOf(".")) == ".exe") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.module.sendEmail.fileType.exe")], 2);
                                    return;
                                }
                                if(rec[recRecord].data.name.lastIndexOf(".")!=-1){
                                    tmpJson.id = rec[recRecord].data.docid + rec[recRecord].data.name.substr(rec[recRecord].data.name.lastIndexOf("."));
                                }else{
                                    tmpJson.id = rec[recRecord].data.docid;
                                }
                                tmpJson.name = rec[recRecord].data.name;
                                this.quotationAttachDoc = true;
                                this.attach.push(tmpJson);
                                mainArray.push(tmpJson);
                            }
                            Wtf.getCmp('selectfolder' + this.id).close();
                        }
                        this.onSuccessAttached(mainArray, true);
                    },
                    scope: this
                },
                {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                    handler: function () {
                        Wtf.getCmp('selectfolder' + this.id).close();
                    },
                    scope: this
                }],
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.myamails.selectfiles"), WtfGlobal.getLocaleText("acc.myamails.selectfiles.desc"))
                }, {
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    layout: 'fit',
                    items: [mainGrid]
                }]
        }).show();
    },
     AttachfromComputer:function() {
        this.fileuploadwin = new Wtf.form.FormPanel(
                {
                    url: "ACCInvoiceCMN/attachDocuments.do",
                    waitMsgTarget: true,
                    fileUpload: true,
                    method: 'POST',
                    border: false,
                    scope: this,
                    bodyStyle: 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
                    lableWidth: 50,
                    items: [
                        this.sendInvoiceId = new Wtf.form.Hidden(
                                {
                                    name: 'invoiceid'
                                }),
                        /* Parameter for moduleid*/
                        this.sendModuleidId = new Wtf.form.Hidden(
                                {
                                    name: 'moduleid'
                                }),
                        this.tName = new Wtf.form.TextField(
                                {
                                    fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                                    name: 'file',
                                    inputType: 'file',
                                    width: 200,
                                    blankText: WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                                    allowBlank: false,
                                    msgTarget: 'qtip'
                                })]
                });

        this.upwin = new Wtf.Window(
                {
                    id: 'upfilewin',
                    title: WtfGlobal
                            .getLocaleText("acc.invoiceList.uploadfile"),
                    closable: true,
                    width: 450,
                    height: 120,
                    plain: true,
                    iconCls: 'iconwin',
                    resizable: false,
                    layout: 'fit',
                    scope: this,
                    listeners: {
                        scope: this,
                        close: function () {
                            scope: this;
                            this.fileuploadwin.destroy();
                        }
                    },
                    items: this.fileuploadwin,
                    buttons: [
                        {
                            anchor: '90%',
                            id: 'UploadDoc',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                            scope: this,
                            handler: function(){
                                if (this.fileuploadwin.form.isValid()) {
                                    Wtf.getCmp('UploadDoc').disabled = true;
                                }
                                if (this.fileuploadwin.form.isValid()) {
                                    this.fileuploadwin.form.submit({
                                        scope: this,
                                        failure: function (frm, action) {
                                            this.upwin.close();
                                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                                        },
                                        success: function (frm, action) {
//                                            alert('hi');
                                            var mainArray = [];
                                            var tmpJson = {};
                                            this.upwin.close();
                                            var resObj = eval("(" + action.response.responseText + ")");
                                            if (resObj.name.lastIndexOf(".") != -1) {
                                                tmpJson.id = resObj.docid + resObj.name.substr(resObj.name.lastIndexOf("."));
                                            } else {
                                                tmpJson.id = resObj.docid;
                                            }
                                            tmpJson.name=resObj.name;
                                            this.attach.push(tmpJson);
                                            mainArray.push(tmpJson);
                                            this.onSuccessAttached(mainArray, true);
                                        }
                                    })
                                }
                            }
                        },
                        {
                            anchor: '90%',
                            id: 'closeUploadDoc',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
                            handler: function(){
                                this.upwin.close();
                            },
                            scope: this
                        }]

                });
        this.sendInvoiceId.setValue(this.data.billid);
        this.sendModuleidId.setValue(this.data.moduleid);
        this.upwin.show();
    },
    onSuccessAttached: function (fileInfo, isNewFile) {
        for (var i = 0; i < fileInfo.length; i++) {
            var pid = 'fileattach_' + (isNewFile ? "new_" : "old_") + this.count;
            this.count++;
            var lp = new Wtf.linkPanel({
                id: pid,
                border: false,
                text: WtfGlobal.getLocaleText("acc.field.Remove"), //"Remove",
                guid : fileInfo[i].id,
                nameForDisplay: fileInfo[i].name
            });
            this.attachPanel.insert(this.count, lp);
            lp.on("linkClicked", this.removeFile, this);
            this.attachheight = this.attachheight + 20;
            this.attachPanel.setHeight(this.attachheight);
        }
        this.doLayout();
    },
    removeFile: function (linkDom, linkPanel) {
        var attachmentarr=this.attach;
        var temparr=[];
        for(var i=0;i<attachmentarr.length;i++){
            if(attachmentarr[i].id!=linkPanel.guid){
                temparr.push(attachmentarr[i]);
            }
        }
        this.attach=temparr;
        this.attachPanel.remove(linkPanel, true);
        this.attachheight -= 20;
        this.attachPanel.setHeight(this.attachheight);
//        this.count--;
        this.doLayout();
    },
     handleSend: function(bobj, edfd){
        //FIXME: msg sending problem from saved drafts
        this.sendBtn.disable();
        this.closeBtn.disable();
        if (!(this.isPurchaseRequisition || this.isRFQ)) {
            if (!(this.To.isValid()) || this.To.getValue().trim() == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")], 2);
                //Wtf.Msg.alert('Alert', 'Please specify atleast one recipient.'2);
                this.sendBtn.enable();
                this.closeBtn.enable();
                return;
            }
        } else {
             if ((this.VendorBccEmail.getValue()).trim() == "" && this.Name.getValue().trim() == "" ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")], 2);
                this.sendBtn.enable();
                this.closeBtn.enable();
                return;
            }
        }
        if(this.Subject.getValue().trim()==""){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.MAilWin.msg18"),
                function(btn){
                  if (btn == "yes") {
                      this.sendMail();
                  }
                  else {
                    this.sendBtn.enable();
                    this.closeBtn.enable();
                    return false;
                  }
                }, this);
        } else this.sendMail();
    },
    //Below Method is used to getting Vendor billing email id's and finally send the mail id's to send email to vendor. 
    setVendorEmails:function(c,rec,ind){
        this.bccEmailIds="";
        var vendorIds=this.Name.getValue();
        var selectedValuesArr = vendorIds.split(',');
        var emailStringArray="";
        if (selectedValuesArr.length > 0 && selectedValuesArr != "") {
            for (var i = 0; i < selectedValuesArr.length; i++) {
                emailStringArray = "";
                var record = WtfGlobal.searchRecord(this.Name.store, selectedValuesArr[i], "accid");
                if (record != null && record != undefined && record.data != undefined && record.data.billingEmail != undefined && record.data.billingEmail!="") {
                    emailStringArray = record.data.billingEmail.split(',');
                    for (var j = 0; j < emailStringArray.length; j++) {
                        this.bccEmailIds = this.bccEmailIds + emailStringArray[j] + ";";
                    }
                }
            }

        } 
    },
    sendMail:function(){
        var email="";
        if(this.sendCopy.checked == true){
            if(this.userdata != undefined && this.userdata.emailid != undefined)
                email+=this.userdata.emailid+";";
            else
                email+=this.emailid+";";
        }
       
        if ((this.isPurchaseRequisition ||this.isRFQ) && this.VendorBccEmail != undefined && this.VendorBccEmail != "") {
            if (this.VendorBccEmail.getValue() != "") {
                this.bccEmailIds = this.bccEmailIds + this.VendorBccEmail.getValue() + ";";
                email += this.bccEmailIds.trim();
            } else {
                email += this.bccEmailIds.trim();
            }
        } else {
            email += this.To.getValue().trim();
        }
        
        if ((this.isPurchaseRequisition||this.isRFQ) && email.trim() == "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")], 2);
            this.sendBtn.enable();
            this.closeBtn.enable();
            return;
        }
        var templateRec; 
        var templateName = ""; 
        if (this.templateCombo) {
           templateRec = WtfGlobal.searchRecord(this.templateCombo.store,this.templateCombo.getValue(),"templateid");
        }
        if (templateRec && templateRec.data) {
            templateName = templateRec.data.templatename;
        }
        
        if(this.mode == 8){
            this.moduleid = Wtf.Acc_Make_Payment_ModuleId;
        }else if(this.mode == 4){
            this.moduleid = Wtf.Acc_Receive_Payment_ModuleId;
        }
        var entryno=this.rec.data.billno;
        var msg = this.Message.getValue();
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        var rec=this.Form.getForm().getValues();
        rec.mode=this.mode;
        rec.billid=this.data.billid;
        rec.mailingDate=WtfGlobal.convertToGenericDate(new Date());
        rec.emailid=email;
        rec.amount=this.data.amount;
        rec.currencyid=this.data.currencyid;
        rec.personid=this.data.personid;
        rec.accname=this.data.personname?this.data.personname:"";
        rec.sendpdf=this.tax1099?false:this.sendpdf.getValue();
        if(this.isPurchaseRequisition){
            rec.attachmentSelection='';
            rec.attachments=JSON.stringify(this.attach);
        }else{
        rec.attachmentSelection=this.getAttachmentSelection();
        }
        rec.templateName = templateName;
        rec.isexpenseinv=this.rec.data.isexpenseinv;
        rec.entryno=entryno;
        rec.bills=this.data.billid;     //For Default Export Template of Payment Voucher
        rec.billno=entryno
        rec.templateflag=Wtf.templateflag;  //For Default Export Template of Payment Voucher
        rec.moduleid=this.data.moduleid;
        if(this.data.isadvancepayment!=undefined)
            {        
            rec.isadvancepayment=this.data.isadvancepayment;
            }
        if(this.data.advanceUsed!=undefined)
            {
            rec.advanceUsed=this.data.advanceUsed;
            }
        if(this.data.advanceid!=undefined){
            var advanceFlagVar=false;
            if(this.data.advanceid!="")
                advanceFlagVar=true;
            rec.advanceFlag=advanceFlagVar;    
        }
        if(this.data.advanceamount!=undefined)
            {
            rec.advanceAmount=this.data.advanceamount;
            }
        Wtf.Ajax.requestEx({
            url:"CommonFunctions/sendMail.do",
//                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            params: rec
        },this,this.success,this.failure);
     }, 

    success:function(response){
        if(response.success){
            var label="";
            this.data.isLeaseFixedAsset ? label=WtfGlobal.getLocaleText("acc.lease.DO"):label=this.label;
            WtfComMsgBox([label,response.msg],3);
             if(this.tax1099){
                var rec=[];
                rec.accid=this.data.personid;
                rec.taxidmailon=WtfGlobal.convertToGenericDate(new Date());;
                Wtf.Ajax.requestEx({
                    url:"ACCVendor/saveVendorMailingDate.do",
//                  url:Wtf.req.account+this.businessPerson+'Manager.jsp',
                    params: rec
                },this,this.mailSuccessResponse,this.mailFailureResponse);
             }
           this.handleClose();
         } else {
             var label="";
            this.data.isLeaseFixedAsset ? label=WtfGlobal.getLocaleText("acc.lease.DO"):label=this.label;
             if(response.msg && response.isMsgSizeException){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);        	 
             }else{
                 WtfComMsgBox([label,WtfGlobal.getLocaleText("acc.rem.210")],3);
             }
             this.sendBtn.enable();
             this.closeBtn.enable();
         }
    },

    failure:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.handleClose();
    },
    mailSuccessResponse:function(response){
        this.close();
    },
    mailFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    showCheckBox:function(){
//        for(var i=0;i<this.documentStore.data.length;i++)
//        {
//            this.checkBox= new Wtf.form.Checkbox({
//                boxLabel:this.documentStore.data.items[i].data.docname,
//                id:"Attachment_"+this.documentStore.data.items[i].data.docid,
//                name:this.documentStore.data.items[i].data.docid,   
//                style: 'margin-right:20px;margin-left:12px;',
//                baseCls:'bckgroundcolor',
//                checked:true,
//                width: 300                
//            });
//            this.southDownPanel.add(this.checkBox);
//        }
//        this.southDownPanel.doLayout();
        
        for(var i=0;i<this.documentStore.data.length;i++)
        {
            var docName=this.documentStore.data.items[i].data.docname;
            var docID=this.documentStore.data.items[i].data.docid;
            var fileExt = docName.substr(docName.lastIndexOf("."));
            var selNames =docID + fileExt;
                            this.attachPanel.add({
                                layout : 'table',
                                border : false,
                                items : [new Wtf.form.Checkbox({
                                    //boxLabel:this.documentStore.data.items[i].data.docname,
                                    id:"Attachment_"+this.documentStore.data.items[i].data.docid,
                                    name:this.documentStore.data.items[i].data.docid,   
                                    checked:true,
                                    width: 20
                                    
                                }), {
                                    border : false,
                                    bodyStyle : 'padding-left:5px;',                                    
                                    html : "<a href='#' style='color:#083772;'  title='"+WtfGlobal.getLocaleText("acc.invoiceList.downloaddocument")+"' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+docName+"\")'>"+docName+"</a>"
                                }]

                            })
            
        }
        this.attachPanel.doLayout();
        
//        this.southDownPanel.doLayout();
    },
    getAttachmentSelection:function(){
        var storelength=this.documentStore.data.length;              
        var attachmentSelectionArray=[];
        for(var i=0;i<storelength;i++)
        {
            var cmponent=Wtf.getCmp("Attachment_"+this.documentStore.data.items[i].data.docid);            
            if(cmponent.checked){
                attachmentSelectionArray.push(cmponent.name);
            }
        }
        return attachmentSelectionArray.toString();
    }
});



// ******************************   Mail Window Component For sending Multiple Records   *********************************************//

Wtf.account.MultipleRecordsMailWindow = function(config){
     this.recArr=(config.recArr==undefined?"":config.recArr);
     this.userdata=null;
     this.filterParams=config.filterParams;//this.filterParams are used only for statement of accounts report mail functionality.
//     this.tax1099=(config.tax1099==undefined?false:config.tax1099);
//     this.data=this.rec.data;
     this.mode=null;
     this.configstr = config.configstr;
     this.label=(config.label==undefined?"":config.label);
     this.moduleid=(config.moduleid==undefined?"":config.moduleid);
     this.data=(config.data==undefined?"":config.data);
     this.isCustomer=config.isCustomer;
     this.receivableOrPayableText = (this.isCustomer)?'Receivable':'Payable'
     /*
      * Below flag is used to show the Grid on email window or not.
      * If Invoice Module && selected count <=1 then it will work as it is.
      * If Invoice Module && selected count >1 then it will show two grid's with or without email id
      */
     this.showEmailGrid=false;
     this.isMailToShippingEmail=false;
        if(this.moduleid==Wtf.Acc_Invoice_ModuleId && this.recArr!=undefined && this.recArr!=""&& this.recArr.length>0){
             if (this.recArr.length == 1) {
                 this.showEmailGrid=false;
             }else{
                config.width=1000; 
                config.height=Wtf.isIE?695:657; 
                this.showEmailGrid=true; 
             }
         }
     this.sendBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.MailWin.send")  //'Send'
    });
    this.sendBtn.on('click', this.handleSend, this);
     this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);
    
    this.addEvents({
        "loadReportView": true
    })
    this.attach=[];
     Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.MailWin.sendMail")+" "+WtfGlobal.getLocaleText("acc.field.With")+this.label,  //"Send Mail",
        buttons: [this.sendBtn,this.closeBtn]
    },config);

    Wtf.account.MultipleRecordsMailWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.MultipleRecordsMailWindow, Wtf.Window, {
    getRecord:function(){
        var moduleid='';
        var personname='';
        this.userMailid=null;
        var billid='';
        if(this.label === Wtf.Email_Module_Name_Aged_Recivable){
            if(this.moduleid){
                moduleid= this.moduleid;
            }else{
                moduleid=Wtf.Acc_Customer_ModuleId;
            }
        }else if(this.label === Wtf.Email_Module_Name_Aged_Payable){
            moduleid=Wtf.Acc_Vendor_ModuleId;
        }else if(this.label === Wtf.Email_Module_Name_Customer_Account_Statement || this.label === Wtf.Email_Module_Name_Vendor_Account_Statement){
            moduleid=Wtf.Account_Statement_ModuleId;
           personname=this.filterParams.accountname;
        }else{
            if(this.moduleid){
                moduleid= this.moduleid;
            }
        }
        
        if(this.recArr[0]!=undefined && this.recArr[0].data!=undefined && this.recArr[0].data.personname!=undefined){
            personname=this.recArr[0].data.personname;    
            billid=this.recArr[0].data.billid;
            this.emailid=this.recArr[0].data.billingEmail;
        }else if(this.recArr!=undefined && this.recArr.data!=undefined && this.recArr.data.personname!=undefined){
            personname=this.recArr.data.personname; 
            billid=this.recArr.data.billid;
             this.emailid=this.recArr.data.billingEmail;
        }
        
        var url="";
        if((this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId 
                || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId ||
                this.moduleid==Wtf.Acc_Sales_Return_ModuleId || this.moduleid==Wtf.Acc_Purchase_Return_ModuleId ||
                this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId) && !this.showEmailGrid){
            url="MailNotify/ReplacePlaceholdersofEmailContent.do";
        }else{
            url="MailNotification/getEmailTemplateToSendMail.do";
        }
        var mode="";
        if(this.moduleid == Wtf.Acc_Make_Payment_ModuleId){
            mode = 8;
        }else if(this.moduleid == Wtf.Acc_Receive_Payment_ModuleId){
            mode = 4;
        }
        
        Wtf.Ajax.requestEx({
            url:url,
            params:{
                personname:personname,
                moduleid:moduleid,
                fieldid:Wtf.Email_Button_From_Report,
                billid:billid,
                mode:mode
            }
        },this,this.genSuccessResponse,this.genFailureResponse); 
            
//        Wtf.Ajax.requestEx({
//            url:"ProfileHandler/getAllUserDetails.do",
//            params:{
//                mode:11,
//                lid:Wtf.userid
//            }
//        },this,this.genSuccessResponse,this.genFailureResponse);

    },
    genSuccessResponse:function(response){
        
        if(response.success === true) {    
            this.Subject.setValue(response.subject);
            this.Message.setValue(response.message);
            if(response.templateid){
                this.templateCombo.setValue(response.templateid);
            }
            if (response.isMailToShippingEmail) {
                this.isMailToShippingEmail = true;
            }
            if(this.moduleid!=Wtf.Acc_Purchase_Order_ModuleId && this.moduleid!=Wtf.Acc_Sales_Order_ModuleId){
                this.emailid=response.emailid;
                this.userMailid=response.emailid;
                var a='<br><br>'+WtfGlobal.getLocaleText("acc.field.Attach")+this.label+ WtfGlobal.getLocaleText("acc.field.PDF");//this.sendCopy.show();
                if(this.emailid!=undefined&&this.emailid!=""){
                    a='<span>'+WtfGlobal.getLocaleText("acc.field.Sendmeemailidacopy")+'</span>';
                }else{
                    this.sendCopy.hide();
                }
                this.tplSummary=new Wtf.XTemplate(a);
                this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                    emailid:this.emailid
                });
                if(response.isMailToShippingEmail){
                    var shipEmail = "";
                    var record = undefined;
                    //if send mail to shipping address then set shipping email id in 'To' field.
                    if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
                       record = this.recArr[0];   
                    } else {
                       record =   this.recArr;
                    }                       
                    if(record.data.shippingEmail!=undefined && record.data.shippingEmail!="" && record.data.shippingEmail!=null){
                        shipEmail = record.data.shippingEmail.replace(/,/g, ";");
                    }
                    this.To.setValue(shipEmail + ";");
                }
            }else{
                var useremailid=response.emailid;
                this.userMailid=response.emailid;
                var a='<br><br>'+WtfGlobal.getLocaleText("acc.field.Attach")+this.label+ WtfGlobal.getLocaleText("acc.field.PDF");//this.sendCopy.show();
                if(useremailid!=undefined && useremailid!=""){
                    a='<span>'+WtfGlobal.getLocaleText("acc.field.Sendmeemailidacopy")+'</span>';
                    this.tplSummary=new Wtf.XTemplate(a);
                    this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{
                        emailid:useremailid
                    });
                }else{
                    this.sendCopy.hide();
                }
                if(response.isMailToShippingEmail){
                    //if send mail to shipping address then set shipping email id in 'To' field.
                    var record = undefined;
                    if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
                       record =this.recArr[0];   
                    } else {
                        record = this.recArr;
                    }
                    if(record.data.shippingEmail!=undefined && record.data.shippingEmail!="" && record.data.shippingEmail!=null){
                        this.emailid = record.data.shippingEmail.replace(/,/g, ";");
                    }
                } else{
                    this.emailid=this.emailid.replace(/,/g, ";");
                }
                this.To.setValue(this.emailid + ";")
            }
            
        }
           
        if (this.showEmailGrid) {
            /*
             * Create the Data of selected Invoices 
             */
            this.createDataArray();
            /*
             * Load the data in both store with or without Email Grid
             */
            this.withEmailStore.loadData(this.withEmailArray);
            this.withOutEmailStore.loadData(this.withOutEmailArray);
            this.withOutEmailStore.totalLength = this.withOutEmailArray.length;
        }
    //        this.userdata=response.data[0];
    //        var a='<br><br>'+WtfGlobal.getLocaleText("acc.field.Attach")+this.label+ WtfGlobal.getLocaleText("acc.field.PDF");//this.sendCopy.show();
    //        if(this.userdata!=null&&this.userdata.emailid!=undefined&&this.userdata.emailid!=""){
    //        
    //            a='<span>'+WtfGlobal.getLocaleText("acc.field.Sendmeemailidacopy")+'</span>';
    //        }else{this.sendCopy.hide();}
    //        this.tplSummary=new Wtf.XTemplate(a);
    //        this.userdata=response.data[0];
    //        
    ////        this.Subject.setValue(this.label+"-"+companyName);
    //        this.tplSummary.overwrite(Wtf.getCmp('sendpadftmp').body,{emailid:this.userdata.emailid});
    //            this.Message.setValue(WtfGlobal.getLocaleText("acc.MAilWin.hello")+" "+this.recArr[0].data.personname+"<br/>"+
    //            "<br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.field.WehaveenclosedyourAged")+this.receivableOrPayableText+' '+WtfGlobal.getLocaleText("acc.field.Invoicesdetails")+" <br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.field.Ifyouhaveanyquestionsaboutdetailpleasephone/mailat")+' '+Wtf.account.companyAccountPref.companyPhoneNo+(Wtf.account.companyAccountPref.companyPhoneNo.length>0?"/":"")+Wtf.account.companyAccountPref.companyEmailId+" <br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.msg4")+"<br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.taxID3")+"<br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.taxID4")+"<br/>"+
    //            "<br/>"+
    //            this.userdata.fname+" "+this.userdata.lname+"<br/>"+
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.msg11")+"<br/>");
    //            "<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.msg6")+"<br/>"+
    //            WtfGlobal.getLocaleText("acc.MailWin.msg7")+":"+'bill no.'+"");
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],1);
    },
    
    onRender: function(config){
        this.createForm();
        this.getRecord();
        var image="../../images/accounting_image/bank-reconciliation.jpg";

//        this.documentRec = Wtf.data.Record.create ([
//            {name:'docname'},
//            {name:'docid'},
//            {name:'doctypeid'}        
//        ]);
//        this.documentStoreUrl = "ACCInvoiceCMN/getAttachDocuments.do";    
//        this.documentStore = new Wtf.data.Store({
//        url:this.documentStoreUrl,
//        baseParams:{
//            id:this.rec.data.billid
//        },
//        reader: new Wtf.data.KwlJsonReader({
//            root: "data"
//        },this.documentRec)
//        });
//        this.documentStore.load();    
//        this.documentStore.on('load', this.showCheckBox, this);    
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            items:this.Form
        });
        Wtf.account.MultipleRecordsMailWindow.superclass.onRender.call(this, config);                               
    },
    createForm:function(){
       /*
        *  Create the grid's for showing with email or without email data
       */
        if (this.showEmailGrid) {
            this.createDisplayGrid();
        }
        this.southTpl=new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            style: 'padding-top:8px;',
            layout: 'fit'
        });
        this.southDownPanel=new Wtf.Panel({
            region: 'south',
            border: false,
            baseCls:'bckgroundcolor',                      
            layout: 'fit'
        })
        this.Rec = Wtf.data.Record.create ([
        {name: 'userid'},
        {name: 'username',mapping:'accname'},
        {name: "fullname",mapping:'accname'},
        {name: "emailid",mapping:'email'},
        {name: 'image',mapping:''},
        {name:'accid'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'accname'},
        {name:'address'},
        {name:'company'},
        {name:'email'},
        {name:'contactno'},
        {name:'contactno2'},
        {name:'fax'},
        {name:'shippingaddress'},
        {name:'pdm'},
        {name:'pdmname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'bankaccountno'},
        {name:'termid'},
        {name:'termname'},
        {name:'other'},
        {name: 'leaf'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
        {name: 'level'}
        ]);
        this.url=this.isSalesSideDocMail(this.moduleid) ?"ACCCustomerCMN/getCustomers.do":"ACCVendorCMN/getVendors.do";
        this.baseParams=this.tax1099?{
            deleted:false,
            nondeleted:true
        }:{
            mode:2,
            group:[10]//:[13])
        }
        this.contactStore = new Wtf.data.Store({
            url:this.url,
            baseParams:this.baseParams,
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalcount",
                root: "data"
            },this.Rec)
        });
        this.resultTpl = new Wtf.XTemplate(
            '<tpl for="."><div class="search-item">',
            '<img src="{[this.f(values)]}">',
            '<div><h3><span>{fullname} - ({username})</span></h3><br>',
            '<div class="search-item-email">{emailid}</div></div>',
            '</div></tpl>', {
                f: function(val){
                    if(val.image == "")
                        val.image = "../../images/user100.png";
                    return val.image;
                },
                scope: this
            });

        this.To = new Wtf.form.ComboBox({
            store: this.contactStore,
            name:"to",
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to")+"*",
            emptyText:';' ,// WtfGlobal.getLocaleText("acc.MAilWin.msg19"),  'Type user name and select one from the list',
            tabIndex:1,
            cls: 'search-username-combo',
            listClass:'search-username-combo_list', // this class is used to show the scroll bar in list
            displayField: 'emailid',
            typeAhead: false,
            loadingText: WtfGlobal.getLocaleText("acc.MAilWin.search"),  //'Searching...',
            pageSize:3,
            hidden: this.showEmailGrid,
            hideLabel:this.showEmailGrid,
            anchor:'95%',
            hideTrigger:true,
            tpl: this.resultTpl,
            itemSelector: 'div.search-item',
            minChars: 1,
            onSelect: function(record){
                var v = this.getValue().toString();
                var isAlreadyPresent=false;
                
                /*
                 * Below if block is used to check the duplicates email in the selected string. //ERP-21905
                */
                if (record.data['emailid'] !== "") {
                    var currentValuesArr = v.split(';');
                    var selectedValuesArr = record.data['emailid'].split(';');
                    if (currentValuesArr !== "" && selectedValuesArr !== "" && currentValuesArr.length > 0 && selectedValuesArr.length > 0) {
                        for (var i = 0; i < currentValuesArr.length; i++) {
                            for (var j = 0; j < selectedValuesArr.length; j++) {
                                if (currentValuesArr[i] === selectedValuesArr[j]) {
                                    isAlreadyPresent = true;
                                    break;
                                }
                            }
                        }
                }
                if(!isAlreadyPresent){
                    if(v.charAt(v.length) == ';')
                        this.setValue(v+record.data['emailid'] + ';');
                    else{
                        var temp = '';
                        if(v.indexOf(';') !== -1)
                            temp = v.substring(0, v.lastIndexOf(';')+1);
                        else
                            temp = '';
                        this.reset();
                        this.setValue(temp + record.data['emailid'] + ';');
                    }
                } else {
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.MailWin.msg15")],2);//Showing alert message when email id is alreday present in receiptatent list.
                }
                this.focus();
            }
        }
        });
        var toMailStr = this.getToMailString();
        this.To.setValue(toMailStr+";");
        if (this.recArr.length > 0) {
            if (this.recArr[0].data.billingEmail != undefined && this.recArr[0].data.billingEmail != "" && this.recArr[0].data.billingEmail != null) {
                var billingEmail = this.recArr[0].data.billingEmail;
                billingEmail = billingEmail.replace(/,/g, ";");
                this.To.setValue(billingEmail + ";")
            }
        }
        this.To.on('beforequery', function(q){
            var qt = q.query.trim();
            var curr_q = qt.substr(qt.lastIndexOf(';')+1);
            curr_q = WtfGlobal.HTMLStripper(curr_q);
            q.query = curr_q;
        }, this)
        this.Subject=new Wtf.form.TextField({
            name:"subject",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.sub"),  //"Subject",
            maxLength:1024,
            anchor:'95%'
        });
        this.count = 1;
        this.attachheight = 45;
        this.attachSupportingDoc = new Wtf.linkPanel({           
            text: WtfGlobal.getLocaleText("acc.mySupportingDocattach.btntxt"),        
            height: 20, 
            bodyStyle:'margin-bottom: 5px;margin-left: -5px;margin-top: 5px;'
        });
        this.attachDocFromComputer = new Wtf.linkPanel({           
            text: WtfGlobal.getLocaleText("acc.myCompuerDocattach.btntxt"),
            height: 20, 
            bodyStyle:'margin-bottom: 5px;margin-left: -5px;margin-top: 5px;'
        });
        this.attachSupportingDoc.on("linkClicked", this.AttachfileWithAttach, this);
        this.attachDocFromComputer.on("linkClicked", this.AttachfromComputer, this);
        this.Message=new Wtf.newHTMLEditor({
            name:"message",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.MAilWin.message") + "*",  //"Message*",
            xtype:'htmleditor',
            id:'bio',
            anchor:'95%',

            height: 300,
            border: false,
            enableLists: false,
            enableSourceEdit: false,
            enableAlignments: true,
            hideLabel: true
        });
        var templateRecord = new Wtf.data.Record.create([
        {
            name: 'templateid'
        },

        {
            name: 'templatename'
        }
        ]);
        this.templateStore = new Wtf.data.Store({
            record : templateRecord
        });
        var moduleid = (this.moduleid)?this.moduleid:"";
        this.allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                            || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                            || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                            || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                            || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                            || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                            ) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
        this.templateCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDDtemplate.email.select.template")+"*",
            emptyText:"Select Template",
            valueField:'templateid',
            displayField:'templatename',
            store:this.templateStore,
            width:200,
            scope:this,
            labelStyle : "width:150px;",
            hidden: (this.allowDDTemplate)?false:true,
            hideLabel:(this.allowDDTemplate)?false:true,
            typeAhead: true,
            forceSelection: true,
            name:'templateid',
            hiddenName:'templateid',
            allowBlank:(this.allowDDTemplate)?false:true
        });
        var colModArray = GlobalCustomTemplateList[this.moduleid];
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            var none  = new templateRecord({
                    templateid :"None",
                    templatename : "None"
                });
            this.templateStore.add(none);
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !addTemplate(id1)){
                    continue;
                }
                var record1  = new templateRecord({
                    templateid :id1,
                    templatename : name1
                });
                this.templateStore.add(record1);
                if(colModArray.length == 1){
                    this.templateCombo.setValue(id1);
            }
        }
        }
        this.Form=new Wtf.form.FormPanel({
            height : 'auto',
            border:false,
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:60,
                border:false,
                items:[this.To,(this.showEmailGrid?{border:false}:{
                    border:false,
                    xtype:'panel',
                            
                    bodyStyle:'padding:0px 0px 10px 65px;',
                    html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.MailWin.msg16") +'</font>'
                }),this.Subject,
                this.attachDocFromComputer,
                this.attachSupportingDoc,          
                this.Message,
                this.attachPanel = new Wtf.Panel({
                    border : false,
                    height : this.attachheight,
                    labelWidth:60,
                    bodyStyle : 'padding-bottom:5px;padding-top:10px;',
                    items : [{
                        layout : 'table',
                        border : false,
                        items : [this.sendCopy= new Wtf.form.Checkbox({
                            name:'emailcopy',
                            checked:!this.tax1099,
                            width: 20
                        }),{
                            border : false,
                            bodyStyle : 'padding-left:5px;',
                            id : 'sendpadftmp'
                        }
                        ]

                    },{
                        layout : 'table',
                        border : false,
                        items : [this.sendpdf= new Wtf.form.Checkbox({
                            name:'sendpdf',
                            checked:(this.allowDDTemplate)?false:((Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink) && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId))?false:true,
                            hidden:(this.allowDDTemplate)?true:false,
                            style: 'padding:0px 0px 10px 0px;',
                            width: 20
                        }),{
                            border : false,
                            bodyStyle : 'padding-left:5px;',
                            hidden:(this.allowDDTemplate)?true:false,
                            html : WtfGlobal.getLocaleText("acc.field.Attach")+" " +this.label+' '+ WtfGlobal.getLocaleText("acc.field.detailPDF")
                        }]
                        
                    }]
                }),this.templateCombo,
                        (this.showEmailGrid?this.NorthForm = new Wtf.Panel({
                            region: 'center',
                            autoHeight: true,
                            border: false,
                            defaults: {border: false},
                            split: true,
                            layout: 'form',
                            baseCls: 'northFormFormatForEmailGrid',
                            disabledClass: "newtripcmbss",
                            hideMode: 'display',
                            cls: "visibleDisabled",
                            labelWidth: 120,
                            items: [{
                                    layout: 'column',
                                    defaults: {border: false},
                                    items: [{
                                            layout: 'form',
                                            columnWidth: 0.48,
                                            items: [this.emailPresentGrid]
                                        }, {
                                            layout: 'form',
                                            columnWidth: 0.02,
                                            items: [{
                                                    region: 'center',
                                                    border: false,
                                                }]
                                        }, {
                                            layout: 'form',
                                            columnWidth: 0.48,
                                            items: [this.noEmailPresentGrid]
                                        }]
                                }]

                        }):{border:false})]
                }]
        });
    },
    createDisplayGrid: function() {
      
        /*
         * Create store and Grid 
         */
        this.withEmailStore = new Wtf.data.SimpleStore({
            fields: [
            {
                name: 'id'
            },
            {
                name: 'invoiceno'
            },
            {
                name: 'customername'
            },
            {
                name: 'email'
            }
            ],
            data: [],
        });
        
        this.withOutEmailStore = new Wtf.data.SimpleStore({
            fields: [
                {
                    name: 'id'
                },
                {
                    name: 'invoiceno'
                },
                {
                    name: 'customername'
                },
                {
                    name: 'email'
                }
            ],
            proxy: new Wtf.data.HttpProxy({
               url: "abc.do"
            }),
            data: []
        });
        
       
        
        this.emailEditor = new Wtf.form.TextArea({
            name: 'billingEmail',
            maxLength: 254,
            validator: WtfGlobal.validateMultipleEmail
        });

        this.emailPresentGridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
                header: WtfGlobal.getLocaleText("acc.customerList.gridName"),
                sortable: true,
                hidden: true,
                dataIndex: 'id'
            }, {
                header: 'Invoice No',
                dataIndex: 'invoiceno',
                sortable: true
            }, {
                header: 'Customer Name',
                dataIndex: 'customername',
                sortable: true,
            }, {
                header: 'Email',
                dataIndex: 'email',
                sortable: true,
                renderer: WtfGlobal.emailRendererWithoutLink
            }]);

        this.noEmailPresentGridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
                header: WtfGlobal.getLocaleText("acc.customerList.gridName"),
                sortable: true,
                hidden: true,
                dataIndex: 'id'
            }, {
                header: 'Invoice No',
                dataIndex: 'invoiceno',
                sortable: true
            }, {
                header: 'Customer Name',
                dataIndex: 'customername',
                sortable: true,
            }, {
                header: 'Email',
                dataIndex: 'email',
                sortable: true,
                editor: this.emailEditor,
                renderer: WtfGlobal.emailRendererWithoutLink
            }]);

        this.emailPresentGrid = new Wtf.grid.GridPanel({
            title: WtfGlobal.getLocaleText("acc.mail.withemailreport"),
            height: 350,
            width: '97%',
            store: this.withEmailStore,
            cm: this.emailPresentGridcm,
            border: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
        this.noEmailPresentGrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit: 1,
            stripeRows: true,
            title: WtfGlobal.getLocaleText("acc.mail.withoutemailreport")+"<br>"+"(Please "+WtfGlobal.getLocaleText("acc.mail.seperator.comma")+")",
            height: 350,
            width: '97%',
            cm: this.noEmailPresentGridcm,
            store: this.withOutEmailStore,
            border: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
    },
   createDataArray: function() {
        var withEmailRecordArray = [];
        var withOutEmailRecordArray = [];
        this.withEmailArray = [];
        this.withOutEmailArray = [];

        if (this.moduleid == Wtf.Acc_Invoice_ModuleId && this.recArr.length != undefined && this.recArr.length != "") {
            for (var i = 0; i < this.recArr.length; i++) {
                if (this.isMailToShippingEmail?(this.recArr[i].data.shippingEmail != "" && this.recArr[i].data.shippingEmail != undefined):(this.recArr[i].data.billingEmail != "" && this.recArr[i].data.billingEmail != undefined)) {
                    withEmailRecordArray = [];
                    withEmailRecordArray.push(this.recArr[i].data.billid, this.recArr[i].data.billno, this.recArr[i].data.personname, (this.isMailToShippingEmail?this.recArr[i].data.shippingEmail:this.recArr[i].data.billingEmail));
                    this.withEmailArray.push(withEmailRecordArray);
                } else {
                    withOutEmailRecordArray = [];
                    withOutEmailRecordArray.push(this.recArr[i].data.billid, this.recArr[i].data.billno, this.recArr[i].data.personname, "");
                    this.withOutEmailArray.push(withOutEmailRecordArray);
                }
            }
        }
    },
    AttachfileWithAttach: function () {
        var quotationid = "";
        if(this.recArr.data != undefined){
            if (this.recArr.data.billid != null & this.recArr.data.billid != "") {
                quotationid = this.recArr.data.billid;
            }
        } else {//this is executed in cases of sales invoice as we are sending array of object  
            if (this.recArr[0].data.billid != null & this.recArr[0].data.billid!="") {
                quotationid = this.recArr[0].data.billid;
        }
        }
        var mainGrid = new Wtf.docscomGrid({
            id: 'doc-mydocs',
            border: false,
            treeroot: "My Documents",
            autoWidth: true,
            fromUploadFiles: true,
            treeid: 'doctree-mydocs',
            treeRenderto: 'navareadocs',
            pcid: 1,
            quotationID: quotationid,
            moduleid:this.moduleid
        });
        this.win = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.myamails.selectfiles"), //'Select Folder',
            closable: true,
            id: 'selectfolder' + this.id,
            iconCls: "pwnd favwinIcon",
            modal: true,
            height: 410,
            width: 700,
            layout: 'border',
            buttonAlign: 'right',
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.attachDoc.set.btntxt"), //'Ok',
                    handler: function () {
                        var mainArray = [];
                        if (mainGrid.grid1.getSelectionModel().hasSelection()) {
                            var rec = mainGrid.grid1.getSelectionModel().getSelections();
                            for (var recRecord = 0; recRecord < rec.length; recRecord++) {
                                var tmpJson = {};
                                if (rec[recRecord].data.name.substr(rec[recRecord].data.name.lastIndexOf(".")) == ".exe") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.module.sendEmail.fileType.exe")], 2);
                                    return;
                                }
                                if(rec[recRecord].data.name.lastIndexOf(".")!=-1){
                                    tmpJson.id = rec[recRecord].data.docid + rec[recRecord].data.name.substr(rec[recRecord].data.name.lastIndexOf("."));
                                }else{
                                    tmpJson.id = rec[recRecord].data.docid;
                                }
                                tmpJson.name = rec[recRecord].data.name;
                                this.quotationAttachDoc = true;
                                this.attach.push(tmpJson);
                                mainArray.push(tmpJson);
                            }
                            Wtf.getCmp('selectfolder' + this.id).close();
                        }
                        this.onSuccessAttached(mainArray, true);
                    },
                    scope: this
                },
                {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                    handler: function () {
                        Wtf.getCmp('selectfolder' + this.id).close();
                    },
                    scope: this
                }],
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.myamails.selectfiles"), WtfGlobal.getLocaleText("acc.myamails.selectfiles.desc"))
                }, {
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    layout: 'fit',
                    items: [mainGrid]
                }]
        }).show();
    },
    AttachfromComputer: function () {
        this.fileuploadwin = new Wtf.form.FormPanel(
                {
                    url: "ACCInvoiceCMN/attachDocuments.do",
                    waitMsgTarget: true,
                    fileUpload: true,
                    method: 'POST',
                    border: false,
                    scope: this,
                    bodyStyle: 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
                    lableWidth: 50,
                    items: [
                        this.sendInvoiceId = new Wtf.form.Hidden(
                                {
                                    name: 'invoiceid'
                                }),
                        /* Parameter for moduleid*/
                        this.sendModuleidId = new Wtf.form.Hidden(
                                {
                                    name: 'moduleid'
                                }),
                        this.tName = new Wtf.form.TextField(
                                {
                                    fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                                    name: 'file',
                                    inputType: 'file',
                                    width: 200,
                                    blankText: WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                                    allowBlank: false,
                                    msgTarget: 'qtip'
                                })]
                });

        this.upwin = new Wtf.Window(
                {
                    id: 'upfilewin',
                    title: WtfGlobal
                            .getLocaleText("acc.invoiceList.uploadfile"),
                    closable: true,
                    width: 450,
                    height: 120,
                    plain: true,
                    iconCls: 'iconwin',
                    resizable: false,
                    layout: 'fit',
                    scope: this,
                    listeners: {
                        scope: this,
                        close: function () {
                            scope: this;
                            this.fileuploadwin.destroy();
                        }
                    },
                    items: this.fileuploadwin,
                    buttons: [
                        {
                            anchor: '90%',
                            id: 'UploadDoc',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                            scope: this,
                            handler: function(){
                                if (this.fileuploadwin.form.isValid()) {
                                    Wtf.getCmp('UploadDoc').disabled = true;
                                }
                                if (this.fileuploadwin.form.isValid()) {
                                    this.fileuploadwin.form.submit({
                                        scope: this,
                                        failure: function (frm, action) {
                                            this.upwin.close();
                                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                                        },
                                        success: function (frm, action) {
                                            var mainArray = [];
                                            var tmpJson = {};
                                            this.upwin.close();
                                            var resObj = eval("(" + action.response.responseText + ")");
                                            if (resObj.name.lastIndexOf(".") != -1) {
                                                tmpJson.id = resObj.docid + resObj.name.substr(resObj.name.lastIndexOf("."));
                                            } else {
                                                tmpJson.id = resObj.docid;
                                            }
                                            tmpJson.name=resObj.name;
                                            this.attach.push(tmpJson);
                                            mainArray.push(tmpJson);
                                            this.onSuccessAttached(mainArray, true);
                                        }
                                    })
                                }
                            }
                        },
                        {
                            anchor: '90%',
                            id: 'closeUploadDoc',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
                            handler: function(){
                                this.upwin.close();
                            },
                            scope: this
                        }]

                });
                    var billid = "";
        if(this.recArr.data != undefined){
            if (this.recArr.data.billid != null & this.recArr.data.billid != "") {
                billid = this.recArr.data.billid;
            }
        } else {//this is executed in cases of sales invoice as we are sending array of object  
            if (this.recArr[0].data.billid != null & this.recArr[0].data.billid!="") {
                billid = this.recArr[0].data.billid;
        }}
    
        this.sendInvoiceId.setValue(billid);
        this.sendModuleidId.setValue(this.moduleid);
        this.upwin.show();
    },
    onSuccessAttached: function (fileInfo, isNewFile) {
        for (var i = 0; i < fileInfo.length; i++) {
            var pid = 'fileattach_' + (isNewFile ? "new_" : "old_") + this.count;
            this.count++;
            var lp = new Wtf.linkPanel({
                id: pid,
                border: false,
                text: WtfGlobal.getLocaleText("acc.field.Remove"), //"Remove",
                guid : fileInfo[i].id,
                nameForDisplay: fileInfo[i].name
            });
            this.attachPanel.insert(this.count, lp);
            lp.on("linkClicked", this.removeFile, this);
            this.attachheight = this.attachheight + 20;
            this.attachPanel.setHeight(this.attachheight);
        }
        this.doLayout();
    },
    removeFile: function (linkDom, linkPanel) {
        var attachmentarr=this.attach;
        var temparr=[];
        for(var i=0;i<attachmentarr.length;i++){
            if(attachmentarr[i].id!=linkPanel.guid){
                temparr.push(attachmentarr[i]);
            }
        }
        this.attach=temparr;
        this.attachPanel.remove(linkPanel, true);
        this.attachheight -= 20;
        this.attachPanel.setHeight(this.attachheight);
//        this.count--;
        this.doLayout();
    },
    getToMailString : function(){
        if(this.mode != 18){
            var recordArray = this.recArr;
            var toMailString = "";
             var record="";
            if(recordArray[0]){
                record = recordArray[0];
                if(record.data.personemail!=undefined && record.data.personemail!="" && record.data.personemail!=null){
                    toMailString+=record.data.personemail.replace(/,/g, ";");
                }
            }else{
                record=recordArray;
                    if(record.data.billingEmail!=undefined && record.data.billingEmail!="" && record.data.billingEmail!=null){
                        toMailString+=record.data.billingEmail.replace(/,/g, ";");
                    }
                }
        
            return toMailString;
        }else{
            return '';
        }
    },
    getInvoiceIdsString : function(){
        var recordArray = this.recArr;
        var retStr = "";
        for(var i = 0; i<recordArray.length; i++){
            var record = recordArray[i];
            if(record.data.billid!=undefined && record.data.billid!="" && record.data.billid!=null){
                retStr+='"'+record.data.billid+'",';
            }
        }
        retStr =  retStr.substr(0,retStr.length-1);
        return retStr;
    },
    saveData:function(){ 
        if(!this.Form.getForm().isValid())
            WtfComMsgBox(2,2);
        else{
            var rec=this.Form.getForm().getValues();
            callReconciliationLedger(rec)
            this.close();
        }
    },
    isSalesSideDocMail: function(moduleId) {
        var isSalesSide = false;
        switch (moduleId) {
            case Wtf.Acc_Customer_Quotation_ModuleId:
            case Wtf.Acc_Sales_Order_ModuleId:
            case Wtf.Acc_Invoice_ModuleId:
            case Wtf.Acc_Delivery_Order_ModuleId:
            case Wtf.Acc_Sales_Return_ModuleId:
            case Wtf.Acc_Receive_Payment_ModuleId:
                  isSalesSide = true;
                   break;
        }
        return isSalesSide;
    },
    handleClose:function(){
        this.fireEvent('cancel',this)
        this.close();
    },
    
    handleSend:function(bobj,edfd){
        this.sendBtn.disable();
        this.closeBtn.disable();

        if(!(this.To.isValid())||this.To.getValue().trim()=="" && !this.showEmailGrid){          
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")],2);
                this.sendBtn.enable();
                this.closeBtn.enable();
                return ;
        }
        if(Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink){
            var moduleid = this.moduleid?this.moduleid:"";
            var allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                ) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
            if(allowDDTemplate && this.templateCombo && ( this.templateCombo.getValue()== undefined || this.templateCombo.getValue()== "" )){
                this.templateCombo.markInvalid();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2 ); 
                this.sendBtn.enable();
                this.closeBtn.enable();
                return;
            }
        }

        if(this.Subject.getValue().trim()==""){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.MAilWin.msg18"),
                function(btn){
                  if (btn == "yes") {
                      if(this.moduleid == Wtf.Acc_Make_Payment_ModuleId){
                          this.sendMailnotification();
                      }else{
                          this.sendMail();
                      }
                      
                  }
                  else {
                    this.sendBtn.enable();
                    this.closeBtn.enable();
                    return false;
                  }
                }, this);
        } else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId||this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId || 
                this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_Goods_Receipt_ModuleId ||
                this.moduleid==Wtf.Acc_Sales_Return_ModuleId || this.moduleid==Wtf.Acc_Purchase_Return_ModuleId ||
                this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){
              this.sendMailnotification();
        }else{ 
            this.sendMail();
        } 
           
    },
    sendMailnotification:function(){
        var email="";
         if(this.sendCopy.checked == true){
            if(this.userMailid != undefined  )
                email+=this.userMailid+";";
            else
                email+=this.emailid+";";
        }
        email+=this.To.getValue().trim();
//        var recordArray = this.recArr.data;
       // var rec="";
        var billids="";
        
        var recordArray;
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            if( this.recArr.length!=undefined){
            for (var i = 0; i < this.recArr.length; i++) {
                billids = billids + this.recArr[i].data.billid + ",";
            }
            if (billids != '') {
                billids = billids.substr(0, billids.lastIndexOf(","));
            }
            recordArray = this.recArr[0].data;
        }
        else{
                recordArray = this.recArr.data;
                billids=recordArray.billid;
            }
        }
        else {
            recordArray = this.recArr.data;
        }
       
        var entryno=recordArray.billno;
        var msg = this.Message.getValue();
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        var rec=this.Form.getForm().getValues();
        /*
         * ERP-32958 
         * Fetching Template Name from template combo store.
         */
        var templateRec; 
        var templateName = ""; 
        if (this.templateCombo) {
           templateRec = WtfGlobal.searchRecord(this.templateCombo.store,this.templateCombo.getValue(),"templateid");
        }
        if (templateRec && templateRec.data) {
            templateName = templateRec.data.templatename;
        }
        rec.mode=this.mode;
        rec.billid=recordArray.billid;
        rec.billids = billids;
        /*
         * Create json array if invoices are no email id
        */
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId && this.withOutEmailStore != undefined && this.withOutEmailStore.totalLength > 0) {
            var jarray = this.getInvoicesWithEmailIDs();
            if (jarray.length > 0) {
                rec.billidswithemail = jarray;
            }
        }
        rec.sendCopyChecked = this.sendCopy.checked;
        rec.isConsignment=(recordArray.isConsignment!=null &&recordArray.isConsignment!=undefined)?recordArray.isConsignment:false ;
        rec.isLetterHead=false;
        rec.billno=entryno;
        rec.mailingDate=WtfGlobal.convertToGenericDate(new Date());
        rec.emailid=email;
        rec.amount=recordArray.amount;
        rec.currencyid=recordArray.currencyid;
        rec.personid=recordArray.personid;
        rec.accname=recordArray.personname?recordArray.personname:"";
        rec.sendpdf=this.tax1099?false:this.sendpdf.getValue();
        rec.attachmentSelection='';
        rec.isexpenseinv=recordArray.isexpenseinv;
        rec.entryno=entryno;
        rec.templateName = templateName;
        rec.bills=recordArray.billid;     //For Default Export Template of Payment Voucher
        rec.templateflag=Wtf.templateflag;  //For Default Export Template of Payment Voucher
        rec.attachments=JSON.stringify(this.attach);
        rec.moduleid=this.moduleid;
        if(recordArray.isadvancepayment!=undefined)
        {        
            rec.isadvancepayment=recordArray.isadvancepayment;
        }
        if(recordArray.advanceUsed!=undefined)
        {
            rec.advanceUsed=recordArray.advanceUsed;
        }
        if(recordArray.advanceid!=undefined){
            var advanceFlagVar=false;
            if(recordArray.advanceid!="")
                advanceFlagVar=true;
            rec.advanceFlag=advanceFlagVar;    
        }
        if(recordArray.advanceamount!=undefined)
        {
            rec.advanceAmount=recordArray.advanceamount;
        }
        
        //arr.push(rec);
   // }
    
        if(this.moduleid == Wtf.Acc_Invoice_ModuleId){
            rec.fieldid=Wtf.Email_Button_From_Report;
            rec.mode=2
            WtfGlobal.setAjaxTimeOutFor30Minutes();
            Wtf.Ajax.requestEx({
                url:"CommonFunctions/sendInvoicesonMail.do",
                params:rec
              
            },this,this.success,this.failure);
    
        }else  {
            Wtf.Ajax.requestEx({
                url:"CommonFunctions/sendMail.do",
                params: rec
            },this,this.success,this.failure);
        }
    },
    getInvoicesWithEmailIDs: function() {
        var arr = [];
        this.withOutEmailStore.each(function(rec) {
            if (rec.data.email != "") {
                arr.push(this.withOutEmailStore.indexOf(rec));
            }
        }, this)
         return WtfGlobal.getJSONArray(this.noEmailPresentGrid, true, arr);
         
    },
    sendMail:function(){
        var email="";
        if(this.sendCopy.checked == true){
            if(this.userdata != undefined && this.userdata.emailid != undefined)
                email+=this.userdata.emailid+";";
            else
                email+=this.emailid+";";
        }

        email+=this.To.getValue().trim();

        var msg = this.Message.getValue();
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        var rec=this.Form.getForm().getValues();
        rec.mode=this.mode;
//        rec.billid=this.data.billid;
        rec.mailingDate=WtfGlobal.convertToGenericDate(new Date());
        rec.emailid=email;
        rec.multiRecordJobj = (this.recArr).toString();
        rec.config = this.configstr;
        rec.filename = this.fileName;
        rec.filetype = this.filetype;
        rec.get = this.get;
        rec.gridconfig = this.gridConfig;
        rec.deleted = this.deleted;
        rec.nondeleted = this.nondeleted;
        rec.invoiceIds =this.mode!=18 ? this.getInvoiceIdsString() : "";
        if(this.recArr.data!=undefined && this.recArr.data!=null && this.recArr.data!="")
            rec.isexpenseinv=this.recArr.data.isexpenseinv;
//        rec.amount=this.data.amount;
//        rec.currencyid=this.data.currencyid;
//        rec.personid=this.data.personid
        rec.sendpdf=this.sendpdf.getValue();
        rec.multiRecordMailFlag = true;
        rec.attachmentSelection='';
        rec.attachments=JSON.stringify(this.attach);
        //mode 18 is for mail with attachment for statement of accounts report.
        if(this.mode == 18){
            rec.stdate=this.filterParams.stdate;
            rec.interval=this.filterParams.interval;
            rec.statementOfAccountsFlag=this.filterParams.statementOfAccountsFlag,
            rec.vendorIds=this.filterParams.vendorIds;
            rec.customerIds=this.filterParams.customerIds;
            rec.isCustomerSales=this.filterParams.isCustomerSales;
            rec.name=this.filterParams.name;
            rec.withoutinventory=this.filterParams.withoutinventory;
            rec.custVendorID=this.filterParams.custVendorID;
            rec.datefilter=this.filterParams.datefilter;
            rec.ignorezero=this.filterParams.ignorezero;
            rec.isdistributive=this.filterParams.isdistributive;
            rec.isAged=this.filterParams.isAged;
            rec.creditonly=this.filterParams.creditonly;
            rec.mode=this.filterParams.mode;
            rec.nondeleted=this.filterParams.nondeleted;
            rec.withinventory=this.filterParams.withinventory;
            rec.reportWithoutAging=this.filterParams.reportWithoutAging;
            rec.startdate=this.filterParams.startdate;
            rec.curdate=this.filterParams.curdate;
            rec.asofdate=this.filterParams.asofdate;
            rec.enddate=this.filterParams.enddate;
            rec.invoiceAmountDueFilter=this.filterParams.invoiceAmountDueFilter;
            rec.duration=this.filterParams.duration;
            rec.moduleid=this.filterParams.moduleid;
            rec.templateflag=0;
            rec.type=0;
        }
        
        var url = ((this.isCustomer || this.mode == 18 )?"ACCInvoiceCMN":"ACCGoodsReceiptCMN")+"/sendMail.do"
        Wtf.Ajax.requestEx({
            url:url,
            params: rec
        },this,this.success,this.failure);
     },
     
     success:function(response){
        if(response.success){
            WtfComMsgBox([this.label,response.msg],3);
//             if(this.tax1099){
//                var rec=[];
//                rec.accid=this.data.personid;
//                rec.taxidmailon=WtfGlobal.convertToGenericDate(new Date());;
//                Wtf.Ajax.requestEx({
//                    url:"ACCVendor/saveVendorMailingDate.do",
//                    params: rec
//                },this,this.mailSuccessResponse,this.mailFailureResponse);
//             }
            if(this.moduleid == Wtf.Acc_Invoice_ModuleId){
                WtfGlobal.resetAjaxTimeOut();
            } else {
                this.fireEvent('loadReportView',this);
            }
           this.handleClose();
         } else {
             if(response.msg && response.isMsgSizeException){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);        	 
             }else{
                 WtfComMsgBox([this.label,WtfGlobal.getLocaleText("acc.rem.210")],3);
             }
             this.sendBtn.enable();
             this.closeBtn.enable();
         }
    },
    
    failure:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.handleClose();
    },
    mailSuccessResponse:function(response){
        this.close();
    },
    mailFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
    
//    showCheckBox:function(){
//        for(var i=0;i<this.documentStore.data.length;i++)
//        {
////            var docName=this.documentStore.data.items[i].data.docname;
//            var docName='DocumentName.pdf';
////            var docID=this.documentStore.data.items[i].data.docid;
//            var docID='multirecorddocid';
//            var fileExt = docName.substr(docName.lastIndexOf("."));
//            var selNames =docID + fileExt;
//            this.attachPanel.add({
//                layout : 'table',
//                border : false,
//                items : [new Wtf.form.Checkbox({
////                    id:"Attachment_"+this.documentStore.data.items[i].data.docid,
//                    id:"Attachment_"+'multirecorddocid',
////                    name:this.documentStore.data.items[i].data.docid,   
//                    name:'Multi Record Doc',   
//                    checked:true,
//                    width: 20
//
//                }), {
//                    border : false,
//                    bodyStyle : 'padding-left:5px;',                                    
//                    html : "<a href='#' style='color:#083772;'  title='"+WtfGlobal.getLocaleText("acc.invoiceList.downloaddocument")+"' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+docName+"\")'>"+docName+"</a>"
//                }]
//
//            })
//        }
//        this.attachPanel.doLayout();
//        
//    },
//    getAttachmentSelection:function(){
//        var storelength=this.documentStore.data.length;              
//        var attachmentSelectionArray=[];
//        for(var i=0;i<storelength;i++)
//        {
//            var cmponent=Wtf.getCmp("Attachment_"+this.documentStore.data.items[i].data.docid);            
//            if(cmponent.checked){
//                attachmentSelectionArray.push(cmponent.name);
//            }
//        }
//        var cmponent=Wtf.getCmp("Attachment_"+'multirecorddocid');            
//        if(cmponent.checked){
//            attachmentSelectionArray.push(cmponent.name);
//        }
//        return attachmentSelectionArray.toString();
//    }
});

