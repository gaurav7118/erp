Ext.define("Ext.grid.plugin.RowEditing",{extend:"Ext.grid.plugin.Editing",alias:"plugin.rowediting",requires:["Ext.grid.RowEditor"],lockableScope:"top",editStyle:"row",autoCancel:true,errorSummary:true,constructor:function(){var A=this;A.callParent(arguments);if(!A.clicksToMoveEditor){A.clicksToMoveEditor=A.clicksToEdit}A.autoCancel=!!A.autoCancel},init:function(A){this.callParent([A]);if(A.lockedGrid){A.lockedGrid.registerActionable(this);A.normalGrid.registerActionable(this)}else{A.registerActionable(this)}},destroy:function(){Ext.destroy(this.editor);this.callParent()},onBeforeReconfigure:function(){this.callParent(arguments);this.cancelEdit()},onReconfigure:function(D,B,C){var A=this.editor;this.callParent(arguments);if(C&&A&&A.rendered){A.needsSyncFieldWidths=true}},shouldStartEdit:function(A){return true},startEdit:function(A,E){var D=this,C=D.getEditor(),B;if(Ext.isEmpty(E)){E=D.grid.getTopLevelVisibleColumnManager().getHeaderAtIndex(0)}if(C.beforeEdit()!==false){B=D.getEditingContext(A,E);if(B&&D.beforeEdit(B)!==false&&D.fireEvent("beforeedit",D,B)!==false&&!B.cancel){D.context=B;if(D.lockingPartner){D.lockingPartner.cancelEdit()}C.startEdit(B.record,B.column,B);D.editing=true;return true}}return false},activateCell:function(A){if(!A.getCell().query('[tabIndex="-1"]').length){this.startEdit(A.record,A.column);return true}},onEnterKey:function(C){var A=this,B;if(!A.grid.ownerGrid.actionableMode&&A.editing){B=Ext.getCmp(C.getTarget().getAttribute("componentId"));if(!(B&&B.isPickerField&&B.isExpanded)){A.completeEdit()}}},cancelEdit:function(){var A=this;if(A.editing){A.getContextFieldValues();A.getEditor().cancelEdit();A.callParent(arguments);return }return true},completeEdit:function(){var B=this,A=B.context;if(B.editing&&B.validateEdit(A)){B.editing=false;B.fireEvent("edit",B,A)}},validateEdit:function(){this.getContextFieldValues();return this.callParent(arguments)&&this.getEditor().completeEdit()},getEditor:function(){var A=this;if(!A.editor){A.editor=A.initEditor()}return A.editor},getContextFieldValues:function(){var F=this.editor,B=this.context,E=B.record,J={},C={},H=F.query(">[isFormField]"),G=H.length,D,A,I;for(D=0;D<G;D++){I=H[D];A=I.dataIndex;J[A]=I.getValue();C[A]=E.get(A)}Ext.apply(B,{newValues:J,originalValues:C})},initEditor:function(){return new Ext.grid.RowEditor(this.initEditorConfig())},initEditorConfig:function(){var F=this,B=F.grid,G=F.view,C=B.headerCt,D=["saveBtnText","cancelBtnText","errorsText","dirtyText"],H,A=D.length,E={autoCancel:F.autoCancel,errorSummary:F.errorSummary,fields:C.getGridColumns(),hidden:true,view:G,editingPlugin:F},I;for(H=0;H<A;H++){I=D[H];if(Ext.isDefined(F[I])){E[I]=F[I]}}return E},initEditTriggers:function(){var B=this,A=B.view,C=B.clicksToMoveEditor===1?"click":"dblclick";B.callParent(arguments);if(B.clicksToMoveEditor!==B.clicksToEdit){B.mon(A,"cell"+C,B.moveEditorByClick,B)}A.on({render:function(){B.mon(B.grid.headerCt,{scope:B,columnresize:B.onColumnResize,columnhide:B.onColumnHide,columnshow:B.onColumnShow})},single:true})},moveEditorByClick:function(){var A=this;if(A.editing){A.superclass.onCellClick.apply(A,arguments)}},onColumnAdd:function(A,C){if(C.isHeader){var D=this,B;D.initFieldAccessors(C);B=D.editor;if(B){B.onColumnAdd(C)}}},beforeGridHeaderDestroy:function(F){var C=this.grid.getColumnManager().getColumns(),A=C.length,B,D,E;for(B=0;B<A;B++){D=C[B];if(D.hasEditor){if(D.hasEditor()&&(E=D.getEditor())){E.destroy()}this.removeFieldAccessors(D)}}},onColumnResize:function(A,D,C){if(D.isHeader){var E=this,B=E.getEditor();if(B){B.onColumnResize(D,C)}}},onColumnHide:function(A,C){var D=this,B=D.getEditor();if(B){B.onColumnHide(C)}},onColumnShow:function(A,C){var D=this,B=D.getEditor();if(B){B.onColumnShow(C)}},onColumnMove:function(A,D,C,F){var E=this,B=E.getEditor();E.initFieldAccessors(D);if(B){B.onColumnMove(D,C,F)}},setColumnField:function(B,D){var C=this,A=C.getEditor();if(A){A.destroyColumnEditor(B)}C.callParent(arguments);if(A){A.insertColumnEditor(B)}},createColumnField:function(C,A){var B=this.editor,D;if(B){D=B.getDefaultFieldCfg()}return this.callParent([C,A||D])}})