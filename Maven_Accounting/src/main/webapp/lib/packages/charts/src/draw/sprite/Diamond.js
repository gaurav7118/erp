Ext.define("Ext.draw.sprite.Diamond",{extend:"Ext.draw.sprite.Path",alias:"sprite.diamond",inheritableStatics:{def:{processors:{x:"number",y:"number",size:"number"},defaults:{x:0,y:0,size:4},triggers:{x:"path",y:"path",size:"path"}}},updatePath:function(D,B){var C=B.size*1.25,A=B.x-B.lineWidth/2,E=B.y;D.fromSvgString(["M",A,E-C,"l",C,C,-C,C,-C,-C,C,-C,"z"])}})