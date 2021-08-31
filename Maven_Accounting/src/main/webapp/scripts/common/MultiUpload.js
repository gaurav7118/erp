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
 * Author : Malhari Pawar
*/
Wtf.FileBrowseButton = Wtf.extend(Wtf.Button,{
	input_name : 'file',
	input_file : null,
	original_handler : null,
	original_scope : null,
	
	initComponent : function() {
		Wtf.FileBrowseButton.superclass.initComponent.call(this);
		this.original_handler = this.handler || null;
		this.original_scope = this.scope || window;
		this.handler = null;
		this.scope = null;
	},

	onRender : function(ct, position) {
		Wtf.FileBrowseButton.superclass.onRender.call(this, ct, position);
		this.createInputFile();
	},

	createInputFile : function() {
		var button_container = this.el.child('.x-btn-center');
		button_container.position('relative');
		this.input_file = Wtf.DomHelper.append(button_container,{
			tag : 'input',
			type : 'file',
			size : 1,
			name : this.input_name|| Wtf.id(this.el),
			style : 'position: absolute; display: block; border: none; cursor: pointer'
		}, true);

		this.input_file.setOpacity(0.0);
		this.adjustInputFileBox();

		if (this.handleMouseEvents) {
			this.input_file.on('mouseover', this.onMouseOver, this);
			this.input_file.on('mousedown', this.onMouseDown, this);
		}

		if (this.tooltip) {
			if (typeof this.tooltip == 'object') {
				Wtf.QuickTips.register(Wtf.apply( {
					target : this.input_file
				}, this.tooltip));
			} else {
				this.input_file.dom[this.tooltipType] = this.tooltip;
			}
		}

		this.input_file.on('change', this.onInputFileChange, this);
		this.input_file.on('click', function(e) { e.stopPropagation();});
	},

	autoWidth : function() {
		Wtf.FileBrowseButton.superclass.autoWidth.call(this);
		this.adjustInputFileBox();
	},

	adjustInputFileBox : function() {
		var btn_cont, btn_box, inp_box, adj;

		if (this.el && this.input_file) {
			btn_cont = this.el.child('.x-btn-center');
			btn_box = btn_cont.getBox();
			this.input_file.setStyle('font-size',(btn_box.width * 0.5) + 'px');
			inp_box = this.input_file.getBox();
			adj = {	x : 3, y : 3};
			if (Wtf.isIE) {
				adj = {	x : -84,	y : 3 };//to adjust style for following function setLeft() in all ie
			}
			this.input_file.setLeft(btn_box.width - inp_box.width + adj.x + 'px');
			this.input_file.setTop(btn_box.height - inp_box.height + adj.y + 'px');
		}
	},

	detachInputFile : function(no_create) {
		var result = this.input_file;

		no_create = no_create || false;

		if (typeof this.tooltip == 'object') {
			Ext.QuickTips.unregister(this.input_file);
		} else {
			this.input_file.dom[this.tooltipType] = null;
		}
		this.input_file.removeAllListeners();
		this.input_file = null;

		if (!no_create) {
			this.createInputFile();
		}
		return result;
	},
	
	getInputFile : function() {
		return this.input_file;
	},

	disable : function() {
		Wtf.FileBrowseButton.superclass.disable.call(this);
		this.input_file.dom.disabled = true;
	},

	enable : function() {
		Wtf.FileBrowseButton.superclass.enable.call(this);
		this.input_file.dom.disabled = false;
	},

	destroy : function() {
		var input_file = this.detachInputFile(true);
		input_file.remove();
		input_file = null;
		Wtf.FileBrowseButton.superclass.destroy.call(this);
	},

	onInputFileChange : function() {
		if (this.original_handler) {
			this.original_handler.call(this.original_scope,	this);
		}
	}
});


Wtf.MultiFlieUploadPanel = Wtf.extend(Wtf.Panel, {
	autoUpload:false,
	initComponent:function(){
		this.uploading = false;
		this.initialQueuedCount = 0;
		Wtf.MultiFlieUploadPanel.superclass.initComponent.call(this);
		this.addEvents({
			'beforefileadd':true,
			'fileadd':true,
			'beforefileremove':true,
			'fileremove':true
		});
		
		this.fileRecord=Wtf.data.Record.create([
           {name: 'filename'},
           {name: 'id'},
           {name: 'state', type: 'int'},
           {name: 'note'},
           {name: 'input_element'},
           {name: 'params'},
           {name: 'docid'},
           {name: 'docRefId'}
        ]);

	    var store = new Wtf.data.Store({
	        proxy: new Wtf.data.MemoryProxy([]),
	        reader: new Wtf.data.JsonReader({}, this.fileRecord),
	        pruneModifiedRecords: true
	    });
            if((this.isFromOtherForm && this.docid != undefined && this.docid != "") || (this.fileStr !=undefined && this.fileStr != "")){
                var url = "";
                if(this.isFromOtherForm && this.docid != undefined && this.docid != ""){
                    //If call from other forms (like SI, PI, SO, PO, DO, GR) and opened in edit mode then get temporary and permanent saved documents
                    url = "ACCLoanCMN/getTemporaryAndPermanentSavedFiles.do";
                } else if(this.isDisbursement || (this.isFromOtherForm && (this.docid == undefined || this.docid == ""))){
                    //If creating new transaction or is disbursement then get only temporary saved documents
                    url = "ACCLoanCMN/getTemporarySavedFiles.do";
                } else{
                    url = "ExportPDF/getContractFiles.do";
                }
                Wtf.Ajax.request({
	        url : url,
	        params : {fileid:this.fileStr, docid:this.docid, copyInv:this.copyInv, companyid:companyid},//docid - invoiceid
	        success : function(res,req){
                  var result = eval('('+res.responseText+')');
                  if(result.data && result.data.data){
                      var records=result.data.data;
                      var store = this.grid_panel.getStore();
		
                      for(var i=0;i<records.length;i++){
                         store.add(new this.fileRecord( {
			state :3,
			filename : records[i].filename,
			id : records[i].id,
			note : WtfGlobal.getLocaleText("acc.field.UplodedSuccessfully"),
			docid : records[i].docid,
			docRefId : records[i].docRefId
                    })); 
                      }
                  }
	           
	   
                },
	        failure : function(res,req){
                    alert("")
                },
	        scope : this
	    });
            }
	    var cm = new Wtf.grid.ColumnModel( [ {
			header : WtfGlobal.getLocaleText("acc.importLog.fileName"),
			dataIndex : 'filename'
		},{
			header : WtfGlobal.getLocaleText("acc.GIRO.Status"),
			dataIndex : 'state',
			width:25,
			renderer : function(val) {
				var str="images/s.gif";
				switch(val){
					case 1: str = "images/inbox.png"; break;
					case 2: str = "images/loading.gif"; break;
					case 3: str = "images/check16.png"; break;
					case 4: str = "images/exclamation.gif"; break;
					case 5: str = "images/Cancel.gif"; break;
				}
	        	return "<img src='"+str+"' style=\"margin-left:5px;vertical-align:middle;height : 13px\" title='Upload image'></img>";
	    	}
		},{
			header : WtfGlobal.getLocaleText("acc.field.Note"),
			dataIndex : 'note',
                        renderer : function(val,meta) {
                            meta.attr = 'wtf:qtip="' + val + '"';
                            return val;
                        }
		},{
                    header:WtfGlobal.getLocaleText("acc.field.Download"),  //"Download",
                    dataIndex:'download',
                    width:50,
                    align: 'center',
                    hidden:(this.isFromOtherForm) ? false : true,// hide column in Disbursement form
                    renderer:function(a,b,c,d,e,f){
                        if(c.data.state === 3){
                            var docid  = c.data.docid;
                            var name  = c.data.filename;
                            var ext="";
                            if (name.lastIndexOf(".") != -1) {
                                ext = name.substr(name.lastIndexOf("."));
                            }
                                return "<a href='javascript:void(0)' id='downloadlink' title='Download' onclick='openDldUrl(\"" + "../../fdownload.jsp?url="  + docid + ext + "&mailattch=true&dtype=attachment&docname="+name+"&moduleid="+this.moduleid+"\")'><div style = \"margin-left:30px; margin-right:30px\" class='pwnd downloadDoc' > </div></a>";
                            }
                        }
                },{
			header : WtfGlobal.getLocaleText("acc.templateeditor.remove"),
			width:25,
                        hidden: this.readOnly ? this.readOnly : false,// hide if view form
			dataIndex : 'delfield',
			renderer : function() {
                            return "<img class='delete' src='images/Delete.png' style='margin-left:5px;vertical-align:middle;height:13px;' title='Delete this entry'></img>";
                        }
		}]);
	    
        this.grid_panel = new Wtf.grid.GridPanel( {
			ds : store,
			cm : cm,
                        layout : 'fit',
			border : false,
			viewConfig : {
				autoFill : true,
				forceFit : true
			}
		});

        this.grid_panel.on('render', this.attachDrop, this);
        this.grid_panel.on('cellclick', function(g, r, c, e){
        	if(e.getTarget('.delete')){
                        if(g.getStore().getAt(r) != undefined && g.getStore().getAt(r).data.docid != undefined && g.getStore().getAt(r).data.docid != ""){
                                    /**
                                    *This method is used to remove Temporary or Permanent Document.
                                    *We are passing URL with docid.Docid is uuid of that document which we are deleting.
                                    */
                                   var obj ={};
                                   obj.url = "ACCInvoiceCMN/deleteAttachedDocument.do?docid="+g.getStore().getAt(r).data.docid+"&transactionid="+(!this.copyInv?this.docid:"")+"&docRefId="+((g.getStore().getAt(r).data.docRefId != undefined && g.getStore().getAt(r).data.docRefId != "")?g.getStore().getAt(r).data.docRefId:"");
                                   obj.parentObj=this; 
                                   deleteAttachDoc(obj);
                                   this.removeFile(g.getStore().getAt(r));
                        }else if(g.getStore().getAt(r) != undefined && g.getStore().getAt(r).data.state === 1){
                            /**
                             * Remove file who is in "Queued to Upload" state.
                             * state 1 == Queued to Upload.
                             * state 3 == Uploaded Successfully.
                             */
                            this.removeFile(g.getStore().getAt(r));
                        }
        	}
        }, this);
		this.add(this.grid_panel);
	},

	attachDrop:function(){
	    var dropEl = this.grid_panel.getEl();
        dropEl.on('dragover', function(e) {
            e.stopPropagation();
            e.preventDefault();
            var dt = e.browserEvent.dataTransfer;               
            dt.effectAllowed = 'copy';
 
            if (dt.effectAllowed.match(/all|copy/i)) {
                dt.dropEffect = 'copy';
            }
        }, this);
        
        dropEl.on('drop', function(e) {
            e.stopPropagation();
            e.preventDefault();

            //TODO prform Click handling as if browse button clicked 
            
            var dt = e.browserEvent.dataTransfer;
            var files = dt.files;
            
            //TODO add files from the filelist to the queue 
        }, this);
	},

	onRender : function(ct, position) {
		Wtf.MultiFlieUploadPanel.superclass.onRender.call(this, ct,	position);
	    this.form = Wtf.DomHelper.append(this.body, {
	        tag: 'form',
	        method: 'post',
	        action: this.url,
	        style: 'position: absolute; left: -100px; top: -100px; width: 100px; height: 100px'
	      });
              this.addEvents({
                'uploadComplete': true
             });

	},

	addFileToUploadQueue : function(btn) {
		var input_file = btn.detachInputFile();

		input_file.appendTo(this.form);
		input_file.setStyle('width', '100px');
		input_file.dom.disabled = true;

		var store = this.grid_panel.getStore();
		store.add(new this.fileRecord( {
			state :1,
			filename : input_file.dom.value,
			note : WtfGlobal.getLocaleText("acc.field.Queuedtoupload"),
			input_element : input_file
		}));
	},
	addFiles : function(btn) {
		if (this.fireEvent('beforefileadd', this, btn.getInputFile().dom.value) !== false) {
			this.addFileToUploadQueue(btn);
			this.fireEvent('fileadd', this, btn.getInputFile().dom.value);
			if(this.uploading===true){
				this.initialQueuedCount++;
			}else if(this.autoUpload){
				this.startUpload();
			}
		}
	},
	
	removeFile : function(record) {
		if (record&&this.fireEvent('beforefileremove', this, record) !== false) {
			if(record.get('input_element')){
                            record.get('input_element').remove();
                        }
			this.grid_panel.getStore().remove(record);
			this.fireEvent('fileremove', this, record);
		}
	},
	
	prepareNextUploadTask : function() {
		var store = this.grid_panel.getStore();
		var record = null;

		store.each(function(r) {
			if (!record && r.get('state') == 1) {
				record = r;
			} else {
                            if(r.get('input_element')!=undefined)
				r.get('input_element').dom.disabled = true;
			}
		});

		record.get('input_element').dom.disabled = false;
		record.set('state', 2);
		record.set('note', "Processing");
		record.commit();
		this.uploadFile(record);
	},

	getQueuedCount : function(includeProcessing) {
		var count = 0;
		this.grid_panel.getStore().each(function(r) {
			if (r.get('state') == 1||(includeProcessing && r.get('state') == 2)) {
				count++;
			}
		});
		return count;
	},
  
	startUpload:function(){
		if(this.getQueuedCount() > 0){
			this.uploading = true;
			this.initialQueuedCount = this.getQueuedCount();
			this.prepareNextUploadTask();
		}else{
			WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseadd/selectimage(s)first")]);
		}
	},
	
	cancelUpload:function(){
		var store = this.grid_panel.getStore();
		store.each(function(r) {
			if (r.get('state') == 1) {
				this.updateRecordState(r, 5, WtfGlobal.getLocaleText("acc.field.UploadCancelled"),"", "");
			}
		}, this);
	},
	
	clearAll : function() {
		var store = this.grid_panel.getStore();
		store.each(function(r) {
                    if(r.get('input_element')){
                        r.get('input_element').remove();
                    }
		}, this);
		store.removeAll();
	},
	
	uploadFile:function(record){
	    Wtf.Ajax.request({
	        url : this.url,
	        params : Wtf.applyIf(record.get('params') || {}, this.baseParams || this.params),
	        method : 'POST',
	        form : this.form,
	        isUpload : true,
	        success : this.onAjaxSuccess,
	        failure : this.onAjaxFailure,
	        scope : this,
	        record:record
	    });
	},
	
	updateRecordState:function(record, state, message, fileid, docid){
            record.set('state', state);
            record.set('note', message);
            record.set('id', fileid);
            record.set('docid', docid);
            record.commit();
	},
	
	onAjaxSuccess:function(resp, data){
            result = eval('('+resp.responseText+')');
            var state = 4, msg,fileid="", docid = "";
	    if (result.data.success==true) {
	    	state = 3;
	    	fileid = result.data.file;
	    	docid = result.data.docid;
	    	msg = WtfGlobal.getLocaleText("acc.field.UplodedSuccessfully");
                if(this.isDisbursement || this.isFromOtherForm){
                    this.savedFilesMappingId=result.data.savedFilesMappingId;
                    this.url='ACCLoanCMN/attachDocuments.do?type=doc&savedFilesMappingId='+this.savedFilesMappingId;
                    this.savedFilesId=fileid;
                    this.fireEvent("uploadComplete",this);
                }
	    }else{
	    	msg=result.data.msg||WtfGlobal.getLocaleText("acc.field.UploadFailed");
	    }
	    this.updateRecordState(data.record, state, msg, fileid, docid);
		if(this.getQueuedCount() > 0){
			this.prepareNextUploadTask();
		}else{
			this.uploading = false;
		}
	},
	
	onAjaxFailure:function(resp, data){
		this.updateRecordState(data.record, 4, WtfGlobal.getLocaleText("acc.field.CommunicationFailed"),"", "");
		if(this.getQueuedCount() > 0){
			this.prepareNextUploadTask();
		}else{
			this.uploading = false;
		}		
	}
});
