Ext.define("Ext.data.session.BatchVisitor",{map:null,constructor:function(A){this.batch=A},getBatch:function(F){var G=this.map,D=this.batch,H,B,C,A,E;if(G){if(!D){D=new Ext.data.Batch()}for(C in G){H=G[C];B=H.entity;E=B.getProxy();delete H.entity;for(A in H){A=E.createOperation(A,{records:H[A]});A.entityType=B;D.add(A)}}}if(D&&F!==false){D.sort()}return D},onDirtyRecord:function(A){var D=this,B=A.phantom?"create":(A.dropped?"destroy":"update"),C=A.$className,E=(D.map||(D.map={})),F=(E[C]||(E[C]={entity:A.self}));F=F[B]||(F[B]=[]);F.push(A)}})