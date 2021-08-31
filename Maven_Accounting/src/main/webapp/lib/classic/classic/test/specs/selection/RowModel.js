describe("Ext.selection.RowModel",function(){var B,K,G,L,D,M,A,N=true,E=Ext.data.ProxyStore.prototype.load,J,F=Ext.view.Table.prototype.selectedCellCls,C=Ext.view.Table.prototype.selectedItemCls;function I(O){return new Ext.data.Store(Ext.apply({fields:["name"],proxy:{type:"memory",data:A}},O))}function H(O,Q,P){G=new Ext.selection.RowModel(Q||{});B=new Ext.grid.Panel(Ext.apply({store:(O&&O.store)||I(P),columns:[{text:"Name",dataIndex:"name"}],selModel:G,height:200,width:200,renderTo:Ext.getBody()},O));L=B.getStore();if(!P||P.autoLoad!==false){if(L.load){L.load()}}K=B.getView();D=B.view.getVisibleColumnManager().getColumns()}beforeEach(function(){J=Ext.data.ProxyStore.prototype.load=function(){E.apply(this,arguments);if(N){this.flushLoad.apply(this,arguments)}return this};A=[{id:1,name:"Phil"},{id:2,name:"Ben"},{id:3,name:"Evan"},{id:4,name:"Don"},{id:5,name:"Nige"},{id:6,name:"Alex"}]});afterEach(function(){Ext.data.ProxyStore.prototype.load=E;Ext.destroy(B,G);A=L=B=G=null});it("should not select the row upon in-row navigation",function(){H({columns:[{text:"ID",dataIndex:"id"},{text:"Name",dataIndex:"name"}]});var O=K.getNavigationModel();O.setPosition(0,0,null,null,true);waitsFor(function(){return !!O.getPosition()});runs(function(){jasmine.fireKeyEvent(O.getPosition().getCell(true),"keydown",Ext.event.Event.RIGHT);expect(K.selModel.getSelection().length).toBe(0);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(false);jasmine.fireKeyEvent(O.getPosition().getCell(true),"keydown",Ext.event.Event.DOWN);expect(K.selModel.getSelection().length).toBe(1);expect(Ext.fly(K.getNode(1)).hasCls(K.selectedItemCls)).toBe(true)})});it("should render cells without the x-grid-cell-selected cls (EXTJSIV-17255)",function(){H();G.select(0);B.getStore().sort("name","ASC");expect(B.getView().getNode(2).firstChild).not.toHaveCls(F)});it("SINGLE select mode should not select on CTRL/click (EXTJS-18592)",function(){H({},{selType:"rowmodel",mode:"SINGLE",allowDeselect:true,toggleOnClick:false});M=B.view.getCell(0,D[0]);jasmine.fireMouseEvent(M,"click");var O=G.getSelection();expect(B.view.all.item(0).hasCls(C)).toBe(true);expect(O.length).toBe(1);expect(O[0]===B.store.getAt(0)).toBe(true);M=B.view.getCell(1,D[0]);jasmine.fireMouseEvent(M,"click",null,null,null,false,true);var O=G.getSelection();expect(B.view.all.item(0).hasCls(C)).toBe(true);expect(O.length).toBe(1);expect(O[0]===B.store.getAt(0)).toBe(true);expect(B.view.all.item(1).hasCls(C)).toBe(false)});it("should not allow deselect on SPACE if configured allowDeselect:false",function(){H({},{allowDeselect:false});M=B.view.getCell(0,D[0]);jasmine.fireMouseEvent(M,"click");var O=G.getSelection();expect(B.view.all.item(0).hasCls(C)).toBe(true);expect(O.length).toBe(1);expect(O[0]===B.store.getAt(0)).toBe(true);jasmine.fireKeyEvent(M,"keydown",Ext.event.Event.SPACE);O=G.getSelection();expect(B.view.all.item(0).hasCls(C)).toBe(true);expect(O.length).toBe(1);expect(O[0]===B.store.getAt(0)).toBe(true);G.allowDeselect=true;jasmine.fireKeyEvent(M,"keydown",Ext.event.Event.SPACE);O=G.getSelection();expect(B.view.all.item(0).hasCls(C)).toBe(false);expect(O.length).toBe(0)});describe("deselectOnContainerClick",function(){it("should default to false",function(){H();expect(G.deselectOnContainerClick).toBe(false)});describe("deselectOnContainerClick: false",function(){it("should not deselect when clicking the container",function(){H(null,{deselectOnContainerClick:false});G.select(0);jasmine.fireMouseEvent(K.getEl(),"click",180,180);expect(G.isSelected(0)).toBe(true)})});describe("deselectOnContainerClick: true",function(){it("should deselect when clicking the container",function(){H(null,{deselectOnContainerClick:true});G.select(0);jasmine.fireMouseEvent(K.getEl(),"click",180,180);expect(G.isSelected(0)).toBe(false)})})});describe("pruneRemoved",function(){describe("pruneRemoved: true",function(){it("should remove records from selection by default when removed from the store",function(){H({bbar:{xtype:"pagingtoolbar"}},null,{autoLoad:false,pageSize:2});L.proxy.enablePaging=true;var O=B.down("pagingtoolbar"),P;O.setStore(L);L.loadPage(1);G.select(0);P=G.getSelection();expect(P.length).toBe(1);expect(P[0]===L.getAt(0)).toBe(true);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(true);O.moveNext();expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(false);O.movePrevious();P=G.getSelection();expect(P.length).toBe(0);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(false)})});describe("pruneRemoved: false",function(){it("should NOT remove records from selection if pruneRemoved:false when they are removed from the store",function(){H({bbar:{xtype:"pagingtoolbar"}},{pruneRemoved:false},{autoLoad:false,pageSize:2});L.proxy.enablePaging=true;var O=B.down("pagingtoolbar"),P;O.setStore(L);L.loadPage(1);G.select(0);P=G.getSelection();expect(P.length).toBe(1);expect(P[0]===L.getAt(0)).toBe(true);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(true);O.moveNext();expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(false);O.movePrevious();P=G.getSelection();expect(P.length).toBe(1);expect(P[0]===L.getAt(0)).toBe(true);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(true)})})});describe("selecting a range",function(){it("should allow a range to be selected after programmatically selecting the first selection",function(){H({},{mode:"MULTI"});G.select(0);G.selectWithEvent(B.store.getAt(2),{shiftKey:true});expect(G.selected.length).toBe(3)});it("should allow a range to be selected when shift is held down when making first selection",function(){H({},{mode:"MULTI"});G.selectWithEvent(B.store.getAt(2),{shiftKey:true});G.selectWithEvent(B.store.getAt(0),{shiftKey:true});expect(G.selected.length).toBe(3)})});describe("contextmenu",function(){beforeEach(function(){H({},{mode:"MULTI"})});it("should not deselect the range when right-clicking over a previously selected record",function(){G.select(4);G.selectWithEvent(B.store.getAt(0),{shiftKey:true});var O=B.view.getCell(2,D[0]);jasmine.fireMouseEvent(O,"mousedown",null,null,2);expect(G.selected.length).toBe(5)});it("should deselect the range when right-clicking over a record not previously selected",function(){G.select(4);G.selectWithEvent(B.store.getAt(0),{shiftKey:true});var O=B.view.getCell(5,B.view.getVisibleColumnManager().getColumns()[0]);jasmine.fireMouseEvent(O,"mousedown",null,null,2);expect(G.selected.length).toBe(1)})});describe("selected cls",function(){var Q,S,V,R,X,W;function O(a){var Z=B.getView(),b=Z.getNode(a),Y=Z.selectedItemCls;return Ext.fly(b).hasCls(Y)}function U(Y){expect(O(Y)).toBe(true)}function P(Y){expect(O(Y)).toBe(false)}function T(){Q=L.getAt(0);S=L.getAt(1);V=L.getAt(2);R=L.getAt(3);X=L.getAt(4);W=L.getAt(5)}afterEach(function(){Q=S=V=R=X=W=null});describe("before render",function(){beforeEach(function(){H({renderTo:null},{mode:"MULTI"});T()});it("should add the selected cls to a selected record",function(){G.select(Q);B.render(Ext.getBody());U(Q);P(S);P(V);P(R);P(X);P(W)});it("should add the selected cls to multiple selected records",function(){G.select([Q,V,W]);B.render(Ext.getBody());U(Q);P(S);U(V);P(R);P(X);U(W)});it("should not add the selected cls to deselected records",function(){G.select(Q);G.deselect(Q);B.render(Ext.getBody());P(Q)})});describe("after render",function(){beforeEach(function(){H(null,{mode:"MULTI"});T()});it("should add the selected cls to a selected record",function(){G.select(Q);U(Q);P(S);P(V);P(R);P(X);P(W)});it("should add the selected cls to multiple selected records",function(){G.select([Q,V,W]);U(Q);P(S);U(V);P(R);P(X);U(W)});it("should not add the selected cls to deselected records",function(){G.select(Q);G.deselect(Q);P(Q)})});it("should maintain the selected cls after a cell update",function(){H();T();G.select(Q);Q.set("name","Foo");U(Q)});it("should remain selected after a whole row update",function(){H();T();G.select(Q);Q.beginEdit();Q.set("name","Foo");Q.endEdit(true);Q.commit();U(Q)});it("should maintain the selected cls after being sorted",function(){H();T();G.select(Q);L.sort("name","ASC");U(Q)})});it("should remove selections the selection is filtered out of a tree store",function(){var O=Ext.widget({xtype:"treepanel",renderTo:document.body,rootVisible:false,root:{expanded:true,children:[{text:"foo",leaf:true},{text:"bar",leaf:true}]}});O.selModel.select(0);O.store.filter({property:"text",value:"bar"});expect(O.selModel.getSelection().length).toBe(0);O.destroy()});describe("view model selection",function(){var P,Q;beforeEach(function(){Q=jasmine.createSpy();P=new Ext.app.ViewModel()});afterEach(function(){Q=G=P=null});function R(S){G.select(S);P.notify()}function O(T){var S=L.findExact("name",T);return L.getAt(S)}describe("reference",function(){beforeEach(function(){H({reference:"userList",viewModel:P});P.bind("{userList.selection}",Q);P.notify()});it("should publish null by default",function(){var S=Q.mostRecentCall.args;expect(S[0]).toBeNull();expect(S[1]).toBeUndefined()});it("should publish the value when selected",function(){var T=O("Ben");R(T);var S=Q.mostRecentCall.args;expect(S[0]).toBe(T);expect(S[1]).toBeNull()});it("should publish when the selection is changed",function(){var U=O("Ben"),T=O("Nige");R(U);Q.reset();R(T);var S=Q.mostRecentCall.args;expect(S[0]).toBe(T);expect(S[1]).toBe(U)});it("should publish when an item is deselected",function(){var T=O("Ben");R(T);Q.reset();G.deselect(T);P.notify();var S=Q.mostRecentCall.args;expect(S[0]).toBeNull();expect(S[1]).toBe(T)})});describe("two way binding",function(){beforeEach(function(){H({viewModel:P,bind:{selection:"{foo}"}});P.bind("{foo}",Q);P.notify()});describe("changing the selection",function(){it("should trigger the binding when adding a selection",function(){var T=O("Don");R(T);var S=Q.mostRecentCall.args;expect(S[0]).toBe(T);expect(S[1]).toBeUndefined()});it("should trigger the binding when changing the selection",function(){var U=O("Ben"),T=O("Nige");R(U);Q.reset();R(T);var S=Q.mostRecentCall.args;expect(S[0]).toBe(T);expect(S[1]).toBe(U)});it("should trigger the binding when an item is deselected",function(){var T=O("Don");R(T);Q.reset();G.deselect(T);P.notify();var S=Q.mostRecentCall.args;expect(S[0]).toBeNull();expect(S[1]).toBe(T)})});describe("changing the viewmodel value",function(){it("should select the record when setting the value",function(){var S=O("Phil");P.set("foo",S);P.notify();expect(G.isSelected(S)).toBe(true)});it("should select the record when updating the value",function(){var T=O("Phil"),S=O("Ben");P.set("foo",T);P.notify();P.set("foo",S);P.notify();expect(G.isSelected(T)).toBe(false);expect(G.isSelected(S)).toBe(true)});it("should deselect when clearing the value",function(){var S=O("Evan");P.set("foo",S);P.notify();P.set("foo",null);P.notify();expect(G.isSelected(S)).toBe(false)})});describe("reloading the store",function(){beforeEach(function(){MockAjaxManager.addMethods();R(O("Phil"));Q.reset();L.setProxy({type:"ajax",url:"fake"});L.load()});afterEach(function(){MockAjaxManager.removeMethods()});describe("when the selected record is in the result set",function(){it("should trigger the selection binding",function(){Ext.Ajax.mockComplete({status:200,responseText:Ext.encode(A.slice(0,4))});P.notify();expect(Q.callCount).toBe(1);expect(Q.mostRecentCall.args[0]).toBe(L.getAt(0))})});describe("when the selected record is not in the result set",function(){it("should trigger the selection binding",function(){Ext.Ajax.mockComplete({status:200,responseText:"[]"});P.notify();expect(Q.callCount).toBe(1);expect(Q.mostRecentCall.args[0]).toBeNull()})})})})});describe("chained stores",function(){it("should remove records from selection by default when removed from source",function(){H({bbar:{xtype:"pagingtoolbar"},store:new Ext.data.ChainedStore({source:I()})});var Q=L.getSource();Q.load();var O=L.getAt(0);G.select(0);var P=G.getSelection();expect(P.length).toBe(1);expect(P[0]===O).toBe(true);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(true);Q.remove(O);P=G.getSelection();expect(P.length).toBe(0);expect(Ext.fly(K.getNode(0)).hasCls(K.selectedItemCls)).toBe(false)})})})