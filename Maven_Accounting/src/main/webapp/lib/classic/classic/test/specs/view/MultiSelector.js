describe("Ext.view.MultiSelector",function(){var J,A,N,P=true,G=Ext.data.ProxyStore.prototype.load,L;var C=["Ben","Don","Evan","Kevin","Nige","Phil","Ross","Ryan"],K=["Toll","Griffin","Trimboli","Krohe","White","Guerrant","Gerbasi","Smith"],I=[],M=37,B,H,F,E,O,D=0;for(H=0;H<K.length;++H){B={};I.push({id:++D,forename:(O=C[H]),surname:K[H]});B[O]=1;for(F=0;F<3;++F){do{E=M%C.length;M=M*1664525+1013904223;M&=2147483647}while(B[O=C[E]]);B[O]=1;I.push({id:++D,forename:O,surname:K[H]})}}beforeEach(function(){L=Ext.data.ProxyStore.prototype.load=function(){G.apply(this,arguments);if(P){this.flushLoad.apply(this,arguments)}return this};MockAjaxManager.addMethods();J=Ext.define("spec.Employee",{extend:"Ext.data.Model",fields:[{name:"id"},{name:"forename"},{name:"surname"},{name:"name",convert:function(Q,R){return R.editing?Q:R.get("forename")+" "+R.get("surname")}}]})});afterEach(function(){Ext.data.ProxyStore.prototype.load=G;MockAjaxManager.removeMethods();Ext.undefine("spec.Employee");Ext.data.Model.schema.clear();A.destroy()});it("should select the records in the searcher which match by ID the records in the selector",function(){var Q;A=new Ext.panel.Panel({renderTo:document.body,width:400,height:300,layout:"fit",store:{model:"spec.Employee",proxy:{type:"ajax",url:"foo"}},items:[{xtype:"multiselector",title:"Selected Employees",fieldName:"name",viewConfig:{deferEmptyText:false,emptyText:"No employees selected"},search:{field:"name",store:{model:"spec.Employee",autoLoad:true,proxy:{type:"ajax",url:"bar"}}}}]}),N=A.child("multiselector");N.store.load();Ext.Ajax.mockComplete({status:200,responseText:Ext.JSON.encode(I[0])});N.onShowSearch();waitsFor(function(){Q=N.searchPopup.child("gridpanel").store;return(Q instanceof Ext.data.Store)&&Q.isLoading()},"searchStore to kick off a load");runs(function(){Ext.Ajax.mockComplete({status:200,responseText:Ext.JSON.encode(I)})});waitsFor(function(){return Q.getCount()},"searchStore to complete load");runs(function(){expect(N.down("gridpanel").selModel.getSelection()[0].get("name")).toBe(N.store.getAt(0).get("name"))})})})