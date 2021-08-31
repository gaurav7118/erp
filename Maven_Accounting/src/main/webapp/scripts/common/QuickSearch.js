//Ankush

Wtf.wtfQuickSearch = function(config){
    Wtf.wtfQuickSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.wtfQuickSearch, Wtf.form.TextField, {
    Store: null,
    StorageArray: null,
    initComponent: function(){
        Wtf.wtfQuickSearch.superclass.initComponent.call(this);
        this.addEvents({
            'SearchComplete': true
        });
    },
    onRender: function(ct, position){
        Wtf.wtfQuickSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){

    if(this.Store)
        {
            if (this.getValue() != "") {
                this.Store.removeAll();
                var i = 0;
                while (i < this.StorageArray.length) {
                    var str=new RegExp("^"+this.getValue()+".*$|\\s"+this.getValue()+".*$","i");

                    if (str.test(this.StorageArray[i].get(this.field))) {
                        this.Store.add(this.StorageArray[i]);
                    }
                    i++;
                }

            }
            else {
                this.Store.removeAll();
                for (i = 0; i < this.StorageArray.length; i++) {
                    this.Store.insert(i, this.StorageArray[i]);
                }
            }
        }
        this.fireEvent('SearchComplete', this.Store);
    },
    StorageChanged: function(store){
        this.Store = store;
        this.StorageArray = this.Store.getRange();
    }
});
Wtf.reg('MyQuickSearch1', Wtf.wtfQuickSearch);



