describe("Ext.data.validator.Validator",function(){var A;afterEach(function(){A=null});describe("construction",function(){it("should accept a function to be the validate method",function(){var B=function(){};A=new Ext.data.validator.Validator(B);expect(A.validate).toBe(B)})});describe("validate",function(){it("should return true",function(){A=new Ext.data.validator.Validator();expect(A.validate()).toBe(true)})});describe("factory",function(){var B=function(D,C){return Ext.data.validator.Validator.create(Ext.apply({type:D},C))};it("should create a presence validator",function(){expect(B("presence") instanceof Ext.data.validator.Presence).toBe(true)});it("should create a length validator",function(){expect(B("length") instanceof Ext.data.validator.Length).toBe(true)});it("should create a range validator",function(){expect(B("range") instanceof Ext.data.validator.Range).toBe(true)});it("should create an email validator",function(){expect(B("email") instanceof Ext.data.validator.Email).toBe(true)});it("should create a format validator",function(){expect(B("format",{matcher:/foo/}) instanceof Ext.data.validator.Format).toBe(true)});it("should create an inclusion validator",function(){expect(B("inclusion",{list:[]}) instanceof Ext.data.validator.Inclusion).toBe(true)});it("should create an exclusion validator",function(){expect(B("exclusion",{list:[]}) instanceof Ext.data.validator.Exclusion).toBe(true)});it("should default to base",function(){expect(B("") instanceof Ext.data.validator.Validator).toBe(true)})});describe("custom validator",function(){var B;beforeEach(function(){Ext.define("Ext.data.validator.Custom",{extend:"Ext.data.validator.Validator",alias:"data.validator.custom"});B=Ext.data.validator.Validator.create({type:"custom"})});afterEach(function(){B.destroy();Ext.undefine("Ext.data.validator.Custom");Ext.Factory.dataValidator.instance.clearCache()});it("should be able to create a custom validator",function(){expect(B instanceof Ext.data.validator.Custom).toBe(true);expect(B instanceof Ext.data.validator.Validator).toBe(true)});it("should pass value and record to Validator validate method",function(){spyOn(B,"validate").andCallThrough();var D=Ext.define(null,{extend:"Ext.data.Model",fields:["test"],validators:{test:B}}),C=new D({test:"Foo"});C.isValid();expect(B.validate).toHaveBeenCalled();expect(B.validate).toHaveBeenCalledWith("Foo",C)})})})