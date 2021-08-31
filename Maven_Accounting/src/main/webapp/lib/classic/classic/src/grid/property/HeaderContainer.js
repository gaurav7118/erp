Ext.define("Ext.grid.property.HeaderContainer",{extend:"Ext.grid.header.Container",alternateClassName:"Ext.grid.PropertyColumnModel",nameWidth:115,nameText:"Name",valueText:"Value",dateFormat:"m/j/Y",trueText:"true",falseText:"false",nameColumnCls:Ext.baseCSSPrefix+"grid-property-name",nameColumnInnerCls:Ext.baseCSSPrefix+"grid-cell-inner-property-name",constructor:function(B,A){var C=this;C.grid=B;C.store=A;C.callParent([{isRootHeader:true,enableColumnResize:Ext.isDefined(B.enableColumnResize)?B.enableColumnResize:C.enableColumnResize,enableColumnMove:Ext.isDefined(B.enableColumnMove)?B.enableColumnMove:C.enableColumnMove,items:[{header:C.nameText,width:B.nameColumnWidth||C.nameWidth,sortable:B.sortableColumns,dataIndex:B.nameField,scope:C,renderer:C.renderProp,itemId:B.nameField,menuDisabled:true,tdCls:C.nameColumnCls,innerCls:C.nameColumnInnerCls},{header:C.valueText,scope:C,renderer:C.renderCell,getEditor:C.getCellEditor.bind(C),sortable:B.sortableColumns,flex:1,fixed:true,dataIndex:B.valueField,itemId:B.valueField,menuDisabled:true}]}]);C.grid.valueColumn=C.items.getAt(1)},getCellEditor:function(A){return this.grid.getCellEditor(A,this)},renderProp:function(A){return this.getPropertyName(A)},renderCell:function(G,E,F){var C=this,B=C.grid,D=B.getConfigProp(F.get(B.nameField),"renderer"),A=G;if(D){return D.apply(C,arguments)}if(Ext.isDate(G)){A=C.renderDate(G)}else{if(Ext.isBoolean(G)){A=C.renderBool(G)}}return Ext.util.Format.htmlEncode(A)},renderDate:Ext.util.Format.date,renderBool:function(A){return this[A?"trueText":"falseText"]},getPropertyName:function(A){return this.grid.getConfigProp(A,"displayName",A)}})