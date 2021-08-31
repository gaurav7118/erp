describe("Ext.form.field.TextArea",function(){var B,A;function C(D,E){jasmine.expectAriaAttr(B,D,E)}beforeEach(function(){A=function(D){D=D||{};Ext.applyIf(D,{name:"test"});if(B){B.destroy()}B=new Ext.form.field.TextArea(D)}});afterEach(function(){if(B){B.destroy()}B=A=null});it("should encode the input value in the template",function(){A({renderTo:Ext.getBody(),value:'test "  <br/> test'});expect(B.inputEl.dom.value).toBe('test "  <br/> test')});it("should be able to set a numeric value",function(){A({renderTo:Ext.getBody()});B.setValue(100);expect(B.getValue()).toBe("100")});describe("defaults",function(){beforeEach(function(){A()});it("should have growMin = 60",function(){expect(B.growMin).toEqual(60)});it("should have growMax = 1000",function(){expect(B.growMax).toEqual(1000)});it("should have growAppend = '\n-'",function(){expect(B.growAppend).toEqual("\n-")});it("should have enterIsSpecial = false",function(){expect(B.enterIsSpecial).toBe(false)});it("should have preventScrollbars = false",function(){expect(B.preventScrollbars).toBe(false)})});describe("rendering",function(){beforeEach(function(){A({name:"fieldName",value:"fieldValue",tabIndex:5,renderTo:Ext.getBody()})});describe("bodyEl",function(){it("should have the class 'x-form-item-body'",function(){expect(B.bodyEl.hasCls("x-form-item-body")).toBe(true)});it("should have the id '[id]-bodyEl'",function(){expect(B.bodyEl.dom.id).toEqual(B.id+"-bodyEl")})});describe("inputEl",function(){it("should be a textarea element",function(){expect(B.inputEl.dom.tagName.toLowerCase()).toEqual("textarea")});it("should have the component's inputId as its id",function(){expect(B.inputEl.dom.id).toEqual(B.inputId)});it("should have the 'fieldCls' config as a class",function(){expect(B.inputEl.hasCls(B.fieldCls)).toBe(true)});it("should have a class of 'x-form-text'",function(){expect(B.inputEl.hasCls("x-form-text")).toBe(true)});it("should have its name set to the 'name' config",function(){expect(B.inputEl.dom.name).toEqual("fieldName")});it("should have its value set to the 'value' config",function(){expect(B.inputEl.dom.value).toEqual("fieldValue")});it("should have autocomplete = 'off'",function(){expect(B.inputEl.dom.getAttribute("autocomplete")).toEqual("off")});it("should have tabindex set to the tabIndex config",function(){expect(""+B.inputEl.dom.getAttribute("tabIndex")).toEqual("5")})});describe("ariaEl",function(){it("should be inputEl",function(){expect(B.ariaEl).toBe(B.inputEl)})});describe("ARIA attributes",function(){it("should have textbox role",function(){C("role","textbox")});it("should have aria-multiline attribute",function(){C("aria-multiline","true")})});xdescribe("sizing",function(){it("should have the cols property affect size when shrink wrapping",function(){var D=B.getWidth();B.destroy();A({rows:10,cols:40,renderTo:Ext.getBody()});expect(B.getWidth()).toBeGreaterThan(D);B.destroy();A({rows:10,cols:10,renderTo:Ext.getBody()});expect(B.getWidth()).toBeLessThan(D)});it("should give preference to a calculated/configured width",function(){B.destroy();A({rows:10,cols:40,width:500,renderTo:Ext.getBody()});expect(B.getWidth()).toBe(500)});it("should account for a top label when sizing",function(){B.destroy();A({renderTo:Ext.getBody(),width:100,height:100,labelAlign:"top",fieldLabel:"A label"});var D=B.labelEl,E=100-(D.getHeight()+D.getMargin("tb"));expect(B.inputEl.getHeight()).toBe(E)})})});(Ext.isIE8?xdescribe:describe)("autoSize method and grow configs",function(){function D(G){var E=[],F;for(F=0;F<G;++F){E.push("a")}return E.join("\n")}describe("with an auto height",function(){beforeEach(function(){A({grow:true,growMin:40,growMax:200,renderTo:Ext.getBody()})});it("should auto height with an initial value",function(){B.destroy();A({grow:true,growMin:40,growMax:500,renderTo:Ext.getBody(),value:D(10)});expect(B.getHeight()).toBeLessThan(500);expect(B.getHeight()).toBeGreaterThan(40)});it("should set the initial textarea height to growMin",function(){expect(B.getHeight()).toBe(40)});it("should increase the height of the input as the value becomes taller",function(){B.setValue(D(4));var F=B.getHeight();B.setValue(D(5));var E=B.getHeight();expect(E).toBeGreaterThan(F)});it("should decrease the height of the input as the value becomes shorter",function(){B.setValue("A\nB\nC\nD\nE");var F=B.inputEl.getHeight();B.setValue("A\nB\nC\nD");var E=B.inputEl.getHeight();expect(E).toBeLessThan(F)});it("should not increase the height above the growMax config",function(){B.setValue(D(50));var E=B.getHeight();expect(E).toBe(200)});it("should not decrease the height below the growMin config",function(){B.setValue("");var E=B.getHeight();expect(E).toBe(40)});it("should work with markup",function(){B.setValue("<fake tag appears here with longer text that should cause the field to grow");expect(B.getHeight()).toBeGreaterThan(40)})});describe("with a fixed height",function(){it("should have no effect on a configured height",function(){A({renderTo:Ext.getBody(),grow:true,growMin:100,height:150,growMax:700});B.setValue(D(100));expect(B.getHeight()).toBe(150)});it("should have no effect on a calculated height",function(){A({grow:true,growMin:100,growMax:700});var E=new Ext.container.Container({renderTo:Ext.getBody(),layout:"fit",width:150,height:150,items:B});B.setValue(D(100));expect(B.getHeight()).toBe(150);E.destroy()})})});describe("readOnly",function(){describe("readOnly config",function(){it("should set the readonly attribute of the field when rendered",function(){A({readOnly:true,renderTo:Ext.getBody()});expect(B.inputEl.dom.readOnly).toBe(true)})});describe("setReadOnly method",function(){it("should set the readOnly state of the field immediately if rendered",function(){A({renderTo:Ext.getBody()});B.setReadOnly(true);expect(B.inputEl.dom.readOnly).toBe(true)});it("should remember the value if the field has not yet been rendered",function(){A();B.setReadOnly(true);B.render(Ext.getBody());expect(B.inputEl.dom.readOnly).toBe(true)})})});describe("preventScrollbars config",function(){it("should set overflow:hidden on the textarea if true",function(){A({grow:true,preventScrollbars:true,renderTo:Ext.getBody()});expect(B.inputEl.getStyle("overflow")).toEqual("hidden")});it("should should do nothing if preventScrollbars is false",function(){A({grow:true,preventScrollbars:false,renderTo:Ext.getBody()});expect(B.inputEl.dom.style.overflow).not.toEqual("hidden")});it("should should do nothing if grow is false",function(){A({grow:false,preventScrollbars:true,renderTo:Ext.getBody()});expect(B.inputEl.getStyle("overflow")).not.toEqual("hidden")})});describe("initial value",function(){var D=function(E){A({value:E});expect(B.getValue()).toBe(E);expect(B.isDirty()).toBeFalsy()};it("should not insert unspecified new lines",function(){D("initial value");D(" initial  value ");D("  initial   value  ");D(" ");D("  ")});it("should preserve new lines",function(){D("\ninitial value");D("\n\ninitial value");D("   initial value");D("   \ninitial value");D("\n   initial value");D("initial\nvalue");D("initial \n value");D("initial \n\n value");D("initial \n \n value");D("initial value\n");D("initial value\n\n")});it("should preserve empty strings",function(){D("\n");D(" \n ");D("  \n  ");D(" \n \n ");D("  \n  \n  ");D("\n \n");D("\n  \n")})});describe("carriage returns",function(){var D="line1\r\nline2";var F=function(){expect(B.getValue().indexOf("\r")).toBe(-1)};var E=function(){F();expect(B.isDirty()).toBe(false)};it("should strip carriage returns from the initial value before render",function(){A({value:D});E()});it("should strip carriage returns from the initial value after render",function(){A({value:D,renderTo:Ext.getBody()});E()});it("should strip carriage returns when we call setValue before rendering",function(){A();B.setValue(D);F()});it("should strip carriage returns when we call setValue after rendering",function(){A({renderTo:Ext.getBody()});B.setValue(D);F()})});describe("validation",function(){describe("allowBlank",function(){it("should not allow only newlines and spaces when used with allowOnlyWhitespace: false",function(){A({allowOnlyWhitespace:false,value:"  \n\n    \n\n"});expect(B.getErrors()).toContain("This field is required")})})});(Ext.isIE8?xdescribe:describe)("foo",function(){it("should start out at growMin",function(){A({renderTo:document.body,grow:true,growMin:50});expect(B.getHeight()).toBe(50)});it("should initially render at the height of the text",function(){A({renderTo:document.body,value:"m\nm\nm\nm\nm\nm\nm",grow:true,growMin:50});expect(B.getHeight()).toBe(117)});it("should initially render with a height of growMax if initial text height exceeds growMax",function(){A({renderTo:document.body,value:"m\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm\nm",grow:true,growMax:200});expect(B.getHeight()).toBe(200)});it("should grow and shrink",function(){A({renderTo:document.body,grow:true,growMin:50,growMax:100});expect(B.getHeight()).toBe(50);B.setValue("m\nm\nm\nm");expect(B.getHeight()).toBe(75);B.setValue("m\nm\nm\nm\nm\nm\nm\nm\nm\nm");expect(B.getHeight()).toBe(100);B.setValue("m\nm\nm\nm");expect(B.getHeight()).toBe(75);B.setValue("m");expect(B.getHeight()).toBe(50)})});describe("layout",function(){var E={1:"width",2:"height",3:"width and height"};function D(G,F){describe((G?("shrink wrap "+E[G]):"fixed width and height")+" autoFitErrors: "+F,function(){var L=(G&1),O=(G&2),M=18,Q=20,Y=16,H=1,W=105,a=5,N=[3,4],S=W-a,b=1,X=150,T=O?58:100,I=23,Z,K,U,R;function P(c){c=c||{};Z=c.hideLabel;K=(c.labelAlign==="top");U=X;R=T;if(!Z&&!K){U+=W}if(!Z&&K){R+=I}if(c.msgTarget==="side"){U+=M}if(c.msgTarget==="under"){R+=Q}B=Ext.create("Ext.form.field.TextArea",Ext.apply({renderTo:document.body,height:O?null:R,width:L?null:U,autoFitErrors:F,fieldLabel:'<span style="display:inline-block;width:'+S+'px;background-color:red;">&nbsp;</span>',labelSeparator:""},c))}function J(c){B.setActiveError(c||"Error Message")}function V(c){describe(c+" label",function(){var d=(c==="left");(Ext.isIE8?xit:it)("should layout",function(){P({labelAlign:c});expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:W,h:R},".x-form-item-label-inner":{x:d?0:W-a-S,y:N,w:S},bodyEl:{x:W,y:0,w:X,h:T},inputEl:{x:W+b,y:b,w:X-(b*2),h:T-(b*2)}});expect(B.errorWrapEl).toBeNull()});(Ext.isIE8?xit:it)("should layout with side error",function(){P({labelAlign:c,msgTarget:"side"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:W,h:R},".x-form-item-label-inner":{x:d?0:W-a-S,y:N,w:S},bodyEl:{x:W,y:0,w:X,h:T},inputEl:{x:W+b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:U-M,y:0,w:M,h:R},errorEl:{x:U-M+H,y:(T-Y)/2,w:Y,h:Y}})});(Ext.isIE8?xit:it)("should layout with hidden side error",function(){P({labelAlign:c,msgTarget:"side"});var e=(F&&!L)?X+M:X;expect(B).toHaveLayout({el:{w:(L&&F)?U-M:U,h:R},labelEl:{x:0,y:0,w:W,h:R},".x-form-item-label-inner":{x:d?0:W-a-S,y:N,w:S},bodyEl:{x:W,y:0,w:e,h:T},inputEl:{x:W+b,y:b,w:e-(b*2),h:T-(b*2)},errorWrapEl:{x:F?0:U-M,y:F?0:0,w:F?0:M,h:F?0:R},errorEl:{x:F?0:U-M+H,y:F?0:(T-Y)/2,w:F?0:Y,h:F?0:Y}})});(Ext.isIE10m&&!O?xit:it)("should layout with under error",function(){P({labelAlign:c,msgTarget:"under"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:W,h:T},".x-form-item-label-inner":{x:d?0:W-a-S,y:N,w:S},bodyEl:{x:W,y:0,w:X,h:T},inputEl:{x:W+b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:0,y:T,w:U,h:Q},errorEl:{x:W,y:T,w:X,h:Q}})});(Ext.isIE8?xit:it)("should layout with hidden label",function(){P({labelAlign:c,hideLabel:true});expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T}});expect(B.errorWrapEl).toBeNull()});(Ext.isIE8?xit:it)("should layout with hidden label and side error",function(){P({labelAlign:c,hideLabel:true,msgTarget:"side"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T},inputEl:{x:b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:X,y:0,w:M,h:R},errorEl:{x:X+H,y:(T-Y)/2,w:Y,h:Y}})});(Ext.isIE8?xit:it)("should layout with hidden label and hidden side error",function(){P({labelAlign:c,hideLabel:true,msgTarget:"side"});var e=(F&&!L)?X+M:X;expect(B).toHaveLayout({el:{w:(L&&F)?U-M:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:e,h:T},inputEl:{x:b,y:b,w:e-(b*2),h:T-(b*2)},errorWrapEl:{x:F?0:X,y:F?0:0,w:F?0:M,h:F?0:R},errorEl:{x:F?0:X+H,y:F?0:(T-Y)/2,w:F?0:Y,h:F?0:Y}})});(Ext.isIE10m&&!O?xit:it)("should layout with hidden label and under error",function(){P({labelAlign:c,hideLabel:true,msgTarget:"under"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T},inputEl:{x:b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:0,y:T,w:U,h:Q},errorEl:{x:0,y:T,w:U,h:Q}})})})}V("left");V("right");(Ext.isIE10m&&!O?xdescribe:describe)("top label",function(){it("should layout",function(){P({labelAlign:"top"});expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:U,h:I},".x-form-item-label-inner":{x:0,y:0,w:U,h:I},bodyEl:{x:0,y:I,w:X,h:T},inputEl:{x:b,y:I+b,w:X-(b*2),h:T-(b*2)}});expect(B.errorWrapEl).toBeNull()});it("should layout with side error",function(){P({labelAlign:"top",msgTarget:"side"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:U,h:I},".x-form-item-label-inner":{x:0,y:0,w:X,h:I},bodyEl:{x:0,y:I,w:X,h:T},inputEl:{x:b,y:I+b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:X,y:I,w:M,h:T},errorEl:{x:X+H,y:I+((T-Y)/2),w:Y,h:Y}})});it("should layout with hidden side error",function(){P({labelAlign:"top",msgTarget:"side"});U=(L&&F)?U-M:U;var c=(F&&!L)?X+M:X;expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:U,h:I},".x-form-item-label-inner":{x:0,y:0,w:c,h:I},bodyEl:{x:0,y:I,w:c,h:T},inputEl:{x:b,y:I+b,w:c-(b*2),h:T-(b*2)},errorWrapEl:{x:F?0:X,y:F?0:I,w:F?0:M,h:F?0:T},errorEl:{x:F?0:X+H,y:F?0:I+((T-Y)/2),w:F?0:Y,h:F?0:Y}})});it("should layout with under error",function(){P({labelAlign:"top",msgTarget:"under"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{x:0,y:0,w:U,h:I},".x-form-item-label-inner":{x:0,y:0,w:U,h:I},bodyEl:{x:0,y:I,w:X,h:T},inputEl:{x:b,y:I+b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:0,y:I+T,w:U,h:Q},errorEl:{x:0,y:I+T,w:U,h:Q}})});it("should layout with hidden label",function(){P({labelAlign:"top",hideLabel:true});expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T},inputEl:{x:b,y:b,w:X-(b*2),h:T-(b*2)}});expect(B.errorWrapEl).toBeNull()});it("should layout with hidden label and side error",function(){P({labelAlign:"top",hideLabel:true,msgTarget:"side"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T},inputEl:{x:b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:X,y:0,w:M,h:R},errorEl:{x:X+H,y:(T-Y)/2,w:Y,h:Y}})});it("should layout with hidden label and hidden side error",function(){P({labelAlign:"top",hideLabel:true,msgTarget:"side"});var c=(F&&!L)?X+M:X;expect(B).toHaveLayout({el:{w:(L&&F)?U-M:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:c,h:T},inputEl:{x:b,y:b,w:c-(b*2),h:T-(b*2)},errorWrapEl:{x:F?0:X,y:F?0:0,w:F?0:M,h:F?0:R},errorEl:{x:F?0:X+H,y:F?0:(T-Y)/2,w:F?0:Y,h:F?0:Y}})});it("should layout with hidden label and under error",function(){P({labelAlign:"top",hideLabel:true,msgTarget:"under"});J();expect(B).toHaveLayout({el:{w:U,h:R},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:X,h:T},inputEl:{x:b,y:b,w:X-(b*2),h:T-(b*2)},errorWrapEl:{x:0,y:T,w:U,h:Q},errorEl:{x:0,y:T,w:U,h:Q}})})})})}D(0,false);D(1,true);D(2,false);D(2,true);D(3,false);D(3,true)})})