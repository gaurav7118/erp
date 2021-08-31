Ext.define("Ext.fx.animation.Abstract",{extend:"Ext.Evented",isAnimation:true,requires:["Ext.fx.State"],config:{name:"",element:null,before:null,from:{},to:{},after:null,states:{},duration:300,easing:"linear",iteration:1,direction:"normal",delay:0,onBeforeStart:null,callback:null,onEnd:null,onBeforeEnd:null,scope:null,reverse:null,preserveEndState:false,replacePrevious:true},STATE_FROM:"0%",STATE_TO:"100%",DIRECTION_UP:"up",DIRECTION_DOWN:"down",DIRECTION_LEFT:"left",DIRECTION_RIGHT:"right",stateNameRegex:/^(?:[\d\.]+)%$/,constructor:function(){this.states={};this.callParent(arguments);return this},applyElement:function(A){return Ext.get(A)},applyBefore:function(A,B){if(A){return Ext.factory(A,Ext.fx.State,B)}},applyAfter:function(B,A){if(B){return Ext.factory(B,Ext.fx.State,A)}},setFrom:function(A){return this.setState(this.STATE_FROM,A)},setTo:function(A){return this.setState(this.STATE_TO,A)},getFrom:function(){return this.getState(this.STATE_FROM)},getTo:function(){return this.getState(this.STATE_TO)},setStates:function(A){var C=this.stateNameRegex,B;for(B in A){if(C.test(B)){this.setState(B,A[B])}}return this},getStates:function(){return this.states},updateCallback:function(A){if(A){this.setOnEnd(A)}},end:function(){this.stop()},stop:function(){this.fireEvent("stop",this)},destroy:function(){this.stop();this.callParent()},setState:function(B,D){var A=this.getStates(),C;C=Ext.factory(D,Ext.fx.State,A[B]);if(C){A[B]=C}else{if(B===this.STATE_TO){Ext.Logger.error("Setting and invalid '100%' / 'to' state of: "+D)}}return this},getState:function(A){return this.getStates()[A]},getData:function(){var G=this,L=G.getStates(),E={},H=G.getBefore(),C=G.getAfter(),I=L[G.STATE_FROM],J=L[G.STATE_TO],K=I.getData(),F=J.getData(),D,B,A;for(B in L){if(L.hasOwnProperty(B)){A=L[B];D=A.getData();E[B]=D}}return{before:H?H.getData():{},after:C?C.getData():{},states:E,from:K,to:F,duration:G.getDuration(),iteration:G.getIteration(),direction:G.getDirection(),easing:G.getEasing(),delay:G.getDelay(),onEnd:G.getOnEnd(),onBeforeEnd:G.getOnBeforeEnd(),onBeforeStart:G.getOnBeforeStart(),scope:G.getScope(),preserveEndState:G.getPreserveEndState(),replacePrevious:G.getReplacePrevious()}}})