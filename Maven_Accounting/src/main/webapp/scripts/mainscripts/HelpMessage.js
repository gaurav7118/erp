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
Wtf.HelpWindow = function(config) {
    Wtf.apply(this,{
        buttonAlign :'right',
        buttons: [this.prevBtn=new Wtf.Button({
            text: WtfGlobal.getlocaleText("acc.common.backBtn"),
            disabled:true,
            handler: function(){
                this.current--;
                if(this.current<=0){
                    this.prevBtn.setDisabled(true);
                    this.current=0;
                }
                if(this.current<this.links.length-1){
                    this.nextBtn.setDisabled(false);
                }
                this.displayMessage(this.links[this.current],false);
            },
            scope:this
        }),this.nextBtn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.next"),
            disabled:true,
            handler: function(){
                this.current++;
                if(this.current>=this.links.length-1){
                    this.nextBtn.setDisabled(true);
                    this.current=this.links.length-1;
                }
                if(this.current>0){
                    this.prevBtn.setDisabled(false);
                }
                this.displayMessage(this.links[this.current],false);
            },
            scope:this
        }),{
            text: WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),
            handler: function(){
                this.close();
            },
            scope:this
        }]
    }, config);
    
    Wtf.HelpWindow.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.HelpWindow, Wtf.Window, {
    width:600,
    height:500,
    initComponent: function(config) {
        Wtf.HelpWindow.superclass.initComponent.call(this,config);
    },
    iconCls: "favwinIcon",
    onRender: function(config) {
        Wtf.HelpWindow.superclass.onRender.call(this, config);
                
        this.messagePanel = new Wtf.Panel({
            waitMsgTarget: true,
            border : false,
            bodyStyle : 'padding:10px 20px;',
            autoScroll:true,
            lableWidth :70,
            listeners:{
                scope:this,
                render:function(){
                    this.links=[];
                    this.current=-1;
                    this.displayMessage(this.helpLink,true);
                }
            }
        });

        this.templatePanel= new Wtf.Panel({
            frame:true,
            border: false,
            layout:'fit',
            autoScroll:true,
            items:[{
                border:false,
                region:'center',
                layout:"border",
                items:[{
                    border:false,
                    region:'center',
                    baseCls:'bckgroundcolor',
                    layout:"fit",
                    items:this.messagePanel
                }]
            }]
        });
        this.add(this.templatePanel);        
    },

    displayMessage:function(link,isNew){
        if(isNew){
            this.current++;
            this.links.splice(this.current,this.links.length-this.current,link);
            this.nextBtn.setDisabled(true);
            if(this.links.length>20){
                this.links.shift();
                this.current--;
            }
            if(this.prevBtn.disabled==true&&this.current>0)
                this.prevBtn.setDisabled(false);
        }
        var helpMsg=eval("(Wtf.HelpMessages."+link+")");
        if(helpMsg.width&&helpMsg.height&&!isNaN(helpMsg.width)&&!isNaN(helpMsg.height))
        this.setSize(helpMsg.width, helpMsg.height);
        this.tpl.overwrite(this.messagePanel.body,helpMsg);
    }
});

function jumpToHelp(helpLink){
    Wtf.getCmp('acc-help-message-window').displayMessage(helpLink,true);
}

Wtf.HelpButton = function(config){
    Wtf.HelpButton.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.HelpButton, Wtf.Button,{
    text:WtfGlobal.getLocaleText("acc.dashboard.help"),
    tooltip: WtfGlobal.getLocaleText("acc.common.click"),
    iconCls: 'help',
    handler:function(){
               var tpl=new Wtf.XTemplate(
               '<div class="helptitle">{title}</div>',
               '<div class="helpsubtitle">{subTitle}</div>',
                '<div class="helpdata">',
                '<tpl if="numbered">',
                '<ol class="number">',
                '</tpl>',
                '<tpl if="!numbered">',
                '<ol class="bullet">',
                '</tpl>',
                '<tpl for="data">',
                '<li>{.}</li>',
                '</tpl>',
                '</ol></div></font>'
                );
        this.viewhelp=new Wtf.HelpWindow({
            id:'acc-help-message-window',
            modal:true,
            title:WtfGlobal.getLocaleText("acc.dashboard.help"),
            iconCls: 'help',
            resizable:false,
            layout:'fit',
            autoScroll:true,
            message:tpl,
            tpl:tpl,
            helpLink:this.helpLink
        });
        this.viewhelp.show();
    }
});

Wtf.HelpMessages = {
     creditNote:{
        title:WtfGlobal.getLocaleText("acc.field.RecordaPaymentfromacustomerinresponsetoaninvoice"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:false,
        width:600,
        height:400,
        data:[
             'If you receive a payment from a customer in response to an invoice, follow these steps.<ul><li>1.Select invoice from amongst the list of outstanding invoices and click receive payment.</li><li>2.Receive Payments page opens.</li><li>3.(Optional) Change the <b>Date</b>.</li><li>4.Enter the payment <b>Amount</b>.</li><li>5.(Optional) Select a payment method in the <span class="helplink" onClick="jumpToHelp(\'paymentMethod\')">Pmt Method</span> field.</li><li>6.(Optional)Enter a <b>Memo</b>.The Memo appears on the customer\'s statement, if you send statements.</li><li>7.Enter amount received.System auto calculates balance remaining.</li><li>8.Click <b>save</b></li></ul>'
          ]
    },

    paymentMethod:{
        title:WtfGlobal.getLocaleText("acc.pmList.gridPaymentMethod"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:false,
        width:600,
        height:400,
        data:[
              '<b>Payment methods</b>',
              'If you indicate the payment method (such as <b>Cash</b> or <b>Check</b>) on each payment, you make it easier to record those payments later.<br>',
              'For example, in Receive Payments, you might indicate that a payment from a customer was a check. Or, in Sales Receipt, you might select Cash for its payment method.',
              '<b>Adding apayment method</b>,',
              'For a new company,the <b>Cash</b>,<b>Check</b> and major credit card payment methods are created automatically for you but you can create a different payment method for other types of payment.<ul style="margin-left:20px;"><li>1.Click add new payment in the Payment drop down.</li><li>2.Enter a name for the payment method.</li><li>3.<b>Save</b> the payment method.</li></ul>'
        ]
    },

    journalEntry:{
        title:WtfGlobal.getLocaleText("acc.field.MakingaJournalEntry"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:true,
        width:600,
        height:400,
        data:[
            '<b>Journal Date:</b> Enter the date the transaction took place.',
            '<b>Journal Entry Number:</b> Helps you identify the journal entry transaction in journal entry reports.',
            'Enter the first distribution line:Debit or Credit. Often the first line is Debit.',
            '<b>Select account:</b> Account selected is any account present in the chart of accounts. If account is not present, Click <span class="helplink" onClick="jumpToHelp(\'addAcc\')">Add</span> new account in the drop down. New account window appears. Enter account information. Click <b>Save</b>.',
            '<b>Amount:</b> Enter amount on the debit side or the credit side.',
            'Continue to enter distribution lines until the sum of the <b>Debit</b> column entries equals the sum of the <b>Credit</b> column entries.',
            'To help you validate the data you enter, totals for the Debit and Credit column entries appear after the last distribution line.',
            '(Optional) Enter a <b>Memo</b> for the Journal Entry transaction. This memo appears on Journal Entry reports showing transaction summaries.',
            'Click <b>Save</b>.'
        ]
    },
    coa:{
        title:WtfGlobal.getLocaleText("acc.coa.tabTitle "),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:true,
        width:600,
        height:400,
        data:[
            'Chart of Accounts displays all your accounts.Balance sheet accounts track your assets and liabilities, and income and expense accounts categorize your transactions. You will add new accounts as your business grows and changes. For example, you may need to add one or more of the following:<br><ul style="list-style-type:circle;margin-left:20px;"><li>Income accounts to track new sources of income.</li><li>Expense accounts to track new types of expenses.</li><li>Bank accounts when you open new checking or savings accounts at your local bank and for all bank related transactions.</li><li>Other kinds of balance sheet accounts to track specific assets, liabilities, or equity.</li></ul>',
            'Add a<span class="helplink" onClick="jumpToHelp(\'addAcc\')"><b> new account or subaccount</b></span>',
            '<span class="helplink" onClick="jumpToHelp(\'editAcc\')"><b> Edit account or subaccount</b></span>,description, type or parent From here, you can edit accounts.',
            '<span class="helplink" onClick="jumpToHelp(\'deleteAcc\')"><b> Deleting</b></span> an account removes it from the chart of accounts and from other places in the system where you can choose accounts. If the account contains transactions, they remain part of your company data; you can find them through reports. However deleting an account is not a preferable option.'
       ]
    },
    addAcc:{
        title:WtfGlobal.getLocaleText("acc.field.AddinganAccountorSubaccount"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:true,
        width:600,
        height:400,
        data:[
             'Click <b>New Account</b> in the lower right corner. Fill in the information in the new account window.',
             'Check the <b>Is sub account</b> checkbox. Once you have assigned an account as a subaccount. Choose the parent of the subaccount. The parent account must be of the same type. For example, if the subaccount is an expense account, the parent must be an expense account.'
        ]
    },
    editAcc:{
        title:WtfGlobal.getLocaleText("acc.field.EditinganAccountorSubaccount"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:true,
        width:600,
        height:400,
        data:[
             'Select the account you want to edit.',
             'Click Edit in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
    deleteAcc:{
        title:WtfGlobal.getLocaleText("acc.field.DeletinganAccountorSubaccount"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:true,
        width:600,
        height:400,
        data:[
               'Select the account and click Delete...in the bottom right.',
               'Click OK to confirm that you want to delete the account.'
           ]
    },
    createInvoice:{
        title:WtfGlobal.getLocaleText("acc.field.AddNewInvoice"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'If you receive payment from customer later, fill an <b>invoice</b> to oblige your customer to pay you.',
            'Select the customer to whom you are selling products orservices. If no customer is present click <b>add new customer</b> in the drop down.Enter customer information in the new customer window.Click <b>Save</b>.',
            'Select Credit terms: <span class="helplink" onClick="jumpToHelp(\'creditTerm\')">Credit Terms</span> indicate when you expect to receive payment from a customer or when your vendors expect payment from you.For example, "Due upon receipt" means payment is due immediately. Termsare added to the Invoice Date or BillDate to automatically calculate the Due Date on an invoice.',
            'You define the products and servicesyour business provides to:<ul style="list-style-type:circle;margin-left:20px;"><li>Quickly and consistently use product descriptions and their rates.</li><li>Categorize sales of products and services in different income accounts.</li></ul>',
            'If you don\'t use products and services, you won\'t be able totrack the things you sell in different income accounts.<br>',
            'To create your products and services, choose Products &Services List from the Customers menu or else <b>add new product</b> in the drop down.Enter new product information and click <b>Save</b>.<br>',
            '<b>Quantity and Rate on product charge lines</b><br>',
            'When you enter charge lines, you canspecify a quantity and a rate for products and services. If you use productsand services, the description and the rate (the cost per unit) are prefilled.<br>',
            'Entering quantity and rate lets you:<ul style="list-style-type:circle;margin-left:20px;"><li>Avoid doing the math for the amount of a line.</li><li>Track how many things you sold or did, if you use products and services.</li></ul><br>',
            '<b>Discounts offered:</b>On each transaction, you can entera percentage discount or a fixed amount.<br>',
            '<b>Ship products:</b>This adds a Ship To destination, a Ship Date, a place to track the carrier name and tracking number, and a linefor shipping charges.<br>'
        ]
    },
    creditTerm:{
        title:WtfGlobal.getLocaleText("acc.field.TermNames"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'When you create a new company, a list of frequently used terms arepredefined in the Terms List.',
            'The terms include:<ul style="list-style-type:circle;margin-left:20px;"><li>Due on receipt - Payment is due immediately.</li><li>Net 15 - Payment is due in 15 days.</li><li>Net 30 - Payment is due in 30 days.</li><li>Net 60 - Payment is due in 60 days.</li></ul>',
            'When a term name contains the word "Net," it indicates when thepayment is due. For example, if you expect payment from a customer within 30days of the Invoice Date, the terms are named <b>Net 30</b>. These terms are all based on payment due in a fixed number of days.<br>',
            '<b>Using terms</b>',
            'To apply terms to a particular transaction, select the termsyou want from the Terms field on Create Invoice or Enter Bills.Â  When you select terms on an invoice or bill, the Due Date automaticallychanges to reflect the terms you chose.',
            'Invoice Date: Bydefault, the Invoice Date or Bill Dateis the date you are creating the invoice or bill.<br>',
            'Default terms are set in the system for all invoices.<br>',
            'Here is how you know which terms arein effect:<ul style="list-style-type:circle;margin-left:20px;"><li>If the customer has no Terms setting, the Default Invoice Terms are used by default.</li><li>If neither of the above are set, the Terms field is blank by default. This means that the Due Date is set to match the Invoice Date.</li></ul>'
        ]
    },
    companyPreference:{
        title:WtfGlobal.getLocaleText("acc.field.CompanyPreference"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:false,
        width:600,
        height:400,
        data:[
             '<font color="blue">Do you wantautomatic or custom transaction numbers?</font><br>',
             'Transaction numbers help you and your customersidentify transactions. One sequence of transaction numbers appears on invoices,sales receipts, and credit memos. Anothersequence of transaction numbers appears on charges and credits. Estimates also have their own set oftransaction numbers.<br>',
             'Krawler Accounting system lets you <span class="helplink" onClick="jumpToHelp(\'custTransNumber\')">customize</span> your transaction numbers.'
        ]
    },
    custTransNumber:{
        title:WtfGlobal.getLocaleText("acc.field.CustomerorAutomaticTransactionNumbers"),
        numbered:false,
        width:600,
        height:400,
        data:[
             'When <b>Custom transaction numbers</b> is on:<br>',
             '<ul style="list-style-type:circle;margin-left:20px"><li>You can change transaction numbers when you create or modify a transaction.</li><li>Transaction numbers can have letters (such as KWL101).</li><li>System helps prevent duplicate numbers by warning you if you try to enter a duplicate.</li></ul><br>',
             'When <b>Custom transaction numbers</b> is off: Transaction numbers are assigned whentransactions are created.<br>',
             '<b>To turn Custom transaction numbers on or off</b><ul margin-left:20px;"><li>1.Open CompanyPreferences.(Or from the Settings tab, click Preferences.)</li><li> 2.Leave blank <b>fields</b> present in <b>Automatic Number Generation</b>.Fields are cleared toturn <b>Custom transaction numbers</b> off.</li><li>3.Click <b>Save</b>.</li></ul><br>',
             'If you\'re happy with simple, automatically-assigned numbers (such as 1, 2, 3, and so on) for invoices and other transaction forms, you don\'t need custom numbers.Stick with Automatic. Automatic Transaction numbers are sequential. New transactions get new numbers (such as KWL102), based on the last number.<br>',
             'You can set the starting number to be something other than 1 by entering the number;you want to start with for your first transaction in the Automatic Number Generation field.',
             'After you save the transaction, set the numbers back to the custom by clearing the Automatic Number Generation field in Company Preferences.'
        ]
    },
    productDetail:{title:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'Product Details displays all your product available list.In this it will show Product Description,Reorder Quantity,Available Quantity,Purchase Prize,Sales Prize. You will add new Products as your business grows and changes. For example, you may need to add one or more of the following:<br><ul style="list-style-type:circle;margin-left:20px;"><li>Parent Product.</li><li>Sub Product.</li></ul>',
             'Add a<span class="helplink" onClick="jumpToHelp(\'addProduct\')"><b> new Product or subProduct</b></span>',
            '<span class="helplink" onClick="jumpToHelp(\'editProduct\')"><b> Edit Product or subProduct</b></span>,description, type or parent From here, you can edit Product.',
            '<span class="helplink" onClick="jumpToHelp(\'deleteProduct\')"><b> Deleting</b></span> an Product removes it from the Product List and from other places in the system where you can choose product. If the Product contains transactions, they remain part of your company data; you can find them through reports. However deleting an Product is not a preferable option.'
        ]
    },
    addProduct:{title:WtfGlobal.getLocaleText("acc.field.AddNewProducts"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
            '<b>Product Name:</b>Enter the product or subProduct Name.',
            'Indicate whether this is a Parent-product or sub-Product by clicking Is Sub product check box.',
            'Select the "<b>Parent</b>" of the product or service you\'re creating.',
            '<b>Description:</b>Enter  product Description.',
            'Include the product units,Product reorder quantity, reorder level.',
            'Enter a Rate, the price of one unit.  If the product rate typically varies when you enter a charge, enter the new price of product in the product price list.',
            'Enter an income account to associate with the product or service. When you use this product or service, the amount in the income account increases. ',
            'Click <b>Save</b>.'
        ]
    },
    editProduct:{title:WtfGlobal.getLocaleText("acc.field.EditProducts"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
             '<b>Select</b> the Product you want to edit.',
             'Click Edit in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
     deleteProduct:{title:WtfGlobal.getLocaleText("acc.field.DeleteProducts"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
              '<b>Select</b> the Product from Product list and click Delete...in the bottom right.',
              'Click <b>OK</b> to confirm that you want to delete the Product.'
        ]
    },
    vendorDetails:{title:WtfGlobal.getLocaleText("acc.field.VendorsDetails"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'Vendor Details displays all your Vendors available list. In this it will show Vendors Description like name,address,contact number etc. You will add new Vendor as your business grows and changes. You can add one or more vendor of the following type:<br><ul style="list-style-type:circle;margin-left:20px;"><li>Parent Vendor.</li><li>Sub Vendor.</li></ul>',
            'Add a<span class="helplink" onClick="jumpToHelp(\'addVendor\')"><b> new Vendor or subVendor</b></span>',
            '<span class="helplink" onClick="jumpToHelp(\'editVendor\')"><b> Edit Vendor or subVendor</b></span>,it\'s description, type or parent From here, you can edit Vendor.',
            '<span class="helplink" onClick="jumpToHelp(\'deleteVendor\')"><b> Deleting</b></span> an Vendor removes it from the Vendor List and from other places in the system where you can choose it.'
        ]
    },
    addVendor:{title:WtfGlobal.getLocaleText("acc.field.AddNewVendororSubVendor"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
            '<b>Title:</b> Select the Title from the combo if you want new one then select Add new',
            '<b>Vendor Name:</b> Enter the Vendor or subVendor Name.',
            'Indicate whether this is a Parent-Vendor or sub-Vendor by clicking Is Sub Vendor check box.',
            'Select the "<b>Parent</b>" of the Vendor you\'re creating as Sub-Vendor.',
            'Enter a Address,Email,Contact Number,Alternate Phone number,Fax number,Shipping Address.',
            '<b>Delivery Mode:</b> Select the delivery Mode.If you want another one select Add new ',
            'Enter a Other detail if required,Bank Account number.',
            '<b>Debit term:</b> Select the Debit Term.If you want another one select Add new ',
            'Click <b>Save</b>.'
        ]
    },
    editVendor:{title:WtfGlobal.getLocaleText("acc.field.EditCustomerorSubCustomer"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
             '<b>Select</b> the Customer you want to edit.',
             'Click Edit in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
     deleteVendor:{title:WtfGlobal.getLocaleText("acc.field.DeleteCustomerorSubCustomer"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
              '<b>Select</b> the Customer from Customer List and click Delete...in the bottom right.',
              'Click <b>OK</b> to confirm that you want to delete the Vendor.'
        ]
    },
    customerDetails:{title:WtfGlobal.getLocaleText("acc.field.CustomersList"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'Customer List displays all your customers available list. In this it will show Customers Description like name,address,email,contact number etc. You will add new Customer as your business grows and changes. You can add one or more Customer of the following type:<br><ul style="list-style-type:circle;margin-left:20px;"><li>Parent Customer.</li><li>Sub Customer.</li></ul>',
            'Add a<span class="helplink" onClick="jumpToHelp(\'addCustomer\')"><b> new Customer or subCustomer</b></span>',
            '<span class="helplink" onClick="jumpToHelp(\'editCustomer\')"><b> Edit Customer or subCustomer</b></span>,it\'s description, type or parent From here, you can edit Customer.',
            '<span class="helplink" onClick="jumpToHelp(\'deleteCustomer\')"><b> Deleting</b></span> an Customer removes it from the Customer List and from other places in the system where you can choose it.'
        ]
    },
    addCustomer:{title:WtfGlobal.getLocaleText("acc.field.AddNewCustomerorSubCustomer"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
            '<b>Title:</b> Select the Title from the combo if you want new one then select Add new',
            '<b>Customer Name:</b> Enter the Customer or subCustomer Name.',
            'Indicate whether this is a Parent-Customer or sub-Customer by clicking Is Sub Customer check box.',
            'Select the "<b>Parent</b>" of the Customer you\'re creating as Sub-Customer.',
            'Enter a Address,Email,Contact Number,Alternate Phone number,Fax number.',
            '<b>Delivery Mode:</b> Select the delivery Mode.If you want another one select Add new ',
            'Enter a Other detail if required,Bank Account number.',
            '<b>Credit Term:</b> Select the Credit Term.If you want another one select Add new ',
            'Click <b>Save</b>.'
        ]
    },
    editCustomer:{title:WtfGlobal.getLocaleText("acc.field.EditCustomerorSubCustomer"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
             '<b>Select</b> the Customer you want to edit.',
             'Click Edit in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
    deleteCustomer:{title:WtfGlobal.getLocaleText("acc.field.DeleteCustomerorSubCustomer"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
              '<b>Select</b> the Vendor from Customer details and click Delete...in the bottom right.',
              'Click <b>OK</b> to confirm that you want to delete the Customer.'
        ]
    },
    masterConfig:{title:WtfGlobal.getLocaleText("acc.field.MasterConfiguration"),
        numbered:false,
        width:600,
        height:400,
        data:[
            'Master Configuration displays all your Master Groups and Master Item available list. You can <span class="helplink" onClick="jumpToHelp(\'editMasterGroup\')"><b> Edit</b></span> Master Groups',
            'You can also Add a<span class="helplink" onClick="jumpToHelp(\'addMasterItem\')"><b> new Master Item</b></span>',
            'You can also<span class="helplink" onClick="jumpToHelp(\'editMasterItem\')"><b> Edit Master Item</b></span>.',
            'You can also<span class="helplink" onClick="jumpToHelp(\'deleteMasterItem\')"><b> Delete Master Item</b></span>.'
        ]
    },
    editMasterGroup:{title:WtfGlobal.getLocaleText("acc.field.EditMasterGroup"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
             '<b>Select</b> the Master Group you want to edit.',
             'Click on Edit Master Group in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
    addMasterItem:{title:WtfGlobal.getLocaleText("acc.field.AddNewMasterItem"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
            'Enter the Master Item Name.',
            'Click <b>Save</b>.'
        ]
    },
    editMasterItem:{title:WtfGlobal.getLocaleText("acc.field.EditMasterItem"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
             '<b>Select</b> the Master Item you want to edit.',
             'Click on Edit Master Item in the lower right corner.',
             'Make the changes you want.',
             'Cick <b>Save</b>.'
        ]
    },
     deleteMasterItem:{title:WtfGlobal.getLocaleText("acc.field.DeleteMasterItem"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
              '<b>Select</b> the Master Item from Master Item List and click Delete Master Item...in the bottom right.',
              'Click <b>OK</b> to confirm that you want to delete the Master Item.'
        ]
    },
    journalEntryReport:{title:WtfGlobal.getLocaleText("acc.field.JiurnalEntryReport"),
        numbered:true,
        subTitle:WtfGlobal.getLocaleText("acc.field.Stepstofollow"),
        width:600,
        height:400,
        data:[
              'Journal Entry Report show the list of available journal entry list. In which it show Entry date,Entry Number and Memo.'
        ]
    }
};



