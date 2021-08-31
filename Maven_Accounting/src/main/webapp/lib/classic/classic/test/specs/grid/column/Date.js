describe("Ext.grid.column.Date",function(){var C,A,D;function B(G,H){return C.getView().getCellInclusive({row:G,column:H})}function F(H,I){var G=B(H,I);return Ext.fly(G).down(C.getView().innerSelector).dom.innerHTML}function E(G){A=new Ext.data.Store({model:spec.TestModel,data:[{field:G}]});C=new Ext.grid.Panel({store:A,columns:[{xtype:"datecolumn",format:"Y-m-d",text:"Col",dataIndex:"field",flex:1}],width:400,height:100,border:false,renderTo:Ext.getBody()});D=C.getColumnManager().getColumns()}beforeEach(function(){Ext.define("spec.TestModel",{extend:"Ext.data.Model",fields:[{name:"field",defaultValue:undefined}]})});afterEach(function(){Ext.destroy(C,A);D=A=C=null;Ext.undefine("spec.TestModel");Ext.data.Model.schema.clear()});describe("renderer",function(){it("should render render non-date values",function(){E(null);var G=F(0,0);if(G==="&nbsp;"){G="&#160;"}expect(G).toBe("&#160;")});it("should render the date according to the format",function(){E(new Date(2010,2,3));expect(F(0,0)).toBe("2010-03-03")})})})