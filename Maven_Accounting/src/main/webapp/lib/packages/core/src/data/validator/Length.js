Ext.define("Ext.data.validator.Length",{extend:"Ext.data.validator.Bound",alias:"data.validator.length",type:"length",config:{minOnlyMessage:"Length must be at least {0}",maxOnlyMessage:"Length must be no more than {0}",bothMessage:"Length must be between {0} and {1}"},getValue:function(A){return String(A).length}})