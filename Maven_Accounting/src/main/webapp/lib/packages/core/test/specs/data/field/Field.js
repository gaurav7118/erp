describe("Ext.data.field.Field",function(){var A=Ext.data.SortTypes,C;function B(D){C=new Ext.data.field.Field(D)}afterEach(function(){C=null});describe("defaults",function(){beforeEach(function(){B()});it("should configure the type",function(){expect(C.getType()).toBe("auto")});it("should have allowBlank: true",function(){expect(C.getAllowBlank()).toBe(true)});it("should have allowNull: false",function(){expect(C.getAllowNull()).toBe(false)});it("should have convert: null",function(){expect(C.getConvert()).toBeNull()});it("should have defaultValue: undefined",function(){expect(C.getDefaultValue()).toBeUndefined()});it("should have depends: null",function(){expect(C.getDepends()).toBeNull()});it("should have mapping: null",function(){expect(C.getMapping()).toBeNull()});it("should have name: null",function(){expect(C.getName()).toBeNull()});it("should have persist: true",function(){expect(C.getPersist()).toBe(true)});it("should have sortType: none",function(){expect(C.getSortType()).toBe(A.none)})});describe("configuring",function(){it("should accept a string name",function(){B("foo");expect(C.getName()).toBe("foo")});it("should configure the name",function(){B({name:"foo"});expect(C.getName()).toBe("foo")});it("should configure allowBlank",function(){B({allowBlank:false});expect(C.getAllowBlank()).toBe(false)});describe("allowNull",function(){it("should configure a value",function(){B({allowNull:true});expect(C.getAllowNull()).toBe(true)});it("should default to true for fields with a reference (FK)",function(){B({reference:{}});expect(C.getAllowNull()).toBe(true)})});describe("convert",function(){it("should configure a fn",function(){var D=function(){};B({convert:D});expect(C.getConvert()).toBe(D)});describe("calculated",function(){it("should have calculated false if the convert function has < 2 args",function(){B({convert:function(D){}});expect(C.calculated).toBe(false)});it("should have calculated true if the convert function has >= 2 args",function(){B({convert:function(E,D){}});expect(C.calculated).toBe(true)})})});describe("defaultValue",function(){it("should configure a number",function(){B({defaultValue:3});expect(C.getDefaultValue()).toBe(3)});it("should configure a string",function(){B({defaultValue:"foo"});expect(C.getDefaultValue()).toBe("foo")});it("should configure a bool",function(){B({defaultValue:true});expect(C.getDefaultValue()).toBe(true)});it("should not pass the value through the converter",function(){var D=jasmine.createSpy().andReturn(8);B({defaultValue:7,convert:D});expect(C.getDefaultValue()).toBe(7);expect(D).not.toHaveBeenCalled()})});describe("depends",function(){it("should accept a single string",function(){B({depends:"foo"});expect(C.getDepends()).toEqual(["foo"])});it("should accept an array",function(){B({depends:["foo","bar","baz"]});expect(C.getDepends()).toEqual(["foo","bar","baz"])});describe("auto detection",function(){it("should detect dot property names",function(){B({calculate:function(D){return D.foo+D.bar}});expect(C.getDepends()).toEqual(["foo","bar"])});it("should not repeat",function(){B({calculate:function(D){return D.foo+D.foo+D.foo}});expect(C.getDepends()).toEqual(["foo"])});it("should match any argument name",function(){B({calculate:function(D){return D.foo+D.bar}});expect(C.getDepends()).toEqual(["foo","bar"])});it("should ignore properties that are from other objects",function(){var D={foo2:1};B({calculate:function(E){return E.foo1+D.foo2+E.foo3}});expect(C.getDepends()).toEqual(["foo1","foo3"])});it("should match fields with numbers",function(){B({calculate:function(D){return D.foo1+D.foo2}});expect(C.getDepends()).toEqual(["foo1","foo2"])});it("should not auto detect when explicitly specified",function(){B({depends:"foo3",calculate:function(D){return D.foo1+D.foo2}});expect(C.getDepends()).toEqual(["foo3"])})})});it("should configure the mapping",function(){B({mapping:"some.obj.key"});expect(C.getMapping()).toBe("some.obj.key")});describe("persist",function(){it("should configure a true value",function(){B({persist:true});expect(C.getPersist()).toBe(true)});it("should configure a false value",function(){B({persist:false});expect(C.getPersist()).toBe(false)});describe("with a convert method",function(){describe("single arg",function(){function D(E){}it("should default to true",function(){B({convert:D});expect(C.getPersist()).toBe(true)});it("should configure a true value",function(){B({persist:true,convert:D});expect(C.getPersist()).toBe(true)});it("should configure a false value",function(){B({persist:false,convert:D});expect(C.getPersist()).toBe(false)})});describe("multi arg",function(){function D(E,F){}it("should default to true",function(){B({convert:D});expect(C.getPersist()).toBe(true)});it("should configure a true value",function(){B({persist:true,convert:D});expect(C.getPersist()).toBe(true)});it("should configure a false value",function(){B({persist:false,convert:D});expect(C.getPersist()).toBe(false)})})});describe("with a calculate method",function(){function D(){}it("should default to false",function(){B({calculate:D});expect(C.getPersist()).toBe(false)});it("should configure a true value",function(){B({persist:true,calculate:D});expect(C.getPersist()).toBe(true)});it("should configure a false value",function(){B({persist:false,calculate:D});expect(C.getPersist()).toBe(false)})})});describe("sortType",function(){it("should accept a string from Ext.data.SortTypes",function(){B({sortType:"asDate"});expect(C.getSortType()).toBe(A.asDate)});it("should accept a custom sorter fn",function(){var D=function(){};B({sortType:D});expect(C.getSortType()).toBe(D)})})});describe("collate",function(){var D=function(E){return E*-1};beforeEach(function(){B({sortType:D})});it("should call the sortType and return -1 if a < b",function(){expect(C.collate(2,1)).toBe(-1)});it("should call the sortType and return 0 if a === b",function(){expect(C.collate(1,1)).toBe(0)});it("should call the sortType and return 1 if a > b",function(){expect(C.collate(1,2)).toBe(1)})});describe("compare",function(){beforeEach(function(){B()});describe("numbers",function(){it("should return -1 if a < b",function(){expect(C.compare(0,1)).toBe(-1)});it("should return 0 if a === b",function(){expect(C.compare(1,1)).toBe(0)});it("should return 1 if a > b",function(){expect(C.compare(2,1)).toBe(1)})});describe("strings",function(){it("should return -1 if a < b",function(){expect(C.compare("a","b")).toBe(-1)});it("should return 0 if a === b",function(){expect(C.compare("b","b")).toBe(0)});it("should return 1 if a > b",function(){expect(C.compare("c","b")).toBe(1)})});describe("dates",function(){var F=new Date(1970,0,1),E=new Date(1970,1,1),D=new Date(1970,2,1);it("should return -1 if a < b",function(){expect(C.compare(F,E)).toBe(-1)});it("should return 0 if a === b",function(){expect(C.compare(E,E)).toBe(0)});it("should return 1 if a > b",function(){expect(C.compare(D,E)).toBe(1)})})});describe("isEqual",function(){beforeEach(function(){B()});describe("numbers",function(){it("should return true if equal",function(){expect(C.isEqual(1,1)).toBe(true)});it("should return false if unequal",function(){expect(C.isEqual(1,3)).toBe(false)})});describe("strings",function(){it("should return true if equal",function(){expect(C.isEqual("foo","foo")).toBe(true)});it("should return false if unequal",function(){expect(C.isEqual("foo","fo")).toBe(false)})});describe("bools",function(){it("should return true if equal",function(){expect(C.isEqual(true,true)).toBe(true)});it("should return false if unequal",function(){expect(C.isEqual(false,true)).toBe(false)})});describe("object",function(){it("should return true if they are equal references",function(){var D={};expect(C.isEqual(D,D)).toBe(true)});it("should return false if they are not equal references",function(){var E={},D={};expect(C.isEqual(E,D)).toBe(false)})});describe("array",function(){it("should return true if they are equal references",function(){var D=[1,2];expect(C.isEqual(D,D)).toBe(true)});it("should return false if they are not equal references",function(){var E=[1,2],D=[1,2];expect(C.isEqual(E,D)).toBe(false)})});describe("dates",function(){it("should return true if they are equal references",function(){var D=new Date();expect(C.isEqual(D,D)).toBe(true)});it("should return false if they are not equal references",function(){var E=new Date(1970,0,1),D=new Date(1970,0,1);expect(C.isEqual(E,D)).toBe(false)})})});describe("factory",function(){var D=function(E){C=Ext.data.field.Field.create({type:E})};describe("boolean",function(){it("should use the bool alias",function(){D("bool");expect(C.isBooleanField).toBe(true)});it("should use the boolean alias",function(){D("boolean");expect(C.isBooleanField).toBe(true)})});it("should create a date field",function(){D("date");expect(C.isDateField).toBe(true)});describe("integer",function(){it("should use the int alias",function(){D("int");expect(C.isIntegerField).toBe(true)});it("should use the integer alias",function(){D("integer");expect(C.isIntegerField).toBe(true)})});describe("number",function(){it("should use the number alias",function(){D("number");expect(C.isNumberField).toBe(true)});it("should use the float alias",function(){D("float");expect(C.isNumberField).toBe(true)})});it("should create a string field",function(){D("string");expect(C.isStringField).toBe(true)});describe("base",function(){it("should create a base field with auto",function(){D("auto");expect(C.isField).toBe(true)});it("should create a base field no type",function(){D();expect(C.isField).toBe(true)})})});describe("subclassing with validators",function(){var H="Must be present",F="Is in the wrong format",G="Is not a valid email address",E;function D(J){E=Ext.define(null,{extend:"Ext.data.field.Field",validators:J})}function I(L,J,N,M){var K=new L(J),O=K.validate(N,"|");if(O===true){O=[]}else{O=O.split("|")}expect(O).toEqual(M)}afterEach(function(){E=null});it("should accept a string",function(){D("presence");I(E,null,null,[H])});it("should accept an object",function(){D({type:"format",matcher:/foo/});I(E,null,null,[F])});it("should accept a function",function(){D(function(){return"Fail"});I(E,null,null,["Fail"])});it("should accept an array of mixed string/object/function",function(){D(["presence",{type:"format",matcher:/foo/},function(){return"Fail"}]);I(E,null,null,[H,F,"Fail"])});it("should combine instance validators with class validators",function(){D("presence");I(E,{validators:"email"},null,[H,G])});describe("extending a custom field",function(){var K;function J(L){K=Ext.define(null,{extend:E,validators:L})}afterEach(function(){K=null});describe("merging",function(){it("should merge a string and a string",function(){D("presence");J("email");I(K,null,null,[H,G])});it("should merge a string and an object",function(){D("presence");J({type:"format",matcher:/foo/});I(K,null,null,[H,F])});it("should merge a string and a function",function(){D("presence");J(function(){return"Fail"});I(K,null,null,[H,"Fail"])});it("should merge a string and an array",function(){D("presence");J(["email",{type:"format",matcher:/foo/},function(){return"Fail"}]);I(K,null,null,[H,G,F,"Fail"])});it("should merge an object and a string",function(){D({type:"format",matcher:/foo/});J("presence");I(K,null,null,[F,H])});it("should merge a function and a string",function(){D(function(){return"Fail"});J("presence");I(K,null,null,["Fail",H])});it("should merge an array and a string",function(){D(["email",{type:"format",matcher:/foo/},function(){return"Fail"}]);J("presence");I(K,null,null,[G,F,"Fail",H])});it("should merge 2 arrays",function(){D(["presence"]);J(["email"]);I(K,null,null,[H,G])});it("should not modify the superclass validators",function(){D("presence");J("email");I(E,null,null,[H]);I(K,null,null,[H,G])})})})})})