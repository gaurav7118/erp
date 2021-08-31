Ext.define("Ext.form.FieldContainer",{extend:"Ext.container.Container",mixins:{labelable:"Ext.form.Labelable",fieldAncestor:"Ext.form.FieldAncestor"},requires:"Ext.layout.component.field.FieldContainer",alias:"widget.fieldcontainer",componentLayout:"fieldcontainer",componentCls:Ext.baseCSSPrefix+"form-fieldcontainer",shrinkWrap:true,autoEl:{tag:"div",role:"presentation"},childEls:["containerEl"],combineLabels:false,labelConnector:", ",combineErrors:false,maskOnDisable:false,invalidCls:"",fieldSubTpl:['<div id="{id}-containerEl" data-ref="containerEl" class="{containerElCls}"','<tpl if="ariaAttributes">','<tpl foreach="ariaAttributes"> {$}="{.}"</tpl>',"<tpl else>",' role="presentation"',"</tpl>",">","{%this.renderContainer(out,values)%}","</div>"],initComponent:function(){var A=this;A.initLabelable();A.initFieldAncestor();A.callParent();A.initMonitor()},onAdd:function(A){var B=this;if(A.isLabelable&&Ext.isGecko&&Ext.firefoxVersion<37&&B.layout.type==="absolute"&&!B.hideLabel&&B.labelAlign!=="top"){A.x+=(B.labelWidth+B.labelPad)}B.callParent(arguments);if(A.isLabelable&&B.combineLabels){A.oldHideLabel=A.hideLabel;A.hideLabel=true}B.updateLabel()},onRemove:function(A,B){var C=this;C.callParent(arguments);if(!B){if(A.isLabelable&&C.combineLabels){A.hideLabel=A.oldHideLabel}C.updateLabel()}},initRenderData:function(){var A=this,B=A.callParent();B.containerElCls=A.containerElCls;B=Ext.applyIf(B,A.getLabelableRenderData());B.tipAnchorTarget=A.id+"-containerEl";return B},getFieldLabel:function(){var A=this.fieldLabel||"";if(!A&&this.combineLabels){A=Ext.Array.map(this.query("[isFieldLabelable]"),function(B){return B.getFieldLabel()}).join(this.labelConnector)}return A},getSubTplData:function(){var A=this.initRenderData();Ext.apply(A,this.subTplData);return A},getSubTplMarkup:function(B){var D=this,A=D.getTpl("fieldSubTpl"),C;if(!A.renderContent){D.setupRenderTpl(A)}C=A.apply(D.getSubTplData(B));return C},updateLabel:function(){var B=this,A=B.labelEl;if(A){B.setFieldLabel(B.getFieldLabel())}},onFieldErrorChange:function(){if(this.combineErrors){var C=this,D=C.getActiveError(),B=Ext.Array.filter(C.query("[isFormField]"),function(E){return E.hasActiveError()}),A=C.getCombinedErrors(B);if(A){C.setActiveErrors(A)}else{C.unsetActiveError()}if(D!==C.getActiveError()){C.updateLayout()}}},getCombinedErrors:function(D){var I=[],B,J=D.length,G,C,H,A,E,F;for(B=0;B<J;B++){G=D[B];C=G.getActiveErrors();A=C.length;for(H=0;H<A;H++){E=C[H];F=G.getFieldLabel();I.push((F?F+": ":"")+E)}}return I},privates:{applyTargetCls:function(B){var A=this.containerElCls;this.containerElCls=A?A+" "+B:B},getTargetEl:function(){return this.containerEl},initRenderTpl:function(){var A=this;if(!A.hasOwnProperty("renderTpl")){A.renderTpl=A.getTpl("labelableRenderTpl")}return A.callParent()}}})