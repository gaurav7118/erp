Ext.define("Ext.draw.overrides.sprite.Path",{override:"Ext.draw.sprite.Path",requires:["Ext.draw.Color"],isPointInPath:function(C,G){var B=this.attr;if(B.fillStyle===Ext.draw.Color.RGBA_NONE){return this.isPointOnPath(C,G)}var E=B.path,D=B.matrix,F,A;if(!D.isIdentity()){F=E.params.slice(0);E.transform(B.matrix)}A=E.isPointInPath(C,G);if(F){E.params=F}return A},isPointOnPath:function(C,G){var B=this.attr,E=B.path,D=B.matrix,F,A;if(!D.isIdentity()){F=E.params.slice(0);E.transform(B.matrix)}A=E.isPointOnPath(C,G);if(F){E.params=F}return A},hitTest:function(I,L){var E=this,C=E.attr,K=C.path,G=C.matrix,H=I[0],F=I[1],D=E.callParent([I,L]),J=null,A,B;if(!D){return J}L=L||Ext.draw.sprite.Sprite.defaultHitTestOptions;if(!G.isIdentity()){A=K.params.slice(0);K.transform(C.matrix)}if(L.fill&&L.stroke){B=C.fillStyle!==Ext.draw.Color.NONE&&C.fillStyle!==Ext.draw.Color.RGBA_NONE;if(B){if(K.isPointInPath(H,F)){J={sprite:E}}}else{if(K.isPointInPath(H,F)||K.isPointOnPath(H,F)){J={sprite:E}}}}else{if(L.stroke&&!L.fill){if(K.isPointOnPath(H,F)){J={sprite:E}}}else{if(L.fill&&!L.stroke){if(K.isPointInPath(H,F)){J={sprite:E}}}}}if(A){K.params=A}return J},getIntersections:function(J){if(!(J.isSprite&&J.isPath)){return[]}var E=this.attr,D=J.attr,I=E.path,H=D.path,G=E.matrix,A=D.matrix,C,F,B;if(!G.isIdentity()){C=I.params.slice(0);I.transform(E.matrix)}if(!A.isIdentity()){F=H.params.slice(0);H.transform(D.matrix)}B=I.getIntersections(H);if(C){I.params=C}if(F){H.params=F}return B}})