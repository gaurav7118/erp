Ext.define("Ext.draw.sprite.Tick",{extend:"Ext.draw.sprite.Line",alias:"sprite.tick",inheritableStatics:{def:{processors:{x:"number",y:"number",size:"number"},defaults:{x:0,y:0,size:4},triggers:{x:"tick",y:"tick",size:"tick"},updaters:{tick:function(B){var D=B.size*1.5,C=B.lineWidth/2,A=B.x,E=B.y;this.setAttributes({fromX:A-C,fromY:E-D,toX:A-C,toY:E+D})}}}}})