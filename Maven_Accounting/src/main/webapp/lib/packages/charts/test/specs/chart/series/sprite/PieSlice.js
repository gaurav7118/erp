describe("Ext.chart.series.sprite.PieSlice",function(){describe("destroy",function(){it("should remove itself from the surface",function(){var A=new Ext.draw.Surface({}),B=new Ext.chart.series.sprite.PieSlice({}),C=B.getId();A.add(B);B.destroy();expect(A.getItems().length).toBe(0);expect(A.get(C)).toBe(undefined);A.destroy()})})})