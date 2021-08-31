Ext.define("Ext.data.Session",{requires:["Ext.data.schema.Schema","Ext.data.Batch","Ext.data.matrix.Matrix","Ext.data.session.ChangesVisitor","Ext.data.session.ChildChangesVisitor","Ext.data.session.BatchVisitor"],isSession:true,config:{schema:"default",parent:null,autoDestroy:true,crudProperties:{create:"C",read:"R",update:"U",drop:"D"}},destroyed:false,crudOperations:[{type:"R",entityMethod:"readEntities"},{type:"C",entityMethod:"createEntities"},{type:"U",entityMethod:"updateEntities"},{type:"D",entityMethod:"dropEntities"}],crudKeys:{C:1,R:1,U:1,D:1},constructor:function(A){var B=this;B.data={};B.matrices={};B.identifierCache={};B.recordCreator=B.recordCreator.bind(B);B.initConfig(A)},destroy:function(){var D=this,B=D.matrices,E=D.data,C,F,A,G;for(G in B){B[G].destroy()}for(C in E){F=E[C];for(G in F){A=F[G].record;if(A){A.$source=A.session=null}}}D.recordCreator=D.matrices=D.data=null;D.setSchema(null);D.callParent()},adopt:function(B){var D=this,C=B.associations,A;D.checkModelType(B.self);if(B.session&&B.session!==D){Ext.raise("Record already belongs to an existing session")}if(B.session!==D){B.session=D;D.add(B);if(C){for(A in C){C[A].adoptAssociated(B,D)}}}},commit:function(){var D=this.data,B=this.matrices,C,E,F,A;for(C in D){E=D[C];for(F in E){A=E[F].record;if(A){A.commit()}}}for(F in B){B[F].commit()}},createRecord:function(B,C){this.checkModelType(B);var D=B.$isClass?B:this.getSchema().getEntity(B),A=this.getParent(),E;if(C&&A){E=D.getIdFromData(C);if(A.peekRecord(D,E)){Ext.raise("A parent session already contains an entry for "+D.entityName+": "+E)}}return new D(C,this)},getChanges:function(){var A=new Ext.data.session.ChangesVisitor(this);this.visitData(A);return A.result},getChangesForParent:function(){var A=new Ext.data.session.ChildChangesVisitor(this);this.visitData(A);return A.result},getRecord:function(H,A,C){var G=this,B=H.isModel,F,D,I,E;if(B){B=H;A=H.id;H=H.self}F=G.peekRecord(H,A);if(!F){D=H.$isClass?H:G.getSchema().getEntity(H);I=G.getParent();if(I){E=I.peekRecord(D,A)}if(E){if(E.isLoading()){B=false}else{F=E.copy(undefined,G);F.$source=E}}if(!F){if(B){F=B;G.adopt(F)}else{F=D.createWithId(A,null,G);if(C!==false){F.load(Ext.isObject(C)?C:undefined)}}}}return F},getSaveBatch:function(A){var B=new Ext.data.session.BatchVisitor();this.visitData(B);return B.getBatch(A)},onInvalidAssociationEntity:function(A,B){Ext.raise("Unable to read association entity: "+this.getModelIdentifier(A,B))},onInvalidEntityCreate:function(A,B){Ext.raise("Cannot create, record already not exists: "+this.getModelIdentifier(A,B))},onInvalidEntityDrop:function(A,B){Ext.raise("Cannot drop, record does not exist: "+this.getModelIdentifier(A,B))},onInvalidEntityRead:function(A,B){Ext.raise("Cannot read, record already not exists: "+this.getModelIdentifier(A,B))},onInvalidEntityUpdate:function(A,C,B){if(B){Ext.raise("Cannot update, record dropped: "+this.getModelIdentifier(A,C))}else{Ext.raise("Cannot update, record does not exist: "+this.getModelIdentifier(A,C))}},peekRecord:function(E,H,A){this.checkModelType(E);var G=E.$isClass?E:this.getSchema().getEntity(E),C=G.entityName,F=this.data[C],B,D;F=F&&F[H];B=F&&F.record;if(!B&&A){D=this.getParent();B=D&&D.peekRecord(E,H,A)}return B||null},save:function(){if(!this.getParent()){Ext.raise("Cannot commit session, no parent exists")}var A=new Ext.data.session.ChildChangesVisitor(this);this.visitData(A);this.getParent().update(A.result)},spawn:function(){return new this.self({schema:this.getSchema(),parent:this})},update:function(I){var K=this,E=K.getSchema(),A=K.crudOperations,J=A.length,L=K.crudKeys,M,F,O,H,D,P,C,N,G,B;K.getSchema().processKeyChecks(true);for(M in I){F=E.getEntity(M);if(!F){Ext.raise("Invalid entity type: "+M)}O=I[M];for(H=0;H<J;++H){D=A[H];P=O[D.type];if(P){K[D.entityMethod](F,P)}}}for(M in I){F=E.getEntity(M);C=F.associations;O=I[M];for(N in O){if(L[N]){continue}G=C[N];if(!G){Ext.raise("Invalid association key for "+M+', "'+N+'"')}B=O[G.role];G.processUpdate(K,B)}}},privates:{add:function(B){var E=this,F=B.id,D=E.getEntry(B.self,F),C,A;if(D.record){Ext.raise("Duplicate id "+B.id+" for "+B.entityName)}D.record=B;E.registerReferences(B);C=B.associations;for(A in C){C[A].checkMembership(E,B)}},afterErase:function(A){this.evict(A)},applySchema:function(A){return Ext.data.schema.Schema.get(A)},checkModelType:function(A){if(A.$isClass){A=A.entityName}if(!A){Ext.raise("Unable to use anonymous models in a Session")}else{if(!this.getSchema().getEntity(A)){Ext.raise("Unknown entity type "+A)}}},createEntities:function(D,B){var A=B.length,C,E,F,G;for(C=0;C<A;++C){E=B[C];G=D.getIdFromData(E);F=this.peekRecord(D,G);if(!F){F=this.createRecord(D,E)}else{this.onInvalidEntityCreate(D,G)}F.phantom=true}},dropEntities:function(E,D){var B=D.length,C,F,G,A;if(B){A=Ext.isObject(D[0])}for(C=0;C<B;++C){G=D[C];if(A){G=E.getIdFromData(G)}F=this.peekRecord(E,G);if(F){F.drop()}else{this.onInvalidEntityDrop(E,G)}}},evict:function(A){var B=A.entityName,D=this.data[B],E=A.id,C;if(D){delete D[E]}},getEntityList:function(D,C){var A=C.length,B,G,F,E;for(B=0;B<A;++B){G=C[B];F=this.peekRecord(D,G);if(F){C[B]=F}else{E=true;C[B]=null;this.onInvalidAssociationEntity(D,G)}}if(E){C=Ext.Array.clean(C)}return C},getEntry:function(B,F){if(B.isModel){F=B.getId();B=B.self}var D=B.$isClass?B:this.getSchema().getEntity(B),A=D.entityName,E=this.data,C;C=E[A]||(E[A]={});C=C[F]||(C[F]={});return C},getRefs:function(D,C,H){var F=this.getEntry(D),E=F&&F.refs&&F.refs[C.role],G=H&&this.getParent(),I,A,B;if(G){I=G.getRefs(D,C);if(I){for(A in I){B=I[A];if((!E||!E[A])){this.getRecord(B.self,B.id)}}E=F&&F.refs&&F.refs[C.role]}}return E||null},getIdentifier:function(F){var E=this.getParent(),A,C,D,B;if(E){B=E.getIdentifier(F)}else{A=this.identifierCache;C=F.identifier;D=C.id||F.entityName;B=A[D];if(!B){if(C.clone){B=C.clone({cache:A})}else{B=C}A[D]=B}}return B},getMatrix:function(A,E){var D=A.isManyToMany?A.name:A,C=this.matrices,B;B=C[D];if(!B&&!E){B=C[D]=new Ext.data.matrix.Matrix(this,A)}return B||null},getMatrixSlice:function(D,C){var A=this.getMatrix(D.association),B=A[D.side];return B.get(C)},getModelIdentifier:function(A,B){return B+"@"+A.entityName},onIdChanged:function(F,L,H){var Q=this,B=Q.matrices,I=F.entityName,M=F.id,T=Q.data[I],C=T[L],P=F.associations,D=C.refs,A=Q._setNoRefs,G,E,O,J,S,K,N,R;if(T[H]){Ext.raise("Cannot change "+I+" id from "+L+" to "+H+" id already exists")}delete T[L];T[H]=C;for(R in B){B[R].updateId(F,L,H)}if(D){for(K in D){N=D[K];S=P[K];G=S.association;if(!G.isManyToMany){E=G.field.name;for(J in N){N[J].set(E,M,A)}}}}Q.registerReferences(F,L)},processManyBlock:function(D,E,G,B){var H=this,A,F,C,I;if(G){for(A in G){F=H.peekRecord(D,A);if(F){C=H.getEntityList(E.cls,G[A]);I=E.getAssociatedItem(F);H[B](E,I,F,C)}else{H.onInvalidAssociationEntity(D,A)}}}},processManyCreate:function(D,C,A,B){if(C){C.add(B)}else{A[D.getterName](null,null,B)}},processManyDrop:function(D,C,A,B){if(C){C.remove(B)}},processManyRead:function(D,C,A,B){if(C){C.setRecords(B)}else{A[D.getterName](null,null,B)}},readEntities:function(D,B){var A=B.length,C,E,F,G;for(C=0;C<A;++C){E=B[C];G=D.getIdFromData(E);F=this.peekRecord(D,G);if(!F){F=this.createRecord(D,E)}else{this.onInvalidEntityRead(D,G)}F.phantom=false}},recordCreator:function(C,D){var B=this,E=D.getIdFromData(C),A=B.peekRecord(D,E,true);if(!A){A=new D(C,B)}else{A=B.getRecord(D,E)}return A},registerReferences:function(H,C){var K=H.entityName,B=H.id,A=H.data,G=C||C===0,L,F,N,I,D,M,J,E;I=(M=H.references).length;for(F=0;F<I;++F){D=M[F];N=A[D.name];if(N||N===0){D=D.reference;K=D.type;E=D.inverse.role;L=this.getEntry(D.cls,N);J=L.refs||(L.refs={});J=J[E]||(J[E]={});J[B]=H;if(G){delete J[C]}}}},updateEntities:function(E,B){var A=B.length,D,F,G,H,C;if(Ext.isArray(B)){for(D=0;D<A;++D){F=B[D];H=E.getIdFromData(F);G=this.peekRecord(E,H);if(G){G.set(F)}else{this.onInvalidEntityUpdate(E,H)}}}else{for(H in B){F=B[H];G=this.peekRecord(E,H);if(G&&!G.dropped){C=G.set(F)}else{this.onInvalidEntityUpdate(E,H,!!G)}}}},updateReference:function(F,H,C,A){var D=H.reference,I=D.type,E=D.inverse.role,B=F.id,J,G;if(A||A===0){G=this.getEntry(I,A).refs[E];delete G[B]}if(C||C===0){J=this.getEntry(I,C);G=J.refs||(J.refs={});G=G[E]||(G[E]={});G[B]=F}},visitData:function(G){var I=this,E=I.data,M=I.matrices,L,H,C,O,J,D,B,F,K,N,A;I.getSchema().processKeyChecks(true);for(B in E){L=E[B];for(C in L){F=L[C].record;if(F){if(F.phantom||F.dirty||F.dropped){if(G.onDirtyRecord){G.onDirtyRecord(F)}}else{if(G.onCleanRecord){G.onCleanRecord(F)}}}}}if(G.onMatrixChange){for(B in M){J=M[B].left;N=J.slices;H=J.role.association;for(C in N){K=N[C];D=K.members;for(O in D){A=(F=D[O])[2];if(A){G.onMatrixChange(H,F[0],F[1],A)}}}}}return G},_setNoRefs:{refs:false}}})