describe("Ext.plugin.Responsive",function(){var B,A,D,F,E={ipad:{landscape:{width:1024,height:768,orientation:"landscape"},portrait:{width:768,height:1024,orientation:"portrait"}}},C;beforeEach(function(){B=Ext.mixin.Responsive;A=Ext.dom.Element.getOrientation;D=Ext.dom.Element.getViewportWidth;F=Ext.dom.Element.getViewportHeight;Ext.dom.Element.getOrientation=function(){return C.orientation};Ext.dom.Element.getViewportWidth=function(){return C.width};Ext.dom.Element.getViewportHeight=function(){return C.height}});afterEach(function(){Ext.dom.Element.getOrientation=A;Ext.dom.Element.getViewportWidth=D;Ext.dom.Element.getViewportHeight=F;expect(B.active).toBe(false);expect(B.count).toBe(0)});describe("responsive border region",function(){var G;beforeEach(function(){C=E.ipad.landscape;B.context={platform:{tablet:true}}});afterEach(function(){G=Ext.destroy(G)});function H(I){G=Ext.create({xtype:"panel",layout:"border",width:600,height:600,renderTo:Ext.getBody(),referenceHolder:true,items:[{reference:"child",title:"Some Title",plugins:I,responsiveFormulas:{narrow:function(J){return J.width<800}},responsiveConfig:{"width < 800":{region:"north"},"width >= 800":{region:"west"},narrow:{title:"Title - Narrow"},"!narrow":{title:"Title - Not Narrow"}}},{title:"Center",region:"center"}]})}it("respond to size change",function(){H("responsive");var I=G.lookupReference("child");expect(I.region).toBe("west");expect(I.title).toBe("Title - Not Narrow");C=E.ipad.portrait;B.notify();expect(I.region).toBe("north");expect(I.title).toBe("Title - Narrow")});describe("creation",function(){it("should be created using config object",function(){H({ptype:"responsive"});var I=G.lookupReference("child");expect(I.region).toBe("west")});it("should be created using array of config objects",function(){H([{ptype:"responsive"}]);var I=G.lookupReference("child");expect(I.region).toBe("west")})})})})