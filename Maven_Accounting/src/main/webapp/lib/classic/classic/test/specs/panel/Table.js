describe("Ext.panel.Table",function(){var C=function(E,D){A=Ext.create("Ext.data.Store",Ext.apply({storeId:"simpsonsStore",fields:["name","email","phone"],data:{"items":[{"name":"Lisa","email":"lisa@simpsons.com","phone":"555-111-1224"},{"name":"Bart","email":"bart@simpsons.com","phone":"555-222-1234"},{"name":"Homer","email":"homer@simpsons.com","phone":"555-222-1244"},{"name":"Marge","email":"marge@simpsons.com","phone":"555-222-1254"}]},proxy:{type:"memory",reader:{type:"json",rootProperty:"items"}}},E));B=Ext.create("Ext.grid.Panel",Ext.apply({title:"Simpsons",store:A,columns:[{header:"Name",dataIndex:"name",width:100},{header:"Email",dataIndex:"email",flex:1},{header:"Phone",dataIndex:"phone",flex:1,hidden:true}],height:200,width:400},D))},A,B;afterEach(function(){Ext.destroy(B);B=null});describe("forceFit",function(){it("should let the headerCt know it is part of a forceFit grid when header is a grid config",function(){C({},{forceFit:true});expect(B.forceFit).toBe(true);expect(B.headerCt.forceFit).toBe(true)});it("should let the headerCt know it is part of a forceFit grid when header is an instance",function(){C({},{forceFit:true,columns:new Ext.grid.header.Container({items:[{header:"Name",dataIndex:"name",width:100},{header:"Email",dataIndex:"email",flex:1},{header:"Phone",dataIndex:"phone",flex:1,hidden:true}]})});expect(B.forceFit).toBe(true);expect(B.headerCt.forceFit).toBe(true)})})})