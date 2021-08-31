describe("Ext.Element.static",function(){var C,A,E,D,B;beforeEach(function(){C=Ext.getBody().createChild({id:"ExtElementHelper",style:"position:absolute;",children:[{id:"child1",style:"position:absolute;"},{id:"child2",style:"position:absolute;"},{id:"child3",style:"position:absolute;"}]});A=Ext.getBody().createChild({id:"ExtElementInputHelper",tag:"input",type:"text"});E=Ext.get("child1");D=Ext.get("child2");B=Ext.get("child3")});afterEach(function(){C.destroy();A.destroy();E.destroy();D.destroy();B.destroy()});describe("addUnits",function(){it("should add the defualt unit",function(){expect(Ext.Element.addUnits(10)).toEqual("10px")});it("should not add the defualt unit",function(){expect(Ext.Element.addUnits("10px")).toEqual("10px")})});describe("parseBox",function(){describe("number",function(){describe("when 1 argument",function(){it("should return an object with correct values",function(){expect(Ext.Element.parseBox(10)).toEqual({top:10,right:10,bottom:10,left:10})})})});describe("string",function(){describe("when 1 argument",function(){it("should return an object with correct values",function(){expect(Ext.Element.parseBox("10")).toEqual({top:10,right:10,bottom:10,left:10})})});describe("when 2 arguments",function(){it("should return an object with correct values",function(){expect(Ext.Element.parseBox("10 5")).toEqual({top:10,right:5,bottom:10,left:5})})});describe("when 3 arguments",function(){it("should return an object with correct values",function(){expect(Ext.Element.parseBox("10 5 15")).toEqual({top:10,right:5,bottom:15,left:5})})});describe("when 4 arguments",function(){it("should return an object with correct values",function(){expect(Ext.Element.parseBox("10 5 15 0")).toEqual({top:10,right:5,bottom:15,left:0})})})})});describe("unitizeBox",function(){it("should return a string",function(){expect(Ext.Element.unitizeBox("10 5 15 0")).toEqual("10px 5px 15px 0px")})});describe("normalize",function(){it("should change border-radius > borderRadius",function(){expect(Ext.Element.normalize("border-radius")).toEqual("borderRadius")})});describe("getDocumentHeight",function(){it("should return the document height",function(){var F=Ext.Element.getDocumentHeight();expect(F).toBeDefined();expect(Ext.isNumber(F)).toBeTruthy()})});describe("getDocumentWidth",function(){it("should return the document width",function(){var F=Ext.Element.getDocumentWidth();expect(F).toBeDefined();expect(Ext.isNumber(F)).toBeTruthy()})});describe("getViewportHeight",function(){it("should return the window height",function(){var F=Ext.Element.getViewportHeight();expect(F).toBeDefined();expect(Ext.isNumber(F)).toBeTruthy()})});describe("getViewportWidth",function(){it("should return the window width",function(){var F=Ext.Element.getViewportWidth();expect(F).toBeDefined();expect(Ext.isNumber(F)).toBeTruthy()})});describe("getViewSize",function(){it("should return the window height and width",function(){expect(Ext.Element.getViewSize()).toEqual({width:Ext.isIE8?document.documentElement.scrollWidth:window.innerWidth,height:Ext.isIE8?document.documentElement.scrollHeight:window.innerHeight})})});describe("getOrientation",function(){it("should return the correct orientation",function(){expect(Ext.Element.getOrientation()).toEqual((window.innerHeight>window.innerWidth)?"portrait":"landscape")})});describe("fromPoint",function(){var F=Ext.isSafari3||Ext.isSafari4||Ext.isOpera||Ext.isIE9m?xit:it;F("should return nothing",function(){expect(Ext.Element.fromPoint(-550000,-550000)).toBeNull()})})},"/src/dom/Element.static.js")