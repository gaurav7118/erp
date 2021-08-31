Ext.define("Ext.data.proxy.Sql",{alias:"proxy.sql",extend:"Ext.data.proxy.Client",alternateClassName:"Ext.data.proxy.SQL",isSQLProxy:true,config:{reader:null,writer:null,table:null,database:"Sencha"},_createOptions:{silent:true,dirty:false},updateModel:function(C){var G=this,B,A,E,D,F;if(C){G.uniqueIdStrategy=C.identifier.isUnique;if(!G.getTable()){B=C.entityName;G.setTable(B.slice(B.lastIndexOf(".")+1))}G.columns=D=G.getPersistedModelColumns(C);G.quotedColumns=F=[];for(E=0,A=D.length;E<A;++E){F.push('"'+D[E]+'"')}}G.callParent([C])},setException:function(A,B){A.setException(B)},create:function(C){var E=this,B=C.getRecords(),A,D;C.setStarted();E.executeTransaction(function(F){E.insertRecords(B,F,function(H,G){A=H;D=G})},function(F){C.setException(F)},function(){if(D){C.setException(statementError)}else{C.process(A)}})},read:function(D){var G=this,E=G.getModel(),B=D.getRecords(),F=B?B[0]:null,I,H,A,C;if(F&&!F.phantom){A=F.getId()}else{A=D.getId()}if(A!==undefined){C={idOnly:true,id:A}}else{C={page:D.getPage(),start:D.getStart(),limit:D.getLimit(),sorters:D.getSorters(),filters:D.getFilters()}}D.setStarted();G.executeTransaction(function(J){G.selectRecords(J,C,function(L,K){I=L;H=K})},function(J){D.setException(J)},function(){if(H){D.setException(statementError)}else{D.process(I)}})},update:function(C){var E=this,B=C.getRecords(),A,D;C.setStarted();E.executeTransaction(function(F){E.updateRecords(F,B,function(H,G){A=H;D=G})},function(F){C.setException(F)},function(){if(D){C.setException(statementError)}else{C.process(A)}})},erase:function(C){var E=this,B=C.getRecords(),A,D;C.setStarted();E.executeTransaction(function(F){E.destroyRecords(F,B,function(H,G){A=H;D=G})},function(F){C.setException(F)},function(){if(D){C.setException(D)}else{C.process(A)}})},createTable:function(B){var A=this;if(!B){A.executeTransaction(function(C){A.createTable(C)});return }A.executeStatement(B,'CREATE TABLE IF NOT EXISTS "'+A.getTable()+'" ('+A.getSchemaString()+")",function(){A.tableExists=true})},insertRecords:function(J,K,D){var P=this,A=P.columns,H=J.length,R=0,O=P.uniqueIdStrategy,N=P._createOptions,M=J.length,I,C,E,L,Q,B,F,G;G=function(S){++R;if(R===H){D.call(P,new Ext.data.ResultSet({success:!F}),F)}};E=Ext.String.repeat("?",A.length,",");L='INSERT INTO "'+P.getTable()+'" ('+P.quotedColumns.join(",")+") VALUES ("+E+")";for(I=0;I<M;++I){C=J[I];Q=P.getRecordData(C);B=P.getColumnValues(A,Q);(function(S){P.executeStatement(K,L,B,function(U,T){if(!O){S.setId(T.insertId,N)}G()},function(U,T){if(!F){F=[]}F.push(T);G()})})(C)}},selectRecords:function(R,U,E,C){var V=this,K=V.getModel(),I=K.idProperty,S='SELECT * FROM "'+V.getTable()+'"',A=" WHERE ",N=" ORDER BY ",D=[],B,J,F,Q,T,M,L,P,G,H,O;if(U.idOnly){S+=A+'"'+I+'" = ?';D.push(U)}else{J=U.filters;T=J&&J.length;if(T){for(Q=0;Q<T;Q++){L=J[Q];G=L.getProperty();O=V.toSqlValue(L.getValue(),K.getField(G));H=L.getOperator();if(G!==null){H=H||"=";F="?";if(H==="like"||(H==="="&&L.getAnyMatch())){H="LIKE";O="%"+O+"%"}if(H==="in"||H==="notin"){if(H==="notin"){H="not in"}F="("+Ext.String.repeat("?",O.length,",")+")";D=D.concat(O)}else{D.push(O)}S+=A+'"'+G+'" '+H+" "+F;A=" AND "}}}B=U.sorters;T=B&&B.length;if(T){for(Q=0;Q<T;Q++){P=B[Q];G=P.getProperty();if(G!==null){S+=N+'"'+G+'" '+P.getDirection();N=", "}}}if(U.page!==undefined){S+=" LIMIT "+parseInt(U.start,10)+", "+parseInt(U.limit,10)}}V.executeStatement(R,S,D,function(Y,h){var l=h.rows,f=l.length,Z=[],d=K.fields,X=d.length,k,c,b,e,a,g,W;for(b=0,e=f;b<e;++b){k=l.item(b);c={};for(a=0;a<X;++a){g=d[a];W=g.name;c[W]=V.fromSqlValue(k[W],g)}Z.push(new K(c))}E.call(V,new Ext.data.ResultSet({records:Z,success:true,total:f,count:f}))},function(X,W){E.call(V,new Ext.data.ResultSet({success:false,total:0,count:0}),W)})},updateRecords:function(M,L,D){var Q=this,A=Q.columns,I=Q.quotedColumns,J=L.length,S=0,G=[],P=Q._createOptions,O,K,C,E,N,R,B,F,H;H=function(T){++S;if(S===J){D.call(Q,new Ext.data.ResultSet({success:!F}),F)}};for(K=0,O=I.length;K<O;K++){G.push(I[K]+" = ?")}N='UPDATE "'+Q.getTable()+'" SET '+G.join(", ")+' WHERE "'+Q.getModel().idProperty+'" = ?';for(K=0,O=L.length;K<O;++K){C=L[K];R=Q.getRecordData(C);B=Q.getColumnValues(A,R);B.push(C.getId());(function(T){Q.executeStatement(M,N,B,function(V,U){H()},function(V,U){if(!F){F=[]}F.push(U);H()})})(C)}},destroyRecords:function(B,C,K){var I=this,L=I.getTable(),M=I.getModel().idProperty,A=[],J=[],D=[],H=C.length,G='"'+M+'" = ?',E,O,F,N;for(E=0;E<H;E++){A.push(G);J.push(C[E].getId())}N='DELETE FROM "'+I.getTable()+'" WHERE '+A.join(" OR ");I.executeStatement(B,N,J,function(Q,P){K.call(I,new Ext.data.ResultSet({success:true}))},function(Q,P){K.call(I,new Ext.data.ResultSet({success:false}),P)})},getRecordData:function(E){var I=this,G=E.fields,L=E.idProperty,F=I.uniqueIdStrategy,D={},H=G.length,B=E.data,C,A,K,J;for(C=0;C<H;++C){J=G[C];if(J.persist!==false){A=J.name;if(A===L&&!F){continue}D[A]=I.toSqlValue(B[A],J)}}return D},getColumnValues:function(D,G){var A=D.length,B=[],C,E,F;for(C=0;C<A;C++){E=D[C];F=G[E];if(F!==undefined){B.push(F)}}return B},getSchemaString:function(){var I=this,B=[],D=I.getModel(),K=D.idProperty,F=D.fields,E=I.uniqueIdStrategy,G=F.length,C,J,H,A;for(C=0;C<G;C++){J=F[C];H=J.getType();A=J.name;if(A===K){if(E){H=I.convertToSqlType(H);B.unshift('"'+K+'" '+H)}else{B.unshift('"'+K+'" INTEGER PRIMARY KEY AUTOINCREMENT')}}else{H=I.convertToSqlType(H);B.push('"'+A+'" '+H)}}return B.join(", ")},convertToSqlType:function(A){switch(A.toLowerCase()){case"string":case"auto":return"TEXT";case"int":case"date":return"INTEGER";case"float":return"REAL";case"bool":return"NUMERIC"}},dropTable:function(){var A=this;A.executeTransaction(function(B){A.executeStatement(B,'DROP TABLE "'+A.getTable()+'"',function(){A.tableExists=false})},null,null,false)},getDatabaseObject:function(){return window.openDatabase(this.getDatabase(),"1.0","Sencha Database",5*1024*1024)},privates:{executeStatement:function(F,E,A,D,B){var C=this;F.executeSql(E,A,D?function(){D.apply(C,arguments)}:null,B?function(){B.apply(C,arguments)}:null)},executeTransaction:function(C,A,E,D){var B=this;D=D!==false;B.getDatabaseObject().transaction(C?function(F){if(D&&!B.tableExists){B.createTable(F)}C.apply(B,arguments)}:null,A?function(){A.apply(B,arguments)}:null,E?function(){E.apply(B,arguments)}:null)},fromSqlValue:function(A,B){if(B.isDateField){A=A?new Date(A):null}else{if(B.isBooleanField){A=A===1}}return A},getPersistedModelColumns:function(D){var F=D.fields,E=this.uniqueIdStrategy,I=D.idProperty,B=[],G=F.length,C,H,A;for(C=0;C<G;++C){H=F[C];A=H.name;if(A===I&&!E){continue}if(H.persist!==false){B.push(H.name)}}return B},toSqlValue:function(A,B){if(B.isDateField){A=A?A.getTime():null}else{if(B.isBooleanField){A=A?1:0}}return A}}})