describe("Ext.slider.Widget",function(){var A,B;afterEach(function(){A=B=Ext.destroy(A)});describe("binding",function(){var E,C;beforeEach(function(){C=new Ext.app.ViewModel({data:{val:20}});E=C.getData()});function F(G){A=Ext.create({xtype:"panel",renderTo:Ext.getBody(),items:B=Ext.create(Ext.apply({xtype:"sliderwidget",bind:"{val}",viewModel:C,width:200,height:20,animate:false},G))});D()}function D(){C.getScheduler().notify()}afterEach(function(){C=Ext.destroy(C)});it("should receive the initial value",function(){F();var G=B.getValue();expect(G).toBe(20)});it("should update viewModel on setValue complete",function(){F({publishOnComplete:true});B.setValue(50);D();expect(E.val).toBe(50)});it("should update viewModel on setValue when publishOnComplete:false",function(){F({publishOnComplete:false});B.setValue(50);D();expect(E.val).toBe(50)})})})