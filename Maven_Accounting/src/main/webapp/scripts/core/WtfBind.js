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

dojo.cometd = new function(){
	
	// cometd states:
 	this.DISCONNECTED="DISCONNECTED";	// _initialized==false 	&& _connected==false
 	this.CONNECTING="CONNECTING";		// _initialized==true	&& _connected==false (handshake sent)
 	this.CONNECTED="CONNECTED";		// _initialized==true	&& _connected==true (first successful connect)
 	this.DISCONNECTING="DISCONNECING";	// _initialized==false 	&& _connected==true (disconnect sent)
 	
	this._initialized = false;
	this._connected = false;
	this._polling = false;

	this.connectionTypes = new dojo.AdapterRegistry(true);

	this.version="1.0";
	this.minimumVersion="0.9";
	this.clientId=null;
	this.messageId=0;
	this.batch=0;

	this._isXD = false;
	this.handshakeReturn=null;
	this.currentTransport=null;
	this.url = null;
	this.lastMessage=null;
	this._messageQ=[];
	this.handleAs="json-comment-optional";
	this._advice={};
	this._maxInterval=30000;
	this._backoffInterval=1000;
	this._deferredSubscribes={};
	this._deferredUnsubscribes={};
	this._subscriptions=[];
	this._extendInList=[];	// List of functions invoked before delivering messages
	this._extendOutList=[];	// List of functions invoked before sending messages

	this.state = function() {
		return this._initialized?(this._connected?this.CONNECTED:this.CONNECTING):(this._connected?this.DISCONNECTING:this.DISCONNECTED);
	}

	this.init = function(	/*String*/	root,
				/*Object|null */ props,
				/*Object|null */ bargs){	// return: dojo.Deferred
		//	summary:
		//		Initialize the cometd implementation of the Bayeux protocol
		//	description:
		//		Initialize the cometd implementation of the Bayeux protocol by
		//		sending a handshake message. The cometd state will be changed to CONNECTING
		//		until a handshake response is received and the first successful connect message
		//		has returned.
		//		The protocol state changes may be monitored
		//		by subscribing to the dojo topic "/cometd/meta" where events are
		//		published in the form {cometd:this,action:"handshake",successful:true,state:this.state()}
		//	root:
		//		The URL of the cometd server. If the root is absolute, the host
		//		is examined to determine if xd transport is needed. Otherwise the
		//		same domain is assumed.
		//	props:
		//		An optional object that is used as the basis of the handshake message
		//	bargs:
		//		An optional object of bind args mixed in with the send of the handshake
		//	example:
		//	|	dojo.cometd.init("/cometd");
		//	|	dojo.cometd.init("http://xdHost/cometd",{ext:{user:"fred",pwd:"secret"}});


		// FIXME: if the root isn't from the same host, we should automatically
		// try to select an XD-capable transport
		props = props||{};
		// go ask the short bus server what we can support
		props.version = this.version;
		props.minimumVersion = this.minimumVersion;
		props.channel = "/meta/handshake";
		props.id = ""+this.messageId++;

		this.url = root;
		if(!this.url){
			console.debug("no cometd root passed");
			return null;
		}

		// Are we x-domain? borrowed from dojo.uri.Uri in lieu of fixed host and port properties
		var regexp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
		var parts = (""+window.location).match(new RegExp(regexp));
		if(parts[4]){
			var tmp = parts[4].split(":");
			var thisHost = tmp[0];
			var thisPort = tmp[1]||"80"; // FIXME: match 443

			parts = this.url.match(new RegExp(regexp));
			if(parts[4]){
				tmp = parts[4].split(":");
				var urlHost = tmp[0];
				var urlPort = tmp[1]||"80";
				this._isXD = ((urlHost != thisHost)||(urlPort != thisPort));
			}
		}

		if(!this._isXD){
			if(props.ext){
				if(props.ext["json-comment-filtered"]!==true && props.ext["json-comment-filtered"]!==false){
					props.ext["json-comment-filtered"] = true;
				}
			}else{
				props.ext = { "json-comment-filtered": true };
			}
		}

		props=this._extendOut(props);

		var bindArgs = {
			url: this.url,
			handleAs: this.handleAs,
			content: { "message": Wtf.encode([props]) },
			load: dojo.hitch(this,function(msg){
				this._finishInit(msg);
			}),
			error: dojo.hitch(this,function(e){
				console.debug("handshake error!:",e);
				this._finishInit([{}]);
			})
		};

		if(bargs){
			Wtf.apply(bindArgs, bargs);
		}
		this._props=props;
		for(var tname in this._subscriptions){
			for(var sub in this._subscriptions[tname]){
				if(this._subscriptions[tname][sub].topic){
		 			dojo.unsubscribe(this._subscriptions[tname][sub].topic);
				}
			}
		}
		this._messageQ = [];
		this._subscriptions = [];
		this._initialized=true;
		this.batch=0;
		this.startBatch();
		
		var r;
		// if xdomain, then we assume jsonp for handshake
		if(this._isXD){
			bindArgs.callbackParamName="jsonp";
			r= dojo.script.get(bindArgs);
		} else
			r = dojo.xhrPost(bindArgs);
		dojo.publish("/cometd/meta", [{cometd:this,action:"handshake",successful:true,state:this.state()}]);
		return r;
	}
	
	
	this.publish = function(/*String*/channel, /*Object */data, /*Object|null */properties){
		// summary:
		//		publishes the passed message to the cometd server for delivery
		//		on the specified topic
		// channel:
		//		the destination channel for the message
		// data:
		//		a JSON object containing the message "payload"
		// properties:
		//		Optional. Other meta-data to be mixed into the top-level of the
		//		message
		var message = {
			data: data,
			channel: channel
		};
		if(properties){
			Wtf.apply(message, properties);
		}
		this._sendMessage(message);
	}

	
	this.subscribe = function(	/*String */	channel,
					/*Object */	objOrFunc,
					/*String */	funcName){ // return: dojo.Deferred
		// summary:
		//		inform the server of this client's interest in channel
		// channel:
		//		name of the cometd channel to subscribe to
		// objOrFunc:
		//		an object scope for funcName or the name or reference to a
		//		function to be called when messages are delivered to the
		//		channel
		// funcName:
		//		the second half of the objOrFunc/funcName pair for identifying
		//		a callback function to notifiy upon channel message delivery

		if(objOrFunc){
			var tname = "/cometd"+channel;
			var subs=this._subscriptions[tname];
			if(!subs || subs.length==0){
				subs=[];
				this._sendMessage({
					channel: "/meta/subscribe",
					subscription: channel
				});
				
				var _ds = this._deferredSubscribes;
				_ds[channel] = new dojo.Deferred();
				if(_ds[channel]){
					_ds[channel].cancel();
					delete _ds[channel];
				}
			}
			
			for (var i in subs){
				if (subs[i].objOrFunc===objOrFunc&&(!subs[i].funcName&&!funcName||subs[i].funcName==funcName))
					return null;
			}
			
			var topic = dojo.subscribe(tname, objOrFunc, funcName);
			subs.push({ 
				topic: topic, 
				objOrFunc: objOrFunc, 
				funcName: funcName
			});
			this._subscriptions[tname] =subs;
		}
		return this._deferredSubscribes[channel];
	}



	this.unsubscribe = function(	/*string*/	channel,
					/*object|null*/ objOrFunc,
					/*string|null*/ funcName){
		// summary:
		//		inform the server of this client's disinterest in channel
		// channel:
		//		name of the cometd channel to unsubscribe from
		// objOrFunc:
		//		an object scope for funcName or the name or reference to a
		//		function to be called when messages are delivered to the
		//		channel. If null then all subscribers to the channel are unsubscribed.
		// funcName:
		//		the second half of the objOrFunc/funcName pair for identifying
		//		a callback function to notifiy upon channel message delivery
		
		var tname = "/cometd"+channel;
		var subs=this._subscriptions[tname];
		if(!subs || subs.length==0){
			return null;
		}

		var s=0;
		for(var i in subs){
			var sb=subs[i];
			if( (!objOrFunc) ||
				(
					sb.objOrFunc===objOrFunc &&
					(!sb.funcName && !funcName || sb.funcName==funcName)
				)
			){
				dojo.unsubscribe(subs[i].topic);
				delete subs[i];
			}else{
				s++;
			}
		}
		
		if(s==0){
			delete this._subscriptions[tname];
			this._sendMessage({
				channel: "/meta/unsubscribe",
				subscription: channel
			});
			this._deferredUnsubscribes[channel] = new dojo.Deferred();
			if (this._deferredSubscribes[channel]){
				this._deferredSubscribes[channel].cancel();
				delete this._deferredSubscribes[channel];
			}
		}
		return this._deferredUnsubscribes[channel];
	}
	
	
	this.disconnect = function(){
		//	summary:
		//		Disconnect from the server.
		//	description:
		//		Disconnect from the server by sending a disconnect message
		//	example:
		//	|	dojo.cometd.disconnect();

		for(var tname in this._subscriptions){
			for(var sub in this._subscriptions[tname]){
				if(this._subscriptions[tname][sub].topic){
					dojo.unsubscribe(this._subscriptions[tname][sub].topic);
				}
			}
		}
		this._subscriptions = [];
		this._messageQ = [];
		if(this._initialized && this.currentTransport){
			this._initialized=false;
			this.currentTransport.disconnect();
		}
		if(!this._polling) {
			this._connected=false;
			dojo.publish("/cometd/meta", [{cometd:this,action:"connect",successful:false,state:this.state()}]);
		}
		this._initialized=false;
		dojo.publish("/cometd/meta", [{cometd:this,action:"disconnect",successful:true,state:this.state()}]);
	}

	
	// public extension points
	
	this.subscribed = function(	/*String*/channel, /*Object*/message){ }

	this.unsubscribed = function(/*String*/channel, /*Object*/message){ }


	// private methods (TODO name all with leading _)

	this.tunnelInit = function(childLocation, childDomain){
		// placeholder - replaced by _finishInit
	}
	
	this.tunnelCollapse = function(){
		// placeholder - replaced by _finishInit
	}
	
	this._backoff = function(){
		if(!this._advice || !this._advice.interval){
			this._advice={reconnect:"retry",interval:0}; // TODO Is this good advice?
		}
		if(this._advice.interval<this._maxInterval){
			this._advice.interval+=this._backoffInterval;
		}
	}

	this._finishInit = function(data){
		//	summary:
		//		Handle the handshake return from the server and initialize
		//		connection if all is OK
		data = data[0];
		this.handshakeReturn = data;
		
		// remember any advice
		if(data["advice"]){
			this._advice = data.advice;
		}

		var successful=data.successful?data.successful:false;
		
		// check version
		if(data.version < this.minimumVersion){
			console.debug("cometd protocol version mismatch. We wanted", this.minimumVersion, "but got", data.version);
			successful=false;
			this._advice.reconnect="none";
		}
		
		// If all OK
		if(successful){
			// pick a transport
			this.currentTransport = this.connectionTypes.match(
				data.supportedConnectionTypes,
				data.version,
				this._isXD
			);
			// initialize the transport
			this.currentTransport._cometd = this;
			this.currentTransport.version = data.version;
			this.clientId = data.clientId;
			this.tunnelInit = dojo.hitch(this.currentTransport, "tunnelInit");
			this.tunnelCollapse = dojo.hitch(this.currentTransport, "tunnelCollapse");
			this.currentTransport.startup(data);
		}

		dojo.publish("/cometd/meta", [{cometd:this,action:"handshook",successful:successful,state:this.state()}]);

		// If there is a problem
		if(!successful){
			console.debug("cometd init failed");
			this._backoff();
			// follow advice
			if(this._advice && this._advice["reconnect"]=="none"){
				console.debug("cometd reconnect: none");
			}else if(this._advice && this._advice["interval"] && this._advice.interval>0 ){
				setTimeout(
					dojo.hitch(this, function(){ this.init(this.url,this._props); }),
					this._advice.interval
				);
			}else{
				this.init(this.url,this._props);
			}
		}
	}

	this._extendIn = function(message){
		// Handle extensions for inbound messages
		var m=message;
		Wtf.each(dojo.cometd._extendInList, function(f){
			var n=f(m);
			if(n)m=n;
		});
		return m;
	}

	this._extendOut= function(message){
		// Handle extensions for inbound messages
		var m=message;
		Wtf.each(dojo.cometd._extendOutList, function(f){
			var n=f(m);
			if(n)m=n;
		});
		return m;
	}


	this.deliver = function(messages){
		Wtf.each(messages, this._deliver, this);
		return messages;
	}

	this._deliver = function(message){
		// dipatch events along the specified path
		
		message=this._extendIn(message);

		if(!message["channel"]){
			if(message["success"] !== true){
				console.debug("cometd error: no channel for message!", message);
				return;
			}
		}
		this.lastMessage = message;

		if(message.advice){
			this._advice = message.advice; // TODO maybe merge?
		}

		// check to see if we got a /meta channel message that we care about
		var deferred=null;
		if(	(message["channel"]) &&
			(message.channel.length > 5)&&
			(message.channel.substr(0, 5) == "/meta")){
			// check for various meta topic actions that we need to respond to
			switch(message.channel){
				case "/meta/connect":
					if(message.successful && !this._connected){
						this._connected = this._initialized;
						this.endBatch();
					} else if(!this._initialized){
						this._connected = false; // finish disconnect
					}
					dojo.publish("/cometd/meta",[{cometd:this,action:"connect",successful:message.successful,state:this.state()}]);
					break;
				case "/meta/subscribe":
					deferred = this._deferredSubscribes[message.subscription];
					if(!message.successful){
						if(deferred){
							deferred.errback(new Error(message.error));
						}
						return;
					}
					dojo.cometd.subscribed(message.subscription, message);
					if(deferred){
						deferred.callback(true);
					}
					break;
				case "/meta/unsubscribe":
					deferred = this._deferredUnsubscribes[message.subscription];
					if(!message.successful){
						if(deferred){
							deferred.errback(new Error(message.error));
						}
						return;
					}
					this.unsubscribed(message.subscription, message);
					if(deferred){
						deferred.callback(true);
					}
					break;
			}
		}
		
		// send the message down for processing by the transport
		this.currentTransport.deliver(message);

		if(message.data){
			// dispatch the message to any locally subscribed listeners
			try {
				var tname = "/cometd"+message.channel;
				dojo.publish(tname, [ message ]);
			}catch(e){
				console.debug(e);
			}
		}
	}

	this._sendMessage = function(/* object */ message){
		if(this.currentTransport && this._connected && this.batch==0){
			return this.currentTransport.sendMessages([message]);
		}
		else{
			this._messageQ.push(message);
			return null;
		}
	}

	this.startBatch = function(){
		this.batch++;
	}

	this.endBatch = function(){
		if(--this.batch <= 0 && this.currentTransport && this._connected){
			this.batch=0;

			var messages=this._messageQ;
			this._messageQ=[];
			if(messages.length>0){
				this.currentTransport.sendMessages(messages);
			}
		}
	}
	
	this._onUnload = function(){
		// make this the last of the onUnload method
		dojo.addOnUnload(dojo.cometd,"disconnect");
	}
}

/*
transport objects MUST expose the following methods:
	- check
	- startup
	- sendMessages
	- deliver
	- disconnect
optional, standard but transport dependent methods are:
	- tunnelCollapse
	- tunnelInit

Transports SHOULD be namespaced under the cometd object and transports MUST
register themselves with cometd.connectionTypes

here's a stub transport defintion:

cometd.blahTransport = new function(){
	this._connectionType="my-polling";
	this._cometd=null;
	this.lastTimestamp = null;

	this.check = function(types, version, xdomain){
		// summary:
		//		determines whether or not this transport is suitable given a
		//		list of transport types that the server supports
		return dojo.lang.inArray(types, "blah");
	}

	this.startup = function(){
		if(dojo.cometd._polling){ return; }
		// FIXME: fill in startup routine here
		dojo.cometd._polling = true;
	}

	this.sendMessages = function(message){
		// FIXME: fill in message array sending logic
	}

	this.deliver = function(message){
	}

	this.disconnect = function(){
	}
}
cometd.connectionTypes.register("blah", cometd.blahTransport.check, cometd.blahTransport);
*/

dojo.cometd.longPollTransport = new function(){
	this._connectionType="long-polling";
	this._cometd=null;

	this.check = function(types, version, xdomain){
		return ((!xdomain)&&(types.indexOf("long-polling") >= 0));
	}

	this.tunnelInit = function(){
		var message = {
			channel:	"/meta/connect",
			clientId:	this._cometd.clientId,
			connectionType: this._connectionType,
			id:	""+this._cometd.messageId++
		};
		message=this._cometd._extendOut(message);
		this.openTunnelWith({message: Wtf.encode([message])});
	}

	this.tunnelCollapse = function(){
		// TODO handle transport specific advice
		
		if(!this._cometd._initialized){ return; }
			
		if(this._cometd._advice){
			if(this._cometd._advice["reconnect"]=="none"){
				return;
			}
			if(	(this._cometd._advice["interval"])&&
				(this._cometd._advice.interval>0) ){
				setTimeout(dojo.hitch(this,function(){ this._connect(); }),this._cometd._advice.interval);
			}else{
				this._connect();
			}
		}else{
			this._connect();
		}
	}

	this._connect = function(){
		if(!this._cometd._initialized){ return; }
		if(this._cometd._polling) {
			console.debug("wait for poll to complete or fail");
			return;
		}
			
		if(	(this._cometd._advice) &&
			(this._cometd._advice["reconnect"]=="handshake")
		){
			this._cometd._connected=false;
			this._initialized = false;
			this._cometd.init(this._cometd.url,this._cometd._props);
 		}else if(this._cometd._connected){
			var message={
				channel:	"/meta/connect",
				connectionType: this._connectionType,
				clientId:	this._cometd.clientId,
				id:	""+this._cometd.messageId++
			};
			message=this._cometd._extendOut(message);
			this.openTunnelWith({message: Wtf.encode([message])});
		}
	}

	this.deliver = function(message){
		// Nothing to do
	}

	this.openTunnelWith = function(content, url){
		this._cometd._polling = true;
		var d = dojo.xhrPost({
			url: (url||this._cometd.url),
			content: content,
			handleAs: this._cometd.handleAs,
			load: dojo.hitch(this, function(data){
				this._cometd._polling = false;
				this._cometd.deliver(data);
				this.tunnelCollapse();
			}),
			error: dojo.hitch(this, function(err){
				this._cometd._polling = false;
				console.debug("tunnel opening failed:", err);
				dojo.publish("/cometd/meta", [{cometd:this._cometd,action:"connect",successful:false,state:this._cometd.state()}]);
				this._cometd._backoff();
				this.tunnelCollapse();
			})
		});
	}

	this.sendMessages = function(messages){
		for(var i=0; i<messages.length; i++){
			messages[i].clientId = this._cometd.clientId;
			messages[i].id = ""+this._cometd.messageId++;
			messages[i]=this._cometd._extendOut(messages[i]);
		}
		return dojo.xhrPost({
			url: this._cometd.url,
			handleAs: this._cometd.handleAs,
			load: dojo.hitch(this._cometd, "deliver"),
			content: {
				message: Wtf.encode(messages)
			}
		});
	}

	this.startup = function(handshakeData){
		if(this._cometd._connected){ return; }
		this.tunnelInit();
	}

	this.disconnect = function(){
		var message={
			channel:	"/meta/disconnect",
			clientId:	this._cometd.clientId,
			id:	""+this._cometd.messageId++
		};
		message=this._cometd._extendOut(message);
		dojo.xhrPost({
			url: this._cometd.url,
			handleAs: this._cometd.handleAs,
			content: {
				message: Wtf.encode([message])
			}
		});
	}
}

dojo.cometd.callbackPollTransport = new function(){
	this._connectionType = "callback-polling";
	this._cometd = null;

	this.check = function(types, version, xdomain){
		// we handle x-domain!
		return (types.indexOf("callback-polling") >= 0);
	}

	this.tunnelInit = function(){
		var message = {
			channel:	"/meta/connect",
			clientId:	this._cometd.clientId,
			connectionType: this._connectionType,
			id:	""+this._cometd.messageId++
		};
		message = this._cometd._extendOut(message);		
		this.openTunnelWith({
			message: Wtf.encode([message])
		});
	}

	this.tunnelCollapse = dojo.cometd.longPollTransport.tunnelCollapse;
	this._connect = dojo.cometd.longPollTransport._connect;
	this.deliver = dojo.cometd.longPollTransport.deliver;

	this.openTunnelWith = function(content, url){
		this._cometd._polling = true;
		dojo.script.get({
			load: dojo.hitch(this, function(data){
				this._cometd._polling = false;
				this._cometd.deliver(data);
				this.tunnelCollapse();
			}),
			error: dojo.hitch(this, function(err){
				this._cometd._polling = false;
				console.debug("tunnel opening failed:", err);
				dojo.publish("/cometd/meta", [{cometd:this._cometd,action:"connect",successful:false,state:this._cometd.state()}]);
				this._cometd._backoff();
				this.tunnelCollapse();
			}),
			url: (url||this._cometd.url),
			content: content,
			callbackParamName: "jsonp"
		});
	}

	this.sendMessages = function(/*array*/ messages){
		for(var i=0; i<messages.length; i++){
			messages[i].clientId = this._cometd.clientId;
			messages[i].id = ""+this._cometd.messageId++;
			messages[i]=this._cometd._extendOut(messages[i]);
		}
		var bindArgs = {
			url: this._cometd.url,
			load: dojo.hitch(this._cometd, "deliver"),
			callbackParamName: "jsonp",
			content: { message: Wtf.encode( messages ) }
		};
		return dojo.script.get(bindArgs);
	}

	this.startup = function(handshakeData){
		if(this._cometd._connected){ return; }
		this.tunnelInit();
	}

	this.disconnect = dojo.cometd.longPollTransport.disconnect;
	
	this.disconnect = function(){
		var message={
			channel:"/meta/disconnect",
			clientId:this._cometd.clientId,
			id:""+this._cometd.messageId++
		};
		message=this._cometd._extendOut(message);		
		dojo.script.get({
			url: this._cometd.url,
			callbackParamName: "jsonp",
			content: {
				message: Wtf.encode([message])
			}
		});
	}
}
dojo.cometd.connectionTypes.register("long-polling", dojo.cometd.longPollTransport.check, dojo.cometd.longPollTransport);
dojo.cometd.connectionTypes.register("callback-polling", dojo.cometd.callbackPollTransport.check, dojo.cometd.callbackPollTransport);

dojo.addOnUnload(dojo.cometd,"_onUnload");
