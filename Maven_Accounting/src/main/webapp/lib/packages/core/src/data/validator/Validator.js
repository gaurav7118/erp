Ext.define("Ext.data.validator.Validator",{mixins:["Ext.mixin.Factoryable"],alias:"data.validator.base",isValidator:true,type:"base",statics:{all:{},register:function(B,A){var C=this.all;C[B.toUpperCase()]=C[B.toLowerCase()]=C[B]=A.prototype}},onClassExtended:function(A,B){if(B.type){Ext.data.validator.Validator.register(B.type,A)}},constructor:function(A){if(typeof A==="function"){this.fnOnly=true;this.validate=A}else{this.initConfig(A)}},validate:function(){return true},clone:function(){var A=this;if(A.fnOnly){return new Ext.data.validator.Validator(A.validate)}return new A.self(A.getCurrentConfig())}},function(){this.register(this.prototype.type,this)})