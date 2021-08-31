describe("Ext.grid.header.Container",function(){var C=function(E,D){A=Ext.create("Ext.data.Store",Ext.apply({storeId:"simpsonsStore",fields:["name","email","phone"],data:{"items":[{"name":"Lisa","email":"lisa@simpsons.com","phone":"555-111-1224"},{"name":"Bart","email":"bart@simpsons.com","phone":"555-222-1234"},{"name":"Homer","email":"homer@simpsons.com","phone":"555-222-1244"},{"name":"Marge","email":"marge@simpsons.com","phone":"555-222-1254"}]},proxy:{type:"memory",reader:{type:"json",rootProperty:"items"}}},E));B=Ext.create("Ext.grid.Panel",Ext.apply({title:"Simpsons",store:A,columns:[{header:"Name",dataIndex:"name",width:100},{header:"Email",dataIndex:"email",flex:1},{header:"Phone",dataIndex:"phone",flex:1,hidden:true}],height:200,width:400,renderTo:Ext.getBody()},D))},A,B;afterEach(function(){A.destroy();B=A=Ext.destroy(B);Ext.state.Manager.clear("foo")});describe("column menu showing",function(){it("should show the menu on trigger click",function(){var D,E;runs(function(){C({},{renderTo:Ext.getBody()});D=B.columns[0];D.triggerEl.show();jasmine.fireMouseEvent(D.triggerEl.dom,"click");E=D.activeMenu;expect(E.isVisible()).toBe(true);expect(E.containsFocus).toBeFalsy();jasmine.fireMouseEvent(D.triggerEl.dom,"mousedown");expect(E.isVisible()).toBe(false);D.el.focus();jasmine.fireKeyEvent(D.el.dom,"keydown",Ext.event.Event.DOWN)});waitsFor(function(){return E.isVisible()&&E.containsFocus})})});describe("columnManager delegations",function(){it("should allow columns to call methods on the ColumnManager",function(){var D;C({},{renderTo:Ext.getBody()});D=B.columns[0];expect(D.getHeaderIndex(D)).toBe(0);expect(D.getHeaderAtIndex(0)).toBe(D);expect(D.getVisibleHeaderClosestToIndex(0)).toBe(D)})});describe("gridVisibleColumns",function(){it("should keep track of state information for visible grid columns",function(){var D=[{header:"Name",headerId:"a",dataIndex:"name",width:100},{header:"Email",headerId:"b",dataIndex:"email",flex:1},{header:"Phone",headerId:"c",dataIndex:"phone",flex:1,hidden:true}];new Ext.state.Provider();C({},{columns:D,stateful:true,stateId:"foo"});B.columns[2].show();B.saveState();Ext.destroy(B);C({},{columns:D,stateful:true,stateId:"foo"});expect(B.headerCt.gridVisibleColumns.length).toBe(3)});it("should keep track of state information for visible grid columns when moved",function(){var D=[{header:"Name",headerId:"a",dataIndex:"name",width:100},{header:"Email",headerId:"b",dataIndex:"email",flex:1},{header:"Phone",headerId:"c",dataIndex:"phone",flex:1,hidden:true}];new Ext.state.Provider();C({},{columns:D,stateful:true,stateId:"foo"});B.columns[2].show();B.headerCt.move(2,0);B.saveState();Ext.destroy(B);C({},{columns:D,stateful:true,stateId:"foo"});expect(B.headerCt.gridVisibleColumns.length).toBe(3);expect(B.headerCt.gridVisibleColumns[0].dataIndex).toBe("phone")});it("should insert new columns into their correct new ordinal position after state restoration",function(){var D=[{header:"Email",headerId:"b",dataIndex:"email",flex:1},{header:"Phone",headerId:"c",dataIndex:"phone",flex:1}],E=[{header:"Name",headerId:"a",dataIndex:"name",width:100},{header:"Email",headerId:"b",dataIndex:"email",flex:1},{header:"Phone",headerId:"c",dataIndex:"phone",flex:1}];new Ext.state.Provider();C({},{columns:D,stateful:true,stateId:"foo"});B.headerCt.move(1,0);B.saveState();Ext.destroy(B);C({},{columns:E,stateful:true,stateId:"foo"});expect(B.headerCt.gridVisibleColumns[0].dataIndex).toBe("name");expect(B.headerCt.gridVisibleColumns[1].dataIndex).toBe("phone");expect(B.headerCt.gridVisibleColumns[2].dataIndex).toBe("email")})});describe("non-column descendants of headerCt",function(){describe("headerCt events",function(){var E,D;beforeEach(function(){C(null,{columns:[{header:"Name",dataIndex:"name",width:100},{header:"Email",dataIndex:"email",flex:1,items:[{xtype:"textfield"}]}]});E=B.headerCt;D=E.down("textfield")});afterEach(function(){E=D=null});it("should not throw in reaction to a delegated keydown event",function(){jasmine.fireKeyEvent(D.inputEl,"keydown",13);expect(function(){var F={getTarget:function(){return D.inputEl.dom}};E.onHeaderActivate(F)}).not.toThrow()});it("should not react to keydown events delegated from the headerCt",function(){var F=false,G=function(){F=true};E.on("sortchange",G);jasmine.fireKeyEvent(D.inputEl,"keydown",13);expect(F).toBe(false)})})});describe("keyboard events",function(){beforeEach(function(){C()});it("should focus first column header on Home key",function(){jasmine.syncPressKey(B.headerCt.el,"home");jasmine.expectFocused(B.headerCt.gridVisibleColumns[0])});it("should focus last column header on End key",function(){jasmine.syncPressKey(B.headerCt.el,"end");jasmine.expectFocused(B.headerCt.gridVisibleColumns[1])})});describe("Disabling column hiding",function(){beforeEach(function(){C()});it("should disable hiding the last visible column",function(){var I,E=B.columns[0],H,D,G,F;E.triggerEl.show();jasmine.fireMouseEvent(E.triggerEl.dom,"click");I=E.activeMenu;H=I.child("#columnItem");jasmine.fireMouseEvent(H.el.dom,"mouseover");waitsFor(function(){D=H.menu;return D&&D.isVisible()});runs(function(){G=D.child("[text=Name]");F=D.child("[text=Email]");jasmine.fireMouseEvent(G.el.dom,"click")});waitsFor(function(){return F.disabled})})})})