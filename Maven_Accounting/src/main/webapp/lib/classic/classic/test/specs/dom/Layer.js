describe("Ext.dom.Layer",function(){var A;afterEach(function(){A.destroy()});it("should create a div by default",function(){A=new Ext.dom.Layer();expect(A.dom.tagName).toBe("DIV")});it("should have the x-layer cls",function(){A=new Ext.dom.Layer();expect(A).toHaveCls("x-layer")});it("should accept a domhelper config as its element",function(){A=new Ext.dom.Layer({dh:{tag:"p",cls:"today-is-the-greatest-day-Ive-ever-known"}});expect(A.dom.tagName).toBe("P");expect(A).toHaveCls("today-is-the-greatest-day-Ive-ever-known")});it("should append the layer to document.body",function(){A=new Ext.dom.Layer();expect(A.dom.parentNode).toBe(document.body)});it("should allow the parent node to be configured",function(){var B=Ext.getBody().createChild();A=new Ext.dom.Layer({parentEl:B});expect(A.dom.parentNode).toBe(B.dom);B.destroy()});it("should not create a shadow by default",function(){A=new Ext.dom.Layer();expect(A.shadow).toBeUndefined()});it("should create a shadow if shadow is true",function(){A=new Ext.dom.Layer({shadow:true});expect(A.shadow instanceof Ext.dom.Shadow).toBe(true);expect(A.shadow.mode).toBe("drop")});it("should create a shadow using a shadow mode",function(){A=new Ext.dom.Layer({shadow:"sides"});expect(A.shadow instanceof Ext.dom.Shadow).toBe(true);expect(A.shadow.mode).toBe("sides")});it("should not create a shim by default",function(){A=new Ext.dom.Layer();expect(A.shim).toBeUndefined()});it("should create a shim if shim is true",function(){A=new Ext.dom.Layer({shim:true});expect(A.shim instanceof Ext.dom.Shim).toBe(true)});it("should accept a cls",function(){A=new Ext.dom.Layer({cls:"ohyeah"});expect(A).toHaveCls("ohyeah")});it("should accept a shadowOffset",function(){A=new Ext.dom.Layer({shadow:true,shadowOffset:9999});expect(A.shadow.offset).toBe(9999)});it("should use css visibility to hide",function(){A=new Ext.dom.Layer();expect(A.getVisibilityMode()).toBe(Ext.Element.VISIBILITY)});it("should use display to hide if useDisplay is true",function(){A=new Ext.dom.Layer({useDisplay:true});expect(A.getVisibilityMode()).toBe(Ext.Element.DISPLAY)});it("should configure the visibility mode using hideMode:'display'",function(){A=new Ext.dom.Layer({hideMode:"display"});expect(A.getVisibilityMode()).toBe(Ext.Element.DISPLAY)});it("should configure the visibility mode using hideMode:'visibility'",function(){A=new Ext.dom.Layer({hideMode:"visibility"});expect(A.getVisibilityMode()).toBe(Ext.Element.VISIBILITY)});it("should configure the visibility mode using hideMode:'offsets'",function(){A=new Ext.dom.Layer({hideMode:"offsets"});expect(A.getVisibilityMode()).toBe(Ext.Element.OFFSETS)})})