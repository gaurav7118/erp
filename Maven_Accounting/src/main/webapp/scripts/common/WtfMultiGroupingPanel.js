/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.namespace("Wtf.ux.grid");
 	
Wtf.ux.grid.MultiGroupingGrid = function(config) {
    config = config||{};
 	
    // Cache the orignal column model, before state is applied
    if(config.cm)
        this.origColModel = config.cm.config;
    else if(config.colModel)
        this.origColModel = config.colModel.config;
    Wtf.ux.grid.MultiGroupingGrid.superclass.constructor.call(this, config);
//console.debug("Create MultiGroupingGrid",config);
};
Wtf.extend(Wtf.ux.grid.MultiGroupingGrid, Wtf.grid.GridPanel, {
    
    initComponent : function(){
        //console.debug("MultiGroupingGrid.initComponent",this);
        Wtf.ux.grid.MultiGroupingGrid.superclass.initComponent.call(this);
        // Initialise DragZone
//        this.on("render", this.setUpDragging, this);
    }
 	
    /** @cfg emptyToolbarText String to display on tool bar when there are no groups
 	*/
    ,
    emptyToolbarText :WtfGlobal.getLocaleText("acc.field.DropColumnsHereToGroup")
    /** Extend basic version so the Grouping Columns State is remebered
 	*/
    ,
    getState : function(){
        var s = Wtf.ux.grid.MultiGroupingGrid.superclass.getState.call(this);
        s.groupFields = this.store.getGroupState();
        return s;
    }
 	
    /** Extend basic version so the Grouping Columns State is applied
 	*/
    ,
    applyState : function(state){
        Wtf.ux.grid.MultiGroupingGrid.superclass.applyState.call(this,state);
        if(state.groupFields) {
            this.store.groupBy(state.groupFields,true);
            console.debug("Grid.applyState: Groups=",state.groupFields);
        }
    }
 	
    ,
    setUpDragging: function() {
    }
 	
    ,
    buildFilters: function (columns, record) {
        //console.debug("Grid.buildFilters: Created Filters from ", columns, record);
        var config = [];
        for(var i=0;i<columns.length;i++) {
            var col = columns[i];
            var meta = record.getField(col.dataIndex);
            //console.debug("Meta Data For ", col.dataIndex, meta)
            if(meta && (meta.filter || meta.filterFieldName)) {
                var dt = meta.dataType || 'string';
                if (dt=='int' || dt=='long' || dt=='float' || dt=='double')
                    dt=='numeric';
                else if (dt=='dateonly' || dt=='datetime')
                    dt='date';
                //FIXME pass caseType on this filter definition, so it can be applied to the filter field
                var f = {
                    dataIndex:col.dataIndex, 
                    type:dt, 
                    paramName:col.filterFieldName
                };
                config[config.length] = f;
            }
        }
        console.debug("Grid.buildFilters: Created Filters for ", config);
        if(config.length==0)
            return null;
        else
            return new Wtf.ux.grid.GridFilters({
                filters:config, 
                local:false
            });
    }
 	
    ,
    buildColumnModel: function (columns, record) {
        var config = [];
        for(var i=0;i<columns.length;i++) {
            var col = columns[i];
            var meta = record.getField(col.dataIndex);
            var cm = Wtf.apply({},col);
            if(meta) {
                // Apply stuff from the Record's Meta Data
                if(!cm.hidden && meta.hidden==true) cm.hidden = true;
                if(!cm.header && meta.label) cm.header = meta.label;
                if(!cm.renderer && meta.renderer) cm.renderer = meta.renderer;
                if(!cm.summaryType && meta.summaryType) cm.summaryType = meta.summaryType;
                if(!cm.summaryRenderer && meta.summaryRenderer) cm.summaryRenderer = meta.summaryRenderer;
                cm.sortable=(meta.sortable===true || (meta.sortFieldName&&meta.sortFieldName!=''));
 	
                // Apply more metadata from associated ClassMetaData
                var mc = meta.metaClass || record.defaultMetaClass;
                var mfn = (meta.mapping||col.dataIndex).match(/.*\b(\w+)$/)[1];
                var mf = ClassMetaData[mc]?ClassMetaData[mc].fields[mfn]:undefined;
                if(!mf) mf = ClassMetaData[mc]?ClassMetaData[mc].fields[col.dataIndex]:undefined;
                console.debug("Meta Class=",mc,ClassMetaData[mc],', dataIndex=',col.dataIndex,', mapping=',meta.mapping,', mfn=',mfn,', mf=',mf,', meta=',meta);
                if(mf) {
                    // Default the header text
                    if(!cm.header && mf.label) cm.header=mf.label;
                    // Default the column width
                    if(!cm.width) {
                        if(mf.maxLength) cm.width = Math.min(Math.max(mf.maxLength,5),40)*8;
                        else if(mf.type) {
                            if(mf.type=='dateonly') cm.width=100;
                            else if(mf.type=='datetime') cm.width=140;
                            else if(mf.type=='boolean') cm.width=50;
                        }
                    }
                    // Default the alignment
                    if(!cm.align && mf.type && (mf.type=='float'||mf.type=='int'))
                        cm.align = 'right';
                    // Default standard renderers
                    if(!cm.renderer && mf.type) {
                        if(mf.type=='dateonly') cm.renderer = Wtf.util.Format.dateRenderer();
                        else if(mf.type=='datetime') cm.renderer = Wtf.util.Format.dateTimeRenderer();
                    }
                    if(mf.hidden==true) cm.hidden = true;
                }
            }
            if(!cm.header) cm.header = col.dataIndex;
            cm.groupable = (cm.groupable==true || cm.sortable==true);
            config[config.length] = cm;
            console.debug("Grid.buildColumnModel: Width", cm.dataIndex, cm.width);
 	
        }
        console.debug("Grid.buildColumnModel: Created Columns for ", config);
        return new Wtf.grid.ColumnModel(config);
    }
 	
});
 	

Wtf.ux.grid.MultiGroupingPagingGrid = Wtf.extend(Wtf.ux.grid.MultiGroupingGrid, {
 	
    /** When creating the store, register an internal callback for post load processing
	*/
    constructor: function(config) {
        config = config||{};
        config.bbar = [].concat(config.bbar);
        config.bbar = config.bbar.concat([
        {
            xtype:'tbfill'
        }
        ,{
            xtype:'tbtext',
            id:'counter', 
            text: WtfGlobal.getLocaleText("acc.field.?of?")
        }
        ,{
            xtype:'tbspacer'
        }
        ,{
            xtype:'tbbutton',
            id:'loading',
            hidden: true,
            iconCls: "x-tbar-loading"
        }
        ,{
            xtype:'tbseparator'
        }
        ,{
            xtype:'tbbutton',
            id:'more',
            text: WtfGlobal.getLocaleText("acc.field.More..."),
            handler: function() {
                this.store.loadMore(false);
            }, 
            scope: this
        }
        ]);
 	
        Wtf.ux.grid.MultiGroupingPagingGrid.superclass.constructor.apply(this, arguments);
 	
        // Create Event that asks for more data when we scroll to the end
        this.on("bodyscroll", function() {
            var s = this.view.scroller.dom;
            if( (s.offsetHeight+s.scrollTop+5 > s.scrollHeight) && !this.isLoading) {
                console.debug("Grid.on.bodyscroll: Get more...");
                this.store.loadMore(false);
            }
        }, this);
 	
        // When the grid start loading, display a loading icon
        this.store.on("beforeload", function(store,o) {
            if(this.isLoading) {
                console.debug("Store.on.beforeload: Reject Load, one is in progress");
                return false;
            }
            this.isLoading = true;
            if(this.rendered) {
                this.barLoading.show();
            }
            console.debug("Store.on.beforeload: options=",o, this);
            return true;
        }, this);
	
        // When loading has finished, disable the loading icon, and update the row count
        this.store.on("load", function() {
            delete this.isLoading;
            if(this.rendered) {
                this.barLoading.hide();
                console.debug("Store.on.load: Finished loading.. ",this.store.totalCount);
                this.barCounter.getEl().innerHTML = "Showing " + this.store.getCount()+' of ' +
                (this.store.totalCount?this.store.totalCount:'?');
                if(this.store.totalCount)
                    this.barMore.disable();
                else
                    this.barMore.enable();
            }
            return true;
        }, this);
 	
        // When a loading error occurs, disable the loading icon and display error
        this.store.on("loadexception", function(store, e) {
            console.debug("Store.loadexception.Event:",arguments);
            delete this.isLoading;
            if(this.rendered) {
                this.barLoading.hide();
            }
            if(e)
                Wtf.Msg.show({
                    title:WtfGlobal.getLocaleText("acc.field.ShowDetails"),
                    msg: WtfGlobal.getLocaleText("acc.field.ErrorLoadingRecords-") + e,
                    buttons: Wtf.Msg.OK,
                    icon: Wtf.MessageBox.ERROR
                });
            return false;
        }, this);
 	
        // As the default onLoad to refocus on the first row has been disabled,
        // This has been added so if a load does happen, and its an initial load
        // it refocuses. If this is a refresh caused by a sort/group or a new page
        // of data being loaded, it does not refocus
        this.store.on("load", function(r,o) {
            if(o&&o.initial==true)
                Wtf.ux.grid.MultiGroupingView.superclass.onLoad.call(this);
        }, this.view);
    }
 	
    // private
    ,
    onRender : function(ct, position){
        Wtf.ux.grid.MultiGroupingPagingGrid.superclass.onRender.call(this, ct, position);
        var bb=this.getBottomToolbar();
        this.barCounter = bb.items.itemAt(bb.items.length-5);
        this.barMore = bb.items.itemAt(bb.items.length-1);
        this.barLoading = bb.items.itemAt(bb.items.length-3);
    }
});
