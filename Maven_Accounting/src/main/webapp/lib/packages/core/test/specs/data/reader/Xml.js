describe("Ext.data.reader.Xml",function(){var F,H,B,K,E,D,C,J=document,G=function G(M){if(window.ActiveXObject){var L=new ActiveXObject("Microsoft.XMLDOM");L.loadXML(M);return L}else{if(window.DOMParser){return(new DOMParser()).parseFromString(M,"text/xml")}}return""},A=function(L){L="<root>"+L+"</root>";return G(L)},I;beforeEach(function(){I=Ext.DomQuery;Ext.DomQuery={isXml:function(L){var M=(L?L.ownerDocument||L:0).documentElement;return M?M.nodeName!=="HTML":false},selectNode:function(M,L){return Ext.DomQuery.select(M,L,null,true)[0]},select:function(N,L,M,O){if(typeof L=="string"){return[]}if(J.querySelectorAll&&!I.isXml(L)){return O?[L.querySelector(N)]:Ext.Array.toArray(L.querySelectorAll(N))}else{return I.jsSelect.call(this,N,L,M)}}}});afterEach(function(){Ext.DomQuery=I;if(F){F.destroy()}F=null});describe("raw data",function(){var N,L,M;beforeEach(function(){N=Ext.define("spec.Xml",{extend:"Ext.data.Model",fields:["name"]});L=A("<dog><name>Utley</name></dog><dog><name>Molly</name></dog>");F=new Ext.data.reader.Xml({model:"spec.Xml",record:"dog"})});afterEach(function(){Ext.data.Model.schema.clear(true);Ext.undefine("spec.Xml");M=L=N=null});it("should not set raw data reference by default",function(){M=F.readRecords(L).getRecords()[0];expect(M.raw).not.toBeDefined()});it("should set raw data reference for a TreeStore record",function(){spec.Xml.prototype.isNode=true;M=F.readRecords(L).getRecords()[0];expect(M.raw).toBe(L.firstChild.firstChild)})});describe("copyFrom",function(){var L=Ext.define(null,{extend:"Ext.data.Model"});it("should copy the model",function(){var M=new Ext.data.reader.Xml({model:L});var N=new Ext.data.reader.Xml();N.copyFrom(M);expect(N.getModel()).toBe(L)});it("should copy the record",function(){var N=new Ext.data.reader.Xml({model:L,record:"foo"});var O=new Ext.data.reader.Xml();O.copyFrom(N);expect(O.getRecord()).toBe("foo");var M=N.read(A("<foo /><foo /><foo /><bar />"));expect(M.getCount()).toBe(3)});it("should copy the totalProperty",function(){var N=new Ext.data.reader.Xml({model:L,totalProperty:"aTotal",record:"foo"});var O=new Ext.data.reader.Xml();O.copyFrom(N);expect(O.getTotalProperty()).toBe("aTotal");var M=N.read(A("<aTotal>1000</aTotal>"));expect(M.getTotal()).toBe(1000)});it("should copy the successProperty",function(){var N=new Ext.data.reader.Xml({model:L,successProperty:"aSuccess",record:"foo"});var O=new Ext.data.reader.Xml();O.copyFrom(N);expect(O.getSuccessProperty()).toBe("aSuccess");var M=N.read(A("<aSuccess>false</aSuccess>"));expect(M.getSuccess()).toBe(false)});it("should copy the messageProperty",function(){var N=new Ext.data.reader.Xml({model:L,messageProperty:"aMessage",record:"foo"});var O=new Ext.data.reader.Xml();O.copyFrom(N);expect(O.getMessageProperty()).toBe("aMessage");var M=N.read(A("<aMessage>Some Message</aMessage>"));expect(M.getMessage()).toBe("Some Message")});it("should copy the rootProperty",function(){var N=new Ext.data.reader.Xml({model:L,rootProperty:"aRoot",record:"foo"});var O=new Ext.data.reader.Xml();O.copyFrom(N);expect(O.getRootProperty()).toBe("aRoot");var M=N.read(A("<notRoot><foo /><foo /><foo /></notRoot><aRoot><foo /></aRoot>"));expect(M.getCount()).toBe(1)})});describe("extractors",function(){function L(M){Ext.define("spec.FooXmlTest",{extend:"Ext.data.Model",fields:["field"]});M=M||{};F=new Ext.data.reader.Xml(Ext.apply({model:"spec.FooXmlTest"},M))}afterEach(function(){Ext.data.Model.schema.clear();Ext.undefine("spec.FooXmlTest")});it("should run function extractors in the reader scope",function(){var M;L({successProperty:function(){M=this;return true}});F.getSuccess({success:true});expect(M).toBe(F)});describe("getTotal",function(){it("should default to total",function(){L();expect(F.getTotal(A("<total>10</total>"))).toBe("10")});it("should have no getTotal method if the totalProperty isn't specified",function(){L({totalProperty:""});expect(F.getTotal).toBeUndefined()});it("should read the specified property name",function(){L({totalProperty:"foo"});expect(F.getTotal(A("<foo>17</foo>"))).toBe("17")});it("should accept a function configuration",function(){L({totalProperty:function(M){return this.getNodeValue(M.firstChild.childNodes[2])}});expect(F.getTotal(A("<node1>1</node1><node2>2</node2><node3>3</node3>"))).toBe("3")});xit("should be able to use some xpath",function(){L({totalProperty:"foo/bar"});expect(F.getTotal(A("<foo><bar>18</bar></foo>"))).toBe("18")});xit("should support attribute reading",function(){L({totalProperty:"@total"});expect(F.getTotal(G('<node total="11" />').firstChild)).toBe("11")})});describe("getSuccess",function(){it("should default to success",function(){L();expect(F.getSuccess(A("<success>true</success>"))).toBe("true")});it("should have no getSuccess method if the successProperty isn't specified",function(){L({successProperty:""});expect(F.getSuccess).toBeUndefined()});it("should read the specified property name",function(){L({successProperty:"foo"});expect(F.getSuccess(A("<foo>false</foo>"))).toBe("false")});it("should accept a function configuration",function(){L({successProperty:function(M){return this.getNodeValue(M.firstChild.childNodes[0])}});expect(F.getSuccess(A("<node1>true</node1><node2>false</node2><node3>false</node3>"))).toBe("true")});xit("should be able to use some xpath",function(){L({successProperty:"a/node/path"});expect(F.getSuccess(A("<a><node><path>false</path></node></a>"))).toBe("false")});xit("should support attribute reading",function(){L({totalProperty:"@success"});expect(F.getTotal(G('<node success="true" />').firstChild)).toBe("true")})});describe("getMessage",function(){it("should default to undefined",function(){L();expect(F.getMessage).toBeUndefined()});it("should have no getMessage method if the messageProperty isn't specified",function(){L({messageProperty:""});expect(F.getMessage).toBeUndefined()});it("should read the specified property name",function(){L({messageProperty:"foo"});expect(F.getMessage(A("<foo>a msg</foo>"))).toBe("a msg")});it("should accept a function configuration",function(){L({messageProperty:function(M){return this.getNodeValue(M.firstChild.childNodes[1])}});expect(F.getMessage(A("<node1>msg1</node1><node2>msg2</node2><node3>msg3</node3>"))).toBe("msg2")});xit("should be able to use some xpath",function(){L({messageProperty:"some/nodes"});expect(F.getMessage(A("<some><nodes>message here</nodes></some>"))).toBe("message here")});xit("should support attribute reading",function(){L({totalProperty:"@message"});expect(F.getTotal(G('<node message="attribute msg" />').firstChild)).toBe("attribute msg")})});describe("fields",function(){var M={recordCreator:Ext.identityFn};function N(O,P){Ext.define("spec.XmlFieldTest",{extend:"Ext.data.Model",fields:O});F=new Ext.data.reader.Xml(Ext.apply({model:"spec.XmlFieldTest",record:"root"},P))}afterEach(function(){Ext.data.Model.schema.clear();Ext.undefine("spec.XmlFieldTest")});it("should read the name if no mapping is specified",function(){N(["field"]);var O=F.readRecords(A("<field>val</field>").firstChild,M).getRecords()[0];expect(O.field).toBe("val")});it("should give precedence to the mapping",function(){N([{name:"field",mapping:"other"}]);var O=F.readRecords(A("<field>val</field><other>real value</other>").firstChild,M).getRecords()[0];expect(O.field).toBe("real value")});it("should handle dot notation mapping with nested undefined properties",function(){N([{name:"field",mapping:"some.nested.property"}]);var O=F.readRecords(A("<foo>val</foo>").firstChild,M).getRecords()[0];expect(O.field).toBeUndefined()});it("should accept a function",function(){N([{name:"field",mapping:function(P){return F.getNodeValue(P.childNodes[1])}}]);var O=F.readRecords(A("<node1>a</node1><node2>b</node2><node3>c</node3>"),M).getRecords()[0];expect(O.field).toBe("b")});xit("should allow basic xpath",function(){N([{name:"field",mapping:"some/xpath/here"}]);var O=F.readRecords(A("<some><xpath><here>a value</here></xpath></some>"),M).getRecords()[0];expect(O.field).toBe("a value")});xit("should support attribute reading",function(){N([{name:"field",mapping:"@other"}]);var O=F.readRecords(G('<node other="attr value" />').firstChild,M).getRecords()[0];expect(O.field).toBe("attr value")});xit("should read fields from xml nodes that have a namespace prefix",function(){N(["field"],{namespace:"n"});var O=F.readRecords(A('<n:field xmlns:n="nns">val</n:field>').firstChild,M).getRecords()[0];expect(O.field).toBe("val")});xit("should read field data from a mapped xml node with namespace prefix",function(){N([{name:"field",mapping:"m|other"}]);var O=F.readRecords(A('<n:field xmlns:n="nns">val</n:field><m:other xmlns:m="mns">real value</m:other>').firstChild,M).getRecords()[0];expect(O.field).toBe("real value")})})});xdescribe("reading data",function(){var M,L;beforeEach(function(){Ext.define("spec.XmlReader",{extend:"Ext.data.Model",fields:[{name:"id",mapping:"idProp",type:"int"},{name:"name",mapping:"FullName",type:"string"},{name:"email",mapping:"@email",type:"string"}]});F=new Ext.data.reader.Xml({root:"data",totalProperty:"totalProp",messageProperty:"messageProp",successProperty:"successProp",model:"spec.XmlReader",record:"user"});C=new MockAjax();H=["<results>","<totalProp>2300</totalProp>","<successProp>true</successProp>","<messageProp>It worked</messageProp>","<data>",'<user email="ed@sencha.com">',"<idProp>123</idProp>","<FullName>Ed Spencer</FullName>","</user>","</data>","</results>"].join("");C.complete({status:200,statusText:"OK",responseText:H,responseHeaders:{"Content-type":"application/xml"}});M=F.read(C);L=M.getRecords()[0]});afterEach(function(){Ext.data.Model.schema.clear();Ext.undefine("spec.XmlReader")});it("should extract the correct total",function(){expect(M.getTotal()).toBe(2300)});it("should extract success",function(){expect(M.getSuccess()).toBe(true)});it("should extract count",function(){expect(M.getCount()).toBe(1)});it("should extract the message",function(){expect(M.getMessage()).toBe("It worked")});it("should extract the id",function(){expect(L.getId()).toBe(123)});it("should respect field mappings",function(){expect(L.get("name")).toBe("Ed Spencer")});it("should respect field mappings containing @",function(){expect(L.get("email")).toBe("ed@sencha.com")})});xdescribe("loading nested data",function(){beforeEach(function(){C=new MockAjax();H=["<users>","<user>","<id>123</id>","<name>Ed</name>","<orders>","<order>","<id>50</id>","<total>100</total>","<order_items>","<order_item>","<id>20</id>","<price>40</price>","<quantity>2</quantity>","<product>","<id>1000</id>","<name>MacBook Pro</name>","</product>","</order_item>","<order_item>","<id>21</id>","<price>20</price>","<quantity>1</quantity>","<product>","<id>1001</id>","<name>iPhone</name>","</product>","</order_item>","</order_items>","</order>","<order>","<id>51</id>","<total>10</total>","<order_items>","<order_item>","<id>22</id>","<price>10</price>","<quantity>1</quantity>","<product>","<id>1002</id>","<name>iPad</name>","</product>","</order_item>","</order_items>","</order>","</orders>","</user>","</users>"].join("");C.complete({status:200,statusText:"OK",responseText:H});Ext.define("spec.User",{extend:"Ext.data.Model",fields:["id","name"],hasMany:{model:"spec.Order",name:"orders"},proxy:{type:"rest",reader:{type:"xml",root:"users"}}});Ext.define("spec.Order",{extend:"Ext.data.Model",fields:["id","total"],hasMany:{model:"spec.OrderItem",name:"orderItems",associationKey:"order_items"},belongsTo:"spec.User",proxy:{type:"memory",reader:{type:"xml",root:"orders",record:"order"}}});Ext.define("spec.OrderItem",{extend:"Ext.data.Model",fields:["id","price","quantity","order_id","product_id"],belongsTo:["spec.Order",{model:"spec.Product",getterName:"getProduct",associationKey:"product"}],proxy:{type:"memory",reader:{type:"xml",root:"order_items",record:"order_item"}}});Ext.define("spec.Product",{extend:"Ext.data.Model",fields:["id","name"],hasMany:{model:"spec.OrderItem",name:"orderItems"},proxy:{type:"memory",reader:{type:"xml",record:"product"}}});K=function(L){return new Ext.data.reader.Xml(Ext.apply({},L,{model:"spec.User",root:"users",record:"user"}))}});afterEach(function(){Ext.data.Model.schema.clear();Ext.undefine("spec.User");Ext.undefine("spec.Order");Ext.undefine("spec.OrderItem");Ext.undefine("spec.Product")});it("should set implicitIncludes to true by default",function(){F=K();expect(F.getImplicitIncludes()).toBe(true)});it("should not parse includes if implicitIncludes is set to false",function(){F=K({implicitIncludes:false});B=F.read(C);E=B.records[0];D=E.orders();expect(D.getCount()).toEqual(0)});describe("when reading nested data",function(){beforeEach(function(){F=K();B=F.read(C);E=B.records[0];D=E.orders()});it("should populate first-order associations",function(){expect(D.getCount()).toEqual(2)});it("should populate second-order associations",function(){var L=D.first();expect(L.orderItems().getCount()).toEqual(2)});it("should populate belongsTo associations",function(){var L=D.first(),M=L.orderItems().first(),N=M.getProduct();expect(N.get("name")).toEqual("MacBook Pro")})})});describe("reading xhr",function(){var M="<users><success>true</success><user><name>Ben</name><location>Boston</location></user><user><name>Mike</name><location>Redwood City</location></user><user><name>Nick</name><location>Kansas City</location></user></users>",N={responseText:"something",responseXML:G(M)},O={responseText:"something",responseXML:null};beforeEach(function(){Ext.define("spec.User",{extend:"Ext.data.Model",fields:["name","location"]});F=new Ext.data.reader.Xml({record:"user",model:"spec.User",listeners:{exception:function(P,Q,S,R){}}});spyOn(F,"readRecords").andCallThrough();spyOn(F,"getResponseData").andCallThrough()});afterEach(function(){Ext.data.Model.schema.clear();Ext.undefine("spec.User")});function L(P){return F.read(P)}describe("if there is a responseXML property",function(){describe("if there is valid XML",function(){it("should call readRecords",function(){L(N);expect(F.readRecords).toHaveBeenCalled()});it("should be successful",function(){expect(L(N).getSuccess()).toBe(true)});it("should return the expected number of records",function(){expect(L(N).getCount()).toBe(3)});it("should not return a non-empty dataset",function(){expect(L(N).getRecords().length).toBeGreaterThan(0)})});describe("if there is invalid XML",function(){beforeEach(function(){spyOn(Ext,"log");spyOn(Ext.Logger,"log")});it("should not call readRecords",function(){L(O);expect(F.readRecords).not.toHaveBeenCalled()});it("should not be successful",function(){expect(L(O).getSuccess()).toBe(false)});it("should not return any records",function(){expect(L(O).getTotal()).toBe(0)});it("should return any empty dataset",function(){expect(L(O).getRecords().length).toBe(0)})})});describe("if there is no responseText property",function(){beforeEach(function(){L("something")});it("should not call readRecords",function(){expect(F.getResponseData).not.toHaveBeenCalled()})})})})