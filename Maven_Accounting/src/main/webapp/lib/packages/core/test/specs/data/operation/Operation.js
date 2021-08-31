describe("Ext.data.operation.Operation",function(){var B;function A(C){B=new Ext.data.operation.Operation(C)}afterEach(function(){B=null});describe("state",function(){describe("initial state",function(){beforeEach(function(){A()});it("should not be started",function(){expect(B.isStarted()).toBe(false)});it("should not be running",function(){expect(B.isRunning()).toBe(false)});it("should not be complete",function(){expect(B.isComplete()).toBe(false)});it("should not be successful",function(){expect(B.wasSuccessful()).toBe(false)});it("should have no error",function(){expect(B.getError()).toBeUndefined()});it("should have no exception",function(){expect(B.hasException()).toBe(false)})});describe("starting",function(){beforeEach(function(){A();B.execute()});it("should be started",function(){expect(B.isStarted()).toBe(true)});it("should be running",function(){expect(B.isRunning()).toBe(true)});it("should not be complete",function(){expect(B.isComplete()).toBe(false)});it("should not be successful",function(){expect(B.wasSuccessful()).toBe(false)});it("should have no error",function(){expect(B.getError()).toBeUndefined()});it("should have no exception",function(){expect(B.hasException()).toBe(false)})});describe("completing successfully",function(){beforeEach(function(){A();B.execute();B.setSuccessful(true)});it("should be started",function(){expect(B.isStarted()).toBe(true)});it("should not be running",function(){expect(B.isRunning()).toBe(false)});it("should be complete",function(){expect(B.isComplete()).toBe(true)});it("should be successful",function(){expect(B.wasSuccessful()).toBe(true)});it("should have no error",function(){expect(B.getError()).toBeUndefined()});it("should have no exception",function(){expect(B.hasException()).toBe(false)})});describe("completing with failure",function(){beforeEach(function(){A();B.execute();B.setException("Failed")});it("should be started",function(){expect(B.isStarted()).toBe(true)});it("should not be running",function(){expect(B.isRunning()).toBe(false)});it("should be complete",function(){expect(B.isComplete()).toBe(true)});it("should not be successful",function(){expect(B.wasSuccessful()).toBe(false)});it("should have the passed error",function(){expect(B.getError()).toBe("Failed")});it("should have an exception",function(){expect(B.hasException()).toBe(true)})});describe("completeOperation",function(){var C;beforeEach(function(){C={completeOperation:jasmine.createSpy("completeOperation")}});afterEach(function(){C=null});it("should be called when completed successfully",function(){A({proxy:C});B.execute();B.setSuccessful(true);expect(C.completeOperation).toHaveBeenCalledWith(B)});it("should be called when failed",function(){A({proxy:C});B.execute();B.setException("Fail!");expect(C.completeOperation).toHaveBeenCalledWith(B)})})});describe("aborting",function(){var C,D;beforeEach(function(){C=new Ext.data.proxy.Proxy();A({proxy:C});B.doExecute=function(){D=new Ext.data.Request();return D};spyOn(C,"abort");spyOn(C,"completeOperation")});afterEach(function(){C=D=null});it("should not call if the operation has not started",function(){B.abort();expect(C.abort).not.toHaveBeenCalled()});it("should not call if the operation has been completed",function(){B.execute();B.setSuccessful(true);B.abort();expect(C.abort).not.toHaveBeenCalled()});it("should pass the request for this operation to abort",function(){B.execute();B.abort();expect(C.abort).toHaveBeenCalledWith(D)});it("should not call completeOperation",function(){B.execute();B.abort();expect(C.completeOperation).not.toHaveBeenCalled()})});describe("callbacks",function(){it("should trigger when setting completed",function(){var C=false;A({callback:function(){C=true}});B.execute();B.setSuccessful(true);expect(C).toBe(true)});it("should trigger when setting an exception",function(){var C=false;A({callback:function(){C=true}});B.execute();B.setException("Failed");expect(C).toBe(true)});it("should default the scope to the operation",function(){var C;A({callback:function(){C=this}});B.execute();B.setSuccessful(true);expect(C).toBe(B)});it("should use a passed scope",function(){var D={},C;A({scope:D,callback:function(){C=this}});B.execute();B.setSuccessful(true);expect(C).toBe(D)});it("should pass the records, operation and success state",function(){var C=jasmine.createSpy();A({callback:C});B.execute();B.setSuccessful(true);expect(C).toHaveBeenCalledWith(B.getRecords(),B,true)})});describe("process",function(){var F=Ext.data.reader.Reader.prototype.nullResultSet,C,E,D,G;beforeEach(function(){G=Ext.define(null,{extend:"Ext.data.Model",fields:["id"]});C={};E=new Ext.data.Request();A();B.setRecords([new G()])});afterEach(function(){G=C=E=D=null});it("should set the resultSet",function(){B.process(F,E,C);expect(B.getResultSet()).toBe(F)});it("should set the response",function(){B.process(F,E,C);expect(B.getResponse()).toBe(C)});describe("result set with success: false",function(){it("should set an exception ",function(){B.process(new Ext.data.ResultSet({success:false}),E,C);expect(B.hasException()).toBe(true);expect(B.wasSuccessful()).toBe(false)});it("should set the error to the message returned by the result set",function(){B.process(new Ext.data.ResultSet({success:false,message:"Failed"}),E,C);expect(B.getError()).toBe("Failed")})});describe("result set with success: true",function(){it("should set success if the result set is successful",function(){B.process(F,E,C);expect(B.wasSuccessful()).toBe(true)});it("should call doProcess",function(){spyOn(B,"doProcess");B.process(F,E,C);expect(B.doProcess).toHaveBeenCalledWith(F,E,C)})})});describe("retrying an operation",function(){beforeEach(function(){A();B.doExecute=function(){return new Ext.data.Request()};B.setException("Err");B.execute()});it("should clear any error",function(){expect(B.getError()).toBeUndefined()});it("should clear the success flag",function(){expect(B.wasSuccessful()).toBe(false)});it("should clear the complete flag",function(){expect(B.isComplete()).toBe(false)});it("should clear the exception flag",function(){expect(B.hasException()).toBe(false)})})})