Ext.define("Ext.chart.series.sprite.Aggregative",{extend:"Ext.chart.series.sprite.Cartesian",requires:["Ext.draw.LimitedCache","Ext.draw.SegmentTree"],inheritableStatics:{def:{processors:{dataHigh:"data",dataLow:"data",dataClose:"data"},aliases:{dataOpen:"dataY"},defaults:{dataHigh:null,dataLow:null,dataClose:null}}},config:{aggregator:{}},applyAggregator:function(B,A){return Ext.factory(B,Ext.draw.SegmentTree,A)},constructor:function(){this.callParent(arguments)},processDataY:function(){var D=this,B=D.attr,E=B.dataHigh,A=B.dataLow,F=B.dataClose,C=B.dataY;D.callParent(arguments);if(B.dataX&&C&&C.length>0){if(E){D.getAggregator().setData(B.dataX,B.dataY,E,A,F)}else{D.getAggregator().setData(B.dataX,B.dataY)}}},getGapWidth:function(){return 1},renderClipped:function(B,C,G,F){var E=this,D=Math.min(G[0],G[2]),A=Math.max(G[0],G[2]),H=E.getAggregator()&&E.getAggregator().getAggregation(D,A,(A-D)/F[2]*E.getGapWidth());if(H){E.dataStart=H.data.startIdx[H.start];E.dataEnd=H.data.endIdx[H.end-1];E.renderAggregates(H.data,H.start,H.end,B,C,G,F)}}})