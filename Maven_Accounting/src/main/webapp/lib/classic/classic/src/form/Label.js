Ext.define("Ext.form.Label",{extend:"Ext.Component",alias:"widget.label",requires:["Ext.util.Format"],autoEl:"label",maskOnDisable:false,getElConfig:function(){var A=this;A.html=A.text?Ext.util.Format.htmlEncode(A.text):(A.html||"");return Ext.apply(A.callParent(),{htmlFor:A.forId||""})},setText:function(C,B){var A=this;B=B!==false;if(B){A.text=C;delete A.html}else{A.html=C;delete A.text}if(A.rendered){A.el.dom.innerHTML=B!==false?Ext.util.Format.htmlEncode(C):C;A.updateLayout()}return A}})