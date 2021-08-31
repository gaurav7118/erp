describe("Ext.form.RadioGroup",function(){var B;function A(D,C){B=new Ext.form.RadioGroup(Ext.apply({renderTo:Ext.getBody(),items:D},C))}afterEach(function(){Ext.destroy(B);B=null});describe("setValue",function(){it("should check the matching item",function(){A([{name:"foo",inputValue:"a"},{name:"foo",inputValue:"b"},{name:"foo",inputValue:"c"}]);B.setValue({foo:"b"});expect(B.getValue()).toEqual({foo:"b"})});describe("with a view model",function(){it("should be able to set the value with inline data",function(){var C=new Ext.app.ViewModel({data:{theValue:{foo:"b"}}});A([{name:"foo",inputValue:"a"},{name:"foo",inputValue:"b"},{name:"foo",inputValue:"c"}],{viewModel:C,bind:{value:"{theValue}"}});C.notify();expect(B.getValue()).toEqual({foo:"b"})});it("should be able to set the value with a defined viewmodel",function(){Ext.define("spec.Bar",{extend:"Ext.app.ViewModel",alias:"viewmodel.bar",data:{theValue:{foo:"b"}}});A([{name:"foo",inputValue:"a"},{name:"foo",inputValue:"b"},{name:"foo",inputValue:"c"}],{viewModel:{type:"bar"},bind:{value:"{theValue}"}});B.getViewModel().notify();expect(B.getValue()).toEqual({foo:"b"});Ext.undefine("spec.Bar");Ext.Factory.viewModel.instance.clearCache()})})});describe("ARIA",function(){function C(D,E){jasmine.expectAriaAttr(B,D,E)}beforeEach(function(){A([{name:"foo"},{name:"bar"},{name:"baz"}])});describe("ariaEl",function(){it("should have containerEl as ariaEl",function(){expect(B.ariaEl).toBe(B.containerEl)})});describe("attributes",function(){it("should have radiogroup role",function(){C("role","radiogroup")});it("should have aria-invalid",function(){C("aria-invalid","false")});it("should have aria-owns",function(){var F=B.down("[name=foo]").inputEl,D=B.down("[name=bar]").inputEl,E=B.down("[name=baz]").inputEl;C("aria-owns",[F.id,D.id,E.id].join(" "))});describe("aria-required",function(){it("should be false when allowBlank",function(){C("aria-required","false")});it("should be true when !allowBlank",function(){var D=new Ext.form.RadioGroup({renderTo:Ext.getBody(),allowBlank:false,items:[{name:"foo"},{name:"bar"}]});jasmine.expectAriaAttr(D,"aria-required","true");Ext.destroy(D);D=null})})});describe("state",function(){describe("aria-invalid",function(){beforeEach(function(){B.markInvalid(["foo"])});it("should set aria-invalid to tru in markInvalid",function(){C("aria-invalid","true")});it("should set aria-invalid to false in clearInvalid",function(){B.clearInvalid();C("aria-invalid","false")})})})})})