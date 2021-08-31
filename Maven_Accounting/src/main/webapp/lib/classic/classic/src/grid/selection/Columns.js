Ext.define("Ext.grid.selection.Columns",{extend:"Ext.grid.selection.Selection",type:"columns",isColumns:true,clone:function(){var C=this,A=new C.self(C.view),B=C.selectedColumns;if(B){A.selectedColumns=Ext.Array.slice(B)}return A},eachRow:function(C,B){var A=this.selectedColumns;if(A&&A.length){this.view.dataSource.each(C,B||this)}},eachColumn:function(G,F){var H=this,B=H.view,E=H.selectedColumns,A,D,C=new Ext.grid.CellContext(B);if(E){A=E.length;for(D=0;D<A;D++){C.setColumn(E[D]);if(G.call(F||H,C.column,C.colIdx)===false){return false}}}},eachCell:function(G,F){var H=this,B=H.view,E=H.selectedColumns,A,D,C=new Ext.grid.CellContext(B);if(E){A=E.length;B.dataSource.each(function(I){C.setRow(I);for(D=0;D<A;D++){C.setColumn(E[D]);if(G.call(F||H,C,C.colIdx,C.rowIdx)===false){return false}}})}},contains:function(B){var A=this.selectedColumns;if(B&&B.isColumn&&A&&A.length){return Ext.Array.contains(A,B)}return false},getCount:function(){var A=this.selectedColumns;return A?A.length:0},getColumns:function(){return this.selectedColumns||[]},privates:{add:function(A){if(!A.isColumn){Ext.raise("Column selection must be passed a grid Column header object")}Ext.Array.include((this.selectedColumns||(this.selectedColumns=[])),A);this.refreshColumns(A)},clear:function(){var A=this,B=A.selectedColumns;if(B&&B.length){A.selectedColumns=[];A.refreshColumns.apply(A,B)}},isAllSelected:function(){var A=this.selectedColumns;return A&&A.length===this.view.ownerGrid.getVisibleColumnManager().getColumns().length},refreshColumns:function(B){var H=this,I=H.view,J=I.all,A,C=arguments,G=C.length,E,F=new Ext.grid.CellContext(I),D=[];if(I.rendered){for(E=0;E<G;E++){D[E]=H.contains(C[E])}for(A=J.startIndex;A<=J.endIndex;A++){F.setRow(A);for(E=0;E<G;E++){F.setColumn(C[E]);if(D[E]){I.onCellSelect(F)}else{I.onCellDeselect(F)}}}}},remove:function(A){if(!A.isColumn){Ext.raise("Column selection must be passed a grid Column header object")}if(this.selectedColumns){Ext.Array.remove(this.selectedColumns,A);if(A.getView()&&A.isVisible()){this.refreshColumns(A)}}},selectAll:function(){var A=this;A.clear();A.selectedColumns=A.view.getSelectionModel().lastContiguousColumnRange=A.view.getVisibleColumnManager().getColumns();A.refreshColumns.apply(A,A.selectedColumns)},extendRange:function(A){var D=this,C=D.view.getVisibleColumnManager().getColumns(),B;for(B=A.start.colIdx;B<=A.end.colIdx;B++){D.add(C[B])}},onSelectionFinish:function(){var B=this,A=B.getContiguousSelection();if(A){B.view.getSelectionModel().onSelectionFinish(B,new Ext.grid.CellContext(B.view).setPosition(0,A[0]),new Ext.grid.CellContext(B.view).setPosition(B.view.dataSource.getCount()-1,A[1]))}else{B.view.getSelectionModel().onSelectionFinish(B)}},getContiguousSelection:function(){var C=Ext.Array.sort(this.selectedColumns,function(E,D){return E.getView().ownerGrid.getVisibleColumnManager().indexOf(E)-D.getView().ownerGrid.getVisibleColumnManager().indexOf(D)}),A=C.length,B;if(A){for(B=1;B<A;B++){if(C[B].getVisibleIndex()!==C[B-1].getVisibleIndex()+1){return false}}return[C[0],C[A-1]]}}}})