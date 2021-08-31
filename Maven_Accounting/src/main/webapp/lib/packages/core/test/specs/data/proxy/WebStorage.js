describe("Ext.data.proxy.WebStorage",function(){var C,B;var A={items:{},getItem:function(D){return this.items[D]||null},setItem:function(D,E){this.items[D]=E+""},removeItem:function(D){delete this.items[D]},clear:function(){this.items={}}};beforeEach(function(){Ext.define("spec.User",{extend:"Ext.data.Model",fields:[{name:"id",type:"int"},{name:"name",type:"string"},{name:"age",type:"int"}]});Ext.define("spec.Storage",{extend:"Ext.data.proxy.WebStorage",getStorageObject:function(){return A}})});afterEach(function(){A.clear();Ext.undefine("spec.User");Ext.undefine("spec.Storage");Ext.data.Model.schema.clear()});describe("getIds",function(){beforeEach(function(){spyOn(A,"getItem").andCallThrough();A.setItem("wsId","1,2,3");C=new spec.Storage({id:"wsId",model:spec.User})});it("should retrieve the list of ids from the storage object",function(){expect(A.getItem).toHaveBeenCalledWith("wsId")});it("should return an array",function(){expect(Ext.isArray(C.getIds())).toBe(true)});describe("if the id field is is not a string field",function(){it("should return each array item as a number",function(){var E=C.getIds(),F=E.length,D;for(D=0;D<F;D++){expect(typeof E[D]==="number").toBe(true)}})});describe("if the id field is a string field",function(){beforeEach(function(){spec.User=Ext.define(null,{extend:"Ext.data.Model",fields:[{name:"id",type:"string"}]});C=new spec.Storage({id:"wsId",model:spec.User})});it("should return each array item as a string",function(){var E=C.getIds(),F=E.length,D;for(D=0;D<F;D++){expect(typeof E[D]==="string").toBe(true)}})})});describe("getNextId",function(){beforeEach(function(){A.setItem(C.getRecordCounterKey(),"3");C=new spec.Storage({id:"wsId",model:spec.User})});it("should increment the counter in the storage object",function(){C.getNextId();expect(A.getItem(C.getRecordCounterKey())).toEqual("4")});describe("if the id field is is not a string field",function(){it("should return an incremented id as a number",function(){expect(C.getNextId()).toEqual(4)})});describe("when the id field is a string field",function(){beforeEach(function(){spec.User=Ext.define(null,{extend:"Ext.data.Model",fields:[{name:"id",type:"string"}]});C=new spec.Storage({id:"wsId",model:spec.User})});it("should return a string",function(){expect(C.getNextId()).toEqual("4")})})});describe("instantiation with id configuration option and methods",function(){B={id:"User"};beforeEach(function(){C=new spec.Storage(B)});describe("instantiation",function(){it("should set id",function(){expect(C.getId()).toEqual("User")});it("should extend Ext.data.proxy.Client",function(){expect(C.superclass.superclass).toEqual(Ext.data.proxy.Client.prototype)});it("should test getStorageObject in constructor",function(){expect(C.getStorageObject()).toBe(A)})});describe("methods",function(){describe("getRecordKey",function(){var D;beforeEach(function(){Ext.define("spec.Human",{extend:"Ext.data.Model",fields:[{name:"name",type:"string"},{name:"age",type:"int"},{name:"planet",type:"string",defaultValue:"Earth"}]});D=new spec.Human({id:1,name:"Nicolas",age:27})});afterEach(function(){Ext.undefine("spec.Human")});it("should return a unique string with a string given",function(){expect(C.getRecordKey("33")).toEqual("User-33")});it("should return a unique string with a model given",function(){expect(C.getRecordKey(D)).toEqual("User-1")})});describe("getRecordCounterKey",function(){it("should return the unique key used to store the current record counter for this proxy",function(){expect(C.getRecordCounterKey()).toEqual("User-counter")})});describe("getTreeKey",function(){it("should return the unique key used to store the tree indicator for this proxy",function(){expect(C.getTreeKey()).toEqual("User-tree")})});describe("getStorageObject",function(){it("should throw an error on getStorageObject",function(){expect(Ext.data.proxy.WebStorage.prototype.getStorageObject).toRaiseExtError()})})})});describe("instantiation with tree-indicator set in storage object",function(){var D={id:"tree-test"};beforeEach(function(){A.setItem(D.id+"-tree",true);C=new spec.Storage(D)});it("should set the isHierarchical flag",function(){expect(C.isHierarchical).toEqual(true)})});describe("destroying records after they have been added",function(){var D;beforeEach(function(){C=new spec.Storage({id:"lsTest"});D=new Ext.data.Store({model:spec.User,proxy:C});D.add({name:"Ed"},{name:"Abe"},{name:"Aaron"},{name:"Tommy"});D.sync()});it("should remove a single record",function(){var E=D.getCount();D.remove(D.getAt(1));D.sync();expect(D.getCount()).toEqual(E-1);expect(D.getAt(0).get("name")).toEqual("Ed");expect(D.getAt(1).get("name")).toEqual("Aaron")});it("should remove an array of records",function(){var E=D.getCount();D.remove([D.getAt(1),D.getAt(2)]);D.sync();expect(D.getCount()).toEqual(E-2);expect(D.getAt(0).get("name")).toEqual("Ed");expect(D.getAt(1).get("name")).toEqual("Tommy")});it("should remove the records ids from storage",function(){D.remove([D.getAt(1),D.getAt(2)]);D.sync();expect(C.getIds()).toEqual([1,4])})});describe("destroying a tree node",function(){var G,F,E,D,J,I,H;beforeEach(function(){C=new spec.Storage({id:"tree-test"});spec.User=Ext.define(null,{extend:"Ext.data.TreeModel",fields:[{name:"id",type:"int"},{name:"name",type:"string"}],proxy:C});Ext.data.NodeInterface.decorate(spec.User);G=new Ext.data.TreeStore({model:spec.User,proxy:C,root:{name:"Users",expanded:true,id:42}});F=new spec.User({name:"Abe"});E=new spec.User({name:"Sue"});D=new spec.User({name:"Phil"});J=new spec.User({name:"Don"});I=new spec.User({name:"Ed"});H=new spec.User({name:"Nico"});E.appendChild([D,J]);F.appendChild([E,I]);G.getRoot().appendChild(F);G.getRoot().appendChild(H);G.sync()});it("should recursively remove the node and all of its descendants",function(){spyOn(C,"removeRecord").andCallThrough();F.erase();expect(C.removeRecord).toHaveBeenCalledWith(F);expect(C.removeRecord).toHaveBeenCalledWith(E);expect(C.removeRecord).toHaveBeenCalledWith(D);expect(C.removeRecord).toHaveBeenCalledWith(J);expect(C.removeRecord).toHaveBeenCalledWith(I)});it("should remove the node and its descendants from the storage object",function(){F.erase();expect(C.getRecord(1)).toBeNull();expect(C.getRecord(2)).toBeNull();expect(C.getRecord(3)).toBeNull();expect(C.getRecord(4)).toBeNull();expect(C.getRecord(5)).toBeNull()});it("should remove the ids for the node and its descendants",function(){F.erase();expect(C.getIds()).toEqual([6])});it("should remove the node and its descendants from the cache",function(){F.erase();expect(C.cache[1]).toBeUndefined();expect(C.cache[2]).toBeUndefined();expect(C.cache[3]).toBeUndefined();expect(C.cache[4]).toBeUndefined();expect(C.cache[5]).toBeUndefined()})});describe("adding records to the storage object",function(){var D,E;beforeEach(function(){C=new spec.Storage({model:spec.User,id:"someId"});spyOn(C,"getNextId").andReturn(10);spyOn(C,"setIds").andCallThrough();spyOn(C,"getIds").andReturn([]);spyOn(C,"setRecord").andCallThrough()});var F=function(){E=new Ext.data.operation.Create({records:[D]});spyOn(E,"setCompleted").andCallThrough();spyOn(E,"setSuccessful").andCallThrough()};describe("if the records are phantoms",function(){beforeEach(function(){D=new spec.User({name:"Ed"});F()});it("should assign the next id to the record",function(){C.create(E);expect(D.getId()).toEqual(10)});it("should retain an id if using a UUID",function(){var G=Ext.define(null,{extend:"Ext.data.Model",fields:["id"],identifier:"uuid"});C=new spec.Storage({model:G,id:"someId"});D=new G();var H=D.getId();F();C.create(E);expect(D.getId()).toBe(H)});it("should mark the Operation as completed",function(){C.create(E);expect(E.setCompleted).toHaveBeenCalled()});it("should mark the Operation as successful",function(){C.create(E);expect(E.setSuccessful).toHaveBeenCalled()});it("should add the id to the set of all ids",function(){C.create(E);expect(C.setIds).toHaveBeenCalledWith([10])});it("should add the record to the storage object",function(){C.create(E);expect(C.setRecord).toHaveBeenCalledWith(D,10)});it("should call commit on the record",function(){spyOn(D,"commit").andCallThrough();C.create(E);expect(D.commit).toHaveBeenCalled()});it("should call the callback function with the records and operation",function(){var H,G;E.setCallback(function(I,J){G=I;H=J});C.create(E);expect(H).toEqual(E);expect(G).toEqual(E.getRecords())});it("should call the callback function with the correct scope",function(){var G;E.setCallback(function(){G=this});E.setScope(fakeScope);C.create(E);expect(G).toBe(fakeScope)})});describe("if the records are not phantoms",function(){beforeEach(function(){D=new spec.User({id:20,name:"Ed"});F()});it("should add the id to the set of all ids",function(){C.create(E);expect(C.setIds).toHaveBeenCalledWith([20])});it("should not generate the next id",function(){C.create(E);expect(C.getNextId).not.toHaveBeenCalled()});it("should add the record to the storage object",function(){C.create(E);expect(C.setRecord).toHaveBeenCalledWith(D,20)})});describe("if the records are decorated with NodeInterface",function(){beforeEach(function(){Ext.data.NodeInterface.decorate(spec.User);D=new spec.User({name:"Phil"});F()});it("should set the tree indicator in the storage object the first time a record is created",function(){C.create(E);expect(C.getStorageObject().getItem(C.getTreeKey())).toEqual("true")});it("should set the isHierarchical flag on the proxy the first time a record is created",function(){C.create(E);expect(C.isHierarchical).toEqual(true)})})});describe("updating existing records",function(){var E,D;beforeEach(function(){C=new spec.Storage({model:spec.User,id:"someId"});spyOn(C,"setRecord").andCallThrough();D=new spec.User({id:100,name:"Ed"});E=new Ext.data.operation.Update({records:[D]});spyOn(E,"setCompleted").andCallThrough();spyOn(E,"setSuccessful").andCallThrough()});it("should mark the Operation as completed",function(){C.update(E);expect(E.setCompleted).toHaveBeenCalled()});it("should mark the Operation as successful",function(){C.update(E);expect(E.setSuccessful).toHaveBeenCalled()});it("should add the record to the storage object",function(){C.update(E);expect(C.setRecord).toHaveBeenCalledWith(D)});it("should call commit on the record",function(){spyOn(D,"commit").andCallThrough();C.update(E);expect(D.commit).toHaveBeenCalled()});it("should call the callback function with the records and operation",function(){var G,F;E.setCallback(function(H,I){F=H;G=I});C.update(E);expect(G).toEqual(E);expect(F).toEqual(E.getRecords())});it("should call the callback function with the correct scope",function(){var F;E.setCallback(function(){F=this});E.setScope(fakeScope);C.update(E);expect(F).toBe(fakeScope)});describe("if the record is not already in the storage object",function(){it("should add the record's id to the set of ids",function(){spyOn(C,"setIds").andCallThrough();C.update(E);expect(C.setIds).toHaveBeenCalledWith([100])})})});describe("setRecord",function(){var D,F,E;beforeEach(function(){spyOn(A,"setItem").andReturn();spyOn(A,"removeItem").andReturn();C=new spec.Storage({model:spec.User,id:"someId"});D=new spec.User({id:100,name:"Ed"});F="someId-100";E="some encoded data";spyOn(Ext,"encode").andReturn(E);spyOn(D,"set").andCallThrough();spyOn(C,"getRecordKey").andReturn(F)});describe("if a new id is passed",function(){it("should set the id on the record",function(){C.setRecord(D,20);expect(D.set).toHaveBeenCalledWith("id",20,{commit:true})})});describe("if a new id is not passed",function(){it("should get the id from the record",function(){spyOn(D,"getId").andCallThrough();C.setRecord(D);expect(D.getId).toHaveBeenCalled()})});it("should get the record key for the model instance",function(){C.setRecord(D);expect(C.getRecordKey).toHaveBeenCalledWith(100)});it("should remove the item from the storage object before adding it again",function(){C.setRecord(D);expect(A.removeItem).toHaveBeenCalledWith(F)});it("should add the item to the storage object",function(){C.setRecord(D);expect(A.setItem).toHaveBeenCalledWith(F,E)});it("should json encode the data",function(){var G=Ext.clone(D.data);C.setRecord(D);delete G.id;expect(Ext.encode).toHaveBeenCalledWith(G)})});describe("reading",function(){var E,D;beforeEach(function(){B={id:"User",model:spec.User};C=new spec.Storage(B)});describe("via the model",function(){it("should load the data",function(){spec.User.setProxy(C);var G=new spec.User({id:1,name:"Foo"});G.save();var F=spec.User.load(1);expect(F.getId()).toBe(1);expect(F.get("name")).toBe("Foo")})});describe("if passed an id",function(){var F;beforeEach(function(){F={id:100,name:"Phil"};spyOn(C,"getRecord").andReturn(F);D=new Ext.data.operation.Read({id:100})});it("should attempt to get the record for the given id",function(){C.read(D);expect(C.getRecord).toHaveBeenCalledWith(100)});it("should mark the operation successful",function(){spyOn(D,"setSuccessful").andCallThrough();C.read(D);expect(D.setSuccessful).toHaveBeenCalled()});it("should mark the operation completed",function(){spyOn(D,"setCompleted").andCallThrough();C.read(D);expect(D.setCompleted).toHaveBeenCalled()});describe("the resultSet",function(){var G;beforeEach(function(){D.setCallback(function(H,I){G=I.getResultSet()});C.read(D)});it("should contain the loaded record",function(){expect(G.getRecords()[0].getId()).toEqual(100);expect(G.getRecords()[0].get("name")).toEqual("Phil")});it("should set the correct total number of records",function(){expect(G.getTotal()).toEqual(1)});it("should mark itself as loaded",function(){expect(G.getLoaded()).toBe(true)})});it("should call the recordCreator function to create the record",function(){var G=jasmine.createSpy();D.setRecordCreator(G);C.read(D);expect(G).toHaveBeenCalledWith({id:100,name:"Phil"},spec.User)})});describe("if not passed an id",function(){var F;beforeEach(function(){A.setItem("User","1,2,3,4");A.setItem("User-1",'{"firstName":"Bob","lastName":"Smith","age":"2"}');A.setItem("User-2",'{"firstName":"Joe","lastName":"Smith","age":"50"}');A.setItem("User-3",'{"firstName":"Tim","lastName":"Jones","age":"41"}');A.setItem("User-4",'{"firstName":"Jim","lastName":"Smith","age":"33"}');D=new Ext.data.operation.Read()});it("should mark the operation successful",function(){spyOn(D,"setSuccessful").andCallThrough();C.read(D);expect(D.setSuccessful).toHaveBeenCalled()});it("should mark the operation completed",function(){spyOn(D,"setCompleted").andCallThrough();C.read(D);expect(D.setCompleted).toHaveBeenCalled()});it("should call the recordCreator function to create the records",function(){var G=jasmine.createSpy();D.setRecordCreator(G);C.read(D);expect(G.callCount).toBe(4);expect(G.calls[0].args).toEqual([{id:1,firstName:"Bob",lastName:"Smith",age:"2"},spec.User]);expect(G.calls[1].args).toEqual([{id:2,firstName:"Joe",lastName:"Smith",age:"50"},spec.User]);expect(G.calls[2].args).toEqual([{id:3,firstName:"Tim",lastName:"Jones",age:"41"},spec.User]);expect(G.calls[3].args).toEqual([{id:4,firstName:"Jim",lastName:"Smith",age:"33"},spec.User])});describe("the resultSet",function(){var G;beforeEach(function(){D.setCallback(function(H,I){G=I.getResultSet()});C.read(D)});it("should contain the loaded records",function(){expect(G.getRecords()[0].get("firstName")).toBe("Bob");expect(G.getRecords()[1].get("firstName")).toBe("Joe");expect(G.getRecords()[2].get("firstName")).toBe("Tim");expect(G.getRecords()[3].get("firstName")).toBe("Jim")});it("should contain the correct number of loaded records",function(){expect(G.getRecords().length).toBe(4)});it("should set the correct total number of records",function(){expect(G.getTotal()).toEqual(4)});it("should mark itself as loaded",function(){expect(G.getLoaded()).toBe(true)});it("should cache the records",function(){expect(C.cache[1].firstName).toBe("Bob");expect(C.cache[2].firstName).toBe("Joe");expect(C.cache[3].firstName).toBe("Tim");expect(C.cache[4].firstName).toBe("Jim")})});it("should respect filters on the Operation",function(){var G;D=new Ext.data.operation.Read({filters:[new Ext.util.Filter({property:"lastName",value:"Smith"}),new Ext.util.Filter({filterFn:function(H){return H.get("age")<40}})],callback:function(H){G=H}});C.read(D);expect(G.length).toBe(2);expect(G[0].get("firstName")).toBe("Bob");expect(G[1].get("firstName")).toBe("Jim")});it("should respect start and limit on the Operation",function(){var G;D=new Ext.data.operation.Read({start:1,limit:2,callback:function(H){G=H}});C.read(D);expect(G.length).toBe(2);expect(G[0].get("firstName")).toBe("Joe");expect(G[1].get("firstName")).toBe("Tim")});it("should respect sorters on the Operation",function(){var G;D=new Ext.data.operation.Read({sorters:[new Ext.util.Sorter({property:"lastName",root:"data"}),new Ext.util.Sorter({sorterFn:function(I,H){return I.get("age")-H.get("age")}})],callback:function(H){G=H}});C.read(D);expect(G.length).toBe(4);expect(G[0].get("firstName")).toBe("Tim");expect(G[1].get("firstName")).toBe("Bob");expect(G[2].get("firstName")).toBe("Jim");expect(G[3].get("firstName")).toBe("Joe")});it("should apply sorters before filters",function(){var G;D=new Ext.data.operation.Read({sorters:[new Ext.util.Sorter({property:"lastName",root:"data"}),new Ext.util.Sorter({sorterFn:function(I,H){return I.get("age")-H.get("age")}})],filters:[new Ext.util.Filter({property:"lastName",value:"Smith"}),new Ext.util.Filter({filterFn:function(H){return H.get("age")<40}})],callback:function(H){G=H}});C.read(D);expect(G.length).toBe(2);expect(G[0].get("firstName")).toBe("Bob");expect(G[1].get("firstName")).toBe("Jim")});it("should apply sorters before start and limit",function(){var G;D=new Ext.data.operation.Read({sorters:[new Ext.util.Sorter({property:"lastName",root:"data"}),new Ext.util.Sorter({sorterFn:function(I,H){return I.get("age")-H.get("age")}})],start:1,limit:2,callback:function(H){G=H}});C.read(D);expect(G.length).toBe(2);expect(G[0].get("firstName")).toBe("Bob");expect(G[1].get("firstName")).toBe("Jim")})});describe("the tree indicator flag is set",function(){beforeEach(function(){C=new spec.Storage({model:spec.User,id:"tree-test"});Ext.data.NodeInterface.decorate(spec.User);C.isHierarchical=true;D=new Ext.data.operation.Read({})});it("should get tree data",function(){spyOn(C,"getTreeData").andReturn([new spec.User({id:1,name:"Phil"})]);C.read(D);expect(C.getTreeData).toHaveBeenCalled()})});describe("getting tree data from the storage object",function(){var F;beforeEach(function(){C=new spec.Storage({model:spec.User,id:"tree-test"});Ext.data.NodeInterface.decorate(spec.User);A.setItem("tree-test","1,2,3,4,5,6");A.setItem("tree-test-tree",true);A.setItem("tree-test-counter","6");A.setItem("tree-test-1",'{"name":"Phil","index":2,"leaf":true}');A.setItem("tree-test-2",'{"name":"Don","index":1,"leaf":false}');A.setItem("tree-test-3",'{"name":"Evan","parentId":2,"index":1,"leaf":true}');A.setItem("tree-test-4",'{"name":"Nige","parentId":2,"index":0,"leaf":false}');A.setItem("tree-test-5",'{"name":"Thomas","parentId":4,"index":0,"leaf":false}');A.setItem("tree-test-6",'{"name":"Brian","index":0,"leaf":false}')});it("should return an array of records",function(){F=C.getTreeData();expect(Ext.isArray(F)).toBe(true)});it("should return 3 records",function(){F=C.getTreeData();expect(F.length).toBe(3)});it("should have the correct root level nodes",function(){F=C.getTreeData();expect(F[0].get("name")).toEqual("Phil");expect(F[1].get("name")).toEqual("Don");expect(F[2].get("name")).toEqual("Brian")});it("should call getRecord with each record id",function(){spyOn(C,"getRecord").andCallThrough();C.getTreeData();expect(C.getRecord).toHaveBeenCalledWith(1);expect(C.getRecord).toHaveBeenCalledWith(2);expect(C.getRecord).toHaveBeenCalledWith(3);expect(C.getRecord).toHaveBeenCalledWith(4);expect(C.getRecord).toHaveBeenCalledWith(5);expect(C.getRecord).toHaveBeenCalledWith(6)});it("should convert the records into a heirarchical structure",function(){F=C.getTreeData();expect(F[1].data.children[0].name).toEqual("Evan");expect(F[1].data.children[1].name).toEqual("Nige");expect(F[1].data.children[1].children[0].name).toEqual("Thomas")});it("should cache the records",function(){C.getTreeData();expect(C.cache[1].name).toEqual("Phil");expect(C.cache[2].name).toEqual("Don");expect(C.cache[3].name).toEqual("Evan");expect(C.cache[4].name).toEqual("Nige");expect(C.cache[5].name).toEqual("Thomas");expect(C.cache[6].name).toEqual("Brian")});it("should set loaded to true on non-leaf nodes that have no children",function(){expect(F[2].isLoaded()).toBe(true);expect(F[1].data.children[1].children[0].loaded).toBe(true)})})});describe("clearing",function(){beforeEach(function(){C=new spec.Storage({model:spec.User,id:"clear-test"});A.setItem("clear-test","1,2,3");A.setItem("clear-test-tree",true);A.setItem("clear-test-counter","6");A.setItem("clear-test-1",'{"name":"Phil"}');A.setItem("clear-test-2",'{"name":"Thomas"}');A.setItem("clear-test-3",'{"name":"Don"}');C.clear()});it("should remove all the records",function(){expect(A.getItem("clear-test-1")).toBeNull();expect(A.getItem("clear-test-2")).toBeNull();expect(A.getItem("clear-test-3")).toBeNull()});it("should remove the record counter",function(){expect(A.getItem("clear-test-counter")).toBeNull()});it("should remove the tree flag",function(){expect(A.getItem("clear-test-tree")).toBeNull()});it("should remove the ids",function(){expect(A.getItem("clear-test")).toBeNull()});it("should clear the cache",function(){expect(C.cache).toEqual({})})})})