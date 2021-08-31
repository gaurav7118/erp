describe("Ext.data.field.Boolean",function(){var B;function A(D){B=new Ext.data.field.Boolean(D)}function C(D){return B.convert(D)}afterEach(function(){B=null});describe("defaults",function(){it("should configure the type",function(){A();expect(B.getType()).toBe("bool")})});describe("convert",function(){it("should return true if passed true",function(){A();expect(C(true)).toBe(true)});it("should return false if passed false",function(){A();expect(C(false)).toBe(false)});describe("truth matching",function(){beforeEach(function(){A()});it("should return true if passed 'true'",function(){expect(C("true")).toBe(true)});it("should return true if passed 'TRUE'",function(){expect(C("TRUE")).toBe(true)});it("should return true if passed 'yes'",function(){expect(C("yes")).toBe(true)});it("should return true if passed 'YES'",function(){expect(C("YES")).toBe(true)});it("should return true if passed 'on'",function(){expect(C("on")).toBe(true)});it("should return true if passed 'ON'",function(){expect(C("ON")).toBe(true)});it("should return true if passed 1",function(){expect(C(1)).toBe(true)});it("should return true if passed '1'",function(){expect(C("1")).toBe(true)});it("should return false for undefined",function(){expect(C(undefined)).toBe(false)});it("should return false for null",function(){expect(C(null)).toBe(false)});it("should return false for ''",function(){expect(C("")).toBe(false)});it("should return false for 0",function(){expect(C(0)).toBe(false)});it("should return false for '0'",function(){expect(C("0")).toBe(false)});it("should return false for other truthy values",function(){expect(C("foo")).toBe(false);expect(C(100)).toBe(false);expect(C([1,2,3])).toBe(false);expect(C(new Date())).toBe(false);expect(C({foo:true})).toBe(false)})});describe("allowNull",function(){beforeEach(function(){A({allowNull:true})});it("should return null if passed undefined",function(){expect(C(undefined)).toBe(null)});it("should return null if passed null",function(){expect(C(null)).toBe(null)});it("should return null if passed ''",function(){expect(C("")).toBe(null)})})})})