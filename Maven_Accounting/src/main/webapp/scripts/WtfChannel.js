/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * The queue that will store all XMLHttpRequests
 */
Wtf.lib.Ajax._queue = [];

/**
 * Stores the number of XMLHttpRequests being processed
 */
Wtf.lib.Ajax._activeRequests = 0;
Wtf.lib.Ajax.counter = 0;
Wtf.lib.Ajax.Queue_Size = 1;

/**
 * Overwritten so pending XMLHttpRequests in the queue will be removed
 */
Wtf.lib.Ajax.abort=function(o, callback, isTimeout)
{
    if (this.isCallInProgress(o)) {
        o.conn.abort();
        window.clearInterval(this.poll[o.tId]);
        delete this.poll[o.tId];
        if (isTimeout) {
            delete this.timeout[o.tId];
        }

        this.handleTransactionResponse(o, callback, true);

        return true;
    }
    else {

        // check if the connection is pending and delete it
        for (var i = 0, max_i = this._queue.length; i < max_i; i++) {
            if (this._queue[i].o.tId == o.tId) {
                this._queue.splice(i, 1);
                break;
            }
        }

        return false;
    }
};

/**
 * Pushes the XMLHttpRequests into the queue and processes the queue afterwards.
 *
 */
Wtf.lib.Ajax.asyncRequest = function(method, uri, callback, postData)
{
    var o = this.getConnectionObject();

    if (!o) {
        return null;
    }
    else {

        this._queue.push({
           o : o,
           method: method,
           uri: uri,
           callback: callback,
           postData : postData
        });

        this._processQueue();

        return o;
    }
};

/**
 * Peeks into the queue and will process the first XMLHttpRequest found, if, and only if
 * there are not more than 2 simultaneously XMLHttpRequests already processing.
 */

Wtf.lib.Ajax.combinereq = function(){
    if(this.task!=null&&this.task!=undefined){
        this.task.cancel();
    }
    //alert(this._queue.length);
    var param = [];
    var newparam = [];
    var ele;
    while(this._queue.length>0){
        ele = this._queue.shift();
        if(ele.uri=="General/getData.do"){
            newparam.push(ele);
            ele = null;
            continue
        }
        var uriobj = ele.uri.split('?');
        if(uriobj.length>1){
            ele.uri = uriobj[0];
                ele.postData += "&"+uriobj[1];
            ele.callback.argument.options.params = Wtf.urlDecode(ele.postData,true);
        }
        var dotindex = ele.uri.lastIndexOf(".");
        var ext = ele.uri.substring(dotindex+1);
        if(ext=="jsp"){
            newparam.push(ele);
            ele = null;
            continue
        } 
        //if(to.callback.argument.options.params.grouper==this._queue[i].callback.argument.options.params.grouper){
        var subparam = "{url:'"+ele.uri+"',params:"+Wtf.encode(ele.callback.argument.options.params)+",no:"+this.counter+"}";
        callbackmap[this.counter]=ele.callback;

        this.counter++;
        param.push(subparam);
        //}
    }
    this._queue = newparam;
    if(param.length>0){
        if(ele){
            if(this._activeRequests < this.Queue_Size){
                this._asyncRequest(ele.o, ele.method, "General/getData.do", ele.callback, Wtf.urlEncode(param));
            }else{
                this._queue.push({
                   o : ele.o,
                   method: ele.method,
                   uri: "General/getData.do",
                   callback: ele.callback,
                   postData : Wtf.urlEncode(param)
                });
            }
        }
    }
}


Wtf.lib.Ajax.simplerequest = function(){
    if(this._activeRequests < this.Queue_Size){
        var to = this._queue.shift();
        this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData);
    }
}

Wtf.lib.Ajax._processQueue = function(flag)
{
    var to;
    if(flag==1){
        to = this._queue[0];
        if(to&&to.uri=="General/getData.do"){
            this.simplerequest();
            return;
        }
    }else{
        to = this._queue[this._queue.length-1];
    }
        var combineData=1;//Send -1 if you don't want to include request in getData.do
        var sendgroup=-1;
        var send = -1
        var common = -1
        if (to){
            if(to.postData!=undefined&&to.postData!=null){
               sendgroup=to.postData.search("firequery");
               send = to.postData.search("grouper");
               common = to.postData.search("common");
               combineData = to.postData.search("combineData");
            }

            if(combineData==-1) {
                this._queue.pop();
                this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData);
                flag=1;
            } else if(flag!=1&&sendgroup>-1){
                this.combinereq();
            }else if(send==-1&&common==-1){
                if(this._activeRequests < this.Queue_Size){
//                    var dotindex = to.uri.lastIndexOf(".");
//                    var ext = to.uri.substring(dotindex+1);
//                    if(this._queue.length>1&&ext.search("jsp")==-1){
//                        this.combinereq();
//                    }else{
                        if(flag==1){
                            to = this._queue.shift();
                        }else{
                            to = this._queue.pop();
                        }
                        this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData);
                    //}
                }
            }
            if(flag!=1&&(common!=-1||send!=-1)){
                if(this.task==null||this.task==undefined){
                    this.task = new Wtf.util.DelayedTask(this.combinereq,this);
                }
                this.task.delay(1);
            }
        }
};

//Wtf.lib.Ajax._processQueue = function()
//{
//    var to = this._queue[0];
//
//    if (to && this._activeRequests < 1) {
//        to = this._queue.shift();
//        this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData);
//    }
//
//};

var callbackmap = [];

/**
 * Executes a XMLHttpRequest and updates the _activeRequests property to match the
 * number of concurrent ajax calls.
 */
Wtf.lib.Ajax._asyncRequest = function(o, method, uri, callback, postData)
{
    this._activeRequests++;
    o.conn.open(method, uri, true);

    if (this.useDefaultXhrHeader) {
        if (!this.defaultHeaders['X-Requested-With']) {
            this.initHeader('X-Requested-With', this.defaultXhrHeader, true);
        }
    }

    if(postData && this.useDefaultHeader){
        this.initHeader('Content-Type', this.defaultPostHeader);
    }

     if (this.hasDefaultHeaders || this.hasHeaders) {
        this.setHeader(o);
    }

    this.handleReadyState(o, callback);
    o.conn.send(postData || null);

};

/**
 * Called after a XMLHttpRequest finishes. Updates the number of ongoing ajax calls
 * and checks afterwards if there are still requests pending.
 */
Wtf.lib.Ajax.releaseObject = function(o)
{
    o.conn = null;
    o = null;

    this._activeRequests--;
    this._processQueue(1);
};

Wtf.Ajax.requestEx = function(config, scope, successCallback, failureCallback){
    Wtf.Ajax.request({
        method: "POST",
        url: config.url,
        scope: scope,
        params: config.params,
        success: function(request, response){
            var res = null;
            try{
                var restext = request.responseText.trim();
                if(restext && (restext.length > 0)){
                    res = eval( '(' + restext + ')');
                    if(res && res.valid){
                        try{
                            if(successCallback){
                                successCallback.call(this, res.data, response);
                            }
                        } catch (e){
                            clog(e);
                        }
                    }
                    else if(res && (res.valid == false)){
                        if (res.data && res.data.reason) {
                            signOut(res.data.reason)
                        } else {
                            signOut("timeout");
                        }
                    }
                }
            } catch (e){
                clog(e);
                if(failureCallback)
                    failureCallback.call(this, request, response);
            }
        },
        failure: function(request, response){
            if(failureCallback)
                failureCallback.call(this, request, response);
        }
    });
}

function clog(e){
	if(window.console != undefined) {
	    if(console && console.debug && e){
	        console.debug(e.toString());
	    }
	}
}
