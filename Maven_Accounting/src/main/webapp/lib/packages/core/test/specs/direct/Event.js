describe("Ext.direct.Event",function(){var A;describe("Event",function(){beforeEach(function(){A=new Ext.direct.Event({name:"foo",data:{foo:"bar"}})});it("should instantiate",function(){expect(A).toBeDefined()});it("should have true status",function(){expect(A.status).toBe(true)});it("should return name with getName()",function(){var B=A.getName();expect(B).toBe("foo")});it("should return data with getData()",function(){var B=A.getData();expect(B).toEqual({foo:"bar"})})});describe("ExceptionEvent",function(){beforeEach(function(){A=new Ext.direct.ExceptionEvent({name:"bar",data:{bar:"baz"}})});it("should instantiate",function(){expect(A).toBeDefined()});it("should have false status",function(){expect(A.status).toBe(false)})});describe("RemotingEvent",function(){var B;beforeEach(function(){B=new Ext.direct.Transaction({provider:{}});Ext.direct.Manager.addTransaction(B)});afterEach(function(){Ext.direct.Manager.removeTransaction(B)});it("returns transaction directly",function(){A=new Ext.direct.RemotingEvent({name:"baz",data:{baz:"qux"},transaction:B});var C=A.getTransaction();expect(C).toEqual(B)});it("returns transaction by tid",function(){A=new Ext.direct.RemotingEvent({name:"baz",data:{baz:"qux"},tid:B.tid});var C=A.getTransaction();expect(C).toEqual(B)})})})