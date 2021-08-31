Ext.define("Ext.ux.colorpick.SliderValue",{extend:"Ext.ux.colorpick.Slider",alias:"widget.colorpickerslidervalue",cls:Ext.baseCSSPrefix+"colorpicker-value",requires:["Ext.XTemplate"],gradientStyleTpl:Ext.create("Ext.XTemplate",Ext.isIE&&Ext.ieVersion<10?"filter: progid:DXImageTransform.Microsoft.gradient(GradientType=0, startColorstr='#{hex}', endColorstr='#000000');":"background: -mox-linear-gradient(top, #{hex} 0%, #000000 100%);background: -webkit-linear-gradient(top, #{hex} 0%,#000000 100%);background: -o-linear-gradient(top, #{hex} 0%,#000000 100%);background: -ms-linear-gradient(top, #{hex} 0%,#000000 100%);background: linear-gradient(to bottom, #{hex} 0%,#000000 100%);"),setValue:function(G){var D=this,B=D.getDragContainer(),A=D.getDragHandle(),F=B.getEl(),E=F.getHeight(),C,H;if(!A.dd||!A.dd.constrain){return }if(typeof A.dd.dragEnded!=="undefined"&&!A.dd.dragEnded){return }C=1-(G/100);H=E*C;A.getEl().setStyle({top:H+"px"})},setHue:function(B){var E=this,A=E.getDragContainer(),C,D;if(!E.getEl()){return }C=Ext.ux.colorpick.ColorUtils.hsv2rgb(B,1,1);D=Ext.ux.colorpick.ColorUtils.rgb2hex(C.r,C.g,C.b);A.getEl().applyStyles(E.gradientStyleTpl.apply({hex:D}))}})