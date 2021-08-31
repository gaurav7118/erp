Ext.define("Ext.layout.SizeModel",{constructor:function(C){var E=this,D=E.self,A=D.sizeModelsArray,B;Ext.apply(E,C);E[B=E.name]=true;E.fixed=!(E.auto=E.natural||E.shrinkWrap);A[E.ordinal=A.length]=D[B]=D.sizeModels[B]=E},statics:{sizeModelsArray:[],sizeModels:{}},calculated:false,configured:false,constrainedMax:false,constrainedMin:false,natural:false,shrinkWrap:false,calculatedFromConfigured:false,calculatedFromNatural:false,calculatedFromShrinkWrap:false,names:null},function(){var E=this,A=E.sizeModelsArray,C,B,G,F,D;new E({name:"calculated"});new E({name:"configured",names:{width:"width",height:"height"}});new E({name:"natural"});new E({name:"shrinkWrap"});new E({name:"calculatedFromConfigured",configured:true,calculatedFrom:true,names:{width:"width",height:"height"}});new E({name:"calculatedFromNatural",natural:true,calculatedFrom:true});new E({name:"calculatedFromShrinkWrap",shrinkWrap:true,calculatedFrom:true});new E({name:"constrainedMax",configured:true,constrained:true,names:{width:"maxWidth",height:"maxHeight"}});new E({name:"constrainedMin",configured:true,constrained:true,names:{width:"minWidth",height:"minHeight"}});new E({name:"constrainedDock",configured:true,constrained:true,constrainedByMin:true,names:{width:"dockConstrainedWidth",height:"dockConstrainedHeight"}});for(C=0,G=A.length;C<G;++C){D=A[C];D.pairsByHeightOrdinal=F=[];for(B=0;B<G;++B){F.push({width:D,height:A[B]})}}})