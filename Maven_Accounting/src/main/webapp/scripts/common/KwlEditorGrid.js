/*Ankush Kale */


Wtf.KwlEditorGridPanel = function(config){
    Wtf.apply(this,config);
    if(!this.id)
        this.id="id"+Math.random()*100000;
	this.loadMask=true;
    this.emptext=this.searchEmptyText ? this.searchEmptyText : WtfGlobal.getLocaleText("acc.field.Searchhere");
    
    if(this.serverSideSearch){//If server side search required



        this.quickSearchTF = new Wtf.KWLTagSearch({
            id : 'Quick'+this.id,
            width: this.qsWidth?this.qsWidth:200,
            emptyText:this.emptext
        });

        if(this.bbar) //If bbar is there then it Appends in paging toolbar
        {
            var separator="-";
            this.tmpBottom=this.bbar;
            this.b1= new Array();

            for(i=0;i<this.tmpBottom.length;i++)
                this.b1[i+1] = this.tmpBottom[i];

            this.b1[0]=separator;

            pag=new Wtf.PagingSearchToolbar({
                pageSize: 120,
                border : false,
                id : "paggintoolbar"+this.id,
                searchField: this.quickSearchTF,
                store: this.store,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:this.displayInfo?this.displayInfo:false,
                items:this.b1
            });
        }
        else
        {
            pag=new Wtf.PagingSearchToolbar({
                pageSize: 120,
                border : false,
                id : "paggintoolbar"+this.id,
                searchField: this.quickSearchTF,
                store: this.store,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:this.displayInfo?this.displayInfo:false
            });
        }

    }
    else
    {
        this.quickSearchTF = new Wtf.wtfQuickSearch({
            id : 'Quick'+this.id,
            width: this.qsWidth?this.qsWidth:200,
            emptyText:this.emptext,
            field:this.searchField
        });
        if(this.bbar)
        {
            this.tmpBottom=this.bbar;
            this.b1= new Array();

            for(i=0;i<this.tmpBottom.length;i++)
                this.b1[i] = this.tmpBottom[i];

            pag=new Wtf.PagingToolbar({
                pageSize: 120,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.store,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:this.displayInfo?this.displayInfo:false,
                items:this.b1
            });
        }
        else
        {
            pag=new Wtf.PagingToolbar({
                pageSize: 120,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.store,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:this.displayInfo?this.displayInfo:false
            });
        }
    }

    if(!this.nopaging)
        this.bbar=pag;
    this.searchLabel = this.searchLabel ? this.searchLabel : WtfGlobal.getLocaleText("acc.field.QuickSearch");
    this.searchLabelSeparator= this.searchLabelSeparator ? this.searchLabelSeparator : " : ";

    this.searchLabel= this.searchLabel + this.searchLabelSeparator;

    if(this.tbar)//If tbar is there then Append with Quick Search
    {
        this.elements+=",tbar";
        if(typeof this.tbar=="object"){
            this.tmptoolbar=this.tbar;
            if(this.noSearch)
                {
                    this.topToolbar1=this.tmptoolbar;
                }
                else
                {
                    this.topToolbar1=new Array();
                    var k =this.tmptoolbar.length;
                    var i =0 ;
                    var j=2;
                    for(i=0;i<k;i++)
                        this.topToolbar1[j++] = this.tmptoolbar[i];

                    this.topToolbar1[0] =this.searchLabel;
                    this.topToolbar1[1] =this.quickSearchTF;
                }
            this.topToolbar=this.topToolbar1;
        }delete this.tbar
    }
    else
        {
            if(!this.noSearch)
                {
                    this.tbar=[this.searchLabel,this.quickSearchTF];
                }
        }

        


    this.store.on("load",function(){

        this.quickSearchTF.StorageChanged(this.store);
    },this);

    this.store.on("datachanged", function(){
        if(this.serverSideSearch){
            if(this.pPageSizeObj.combo != undefined){
                this.quickSearchTF.setPage(this.pPageSizeObj.combo.value);
            }
        }
    }, this);

    this.doLayout();
    Wtf.KwlEditorGridPanel.superclass.constructor.call(this);
};
Wtf.extend(Wtf.KwlEditorGridPanel,Wtf.grid.EditorGridPanel,{
    layout:'fit',
    onRender: function(config){
        Wtf.KwlEditorGridPanel.superclass.onRender.call(this,config);
    }
});

