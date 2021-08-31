Ext.define("Ext.form.action.Submit",{extend:"Ext.form.action.Action",alternateClassName:"Ext.form.Action.Submit",alias:"formaction.submit",type:"submit",run:function(){var B=this,A=B.form;if(B.clientValidation===false||A.isValid()){B.doSubmit()}else{B.failureType=Ext.form.action.Action.CLIENT_INVALID;A.afterAction(B,false)}},doSubmit:function(){var D=this,B=Ext.apply(D.createCallback(),{url:D.getUrl(),method:D.getMethod(),headers:D.headers}),C=D.form,E=D.jsonSubmit||C.jsonSubmit,A=E?"jsonData":"params",F;if(C.hasUpload()){F=D.buildForm();B.form=F.formEl;B.isUpload=true}else{B[A]=D.getParams(E)}Ext.Ajax.request(B);if(F){D.cleanup(F)}},cleanup:function(G){var E=G.formEl,D=G.uploadEls,B=G.uploadFields,A=B.length,C,F;for(C=0;C<A;++C){F=B[C];if(!F.clearOnSubmit){F.restoreInput(D[C])}}if(E){Ext.removeNode(E)}},getParams:function(D){var C=false,B=this.callParent(),A=this.form.getValues(C,C,this.submitEmptyText!==C,D,true);return Ext.apply({},A,B)},buildForm:function(){var I=this,L=[],J,Q,G=I.form,D=I.getParams(),C=[],A=[],F=G.getFields().items,E,H=F.length,K,P,N,O,M,B;for(E=0;E<H;++E){K=F[E];if(K.isFileUpload()){C.push(K)}}for(P in D){if(D.hasOwnProperty(P)){N=D[P];if(Ext.isArray(N)){M=N.length;for(O=0;O<M;O++){L.push(I.getFieldConfig(P,N[O]))}}else{L.push(I.getFieldConfig(P,N))}}}J={tag:"form",role:"presentation",action:I.getUrl(),method:I.getMethod(),target:I.target?(Ext.isString(I.target)?I.target:Ext.fly(I.target).dom.name):"_self",style:"display:none",cn:L};if(!J.target){Ext.raise("Invalid form target.")}if(C.length){J.encoding=J.enctype="multipart/form-data"}Q=Ext.DomHelper.append(Ext.getBody(),J);H=C.length;for(E=0;E<H;++E){B=C[E].extractFileInput();Q.appendChild(B);A.push(B)}return{formEl:Q,uploadFields:C,uploadEls:A}},getFieldConfig:function(A,B){return{tag:"input",type:"hidden",name:A,value:Ext.String.htmlEncode(B)}},onSuccess:function(B){var D=this.form,C=D&&!D.destroying&&!D.destroyed,E=true,A=this.processResponse(B);if(A!==true&&!A.success){if(A.errors&&C){D.markInvalid(A.errors)}this.failureType=Ext.form.action.Action.SERVER_INVALID;E=false}if(C){D.afterAction(this,E)}},handleResponse:function(D){var A=this.form,C=A.errorReader,E,I,F,G,B,J;if(C){E=C.read(D);B=E.records;I=[];if(B){for(F=0,G=B.length;F<G;F++){I[F]=B[F].data}}if(I.length<1){I=null}J={success:E.success,errors:I}}else{try{J=Ext.decode(D.responseText)}catch(H){J={success:false,errors:[]}}}return J}})