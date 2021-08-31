describe("Ext.view.NodeCache",function(){var F,D,B,G,A=true,E=Ext.data.ProxyStore.prototype.load,C;beforeEach(function(){C=Ext.data.ProxyStore.prototype.load=function(){E.apply(this,arguments);if(A){this.flushLoad.apply(this,arguments)}return this};D=Ext.create("Ext.data.Store",{fields:["name"],autoDestroy:true,data:{"items":[{"name":"Lisa"},{"name":"Bart"},{"name":"Homer"},{"name":"Marge"}]},proxy:{type:"memory",reader:{type:"json",rootProperty:"items"}}});F=Ext.create("Ext.grid.Panel",{store:D,height:100,width:100,renderTo:Ext.getBody(),columns:[{text:"Name",dataIndex:"name"}]});B=F.getView();G=B.all});afterEach(function(){Ext.data.ProxyStore.prototype.load=E;F.destroy()});it("Store rejectChanges() should not break NodeCache insert()",function(){var H=function(){D.rejectChanges()};var I=D.getCount();D.removeAt(I-1);D.removeAt(I-2);expect(H).not.toThrow();expect(D.getAt(3).get("name")).toBe("Marge");expect(D.getAt(2).get("name")).toBe("Homer")});it("should not mutate the rendered block on moveBlock(0)",function(){var J=G.startIndex,H=G.endIndex,I=G.slice();G.moveBlock(0);expect(G.startIndex).toBe(J);expect(G.endIndex).toBe(H);expect(G.slice()).toEqual(I)})})