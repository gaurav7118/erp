Ext.define("Ext.ux.colorpick.SelectorModel",{extend:"Ext.app.ViewModel",alias:"viewmodel.colorpick-selectormodel",requires:["Ext.ux.colorpick.ColorUtils"],data:{selectedColor:{r:255,g:255,b:255,h:0,s:1,v:1,a:1},previousColor:{r:0,g:0,b:0,h:0,s:1,v:1,a:1}},formulas:{hex:{get:function(C){var E=C("selectedColor.r").toString(16),D=C("selectedColor.g").toString(16),B=C("selectedColor.b").toString(16),A;A=Ext.ux.colorpick.ColorUtils.rgb2hex(E,D,B);return"#"+A},set:function(B){var A=Ext.ux.colorpick.ColorUtils.hex2rgb(B);this.changeRGB(A)}},red:{get:function(A){return A("selectedColor.r")},set:function(A){this.changeRGB({r:A})}},green:{get:function(A){return A("selectedColor.g")},set:function(A){this.changeRGB({g:A})}},blue:{get:function(A){return A("selectedColor.b")},set:function(A){this.changeRGB({b:A})}},hue:{get:function(A){return A("selectedColor.h")*360},set:function(A){this.changeHSV({h:A/360})}},saturation:{get:function(A){return A("selectedColor.s")*100},set:function(A){this.changeHSV({s:A/100})}},value:{get:function(B){var A=B("selectedColor.v");return A*100},set:function(A){this.changeHSV({v:A/100})}},alpha:{get:function(B){var A=B("selectedColor.a");return A*100},set:function(A){this.set("selectedColor",Ext.applyIf({a:A/100},this.data.selectedColor))}}},changeHSV:function(B){Ext.applyIf(B,this.data.selectedColor);var A=Ext.ux.colorpick.ColorUtils.hsv2rgb(B.h,B.s,B.v);B.r=A.r;B.g=A.g;B.b=A.b;this.set("selectedColor",B)},changeRGB:function(B){Ext.applyIf(B,this.data.selectedColor);var A=Ext.ux.colorpick.ColorUtils.rgb2hsv(B.r,B.g,B.b);B.h=A.h;B.s=A.s;B.v=A.v;this.set("selectedColor",B)}})