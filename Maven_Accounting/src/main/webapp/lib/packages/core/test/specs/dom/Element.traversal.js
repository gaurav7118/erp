describe("Ext.Element.traversal",function(){var F,A,H,G,E,C,B,D;beforeEach(function(){F=Ext.getBody().createChild({id:"ExtElementHelper",cls:"wrapper",style:"position:absolute;",children:[{id:"child1",style:"position:absolute;"},{id:"child2",style:"position:absolute;"},{id:"child3",style:"position:absolute;"},{id:"child4",children:[{id:"child4_1",cls:"findIt",children:[{id:"child4_1_1"}]}]}]});A=Ext.getBody().createChild({id:"ExtElementInputHelper",tag:"input",type:"text"});H=Ext.get("child1");G=Ext.get("child2");E=Ext.get("child3");C=Ext.get("child4");B=Ext.get("child4_1");D=Ext.get("child4_1_1")});afterEach(function(){Ext.each([F,A,H,G,E,C,B,D],function(I){I.destroy()})});describe("findParentNode",function(){it("should return document.body",function(){expect(F.findParentNode("body")).toEqual(document.body)});it("should return a dom",function(){expect(H.findParentNode(".wrapper")).toEqual(Ext.getDom(F))});it("should return an el",function(){expect(H.findParentNode(".wrapper",null,true)).toEqual(F)});describe("when maxDepth",function(){describe("1",function(){it("should not return the el",function(){expect(B.findParentNode(".wrapper",1)).toBeNull()})});describe("2",function(){it("should not return the el",function(){expect(B.findParentNode(".wrapper",2)).toEqual(Ext.getDom(F))})})})});describe("up",function(){it("should return Ext.getBody()",function(){expect(F.up("body")).toEqual(Ext.getBody())});it("should return a el",function(){expect(H.up(".wrapper")).toEqual(F)});describe("when maxDepth",function(){describe("1",function(){it("should not return the el",function(){expect(B.up(".wrapper",1)).toBeNull()})});describe("2",function(){it("should not return the el",function(){expect(B.up(".wrapper",2)).toEqual(F)})})})});describe("select",function(){it("should return an Ext.CompositeELementLite",function(){var I=F.select("div");expect(I).toBeDefined();expect(I.elements.length).toEqual(6);expect(I instanceof Ext.CompositeElementLite).toBe(true)})});describe("query",function(){it("should return elements",function(){var I=F.query("div");expect(I).toBeDefined();expect(I.length).toEqual(6);expect(I.isComposite).toBeFalsy();expect(Ext.isArray(I)).toBeTruthy()})});describe("down",function(){it("should return an el",function(){var I=F.down(".findIt");expect(I).toBeDefined();expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=F.down(".findIt",true);expect(I).toBeDefined();expect(Ext.isElement(I)).toBeTruthy()})});describe("child",function(){it("should return null",function(){var I=F.child(".findIt");expect(I).toBeNull()});it("should return an el",function(){var I=C.child(".findIt");expect(I).toBeDefined();expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=C.child(".findIt",true);expect(I).toBeDefined();expect(Ext.isElement(I)).toBeTruthy()})});describe("parent",function(){it("should return an el",function(){var I=H.parent();expect(I).toBeDefined();expect(I).toEqual(F);expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=H.parent(null,true);expect(I).toBeDefined();expect(I).toEqual(Ext.getDom(F));expect(Ext.isElement(I)).toBeTruthy()})});describe("next",function(){it("should return an el",function(){var I=H.next();expect(I).toBeDefined();expect(I).toEqual(G);expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=H.next(null,true);expect(I).toBeDefined();expect(I).toEqual(Ext.getDom(G));expect(Ext.isElement(I)).toBeTruthy()})});describe("prev",function(){it("should return an el",function(){var I=G.prev();expect(I).toBeDefined();expect(I).toEqual(H);expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=G.prev(null,true);expect(I).toBeDefined();expect(I).toEqual(Ext.getDom(H));expect(Ext.isElement(I)).toBeTruthy()})});describe("first",function(){it("should return an el",function(){var I=F.first();expect(I).toBeDefined();expect(I).toEqual(H);expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=F.first(null,true);expect(I).toBeDefined();expect(I).toEqual(Ext.getDom(H));expect(Ext.isElement(I)).toBeTruthy()})});describe("last",function(){it("should return an el",function(){var I=F.last();expect(I).toBeDefined();expect(I).toEqual(C);expect(Ext.isElement(I)).toBeFalsy()});it("should return a dom",function(){var I=F.last(null,true);expect(I).toBeDefined();expect(I).toEqual(Ext.getDom(C));expect(Ext.isElement(I)).toBeTruthy()})});describe("findParent",function(){it("should return document.body",function(){expect(F.findParent("body")).toEqual(document.body)});it("should return a dom",function(){expect(H.findParent(".wrapper")).toEqual(Ext.getDom(F))});it("should return an el",function(){expect(H.findParent(".wrapper",null,true)).toEqual(F)});it("should include itself if it matches",function(){expect(D.findParent("#child4_1_1",null,true)).toBe(D)});it("should default the maxDepth to 50 or the document element",function(){var I=Ext.getBody().createChild({cls:"findParentRoot"}),L=I,K=[I],J;for(J=0;J<49;++J){L=L.createChild();K.push(L)}expect(L.findParent(".findParentRoot",undefined,true)).toBe(I);L=L.createChild();K.push(L);expect(L.findParent(".findParentRoot",undefined,true)).toBeNull();expect(K[10].findParent(".doesntExist")).toBeNull();Ext.destroy(K)});describe("with maxDepth",function(){describe("as a number",function(){it("should include an element within the limit",function(){expect(D.findParent("#child4",3,true)).toBe(C)});it("should exclude an element at the limit",function(){expect(D.findParent("#child4",2,true)).toBeNull()});it("should exclude an element above the limit",function(){expect(D.findParent("#child4",1,true)).toBeNull()})});describe("as an element",function(){it("should accept a string id",function(){expect(D.findParent(".wrapper","child4_1")).toBeNull()});it("should accept a dom element",function(){expect(D.findParent(".wrapper",B.dom)).toBeNull()});it("should accept an Ext.dom.Element",function(){expect(D.findParent(".wrapper",B)).toBeNull()});it("should include an element within the limit",function(){expect(D.findParent(".findIt",C,true)).toBe(B)});it("should exclude elements at the limit",function(){expect(D.findParent("#child4",C,true)).toBeNull()});it("should exclude an element above the limit",function(){expect(D.findParent(".wrapper",C,true)).toBeNull()})})})});describe("contains",function(){it("should return false for siblings",function(){expect(Ext.fly(H).contains(D)).toBe(false);expect(Ext.fly(G).contains(D)).toBe(false)});it("should return true for parents",function(){expect(Ext.fly(B).contains(D)).toBe(true)});it("should return true for grandparents",function(){expect(Ext.fly(C).contains(D)).toBe(true)});it("should return true for self",function(){expect(Ext.fly(D).contains(D)).toBe(true)})})},"/src/dom/Element.traversal.js")