describe("Ext.selection.CheckboxModel",function(){var A,L,O,D,I,M,F,P,Q=true,G=Ext.data.ProxyStore.prototype.load,K;function J(S,R){D=new Ext.selection.CheckboxModel(S);D.getHeaderCheckbox=function(){return this.views[0].headerCt.child("gridcolumn[isCheckerHd]")};A=new Ext.grid.Panel(Ext.apply({store:O,columns:[{text:"name",flex:1,sortable:true,dataIndex:"name"}],columnLines:true,selModel:D,width:300,height:300,renderTo:Ext.getBody()},R));L=A.view}beforeEach(function(){K=Ext.data.ProxyStore.prototype.load=function(){G.apply(this,arguments);if(Q){this.flushLoad.apply(this,arguments)}return this};Ext.define("spec.CheckboxModel",{extend:"Ext.data.Model",fields:[{name:"name"}]});O=Ext.create("Ext.data.Store",{model:"spec.CheckboxModel",proxy:"memory",data:I||[{id:1,name:"Don"},{id:2,name:"Evan"},{id:3,name:"Nige"}]});M=O.getById(1);F=O.getById(2);P=O.getById(3)});afterEach(function(){Ext.data.ProxyStore.prototype.load=G;M=F=P=I=null;A.destroy();D.destroy();O.destroy();Ext.undefine("spec.CheckboxModel");Ext.data.Model.schema.clear()});function C(S,T){var R=S.getHeaderCheckbox();expect(R.hasCls(S.checkerOnCls)).toBe(T)}function H(){jasmine.fireMouseEvent(D.getHeaderCheckbox().el.dom,"click",10,10)}function B(S){var R=L.getCellByPosition({row:S,column:0});L.focus();jasmine.fireMouseEvent(R.down(D.checkSelector),"click")}function N(S,T){var R=L.getCellByPosition({row:S,column:T});L.focus();jasmine.fireMouseEvent(R,"click")}function E(T,V,S,W,U){var R=A.getView().getCellByPosition({row:T,column:0});jasmine.fireKeyEvent(R.down(D.checkSelector),"keydown",V,S,W,U)}describe("column insertion",function(){var R;afterEach(function(){R=null});it("should ignore any xtype defaults and insert a gridcolumn",function(){J(null,{columns:{defaults:{xtype:"widgetcolumn",widget:{xtype:"button"}},items:[{dataIndex:"name"}]}});var S=A.getColumnManager().getColumns();expect(S[0].$className).toBe("Ext.grid.column.Column");expect(S[0].isCheckerHd).toBe(true)});describe("without locking",function(){beforeEach(function(){R=[{dataIndex:"name"},{dataIndex:"name"},{dataIndex:"name"}]});it("should insert the column at the start by default",function(){J(null,{columns:R});var T=A.getColumnManager().getColumns(),S=T[0];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(T.length).toBe(4)});it("should insert the column at the start with injectCheckbox: 'first'",function(){J({injectCheckbox:"first"},{columns:R});var T=A.getColumnManager().getColumns(),S=T[0];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(T.length).toBe(4)});it("should insert the column at the end with injectCheckbox: 'last'",function(){J({injectCheckbox:"last"},{columns:R});var T=A.getColumnManager().getColumns(),S=T[3];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(T.length).toBe(4)});it("should insert the column at the specified index",function(){J({injectCheckbox:1},{columns:R});var T=A.getColumnManager().getColumns(),S=T[1];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(T.length).toBe(4)})});describe("with locking",function(){beforeEach(function(){R=[{dataIndex:"name",locked:true},{dataIndex:"name",locked:true},{dataIndex:"name",locked:true},{dataIndex:"name"},{dataIndex:"name"},{dataIndex:"name"}]});it("should insert the column at the start by default",function(){J(null,{columns:R});var T=A.getColumnManager().getColumns(),S=T[0];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(A.normalGrid.query("[isCheckerHd]").length).toBe(0);expect(T.length).toBe(7)});it("should insert the column at the start with injectCheckbox: 'first'",function(){J({injectCheckbox:"first"},{columns:R});var T=A.getColumnManager().getColumns(),S=T[0];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(A.normalGrid.query("[isCheckerHd]").length).toBe(0);expect(T.length).toBe(7)});it("should insert the column at the end with injectCheckbox: 'last'",function(){J({injectCheckbox:"last"},{columns:R});var T=A.getColumnManager().getColumns(),S=T[3];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(A.normalGrid.query("[isCheckerHd]").length).toBe(0);expect(T.length).toBe(7)});it("should insert the column at the specified index",function(){J({injectCheckbox:1},{columns:R});var T=A.getColumnManager().getColumns(),S=T[1];expect(S.isCheckerHd).toBe(true);expect(A.query("[isCheckerHd]").length).toBe(1);expect(A.normalGrid.query("[isCheckerHd]").length).toBe(0);expect(T.length).toBe(7)})})});describe("multiple selection",function(){beforeEach(function(){J()});describe("by clicking",function(){(Ext.isIE?xit:it)("should select unselected records on click, and deselect selected records on click",function(){A.focus();waitsFor(function(){return L.cellFocused});runs(function(){B(0);B(1);B(2)});waitsFor(function(){return D.getSelection().length===3},"all three records to be selected");runs(function(){B(1)});waitsFor(function(){return D.getSelection().length===2},"the first record to be deselected")})});describe("by key navigation",function(){it("should select unselected records on ctrl+SPACE, and deselect selected records on ctrl+SPACE",function(){A.view.getNavigationModel().setPosition(0);expect(D.getSelection().length).toBe(0);E(0,Ext.event.Event.SPACE);expect(D.getSelection().length).toBe(1);E(0,Ext.event.Event.DOWN,false,true);expect(D.getSelection().length).toBe(1);E(1,Ext.event.Event.DOWN,false,true);E(2,Ext.event.Event.SPACE);expect(D.getSelection().length).toBe(2);E(2,Ext.event.Event.UP,false,true);E(1,Ext.event.Event.UP,false,true);E(0,Ext.event.Event.SPACE);expect(D.getSelection().length).toBe(1)})})});describe("header state",function(){beforeEach(function(){J()});it("should be initially unchecked",function(){C(D,false)});it("should be unchecked if there are no records",function(){O.removeAll();C(D,false)});it("should check header when all rows are selected",function(){C(D,false);D.select(M,true);C(D,false);D.select(F,true);C(D,false);D.select(P,true);C(D,true)});it("should uncheck header when any row is deselected",function(){D.selectAll();C(D,true);D.selectAll();D.deselect(M);C(D,false);D.selectAll();D.deselect(F);C(D,false);D.selectAll();D.deselect(P);C(D,false)});describe("loading",function(){it("should keep the header checked when reloaded and all items were checked",function(){D.selectAll();C(D,true);O.load();C(D,true)});it("should keep the header checked when reloaded and loading a subset of items",function(){D.selectAll();C(D,true);O.getProxy().setData([{id:1,name:"Don"}]);O.load();C(D,true)});it("should be unchecked when the loaded items do not match",function(){D.selectAll();C(D,true);O.getProxy().setData([{id:4,name:"Foo"}]);O.load();C(D,false)})});it("should uncheck header when an unchecked record is added",function(){D.selectAll();C(D,true);O.add({name:"Marcelo"});C(D,false)});it("should check header when last unchecked record is removed before rows are rendered",function(){D.select(M,true);D.select(F,true);C(D,false);O.removeAt(O.find("name","Nige"));waitsFor(function(){return A.view.viewReady});runs(function(){C(D,true)})});it("should check header when last unchecked record is removed after rows are rendered",function(){D.select(M,true);D.select(F,true);C(D,false);waitsFor(function(){return A.view.viewReady});runs(function(){O.remove(P);C(D,true)})})});describe("check all",function(){describe('mode="SINGLE"',function(){it("should not render the header checkbox by default",function(){J({mode:"SINGLE"});expect(D.getHeaderCheckbox()).toBe(null)});it("should not render the header checkbox by config",function(){expect(function(){J({mode:"SINGLE",showHeaderCheckbox:true})}).toThrow("The header checkbox is not supported for SINGLE mode selection models.")})});describe('mode="MULTI"',function(){beforeEach(function(){J()});it("should check all when no record is checked",function(){C(D,false);H();C(D,true);expect(D.isSelected(M)).toBe(true);expect(D.isSelected(F)).toBe(true);expect(D.isSelected(P)).toBe(true)});it("should check all when some records are checked",function(){C(D,false);D.select(M,true);D.select(P,true);H();C(D,true);expect(D.isSelected(M)).toBe(true);expect(D.isSelected(F)).toBe(true);expect(D.isSelected(P)).toBe(true)})})});describe("uncheck all",function(){beforeEach(function(){J()});it("should uncheck all when all records are checked",function(){D.select(M,true);D.select(F,true);D.select(P,true);C(D,true);H();C(D,false);expect(D.isSelected(M)).toBe(false);expect(D.isSelected(F)).toBe(false);expect(D.isSelected(P)).toBe(false)})});describe("checkOnly",function(){function R(U,T){return A.getView().getCellByPosition({row:U,column:T})}function S(U,T){J({checkOnly:U,mode:T})}describe("mode: multi",function(){describe("with checkOnly: true",function(){beforeEach(function(){S(true,"MULTI")});it("should not select when clicking on the row",function(){jasmine.fireMouseEvent(R(0,1),"click");expect(D.isSelected(M)).toBe(false)});it("should not select when calling selectByPosition on a cell other than the checkbox cell",function(){D.selectByPosition({row:0,column:1});expect(D.isSelected(M)).toBe(false)});it("should not select when navigating with keys",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.LEFT);expect(D.isSelected(M)).toBe(false);jasmine.fireKeyEvent(R(0,0),"keydown",Ext.event.Event.RIGHT);expect(D.isSelected(M)).toBe(false)});it("should select when clicking on the checkbox",function(){var T=R(0,0).down(D.checkSelector);jasmine.fireMouseEvent(T,"click");expect(D.isSelected(M)).toBe(true)});it("should select when pressing space with the checker focused",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.LEFT);expect(D.isSelected(M)).toBe(false);jasmine.fireKeyEvent(R(0,0),"keydown",Ext.event.Event.SPACE);expect(D.isSelected(M)).toBe(true)})});describe("with checkOnly: false",function(){beforeEach(function(){S(false,"MULTI")});it("should select when clicking on the row",function(){jasmine.fireMouseEvent(R(0,1),"click");expect(D.isSelected(M)).toBe(true)});it("should select when calling selectByPosition on a cell other than the checkbox cell",function(){D.selectByPosition({row:0,column:1});expect(D.isSelected(M)).toBe(true)});it("should select when navigating with keys",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.DOWN);expect(D.isSelected(F)).toBe(true)});it("should select when clicking on the checkbox",function(){var T=R(0,0).down(D.checkSelector);jasmine.fireMouseEvent(T,"click");expect(D.isSelected(M)).toBe(true)})})});describe("mode: single",function(){describe("with checkOnly: true",function(){beforeEach(function(){S(true,"SINGLE")});it("should not select when clicking on the row",function(){jasmine.fireMouseEvent(R(0,1),"click");expect(D.isSelected(M)).toBe(false)});it("should not select when navigating with keys",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.LEFT);expect(D.isSelected(M)).toBe(false);jasmine.fireKeyEvent(R(0,0),"keydown",Ext.event.Event.RIGHT);expect(D.isSelected(M)).toBe(false)});it("should select when clicking on the checkbox",function(){var T=R(0,0).down(D.checkSelector);jasmine.fireMouseEvent(T,"click");expect(D.isSelected(M)).toBe(true)});it("should select when pressing space with the checker focused",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.LEFT);expect(D.isSelected(M)).toBe(false);jasmine.fireKeyEvent(R(0,0),"keydown",Ext.event.Event.SPACE);expect(D.isSelected(M)).toBe(true)})});describe("with checkOnly: false",function(){beforeEach(function(){S(false,"SINGLE")});it("should select when clicking on the row",function(){jasmine.fireMouseEvent(R(0,1),"click");expect(D.isSelected(M)).toBe(true)});it("should select when navigating with keys",function(){jasmine.fireMouseEvent(R(0,1),"click");jasmine.fireKeyEvent(R(0,1),"keydown",Ext.event.Event.DOWN);expect(D.isSelected(F)).toBe(true)});it("should select when clicking on the checkbox",function(){var T=R(0,0).down(D.checkSelector);jasmine.fireMouseEvent(T,"click");expect(D.isSelected(M)).toBe(true)})})})});describe("event selection",function(){var R,V,T;function W(){R=jasmine.createSpy();V=jasmine.createSpy();T=jasmine.createSpy();D.on("selectionchange",R);D.on("select",V);D.on("deselect",T)}function X(Y){var Z=R.mostRecentCall.args;expect(R.callCount).toBe(1);expect(Z[0]).toBe(D);expect(Z[1]).toEqual(Y)}function S(Y){var Z=V.mostRecentCall.args;expect(V.callCount).toBe(1);expect(Z[0]).toBe(D);expect(Z[1]).toBe(Y)}function U(Y){var Z=T.mostRecentCall.args;expect(T.callCount).toBe(1);expect(Z[0]).toBe(D);expect(Z[1]).toBe(Y)}afterEach(function(){R=V=T=null});describe("multi",function(){beforeEach(function(){J({mode:"MULTI"})});describe("selection when clicking on the checkbox",function(){describe("on a selected record",function(){it("should deselect when there are no other selections",function(){D.select(M);W();B(0);expect(D.isSelected(M)).toBe(false);X([]);U(M);expect(V).not.toHaveBeenCalled()});it("should deselect and keep existing selections",function(){D.selectAll();W();B(0);expect(D.isSelected(M)).toBe(false);X([F,P]);U(M);expect(V).not.toHaveBeenCalled()})});describe("on an unselected record",function(){it("should select the record when there are no other selections",function(){W();B(0);expect(D.isSelected(M)).toBe(true);X([M]);S(M);expect(T).not.toHaveBeenCalled()});it("should select and keep existing selections",function(){D.select([F,P]);W();B(0);expect(D.isSelected(M)).toBe(true);X([F,P,M]);S(M);expect(T).not.toHaveBeenCalled()})})});describe("with shiftKey",function(){var Y;beforeEach(function(){Y=O.add({id:4,name:"Phil"})[0]});it("should deselect everything past & including the clicked item",function(){D.selectAll();var a=A.getView(),Z;N(0,1);spyOn(a,"processUIEvent").andCallFake(function(b){if(b.type==="click"){b.shiftKey=true}Ext.grid.View.prototype.processUIEvent.apply(a,arguments)});N(2,1);N(1,1);expect(D.isSelected(M)).toBe(true);expect(D.isSelected(F)).toBe(true);expect(D.isSelected(P)).toBe(false);expect(D.isSelected(Y)).toBe(false)})})});describe("single",function(){beforeEach(function(){J({mode:"SINGLE"})});describe("on the checkbox",function(){it("should select the record on click",function(){B(0);expect(D.isSelected(M)).toBe(true)});it("should deselect any selected records",function(){B(0);B(1);expect(D.isSelected(M)).toBe(false);expect(D.isSelected(F)).toBe(true)})});describe("on the row",function(){it("should select the record on click",function(){B(0);expect(D.isSelected(M)).toBe(true)});it("should deselect any selected records",function(){B(0);B(1);expect(D.isSelected(M)).toBe(false);expect(D.isSelected(F)).toBe(true)})})})})})