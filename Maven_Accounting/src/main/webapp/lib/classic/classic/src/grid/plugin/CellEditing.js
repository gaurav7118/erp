Ext.define("Ext.grid.plugin.CellEditing",{alias:"plugin.cellediting",extend:"Ext.grid.plugin.Editing",requires:["Ext.grid.CellEditor","Ext.util.DelayedTask"],init:function(A){var B=this;B.cacheDeactivatedEditors=Ext.Function.createAnimationFrame(B.cacheDeactivatedEditors);A.registerActionable(B);B.callParent(arguments);B.editors=new Ext.util.MixedCollection(false,function(C){return C.editorId})},beforeGridHeaderDestroy:function(G){var F=this,C=F.grid.getColumnManager().getColumns(),A=C.length,B,E,D;for(B=0;B<A;B++){E=C[B];D=F.editors.getByKey(E.getItemId());if(!D){D=E.editor||E.field}Ext.destroy(D);F.removeFieldAccessors(E)}},onReconfigure:function(C,A,B){if(B){this.editors.clear()}this.callParent()},destroy:function(){var A=this;if(A.editors){A.editors.each(Ext.destroy,Ext);A.editors.clear()}A.callParent()},initCancelTriggers:function(){var B=this,A=B.grid;B.mon(A,{columnresize:B.cancelEdit,columnmove:B.cancelEdit,scope:B})},isCellEditable:function(A,D){var C=this,B=C.getEditingContext(A,D);if(B.view.isVisible(true)&&B){D=B.column;A=B.record;if(D&&C.getEditor(A,D)){return true}}},activateCell:function(B){var G=this,C=B.record,F=B.column,D,A,E;D=G.getEditingContext(C,F);if(!D){return }if(!G.preventBeforeCheck){if(!F.getEditor(C)||G.beforeEdit(D)===false||G.fireEvent("beforeedit",G,D)===false||D.cancel){return }}E=G.getEditor(C,F);if(E){A=Ext.get(D.cell);if(!E.rendered){E.hidden=true;E.render(A,0)}else{if(E.container!==A){E.container=A;A.dom.insertBefore(E.el.dom,A.dom.firstChild)}E.hide()}G.setEditingContext(D);E.startEdit(A,D.value,false);if(E.editing){G.setActiveEditor(E);G.setActiveRecord(C);G.setActiveColumn(F);G.editing=true;G.scroll=B.view.el.getScroll()}return E.editing}},activateRow:Ext.emptyFn,deactivate:function(){var D=this,C=D.editors.items,A=C.length,B;for(B=0;B<A;B++){C[B].hide()}D.cacheDeactivatedEditors()},cacheDeactivatedEditors:function(){var F=this,E=F.editors.items,A=E.length,C,D,B=Ext.getDetachedBody();for(C=0;C<A;C++){D=E[C];if(!D.isVisible()){B.dom.appendChild(D.el.dom);D.container=B}}},startEdit:function(A,B){this.startEditByPosition(new Ext.grid.CellContext(this.view).setPosition(A,B))},completeEdit:function(A){var B=this.getActiveEditor();if(B){B.completeEdit(A)}},setEditingContext:function(A){this.context=A},setActiveEditor:function(A){this.activeEditor=A},getActiveEditor:function(){return this.activeEditor},setActiveColumn:function(A){this.activeColumn=A},getActiveColumn:function(){return this.activeColumn},setActiveRecord:function(A){this.activeRecord=A},getActiveRecord:function(){return this.activeRecord},getEditor:function(A,D){var F=this,E=F.editors,C=D.getItemId(),B=E.getByKey(C);if(!B){B=D.getEditor(A);if(!B){return false}if(B instanceof Ext.grid.CellEditor){B.floating=true}else{B=new Ext.grid.CellEditor({floating:true,editorId:C,field:B})}B.field.excludeForm=true;if(B.column!==D){B.column=D;B.on({scope:F,complete:F.onEditComplete,canceledit:F.cancelEdit});D.on("removed",F.onColumnRemoved,F)}E.add(B)}B.ownerCmp=F.grid.ownerGrid;if(D.isTreeColumn){B.isForTree=D.isTreeColumn;B.addCls(Ext.baseCSSPrefix+"tree-cell-editor")}B.setGrid(F.grid);B.editingPlugin=F;return B},onColumnRemoved:function(B){var C=this,A=C.context;if(A&&A.column===B){C.cancelEdit()}B.un("removed",C.onColumnRemoved,C)},setColumnField:function(B,C){var A=this.editors.getByKey(B.getItemId());Ext.destroy(A,B.field);this.editors.removeAtKey(B.getItemId());this.callParent(arguments)},getCell:function(A,B){return this.grid.getView().getCell(A,B)},onEditComplete:function(D,G,C){var F=this,E=D.context,B,A;B=E.view;A=E.record;E.value=G;if(!F.validateEdit(E)){F.editing=false;return }if(!A.isEqual(G,C)){A.set(E.column.dataIndex,G);E.rowIdx=B.indexOf(A)}F.fireEvent("edit",F,E);if(F.context===E){F.setActiveEditor(null);F.setActiveColumn(null);F.setActiveRecord(null);F.editing=false}},cancelEdit:function(C){var B=this,A=B.context;if(C&&C.isCellEditor){B.context.value=("editedValue" in C)?C.editedValue:C.getValue();B.callParent(arguments);if(C.context===A){B.setActiveEditor(null);B.setActiveColumn(null);B.setActiveRecord(null)}else{B.editing=true}}else{C=B.getActiveEditor();if(C&&C.field){C.cancelEdit()}}},startEditByPosition:function(B){var E=this,A=E.grid.getColumnManager(),C,D=E.getActiveEditor();if(!B.isCellContext){B=new Ext.grid.CellContext(E.view).setPosition(B.row,E.grid.getColumnManager().getColumns()[B.column])}C=A.getHeaderIndex(B.column);B.column=A.getVisibleHeaderClosestToIndex(C);if(E.grid.actionableMode){if(E.editing&&B.isEqual(E.context)){return }if(D){D.completeEdit()}}if(E.grid.actionableMode){if(E.activateCell(B)){E.activateRow(E.view.all.item(B.rowIdx,true));D=E.getEditor(B.record,B.column);if(D){D.field.focus()}}}else{return E.grid.setActionableMode(true,B)}}})