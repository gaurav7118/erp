Ext.define("Ext.form.action.StandardSubmit",{extend:"Ext.form.action.Submit",alias:"formaction.standardsubmit",doSubmit:function(){var A=this.buildForm();A.formEl.submit();this.cleanup(A)}})