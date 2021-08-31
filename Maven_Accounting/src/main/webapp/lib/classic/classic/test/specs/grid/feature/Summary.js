describe("Ext.grid.feature.Summary",function(){var A=true,D=Ext.data.ProxyStore.prototype.load,B;function C(E){describe(E?"with locking":"without locking",function(){var F,R,S,Q,M,J,N,I,P,U,L,H=false;function O(V,Z,X,W){N=[{student:"Student 1",subject:"Math",mark:84},{student:"Student 1",subject:"Science",mark:72},{student:"Student 2",subject:"Math",mark:96},{student:"Student 2",subject:"Science",mark:68}];var Y=W||N;S=new Ext.data.Store(Ext.apply({fields:["student","subject",{name:"mark",type:"int"}],data:Y,autoDestroy:true},X));Q=new Ext.grid.feature.Summary(Ext.apply({ftype:"summary"},Z));F=new Ext.grid.Panel(Ext.apply({store:S,columns:[{itemId:"studentColumn",dataIndex:"student",locked:E,flex:E?undefined:1,width:E?500:undefined,text:"Name",summaryType:"count",summaryRenderer:function(b,a,c){M=arguments;return Ext.String.format("{0} student{1}",b,b!==1?"s":"")}},{itemId:"markColumn",dataIndex:"mark",text:"Mark",summaryType:"average",hidden:H}],width:600,height:300,features:Q,renderTo:Ext.getBody()},V));R=F.view;J=Q.summaryRowSelector;if(E){I=F.lockedGrid;U=I.view;P=F.normalGrid;L=P.view}}beforeEach(function(){B=Ext.data.ProxyStore.prototype.load=function(){D.apply(this,arguments);if(A){this.flushLoad.apply(this,arguments)}return this}});afterEach(function(){Ext.data.ProxyStore.prototype.load=D;F=R=S=Q=M=Ext.destroy(F);if(E){I=U=P=L=null}});function K(V){V=V||R;return V.el.down(J,true)||null}function T(){var W="",V;if(E){W+=G(U);W+=G(L)}else{W+=G(R)}return W.replace(/\r\n?|\n/g,"").replace(/\s/g,"")}function G(V){var X=K(V),W;if(X){W=X.textContent||X.innerText}return W||""}describe("init",function(){it("should give the item a default class",function(){O();if(E){expect(K(U)).toHaveCls(Q.summaryRowCls);expect(K(L)).toHaveCls(Q.summaryRowCls)}else{expect(K()).toHaveCls(Q.summaryRowCls)}});it("should respect configured value for summaryRowCls",function(){var V="utley";O(null,{summaryRowCls:V});if(E){expect(K(U)).toHaveCls(V);expect(K(L)).toHaveCls(V)}else{expect(K()).toHaveCls(V)}})});describe("No data",function(){it("should size the columns in the summary",function(){var V;O(null,null,null,[]);if(E){V=K(U);expect(V.childNodes[0].offsetWidth).toBe(500);V=K(L);expect(V.childNodes[0].offsetWidth).toBe(100)}else{V=K();expect(V.childNodes[0].offsetWidth).toBe(498);expect(V.childNodes[1].offsetWidth).toBe(100)}})});describe("summaryRenderer",function(){it("should render a column's summary on show of the column",function(){H=true;O();H=false;expect(T()).toBe("4students");F.getColumnManager().getColumns()[1].show();expect(T()).toBe("4students80")});it("should be passed the expected function parameters",function(){O();expect(M.length).toBe(4);expect(M[0]).toBe(4);expect(M[1]).toEqual(E?{studentColumn:4}:{studentColumn:4,markColumn:80});expect(M[2]).toBe("student");expect(M[3].tdCls).toBeDefined()});it("should not blow out the table cell if the value returned from the renderer is bigger than the allotted width",function(){O({columns:[{itemId:"studentColumn",dataIndex:"student",text:"Name",locked:E,width:200,summaryType:"count",summaryRenderer:function(X,W,Y){return"Lily Rupert Utley Molly Pete"}},{itemId:"markColumn",dataIndex:"mark",text:"Mark",summaryType:"average"}]});var V=S.getAt(0);if(E){expect(K(U).firstChild.offsetWidth).toBe(U.getCell(V,F.down("#studentColumn")).dom.offsetWidth);expect(K(L).firstChild.offsetWidth).toBe(L.getCell(V,F.down("#markColumn")).dom.offsetWidth)}else{expect(K().firstChild.offsetWidth).toBe(R.getCell(V,F.down("#studentColumn")).dom.offsetWidth);expect(K().lastChild.offsetWidth).toBe(R.getCell(V,F.down("#markColumn")).dom.offsetWidth)}})});describe("no summaryRenderer",function(){it("should display the summary result",function(){O({columns:[{id:"markColumn",dataIndex:"mark",locked:E,text:"Mark",summaryType:"average"},{dataIndex:"mark",text:"Mark",summaryType:"average"}]});expect(T()).toBe("8080")})});describe("dock",function(){it("should dock top under the headers",function(){O(null,{dock:"top"});if(E){expect(I.getDockedItems()[1]).toBe(Q.summaryBar);expect(P.getDockedItems()[1]).toBe(P.features[0].summaryBar)}else{expect(F.getDockedItems()[1]).toBe(Q.summaryBar)}});it("should dock at the bottom under the headers",function(){var V;O(null,{dock:"bottom"});if(E){V=I.getDockedItems()[1];expect(V).toBe(Q.summaryBar);expect(V.dock).toBe("bottom");V=P.getDockedItems()[1];expect(V).toBe(P.features[0].summaryBar);expect(V.dock).toBe("bottom")}else{V=F.getDockedItems()[1];expect(V).toBe(Q.summaryBar);expect(V.dock).toBe("bottom")}})});describe("toggling the summary row",function(){function V(W){Q.toggleSummaryRow(W)}describe("without docking",function(){function W(X){if(E){if(X){expect(K(U)).not.toBeNull();expect(K(L)).not.toBeNull()}else{expect(K(U)).toBeNull();expect(K(L)).toBeNull()}}else{if(X){expect(K()).not.toBeNull()}else{expect(K()).toBeNull()}}}it("should show the summary row by default",function(){O();W(true)});it("should not render the summary rows if configured with showSummaryRow: false",function(){O(null,{showSummaryRow:false});W(false)});it("should not show summary rows when toggling off",function(){O();W(true);V();W(false)});it("should show summary rows when toggling on",function(){O(null,{showSummaryRow:false});W(false);V();W(true)});it("should leave the summary visible when explicitly passing visible: true",function(){O();V(true);W(true)});it("should leave the summary off when explicitly passed visible: false",function(){O();V();V(false);W(false)});it("should update the summary row if the change happened while not visible",function(){var Y,X,Z;O();V();S.first().set("mark",0);V();Y=F.down("#markColumn").getCellSelector();if(E){X=Ext.fly(K(L)).down(Y);Z=X.down(L.innerSelector).dom.innerHTML}else{X=Ext.fly(K()).down(Y);Z=X.down(R.innerSelector).dom.innerHTML}expect(Z).toBe("59")})});describe("with docking",function(){it("should show the summary row by default",function(){O(null,{dock:"top"});expect(Q.getSummaryBar().isVisible()).toBe(true)});it("should not render the summary rows if configured with showSummaryRow: false",function(){O(null,{dock:"top",showSummaryRow:false});expect(Q.getSummaryBar().isVisible()).toBe(false)});it("should not show summary rows when toggling off",function(){O(null,{dock:"top"});expect(Q.getSummaryBar().isVisible()).toBe(true);V();expect(Q.getSummaryBar().isVisible()).toBe(false)});it("should show summary rows when toggling on",function(){O(null,{dock:"top",showSummaryRow:false});expect(Q.getSummaryBar().isVisible()).toBe(false);V();expect(Q.getSummaryBar().isVisible()).toBe(true)});it("should leave the summary visible when explicitly passing visible: true",function(){O(null,{dock:"top"});V(true);expect(Q.getSummaryBar().isVisible()).toBe(true)});it("should leave the summary off when explicitly passed visible: false",function(){O(null,{dock:"top"});V();V(false);expect(Q.getSummaryBar().isVisible()).toBe(false)});it("should update the summary row when if the change happened while not visible and docked",function(){var X,W,Y;O(null,{dock:"top"});V();S.first().set("mark",0);V();X=F.down("#markColumn").getCellSelector();if(E){W=P.features[0].summaryBar.getEl().down(X);Y=W.down(L.innerSelector).dom.innerHTML}else{W=Q.summaryBar.getEl().down(X);Y=W.down(F.getView().innerSelector).dom.innerHTML}expect(Y).toBe("59")})})});describe("calculated fields",function(){it("should work",function(){O({columns:[{locked:E,text:"Price inc",dataIndex:"priceInc",summaryType:"sum",formatter:'number("0.00")',summaryFormatter:'number("0.00")'},{text:"Name",dataIndex:"text",summaryType:"none"},{text:"Price ex",dataIndex:"priceEx",summaryType:"sum"}]},null,{fields:[{name:"text",type:"string"},{name:"priceEx",type:"float"},{name:"vat",type:"float"},{name:"priceInc",calculate:function(V){return V.priceEx*V.vat},type:"float"}],data:[{text:"Foo",priceEx:100,vat:1.1},{text:"Bar",priceEx:200,vat:1.25},{text:"Gah",priceEx:150,vat:1.25},{text:"Meh",priceEx:99,vat:1.3},{text:"Muh",priceEx:80,vat:1.4}]});expect(T()).toBe("788.20629")})});describe("remoteRoot",function(){function V(W){Ext.Ajax.mockComplete({status:200,responseText:Ext.JSON.encode(W)})}beforeEach(function(){MockAjaxManager.addMethods();O(null,{remoteRoot:"summaryData"},{remoteSort:true,proxy:{type:"ajax",url:"data.json",reader:{type:"json",rootProperty:"data"}},grouper:{property:"student"},data:null});S.load();S.flushLoad();V({data:N,summaryData:{mark:42,student:15},total:4})});afterEach(function(){MockAjaxManager.removeMethods()});it("should correctly render the data in the view",function(){expect(T()).toBe("15students42")});it("should create a summaryRecord",function(){var W=Q.summaryRecord;expect(W.isModel).toBe(true);expect(W.get("mark")).toBe(42);expect(W.get("student")).toBe(15)})});describe("reacting to store changes",function(){function V(Z,Y){var X;if(Z){if(E){X=W(Q.summaryBar,U);X+=W(P.features[0].summaryBar,L)}else{X=W(Q.summaryBar)}}else{X=T()}expect(X).toBe(Y)}function W(Z,X){X=X||R;var Y="";Ext.Array.forEach(Z.el.query(X.innerSelector),function(a){Y+=a.textContent||a.innerText||""});return Y.replace(/\s/g,"")}describe("before being rendered",function(){function X(Y){describe(Y?"with docking":"without docking",function(){beforeEach(function(){O({renderTo:null},{dock:Y?"top":null})});it("should not cause an exception on update",function(){expect(function(){S.getAt(0).set("mark",100)}).not.toThrow()});it("should not cause an exception on add",function(){expect(function(){S.add({student:"Student 5",subject:"Math",mark:10})}).not.toThrow()});it("should not cause an exception on remove",function(){expect(function(){S.removeAt(3)}).not.toThrow()});it("should not cause an exception on removeAll",function(){expect(function(){S.removeAll()}).not.toThrow()});it("should not cause an exception on load of new data",function(){expect(function(){S.loadData([{student:"Foo",mark:75},{student:"Bar",mark:25}])}).not.toThrow()})})}X(false);X(true)});describe("original store",function(){function X(Y){describe(Y?"with docking":"without docking",function(){beforeEach(function(){O(null,{dock:Y?"top":null})});it("should react to an update",function(){S.getAt(0).set("mark",100);V(Y,"4students84")});it("should react to an add",function(){S.add({student:"Student 5",subject:"Math",mark:10});V(Y,"5students66")});it("should react to a remove",function(){S.removeAt(3);V(Y,"3students84")});it("should react to a removeAll",function(){S.removeAll();V(Y,"0students0")});it("should react to a load of new data",function(){S.loadData([{student:"Foo",mark:75},{student:"Bar",mark:25}]);V(Y,"2students50")})})}X(false);X(true)});describe("reconfigured store",function(){function X(Y){describe(Y?"with docking":"without docking",function(){beforeEach(function(){O(null,{dock:Y?"top":null});var Z=S;S=new Ext.data.Store({fields:["student","subject",{name:"mark",type:"int"}],data:[{student:"Student 1",mark:30},{student:"Student 2",mark:50}],autoDestroy:true});F.reconfigure(S);Z.destroy()});it("should react to an update",function(){S.getAt(0).set("mark",100);V(Y,"2students75")});it("should react to an add",function(){S.add({student:"Student 3",mark:10});V(Y,"3students30")});it("should react to a remove",function(){S.removeAt(0);V(Y,"1student50")});it("should react to a removeAll",function(){S.removeAll();V(Y,"0students0")});it("should react to a load of new data",function(){S.loadData([{student:"Foo",mark:75},{student:"Bar",mark:25}]);V(Y,"2students50")})})}X(false);X(true)})});describe("buffered rendering",function(){it("should not render the summary row until the last row is in the view",function(){var X=[],V;for(V=1;V<=1000;++V){X.push({id:V,student:"Student "+V,subject:(V%2===0)?"Math":"Science",mark:V%100})}O({bufferedRenderer:true},null,null,X);var W=E?U:R;expect(W.getEl().down(J)).toBeNull();waitsFor(function(){F.scrollByDeltaY(100);if(R.all.endIndex===S.getCount()-1){expect(W.getEl().down(J)).not.toBeNull();return true}expect(W.getEl().down(J)).toBeNull()})})})})}C(false);C(true)})