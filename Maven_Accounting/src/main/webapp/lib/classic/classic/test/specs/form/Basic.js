describe("Ext.form.Basic",function(){var B,A,C,D;function E(F,G){var H;if(!F.isComponent){Ext.apply(F,{isEqual:Ext.form.field.Base.prototype.isEqualAsString});H=new Ext.form.field.Base(F)}else{H=F}G=G||A;return G.add(H)}beforeEach(function(){Ext.define("MockAction",{alias:"formaction.mock",constructor:function(){C=this;if(D){D.apply(this,arguments)}},run:Ext.emptyFn});A=new Ext.container.Container({});B=new Ext.form.Basic(A);B.initialize()});afterEach(function(){Ext.undefine("MockAction");A.destroy();B.destroy();B=A=C=null});describe("paramOrder normalization",function(){var F=["one","two","three"];it("should accept paramOrder config as an array",function(){var G=new Ext.form.Basic(A,{paramOrder:F});expect(G.paramOrder).toEqual(F)});it("should accept paramOrder config as a comma-separated string and normalize it to an array",function(){var G=new Ext.form.Basic(A,{paramOrder:"one,two,three"});expect(G.paramOrder).toEqual(F)});it("should accept paramOrder config as a space-separated string and normalize it to an array",function(){var G=new Ext.form.Basic(A,{paramOrder:"one two three"});expect(G.paramOrder).toEqual(F)});it("should accept paramOrder config as a pipe-separated string and normalize it to an array",function(){var G=new Ext.form.Basic(A,{paramOrder:"one|two|three"});expect(G.paramOrder).toEqual(F)})});describe("getFields",function(){beforeEach(function(){E({name:"one"});E({name:"two"})});it("should return all field objects within the owner",function(){var F=B.getFields();expect(F.length).toEqual(2);expect(F.getAt(0).name).toEqual("one");expect(F.getAt(1).name).toEqual("two")});it("should cache the list of fields after first access",function(){var G=B.getFields(),F=B.getFields();expect(F).toBe(G)});it("should requery the list when a field is added",function(){var G=B.getFields();E({name:"three"});var F=B.getFields();expect(F.getCount()).toEqual(3);expect(F.getAt(2).name).toEqual("three")});it("should requery the list when a field is removed",function(){var G=B.getFields();A.remove(A.items.getAt(0));var F=B.getFields();expect(F.getCount()).toEqual(1);expect(F.getAt(0).name).toEqual("two")});it("should requery the list when an field is added in a container",function(){A.add(new Ext.form.field.Base());expect(B.getFields().getCount()).toBe(3)});it("should requery the list when an field is removed from a container",function(){A.add(new Ext.form.field.Base());A.removeAll();expect(B.getFields().getCount()).toBe(0)})});describe("isValid method",function(){it("should return true if no fields are invalid",function(){E({name:"one"});E({name:"two"});expect(B.isValid()).toBeTruthy()});it("should return false if any fields are invalid",function(){E({name:"one"});E({name:"two",isValid:function(){return false}});expect(B.isValid()).toBeFalsy()})});describe("isDirty method",function(){it("should return false if no fields are dirty",function(){var G=E({name:"one"}),F=E({name:"two"});expect(B.isDirty()).toBeFalsy()});it("should return true if any fields are dirty",function(){E({name:"one"});var F=E({name:"two",value:"aaa"});F.setValue("bbb");expect(B.isDirty()).toBeTruthy()})});describe("reset method",function(){it("should reset all fields to their initial values",function(){var G=E({name:"one"}),F=E({name:"one"});spyOn(G,"reset");spyOn(F,"reset");B.reset();expect(G.reset).toHaveBeenCalled();expect(F.reset).toHaveBeenCalled()});it("should not clear any record reference by default",function(){var F={getData:function(){return{one:"value 1",two:"value 2"}}};B.loadRecord(F);B.reset();expect(B.getRecord()).toBe(F)});it("should clear any record reference if resetRecord is passed",function(){var F={getData:function(){return{one:"value 1",two:"value 2"}}};B.loadRecord(F);B.reset(true);expect(B.getRecord()).toBeUndefined()})});describe("findField method",function(){it("should find a field by id",function(){var G=E({name:"one",id:"oneId"}),F=B.findField("oneId");expect(F).toBe(G)});it("should find a field by name",function(){var G=E({name:"one"}),F=B.findField("one");expect(F).toBe(G)});it("should return null if no matching field is found",function(){var G=E({name:"one"}),F=B.findField("doesnotmatch");expect(F).toBeNull()});it("should exclude items with the excludeForm property on the field",function(){E({name:"foo",excludeForm:true});expect(B.findField("foo")).toBeNull()})});describe("markInvalid method",function(){it("should accept an object where the keys are field names and the values are error messages",function(){var G=E({name:"one"}),F=E({name:"two"});spyOn(G,"markInvalid");spyOn(F,"markInvalid");B.markInvalid({one:"error one",two:"error two"});expect(G.markInvalid).toHaveBeenCalledWith("error one");expect(F.markInvalid).toHaveBeenCalledWith("error two")});it("should accept an array of objects with 'id' and 'msg' properties",function(){var G=E({name:"one"}),F=E({name:"two"});spyOn(G,"markInvalid");spyOn(F,"markInvalid");B.markInvalid([{id:"one",msg:"error one"},{id:"two",msg:"error two"}]);expect(G.markInvalid).toHaveBeenCalledWith("error one");expect(F.markInvalid).toHaveBeenCalledWith("error two")})});describe("clearInvalid method",function(){it("should clear the invalid state of all fields",function(){var G=E({name:"one"}),F=E({name:"two"});spyOn(G,"clearInvalid");spyOn(F,"clearInvalid");B.clearInvalid();expect(G.clearInvalid).toHaveBeenCalled();expect(F.clearInvalid).toHaveBeenCalled()})});describe("applyToFields method",function(){it("should call apply() on all fields with the given arguments",function(){var G=E({name:"one"}),F=E({name:"two"});B.applyToFields({customProp:"custom"});expect(G.customProp).toEqual("custom");expect(F.customProp).toEqual("custom")})});describe("applyIfToFields method",function(){it("should call applyIf() on all fields with the given arguments",function(){var G=E({name:"one",customProp1:1}),F=E({name:"two",customProp1:1});B.applyIfToFields({customProp1:2,customProp2:3});expect(G.customProp1).toEqual(1);expect(G.customProp2).toEqual(3);expect(F.customProp1).toEqual(1);expect(F.customProp2).toEqual(3)})});describe("setValues method",function(){it("should accept an object mapping field ids to new field values",function(){var G=E({name:"one"}),F=E({name:"two"});spyOn(G,"setValue");spyOn(F,"setValue");B.setValues({one:"value 1",two:"value 2"});expect(G.setValue).toHaveBeenCalledWith("value 1");expect(F.setValue).toHaveBeenCalledWith("value 2")});it("should accept an array of objects with 'id' and 'value' properties",function(){var G=E({name:"one"}),F=E({name:"two"});spyOn(G,"setValue");spyOn(F,"setValue");B.setValues([{id:"one",value:"value 1"},{id:"two",value:"value 2"}]);expect(G.setValue).toHaveBeenCalledWith("value 1");expect(F.setValue).toHaveBeenCalledWith("value 2")});it("should not set the fields' originalValue property by default",function(){var F=E({name:"one",value:"orig value"});B.setValues({one:"new value"});expect(F.originalValue).toEqual("orig value")});it("should set the fields' originalValue property if the 'trackResetOnLoad' config is true",function(){var F=E({name:"one",value:"orig value"});B.trackResetOnLoad=true;B.setValues({one:"new value"});expect(F.originalValue).toEqual("new value")});it("should only trigger a single layout",function(){var F=[],J={},H=0,I=0,G;for(;H<5;++H){G="field"+H;E({name:G,setRawValue:function(L){var K=this;L=Ext.valueFrom(K.transformRawValue(L),"");K.rawValue=L;if(K.inputEl){K.inputEl.dom.value=L}K.updateLayout();return L}});J[G]=G}A.render(Ext.getBody());A.on("afterlayout",function(){++I});B.setValues(J);expect(I).toBe(1)})});describe("getValues method",function(){var F;afterEach(function(){F=null});it("should return an object mapping field names to field values",function(){E({name:"one",value:"value 1"});E({name:"two",value:"value 2"});F=B.getValues();expect(F).toEqual({one:"value 1",two:"value 2"})});it("should populate an array of values for multiple fields with the same name",function(){E({name:"one",value:"value 1"});E({name:"two",value:"value 2"});E({name:"two",value:"value 3"});F=B.getValues();expect(F).toEqual({one:"value 1",two:["value 2","value 3"]})});it("should populate an array of values for single fields who return an array of values",function(){E({name:"one",value:"value 1"});E({name:"two",value:"value 2"});E({name:"two",getRawValue:function(){return["value 3","value 4"]}});F=B.getValues();expect(F).toEqual({one:"value 1",two:["value 2","value 3","value 4"]})});it("should return a url-encoded query parameter string if the 'asString' argument is true",function(){E({name:"one",value:"value 1"});E({name:"two",value:"value 2"});E({name:"two",value:"value 3"});F=B.getValues(true);expect(F).toEqual("one=value%201&two=value%202&two=value%203")});it("should return only dirty fields if the 'dirtyOnly' argument is true",function(){E({name:"one",value:"value 1"}).setValue("dirty value");E({name:"two",value:"value 2"});F=B.getValues(false,true);expect(F).toEqual({one:"dirty value"})});it("should return the emptyText for empty fields if the 'includeEmptyText' argument is true",function(){E({name:"one",value:"value 1",dirty:true,emptyText:"empty 1"});E({name:"two",value:"",dirty:false,emptyText:"empty 2"});F=B.getValues(false,false,true);expect(F).toEqual({one:"value 1",two:"empty 2"})});it("should include fields whose value is empty string",function(){E({name:"one",value:""});E({name:"two",value:"value 2"});F=B.getValues();expect(F).toEqual({one:"",two:"value 2"})});it("should not include fields whose getSubmitData method returns null",function(){E({name:"one",value:"value 1",getSubmitData:function(){return null}});E({name:"two",value:"value 2"});F=B.getValues();expect(F).toEqual({two:"value 2"})});it("should not include filefields (which do not submit by default)",function(){E({name:"one",value:"value 1"});E({name:"two",value:"value 2"});A.add({xtype:"filefield",name:"three"});F=B.getValues();expect(F).toEqual({one:"value 1",two:"value 2"})})});describe("doAction method",function(){beforeEach(function(){D=jasmine.createSpy()});afterEach(function(){D=undefined});it("should accept an instance of Ext.form.action.Action for the 'action' argument",function(){var F=new MockAction();spyOn(F,"run");runs(function(){B.doAction(F)});waitsFor(function(){return F.run.callCount===1},"did not call the action's run method")});it("should accept an action name for the 'action' argument",function(){spyOn(MockAction.prototype,"run");runs(function(){B.doAction("mock")});waitsFor(function(){return MockAction.prototype.run.callCount===1},"did not call the action's run method")});it("should pass the options argument to the Action constructor",function(){B.doAction("mock",{});expect(D).toHaveBeenCalledWith({form:B})});it("should call the beforeAction method",function(){spyOn(B,"beforeAction");B.doAction("mock");expect(B.beforeAction).toHaveBeenCalledWith(C)});it("should fire the beforeaction event",function(){var F=jasmine.createSpy();B.on("beforeaction",F);B.doAction("mock");expect(F).toHaveBeenCalledWith(B,C)});it("should cancel the action if a beforeaction listener returns false",function(){var F=function(){return false};B.on("beforeaction",F);spyOn(B,"beforeAction");B.doAction("mock");expect(B.beforeAction).not.toHaveBeenCalled()})});describe("beforeAction method",function(){it("should call syncValue on any fields with that method",function(){var G=new MockAction(),F=jasmine.createSpy();E({name:"one",syncValue:F});B.beforeAction(G);expect(F).toHaveBeenCalled()});xit("should display a wait message box if waitMsg is defined and waitMsgTarget is not defined",function(){});xit("should mask the owner's element if waitMsg is defined and waitMsgTarget is true",function(){});xit("should mask the waitMsgTarget element if waitMsg is defined and waitMsgTarget is an element",function(){})});describe("afterAction method",function(){xit("should hide the wait message box if waitMsg is defined and waitMsgTarget is not defined",function(){});xit("should unmask the owner's element if waitMsg is defined and waitMsgTarget is true",function(){});xit("should unmask the waitMsgTarget element if waitMsg is defined and waitMsgTarget is an element",function(){});describe("success",function(){it("should invoke the reset method if the Action's reset option is true",function(){var F=new MockAction();F.reset=false;spyOn(B,"reset");B.afterAction(F,true);expect(B.reset).not.toHaveBeenCalled();F.reset=true;B.afterAction(F,true);expect(B.reset).toHaveBeenCalled()});it("should invoke the Action's success option as a callback with a reference to the BasicForm and the Action",function(){var F=jasmine.createSpy(),G=new MockAction();G.success=F;B.afterAction(G,true);expect(F).toHaveBeenCalledWith(B,G)});it("should fire the 'actioncomplete' event with a reference to the BasicForm and the Action",function(){var F=jasmine.createSpy(),G=new MockAction();B.on("actioncomplete",F);B.afterAction(G,true);expect(F).toHaveBeenCalledWith(B,G)})});describe("failure",function(){it("should invoke the Action's failure option as a callback with a reference to the BasicForm and the Action",function(){var F=jasmine.createSpy(),G=new MockAction();G.failure=F;B.afterAction(G,false);expect(F).toHaveBeenCalledWith(B,G)});it("should fire the 'actionfailed' event with a reference to the BasicForm and the Action",function(){var F=jasmine.createSpy(),G=new MockAction();B.on("actionfailed",F);B.afterAction(G,false);expect(F).toHaveBeenCalledWith(B,G)})})});describe("submit method",function(){it("should call doAction with 'submit' by default",function(){var F={};spyOn(B,"doAction");B.submit(F);expect(B.doAction).toHaveBeenCalledWith("submit",F)});it("should call doAction with 'standardsubmit' if the standardSubmit config is true",function(){B.standardSubmit=true;var F={};spyOn(B,"doAction");B.submit(F);expect(B.doAction).toHaveBeenCalledWith("standardsubmit",F)});it("should call doAction with 'directsubmit' if the api config is defined",function(){B.api={};var F={};spyOn(B,"doAction");B.submit(F);expect(B.doAction).toHaveBeenCalledWith("directsubmit",F)})});describe("load method",function(){it("should call doAction with 'load' by default",function(){var F={};spyOn(B,"doAction");B.load(F);expect(B.doAction).toHaveBeenCalledWith("load",F)});it("should call doAction with 'directload' if the api config is defined",function(){B.api={};var F={};spyOn(B,"doAction");B.load(F);expect(B.doAction).toHaveBeenCalledWith("directload",F)})});describe("checkValidity method",function(){it("should be called when a field's 'validitychange' event is fired",function(){runs(function(){spyOn(B,"checkValidity");B.checkValidityTask=new Ext.util.DelayedTask(B.checkValidity,B);var F=E({name:"one"});F.fireEvent("validitychange",F,false)});waitsFor(function(){return B.checkValidity.callCount===1},"checkValidity was not called")});it("should fire the 'validitychange' event if the overall validity of the form has changed",function(){var G=jasmine.createSpy("validitychange handler"),F=E({name:"one"}),H=E({name:"two"});B.checkValidity();B.on("validitychange",G);F.isValid=function(){return false};B.checkValidity();expect(G).toHaveBeenCalled()});it("should not fire the 'validitychange' event if the overally validity of the form has not changed",function(){var G=jasmine.createSpy("validitychange handler"),F=E({name:"one",isValid:function(){return false}}),H=E({name:"two",isValid:function(){return false}});B.checkValidity();B.on("validitychange",G);F.isValid=function(){return true};B.checkValidity();expect(G).not.toHaveBeenCalled()});describe("add/remove items",function(){it("should checkValidity when removing a field",function(){runs(function(){E({name:"one"});E({name:"two"});spyOn(B,"checkValidity");B.checkValidityTask=new Ext.util.DelayedTask(B.checkValidity,B);A.remove(0)});waitsFor(function(){return B.checkValidity.callCount===1},"checkValidity was not called")});it("should checkValidity when adding a field",function(){runs(function(){E({name:"one"});spyOn(B,"checkValidity");B.checkValidityTask=new Ext.util.DelayedTask(B.checkValidity,B);E({name:"two"})});waitsFor(function(){return B.checkValidity.callCount===1},"checkValidity was not called")});it("should checkValidity when removing a container that contains a field",function(){runs(function(){var F=A.add({xtype:"container"});E({name:"one"},F);spyOn(B,"checkValidity");B.checkValidityTask=new Ext.util.DelayedTask(B.checkValidity,B);A.remove(0)});waitsFor(function(){return B.checkValidity.callCount===1},"checkValidity was not called")});it("should checkValidity when adding a container that contains a field",function(){runs(function(){var F=new Ext.container.Container();E({name:"one"},F);spyOn(B,"checkValidity");B.checkValidityTask=new Ext.util.DelayedTask(B.checkValidity,B);A.add(F)});waitsFor(function(){return B.checkValidity.callCount===1},"checkValidity was not called")})})});describe("checkDirty method",function(){it("should be called when a field's 'dirtychange' event is fired",function(){runs(function(){spyOn(B,"checkDirty");B.checkDirtyTask=new Ext.util.DelayedTask(B.checkDirty,B);var F=E({name:"one"});F.fireEvent("dirtychange",F,false)});waitsFor(function(){return B.checkDirty.callCount===1},"checkDirty was not called")});it("should fire the 'dirtychange' event if the overall dirty state of the form has changed",function(){var G=jasmine.createSpy("dirtychange handler"),F=E({name:"one"}),H=E({name:"two"});B.checkDirty();B.on("dirtychange",G);F.isDirty=function(){return true};B.checkDirty();expect(G).toHaveBeenCalled()});it("should not fire the 'dirtychange' event if the overally dirty state of the form has not changed",function(){var G=jasmine.createSpy("dirtychange handler"),F=E({name:"one",isDirty:function(){return true}}),H=E({name:"two",isDirty:function(){return true}});B.checkDirty();B.on("dirtychange",G);F.isDirty=function(){return false};B.checkDirty();expect(G).not.toHaveBeenCalled()})});describe("formBind child component property",function(){it("should disable a child component with formBind=true when the form becomes invalid",function(){var F=E({name:"one",isValid:function(){return true}}),H=E({name:"two",isValid:function(){return true}}),G=new Ext.Button({formBind:true});B.checkValidity();spyOn(G,"setDisabled");A.add(G);F.isValid=function(){return false};B.checkValidity();expect(G.setDisabled).toHaveBeenCalledWith(true)});it("should enable a child component with formBind=true when the form becomes valid",function(){var F=E({name:"one",isValid:function(){return false}}),H=E({name:"two",isValid:function(){return true}}),G=new Ext.Button({formBind:true,disabled:true});B.checkValidity();spyOn(G,"setDisabled");A.add(G);F.isValid=function(){return true};B.checkValidity();expect(G.setDisabled).toHaveBeenCalledWith(false)});it("should not disable a child component with formBind=true when the form remains invalid",function(){var F=E({name:"one",isValid:function(){return false}}),H=E({name:"two"}),G=new Ext.Button({formBind:true});B.checkValidity();spyOn(G,"setDisabled");A.add(G);B.checkValidity();expect(G.setDisabled).not.toHaveBeenCalled()});it("should not enable a child component with formBind=true when the form remains valid",function(){var F=E({name:"one",isValid:function(){return true}}),H=E({name:"two",isValid:function(){return true}}),G=new Ext.Button({formBind:true,disabled:true});B.checkValidity();spyOn(G,"setDisabled");A.add(G);F.isValid=function(){return true};B.checkValidity();expect(G.setDisabled).not.toHaveBeenCalled()});it("should update a formBind button's state when a field changes enabled/disabled state",function(){var F=E({name:"one",isValid:function(){return true}}),H=A.add({xtype:"textfield",name:"two",allowBlank:false}),G=new Ext.Button({formBind:true});A.add(G);B.checkValidity();expect(G.disabled).toBe(true);H.disable();waitsFor(function(){return G.disabled===false})})});describe("loadRecord method",function(){it("should call setValues with the record's data",function(){var G={one:"value 1",two:"value 2"},F={getData:function(){return G}};spyOn(B,"setValues");B.loadRecord(F);expect(B.setValues).toHaveBeenCalledWith(G)});it("should keep a reference to the record on the form",function(){var G={one:"value 1",two:"value 2"},F={getData:function(){return G}};B.loadRecord(F);expect(B.getRecord()).toBe(F)})});describe("updateRecord method",function(){var F;beforeEach(function(){Ext.define("BasicFormTestModel",{extend:"Ext.data.Model",fields:["one",{type:"int",name:"two"},{type:"date",name:"three"}]});F=new BasicFormTestModel()});afterEach(function(){Ext.undefine("BasicFormTestModel");Ext.data.Model.schema.clear()});it("should update fields on a given model to match corresponding form fields",function(){var G=new Date();E({name:"one",value:"valueone"});E(new Ext.form.field.Number({name:"two",value:2}));E(new Ext.form.field.Date({name:"three",value:G}));B.updateRecord(F);expect(F.get("one")).toBe("valueone");expect(F.get("two")).toBe(2);var I=F.get("three"),H=G;expect(I.getFullYear()).toBe(H.getFullYear());expect(I.getMonth()).toBe(H.getMonth());expect(I.getDate()).toBe(H.getDate())});it("should use a record specified by loadRecord if one isn't provided",function(){B.loadRecord(F);var G=new Date();E({name:"one",value:"valueone"});E(new Ext.form.field.Number({name:"two",value:2}));E(new Ext.form.field.Date({name:"three",value:G}));B.updateRecord();expect(F.get("one")).toBe("valueone");expect(F.get("two")).toBe(2);var I=F.get("three"),H=G;expect(I.getFullYear()).toBe(H.getFullYear());expect(I.getMonth()).toBe(H.getMonth());expect(I.getDate()).toBe(H.getDate())})});describe("radios & getModelData",function(){it("should take the selected radio value",function(){A.add([{xtype:"radiofield",inputValue:"1",name:"foo"},{xtype:"radiofield",inputValue:"2",name:"foo"},{xtype:"radiofield",inputValue:"3",name:"foo"},{xtype:"radiofield",inputValue:"4",name:"foo"}]);A.items.getAt(2).setValue(true);expect(B.getValues(undefined,undefined,undefined,true).foo).toBe("3")});it("should return null if there is no selected radio",function(){A.add([{xtype:"radiofield",inputValue:"1",name:"foo"},{xtype:"radiofield",inputValue:"2",name:"foo"},{xtype:"radiofield",inputValue:"3",name:"foo"},{xtype:"radiofield",inputValue:"4",name:"foo"}]);A.items.getAt(2).setValue(true);A.items.getAt(2).setValue(false);expect(B.getValues(undefined,undefined,undefined,true).foo).toBeNull()})})})