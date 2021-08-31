Ext.define("Ext.grid.filters.filter.String",{extend:"Ext.grid.filters.filter.SingleFilter",alias:"grid.filter.string",type:"string",operator:"like",emptyText:"Enter Filter Text...",itemDefaults:{xtype:"textfield",enableKeyEvents:true,hideEmptyLabel:false,iconCls:Ext.baseCSSPrefix+"grid-filters-find",labelSeparator:"",labelWidth:29,margin:0,selectOnFocus:true},menuDefaults:{bodyPadding:3,showSeparator:false},createMenu:function(){var B=this,A;B.callParent();A=Ext.apply({},B.getItemDefaults());if(A.iconCls&&!("labelClsExtra" in A)){A.labelClsExtra=Ext.baseCSSPrefix+"grid-filters-icon "+A.iconCls}delete A.iconCls;A.emptyText=A.emptyText||B.emptyText;B.inputItem=B.menu.add(A);B.inputItem.on({scope:B,keyup:B.onValueChange,el:{click:function(C){C.stopPropagation()}}})},setValue:function(B){var A=this;if(A.inputItem){A.inputItem.setValue(B)}A.filter.setValue(B);if(B&&A.active){A.value=B;A.updateStoreFilter()}else{A.setActive(!!B)}},activateMenu:function(){this.inputItem.setValue(this.filter.getValue())}})