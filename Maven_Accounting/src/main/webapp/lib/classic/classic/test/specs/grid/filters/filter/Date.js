describe("Ext.grid.filters.filter.Date",function(){var A,G,L,Q,C,O,K,N,T,I,U,R,F,P,H=true,B=Ext.data.ProxyStore.prototype.load,V;function M(Y,W,X){H=false;L=new Ext.data.Store(Ext.apply({fields:["name","email","phone",{name:"dob",type:"date"}],data:[{name:"evan",dob:Ext.Date.parse("1992-12-12T12:30:01","c")},{name:"nige",dob:Ext.Date.parse("1992-12-11T11:30:01","c")},{name:"phil",dob:Ext.Date.parse("1992-12-10T10:30:01","c")},{name:"don",dob:Ext.Date.parse("1992-12-09T09:30:01","c")},{name:"alex",dob:Ext.Date.parse("1992-12-08T08:30:01","c")},{name:"ben",dob:Ext.Date.parse("1992-12-08T07:30:01","c")}],autoDestroy:true},X));A=new Ext.grid.Panel(Ext.apply({store:L,autoLoad:true,columns:[{dataIndex:"name",width:100},{dataIndex:"dob",width:100,filter:Ext.apply({type:"date",updateBuffer:0},Y)}],plugins:"gridfilters",height:200,width:400,renderTo:Ext.getBody()},W));G=A.filters;Q=A.columnManager.getHeaderByDataIndex("dob").filter;G=A.filters;H=true;L.flushLoad()}function D(W){N.setValue(new Date(W));jasmine.fireMouseEvent(N.eventEl.down(".x-datepicker-selected div").dom,"click")}function S(){var W=A.getColumnManager().getLast();O=A.headerCt;O.showMenuBy(null,W.triggerEl.dom,W);K=O.menu.items.last();K.activated=true;K.expandMenu(null,0);C=K.menu}function E(X){var W;if(!C){S()}W=C.down('[text="'+X+'"]');W.activated=true;W.expandMenu(null,0);N=W.menu.down("datepicker");T=N.el;I=T.down(".x-datepicker-header",true);U=T.down(".x-datepicker-selected",true);return N}beforeEach(function(){V=Ext.data.ProxyStore.prototype.load=function(){B.apply(this,arguments);if(H){this.flushLoad.apply(this,arguments)}return this}});function J(){Ext.data.ProxyStore.prototype.load=B;A=G=L=Q=C=O=K=N=T=I=U=R=F=P=Ext.destroy(A)}afterEach(J);describe("init",function(){it("should add a menu separator to the menu",function(){M();S();expect(C.down("menuseparator")).not.toBeNull()})});describe("setValue",function(){var W=Ext.Date.parse;it("should filter the store regardless of whether the menu has been created",function(){M();expect(L.data.length).toBe(6);Q.setValue({eq:Ext.Date.parse("1992-12-08T07:30:01","c")});expect(L.data.length).toBe(2)});it("should update the value of the date whenever called",function(){M();Q.createMenu();Q.setValue({eq:W("08/08/1992","d/m/Y")});Q.setValue({eq:W("26/09/2009","d/m/Y")});expect(Q.filter.eq.getValue()).toEqual(W("26/09/2009","d/m/Y"))})});describe("the filter",function(){it("should serialize the filter according to the dateFormat",function(){M();Q.setDateFormat("Y/m/d");Q.createMenu();var W=new Date(2010,0,1);Q.setValue({lt:W});expect(L.getFilters().first().serialize().value).toBe("2010/01/01")});it("should only compare the date part when using the before filter",function(){M(null,null,{remoteFilter:false});Q.createMenu();var W=new Date(1992,11,9);Q.setValue({lt:W});expect(L.getCount()).toBe(2);expect(L.getAt(0).get("name")).toBe("alex");expect(L.getAt(1).get("name")).toBe("ben")});it("should only compare the date part when using the after filter",function(){M(null,null,{remoteFilter:false});Q.createMenu();var W=new Date(1992,11,9);Q.setValue({gt:W});expect(L.getCount()).toBe(3);expect(L.getAt(0).get("name")).toBe("evan");expect(L.getAt(1).get("name")).toBe("nige");expect(L.getAt(2).get("name")).toBe("phil")});it("should only compare the date part when using the on filter",function(){M(null,null,{remoteFilter:false});Q.createMenu();var W=new Date(1992,11,9);Q.setValue({eq:W});expect(L.getCount()).toBe(1);expect(L.getAt(0).get("name")).toBe("don")})});describe("onMenuSelect handler and setFieldValue",function(){it("should correctly filter based upon picker selections",function(){M();E("Before","12/12/1992");D("12/10/1992");expect(L.getCount()).toBe(3);D("12/12/1992");expect(L.getCount()).toBe(5)})});describe("removing store filters, tri-filter",function(){beforeEach(function(){M();spyOn(Q,"onFilterRemove");L.getFilters().add({property:"dob",value:{eq:new Date()}})});it("should not throw if removing filters directly on the bound store",function(){expect(function(){L.clearFilter()}).not.toThrow()});it("should not call through to the delegated handler if the store filter was not generated by the class",function(){L.clearFilter();expect(Q.onFilterRemove).not.toHaveBeenCalled()});it("should not call through to the delegated handler when the store filter is replaced",function(){G.addFilter({type:"date",dataIndex:"dob",value:{eq:new Date()}});L.clearFilter();expect(Q.onFilterRemove).not.toHaveBeenCalled()});it("should call through to the delegated handler when if the store filter was generated by the class (when menu has been created)",function(){J();M({value:{eq:Ext.Date.parse("1992-12-12T12:30:01","c")}});S();spyOn(Q,"onFilterRemove");Q.addStoreFilter({id:"x-gridfilter-dob-eq",property:"dob",operator:"eq",value:Ext.Date.parse("1972-12-12T12:30:01","c")});expect(Q.onFilterRemove).toHaveBeenCalled()})});describe("adding a column filter, tri-filter",function(){describe("replacing an existing column filter",function(){it("should not throw",function(){M();expect(function(){G.addFilter({type:"string",value:"ben germane"})}).not.toThrow()});it("should replace the existing store filter",function(){var Y,X,a,W=Ext.Date.parse("1992-12-08T07:30:01","c"),Z=Ext.Date.parse("1992-12-10T10:30:01","c");M({value:{eq:W}});a=Q.getBaseIdPrefix()+"-eq";Y=L.getFilters();X=Y.getAt(0);expect(Y.length).toBe(1);expect(X.getId()).toBe(a);expect(X.getValue()).toBe(W);G.addFilter({type:"date",dataIndex:"dob",value:{eq:Z}});X=Y.getAt(0);expect(Y.length).toBe(1);expect(X.getId()).toBe(a);expect(X.getValue()).toBe(Z)})})});describe("showing the menu",function(){function W(X){it("should not add a filter to the store when shown",function(){M({active:X,value:[{on:new Date()}]});spyOn(Q,"addStoreFilter");S();expect(Q.addStoreFilter).not.toHaveBeenCalled()})}W(true);W(false)});describe("clearing filters",function(){it('should not recheck the root menu item ("Filters") when showing menu after clearing filters',function(){M();S();Q.setValue({lt:new Date()});expect(K.checked).toBe(true);O.getMenu().hide();G.clearFilters();S();expect(K.checked).toBe(false)})});describe("selecting using the UI",function(){var W;afterEach(function(){W=null});describe("the After datepicker",function(){function X(){R=E("Before");D("12/8/2014");expect(F.up("menuitem").checked).toBe(true);expect(R.up("menuitem").checked).toBe(true);expect(W.length).toBe(2);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-gt");expect(W.getAt(1).getId()).toBe(Q.getBaseIdPrefix()+"-lt")}function Y(){P=E("On");D("9/26/2009");expect(F.up("menuitem").checked).toBe(false);if(R){expect(R.up("menuitem").checked).toBe(false)}expect(P.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-eq")}beforeEach(function(){M();F=E("After");D("8/8/1992");W=L.getFilters()});it("should enable and activate after a selection is made",function(){expect(F.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-gt")});it("should support the enabling and activating of the Before bits if a supported Before selection is made",function(){X()});it("should disable and deactivate the After bits if an unsupported Before selection is made",function(){R=E("Before");D("8/7/1992");expect(F.up("menuitem").checked).toBe(false);expect(R.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-lt")});it("should disable and deactivate the After bits if an On selection is made",function(){Y()});it("should disable and deactivate the After and Before bits if an On selection is made",function(){X();Y()})});describe("the Before datepicker",function(){function X(){F=E("After");D("8/8/1992");expect(F.up("menuitem").checked).toBe(true);expect(R.up("menuitem").checked).toBe(true);expect(W.length).toBe(2);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-lt");expect(W.getAt(1).getId()).toBe(Q.getBaseIdPrefix()+"-gt")}function Y(){P=E("On");D("9/26/2009");expect(R.up("menuitem").checked).toBe(false);if(F){expect(F.up("menuitem").checked).toBe(false)}expect(P.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-eq")}beforeEach(function(){M();R=E("Before");D("12/8/2014");W=L.getFilters()});it("should enable and activate after a selection is made",function(){expect(R.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-lt")});it("should support the enabling and activating of the Before bits if a supported Before selection is made",function(){X()});it("should disable and deactivate the Before bits if an unsupported After selection is made",function(){F=E("After");D("12/9/2014");expect(R.up("menuitem").checked).toBe(false);expect(F.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-gt")});it("should disable and deactivate the Before bits if an On selection is made",function(){Y()});it("should disable and deactivate the After and Before bits if an On selection is made",function(){X();Y()})});describe("the On datepicker",function(){var Y;function X(a){Y=E(a);waitsFor(function(){return !!N.eventEl});runs(function(){D("8/8/1992");expect(Y.up("menuitem").checked).toBe(true);expect(P.up("menuitem").checked).toBe(false);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+(a==="After"?"-gt":"-lt"))})}function Z(){P=E("On");waitsFor(function(){return !!N.eventEl});runs(function(){D("9/26/2009");expect(R.up("menuitem").checked).toBe(false);if(F){expect(F.up("menuitem").checked).toBe(false)}expect(P.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-eq")})}beforeEach(function(){M();P=E("On");D("1/22/1972");W=L.getFilters()});afterEach(function(){Y=null});it("should enable and activate after a selection is made",function(){expect(P.up("menuitem").checked).toBe(true);expect(W.length).toBe(1);expect(W.getAt(0).getId()).toBe(Q.getBaseIdPrefix()+"-eq")});it("should disable and deactivate the On bits if an After selection is made",function(){X("After")});it("should disable and deactivate the On bits if an Before selection is made",function(){X("Before")})})});describe("the UI and the active state",function(){function W(X){describe("when "+X,function(){var Y=!X?"not":"";it("should "+Y+" check the Filters menu item",function(){M({active:X});S();expect(K.checked).toBe(X)});it("should set any field values that map to a configured value, Before and After",function(){M({active:X,value:{lt:Ext.Date.parse("1992-12-12T12:30:01","c"),gt:Ext.Date.parse("1992-12-08T07:30:01","c")}});E("Before");expect((I.textContent||I.innerText).replace(/\s/g,"")).toBe("December1992");expect((U.textContent||U.innerText).replace(/\s/g,"")).toBe("12");E("After");expect((I.textContent||I.innerText).replace(/\s/g,"")).toBe("December1992");expect((U.textContent||U.innerText).replace(/\s/g,"")).toBe("8")});it("should set any field values that map to a configured value, On",function(){M({active:X,value:{eq:Ext.Date.parse("1972-01-22T12:30:01","c")}});E("On");expect((I.textContent||I.innerText).replace(/\s/g,"")).toBe("January1972");expect((U.textContent||U.innerText).replace(/\s/g,"")).toBe("22")});describe("when a store filter is created",function(){it("should not update the filter collection twice",function(){var Z=0;M({active:X},{listeners:{filterchange:function(){++Z}}});E("On");Q.setValue({eq:Ext.Date.parse("1972-01-22T12:30:01","c")});expect(Z).toBe(1)})})})}W(true);W(false);describe("toggling active state on same filter",function(){it("should update the UI",function(){var X;M();E("Before");D("12/10/1992");X=Q.column;expect(K.checked).toBe(true);expect(X.hasCls(G.filterCls)).toBe(true);K.setChecked(false);expect(K.checked).toBe(false);expect(X.hasCls(G.filterCls)).toBe(false);D("12/09/1992");expect(K.checked).toBe(true);expect(X.hasCls(G.filterCls)).toBe(true)})})})})