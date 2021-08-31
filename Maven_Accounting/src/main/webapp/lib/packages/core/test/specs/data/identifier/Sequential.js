describe("Ext.data.identifier.Sequential",function(){var B;function A(C){B=new Ext.data.identifier.Sequential(C)}afterEach(function(){B=null});describe("defaults",function(){beforeEach(function(){A()});it("should default prefix to null",function(){expect(B.getPrefix()).toBeNull()});it("should default seed to 1",function(){expect(B.getSeed()).toBe(1)})});describe("generating",function(){it("should generate in sequence",function(){A();expect(B.generate()).toBe(1);expect(B.generate()).toBe(2);expect(B.generate()).toBe(3);expect(B.generate()).toBe(4)});it("should generate with a prefix",function(){A({prefix:"foo"});expect(B.generate()).toBe("foo1");expect(B.generate()).toBe("foo2");expect(B.generate()).toBe("foo3");expect(B.generate()).toBe("foo4")});it("should generate with a custom seed",function(){A({seed:103});expect(B.generate()).toBe(103);expect(B.generate()).toBe(104);expect(B.generate()).toBe(105);expect(B.generate()).toBe(106)});it("should generate with a custom prefix & seed",function(){A({prefix:"foo",seed:103});expect(B.generate()).toBe("foo103");expect(B.generate()).toBe("foo104");expect(B.generate()).toBe("foo105");expect(B.generate()).toBe("foo106")})})})