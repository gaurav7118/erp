Ext.onReady(function(){if(Ext.Date){Ext.Date.monthNames=["Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"];Ext.Date.getShortMonthName=function(A){return Ext.Date.monthNames[A].substring(0,3)};Ext.Date.monthNumbers={Jan:0,Fev:1,Mar:2,Abr:3,Mai:4,Jun:5,Jul:6,Ago:7,Set:8,Out:9,Nov:10,Dez:11};Ext.Date.getMonthNumber=function(A){return Ext.Date.monthNumbers[A.substring(0,1).toUpperCase()+A.substring(1,3).toLowerCase()]};Ext.Date.dayNames=["Domingo","Segunda","Terça","Quarta","Quinta","Sexta","Sábado"]}if(Ext.util&&Ext.util.Format){Ext.apply(Ext.util.Format,{thousandSeparator:".",decimalSeparator:",",currencySign:"R$",dateFormat:"d/m/Y"});Ext.util.Format.brMoney=Ext.util.Format.currency}})