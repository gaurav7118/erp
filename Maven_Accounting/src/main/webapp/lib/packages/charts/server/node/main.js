var http=require("http");var qs=require("querystring");var fs=require("fs");var spawn=require("child_process").spawn;var helpers=require("./helpers.js");var v1=require("./converters/v1.js");var v2=require("./converters/v2.js");var MAX_FILE_SIZE=5*1024*1024;var TMP_DIR_NAME=process.cwd()+"/tmp/";var SCRIPT=fs.readFileSync("./save_script_tpl.js",{encoding:"utf8"});if(!fs.existsSync(TMP_DIR_NAME)){fs.mkdirSync(TMP_DIR_NAME)}var defaultHeaders={"Content-Type":"text/plain","Access-Control-Allow-Origin":"*"};function respond(A,B,C,D){D=helpers.apply({},D,defaultHeaders);A.writeHead(B,D);A.end(C)}var counter=(function(){var B=0,A=Math.pow(2,32)-1;return function(){if(B>A){B=0}return B++}})();http.createServer(function(C,B){if(C.method==="POST"){var A="";C.on("data",function(D){if(A.length<=MAX_FILE_SIZE){A+=D}else{respond(B,413,"Request entity too large.")}});C.on("end",function(){try{var G=qs.parse(A)}catch(I){console.error("Parsing request data failed.",I)}if(G){switch(G.version){case"2":G=v2.convert(G);break;default:G=v1.convert(G);break}}if(!G||!G.data){respond(B,400,"Bad request.");return }var D=(G.filename||"chart")+"."+G.format;var H=TMP_DIR_NAME+counter().toString()+"."+G.format;var F=TMP_DIR_NAME+counter().toString()+".js";var E=helpers.interpolate(SCRIPT,helpers.apply(G,{filename:H}));fs.writeFile(F,E,{encoding:"utf8"},function(K){if(K){throw K}var J=spawn("phantomjs",[F]);J.stdout.pipe(process.stdout);J.on("exit",function(L){fs.unlink(F,function(){if(K){throw K}console.log("Successfully deleted:",F)});if(!L){fs.readFile(H,function(M,N){if(M){throw M}respond(B,200,N,{"Content-Type":G.contentType,"Content-Disposition":"attachment; filename="+D});fs.unlink(H,function(){if(M){throw M}console.log("Successfully deleted:",H)})})}else{respond(B,500,"Internal server error.\nphantomjs exited with code "+L)}})})})}else{respond(B,400,"Bad request.")}}).listen(1337,"0.0.0.0")