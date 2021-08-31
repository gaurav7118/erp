Ext.define("Ext.mixin.Selectable",{extend:"Ext.Mixin",mixinConfig:{id:"selectable",after:{updateStore:"updateStore"}},config:{disableSelection:null,mode:"SINGLE",allowDeselect:false,lastSelected:null,lastFocused:null,deselectOnContainerClick:true,selection:null,twoWayBindable:{selection:1},publishes:{selection:1}},modes:{SINGLE:true,SIMPLE:true,MULTI:true},selectableEventHooks:{add:"onSelectionStoreAdd",remove:"onSelectionStoreRemove",update:"onSelectionStoreUpdate",clear:{fn:"onSelectionStoreClear",priority:1000},load:"refreshSelection",refresh:"refreshSelection"},constructor:function(){this.selected=new Ext.util.MixedCollection();this.callParent(arguments)},initSelectable:function(){this.publishState("selection",this.getSelection())},applyMode:function(A){A=A?A.toUpperCase():"SINGLE";return this.modes[A]?A:"SINGLE"},updateStore:function(A,C){var B=this,D=Ext.apply({},B.selectableEventHooks,{scope:B});if(C&&Ext.isObject(C)&&C.isStore){if(C.autoDestroy){C.destroy()}else{C.un(D)}}if(A){A.on(D);B.refreshSelection()}},selectAll:function(A){var C=this,B=C.getStore().getRange();C.select(B,true,A)},deselectAll:function(C){var B=this,A=B.getStore().getRange();B.deselect(A,C);B.selected.clear();B.setLastSelected(null);B.setLastFocused(null)},updateSelection:function(A){if(this.changingSelection){return }if(A){this.select(A)}else{this.deselectAll()}},selectWithEvent:function(A){var C=this,B=C.isSelected(A);switch(C.getMode()){case"MULTI":case"SIMPLE":if(B){C.deselect(A)}else{C.select(A,true)}break;case"SINGLE":if(C.getAllowDeselect()&&B){C.deselect(A)}else{C.select(A,false)}break}},selectRange:function(C,G,H){var F=this,B=F.getStore(),A=[],E,D;if(F.getDisableSelection()){return }if(C>G){E=G;G=C;C=E}for(D=C;D<=G;D++){A.push(B.getAt(D))}this.doMultiSelect(A,H)},select:function(C,E,B){var D=this,A;if(D.getDisableSelection()){return }if(typeof C==="number"){C=[D.getStore().getAt(C)]}if(!C){return }if(D.getMode()=="SINGLE"&&C){A=C.length?C[0]:C;D.doSingleSelect(A,B)}else{D.doMultiSelect(C,E,B)}},doSingleSelect:function(A,B){var D=this,C=D.selected;if(D.getDisableSelection()){return }if(D.isSelected(A)){return }if(C.getCount()>0){D.deselect(D.getLastSelected(),B)}C.add(A);D.setLastSelected(A);D.onItemSelect(A,B);D.setLastFocused(A);if(!B){D.fireSelectionChange([A])}},doMultiSelect:function(A,I,H){if(A===null||this.getDisableSelection()){return }A=!Ext.isArray(A)?[A]:A;var F=this,B=F.selected,E=A.length,G=false,C=0,D;if(!I&&B.getCount()>0){G=true;F.deselect(F.getSelections(),true)}for(;C<E;C++){D=A[C];if(I&&F.isSelected(D)){continue}G=true;F.setLastSelected(D);B.add(D);if(!H){F.setLastFocused(D)}F.onItemSelect(D,H)}if(G&&!H){this.fireSelectionChange(A)}},deselect:function(A,I){var F=this;if(F.getDisableSelection()){return }A=Ext.isArray(A)?A:[A];var B=F.selected,G=false,C=0,H=F.getStore(),E=A.length,D;for(;C<E;C++){D=A[C];if(typeof D==="number"){D=H.getAt(D)}if(B.remove(D)){if(F.getLastSelected()==D){F.setLastSelected(B.last())}G=true}if(D){F.onItemDeselect(D,I)}}if(G&&!I){F.fireSelectionChange(A)}},updateLastFocused:function(B,A){this.onLastFocusChanged(A,B)},fireSelectionChange:function(A){var B=this;B.changingSelection=true;B.setSelection(B.getLastSelected()||null);B.changingSelection=false;B.fireAction("selectionchange",[B,A],"getSelections")},getSelections:function(){return this.selected.getRange()},isSelected:function(A){A=Ext.isNumber(A)?this.getStore().getAt(A):A;return this.selected.indexOf(A)!==-1},hasSelection:function(){return this.selected.getCount()>0},refreshSelection:function(){var B=this,A=B.getSelections();B.deselectAll(true);if(A.length){B.select(A,false,true)}},onSelectionStoreRemove:function(C,B){var G=this,E=G.selected,F=B.length,H,A,D;if(G.getDisableSelection()){return }for(D=0;D<F;D++){A=B[D];if(E.remove(A)){if(G.getLastSelected()==A){G.setLastSelected(null)}if(G.getLastFocused()==A){G.setLastFocused(null)}H=H||[];H.push(A)}}if(H){G.fireSelectionChange([H])}},onSelectionStoreClear:function(B){var A=B.getData().items;this.onSelectionStoreRemove(B,A)},getSelectionCount:function(){return this.selected.getCount()},onSelectionStoreAdd:Ext.emptyFn,onSelectionStoreUpdate:Ext.emptyFn,onItemSelect:Ext.emptyFn,onItemDeselect:Ext.emptyFn,onLastFocusChanged:Ext.emptyFn,onEditorKey:Ext.emptyFn},function(){})