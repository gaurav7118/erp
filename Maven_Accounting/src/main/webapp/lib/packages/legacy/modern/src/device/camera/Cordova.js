Ext.define("Ext.device.camera.Cordova",{alternateClassName:"Ext.device.camera.PhoneGap",extend:"Ext.device.camera.Abstract",getPicture:function(D,B,A){try{navigator.camera.getPicture(D,B,A)}catch(C){alert(C)}},cleanup:function(C,A){try{navigator.camera.cleanup(C,A)}catch(B){alert(B)}},capture:function(H){var F=H.success,D=H.failure,J=H.scope,B=this.source,E=this.destination,G=this.encoding,A=H.source,I=H.destination,C=H.encoding,K={};if(J){F=Ext.Function.bind(F,J);D=Ext.Function.bind(D,J)}if(A!==undefined){K.sourceType=B.hasOwnProperty(A)?B[A]:A}if(I!==undefined){K.destinationType=E.hasOwnProperty(I)?E[I]:I}if(C!==undefined){K.encodingType=G.hasOwnProperty(C)?G[C]:C}if("quality" in H){K.quality=H.quality}if("width" in H){K.targetWidth=H.width}if("height" in H){K.targetHeight=H.height}this.getPicture(F,D,K)}})