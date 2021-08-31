Wtf.namespace("Wtf.ux.grid.plugins");

Wtf.ux.grid.plugins.GroupCheckboxSelection = {

	init: function(grid){

		grid.view.groupTextTpl =
			'<input type="checkbox" ' +
			'class="x-grid-group-checkbox" x-grid-group-hd-text="{text}" /> ' +
			grid.view.groupTextTpl;

		grid.on('render', function() {
			Wtf.ux.grid.plugins.GroupCheckboxSelection.initBehaviors(grid);
		});

		grid.view.on('refresh', function() {
			Wtf.ux.grid.plugins.GroupCheckboxSelection.initBehaviors(grid);
		});
	},

	initBehaviors: function(grid) {
		var id = "#" + grid.id;
		var behaviors = {};

		// Check/Uncheck all items in group
		behaviors[id + ' .x-grid-group-hd .x-grid-group-checkbox@click'] =
			function(e, target){

				var ds = grid.getStore();
				var sm = grid.getSelectionModel();
				var cm = grid.getColumnModel();
//                                sm.singleSelect = false;

				var text = target.getAttribute("x-grid-group-hd-text");
				var parts = text.split(":");

				var header = parts[0].trim();
                                   var value = text.substr(header.length+2, text.length);//SDP-5142 // If value itself was having ":"(colon) in it would split "text" additionally instead of only two
                                   //text.substr(header.length+2, text.length)
                                   //header.length+2 - Here, we have added 2 in header.length because to start from actual value
				var field = cm.getColumnsBy(function(columnConfig, index){
					return (columnConfig.header == header);
				})[0].dataIndex;

				var records = ds.queryBy(function(record){
                                    return (record.get(field) == value);
                                }, this).items;
				
                                
//                                var records = ds.query(field, value).items;

				for(var i = 0, len = records.length; i < len; i++){
					var row = ds.indexOf(records[i]);
					if (target.checked) {
						sm.selectRow(row, true);
					}
					else {
						sm.deselectRow(row);
					}
				}
//                                sm.singleSelect = true;
			};

		// Avoid group expand/collapse clicking on checkbox
		behaviors[id + ' .x-grid-group-hd .x-grid-group-checkbox@mousedown'] =
			function(e, target){
				e.stopPropagation();
			};

		Wtf.addBehaviors(behaviors);
	}

};

