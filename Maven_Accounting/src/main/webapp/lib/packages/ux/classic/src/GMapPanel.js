Ext.define("Ext.ux.GMapPanel",{extend:"Ext.panel.Panel",alias:"widget.gmappanel",requires:["Ext.window.MessageBox"],initComponent:function(){Ext.applyIf(this,{plain:true,gmapType:"map",border:false});this.callParent()},onBoxReady:function(){var A=this.center;this.callParent(arguments);if(A){if(A.geoCodeAddr){this.lookupCode(A.geoCodeAddr,A.marker)}else{this.createMap(A)}}else{Ext.raise("center is required")}},createMap:function(A,B){var C=Ext.apply({},this.mapOptions);C=Ext.applyIf(C,{zoom:14,center:A,mapTypeId:google.maps.MapTypeId.HYBRID});this.gmap=new google.maps.Map(this.body.dom,C);if(B){this.addMarker(Ext.applyIf(B,{position:A}))}Ext.each(this.markers,this.addMarker,this);this.fireEvent("mapready",this,this.gmap)},addMarker:function(A){A=Ext.apply({map:this.gmap},A);if(!A.position){A.position=new google.maps.LatLng(A.lat,A.lng)}var B=new google.maps.Marker(A);Ext.Object.each(A.listeners,function(C,D){google.maps.event.addListener(B,C,D)});return B},lookupCode:function(B,A){this.geocoder=new google.maps.Geocoder();this.geocoder.geocode({address:B},Ext.Function.bind(this.onLookupComplete,this,[A],true))},onLookupComplete:function(C,B,A){if(B!="OK"){Ext.MessageBox.alert("Error",'An error occured: "'+B+'"');return }this.createMap(C[0].geometry.location,A)},afterComponentLayout:function(A,B){this.callParent(arguments);this.redraw()},redraw:function(){var A=this.gmap;if(A){google.maps.event.trigger(A,"resize")}}})