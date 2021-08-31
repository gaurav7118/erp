var tmceInitialized = false;

/** ----------------------------------------------------------
		Wtf.ux.TinyMCE
	*/
Wtf.ux.TinyMCE = Wtf.extend(

    // Constructor
    function( cfg ){

        var config = {
            tinymceSettings: {
                accessibility_focus : false 
            }
        };

        Wtf.apply( config, cfg );

        // Add events
        this.addEvents({
            "editorcreated": true,
            "onChange": true
        });
        Wtf.ux.TinyMCE.superclass.constructor.call( this, config );
    },

    // Base class
    Wtf.form.Field,

    // Members
    {

        // TinyMCE Settings specified for this instance of the editor.
        tinymceSettings: null,

        // HTML markup for this field
        defaultAutoCreate: {
            tag: "div", 
            style: {
                overflow: "hidden"
            }, 
            children: [{
                tag: "textarea"
            }]
        },

       
        initComponent: function(){
            this.tinymceSettings = this.tinymceSettings || {};
            Wtf.ux.TinyMCE.initTinyMCE({
                language: this.tinymceSettings.language
            });
        },

        /** ----------------------------------------------------------
			*/
        onRender : function( ct, position ){
            Wtf.ux.TinyMCE.superclass.onRender.call( this, ct, position );

            var self = this;

            // Fix size if it was specified in config
            var el = this.getEl();
            if( Wtf.type( this.width ) == "number" ) {
                el.setWidth( this.width );
                this.tinymceSettings.width = this.width;
            }
            if( Wtf.type( this.height ) == "number" ) {
                el.setHeight( this.height );
                this.tinymceSettings.height = this.height;
            }

            // Fetch reference to <textarea> element
            var textarea = el.child( "textarea" );
            this.textareaEl = textarea;
            if( this.name ) textarea.set({
                name: this.name
            });
            var id = textarea.id;

            // Create TinyMCE editor.
            this.ed = new tinymce.Editor( id, this.tinymceSettings, tinymce.EditorManager);  

            this.ed.render();
            tinyMCE.add( this.ed );


            // Indicate that editor is created
            this.fireEvent( "editorcreated",this );

        },

        /** ----------------------------------------------------------
			     * Returns the name attribute of the field if available
			     * @return {String} name The field name
			*/
        getName: function(){
            return this.rendered && this.textareaEl.dom.name ? this.textareaEl.dom.name : (this.name || '');
        },

        /** ----------------------------------------------------------
			*/
        initValue : function(){

            if( this.value !== undefined )
            {
                this.setValue( this.value );
            }
            else
            {
                var textarea = this.getEl().child( "textarea", true );
                if( textarea.value.length > 0 )
                    this.setValue( textarea.value );
            }
        },

        /** ----------------------------------------------------------
			*/
        beforeDestroy: function(){
           
            Wtf.ux.TinyMCE.superclass.beforeDestroy.call( this );
        //                            }
        },
       
        getValue : function(){

            if( !this.rendered || !this.ed.initialized )
                return this.value;

            var v = this.ed.getContent();
            if( v === this.emptyText || v === undefined ){
                v = '';
            }
            return v;
        },

        /** ----------------------------------------------------------
			*/
        setValue : function( v ){
            this.value = v;
            if( this.rendered )
                this.withEd( function(){
                    this.ed.undoManager.clear();
                    this.ed.setContent( v === null || v === undefined ? '' : v );
                    this.ed.startContent = this.ed.getContent({
                        format : 'raw'
                    });
                    this.validate();
                });
        },

        /** ----------------------------------------------------------
			*/
        isDirty : function() {
            if( this.disabled || !this.rendered ) {
                return false;
            }
            return this.ed.isDirty();
        },

        /** ----------------------------------------------------------
			*/
        syncValue : function(){
            if( this.rendered && this.ed.initialized )
                this.ed.save();
        },

        /** ----------------------------------------------------------
			*/
        getEd: function() {
            return this.ed;
        },

        /** ----------------------------------------------------------
			*/
        onResize : function( aw, ah ){
            if( this.rendered ){
                this.withEd( function() {

                    if( Wtf.type( aw ) != "number" ) aw = this.el.getWidth();
                    if( Wtf.type( ah ) != "number" ) ah = this.el.getHeight();

                    this.ed.theme.resizeTo( aw, ah );
                });
            }
        },

        /** ----------------------------------------------------------
			*/
        focus: function( selectText, delay ){
            Wtf.ux.TinyMCE.superclass.focus.call( this, selectText, delay );
        },

        /** ----------------------------------------------------------
			*/
        onFocus : function(){
            if(!this.hasFocus){
                this.hasFocus = true;
                this.startValue = this.getValue();
                this.withEd( function() {
                    this.ed.focus();
                    this.fireEvent("focus", this);
                });
            }
        },

        /** ----------------------------------------------------------
				If ed (local editor instance) is already initilized, calls
				specified function directly. Otherwise - adds it to ed.onInit event.
			*/
        withEd: function( func ){

            // If editor is not created yet, reschedule this call.
            if( !this.ed ) this.on(
                "editorcreated",
                function() {
                    this.withEd( func )
                },
                this );

            // Else if editor is created and initialized
            else if( this.ed.initialized ) func.call( this );

        // Else if editor is created but not initialized yet.
        //				else this.ed.onInit.add( function(){ func.defer( 10, this ); }.createDelegate( this ));     // BNM-113 Change syntax
        },

        getSelectedText : function(){
            if( !this.rendered || !this.ed.initialized )
                return this.ed.selection.getContent({
                    format:"html"
                });

            var v = this.ed.selection.getContent({
                format:"html"
            });
            if( v === this.emptyText || v === undefined ){
                v = '';
            }
            return v;
        },

        cutSelectedText : function(){
            return this.ed.execCommand("mceInsertContent",false,"");
        }

    }
    );
// Add static members
Wtf.apply( Wtf.ux.TinyMCE, {

    /**
			Static field with all the plugins that should be loaded by TinyMCE.
			Should be set before first component would be created.
			@static
		*/
    tinymcePlugins: "pagebreak,layer,table,media,contextmenu,paste,noneditable,visualchars,nonbreaking,template,spellchecker,compat3x",  

    initTinyMCE: function( settings ) {
        if( !tmceInitialized ){

            var s = {
                mode : "none",
                plugins : Wtf.ux.TinyMCE.tinymcePlugins,
                theme: "modern"                        
            };
            Wtf.apply( s, settings );

            tinyMCE.init( s );
            tmceInitialized = true;
        }
    }
});

Wtf.ComponentMgr.registerType( "tinymce", Wtf.ux.TinyMCE );


/** ----------------------------------------------------------
		WindowManager
	*/
var WindowManager = Wtf.extend(

    function( editor ) {
        WindowManager.superclass.constructor.call( this, editor );
    },

    tinymce.WindowManager,

    {
        // Override WindowManager methods
        alert : function( txt, cb, s ) {
            Wtf.MessageBox.alert( "", txt, function() {
                cb.call( this );
            }, s );
        },

        confirm : function( txt, cb, s ) {
            Wtf.MessageBox.confirm( "", txt, function( btn ) {
                cb.call( this, btn == "yes" );
            }, s );
        },

        open : function( s, p ) {

            s = s || {};
            p = p || {};

            if ( !s.type )
                this.bookmark = this.editor.selection.getBookmark( 'simple' );

            s.width = parseInt(s.width || 320);
            s.height = parseInt(s.height || 240) + (tinymce.isIE ? 8 : 0);
            s.min_width = parseInt(s.min_width || 150);
            s.min_height = parseInt(s.min_height || 100);
            s.max_width = parseInt(s.max_width || 2000);
            s.max_height = parseInt(s.max_height || 2000);
            s.movable = s.resizable = true;
            p.mce_width = s.width;
            p.mce_height = s.height;
            p.mce_inline = true;

            this.features = s;
            this.params = p;

            var win = new Wtf.Window(
            {
                title: s.name,
                border: false,
                width: s.width,
                height: s.height,
                minWidth: s.min_width,
                minHeight: s.min_height,
                resizable: true,
                maximizable: s.maximizable == true,
                minimizable: s.minimizable == true,
                modal: true,
                layout: "fit",
                items: [
                {
                    xtype: "iframepanel",
                    defaultSrc: s.url || s.file
                }
                ]
            });

            p.mce_window_id = win.getId();

            win.show( null,
                function() {
                    if( s.left && s.top )
                        win.setPagePosition( s.left, s.top );
                    var pos = win.getPosition();
                    s.left = pos[0];
                    s.top = pos[1];
                    this.onOpen.dispatch( this, s, p );
                },
                this
                );

            return win;
        },

        close : function( win ) {

            // Probably not inline
            if( !win.tinyMCEPopup || !win.tinyMCEPopup.id ) {
                WindowManager.superclass.close.call( this, win );
                return;
            }

            var w = Wtf.getCmp( win.tinyMCEPopup.id );
            if( w ) {
                this.onClose.dispatch( this );
                w.close();
            }
        },

        setTitle : function( win, ti ) {

            // Probably not inline
            if( !win.tinyMCEPopup || !win.tinyMCEPopup.id ) {
                WindowManager.superclass.setTitle.call( this, win, ti );
                return;
            }

            var w = Wtf.getCmp( win.tinyMCEPopup.id );
            if( w ) w.setTitle( ti );
        },

        resizeBy : function( dw, dh, id ) {

            var w = Wtf.getCmp( id );
            if( w ) {
                var size = w.getSize();
                w.setSize( size.width + dw, size.height + dh );
            }
        },

        focus : function(id) {
            var w = Wtf.getCmp( id );
            if( w ) w.setActive( true );
        }

    }
    );

/** ----------------------------------------------------------
		ControlManager
	*/
var ControlManager = Wtf.extend(

    // Constructor
    function( control, ed, s ) {
        this.control = control;
        ControlManager.superclass.constructor.call( this, ed, s );
    },

    // Base class
    tinymce.ControlManager,

    // Members
    {
        // Reference to WtfJS control Wtf.ux.TinyMCE.
        control: null,

        createDropMenu: function( id, s ){
            // Call base method
            var res = ControlManager.superclass.createDropMenu.call( this, id, s );

            // Modify returned result
            //var self = this;
            var orig = res.showMenu;
            res.showMenu = function( x, y, px ) {
                orig.call( this, x, y, px );
                //var zi = self.control.getEl().getStyle( "z-index" );
                Wtf.fly( 'menu_' + this.id ).setStyle( "z-index", 200001 );
            }

            return res;
        },

        createColorSplitButton: function( id, s ){
            // Call base method
            var res = ControlManager.superclass.createColorSplitButton.call( this, id, s );

            // Modify returned result
            var orig = res.showMenu;
            res.showMenu = function( x, y, px ) {
                orig.call( this, x, y, px );
                Wtf.fly( this.id + '_menu' ).setStyle( "z-index", 200001 );
            }

            return res;
        }
    }
    );
