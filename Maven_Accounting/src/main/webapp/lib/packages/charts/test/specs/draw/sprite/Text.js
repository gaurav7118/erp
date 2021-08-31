describe("Ext.draw.sprite.Text",function(){var A=Ext.draw.sprite.Text.prototype;describe("makeFontShorthand",function(){var B={fontVariant:"small-caps",fontStyle:"italic",fontWeight:"bold",fontSize:"34px/100px",fontFamily:'"Times New Roman", serif'};it("should not have a leading or trailing space",function(){var D="";var C={setAttributes:function(E){D=E.font}};A.makeFontShorthand.call(C,B);expect(D.length).toEqual(Ext.String.trim(D).length)});it("should list all available values in the preferred order",function(){var D;var C={setAttributes:function(E){D=E.font}};A.makeFontShorthand.call(C,B);expect(D).toEqual('italic small-caps bold 34px/100px "Times New Roman", serif')});it("needs to contain at least font-size and font-family",function(){var C=new Ext.draw.sprite.Text();C.setAttributes({fontWeight:"bold",fontStyle:"italic"});expect(C.attr.font).toEqual("italic bold 10px sans-serif")})});describe("parseFontShorthand",function(){it('needs to handle "normal" values properly',function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"normal 24px Verdana"});expect(B.attr.fontStyle).toEqual("");expect(B.attr.fontVariant).toEqual("");expect(B.attr.fontWeight).toEqual("");expect(B.attr.fontSize).toEqual("24px");expect(B.attr.fontFamily).toEqual("Verdana")});it('should ignore the "inherit" values',function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"inherit 24px Verdana"});expect(B.attr.fontStyle).toEqual("");expect(B.attr.fontVariant).toEqual("");expect(B.attr.fontWeight).toEqual("");expect(B.attr.fontSize).toEqual("24px");expect(B.attr.fontFamily).toEqual("Verdana")});it("should support font names with spaces in them",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:'x-large/110% "New Century Schoolbook", serif'});expect(B.attr.fontFamily).toEqual('"New Century Schoolbook", serif')});it("should support font families with more than one font name",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"italic small-caps normal 13px/150% Arial, Helvetica, sans-serif"});expect(B.attr.fontFamily).toEqual("Arial, Helvetica, sans-serif")});it("should be able to handle fontSize/lineHeight values by extracting fontSize and discarding lineHeigh",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:'x-large/110% "New Century Schoolbook", serif'});expect(B.attr.fontSize).toEqual("x-large")});it("should recognize percentage font sizes",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"80% sans-serif"});expect(B.attr.fontSize).toEqual("80%")});it("should recognize absolute font sizes",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"small serif"});expect(B.attr.fontSize).toEqual("small")});it("should recognize font weight values",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"italic 600 large Palatino, serif"});expect(B.attr.fontWeight).toEqual("600")});it("should recognize font variant values",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"normal small-caps 120%/120% fantasy"});expect(B.attr.fontVariant).toEqual("small-caps")});it("should recognize font style values",function(){var B=new Ext.draw.sprite.Text();B.setAttributes({font:"bold large oblique Palatino, serif"});expect(B.attr.fontStyle).toEqual("oblique")})});describe("fontWeight processor",function(){var C=Ext.draw.sprite.Text.def,B=C.getProcessors().fontWeight;B=Ext.Function.bind(B,C);it("should return an empty string for unrecognized values",function(){var M=B(Infinity),L=B(-Infinity),K=B(101),J=B("hello"),I=B("505"),H=B(NaN),G=B(null),F=B(undefined),E=B(true),D=B(false);expect(M).toEqual("");expect(L).toEqual("");expect(K).toEqual("");expect(J).toEqual("");expect(I).toEqual("");expect(H).toEqual("");expect(G).toEqual("");expect(F).toEqual("");expect(E).toEqual("");expect(D).toEqual("")});it("should accept strings that can be parsed to a valid number",function(){var D=B("700");expect(D).toEqual("700")});it("should always return a string",function(){var D=B(400);expect(D).toEqual("400")});it("should only accept numbers that are multiples of 100 in the [100,900] interval",function(){var E=B(300),D=B(350),G=B(0),F=B(1000);expect(E).toEqual("300");expect(D).toEqual("");expect(G).toEqual("");expect(F).toEqual("")})})})