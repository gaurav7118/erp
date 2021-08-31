describe("Ext.form.Label",function(){var B;function A(C){B=new Ext.form.Label(Ext.apply({name:"test"},C))}afterEach(function(){if(B){Ext.destroy(B)}B=null});it("should have a label as the element",function(){A({renderTo:Ext.getBody()});expect(B.el.dom.tagName.toUpperCase()).toEqual("LABEL")});it("should use the forId attribute",function(){A({renderTo:Ext.getBody(),forId:"foo"});expect(B.el.dom.htmlFor).toEqual("foo")});it("should encode the text attribute",function(){A({text:"<div>foo</div>",renderTo:Ext.getBody()});expect(B.el.dom).hasHTML("&lt;div&gt;foo&lt;/div&gt;")});it("should not encode the html attribute",function(){A({html:"<span>foo</span>",renderTo:Ext.getBody()});expect(B.el.dom).hasHTML("<span>foo</span>")});it("should support setText when not rendered",function(){A();B.setText("foo");B.render(Ext.getBody());expect(B.el.dom).hasHTML("foo");B.destroy();A({text:"foo"});B.setText("bar");B.render(Ext.getBody());expect(B.el.dom).hasHTML("bar")});it("should enforce the encode attribute",function(){A();B.setText("<b>bar</b>",false);B.render(Ext.getBody());expect(B.el.dom).hasHTML("<b>bar</b>");B.setText("<span>foo</span>");expect(B.el.dom).hasHTML("&lt;span&gt;foo&lt;/span&gt;");B.setText("<span>bar</span>",false);expect(B.el.dom).hasHTML("<span>bar</span>")});it("should update the layout when text is set after render",function(){A({renderTo:document.body});var C=B.getWidth();B.setText("New text");expect(B.getWidth()).toBeGreaterThan(C)})})