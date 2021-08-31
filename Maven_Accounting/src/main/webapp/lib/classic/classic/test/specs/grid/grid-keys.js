describe("grid-keys",function(){function A(B){describe(B?"with buffered rendering":"without buffered rendering",function(){var C,N,K,H=Ext.define(null,{extend:"Ext.data.Model",fields:["field1","field2","field3","field4","field5","field6","field7","field8","field9","field10"]});var F=9,O=33,Q=34,J=35,E=36,P=37,I=38,M=39,S=40;function D(X,W,U,Y){var V=N.getHeaderByCell(N.getCellInclusive({row:X,column:W})).getVisibleIndex();N.getNavigationModel().setPosition(X,V);L("click",X,W);G("keydown",X,W,U,Y);G("keyup",X,W,U,Y);G("keypress",X,W,U,Y)}function L(W,Y,X,V,U,a){var Z=R(Y,X);jasmine.fireMouseEvent(Z,W,U,a,V)}function G(V,X,W,U,Y){var Z=R(X,W);jasmine.fireKeyEvent(Z,V,U,null,null,Y)}function R(V,U){return C.getView().getCellInclusive({row:V,column:U},true)}function T(V,X,Z){var Y=[],U=[],W;for(W=1;W<=4;++W){U.push({name:"F"+W,dataIndex:"field"+W})}Z=Z||5;for(W=1;W<=Z;++W){Y.push({field1:W+"."+1,field2:W+"."+2,field3:W+"."+3,field4:W+"."+4,field5:W+"."+5,field6:W+"."+6,field7:W+"."+7,field8:W+"."+8,field9:W+"."+9,field10:W+"."+10})}K=new Ext.data.Store({model:H,data:Y});C=new Ext.grid.Panel({columns:X||U,store:K,selType:V||"rowmodel",width:1000,height:500,bufferedRenderer:B,viewConfig:{mouseOverOutBuffer:0},renderTo:Ext.getBody()});N=C.getView()}afterEach(function(){Ext.destroy(C,K);C=K=N=null;Ext.data.Model.schema.clear()});describe("row model",function(){describe("nav keys",function(){beforeEach(function(){T();C.view.el.dom.focus()});describe("down",function(){it("should move down a row when pressing the down key on the first row",function(){D(0,0,S);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(1))});it("should move down a row when pressing the down key on a middle row",function(){D(2,0,S);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(3))});it("should not move down a row when pressing the down key on the last row",function(){D(4,0,S);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(4))})});describe("up",function(){it("should move up a row when pressing the up key on the last row",function(){D(4,0,I);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(3))});it("should move up a row when pressing the up key on a middle row",function(){D(3,0,I);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(2))});it("should not move up a row when pressing the up key on the first row",function(){D(0,0,I);expect(C.getSelectionModel().getSelection()[0]).toBe(K.getAt(0))})})});describe("special keys",function(){beforeEach(function(){T(null,null,50)});it("should move to the end of the visible rows on page down",function(){var V=C.getSelectionModel(),U=C.getNavigationModel().getRowsVisible();D(0,0,Q);expect(V.getSelection()[0]).toBe(K.getAt(U))});it("should move to the top of the visible rows on page up",function(){var V=C.getSelectionModel(),U=C.getNavigationModel().getRowsVisible();D(49,0,O);expect(V.getSelection()[0]).toBe(K.getAt(49-U))});it("should move to the last cell on ALT+end",function(){var U=C.getSelectionModel();D(0,0,J,true);expect(U.getSelection()[0]).toBe(K.getAt(49))});it("should move to the first cell on ALT+home",function(){var U=C.getSelectionModel();D(49,0,E,true);expect(U.getSelection()[0]).toBe(K.getAt(0))})})});describe("cell model",function(){function U(W,V){var X=C.getSelectionModel().getCurrentPosition();expect(X.row).toBe(W);expect(X.column).toBe(V)}describe("simple movement",function(){beforeEach(function(){T("cellmodel")});describe("left",function(){it("should not move when at the first cell",function(){D(0,0,P);U(0,0)});it("should move the position one to the left",function(){D(3,2,P);U(3,1)});it("should maintain vertical position if not wrapping",function(){D(2,1,P);U(2,0)});it("should wrap to the previous row where possible",function(){D(4,0,P);U(3,3)})});describe("up",function(){it("should not move when in the first row",function(){D(0,2,I);U(0,2)});it("should move the position one up",function(){D(3,2,I);U(2,2)});it("should maintain the vertical position",function(){D(4,1,I);U(3,1)})});describe("right",function(){it("should not move when at the last cell",function(){D(4,3,M);U(4,3)});it("should move the position one to the right",function(){D(3,2,M);U(3,3)});it("should maintain vertical position if not wrapping",function(){D(2,1,M);U(2,2)});it("should wrap to the next row where possible",function(){D(2,3,M);U(3,0)})});describe("down",function(){it("should not move when in the last row",function(){D(4,1,S);U(4,1)});it("should move the position one down",function(){D(3,2,S);U(4,2)});it("should maintain the vertical position",function(){D(1,2,S);U(2,2)})})});describe("hidden columns",function(){describe("left",function(){it("should skip over a hidden first column (left key)",function(){T("cellmodel",[{hidden:true,dataIndex:"field1"},{dataIndex:"field2"},{dataIndex:"field3"}]);D(1,1,P);U(0,2)});it("should skip over multiple hidden first columns (left key)",function(){T("cellmodel",[{hidden:true,dataIndex:"field1"},{hidden:true,dataIndex:"field2"},{dataIndex:"field3"},{dataIndex:"field4"}]);D(1,2,P);U(0,3)});it("should skip over hidden middle columns (left key)",function(){T("cellmodel",[{dataIndex:"field1"},{hidden:true,dataIndex:"field2"},{hidden:true,dataIndex:"field3"},{dataIndex:"field4"}]);D(0,3,P);U(0,0)});it("should skip over a hidden last column (left key)",function(){T("cellmodel",[{dataIndex:"field1"},{dataIndex:"field2"},{hidden:true,dataIndex:"field3"}]);D(1,0,P);U(0,1)});it("should skip over multiple hidden last columns (left key)",function(){T("cellmodel",[{dataIndex:"field1"},{dataIndex:"field2"},{hidden:true,dataIndex:"field3"},{hidden:true,dataIndex:"field4"}]);D(1,0,P);U(0,1)})});describe("right",function(){it("should skip over a hidden first column (right key)",function(){T("cellmodel",[{hidden:true,dataIndex:"field1"},{dataIndex:"field2"},{dataIndex:"field3"}]);D(0,2,M);U(1,1)});it("should skip over multiple hidden first columns (right key)",function(){T("cellmodel",[{hidden:true,dataIndex:"field1"},{hidden:true,dataIndex:"field2"},{dataIndex:"field3"},{dataIndex:"field4"}]);D(0,3,M);U(1,2)});it("should skip over hidden middle columns (right key)",function(){T("cellmodel",[{dataIndex:"field1"},{hidden:true,dataIndex:"field2"},{hidden:true,dataIndex:"field3"},{dataIndex:"field4"}]);D(0,0,M);U(0,3)});it("should skip over a hidden last column (right key)",function(){T("cellmodel",[{dataIndex:"field1"},{dataIndex:"field2"},{hidden:true,dataIndex:"field3"}]);D(0,1,M);U(1,0)});it("should skip over multiple hidden last columns (right key)",function(){T("cellmodel",[{dataIndex:"field1"},{dataIndex:"field2"},{hidden:true,dataIndex:"field3"},{hidden:true,dataIndex:"field4"}]);D(0,1,M);U(1,0)})})})})})}A(false);A(true)})