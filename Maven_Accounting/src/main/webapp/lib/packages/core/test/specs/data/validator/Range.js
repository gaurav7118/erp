describe("Ext.data.validator.Range",function(){var A;function C(F,E,D){A=new Ext.data.validator.Range({min:E,max:D});return A.validate(F)}function B(){return Ext.String.format.apply(Ext.String,arguments)}afterEach(function(){A=null});describe("invalid values",function(){it("should not validate undefined",function(){expect(C(undefined)).toBe(A.getEmptyMessage())});it("should not validate null",function(){expect(C(null)).toBe(A.getEmptyMessage())});it("should not validate non-numbers",function(){expect(C("foo")).toBe(A.getNanMessage())});describe("min only",function(){it("should not validate if the value is less than the minimum",function(){var D=10;expect(C(9,D)).toBe(B(A.getMinOnlyMessage(),D))})});describe("max only",function(){it("should not validate if the value is greater than the maximum",function(){var D=10;expect(C(20,undefined,D)).toBe(B(A.getMaxOnlyMessage(),D))})});describe("min & max",function(){describe("string",function(){var E=5,D=10;it("should not validate if the value is less than the minimum",function(){expect(C(1,E,D)).toBe(B(A.getBothMessage(),E,D))});it("should not validate if the value is greater than the maximum",function(){expect(C(26,E,D)).toBe(B(A.getBothMessage(),E,D))})})})});describe("valid values",function(){describe("min only",function(){it("should validate if the value is equal to the minimum",function(){expect(C(3,3)).toBe(true)});it("should validate if the value is greater than the minimum",function(){expect(C(7,3)).toBe(true)})});describe("max only",function(){it("should validate if the value is equal to the maximum",function(){expect(C(18,undefined,18)).toBe(true)});it("should validate if the value is less than the maximum",function(){expect(C(18,undefined,22)).toBe(true)})});describe("both",function(){it("should validate if the value is equal to the minimum",function(){expect(C(30,30,50)).toBe(true)});it("should validate if the value is equal to the maximum",function(){expect(C(50,30,50)).toBe(true)});it("should validate if the value is between minimum/maximum",function(){expect(C(43,30,50)).toBe(true)});it("should validate if the min === max and the value === min === max",function(){expect(C(70,70,70)).toBe(true)})})});describe("messages",function(){it("should accept a custom empty message",function(){A=new Ext.data.validator.Range({emptyMessage:"Foo"});expect(A.validate(undefined)).toBe("Foo")});it("should accept a custom min message",function(){A=new Ext.data.validator.Range({minOnlyMessage:"Foo{0}",min:1});expect(A.validate(0)).toBe("Foo1")});it("should accept a custom max message",function(){A=new Ext.data.validator.Range({maxOnlyMessage:"Foo{0}",max:3});expect(A.validate(10)).toBe("Foo3")});it("should accept a custom both message",function(){A=new Ext.data.validator.Range({bothMessage:"Foo{0}{1}",min:5,max:7});expect(A.validate(3)).toBe("Foo57")})});describe("runtime changes",function(){var D=function(F,E){A=new Ext.data.validator.Range({min:F,max:E})};describe("min value",function(){it("should be able to change the min value",function(){D(3);expect(A.validate(1)).not.toBe(true);A.setMin(1);expect(A.validate(1)).toBe(true)});it("should update the minMsg after changing the min value",function(){D(3);expect(A.validate(1)).toBe(B(A.getMinOnlyMessage(),3));A.setMin(2);expect(A.validate(1)).toBe(B(A.getMinOnlyMessage(),2))})});describe("max value",function(){it("should be able to change the max value",function(){D(undefined,3);expect(A.validate(4)).not.toBe(true);A.setMax(10);expect(A.validate(4)).toBe(true)});it("should update the maxMsg after changing the max value",function(){D(undefined,3);expect(A.validate(5)).toBe(B(A.getMaxOnlyMessage(),3));A.setMax(4);expect(A.validate(5)).toBe(B(A.getMaxOnlyMessage(),4))})});describe("both",function(){it("should be able to clear the minimum value",function(){D(3,5);expect(A.validate(2)).not.toBe(true);A.setMin(undefined);expect(A.validate(2)).toBe(true)});it("should be able to clear the maximum value",function(){D(3,5);expect(A.validate(7)).not.toBe(true);A.setMax(undefined);expect(A.validate(7)).toBe(true)});describe("messages",function(){it("should update the bothMsg when the min value changes",function(){D(3,5);expect(A.validate(7)).toBe(B(A.getBothMessage(),3,5));A.setMin(2);expect(A.validate(7)).toBe(B(A.getBothMessage(),2,5))});it("should update the bothMsg when the max value changes",function(){D(3,5);expect(A.validate(7)).toBe(B(A.getBothMessage(),3,5));A.setMax(6);expect(A.validate(7)).toBe(B(A.getBothMessage(),3,6))});it("should switch to the max msg when clearing the min value",function(){D(3,5);A.setMin(undefined);expect(A.validate(7)).toBe(B(A.getMaxOnlyMessage(),5))});it("should switch to the min msg when clearing the max value",function(){D(3,5);A.setMax(undefined);expect(A.validate(1)).toBe(B(A.getMinOnlyMessage(),3))})})})})})