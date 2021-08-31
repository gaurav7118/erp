describe("Ext.grid.selection.SpreadsheetModel",function(){var A,L,N,H,M,D=Ext.isIE?xdescribe:describe,Q=true,E=Ext.data.ProxyStore.prototype.load,K;function G(T,V,U,S,R,X){var W=O(V,U);jasmine.fireMouseEvent(W,T,R,X,S)}function O(S,R){return L.getCell(S,M[R])}function J(S,R){return L.getSelectionModel().isCellSelected(L,S,R)}function C(T){var S=L.el.query(M[T].getCellSelector()),R=S.length,U;for(U=0;U<R;U++){if(!Ext.fly(S[U]).hasCls(L.selectedCellCls)){return false}}return true}function B(R){return L.getSelectionModel().isSelected(R)}function P(R,T){var S=O(R,0);jasmine.fireMouseEvent(S,"click",null,null,null,null,T)}function F(S,R,U){var V={fn:U||Ext.emptyFn},T=spyOn(V,"fn");S.addListener(R,V.fn);return T}function I(W,R,Y,U,T){Ext.define("spec.SpreadsheetModel",{extend:"Ext.data.Model",fields:["field1","field2","field3","field4","field5"]});H=new Ext.grid.selection.SpreadsheetModel(Ext.apply({dragSelect:true,cellSelect:true,columnSelect:true,rowSelect:true,checkboxSelect:false},Y));var X=[],S=[],V;if(!W){for(V=1;V<=5;++V){S.push({name:"F"+V,dataIndex:"field"+V,locked:T&&V===1})}}for(V=1;V<=10;++V){if(U&&U.numeric){X.push({field1:V*10+1,field2:V*10+2,field3:V*10+3,field4:V*10+4,field5:V*10+5})}else{X.push({field1:V+"."+1,field2:V+"."+2,field3:V+"."+3,field4:V+"."+4,field5:V+"."+5})}}U=Ext.apply({model:spec.SpreadsheetModel},U);if(U.proxy&&U.proxy.type==="memory"&&!U.proxy.data){U.proxy.data=X}else{if(!("data" in U)){U.data=X}}N=new Ext.data.Store(U);A=new Ext.grid.Panel(Ext.apply({columns:W||S,store:N,selModel:H,width:600,height:300,renderTo:Ext.getBody()},R));L=A.getView();H=A.getSelectionModel();M=A.getColumnManager().getColumns()}beforeEach(function(){K=Ext.data.ProxyStore.prototype.load=function(){E.apply(this,arguments);if(Q){this.flushLoad.apply(this,arguments)}return this}});afterEach(function(){Ext.data.ProxyStore.prototype.load=E;Ext.destroy(A,N);H=A=N=L=null;Ext.undefine("spec.SpreadsheetModel");Ext.data.Model.schema.clear()});D("Non-rendered operation",function(){it("should allow reconfiguration before render",function(){I(null,{renderTo:null});expect(function(){H.setRowSelect(false);H.setRowSelect(true);H.setColumnSelect(false);H.setColumnSelect(true);H.setCellSelect(false);H.setCellSelect(true)}).not.toThrow()});it("should allow selection of cells before render",function(){var R;I(null,{renderTo:null});H.selectCells(R=new Ext.grid.CellContext(L).setPosition(2,2),R);A.render(document.body);expect(J(2,2)).toBe(true)});it("should allow selection of cells before render using array notation",function(){I(null,{renderTo:null});H.selectCells([2,3],[2,3]);A.render(document.body);expect(J(3,2)).toBe(true)});it("should allow selection of records before render",function(){I(null,{renderTo:null});H.select(N.getAt(2));A.render(document.body);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(2)).toBe(true)});it("should allow selection of columns before render",function(){I(null,{renderTo:null});H.selectColumn(M[2]);A.render(document.body);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[2].getCellSelector()).length);expect(C(2)).toBe(true)})});D("Select all",function(){it("should select all on click of header zero",function(){I();var R=O(2,0);jasmine.fireMouseEvent(M[0].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(N.getCount());jasmine.fireMouseEvent(M[0].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(0);jasmine.fireMouseEvent(M[0].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(N.getCount());expect(H.isSelected(2)).toBe(true);jasmine.fireMouseEvent(R,"click",null,null,null,null,true);expect(L.el.query("."+L.selectedItemCls).length).toBe(N.getCount()-1);expect(H.isSelected(2)).toBe(false);jasmine.fireMouseEvent(M[0].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(N.getCount())})});D("Column selection",function(){it("should select a column on click of a header",function(){I();var R=F(N,"sort").andCallThrough();jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(0);expect(R).not.toHaveBeenCalled()});it("should select a column on click of a header and deselect previous columns",function(){I();var R=F(N,"sort").andCallThrough();jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true);jasmine.fireMouseEvent(M[2].el.dom,"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[2].getCellSelector()).length);expect(C(2)).toBe(true);jasmine.fireKeyEvent(M[2].el.dom,"keydown",Ext.event.Event.RIGHT);jasmine.fireKeyEvent(M[3].el.dom,"keydown",Ext.event.Event.SPACE);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[3].getCellSelector()).length);expect(C(3)).toBe(true);expect(R).not.toHaveBeenCalled()});it("should select a column on CTRL/click of a header and not deselect previous columns",function(){I();var R=F(N,"sort").andCallThrough();jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true);jasmine.fireMouseEvent(M[2].el.dom,"click",0,0,1,false,true);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length+L.el.query(M[2].getCellSelector()).length);expect(C(1)).toBe(true);expect(C(2)).toBe(true);jasmine.fireKeyEvent(M[2].el.dom,"keydown",Ext.event.Event.RIGHT);jasmine.fireKeyEvent(M[3].el.dom,"keydown",Ext.event.Event.SPACE,false,true);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length+L.el.query(M[2].getCellSelector()).length+L.el.query(M[3].getCellSelector()).length);expect(C(1)).toBe(true);expect(C(2)).toBe(true);expect(C(3)).toBe(true);expect(R).not.toHaveBeenCalled()})});D("Row selection",function(){it("should select a row on click of a rownumberer",function(){I();P(1);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(0)});it("should select a row on click of a rownumberer and deselect previous rows",function(){I();P(1);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);P(2);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(2)).toBe(true);jasmine.fireKeyEvent(O(2,0),"keydown",Ext.event.Event.DOWN,null,true);jasmine.fireKeyEvent(O(3,0),"keydown",Ext.event.Event.SPACE);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(3)).toBe(true)});it("should select a rown on CTRL/click of a rownumberer and not deselect previous rows",function(){I();P(1);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);P(2,true);expect(L.el.query("."+L.selectedItemCls).length).toBe(2);expect(B(1)).toBe(true);expect(B(2)).toBe(true);jasmine.fireKeyEvent(O(2,0),"keydown",Ext.event.Event.DOWN,null,true);jasmine.fireKeyEvent(O(3,0),"keydown",Ext.event.Event.SPACE,null,true);expect(L.el.query("."+L.selectedItemCls).length).toBe(3);expect(B(1)).toBe(true);expect(B(2)).toBe(true);expect(B(3)).toBe(true)});it("should fire the selectionchange event when rows are selected and rowSelect is set to false",function(){I();P(1);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);var R=F(A,"selectionchange");H.setRowSelect(false);expect(L.el.query("."+L.selectedItemCls).length).toBe(0);expect(R).toHaveBeenCalled()});it("should not copy the rownumberer column",function(){I(null,{plugins:"clipboard"});var S=A.findPlugin("clipboard"),R;P(1);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);R=S.getData(false,{text:1});expect(R.text).toBe("2.1\t2.2\t2.3\t2.4\t2.5")})});describe("Row selection using selectRows",function(){it("should select a row and clear previous non-row selections",function(){I();H.selectCells(new Ext.grid.CellContext(L).setPosition(2,2),new Ext.grid.CellContext(L).setPosition(2,4));expect(L.el.query("."+L.selectedCellCls).length).toBe(3);expect(J(2,2)&&J(2,3)&&J(2,4)).toBe(true);H.selectRows(N.getAt(1));expect(L.el.query("."+L.selectedCellCls).length).toBe(0);expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(L.el.query("."+L.selectedItemCls).length).toBe(0)});it("should select a row and deselect previous rows",function(){I();H.selectRows(N.getAt(1));expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);H.selectRows(N.getAt(2));expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(2)).toBe(true)});it("should select a row and not deselect previous rows",function(){I();H.selectRows(N.getAt(1));expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);H.selectRows(N.getAt(2),true);expect(L.el.query("."+L.selectedItemCls).length).toBe(2);expect(B(1)).toBe(true);expect(B(2)).toBe(true)});it("should fire the selectionchange event when rows are selected and rowSelect is set to false",function(){I();H.selectRows(N.getAt(1));expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);var R=F(A,"selectionchange");H.setRowSelect(false);expect(L.el.query("."+L.selectedItemCls).length).toBe(0);expect(R).toHaveBeenCalled()});it("should not copy the rownumberer column",function(){I(null,{plugins:"clipboard"});var S=A.findPlugin("clipboard"),R;H.selectRows(N.getAt(1));expect(L.el.query("."+L.selectedItemCls).length).toBe(1);expect(B(1)).toBe(true);R=S.getData(false,{text:1});expect(R.text).toBe("2.1\t2.2\t2.3\t2.4\t2.5")})});D("Range selection",function(){it("should select a range on drag",function(){I();var S=O(2,2),R=O(4,4);jasmine.fireMouseEvent(S,"mousedown");jasmine.fireMouseEvent(S,"mousemove");jasmine.fireMouseEvent(R,"mousemove");jasmine.fireMouseEvent(R,"mouseup");expect(L.el.query("."+L.selectedCellCls).length).toBe(9);expect(J(2,2)&&J(3,2)&&J(4,2)&&J(2,3)&&J(3,3)&&J(4,3)&&J(2,4)&&J(3,4)&&J(4,4)).toBe(true)});it("should select a range in a single row on drag",function(){I();var S=O(2,2),R=O(2,4);jasmine.fireMouseEvent(S,"mousedown");jasmine.fireMouseEvent(S,"mousemove");jasmine.fireMouseEvent(R,"mousemove");jasmine.fireMouseEvent(R,"mouseup");expect(L.el.query("."+L.selectedCellCls).length).toBe(3);expect(J(2,2)&&J(2,3)&&J(2,4)).toBe(true)});describe("Range selection using selectCells",function(){it("should work when using CellContext objects to describe the range",function(){I();H.selectCells(new Ext.grid.CellContext(L).setPosition(2,2),new Ext.grid.CellContext(L).setPosition(2,4));expect(L.el.query("."+L.selectedCellCls).length).toBe(3);expect(J(2,2)&&J(2,3)&&J(2,4)).toBe(true)});it("should work when using [x,y] arrays to describe the range",function(){I();H.selectCells([2,2],[2,4]);expect(L.el.query("."+L.selectedCellCls).length).toBe(3);expect(J(2,2)&&J(3,2)&&J(4,2)).toBe(true)})});it("should not wrap when SHIFT+RIGHT on last cell",function(){I();var R=O(2,5),S;jasmine.fireMouseEvent(R,"click");waitsFor(function(){S=L.getNavigationModel().getPosition();return S&&S.getCell(true)===R.dom});runs(function(){jasmine.fireKeyEvent(R,"keydown",Ext.event.Event.RIGHT,true)});waits(100);runs(function(){expect(L.getNavigationModel().getCell().dom).toBe(R.dom)})})});D("Single cell selection",function(){it("should select a single cell on click",function(){I();jasmine.fireMouseEvent(O(2,2),"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(1);expect(J(2,2)).toBe(true);jasmine.fireMouseEvent(O(5,5),"click");expect(L.el.query("."+L.selectedCellCls).length).toBe(1);expect(J(5,5)).toBe(true)})});describe("pruneRemoved",function(){describe("pruneRemoved: true",function(){it("should remove records from selection by default when they are removed from the store",function(){I(null,{bbar:{xtype:"pagingtoolbar"}},null,{autoLoad:false,pageSize:5,proxy:{type:"memory",enablePaging:true}});N.proxy.enablePaging=true;var R=A.down("pagingtoolbar"),S;R.setStore(N);N.loadPage(1);H.select(0);S=H.getSelection();expect(S.length).toBe(1);expect(S[0]===N.getAt(0)).toBe(true);expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(true);R.moveNext();expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(false);R.movePrevious();S=H.getSelection();expect(S.length).toBe(0);expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(false)})});describe("pruneRemoved: false",function(){it("should NOT remove records from selection if pruneRemoved:false when they are removed from the store",function(){I(null,{bbar:{xtype:"pagingtoolbar"}},{pruneRemoved:false},{autoLoad:false,pageSize:5,proxy:{type:"memory",enablePaging:true}});N.proxy.enablePaging=true;var R=A.down("pagingtoolbar"),S;R.setStore(N);N.loadPage(1);H.select(0);S=H.getSelection();expect(S.length).toBe(1);expect(S[0]===N.getAt(0)).toBe(true);expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(true);R.moveNext();expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(false);R.movePrevious();S=H.getSelection();expect(S.length).toBe(1);expect(S[0]===N.getAt(0)).toBe(true);expect(Ext.fly(L.getNode(0)).hasCls(L.selectedItemCls)).toBe(true)})})});describe("view model selection",function(){var S,U,T;function V(X,Z,Y){H=new Ext.selection.RowModel(Z||{});A=new Ext.grid.Panel(Ext.apply({store:new Ext.data.Store(Ext.apply({fields:["name"],proxy:{type:"memory",data:[{name:"Phil"},{name:"Ben"},{name:"Evan"},{name:"Don"},{name:"Nige"},{name:"Alex"}]}},Y)),columns:[{text:"Name",dataIndex:"name"}],selModel:H,height:200,width:200,renderTo:Ext.getBody()},X));N=A.getStore();if(!Y||Y.autoLoad!==false){N.load()}L=A.getView();T=A.view.getVisibleColumnManager().getColumns()}beforeEach(function(){U=jasmine.createSpy();S=new Ext.app.ViewModel()});afterEach(function(){U=H=S=null});function W(X){H.select(X);S.notify()}function R(Y){var X=N.findExact("name",Y);return N.getAt(X)}describe("reference",function(){beforeEach(function(){V({reference:"userList",viewModel:S});S.bind("{userList.selection}",U);S.notify()});it("should publish null by default",function(){var X=U.mostRecentCall.args;expect(X[0]).toBeNull();expect(X[1]).toBeUndefined()});it("should publish the value when selected",function(){var Y=R("Ben");W(Y);var X=U.mostRecentCall.args;expect(X[0]).toBe(Y);expect(X[1]).toBeNull()});it("should publish when the selection is changed",function(){var Z=R("Ben"),Y=R("Nige");W(Z);U.reset();W(Y);var X=U.mostRecentCall.args;expect(X[0]).toBe(Y);expect(X[1]).toBe(Z)});it("should publish when an item is deselected",function(){var Y=R("Ben");W(Y);U.reset();H.deselect(Y);S.notify();var X=U.mostRecentCall.args;expect(X[0]).toBeNull();expect(X[1]).toBe(Y)})});describe("two way binding",function(){beforeEach(function(){V({viewModel:S,bind:{selection:"{foo}"}});S.bind("{foo}",U);S.notify()});describe("changing the selection",function(){it("should trigger the binding when adding a selection",function(){var Y=R("Don");W(Y);var X=U.mostRecentCall.args;expect(X[0]).toBe(Y);expect(X[1]).toBeUndefined()});it("should trigger the binding when changing the selection",function(){var Z=R("Ben"),Y=R("Nige");W(Z);U.reset();W(Y);var X=U.mostRecentCall.args;expect(X[0]).toBe(Y);expect(X[1]).toBe(Z)});it("should trigger the binding when an item is deselected",function(){var Y=R("Don");W(Y);U.reset();H.deselect(Y);S.notify();var X=U.mostRecentCall.args;expect(X[0]).toBeNull();expect(X[1]).toBe(Y)})});describe("changing the viewmodel value",function(){it("should select the record when setting the value",function(){var X=R("Phil");S.set("foo",X);S.notify();expect(H.isSelected(X)).toBe(true)});it("should select the record when updating the value",function(){var Y=R("Phil"),X=R("Ben");S.set("foo",Y);S.notify();S.set("foo",X);S.notify();expect(H.isSelected(Y)).toBe(false);expect(H.isSelected(X)).toBe(true)});it("should deselect when clearing the value",function(){var X=R("Evan");S.set("foo",X);S.notify();S.set("foo",null);S.notify();expect(H.isSelected(X)).toBe(false)})})})});describe("Locked grids",function(){describe("mouse cell selection",function(){it("should track across from locked to normal",function(){I(null,null,null,null,true);var S=O(1,1),R=O(3,3);jasmine.fireMouseEvent(S,"mousedown");jasmine.fireMouseEvent(S,"mousemove");jasmine.fireMouseEvent(R,"mousemove");expect(H.getSelected().isCells).toBe(true);expect(L.el.query("."+L.selectedCellCls).length).toBe(9);expect(H.getSelected().getCount()).toBe(9);expect(J(1,1)&&J(1,2)&&J(1,3)&&J(2,1)&&J(2,2)&&J(2,3)&&J(3,1)&&J(3,2)&&J(3,2)).toBe(true)})});describe("mouse row selection",function(){it("should track across from locked to normal",function(){I(null,null,null,null,true);var S=O(0,0),R=O(2,2);jasmine.fireMouseEvent(S,"mousedown");jasmine.fireMouseEvent(S,"mousemove");jasmine.fireMouseEvent(R,"mousemove");expect(H.getSelected().isRows).toBe(true);expect(L.el.query("."+L.selectedItemCls).length).toBe(6);expect(B(0)).toBe(true);expect(B(1)).toBe(true);expect(B(2)).toBe(true);expect(H.getSelected().getCount()).toBe(3);expect(H.getSelected().contains(N.getAt(0))).toBe(true);expect(H.getSelected().contains(N.getAt(1))).toBe(true);expect(H.getSelected().contains(N.getAt(2))).toBe(true)});it("should select a range of rows using click followed by shift+click",function(){I(null,null,null,null,true);var S=O(0,0),R=O(2,0);jasmine.fireMouseEvent(S,"click");jasmine.fireMouseEvent(R,"click",null,null,null,true);expect(H.getSelected().isRows).toBe(true);expect(L.el.query("."+L.selectedItemCls).length).toBe(6);expect(B(0)).toBe(true);expect(B(1)).toBe(true);expect(B(2)).toBe(true);expect(H.getSelected().getCount()).toBe(3);expect(H.getSelected().contains(N.getAt(0))).toBe(true);expect(H.getSelected().contains(N.getAt(1))).toBe(true);expect(H.getSelected().contains(N.getAt(2))).toBe(true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(H.getSelected().isColumns).toBe(true);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true)})});describe("locking a selected column",function(){it("should successfully deselect",function(){I(null,null,null,null,true);H.selectColumn(M[5]);expect(C(5)).toBe(true);A.lock(M[5]);M=A.getColumnManager().getColumns();expect(C(2)).toBe(true)})});describe("copying selected columns from locked grid",function(){it("should arrange the column data in column-ordinal order according to the outermost grid",function(){I(null,{plugins:"clipboard"},null,null,true);var S=A.findPlugin("clipboard"),R;H.selectColumn(M[2]);H.selectColumn(M[1],true);R=S.getCellData();expect(R).toEqual("1.1\t1.2\n2.1\t2.2\n3.1\t3.2\n4.1\t4.2\n5.1\t5.2\n6.1\t6.2\n7.1\t7.2\n8.1\t8.2\n9.1\t9.2\n10.1\t10.2")})})});describe("mouse column selection",function(){it("should select in both locked and normal sides",function(){I(null,null,null,null,true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(H.getSelected().isColumns).toBe(true);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true);jasmine.fireMouseEvent(M[2].el.dom,"click",0,0,0,false,true);expect(H.getSelected().getCount()).toBe(2);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length+L.el.query(M[2].getCellSelector()).length);expect(C(1)).toBe(true);expect(C(2)).toBe(true)});it("should select columns rage using click then shift+click",function(){I(null,null,null,null,true);jasmine.fireMouseEvent(M[1].el.dom,"click");expect(H.getSelected().isColumns).toBe(true);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length);expect(C(1)).toBe(true);jasmine.fireMouseEvent(M[4].el.dom,"click",0,0,0,true);expect(H.getSelected().getCount()).toBe(4);expect(L.el.query("."+L.selectedCellCls).length).toBe(L.el.query(M[1].getCellSelector()).length+L.el.query(M[2].getCellSelector()).length+L.el.query(M[3].getCellSelector()).length+L.el.query(M[4].getCellSelector()).length);expect(C(1)).toBe(true);expect(C(2)).toBe(true);expect(C(3)).toBe(true);expect(C(4)).toBe(true)})});describe("Buffered store",function(){function R(X,U){var T=X+U,W=[],V;for(V=X;V<T;++V){W.push({id:V,title:"Title"+V})}return W}function S(U){var X=Ext.Ajax.mockGetAllRequests(),T,W,V;while(X.length){T=X[0];W=T.options.params;V=R(W.start,W.limit);Ext.Ajax.mockComplete({status:200,responseText:Ext.encode({total:U||5000,data:V})});X=Ext.Ajax.mockGetAllRequests()}}beforeEach(function(){MockAjaxManager.addMethods()});afterEach(function(){MockAjaxManager.removeMethods()});it("should not throw an error",function(){I(null,null,null,{buffered:true,pageSize:100,proxy:{type:"ajax",url:"fakeUrl",reader:{type:"json",rootProperty:"data"}},data:null});N.loadPage(1);S()})});describe("Selection replication",function(){var T,Z,W,Y,X,V,U,S,R,a;beforeEach(function(){I(null,{plugins:"selectionreplicator"},{extensible:"y"},{numeric:true});X=Ext.clone(N.getAt(0).data);V=Ext.clone(N.getAt(1).data);U=Ext.clone(N.getAt(2).data);S=Ext.clone(N.getAt(5).data);R=Ext.clone(N.getAt(6).data);a=Ext.clone(N.getAt(7).data)});it("should align the extend handle upon column resize",function(){jasmine.fireMouseEvent(O(1,1),"click");var c=H.extensible.handle.getX();M[1].setWidth(M[1].getWidth()+100);var b=H.extensible.handle.getX();if(Ext.isIE8){expect(b).toBeWithin(2,c+100)}else{expect(b).toBe(c+100)}});describe("multiple selection",function(){describe("upwards",function(){it("should replicate the selection by incrementing the values",function(){T=O(3,2);Z=O(4,4);W=new Ext.grid.CellContext(A.view).setPosition(0,2),Y=new Ext.grid.CellContext(A.view).setPosition(2,4),N.getAt(0).set({field2:0,field3:0,field4:0});N.getAt(1).set({field2:0,field3:0,field4:0});N.getAt(2).set({field2:0,field3:0,field4:0});jasmine.fireMouseEvent(T,"mousedown");jasmine.fireMouseEvent(T,"mousemove");jasmine.fireMouseEvent(Z,"mousemove");jasmine.fireMouseEvent(Z,"mouseup");A.fireEvent("beforeselectionextend",A,H.getSelected(),{type:"rows",start:W,end:Y,rows:-3});expect(N.getAt(0).data).toEqual(X);expect(N.getAt(1).data).toEqual(V);expect(N.getAt(2).data).toEqual(U)})});describe("downwards",function(){it("should replicate the selection by incrementing the values",function(){T=O(3,2);Z=O(4,4);W=new Ext.grid.CellContext(A.view).setPosition(5,2),Y=new Ext.grid.CellContext(A.view).setPosition(7,4),N.getAt(5).set({field2:0,field3:0,field4:0});N.getAt(6).set({field2:0,field3:0,field4:0});N.getAt(7).set({field2:0,field3:0,field4:0});jasmine.fireMouseEvent(T,"mousedown");jasmine.fireMouseEvent(T,"mousemove");jasmine.fireMouseEvent(Z,"mousemove");jasmine.fireMouseEvent(Z,"mouseup");A.fireEvent("beforeselectionextend",A,H.getSelected(),{type:"rows",start:W,end:Y,rows:3});expect(N.getAt(5).data).toEqual(S);expect(N.getAt(6).data).toEqual(R);expect(N.getAt(7).data).toEqual(a)})})});describe("single selection",function(){describe("upwards",function(){it("should replicate the selection by repeating the values",function(){var b=N.getAt(3).data;W=new Ext.grid.CellContext(A.view).setPosition(0,2),Y=new Ext.grid.CellContext(A.view).setPosition(2,4),jasmine.fireMouseEvent(O(3,0),"click");A.fireEvent("beforeselectionextend",A,H.getSelected(),{type:"rows",start:W,end:Y,rows:-3});N.getAt(0).data.id=N.getAt(1).data.id=N.getAt(2).data.id=N.getAt(3).data.id;expect(N.getAt(0).data).toEqual(b);expect(N.getAt(1).data).toEqual(b);expect(N.getAt(2).data).toEqual(b)})});describe("downwards",function(){it("should replicate the selection by repeating the values",function(){var b=N.getAt(4).data;W=new Ext.grid.CellContext(A.view).setPosition(5,2),Y=new Ext.grid.CellContext(A.view).setPosition(7,4),jasmine.fireMouseEvent(O(4,0),"click");A.fireEvent("beforeselectionextend",A,H.getSelected(),{type:"rows",start:W,end:Y,rows:3});N.getAt(5).data.id=N.getAt(6).data.id=N.getAt(7).data.id=N.getAt(4).data.id;expect(N.getAt(5).data).toEqual(b);expect(N.getAt(6).data).toEqual(b);expect(N.getAt(7).data).toEqual(b)})})})});describe("reconfigure",function(){var R;beforeEach(function(){I(null,null,{checkboxSelect:true});R=Ext.clone(A.initialConfig.columns)});it("should re-insert the checkbox and row numberer columns on reconfigure",function(){var S=A.getVisibleColumnManager().getColumns();expect(S.length).toBe(R.length+2);A.reconfigure(null,R);expect(S.length).toBe(R.length+2)})})})