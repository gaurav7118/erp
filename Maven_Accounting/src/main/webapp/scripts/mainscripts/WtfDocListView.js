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
Wtf.docscomGrid = function(config){

    Wtf.apply(this, config);
    
    var showQuotationDoc = false;
    
    if(this.quotationID!=undefined && this.quotationID!="" && this.quotationID!=null){
        showQuotationDoc = true;
    }
    

    back = this;
    this.defaultPageSize = 20;

    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: false,
        emptyText:WtfGlobal.emptyGridRenderer("There are no results to display"),
        groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
        hideGroupedColumn: false
    });

    this.tagSearchTF = new Wtf.KWLTagSearch({
        id: 'tagdocquicksearch',
        width: 200,
        hidden:(this.fromUploadFiles)?true:false,
        emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyFileName"),//'Search by file Name',
        tagSearch:true
    });    
    this.defaultSearchType = 1; //Search By Document Name
    var comboval1=WtfGlobal.getLocaleText("acc.mydocuments.searchtypecombo.bydocname");
    var comboval2=WtfGlobal.getLocaleText("acc.mydocuments.searchtypecombo.bycontent");
    this.searchTypeStore = new Wtf.data.SimpleStore({
        fields: ['value','name'],
        data : [
        [0,comboval1],//WtfGlobal.getLocaleText("crm.mydocuments.searchtypecombo.bydocname")]
        [1,comboval2]//WtfGlobal.getLocaleText("crm.mydocuments.searchtypecombo.bycontent")]
        ]
    });
    this.searchType= new Wtf.form.ComboBox({
        store: this.searchTypeStore,
        valueField:'value',
        displayField:'name',
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select Search Type',
        typeAhead:true,
        selectOnFocus:true,
        allowBlank:false,
        width: 180,
        forceSelection: true,
        value: this.defaultSearchType
    });
    this.searchType.on("select", function(cmb, rec, ind){
        this.ds.baseParams.searchType=this.searchType.getValue();
        this.grid1.getView().refresh();
        this.ds.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },this);

    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),//'Reset',
        scope: this,
        disabled: false,
        tooltip: {
            text: WtfGlobal.getLocaleText("acc.field.ResetSearchResults")
            },//'Click to remove any filter settings or search criteria and view all records.'},
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        handler: this.handleResetClick
    });

//    Wtf.ownerStore.load();

    var x=[{
        name: 'name',
        type:'string'
    },{
        name: 'relatedto',
        type:'string'
    },{
        name: 'relatedname',
        type:'string'
    },{
        name: 'size',
        type:'float'
    },{
        name: 'type',
        type:'string'
    },{
        name: 'uploadeddate',
        type:'date'
    },{
        name: 'Tags',
        type:'string'
    },{
        name:'docid',
        type:'string'
    },{
        name: 'author',
        type:'string'
    },{
        name:'uploadername',
        type:'string'
    },{
        name:'Summary'
    },{
        name:'userid',
        type:'string'
    }];

    var fields = Wtf.data.Record.create(x);

    this.reader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: 'data'
    },fields);

    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.sm2 = new Wtf.grid.CheckboxSelectionModel();
    this.sm2.id = "chk";

    this.ds = new Wtf.data.GroupingStore({
        url: "ACCDocumentCMN/getQuotationDocumentList.do",
        reader: this.reader,
        paramNames:{
            sort:'field',
            dir:'direction'
        },
        baseParams: {
            searchType:showQuotationDoc?11:this.defaultSearchType,
            quotationID:showQuotationDoc?this.quotationID:"",
            moduleid:this.moduleid
        },
        sortInfo: {
            field: 'name',
            direction: "DESC"
        }
    });
    this.toolbar=new Wtf.PagingSearchToolbar({
        pageSize: this.defaultPageSize,
        searchField:this.tagSearchTF,
        displayInfo: true,
        id: "doc_pagingtoolbar",
        store: this.ds,
        plugins:this.pP = new Wtf.common.pPageSize({
            id: "pPageSize_" + this.id
        })
    });
    this.cm = new Wtf.grid.ColumnModel([this.sm,
        new Wtf.KWLRowNumberer({
            width: 30
        }),
        {
            id: 'Name',
            header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.mydocuments.header.name")+"'>"+WtfGlobal.getLocaleText("acc.mydocuments.header.name")+"</span>",
            dataIndex: 'name',
            sortable: true,
            groupable: true,
            groupRenderer: WtfGlobal.nameRenderer
        },{
            header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.mydocuments.header.size")+"'>"+WtfGlobal.getLocaleText("acc.mydocuments.header.size")+"</span>",
            dataIndex: 'size',
            sortable: true,
            align: 'center',
            groupable: true,
            renderer:WtfGlobal.sizetypeRenderer,
            groupRenderer: WtfGlobal.sizeRenderer
        },{
            id: 'Date_Modified',
            header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.mydocuments.header.uploadedon")+"'>"+WtfGlobal.getLocaleText("acc.mydocuments.header.uploadedon")+"</span>",
            dataIndex: 'uploadeddate',
            align:'center',
            sortable: true,
            renderer: WtfGlobal.onlyDateRenderer,
            groupable: true,
            groupRenderer: WtfGlobal.dateFieldRenderer
        },{
            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.mydocuments.header.uploadedby")+"'>"+WtfGlobal.getLocaleText("acc.mydocuments.header.uploadedby")+"</span>",
            dataIndex:'uploadername',
            sortable:true,
            groupable: true,
            align:'center',
            groupRenderer: WtfGlobal.nameRenderer

        },{
            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Download")+"'>"+WtfGlobal.getLocaleText("acc.field.Download")+"</span>",
            dataIndex:'abc',
            align:'center',
            renderer:function(a,b,c,d,e,f){
                var docid  = c.json.docid;
                var name  = c.json.name;
                var ext="";
                if (name.lastIndexOf(".") != -1) {
                    ext = name.substr(name.lastIndexOf("."));
                }
                return "<a href='javascript:void(0)' id='downloadlink' title='Download' onclick='openDldUrl(\"" + "../../fdownload.jsp?url="  + docid + ext + "&mailattch=true&dtype=attachment&docname="+name+"&moduleid="+this.moduleid+"\")'><div style = \"margin-left:30px; margin-right:30px\" class='pwnd downloadDoc' > </div></a>";
            }
        }
        ]);

    this.cm.defaultSortable = true;
    this.help=getHelpButton(this,32);
    this.quickSearchTF = new Wtf.KWLTagSearch({
        id: 'docquicksearch',
        width: 200,
        emptyText:WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt")//'Search Text'
    });

    //    var toolBarArr = [this.searchType, " ", this.quickSearchTF, "-", this.resetBttn, '->',this.help];
    //    if(Wtf.URole.roleid == Wtf.AdminId ){
    var  toolBarArr = [this.quickSearchTF, "-", this.resetBttn];
    //   s }
    this.quickSearchTF.on('SearchComplete', function() {
        this.toolbar.searchField=this.quickSearchTF;
        this.grid1.getView().refresh();
    }, this);

    Wtf.docscomGrid.superclass.constructor.call(this, {
        layout: 'border',
        items: [
        this.grid1 = new Wtf.grid.GridPanel({
            border: false,
            region: 'center',
            id: 'topic-grid' + config.id,
            store: this.ds,
            layout:'fit',
            view: this.groupingView,
            autoScroll:true,
            cm: this.cm,
            sm: this.sm2,
            scope:this,
            trackMouseOver: true,
            loadMask: {
                msg: 'Loading Documents...'
            },
            tbar:toolBarArr,
            bbar:this.toolbar
        })]
    });
    this.ds.on('load',function(){
        this.quickSearchTF.StorageChanged(this.ds);
        Wtf.updateProgress();
    },this);

    this.ds.on("datachanged",function(){
        this.quickSearchTF.setPage(this.pP.combo.value);
    },this);
};

Wtf.extend(Wtf.docscomGrid, Wtf.Panel, {

    loadMask: null,
    txtboxid: '',
    editable: 0,
    gridrowindex: '',
    root: null,
    defaultTag: null,
    tagsArray: null,
    tempSpans: null,
    spanlength: null,
    mainTree: null,
    flagForTreeClick: 0,
    flagForReloadTree: 0,
    regx : '^([\'"]?)\\s*([\\w]+[(/|\\\{1})]?)*[\\w]\\1$',
    tagregx: "\\w+|(([\'\"])\\s*([\\w][\\s[\\\\|\\/]\\w]*)\\s*\\2)|([\\w][\\s[\\\\|\\/]\\w]*)",
    patt1 : /(['"])\s*([\w]+[\s\\|\/\w]*)\s*\1/g,
    /*
        *  regx contains string capture by [\'|\"] this group followed by any number of whitespaces and more than one number of alphanumeric cha
         * then string capture by ([\\s\\\\\\/]*\\w+) this group followed by any number of whitespaces then
         * backrefreance to capture groupnumber 1 which is ([\'|\"])
    */
    patt2 : /([\w][\\|\/\w]*)/g,
    /*
    * capture the string which start with alphanumeric charector and it contains zero or more occurance of string captute by
    * (\\w+([\\\\\\/]*\\w+)*
    */
    handleResetClick:function(){
        this.quickSearchTF.setValue("");
        this.toolbar.searchField=this.quickSearchTF;

        this.searchType.reset();
        this.ds.baseParams.searchType=this.searchType.getValue();

        this.grid1.getView().refresh();

        this.ds.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },
    afterRender: function(config){
        Wtf.docscomGrid.superclass.afterRender.call(this, config);
        this.ds.load({
            params: {
                start:0,
                limit:this.defaultPageSize
            }
        });
    }
}); 
