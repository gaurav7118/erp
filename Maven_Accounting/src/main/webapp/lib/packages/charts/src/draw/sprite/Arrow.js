Ext.define("Ext.draw.sprite.Arrow",{extend:"Ext.draw.sprite.Path",alias:"sprite.arrow",inheritableStatics:{def:{processors:{x:"number",y:"number",size:"number"},defaults:{x:0,y:0,size:4},triggers:{x:"path",y:"path",size:"path"}}},updatePath:function(D,B){var C=B.size*1.5,A=B.x-B.lineWidth/2,E=B.y;D.fromSvgString("M".concat(A-C*0.7,",",E-C*0.4,"l",[C*0.6,0,0,-C*0.4,C,C*0.8,-C,C*0.8,0,-C*0.4,-C*0.6,0],"z"))}})