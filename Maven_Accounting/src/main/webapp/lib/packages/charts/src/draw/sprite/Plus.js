Ext.define("Ext.draw.sprite.Plus",{extend:"Ext.draw.sprite.Path",alias:"sprite.plus",inheritableStatics:{def:{processors:{x:"number",y:"number",size:"number"},defaults:{x:0,y:0,size:4},triggers:{x:"path",y:"path",size:"path"}}},updatePath:function(D,B){var C=B.size/1.3,A=B.x-B.lineWidth/2,E=B.y;D.fromSvgString("M".concat(A-C/2,",",E-C/2,"l",[0,-C,C,0,0,C,C,0,0,C,-C,0,0,C,-C,0,0,-C,-C,0,0,-C,"z"]))}})