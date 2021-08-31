Ext.define("Ext.ux.dd.CellFieldDropZone",{extend:"Ext.dd.DropZone",constructor:function(A){A=A||{};if(A.onCellDrop){this.onCellDrop=A.onCellDrop}if(A.ddGroup){this.ddGroup=A.ddGroup}},init:function(A){var B=this;if(A.rendered){B.grid=A;A.getView().on({render:function(C){B.view=C;Ext.ux.dd.CellFieldDropZone.superclass.constructor.call(B,B.view.el)},single:true})}else{A.on("render",B.init,B,{single:true})}},containerScroll:true,getTargetFromEvent:function(E){var D=this,B=D.view;var A=E.getTarget(B.getCellSelector());if(A){var F=B.findItemByChild(A),C=A.cellIndex;if(F&&Ext.isDefined(C)){return{node:A,record:B.getRecord(F),fieldName:D.grid.getVisibleColumnManager().getColumns()[C].dataIndex}}}},onNodeEnter:function(F,A,E,D){delete this.dropOK;if(!F){return }var B=D.field;if(!B){return }var C=F.record.fieldsMap[F.fieldName];if(C.isNumeric){if(!B.isXType("numberfield")){return }}else{if(C.isDateField){if(!B.isXType("datefield")){return }}else{if(C.isBooleanField){if(!B.isXType("checkbox")){return }}}}this.dropOK=true;Ext.fly(F.node).addCls("x-drop-target-active")},onNodeOver:function(D,A,C,B){return this.dropOK?this.dropAllowed:this.dropNotAllowed},onNodeOut:function(D,A,C,B){Ext.fly(D.node).removeCls("x-drop-target-active")},onNodeDrop:function(E,A,D,C){if(this.dropOK){var B=C.field.getValue();E.record.set(E.fieldName,B);this.onCellDrop(E.fieldName,B);return true}},onCellDrop:Ext.emptyFn})