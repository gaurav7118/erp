SenchaTestRunner.bindings={setCurrentScript:function(A){jasmine.setCurrentScript(A)},waitUntilPageIsReady:function(B){var A=arguments;if(typeof Ext!=="undefined"){Ext.require("*");Ext.onReady(function(){window.__pageIsReady=true})}else{window.__pageIsReady=true}},startTestRunner:function(B,A){addGlobal("__pageIsReady");addGlobal("__injectionDone");jasmine.setOptions(B);jasmine.initDebug();jasmine.getEnv().addReporter(new SenchaTestRunner.Reporter());jasmine.getEnv().execute()},testsAreRunning:function(){return SenchaTestRunner.isRunning()},getTestResultsAsJson:function(){return JSON.stringify(SenchaTestRunner.results)}}