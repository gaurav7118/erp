Ext.define("Ext.tree.NavigationModel",{extend:"Ext.grid.NavigationModel",alias:"view.navigation.tree",initKeyNav:function(B){var E=this,D=E.view.ownerGrid.columns,A,C;E.isTreeGrid=D&&D.length>1;E.callParent([B]);for(C=0,A=E.keyNav.length;C<A;C++){E.keyNav[C].map.addBinding([{key:"8",shift:true,handler:E.onAsterisk,scope:E},{key:Ext.event.Event.NUM_MULTIPLY,handler:E.onAsterisk,scope:E}])}E.view.grid.on({columnschanged:E.onColumnsChanged,scope:E})},onColumnsChanged:function(){this.isTreeGrid=this.view.ownerGrid.getVisibleColumnManager().getColumns().length>1},onCellClick:function(D,B,F,C,G,E,A){this.callParent([D,B,F,C,G,E,A]);return !A.nodeToggled},onKeyLeft:function(D){var C=this,B=D.view,A=C.record;if(C.isTreeGrid&&!D.ctrlKey){return C.callParent([D])}if(D.position.column.isTreeColumn&&A.isExpanded()){B.collapse(A)}else{A=A.parentNode;if(A&&!(A.isRoot()&&!B.rootVisible)){C.setPosition(A,null,D)}}},onKeyRight:function(C){var B=this,A=B.record;if(B.isTreeGrid&&!C.ctrlKey){return B.callParent([C])}if(!A.isLeaf()){if(C.position.column.isTreeColumn&&!A.isExpanded()){C.view.expand(A)}else{if(A.isExpanded()){A=A.childNodes[0];if(A){B.setPosition(A)}}}}},onKeyEnter:function(A){if(this.record.data.checked!=null){this.toggleCheck(A)}else{this.callParent([A])}},onKeySpace:function(A){if(this.record.data.checked!=null){this.toggleCheck(A)}else{this.callParent([A])}},toggleCheck:function(A){this.view.onCheckChange(A)},onAsterisk:function(A){this.view.ownerCt.expandAll()}})