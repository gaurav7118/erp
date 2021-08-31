describe("Ext.form.Labelable",function(){var C=":",A;function D(E){Ext.define("spec.Labelable",Ext.apply({extend:"Ext.Component",mixins:["Ext.form.Labelable"],initComponent:function(){this.callParent();this.initLabelable()},initRenderData:function(){return Ext.applyIf(this.callParent(),this.getLabelableRenderData())},privates:{initRenderTpl:function(){this.renderTpl=this.getTpl("labelableRenderTpl");return this.callParent()}}},E))}function B(E){A=Ext.create("spec.Labelable",Ext.apply({renderTo:Ext.getBody()},E))}afterEach(function(){A.destroy();Ext.undefine("spec.Labelable")});describe("rendering",function(){beforeEach(function(){D({ui:"derp",labelClsExtra:"spec-label-extra",fieldBodyCls:"spec-body-cls",extraFieldBodyCls:"spec-body-extra",getSubTplMarkup:function(){return'<div style="height:50px;width:150px;background-color:green;"></div>'}})});describe("child els",function(){var E;beforeEach(function(){E=spec.Labelable.prototype});it("should have a labelEl Element as it's first child",function(){B();expect(A.el.first()).toBe(A.labelEl)});it("should set labelCls on the labelEl",function(){B();expect(A.labelEl).toHaveCls(E.labelCls)});it("should set labeCls with UI on the labelEl",function(){B();expect(A.labelEl).toHaveCls(E.labelCls+"-derp")});it("should set labelClsExtra on the labelEl",function(){B();expect(A.labelEl).toHaveCls("spec-label-extra")});it("should add the unselectable cls to the labelEl",function(){B();expect(A.labelEl).toHaveCls("x-unselectable")});it("should have a bodyEl after the labelEl",function(){B();expect(A.labelEl.next()).toBe(A.bodyEl)});it("should set baseBodyCls on the bodyEl",function(){B();expect(A.bodyEl).toHaveCls(E.baseBodyCls)});it("should set baseBodyCls with UI on the bodyEl",function(){B();expect(A.bodyEl).toHaveCls(E.baseBodyCls+"-derp")});it("should set fieldBodyCls on the bodyEl",function(){B();expect(A.bodyEl).toHaveCls(E.fieldBodyCls)});it("should set fieldBodyCls with UI on the bodyEl",function(){B();expect(A.bodyEl).toHaveCls(E.fieldBodyCls+"-derp")});it("should set extraFieldBodyCls on the bodyEl",function(){B();expect(A.bodyEl).toHaveCls(E.extraFieldBodyCls)});it("should not render an errorEl by default",function(){B();expect(A.errorWrapEl).toBeNull();expect(A.errorEl).toBeNull()});it("should render an errorEl if msgTarget is 'side'",function(){B({msgTarget:"side"});expect(A.bodyEl.next()).toBe(A.errorWrapEl);expect(A.errorWrapEl.first()).toBe(A.errorEl)});it("should render an errorEl if msgTarget is 'under'",function(){B({msgTarget:"under"});expect(A.bodyEl.next()).toBe(A.errorWrapEl);expect(A.errorWrapEl.first()).toBe(A.errorEl)});it("should render ariaErrorEl by default",function(){B();expect(A.ariaErrorEl.dom).toBeDefined()});it("should assign x-hidden-clip to ariaErrorEl",function(){B();expect(A.ariaErrorEl.hasCls("x-hidden-clip")).toBe(true)})});describe("fieldLabel and labelSeparator",function(){it("should render a hidden label if no fieldLabel was configured",function(){B();expect(A.labelEl.isVisible()).toBe(false)});it("should render a hidden label if hideLabel:true was configured",function(){B({fieldLabel:"Label",hideLabel:true});expect(A.labelEl.isVisible()).toBe(false)});it("should render a visible label if fieldLabel was configured",function(){B({fieldLabel:"Label"});expect(A.labelEl.isVisible()).toBe(true)});it("should render the fieldLabel into the labelEl",function(){B({fieldLabel:"Label"});expect(A.labelEl.dom.firstChild.innerHTML).toBe("Label:")});it("should render the labelSeparator after the label",function(){B({fieldLabel:"Label",labelSeparator:"-"});expect(A.labelEl.dom.firstChild.innerHTML).toBe("Label-")});it("should not render the separator if labelSeparator is empty",function(){B({fieldLabel:"Label",labelSeparator:""});expect(A.labelEl.dom.firstChild.innerHTML).toBe("Label")});describe("labelStyle",function(){it("should add the labelStyle to the labelEl",function(){B({fieldLabel:"Foo",labelStyle:"border-top: 50px solid red;"});expect(A.labelEl.getStyle("border-top-width")).toBe("50px")})})})});describe("methods",function(){describe("setFieldLabel",function(){beforeEach(function(){D({getSubTplMarkup:function(){return'<div style="background-color:green;width:200px;height:50px;"></div>'}})});it("should set the label element's innerHTML",function(){B();A.setFieldLabel("foo");expect(A.labelEl.dom.firstChild.innerHTML).toBe("foo"+C)});it("should show the label element",function(){B();A.setFieldLabel("foo");expect(A.labelEl.isVisible()).toBe(true)});it("should hide the label element when setting an empty label",function(){B({fieldLabel:"foo"});A.setFieldLabel("");expect(A.labelEl.isVisible()).toBe(false)});describe("with under error",function(){it("should add the 'x-form-error-wrap-under-side-label' cls to the errorWrapEl when the label is on the side",function(){B({msgTarget:"under"});A.setFieldLabel("foo");expect(A.errorWrapEl).toHaveCls("x-form-error-wrap-under-side-label")});it("should not add the 'x-form-error-wrap-under-side-label' cls to the errorWrapEl when the label is on the top",function(){B({msgTarget:"under",labelAlign:"top"});A.setFieldLabel("foo");expect(A.errorWrapEl).not.toHaveCls("x-form-error-wrap-under-side-label")});it("should remove the 'x-form-error-wrap-under-side-label' cls from the errorWrapEl when empty label is set",function(){B({msgTarget:"under",fieldLabel:"foo"});A.setFieldLabel("");expect(A.errorWrapEl).not.toHaveCls("x-form-error-wrap-under-side-label")})})});describe("setHideLabel",function(){beforeEach(function(){D({getSubTplMarkup:function(){return"<div></div>"}})});describe("before render",function(){it("should hide the label when rendered",function(){B({fieldLabel:"Foo",hideLabel:false,renderTo:null});A.setHideLabel(true);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(false)});it("should show the label when rendered",function(){B({fieldLabel:"Foo",hideLabel:true,renderTo:null});A.setHideLabel(false);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(true)})});describe("after render",function(){it("should hide the label",function(){B({fieldLabel:"Foo",hideLabel:false});A.setHideLabel(true);expect(A.labelEl.isVisible()).toBe(false)});it("should show the label",function(){B({fieldLabel:"Foo",hideLabel:true});A.setHideLabel(false);expect(A.labelEl.isVisible()).toBe(true)});it("should run a layout",function(){B({fieldLabel:"Foo",hideLabel:true});var E=A.componentLayoutCounter;A.setHideLabel(false);expect(A.componentLayoutCounter).toBe(E+1);E=A.componentLayoutCounter;A.setHideLabel(true);expect(A.componentLayoutCounter).toBe(E+1)})})});describe("setHideEmptyLabel",function(){beforeEach(function(){D({getSubTplMarkup:function(){return"<div></div>"}})});describe("before render",function(){it("should hide if the label is empty when rendered",function(){B({fieldLabel:"",hideEmptyLabel:false,renderTo:null});A.setHideEmptyLabel(true);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(false)});it("should show if the label is empty when rendered",function(){B({fieldLabel:"",hideEmptyLabel:true,renderTo:null});A.setHideEmptyLabel(false);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(true)});it("should not be visible if hideLabel: true is configured",function(){B({fieldLabel:"",hideEmptyLabel:true,hideLabel:true,renderTo:null});A.setHideEmptyLabel(false);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(false)});it("should not hide if the label is not empty",function(){B({fieldLabel:"Foo",hideEmptyLabel:false,renderTo:null});A.setHideEmptyLabel(true);A.render(Ext.getBody());expect(A.labelEl.isVisible()).toBe(true)})});describe("after render",function(){it("should hide if the label is empty",function(){B({fieldLabel:"",hideEmptyLabel:false});A.setHideEmptyLabel(true);expect(A.labelEl.isVisible()).toBe(false)});it("should show if the label is empty",function(){B({fieldLabel:"",hideEmptyLabel:true});A.setHideEmptyLabel(false);expect(A.labelEl.isVisible()).toBe(true)});it("should not be visible if hideLabel: true is configured",function(){B({fieldLabel:"",hideEmptyLabel:true,hideLabel:true});A.setHideEmptyLabel(false);expect(A.labelEl.isVisible()).toBe(false)});it("should not hide if the label is not empty",function(){B({fieldLabel:"Foo",hideEmptyLabel:false});A.setHideEmptyLabel(true);expect(A.labelEl.isVisible()).toBe(true)});it("should run a layout",function(){B({fieldLabel:"",hideEmptyLabel:true});var E=A.componentLayoutCounter;A.setHideEmptyLabel(false);expect(A.componentLayoutCounter).toBe(E+1);E=A.componentLayoutCounter;A.setHideEmptyLabel(true);expect(A.componentLayoutCounter).toBe(E+1)})})});describe("setActiveError/unsetActiveError",function(){var E;beforeEach(function(){D({getSubTplMarkup:function(){return"<div></div>"}});B();E=A.ariaErrorEl});afterEach(function(){E=null});describe("setActiveErrors",function(){beforeEach(function(){A.setActiveErrors(["foo","bar"])});it("should set ariaErrorEl text",function(){expect(E.dom.innerHTML).toBe("foo. bar")});it("should point actionEl aria-describedby to ariaErrorEl",function(){var F=A.getActionEl();expect(F.dom.getAttribute("aria-describedby")).toBe(E.id)});describe("unsetActiveError",function(){beforeEach(function(){A.unsetActiveError()});it("should clear ariaErrorEl text",function(){expect(E.dom.innerHTML).toBe("")});it("should remove aria-describedby attribute from actionEl",function(){var F=A.getActionEl();expect(F.dom.hasAttribute("aria-describedby")).toBe(false)})})})})});describe("layout",function(){var F={1:"width",2:"height",3:"width and height"};function E(H,G){describe((H?("shrink wrap "+F[H]):"fixed width and height")+" autoFitErrors: "+G,function(){var M=(H&1),P=(H&2),N=18,R=20,Z=16,I=1,X=105,b=5,O=[3,4],T=X-b,Y=150,U=50,J=23,a,L,V,S;beforeEach(function(){D({getSubTplMarkup:function(){return'<div style="background-color:green;width:'+(M?(Y+"px;"):"auto;")+"height:"+(P?(U+"px;"):"100%;")+'"></div>'}})});function Q(c){c=c||{};a=c.hideLabel;L=(c.labelAlign==="top");V=Y;S=U;if(!a&&!L){V+=X}if(!a&&L){S+=J}if(c.msgTarget==="side"){V+=N}if(c.msgTarget==="under"){S+=R}A=Ext.create("spec.Labelable",Ext.apply({renderTo:document.body,height:P?null:S,width:M?null:V,autoFitErrors:G,fieldLabel:'<span style="display:inline-block;width:'+T+'px;background-color:red;">&nbsp;</span>',labelSeparator:""},c))}function K(c){A.setActiveError(c||"Error Message")}function W(c){describe(c+" label",function(){var d=(c==="left");it("should layout",function(){Q({labelAlign:c});expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:X,h:S},".x-form-item-label-inner":{x:d?0:X-b-T,y:O,w:T},bodyEl:{x:X,y:0,w:Y,h:U}});expect(A.errorWrapEl).toBeNull()});it("should layout with side error",function(){Q({labelAlign:c,msgTarget:"side"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:X,h:S},".x-form-item-label-inner":{x:d?0:X-b-T,y:O,w:T},bodyEl:{x:X,y:0,w:Y,h:U},errorWrapEl:{x:V-N,y:0,w:N,h:S},errorEl:{x:V-N+I,y:(U-Z)/2,w:Z,h:Z}})});it("should layout with hidden side error",function(){Q({labelAlign:c,msgTarget:"side"});expect(A).toHaveLayout({el:{w:(M&&G)?V-N:V,h:S},labelEl:{x:0,y:0,w:X,h:S},".x-form-item-label-inner":{x:d?0:X-b-T,y:O,w:T},bodyEl:{x:X,y:0,w:(G&&!M)?Y+N:Y,h:U},errorWrapEl:{x:G?0:V-N,y:G?0:0,w:G?0:N,h:G?0:S},errorEl:{x:G?0:V-N+I,y:G?0:(U-Z)/2,w:G?0:Z,h:G?0:Z}})});(Ext.isIE10m&&!P?xit:it)("should layout with under error",function(){Q({labelAlign:c,msgTarget:"under"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:X,h:U},".x-form-item-label-inner":{x:d?0:X-b-T,y:O,w:T},bodyEl:{x:X,y:0,w:Y,h:U},errorWrapEl:{x:0,y:U,w:V,h:R},errorEl:{x:X,y:U,w:Y,h:R}})});it("should layout with hidden label",function(){Q({labelAlign:c,hideLabel:true});expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U}});expect(A.errorWrapEl).toBeNull()});it("should layout with hidden label and side error",function(){Q({labelAlign:c,hideLabel:true,msgTarget:"side"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U},errorWrapEl:{x:Y,y:0,w:N,h:S},errorEl:{x:Y+I,y:(U-Z)/2,w:Z,h:Z}})});it("should layout with hidden label and hidden side error",function(){Q({labelAlign:c,hideLabel:true,msgTarget:"side"});expect(A).toHaveLayout({el:{w:(M&&G)?V-N:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:(G&&!M)?Y+N:Y,h:U},errorWrapEl:{x:G?0:Y,y:G?0:0,w:G?0:N,h:G?0:S},errorEl:{x:G?0:Y+I,y:G?0:(U-Z)/2,w:G?0:Z,h:G?0:Z}})});(Ext.isIE10m&&!P?xit:it)("should layout with hidden label and under error",function(){Q({labelAlign:c,hideLabel:true,msgTarget:"under"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U},errorWrapEl:{x:0,y:U,w:V,h:R},errorEl:{x:0,y:U,w:V,h:R}})})})}W("left");W("right");(Ext.isIE10m&&!P?xdescribe:describe)("top label",function(){it("should layout",function(){Q({labelAlign:"top"});expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:V,h:J},".x-form-item-label-inner":{x:0,y:0,w:V,h:J},bodyEl:{x:0,y:J,w:Y,h:U}});expect(A.errorWrapEl).toBeNull()});it("should layout with side error",function(){Q({labelAlign:"top",msgTarget:"side"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:V,h:J},".x-form-item-label-inner":{x:0,y:0,w:Y,h:J},bodyEl:{x:0,y:J,w:Y,h:U},errorWrapEl:{x:Y,y:J,w:N,h:U},errorEl:{x:Y+I,y:J+((U-Z)/2),w:Z,h:Z}})});it("should layout with hidden side error",function(){Q({labelAlign:"top",msgTarget:"side"});V=(M&&G)?V-N:V;expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:V,h:J},".x-form-item-label-inner":{x:0,y:0,w:(G&&!M)?Y+N:Y,h:J},bodyEl:{x:0,y:J,w:(G&&!M)?Y+N:Y,h:U},errorWrapEl:{x:G?0:Y,y:G?0:J,w:G?0:N,h:G?0:U},errorEl:{x:G?0:Y+I,y:G?0:J+((U-Z)/2),w:G?0:Z,h:G?0:Z}})});it("should layout with under error",function(){Q({labelAlign:"top",msgTarget:"under"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{x:0,y:0,w:V,h:J},".x-form-item-label-inner":{x:0,y:0,w:V,h:J},bodyEl:{x:0,y:J,w:Y,h:U},errorWrapEl:{x:0,y:J+U,w:V,h:R},errorEl:{x:0,y:J+U,w:V,h:R}})});it("should layout with hidden label",function(){Q({labelAlign:"top",hideLabel:true});expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U}});expect(A.errorWrapEl).toBeNull()});it("should layout with hidden label and side error",function(){Q({labelAlign:"top",hideLabel:true,msgTarget:"side"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U},errorWrapEl:{x:Y,y:0,w:N,h:S},errorEl:{x:Y+I,y:(U-Z)/2,w:Z,h:Z}})});it("should layout with hidden label and hidden side error",function(){Q({labelAlign:"top",hideLabel:true,msgTarget:"side"});expect(A).toHaveLayout({el:{w:(M&&G)?V-N:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:(G&&!M)?Y+N:Y,h:U},errorWrapEl:{x:G?0:Y,y:G?0:0,w:G?0:N,h:G?0:S},errorEl:{x:G?0:Y+I,y:G?0:(U-Z)/2,w:G?0:Z,h:G?0:Z}})});it("should layout with hidden label and under error",function(){Q({labelAlign:"top",hideLabel:true,msgTarget:"under"});K();expect(A).toHaveLayout({el:{w:V,h:S},labelEl:{xywh:"0 0 0 0"},bodyEl:{x:0,y:0,w:Y,h:U},errorWrapEl:{x:0,y:U,w:V,h:R},errorEl:{x:0,y:U,w:V,h:R}})})})})}E(0,false);E(1,true);E(2,false);E(2,true);E(3,false);E(3,true)})})