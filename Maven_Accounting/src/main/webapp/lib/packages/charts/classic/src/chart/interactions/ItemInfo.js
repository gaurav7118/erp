Ext.define("Ext.chart.interactions.ItemInfo",{extend:"Ext.chart.interactions.Abstract",type:"iteminfo",alias:"interaction.iteminfo",config:{extjsGestures:{"start":{event:"click",handler:"onInfoGesture"},"move":{event:"mousemove",handler:"onInfoGesture"},"end":{event:"mouseleave",handler:"onInfoGesture"}}},item:null,onInfoGesture:function(E,A){var C=this,B=C.getItemForEvent(E),D=B&&B.series.tooltip;if(D){D.onMouseMove.call(D,E)}if(B!==C.item){if(B){B.series.showTip(B)}else{C.item.series.hideTip(C.item)}C.item=B}return false}})