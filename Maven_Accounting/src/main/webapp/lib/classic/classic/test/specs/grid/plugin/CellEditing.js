describe("Ext.grid.plugin.CellEditing",function(){var N,G,A,K,F,C,L,M=9,O=true,E=Ext.data.ProxyStore.prototype.load,I;function H(S,P,R,Q){N=new Ext.data.Store(Ext.apply({fields:["name","email","phone"],data:[{"name":"Lisa","email":"lisa@simpsons.com","phone":"555-111-1224","age":14},{"name":"Bart","email":"bart@simpsons.com","phone":"555-222-1234","age":12},{"name":"Homer","email":"homer@simpsons.com","phone":"555-222-1244","age":44},{"name":"Marge","email":"marge@simpsons.com","phone":"555-222-1254","age":41}],autoDestroy:true},R));G=new Ext.grid.plugin.CellEditing(S);A=new Ext.grid.Panel(Ext.apply({columns:[{header:"Name",dataIndex:"name",editor:"textfield",locked:Q},{header:"Email",dataIndex:"email",flex:1,editor:{xtype:"textareafield",allowBlank:false,grow:true}},{header:"Phone",dataIndex:"phone",editor:"textfield"},{header:"Age",dataIndex:"age",editor:"textfield"}],store:N,selModel:"cellmodel",plugins:[G],width:200,height:400,renderTo:Ext.getBody()},P));K=A.view}function J(Q,P){F=N.getAt(Q||0);C=A.columns[P||0];G.startEdit(F,C);L=C.field}function D(Q,P){jasmine.fireKeyEvent(Q,Ext.supports.SpecialKeyDownRepeat?"keydown":"keypress",P)}beforeEach(function(){I=Ext.data.ProxyStore.prototype.load=function(){E.apply(this,arguments);if(O){this.flushLoad.apply(this,arguments)}return this};MockAjaxManager.addMethods()});afterEach(function(){Ext.data.ProxyStore.prototype.load=E;B();MockAjaxManager.removeMethods()});function B(){N=G=A=K=F=C=L=Ext.destroy(A)}describe("finding the cell editing plugin in a locking grid",function(){beforeEach(function(){H({pluginId:"test-cell-editing"},null,null,true)});it("should find it by id",function(){expect(A.getPlugin("test-cell-editing")).toBe(G)});it("should find it by ptype",function(){expect(A.findPlugin("cellediting")).toBe(G)})});describe("effect of hiding columns on cell editing selection",function(){var R=false,Q,P;beforeEach(function(){H({listeners:{edit:function(S){R=true}}},{selType:"cellmodel"});Q=A.getColumnManager()});afterEach(function(){R=false;Q=null});it("should give the edited cell the selected class after initially hiding columns",function(){Q.getColumns()[0].hide();Q.getColumns()[1].hide();F=A.store.getAt(0);C=Q.getColumns()[2];P=A.view.getCell(F,C);jasmine.fireMouseEvent(P,"dblclick");G.getEditor(F,C).setValue("111-111-1111");G.completeEdit();waitsFor(function(){return R});runs(function(){P=Ext.fly(A.view.getNode(F)).down(".x-grid-cell-selected");expect(P.hasCls("x-grid-cell-"+C.id)).toBe(true)})});it("should move the selected cell along with its column when other columns are hidden",function(){F=A.store.getAt(0);C=Q.columns[2];P=A.view.getCell(F,C);jasmine.fireMouseEvent(P,"dblclick");G.getEditor(F,C).setValue("111-111-1111");G.completeEdit();waitsFor(function(){return R});runs(function(){P=Ext.fly(A.view.getNode(F)).down(".x-grid-cell-selected");expect(P.hasCls("x-grid-cell-"+C.id)).toBe(true);Q.columns[0].hide();P=Ext.fly(A.view.getNode(F)).down(".x-grid-cell-selected");expect(P.hasCls("x-grid-cell-"+C.id)).toBe(true)})})});describe("events",function(){var P,Q;afterEach(function(){P=null});describe("beforeedit",function(){it("should retain changes to the editing context in the event handler",function(){H({listeners:{beforeedit:function(S,R){R.value="motley";P=R}}});J();expect(P.value).toBe("motley")})});describe("canceledit",function(){beforeEach(function(){waits(10);runs(function(){Q=false;H({listeners:{canceledit:function(S,R){Q=true;P=R}}});J()})});it("should be able to get the original value when canceling the edit by the plugin",function(){expect(G.editing).toBe(true);C.getEditor().setValue("baz");G.cancelEdit();expect(Q).toBe(true);expect(P.originalValue).toBe("Lisa")});it("should be able to get the edited value when canceling the edit by the plugin",function(){expect(G.editing).toBe(true);C.getEditor().setValue("foo");G.cancelEdit();expect(Q).toBe(true);expect(P.value).toBe("foo")});it("should have different values for edited value and original value when canceling",function(){expect(G.editing).toBe(true);C.getEditor().setValue("foo");G.cancelEdit();expect(Q).toBe(true);expect(P.value).not.toBe(P.originalValue)});it("should be able to get the edited value when canceling the edit by the editor",function(){expect(G.editing).toBe(true);C.getEditor().setValue("bar");G.getEditor(F,C).cancelEdit();expect(Q).toBe(true);expect(P.value).not.toBe(P.originalValue);expect(P.value).toBe("bar")});describe("falsey values",function(){it("should be able to capture falsey values when canceled by the plugin",function(){expect(G.editing).toBe(true);C.getEditor().setValue("");G.cancelEdit();expect(Q).toBe(true);expect(P.value).toBe("")});it("should be able to capture falsey values for the editedValue when canceled by the editor",function(){expect(G.editing).toBe(true);C.getEditor().setValue("");G.getEditor(F,C).cancelEdit();waitsFor(function(){return Q});runs(function(){expect(P.value).toBe("")})})})});describe("selecting ranges",function(){var T;function U(X,W,V){jasmine.fireMouseEvent(K.getNode(X).getElementsByTagName("td")[0],W,null,null,null,!!V)}function S(X){var W,V;if(arguments.length===1){if(typeof X=="number"){X=N.getAt(X)}expect(T.isSelected(X)).toBe(true)}else{for(W=0,V=arguments.length;W<V;++W){S(arguments[W])}}}afterEach(function(){T=null});function R(V){describe("MULTI",function(){beforeEach(function(){H({clicksToEdit:V==="click"?1:2},{selModel:{type:"rowmodel",mode:"MULTI"}});T=A.selModel});it("should select a range if we have a selection start point and shift is pressed",function(){U(0,V);U(3,V,true);S(0,1,2,3)});it("should maintain selection with a complex sequence",function(){U(0,V);S(0);U(2,V,true);S(0,1,2);U(3,V);S(3);U(1,V,true);S(1,2,3);U(2,V);S(2);U(0,V,true);S(0,1,2);U(3,V,true);S(2,3)})})}R("click");R("dblclick")})});describe("sorting",function(){it("should complete the edit when focusing the column",function(){H();J();C.focus();expect(G.editing).toBe(false)})});describe("making multiple selections with checkbox model",function(){var Q,P;afterEach(function(){Q=P=null});it("should keep existing selections when editing a cell in an previously-selected row",function(){H(null,{selModel:new Ext.selection.CheckboxModel({})});Q=A.store;P=A.selModel;P.select(Q.data.items);J(2);expect(P.getSelection().length).toBe(Q.data.length)});it("should expect that the correct records have been selected",function(){var R=Ext.Array.contains,S;H(null,{selModel:new Ext.selection.CheckboxModel({})});Q=A.store;P=A.selModel;P.select([Q.getAt(1),Q.getAt(3)]);J();S=P.getSelection();expect(R(S,Q.getAt(0))).toBe(false);expect(R(S,Q.getAt(1))).toBe(true);expect(R(S,Q.getAt(2))).toBe(false);expect(R(S,Q.getAt(3))).toBe(true)});it("should keep existing selections when editing a cell in an unselected row",function(){H(null,{selModel:new Ext.selection.CheckboxModel({})});Q=A.store;P=A.selModel;P.select([Q.getAt(0),Q.getAt(1)]);J(3,0);expect(P.getSelection().length).toBe(2)})});describe("setting value while remote querying",function(){var R,Q;function P(S){R=new Ext.data.Store({fields:["id","state","nickname"],proxy:{type:"ajax",url:"fake",reader:{type:"array"}}});H(null,{columns:[{header:"State",dataIndex:"id",renderer:function(V,U,T){return T.get("state")},editor:{xtype:"combo",store:R,queryMode:"remote",typeAhead:true,minChars:2,displayField:"state",valueField:"id",forceSelection:S}}]},{fields:["id","state","nickname"],data:[["AL","Alabama","The Heart of Dixie"],["AK","Alaska","The Land of the Midnight Sun"],["AR","Arkansas","The Natural State"],["AZ","Arizona","The Grand Canyon State"]],proxy:{type:"memory",reader:{type:"array"}}})}describe("only one editable column",function(){function S(U,T){describe(U,function(){function V(W){describe("forceSelection = "+W,function(){beforeEach(function(){P(W)});afterEach(function(){Ext.destroy(R);R=Q=null});function X(Z,a){jasmine.fireMouseEvent(A.view.getNode(N.getAt(0)).getElementsByTagName("td")[0],"dblclick");Q=G.getActiveEditor();if(T){R.load()}if(a==="setRawValue"){Q.field.setRawValue("ben")}else{Q.setValue("ben")}jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9)}function Y(Z){var a=Z?"setRawValue":"setValue";describe(a,function(){it("should write the value to the model",function(){var b="ben";X(W,a);if(W&&a==="setRawValue"){b="AL"}F=N.getAt(0);expect(F.get("id")).toBe(b);expect(F.get("state")).toBe("Alabama")});it("should not set any other fields in the model across tabs",function(){X(W,a);F=N.getAt(1);expect(F.get("id")).toBe("AK");expect(F.get("state")).toBe("Alaska");F=N.getAt(2);jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(F.get("state")).toBe("Arkansas");expect(F.get("nickname")).toBe("The Natural State");F=N.getAt(3);jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(F.get("state")).toBe("Arizona");expect(F.get("nickname")).toBe("The Grand Canyon State")});it("should give the editor different values across tabs",function(){X(W,a);expect(Q.getValue()).toBe("AK");expect(Q.field.getRawValue()).toBe("");jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(Q.getValue()).toBe("AR");jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(Q.getValue()).toBe("AZ")});it("should not give the editor a raw value because the combo store has not been loaded",function(){X(W,a);expect(Q.field.getRawValue()).toBe("");jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(Q.field.getRawValue()).toBe("");jasmine.fireKeyEvent(Q.field.inputEl,"keydown",9);expect(Q.field.getRawValue()).toBe("")})})}Y(false);Y(true)})}V(false);V(true)})}S("before store load is initiated",false);S("while store is loading",true);describe("when tabbing (down/up to the contiguous row)",function(){var T;beforeEach(function(){H({clicksToEdit:1},{columns:[{header:"Name",dataIndex:"name",editor:"textfield"},{header:"Email",dataIndex:"email",flex:1},{header:"Phone",dataIndex:"phone"},{header:"Age",dataIndex:"age"}],selModel:"rowmodel"});J();T=G.activeEditor;spyOn(T,"afterHide").andCallThrough();jasmine.fireKeyEvent(C.field.inputEl,"keydown",9)});afterEach(function(){T=null});it("should not complete",function(){expect(T).not.toBe(null);expect(G.activeColumn).not.toBe(null);expect(G.activeRecord).not.toBe(null)});it("should hide the editor",function(){expect(T).not.toBe(null);expect(T.isVisible()).toBe(true);expect(T.afterHide.callCount).toBe(1)})})})});describe("clicksToEdit",function(){describe("2 clicks",function(){beforeEach(function(){H()});it("should default to 2",function(){expect(G.clicksToEdit).toBe(2)});it("should begin editing when double-clicked",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);jasmine.fireMouseEvent(Ext.fly(node).down(".x-grid-cell"),"dblclick");expect(G.activeEditor).not.toBeFalsy()});it("should not begin editing when single-clicked",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);jasmine.fireMouseEvent(Ext.fly(node).down(".x-grid-cell"),"click");expect(G.activeEditor).toBeFalsy()});describe("editing a new cell",function(){var P,Q;afterEach(function(){P=Q=null});it("should update the activeEditor to point to the new cell, adjacent",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);P=Ext.fly(node).query(".x-grid-cell");Q=P[0];jasmine.fireMouseEvent(Q,"dblclick");expect(G.activeEditor.boundEl.dom).toBe(Q);Q=P[1];jasmine.fireMouseEvent(Q,"dblclick");expect(G.activeEditor.boundEl.dom).toBe(Q)});it("should update the activeEditor to point to the new cell, below",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);Q=Ext.fly(node).down(".x-grid-cell").dom;jasmine.fireMouseEvent(Q,"dblclick");expect(G.activeEditor.boundEl.dom).toBe(Q);F=A.store.getAt(1);node=A.view.getNodeByRecord(F);Q=Ext.fly(node).down(".x-grid-cell").dom;jasmine.fireMouseEvent(Q,"dblclick");expect(G.activeEditor.boundEl.dom).toBe(Q)})})});describe("1 click",function(){beforeEach(function(){H({clicksToEdit:1})});it("should honor a different number than the default",function(){expect(G.clicksToEdit).toBe(1)});it("should begin editing when single-clicked",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);jasmine.fireMouseEvent(Ext.fly(node).down(".x-grid-cell"),"click");expect(G.activeEditor).not.toBeFalsy()});if(!Ext.isIE){it("should not begin editing when double-clicked",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);jasmine.fireMouseEvent(Ext.fly(node).down(".x-grid-cell"),"dblclick");expect(G.activeEditor).toBeFalsy()})}describe("editing a new cell",function(){var P,Q;afterEach(function(){P=Q=null});it("should update the activeEditor to point to the new cell, adjacent",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);P=Ext.fly(node).query(".x-grid-cell");Q=P[0];jasmine.fireMouseEvent(Q,"click");expect(G.activeEditor.boundEl.dom).toBe(Q);Q=P[1];jasmine.fireMouseEvent(Q,"click");expect(G.activeEditor.boundEl.dom).toBe(Q)});it("should update the activeEditor to point to the new cell, below",function(){F=A.store.getAt(0);node=A.view.getNodeByRecord(F);Q=Ext.fly(node).down(".x-grid-cell").dom;jasmine.fireMouseEvent(Q,"click");expect(G.activeEditor.boundEl.dom).toBe(Q);F=A.store.getAt(1);node=A.view.getNodeByRecord(F);Q=Ext.fly(node).down(".x-grid-cell").dom;jasmine.fireMouseEvent(Q,"click");expect(G.activeEditor.boundEl.dom).toBe(Q)})})})});describe("the CellEditor",function(){beforeEach(function(){H();J()});it("should get an ownerCmp reference to the grid",function(){waitsFor(function(){return G.activeEditor&&G.activeEditor.ownerCmp===A})});it("should be able to lookup up its owner in the component hierarchy chain",function(){waitsFor(function(){return G.activeEditor&&G.activeEditor.up("grid")===A})});describe("positioning the editor",function(){it('should default to "l-l!"',function(){L=C.field;expect(L.xtype).toBe("textfield");waitsFor(function(){return G.activeEditor&&G.activeEditor.alignment==="l-l!"})});it("should constrain to the view if the editor goes out of bounds",function(){waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus},"editor to focus",1000);runs(function(){J(0,1)});waitsFor(function(){return L.hasFocus&&L.getRegion().top===Ext.fly(G.activeEditor.container).getRegion().top},"something funky to happen",1000)});it("should not reposition when shown",function(){G.completeEdit();spyOn(Ext.AbstractComponent.prototype,"setPosition");J(0,1);expect(G.activeEditor.setPosition).not.toHaveBeenCalled()});it("should not reposition when within a draggable container",function(){var P;B();H(null,{renderTo:null});P=new Ext.window.Window({items:A}).show();J();spyOn(G.activeEditor,"setPosition");jasmine.fireMouseEvent(P.el.dom,"mousedown");jasmine.fireMouseEvent(P.el.dom,"mousemove",P.x,P.y);jasmine.fireMouseEvent(P.el.dom,"mousemove",(P.x-100),(P.y-100));jasmine.fireMouseEvent(P.el.dom,"mouseup",400);expect(G.activeEditor.setPosition).not.toHaveBeenCalled();P.destroy()})});describe("as textfield",function(){it("should start the edit when ENTER is pressed",function(){var P=K.body.query("td",true)[0];waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus},"beforeEach startEdit to take effect");runs(function(){A.setActionableMode(false)});waitsFor(function(){return G.activeEditor==null&&G.editing===false&&Ext.Element.getActiveElement()===P},"actionable mode to end and cell to regain focus");runs(function(){jasmine.fireKeyEvent(P,"keydown",13)});waitsFor(function(){return G.activeEditor&&G.editing===true},"editing to start on the focused cell")});describe("when currently editing",function(){it("should complete the edit when ENTER is pressed",function(){var Q="Utley is Top Dog",P=N.getAt(0);expect(P.get("name")).toBe("Lisa");L.setValue(Q);jasmine.fireKeyEvent(L.inputEl.dom,"keydown",13);waitsFor(function(){return P.get("name")===Q},"model to be set",1000);runs(function(){expect(P.get("name")).toBe(Q)})});it("should cancel the edit when ESCAPE is pressed",function(){jasmine.pressKey(L,"esc");waitsFor(function(){return !G.editing},"editing to stop",1000);runs(function(){expect(G.editing).toBe(false)})})})});describe("as textarea",function(){beforeEach(function(){J(0,1)});it("should start the edit when ENTER is pressed",function(){var P=K.body.query("td",true)[1];waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus&&K.actionableMode===true},"beforeEach startEdit to take effect");runs(function(){A.setActionableMode(false)});waitsFor(function(){return G.activeEditor==null&&G.editing===false&&Ext.Element.getActiveElement()===P},"actionable mode to end and cell to regain focus");runs(function(){jasmine.fireKeyEvent(P,"keydown",13)});waitsFor(function(){return G.activeEditor&&G.editing===true},"editing to start on the focused cell")});describe("when currently editing",function(){it("should not complete the edit when ENTER is pressed",function(){spyOn(G,"completeEdit");waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus},"beforeEach startEdit to take effect");runs(function(){D(L.inputEl,13);expect(G.completeEdit).not.toHaveBeenCalled()})});it("should not cancel the edit when ENTER is pressed",function(){spyOn(G,"cancelEdit");waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus},"beforeEach startEdit to take effect");runs(function(){D(L.inputEl,13);expect(G.cancelEdit).not.toHaveBeenCalled()})});it("should cancel the edit when ESCAPE is pressed",function(){spyOn(G,"cancelEdit");waitsFor(function(){return G.activeEditor&&G.activeEditor.field.hasFocus},"beforeEach startEdit to take effect");runs(function(){D(L.inputEl,27)});waitsFor(function(){return !G.editing},"ESC keydown to have terminated editing");runs(function(){expect(G.editing).toBe(false)})});describe("grow and auto-sizing",function(){var P="Attention all planets of the Solar Federation!\nAttention all planets of the Solar Federation!\nWe have assumed control!";it("should auto-size when written to",function(){spyOn(L,"autoSize");L.setValue(P);expect(L.autoSize).toHaveBeenCalled()});it("should grow",function(){var Q=L.getHeight();L.setValue(P);expect(L.getHeight()).toBeGreaterThan(Q)})})})})});describe("key mappings",function(){it("should not stop propagation on the enter key",function(){var P=Ext.EventManager;spyOn(P,"stopPropagation");spyOn(P,"preventDefault");H();J(0,1);D(C.field.inputEl,13);expect(P.stopPropagation).not.toHaveBeenCalled();expect(P.preventDefault).not.toHaveBeenCalled()})});describe("in a collapsed container",function(){var Q,R,P;beforeEach(function(){Q=new Ext.form.FieldSet({collapsible:true,items:H({renderTo:null}),width:500,renderTo:Ext.getBody()});J()});afterEach(function(){Ext.destroy(Q,R,P);Q=R=P=null});it("should not set its hierarchicallyHidden property in response to any hierarchyEvents",function(){waitsFor(function(){return(R=G.activeEditor)&&R.field.hasFocus},"editing to start");runs(function(){G.completeEdit();Q.toggle();P=new Ext.grid.CellEditor({field:"textfield",renderTo:Ext.getBody()});Q.toggle();G.startEdit(F,C)});waitsFor(function(){return R.hidden===false},"editor_1 to show");runs(function(){expect(R.hierarchicallyHidden).toBe(false)})});it("should show the CellEditor when the edit is started",function(){waitsFor(function(){return(R=G.activeEditor)&&R.field.hasFocus},"editing to start");runs(function(){G.completeEdit();Q.toggle();P=new Ext.grid.CellEditor({field:"textfield",renderTo:Ext.getBody()});Q.toggle();G.startEdit(F,C)});waitsFor(function(){return R.hidden===false},"editor_1 to show")})});describe("selectOnFocus",function(){((Ext.isGecko||Ext.isOpera||Ext.isIE11)?xit:it)("should select the text in the cell when initiating an edit",function(){var Q;function P(){var R;if(!Ext.isIE){R=window.getSelection().toString()}else{if(document.selection){R=document.selection.createRange().text}}return R}H(null,{columns:[{header:"Name",dataIndex:"name",editor:{xtype:"textfield",selectOnFocus:true}},{header:"Email",dataIndex:"email",flex:1,editor:{xtype:"textfield",selectOnFocus:true}},{header:"Phone",dataIndex:"phone",editor:"textfield"},{header:"Age",dataIndex:"age",editor:"textfield"}]});Q=A.view.getNode(A.store.getAt(1));jasmine.fireMouseEvent(Q.getElementsByTagName("td")[0],"dblclick");expect(P()).toBe("Bart")})});describe("not completing the edit",function(){beforeEach(function(){waits(10)});it("should preserve the correct editing context",function(){var R=function(){return false},P,Q;H(null,{columns:[{header:"Name",dataIndex:"name",editor:{xtype:"textfield",selectOnFocus:true}},{header:"Email",dataIndex:"email",flex:1,editor:{xtype:"textfield",selectOnFocus:true}},{header:"Phone",dataIndex:"phone",editor:"textfield"},{header:"Age",dataIndex:"age",editor:"textfield"}]});J(0,1);waitsFor(function(){P=G.activeEditor;return !!P},"editing to start at cell(0, 1)");runs(function(){Q=G.context;P.on("beforecomplete",R);P.setValue("derp");D(P.field.inputEl,27);expect(G.context).toBe(Q)})})});describe("operations that refresh the view",function(){var P;afterEach(function(){P=null});describe("when editing and tabbing",function(){function Q(R){it("should not complete the edit in the new position, autoSync "+R,function(){H(null,null,{autoSync:R});F=A.store.getAt(0);C=A.columns[0];G.startEdit(F,C);P=G.activeEditor;P.setValue("Pete the Dog was here");jasmine.fireKeyEvent(P.field.inputEl,"keydown",9);waitsFor(function(){return !!G.activeEditor.editing},"editing to start",1000);runs(function(){expect(P.editing).toBe(false);expect(G.activeEditor.editing).toBe(true);expect(G.activeColumn.dataIndex).toBe("email")})})}Q(true);Q(false)});describe("when editing and syncing",function(){it("should not complete the edit in the current position",function(){H();F=A.store.getAt(0);C=A.columns[0];G.startEdit(F,C);P=G.activeEditor;P.setValue("Pete the Dog was here");N.sync();waitsFor(function(){return !!G.activeEditor.editing},"editing to start",1000);runs(function(){expect(P.editing).toBe(true);expect(G.activeColumn.dataIndex).toBe("name")})})})})})