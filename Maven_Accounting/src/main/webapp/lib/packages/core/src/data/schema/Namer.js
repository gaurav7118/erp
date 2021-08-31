Ext.define("Ext.data.schema.Namer",{mixins:["Ext.mixin.Factoryable"],requires:["Ext.util.Inflector"],alias:"namer.default",isNamer:true,capitalize:function(A){return Ext.String.capitalize(A)},fieldRole:function(B){var A=B.match(this.endsWithIdRe,"");if(A){B=B.substr(0,B.length-(A[1]||A[2]).length)}return this.apply("uncapitalize",B)},idField:function(A){return this.apply("uncapitalize,singularize",A)+"Id"},instanceName:function(A){return this.apply("underscore",A)},multiRole:function(A){return this.apply("undotted,uncapitalize,pluralize",A)},pluralize:function(A){return Ext.util.Inflector.pluralize(A)},readerRoot:function(A){return this.apply("uncapitalize",A)},singularize:function(A){return Ext.util.Inflector.singularize(A)},storeName:function(A){return this.apply("underscore",A)},uncapitalize:function(A){return Ext.String.uncapitalize(A)},underscore:function(A){return"_"+A},uniRole:function(A){return this.apply("undotted,uncapitalize,singularize",A)},undotted:function(B){if(B.indexOf(".")<0){return B}var C=B.split("."),A=C.length;while(A-->1){C[A]=this.apply("capitalize",C[A])}return C.join("")},getterName:function(B){var A=B.role;if(B&&B.isMany){return A}return"get"+this.apply("capitalize",A)},inverseFieldRole:function(G,H,E,B){var F=this,A=F.apply(H?"uniRole":"multiRole",G),D=F.apply("pluralize",E),C=F.apply("undotted,pluralize",B);if(D.toLowerCase()!==C.toLowerCase()){A=E+F.apply("capitalize",A)}return A},manyToMany:function(E,D,A){var C=this,B=C.apply("undotted,capitalize,singularize",D)+C.apply("undotted,capitalize,pluralize",A);if(E){B=C.apply("capitalize",E+B)}return B},manyToOne:function(D,B,A,C){return this.apply("capitalize,singularize",A)+this.apply("capitalize",B)},matrixRole:function(C,B){var A=this.apply(C?"multiRole,capitalize":"multiRole",B);return C?C+A:A},oneToOne:function(D,B,A,C){return this.apply("undotted,capitalize,singularize",A)+this.apply("capitalize",B)},setterName:function(A){return"set"+this.apply("capitalize",A.role)},endsWithIdRe:/(?:(_id)|[^A-Z](Id))$/,cache:{},apply:function(E,C){var H=this,B=H.cache,I=B[C]||(B[C]={}),G=I[E],F,D,A;if(!G){if(E.indexOf(",")<0){G=H[E](C)}else{D=(A=E.split(",")).length;G=C;for(F=0;F<D;++F){G=H.apply(A[F],G)}}I[E]=G}return G}})