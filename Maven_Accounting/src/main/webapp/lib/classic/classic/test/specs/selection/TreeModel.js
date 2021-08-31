describe("Ext.selection.TreeModel",function(){var A,F,B,C;function E(H,G){A=new Ext.tree.Panel({width:800,height:600,renderTo:Ext.getBody(),root:G||F,animate:false,selModel:Ext.apply(H||{},{type:"treemodel"})});B=A.getSelectionModel();C=A.down("treecolumn")}beforeEach(function(){F={id:"root",expanded:true,children:[{id:"node1",expanded:true,children:[{id:"node1_1",leaf:true},{id:"node1_2",leaf:true}]},{id:"node2",expanded:true,children:[{id:"node2_1",leaf:true},{id:"node2_2",leaf:true}]},{id:"node3",expanded:true,children:[{id:"node3_1",leaf:true},{id:"node3_2",leaf:true}]}]}});afterEach(function(){Ext.destroy(A);C=B=F=A=null});function D(G){return A.getStore().getNodeById(G)}describe("locking treegrid",function(){it("should not veto a navigation event when using a locking treegrid",function(){var G=0;A=new Ext.tree.Panel({width:800,height:600,renderTo:Ext.getBody(),root:F,columns:[{xtype:"treecolumn",text:"Text",width:200,sortable:true,dataIndex:"id",locked:true},{renderer:function(){return String(++G)}}],animate:false,selModel:{type:"treemodel"}});B=A.getSelectionModel();jasmine.fireMouseEvent(A.view.lockedView.getCellByPosition({row:1,column:0}),"click");expect(B.isSelected(1)).toBe(true)})});describe("deselecting on removal",function(){it("should deselect when the selected node is removed",function(){E();var G=D("node3_2");B.select(G);G.remove();expect(B.isSelected(G)).toBe(false)});it("should deselect when the selected node is a child of the removed node",function(){E();var G=D("node2_1");B.select(G);G.parentNode.remove();expect(B.isSelected(G)).toBe(false)});it("should remove collapsed children",function(){E();var G=D("node2_1");B.select(G);G.parentNode.collapse();G.parentNode.remove();expect(B.isSelected(G)).toBe(false)});it("should deselect a deep child of the removed node",function(){E(null,{expanded:true,children:[{id:"node1",expanded:true,children:[{id:"node2",expanded:true,children:[{id:"node3",expanded:true,children:[{id:"node4",expanded:true,children:[{id:"node5"}]}]}]}]}]});var G=D("node5");B.select(G);D("node1").remove();expect(B.isSelected(G)).toBe(false)});it("should remove the children of a node that is not a direct child of the root",function(){E(null,{expanded:true,children:[{id:"node1",expanded:true,children:[{id:"node2",expanded:true,children:[{id:"node3",expanded:true,children:[{id:"node4",expanded:true,children:[{id:"node5"}]}]}]}]}]});var G=D("node4");B.select(G);D("node2").remove();expect(B.isSelected(G)).toBe(false)});it("should remove all children at various depths",function(){E(null,{expanded:true,children:[{id:"node1",expanded:true,children:[{id:"node2"},{id:"node3",expanded:true,children:[{id:"node4",expanded:true,children:[{id:"node5"}]},{id:"node6"}]}]}]});var G=D("node2"),I=D("node5"),H=D("node6");B.select([G,I,H]);D("node1").remove();expect(B.isSelected(G)).toBe(false);expect(B.isSelected(I)).toBe(false);expect(B.isSelected(H)).toBe(false)})});describe("selectOnExpanderClick",function(){function G(H){jasmine.fireMouseEvent(H,"click")}describe("with selectOnExpanderClick: false",function(){var I,J,H;beforeEach(function(){E({selectOnExpanderClick:false});H=A.getView();I=D("node1");J=H.getRow(I)});afterEach(function(){J=H=I=null});it("should not select when clicking on the expander",function(){G(Ext.fly(J).down(H.expanderSelector));expect(B.isSelected(I)).toBe(false)});it("should select when clicking on another part of the row",function(){G(Ext.fly(J).down("."+C.iconCls));expect(B.isSelected(I)).toBe(true)})});describe("with selectOnExpanderClick: true",function(){var I,J,H;beforeEach(function(){E({selectOnExpanderClick:true});H=A.getView();I=D("node1");J=H.getRow(I)});afterEach(function(){J=H=I=null});it("should select when clicking on the expander",function(){G(Ext.fly(J).down(H.expanderSelector));expect(B.isSelected(I)).toBe(true)});it("should select when clicking on another part of the row",function(){G(Ext.fly(J).down("."+C.iconCls));expect(B.isSelected(I)).toBe(true)})})})})