describe("Ext.button.Cycle",function(){var A;function C(D){jasmine.fireMouseEvent(A.el.dom,D||"click")}function B(D){spyOn(Ext.log,"error");spyOn(Ext.log,"warn");A=new Ext.button.Cycle(Ext.apply({text:"Button",menu:{items:[{text:"Foo",iconCls:"iconFoo",glyph:"glyphFoo"},{text:"Bar",iconCls:"iconBar",glyph:"glyphBar"},{text:"Baz",iconCls:"iconBaz",glyph:"glyphBaz"}]}},D))}afterEach(function(){Ext.destroy(A);A=null});describe("event/handler",function(){var E,D;beforeEach(function(){E=jasmine.createSpy();D=jasmine.createSpy()});afterEach(function(){E=D=null});describe("during construction",function(){it("should not fire the event/handler with no activeItem",function(){B({listeners:{change:E},changeHandler:D});expect(E).not.toHaveBeenCalled();expect(D).not.toHaveBeenCalled()});it("should not fire the event/handler with an activeItem",function(){B({activeItem:1,listeners:{change:E},changeHandler:D});expect(E).not.toHaveBeenCalled();expect(D).not.toHaveBeenCalled()})});describe("arguments",function(){it("should pass the button and the active item",function(){B({listeners:{change:E},changeHandler:D});A.setActiveItem(1);expect(E.callCount).toBe(1);expect(E.mostRecentCall.args[0]).toBe(A);expect(E.mostRecentCall.args[1]).toBe(A.getMenu().items.getAt(1));expect(D.callCount).toBe(1);expect(D.mostRecentCall.args[0]).toBe(A);expect(D.mostRecentCall.args[1]).toBe(A.getMenu().items.getAt(1))})});describe("suppressEvents",function(){it("should not fire if suppressEvents is passed",function(){B({listeners:{change:E},changeHandler:D});A.setActiveItem(1,true);expect(E).not.toHaveBeenCalled();expect(D).not.toHaveBeenCalled()})});describe("scope",function(){it("should default the scope to the button",function(){B({changeHandler:D});A.setActiveItem(1);expect(D.mostRecentCall.object).toBe(A)});it("should use a passed scope",function(){var F={};B({changeHandler:D,scope:F});A.setActiveItem(1);expect(D.mostRecentCall.object).toBe(F)})});it("should be able to resolve to a view controller",function(){var G=new Ext.app.ViewController();G.doSomething=jasmine.createSpy();B({changeHandler:"doSomething"});var F=new Ext.container.Container({controller:G,items:A});A=F.items.first();A.setActiveItem(2);F.destroy()})});describe("showText",function(){describe("with showText: false",function(){it("should show the button text",function(){B({showText:false});expect(A.getText()).toBe("Button");A.setActiveItem(1);expect(A.getText()).toBe("Button");A.setActiveItem(2);expect(A.getText()).toBe("Button")});it("should not prepend the prependText",function(){B({showText:false,prependText:"!"});expect(A.getText()).toBe("Button")})});describe("with showText: true",function(){it("should show the active item text",function(){B({showText:true});expect(A.getText()).toBe("Foo");A.setActiveItem(1);expect(A.getText()).toBe("Bar");A.setActiveItem(2);expect(A.getText()).toBe("Baz")});it("should prepend the prependText",function(){B({showText:true,prependText:"!"});expect(A.getText()).toBe("!Foo");A.setActiveItem(1);expect(A.getText()).toBe("!Bar");A.setActiveItem(2);expect(A.getText()).toBe("!Baz")})})});describe("forceIcon",function(){it("should show the active item iconCls by default",function(){B();expect(A.iconCls).toBe("iconFoo")});it("should update the icon when the active item changes",function(){B();A.setActiveItem(1);expect(A.iconCls).toBe("iconBar")});it("should use the forceIcon if specified",function(){B({forceIcon:"iconForce"});expect(A.iconCls).toBe("iconForce");A.setActiveItem(1);expect(A.iconCls).toBe("iconForce")})});describe("forceGlyph",function(){it("should show the active item glyph by default",function(){B();expect(A.glyph).toBe("glyphFoo")});it("should update the glyph when the active item changes",function(){B();A.setActiveItem(1);expect(A.glyph).toBe("glyphBar")});it("should use the forceIcon if specified",function(){B({forceGlyph:"glyphForce"});expect(A.glyph).toBe("glyphForce");A.setActiveItem(1);expect(A.glyph).toBe("glyphForce")})})})