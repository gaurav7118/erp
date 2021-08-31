Ext.define("Ext.form.field.FileButton",{extend:"Ext.button.Button",alias:"widget.filebutton",childEls:["fileInputEl"],inputCls:Ext.baseCSSPrefix+"form-file-input",cls:Ext.baseCSSPrefix+"form-file-btn",preventDefault:false,tabIndex:undefined,autoEl:{tag:"div",unselectable:"on"},afterTpl:['<input id="{id}-fileInputEl" data-ref="fileInputEl" class="{childElCls} {inputCls}" ','type="file" size="1" name="{inputName}" role="{role}" ','<tpl if="tabIndex != null">tabindex="{tabIndex}"</tpl>',">"],keyHandlers:null,ariaEl:"fileInputEl",getAfterMarkup:function(A){return this.getTpl("afterTpl").apply(A)},getTemplateArgs:function(){var B=this,A;A=B.callParent();A.inputCls=B.inputCls;A.inputName=B.inputName||B.id;A.tabIndex=B.tabIndex||null;A.role=B.ariaRole;return A},afterRender:function(){var A=this;A.callParent(arguments);A.fileInputEl.on({scope:A,change:A.fireChange,focus:A.onFileFocus,blur:A.onFileBlur})},fireChange:function(A){this.fireEvent("change",this,A,this.fileInputEl.dom.value)},createFileInput:function(B){var C=this,A=C.fileInputEl=C.el.createChild({name:C.inputName,id:!B?C.id+"-fileInputEl":undefined,cls:C.inputCls,tag:"input",type:"file",size:1,role:"button"});A.dom.setAttribute("data-componentid",C.id);A.on({scope:C,change:C.fireChange,focus:C.onFileFocus,blur:C.onFileBlur})},onFileFocus:function(B){var A=this.ownerCt;if(!this.hasFocus){this.onFocus(B)}if(A&&!A.hasFocus){A.onFocus(B)}},onFileBlur:function(B){var A=this.ownerCt;if(this.hasFocus){this.onBlur(B)}if(A&&A.hasFocus){A.onBlur(B)}},reset:function(A){var B=this;if(A){B.fileInputEl.destroy()}B.createFileInput(!A)},restoreInput:function(A){var B=this;B.fileInputEl.destroy();A=Ext.get(A);B.el.appendChild(A);B.fileInputEl=A},onDisable:function(){this.callParent();this.fileInputEl.dom.disabled=true},onEnable:function(){this.callParent();this.fileInputEl.dom.disabled=false},privates:{getFocusEl:function(){return this.fileInputEl},getFocusClsEl:function(){return this.el}}})