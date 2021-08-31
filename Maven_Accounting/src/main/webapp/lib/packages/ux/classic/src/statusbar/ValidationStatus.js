Ext.define("Ext.ux.statusbar.ValidationStatus",{extend:"Ext.Component",requires:["Ext.util.MixedCollection"],errorIconCls:"x-status-error",errorListCls:"x-status-error-list",validIconCls:"x-status-valid",showText:"The form has errors (click for details...)",hideText:"Click again to hide the error list",submitText:"Saving...",init:function(B){var A=this;A.statusBar=B;B.on({single:true,scope:A,render:A.onStatusbarRender,beforedestroy:A.destroy});B.on({click:{element:"el",fn:A.onStatusClick,scope:A,buffer:200}})},onStatusbarRender:function(C){var B=this,A=function(){B.monitor=true};B.monitor=true;B.errors=Ext.create("Ext.util.MixedCollection");B.listAlign=(C.statusAlign==="right"?"br-tr?":"bl-tl?");if(B.form){B.formPanel=Ext.getCmp(B.form);B.basicForm=B.formPanel.getForm();B.startMonitoring();B.basicForm.on("beforeaction",function(E,D){if(D.type==="submit"){B.monitor=false}});B.basicForm.on("actioncomplete",A);B.basicForm.on("actionfailed",A)}},startMonitoring:function(){this.basicForm.getFields().each(function(A){A.on("validitychange",this.onFieldValidation,this)},this)},stopMonitoring:function(){this.basicForm.getFields().each(function(A){A.un("validitychange",this.onFieldValidation,this)},this)},onDestroy:function(){this.stopMonitoring();this.statusBar.statusEl.un("click",this.onStatusClick,this);this.callParent(arguments)},onFieldValidation:function(B,C){var A=this,D;if(!A.monitor){return false}D=B.getErrors()[0];if(D){A.errors.add(B.id,{field:B,msg:D})}else{A.errors.removeAtKey(B.id)}this.updateErrorList();if(A.errors.getCount()>0){if(A.statusBar.getText()!==A.showText){A.statusBar.setStatus({text:A.showText,iconCls:A.errorIconCls})}}else{A.statusBar.clearStatus().setIcon(A.validIconCls)}},updateErrorList:function(){var B=this,C,A=B.getMsgEl();if(B.errors.getCount()>0){C=["<ul>"];this.errors.each(function(D){C.push('<li id="x-err-',D.field.id,'"><a href="#">',D.msg,"</a></li>")});C.push("</ul>");A.update(C.join(""))}else{A.update("")}A.setSize("auto","auto")},getMsgEl:function(){var C=this,A=C.msgEl,B;if(!A){A=C.msgEl=Ext.DomHelper.append(Ext.getBody(),{cls:C.errorListCls},true);A.hide();A.on("click",function(D){B=D.getTarget("li",10,true);if(B){Ext.getCmp(B.id.split("x-err-")[1]).focus();C.hideErrors()}},null,{stopEvent:true})}return A},showErrors:function(){var A=this;A.updateErrorList();A.getMsgEl().alignTo(A.statusBar.getEl(),A.listAlign).slideIn("b",{duration:300,easing:"easeOut"});A.statusBar.setText(A.hideText);A.formPanel.body.on("click",A.hideErrors,A,{single:true})},hideErrors:function(){var A=this.getMsgEl();if(A.isVisible()){A.slideOut("b",{duration:300,easing:"easeIn"});this.statusBar.setText(this.showText)}this.formPanel.body.un("click",this.hideErrors,this)},onStatusClick:function(){if(this.getMsgEl().isVisible()){this.hideErrors()}else{if(this.errors.getCount()>0){this.showErrors()}}}})