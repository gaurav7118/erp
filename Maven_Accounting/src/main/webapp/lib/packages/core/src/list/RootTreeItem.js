Ext.define("Ext.list.RootTreeItem",{extend:"Ext.list.AbstractTreeItem",isRootListItem:true,element:{reference:"element",tag:"ul",cls:Ext.baseCSSPrefix+"treelist-root-container"},insertItem:function(B,A){if(A){B.element.insertBefore(A.element)}else{this.element.appendChild(B.element)}},isToggleEvent:function(A){return false}})