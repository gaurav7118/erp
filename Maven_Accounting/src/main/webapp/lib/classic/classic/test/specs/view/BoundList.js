describe("Ext.view.BoundList",function(){var B,A;function C(D,E){D=D||{};D.displayField="name";D.renderTo=document.body;A=D.store=new Ext.data.Store({autoDestroy:true,model:"spec.View",data:E||[{name:"Item1"}]});B=new Ext.view.BoundList(D)}beforeEach(function(){Ext.define("spec.View",{extend:"Ext.data.Model",fields:["name"]})});afterEach(function(){Ext.undefine("spec.View");Ext.data.Model.schema.clear();Ext.destroy(B);B=A=null});describe("custom tpl",function(){it("should clear the view when using a custom node outside the tpl",function(){C({tpl:['<div class="header">header</div>','<tpl for=".">','<li class="x-boundlist-item">{name}</li>',"</tpl>"]});B.refresh();B.refresh();B.refresh();expect(B.getEl().select(".header").getCount()).toBe(1)})});describe("modifying the store",function(){describe("adding",function(){it("should be able to add to an empty BoundList",function(){C({},[]);expect(B.getNodeContainer().dom.childNodes.length).toBe(0);A.add({name:"Item1"});expect(B.getNodeContainer().dom.childNodes.length).toBe(1);var D=B.getNodes();expect(D.length).toBe(1);expect(D[0].innerHTML).toBe("Item1")});it("should be able to add to the end of a BoundList",function(){C({});expect(B.getNodeContainer().dom.childNodes.length).toBe(1);A.add({name:"Item2"});expect(B.getNodeContainer().dom.childNodes.length).toBe(2);var D=B.getNodes();expect(D.length).toBe(2);expect(D[1].innerHTML).toBe("Item2")});it("should be able to insert a node at the start of the BoundList",function(){C({});expect(B.getNodeContainer().dom.childNodes.length).toBe(1);A.insert(0,{name:"Item2"});expect(B.getNodeContainer().dom.childNodes.length).toBe(2);var D=B.getNodes();expect(D.length).toBe(2);expect(D[0].innerHTML).toBe("Item2")});it("should be able to insert a node in the middle of the BoundList",function(){C({},[{name:"Item1"},{name:"Item2"},{name:"Item3"},{name:"Item4"}]);expect(B.getNodeContainer().dom.childNodes.length).toBe(4);A.insert(2,{name:"new"});expect(B.getNodeContainer().dom.childNodes.length).toBe(5);var D=B.getNodes();expect(D.length).toBe(5);expect(D[2].innerHTML).toBe("new")})});describe("updating",function(){it("should update the node content",function(){C({});A.first().set("name","foo");var D=B.getNodes();expect(D.length).toBe(1);expect(D[0].innerHTML).toBe("foo")})});describe("removing",function(){it("should remove a node from the BoundList",function(){C({});A.removeAt(0);var D=B.getNodes();expect(D.length).toBe(0)})})});describe("highlighting",function(){beforeEach(function(){var D=[],E=1;for(;E<=10;++E){D.push({name:"Item "+E})}C({itemCls:"foo",renderTo:Ext.getBody(),itemTpl:"{name}",overItemCls:"over"},D)});it("should apply the highlight class to a node",function(){B.highlightItem(B.getNode(0));var D=B.getEl().select(".foo");expect(D.item(0).hasCls(B.overItemCls)).toBe(true)});it("should remove the highlight on an item",function(){B.highlightItem(B.getNode(0));B.clearHighlight(B.getNode(0));var D=B.getEl().select(".foo");expect(D.item(0).hasCls(B.overItemCls)).toBe(false)});it("should only have at most one item highlighted",function(){B.highlightItem(B.getNode(0));B.highlightItem(B.getNode(1));var D=B.getEl().select(".foo");expect(D.item(0).hasCls(B.overItemCls)).toBe(false);expect(D.item(1).hasCls(B.overItemCls)).toBe(true)});it("should keep highlight on an item when updated",function(){B.highlightItem(B.getNode(0));B.getStore().getAt(0).set("name","New");var D=B.getEl().select(".foo");expect(D.item(0).hasCls(B.overItemCls)).toBe(true)});it("should clear all highlights on refresh",function(){B.highlightItem(B.getNode(0));B.refresh();var D=B.getEl().select(".foo");expect(D.item(0).hasCls(B.overItemCls)).toBe(false)})});describe("masking",function(){describe("disabling the boundlist",function(){it("should mark the boundlist as disabled",function(){C();B.setDisabled(true);expect(B.disabled).toBe(true)});it("should call Element.mask",function(){C();spyOn(Ext.dom.Element.prototype,"mask");B.setDisabled(true);expect(Ext.dom.Element.prototype.mask).toHaveBeenCalled()})});describe("enabling the boundlist",function(){beforeEach(function(){C({disabled:true});spyOn(Ext.dom.Element.prototype,"unmask");B.setDisabled(false)});it("should mark the boundlist as enabled",function(){expect(B.disabled).toBe(false)});it("should call Element.unmask",function(){expect(Ext.dom.Element.prototype.unmask).toHaveBeenCalled()})})})})