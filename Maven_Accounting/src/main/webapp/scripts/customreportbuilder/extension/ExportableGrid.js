/**
 * ExportableGrid.js
 * Wrapper over ExtJs's grid to make grid exportable to xlsx format.
 * 
 * (c) 2016 Nikita Metzger
 * Distributed under MIT license:
 * https://github.com/yorl1n/ext.ExportableGrid/blob/master/LICENSE.md
 * 
 * ExportableGrid library uses two libraries distributed under the MIT license:
 * FileSaver.js - https://github.com/eligrey/FileSaver.js
 * jszip.js - https://github.com/Stuk/jszip
 */
Ext.define('ReportBuilder.extension.ExportableGrid', {
    extend: 'Ext.pivot.Grid',
    xtype: 'exportablegrid',
    alias: 'widget.exportablegrid',
    config: {
        /**
         * Title/name of the exported xlsx.
         */
        xlsTitle: 'export',
        /**
         * Color of the headers.
         */
        xlsHeaderColor: 'A3C9F1',
        /**
         * Color of the grouping headers.
         */
        xlsGroupHeaderColor: 'EBEBEB',
        /**
         * Color of the summary row.
         */
        xlsSummaryColor: 'FFFFFF',
        /**
         * Show/hide first row with name of exported file.
         */
        xlsShowHeader: false
    },
    
    exportFile: function (name, isCollapsed) {
        var exportTask = {
            zip: new JSZip(),
            xlsTitle: name || this.xlsTitle,
            sharedStrings: this.xlsShowHeader ? [this.xlsTitle] : [],
            totalStrings: this.xlsShowHeader ? 1 : 0,
            totalColumns: 0,
            exportableColumns: [],
            style: {
                styles: []
            },
            exportFile: function(grid) {
                this.zip.generateAsync({type: 'blob', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'}).then(function (content) {
                    saveAs(content, exportTask.xlsTitle + '.xlsx');
                });
            },
            isCollapsed: isCollapsed
        };
        
        var cols = this.applyColumnDataType();
        var depth = this.getTotalHeaderDepth(cols);
        var expColHeaders = new Ext.util.HashMap();
        for (var i = 0; i < cols.length; i++) {
            if (this.isColumnExportable(cols[i])) {
                var stl = {align: cols[i].align ? cols[i].align : 'left', width: cols[i].getWidth() / 6};
                if (cols[i].exportNumberFormat != null) {
                    stl.numFmt = cols[i].exportNumberFormat;
                }
                exportTask.style.styles.push(stl);
                exportTask.totalColumns++;
                exportTask.exportableColumns.push(cols[i]);
                var topLvlCol = this.getTopLevelColOrDepth(cols[i]);
                if (!expColHeaders.containsKey(topLvlCol.id)) {
                    expColHeaders.add(topLvlCol.id, this.prepareExportableColumnHeader(topLvlCol, depth));
                }
            }
        }

        exportTask.levels = new Ext.util.HashMap();
        expColHeaders.each(function (k, v, l) {
            this.expandOnLevels(v, exportTask.levels);
        }, this);
        exportTask.levels.each(function (k, v) {
            for (var i = 0; i < v.length; i++) {
                this.totalStrings++;
                if (v[i].text != null && exportTask.sharedStrings.indexOf(v[i].text) < 0) {
                    exportTask.sharedStrings.push(v[i].text);
                }
            }
        }, this);
        this.generateAlphabetPositions(exportTask);
        this.generateStructure(exportTask);
        exportTask.exportFile(this);
    },
    
    applyColumnDataType: function () {
        var gridColumns = this.getView().getGridColumns();
        var model = this.getMatrix().model;
        for (var i = 0; i < gridColumns.length; i++) {
            for(var j = 0; j < model.length; j++) {
                if(gridColumns[i].dataIndex == model[j].name) {
                    gridColumns[i].type = model[j].type;
                }
            }
        }
        return gridColumns;
    },
    
    removeSeparatorFromAmount: function (type, val) {
        if(type != 'string') {
            return val.replace(/[\s,]+/g, '');
        }
        return val;
    },
    
    privates: {
        alphabet: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"],
        columnTypes: {int: 'n', float: 'n', bool: 'b', boolean: 'b', date: 's', string: 's'},
        /**
         * The count of predefined styles.
         */
        staticStylesCount: 7,
        /**
         * Default type is string.
         */
        defaultType: 's',
        /**
         * Returns an excel type by provided extjs's type.
         * @param {type} type
         * @returns {ExportableGridAnonym$0.privates.columnTypes|String}
         */
        getExportableColumnType: function (gridColumn) {
            if (Ext.grid.column.Number && gridColumn instanceof Ext.grid.column.Number) {
                return this.columnTypes['float'];
            }
            if (Ext.grid.column.Date && gridColumn instanceof Ext.grid.column.Date) {
                return this.columnTypes['date'];
            }
            if (Ext.grid.column.Boolean && gridColumn instanceof Ext.grid.column.Boolean) {
                return this.columnTypes['boolean'];
            }
            if (Ext.grid.column.Check && gridColumn instanceof Ext.grid.column.Check) {
                return this.columnTypes['boolean'];
            }
            return this.defaultType;
        },
        /**
         * Checks if the column is exportable and should be exported.
         * @param {type} col
         * @returns {Boolean}
         */
        isColumnExportable: function (col) {
            if (col.xtype !== 'actioncolumn' && (col.dataIndex !== '') && !col.hidden && (col.exportable === undefined || col.exportable)
            && col.innerCls !== Ext.baseCSSPrefix + 'grid-cell-inner-row-expander') {
                return true;
            } else {
                return false;
            }
        },
        /**
         * Expand the object and it's children on levels.
         * @param {type} obj
         * @param {type} map
         * @returns map[level, exportable column]
         */
        expandOnLevels: function (obj, map) {
            if (map.containsKey(obj.level)) {
                map.get(obj.level).push(obj);
            } else {
                map.add(obj.level, [obj]);
            }
            if (obj.children.length > 0) {
                for (var i = 0; i < obj.children.length; i++) {
                    this.expandOnLevels(obj.children[i], map);
                }
            }
        },
        /**
         * Gets total depth of columns.
         * @param {type} cols
         * @returns 
         */
        getTotalHeaderDepth: function (cols) {
            var depth = 1;
            for (var i = 0; i < cols.length; i++) {
                if (this.isColumnExportable(cols[i])) {
                    var tmpDepth = this.getTopLevelColOrDepth(cols[i], 1);
                    if (depth < tmpDepth) {
                        depth = tmpDepth;
                    }
                }
            }
            return depth;
        },
        /**
         * Prepares the exportable column
         * @param {type} col
         * @param {type} depth
         * @param {type} parent
         * @returns 
         */
        prepareExportableColumnHeader: function (col, depth, parent) {
            var text = undefined;
            if(col.text != undefined) {
                if (typeof col.text === "string") {
                    text = col.text.replace(/(<([^>]+)>)/ig, "");   //replace HTML from column header
                }
                else {
                    text = col.text.toString().replace(/(<([^>]+)>)/ig, "");    //replace HTML from column header
                }                
            }
            var obj = {
                id: col.id,
                text: text,
                level: parent ? parent.level + 1 : 0,
                mergeDown: 0, //how many cells should merged down
                align: col.headerAlign || col.align || 'left',
                children: []
            };
            if (col.items && col.items.items && col.items.items.length > 0) {
                for (var i = 0; i < col.items.items.length; i++) {
                    if (!col.items.items[i].hidden) {
                        obj.children.push(this.prepareExportableColumnHeader(col.items.items[i], depth, obj));
                    }
                }
            } else {
                obj.mergeDown = depth - obj.level - 1;
            }
            obj.mergeRight = this.countSubheaders(obj, -1);//how many cells should be merged right.
            return obj;
        },
        /**
         * Count amount of actual columns.
         * @param {type} obj
         * @param {type} counter
         * @returns 
         */
        countSubheaders: function (obj, counter) {
            if (obj.children.length > 0) {
                for (var i = 0; i < obj.children.length; i++) {
                    counter = this.countSubheaders(obj.children[i], counter);
                }
                return counter;
            }
            return counter + 1;
        },
        /**
         * Get the top level column for provided column or the depth for provided column.
         * @param {type} col
         * @param {type} depth
         * @returns 
         */
        getTopLevelColOrDepth: function (col, depth) {
            if (col.ownerCt.xtype === 'gridcolumn') {
                if (depth == null) {
                    return this.getTopLevelColOrDepth(col.ownerCt);
                } else {
                    return this.getTopLevelColOrDepth(col.ownerCt, depth + 1);
                }
            } else {
                if (depth == null) {
                    return col;
                } else {
                    return depth;
                }
            }
        },
        generateAlphabetPositions: function (exportTask) {
            var counter = 0;
            exportTask.alphabetColumns = [];
            var pos = exportTask.totalColumns;
            while (counter < pos) {
                if (exportTask.alphabetColumns.length < this.alphabet.length) {
                    for (var i = 0; i < this.alphabet.length; i++) {
                        exportTask.alphabetColumns.push(this.alphabet[i]);
                        counter++;
                        if (counter >= pos) {
                            break;
                        }
                    }
                } else {
                    var tmpAlpCols = exportTask.alphabetColumns.slice();
                    for (var i = 0; i < this.alphabet.length; i++) {
                        for (var j = 0; j < tmpAlpCols.length; j++) {
                            if (exportTask.alphabetColumns.indexOf(this.alphabet[i] + tmpAlpCols[j]) < 0) {
                                exportTask.alphabetColumns.push(this.alphabet[i] + tmpAlpCols[j]);
                                counter++;
                                if (counter >= pos) {
                                    break;
                                }
                            }
                        }
                        if (counter >= pos) {
                            break;
                        }
                    }
                }
            }
        },
        /**
         * Generates total structure.
         * @param {type} exportTask
         * @returns {undefined}
         */
        generateStructure: function (exportTask) {
            this.generateContentType(exportTask.zip);
            this.generateRels(exportTask.zip);
            this.generateDocProps(exportTask.zip);
            this.generateXl(exportTask);
        },
        /**
         * Generates [Content_Types].xml.
         * @param {type} zip
         * @returns {undefined}
         */
        generateContentType: function (zip) {
            zip.file('[Content_Types].xml', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">' +
                    '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml" />' +
                    '<Default Extension="xml" ContentType="application/xml" />' +
                    '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml" />' +
                    '<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml" />' +
                    '<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml" />' +
                    '<Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml" />' +
                    '<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml" />' +
                    '</Types>');
        },
        /**
         * Generates _rels folder with structure.
         * @param {type} zip
         * @returns {undefined}
         */
        generateRels: function (zip) {
            var _rels = zip.folder("_rels");
            _rels.file('.rels', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
                    '<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml" />' +
                    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml" />' +
                    '</Relationships>');
        },
        /**
         * Generates docProps folder with structure.
         * @param {type} zip
         * @returns {undefined}
         */
        generateDocProps: function (zip) {
            var docProps = zip.folder("docProps");
            docProps.file('app.xml', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">' +
                    '<TotalTime>0</TotalTime>' +
                    '<Application>Microsoft Excel</Application>' +
                    '<DocSecurity>0</DocSecurity>' +
                    '<ScaleCrop>false</ScaleCrop>' +
                    '<HeadingPairs>' +
                    '<vt:vector size="2" baseType="variant">' +
                    '<vt:variant>' +
                    '<vt:lpstr>Worksheets</vt:lpstr>' +
                    '</vt:variant>' +
                    '<vt:variant>' +
                    '<vt:i4>1</vt:i4>' +
                    '</vt:variant>' +
                    '</vt:vector>' +
                    '</HeadingPairs>' +
                    '<TitlesOfParts>' +
                    '<vt:vector size="1" baseType="lpstr">' +
                    '<vt:lpstr>Sheet1</vt:lpstr>' +
                    '</vt:vector>' +
                    '</TitlesOfParts>' +
                    '<Company />' +
                    '<LinksUpToDate>false</LinksUpToDate>' +
                    '<SharedDoc>false</SharedDoc>' +
                    '<HyperlinksChanged>false</HyperlinksChanged>' +
                    '<AppVersion>15.0300</AppVersion>' +
                    '</Properties>');
        },
        /**
         * Generates xl folder with structure.
         * @param {type} exportTask
         * @returns {undefined}
         */
        generateXl: function (exportTask) {
            var xl = exportTask.zip.folder("xl");
            this.generateXlRels(xl);
            this.generateWorkbook(xl);
            this.generateWorksheets(xl, exportTask);
            this.generateSharedStrings(xl, exportTask);
            this.generateStyles(xl, exportTask);
        },
        /**
         * Generates _rels subfolder of xl folder.
         * @param {type} xl
         * @returns {undefined}
         */
        generateXlRels: function (xl) {
            var _rels = xl.folder("_rels");
            _rels.file('workbook.xml.rels', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
                    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml" />' +
                    '<Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml" />' +
                    '<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml" />' +
                    '</Relationships>');
        },
        /**
         * Generates workbook.xml.
         * @param {type} xl
         * @returns {undefined}
         */
        generateWorkbook: function (xl) {
            xl.file('workbook.xml', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:x15="http://schemas.microsoft.com/office/spreadsheetml/2010/11/main" mc:Ignorable="x15">' +
                    '<fileVersion appName="xl" lastEdited="6" lowestEdited="6" rupBuild="14420" />' +
                    '<workbookPr defaultThemeVersion="153222" />' +
                    '<bookViews>' +
                    '<workbookView xWindow="0" yWindow="0" windowWidth="28800" windowHeight="14235" />' +
                    '</bookViews>' +
                    '<sheets>' +
                    '<sheet name="Sheet1" sheetId="1" r:id="rId1" />' +
                    '</sheets>' +
                    '<calcPr calcId="0" />' +
                    '</workbook>');
        },
        /**
         * Generates main worksheet.
         * @param {type} xl
         * @param {type} exportTask
         * @returns {undefined}
         */
        generateWorksheets: function (xl, exportTask) {
            var ws = xl.folder('worksheets');
            var currentRow = 1,
                    currentCol = 1,
                    mergeCells = [],
                    rows = '';
            if (this.xlsShowHeader) {
                currentRow = 2;
                rows += '<row r="1" customHeight="1" ht="38.1" spans="1:' + exportTask.totalColumns + '">' +
                        '<c r="A1" t="s" s="1"><v>0</v></c>';
                for (currentCol; currentCol < exportTask.totalColumns; currentCol++) {
                    rows += '<c r="' + exportTask.alphabetColumns[currentCol] + '1" t="s" s="1"/>';
                }
                rows += '</row>';
                mergeCells.push('A1:' + exportTask.alphabetColumns[exportTask.totalColumns - 1] + '1');
            }
            currentCol = 0;
            exportTask.mergedHeaders = [];
            exportTask.levels.each(function (k, v) {
                rows += '<row r="' + currentRow + '" customHeight="1" spans="1:' + exportTask.totalColumns + '">';
                for (var i = 0; i < v.length; i++) {
                    var styleId = 3;
                    switch (v[i].align) {
                        case 'right':
                            styleId = 2;
                            break;
                        case 'left':
                            styleId = 4;
                            break;
                    }
                    while (exportTask.mergedHeaders.indexOf(exportTask.alphabetColumns[currentCol] + currentRow) >= 0) {
                        rows += '<c r="' + exportTask.alphabetColumns[currentCol] + currentRow + '" t="s" s="' + styleId + '"/>';
                        currentCol++;
                    }
                    rows += '<c r="' + exportTask.alphabetColumns[currentCol] + currentRow + '" t="s" s="' + styleId + '"><v>' + exportTask.sharedStrings.indexOf(v[i].text) + '</v></c>';
                    if (v[i].mergeRight || v[i].mergeDown) {
                        var merge = exportTask.alphabetColumns[currentCol] + currentRow + ':';
                        var pos = currentRow;
                        if (v[i].mergeRight) {
                            for (var j = 0; j < v[i].mergeRight; j++) {
                                rows += '<c r="' + exportTask.alphabetColumns[++currentCol] + currentRow + '" t="s" s="' + styleId + '"/>';
                            }
                        }
                        if (v[i].mergeDown) {
                            for (var mc = 0; mc < v[i].mergeDown; mc++) {
                                pos++;
                                if (v[i].mergeRight) {
                                    for (var mr = 0; mr < v[i].mergeRight; mr++) {
                                        exportTask.mergedHeaders.push(exportTask.alphabetColumns[currentCol + mr] + pos);
                                    }
                                } else {
                                    exportTask.mergedHeaders.push(exportTask.alphabetColumns[currentCol] + pos);
                                }
                            }
                        }
                        merge += exportTask.alphabetColumns[currentCol] + pos;
                        mergeCells.push(merge);
                    }
                    currentCol++;
                    if (i + 1 >= v.length && currentCol < exportTask.totalColumns) {
                        while (exportTask.mergedHeaders.indexOf(exportTask.alphabetColumns[currentCol] + currentRow) >= 0) {
                            rows += '<c r="' + exportTask.alphabetColumns[currentCol] + currentRow + '" t="s" s="' + styleId + '"/>';
                            currentCol++;
                        }
                    }
                }
                rows += '</row>';
                currentRow++;
                currentCol = 0;
            }, this);

            var renderRecords = [];

            var features = {};
	    var commonView = this.lockable ? this.normalGrid.view : this.view;

            for (var i = 0; i < commonView.features.length; i++) {
                features[commonView.features[i].ftype] = commonView.features[i];
            }
            if (!this.store.isGrouped()) {
                renderRecords = Ext.clone(this.store.data.items);
            } else {
                var storeGroups = this.store.getGroups();
                var tpl = null;
                for (var feature in features) {
                    if (feature === 'grouping' || feature === 'groupingsummary') {
                        tpl = features[feature].groupHeaderTpl;
                    }
                }
                if (tpl != null) {
                    storeGroups.each(function (gr) {
                        var groupColumn = this.view.getVisibleColumnManager().getHeaderByDataIndex(this.store.getGroupField());
                        var renderedGroupValue = groupColumn.config.renderer ? groupColumn.config.renderer(gr.getAt(0).get(this.store.getGroupField()), {}, gr.getAt(0)) : gr.getGroupKey();
                        var groupingHeaderValue = tpl.apply({
                            groupValue: gr.getGroupKey(),
                            groupField: this.store.groupField,
                            columnName: groupColumn.text,
                            name: renderedGroupValue,
                            renderedGroupValue: renderedGroupValue
                        });
                        var grH = Ext.create('Ext.data.Model', {
                            groupingHeaderValue: groupingHeaderValue
                        });
                        grH.isGroupingHeader = true;
                        renderRecords.push(grH);
                        for (var i = 0; i < gr.items.length; i++) {
                            renderRecords.push(gr.items[i]);
                        }

                        if (features['groupingsummary']) {
                            var modelData = {};
                            for (var i = 0; i < exportTask.exportableColumns.length; i++) {
                                var exCol = exportTask.exportableColumns[i];
                                if (exCol) {
                                    var summaryObj = features['groupingsummary'].getSummary(this.store, exCol.summaryType, exCol.dataIndex, this.store.getGroups().map[gr.getGroupKey()]);
                                    var summaryVal = (summaryObj instanceof Object) && !(summaryObj instanceof Date) ? summaryObj[gr.getGroupKey()] : summaryObj;
                                    if (exCol.groupingSummaryConverter) {
                                        modelData[exCol.dataIndex] = exCol.groupingSummaryConverter(summaryVal, features['groupingsummary'].summaryData, exCol.dataIndex, this.lockable ? exCol.locked ? this.lockedGrid.view.cellValues : this.normalGrid.view.cellValues : this.view.cellValues);
                                    } else if (exCol.summaryRenderer) {
                                        modelData[exCol.dataIndex] = exCol.summaryRenderer(summaryVal, features['groupingsummary'].summaryData, exCol.dataIndex, this.lockable ? exCol.locked ? this.lockedGrid.view.cellValues : this.normalGrid.view.cellValues : this.view.cellValues);
                                    } else {
                                        modelData[exCol.dataIndex] = summaryVal;
                                    }
                                }
                            }
                            var summaryM = Ext.create('Ext.data.Model', modelData);
                            summaryM.isSummaryRecord = true;
                            renderRecords.push(summaryM);
                        }
                    }, this);
                }
            }
            if (features['summary']) {
                var modelData = {};
                for (var i = 0; i < exportTask.exportableColumns.length; i++) {
                    var exCol = exportTask.exportableColumns[i];
                    if (exCol) {
                        var summaryVal = features['summary'].getSummary(this.store, exCol.summaryType, exCol.dataIndex);
                        if (exCol.summaryConverter) {
                            modelData[exCol.dataIndex] = exCol.summaryConverter(summaryVal, features['summary'].summaryData, exCol.dataIndex, this.lockable ? exCol.locked ? this.lockedGrid.view.cellValues : this.normalGrid.view.cellValues : this.view.cellValues);
                        } else if (exCol.summaryRenderer) {
                            modelData[exCol.dataIndex] = exCol.summaryRenderer(summaryVal, features['summary'].summaryData, exCol.dataIndex, this.lockable ? exCol.locked ? this.lockedGrid.view.cellValues : this.normalGrid.view.cellValues : this.view.cellValues);
                        } else {
                            modelData[exCol.dataIndex] = summaryVal;
                        }
                    }
                }
                var summaryM = Ext.create('Ext.data.Model', modelData);
                summaryM.isSummaryRecord = true;
                features['summary'].dock === 'top' ? renderRecords.unshift(summaryM) : renderRecords.push(summaryM);
            }
            
            for (var i = 0; i < renderRecords.length; i++) {
                rows += '<row r="' + currentRow + '" customHeight="1" spans="1:' + exportTask.totalColumns + '">';
                var rec = renderRecords[i];
                var isSummaryRow = false;
                var isPlaceholder = rec.isPlaceholder ? rec.isPlaceholder : false;
                for (var j = 0; j < exportTask.exportableColumns.length; j++) {
                    if (rec.isGroupingHeader === true) {
                        if (j > 0) {
                            continue;
                        } else {
                            mergeCells.push(exportTask.alphabetColumns[0] + currentRow + ':' + exportTask.alphabetColumns[exportTask.exportableColumns.length - 1] + currentRow);
                        }
                    }

                    var param = rec.isGroupingHeader === true ? {dataIndex: 'groupingHeaderValue'} : exportTask.exportableColumns[j];
                    var type;
                    if (!rec.isSummaryRecord && !rec.isGroupingHeader) {
                        if (param.xtype === 'templatecolumn') {
                            type = 'template';
                        } else if (param.exportConverter) {
                            type = 'converter';
                        } else if (!param.skipRenderer && param.renderer) {
                            type = 'renderer';
                        } else {
                            type = rec.getField(param.dataIndex) ? this.getExportableColumnType(rec.getField(param.dataIndex).type) : this.defaultType;
                        }
                    } else {
                        type = 's';
                    }

                    switch (type) {
                        case 's':
                            var styleId = rec.isGroupingHeader === true ? 5 : rec.isSummaryRecord ? 6 : (j + this.staticStylesCount);
                            if (rec.get(param.dataIndex) != null) {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + styleId + '"><v>' + this.removeSpecials(Ext.util.Format.htmlEncode(String(rec.get(param.dataIndex)))) + '</v></c>';
                            } else {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + styleId + '"/>';
                            }
                            break;
                        case 'template':
                            rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + (j + this.staticStylesCount) + '"><v>' + this.removeSpecials(Ext.util.Format.htmlEncode(String(this.getView().getGridColumns()[param].tpl.apply(rec.data)))) + '</v></c>';
                            break;
                        case 'renderer':
                            var renderCol = exportTask.exportableColumns[j];
			    var renderView = this.lockable ? renderCol.locked ? this.lockedGrid.view : this.normalGrid.view : this.view;
                            renderView.cellValues.column = param;
                            var type = renderCol.type;
                            var renderedValue = param.renderer.call(renderCol.usingDefaultRenderer ? renderCol : renderCol.scope || renderView.ownerCt, rec.get(param.dataIndex), renderView.cellValues, rec, i, j, this.store, renderView);
                            if (renderedValue != null) {
                                renderedValue = this.removeSeparatorFromAmount(renderCol.type, renderedValue);
                                if ((renderedValue.indexOf('Total (') != -1 || renderedValue.indexOf('Grand total') != -1) || (!exportTask.isCollapsed && this.rowSubTotalsPosition == 'first' && isPlaceholder)) {
                                    isSummaryRow = true;
                                }
                                if (isSummaryRow) {
                                    if(type == 'string') {
                                        rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="5"><v>' + this.removeSpecials(Ext.util.Format.htmlEncode(String(renderedValue))) + '</v></c>';
                                    } else {
                                        rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" s="6"><v>' + this.removeSpecials(Ext.util.Format.htmlDecode(String(renderedValue))) + '</v></c>';	 //SDP-13431 : htmlDecode used to render correct currency for Grand Total in Excel Sheet.
                                    }
                                } else {
                                    if(type == 'string') {
                                        rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + (j + this.staticStylesCount) + '"><v>' + this.removeSpecials(Ext.util.Format.htmlEncode(String(renderedValue))) + '</v></c>';
                                    } else {
                                        rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" s="' + (j + this.staticStylesCount) + '"><v>' + this.removeSpecials(Ext.util.Format.htmlDecode(String(renderedValue))) + '</v></c>';	  //SDP-13431 : htmlDecode used to render correct currency for entries in Excel Sheet.
                                    }
                                }
                            }
                            break;
                        case 'converter':
                            var renderCol = exportTask.exportableColumns[j];
                            var renderView = this.lockable ? renderCol.locked ? this.lockedGrid.view : this.normalGrid.view : this.view;
                            renderView.cellValues.column = param;
                            var renderedValue = param.exportConverter(rec.get(param.dataIndex), renderView.cellValues, rec, i, j, this.store, renderView);
                            if (renderedValue != null) {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + (j + this.staticStylesCount) + '"><v>' + this.removeSpecials(Ext.util.Format.htmlEncode(String(renderedValue))) + '</v></c>';
                            } else {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="str" s="' + (j + this.staticStylesCount) + '"/>';
                            }
                            break;
                        default:
                            if (rec.get(param.dataIndex) != null) {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="' + type + '" s="' + (j + this.staticStylesCount) + '"><v>' + rec.get(param.dataIndex) + '</v></c>';
                            } else {
                                rows += '<c r="' + exportTask.alphabetColumns[currentCol++] + currentRow + '" t="' + type + '" s="' + (j + this.staticStylesCount) + '"/>';
                            }
                    }
                }
                rows += '</row>\n';
                if (exportTask.isCollapsed || (this.rowGrandTotalsPosition == 'first' && this.rowSubTotalsPosition == 'first') || (isPlaceholder && isSummaryRow) || (!isPlaceholder && isSummaryRow) || (!isPlaceholder && !isSummaryRow)) {
                    currentRow++;
                }
                currentCol = 0;
            }
            var columns = '';
            if (exportTask.style.styles.length > 0) {
                columns += '<cols>';
                for (var i = 0; i < exportTask.style.styles.length; i++) {
                    columns += '<col customWidth="1" width="' + exportTask.style.styles[i].width + '" max="' + (i + 1) + '" min="' + (i + 1) + '"/>';
                }
                columns += '</cols>';
            }
            var result = '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac" mc:Ignorable="x14ac">' +
                    '<dimension ref="A1:' + exportTask.alphabetColumns[exportTask.totalColumns - 1] + (currentRow - 1) + '"/>' +
                    '<sheetViews>' +
                    '<sheetView tabSelected="1" workbookViewId="0" />' +
                    '</sheetViews>' +
                    '<sheetFormatPr baseColWidth="10" defaultColWidth="9.140625" defaultRowHeight="15" x14ac:dyDescent="0.25" />' +
                    columns +
                    '<sheetData>' +
                    rows +
                    '</sheetData>';
            if (mergeCells.length > 0) {
                result += '<mergeCells count="' + mergeCells.length + '">';
                for (var i = 0; i < mergeCells.length; i++) {
                    result += '<mergeCell ref="' + mergeCells[i] + '"/>';
                }
                result += '</mergeCells>';
            }
            result += '</worksheet>';
            ws.file('sheet1.xml', result);
        },
        /**
         * Generates a sharedStrings.xml.
         * @param {type} xl
         * @param {type} exportTask
         * @returns {undefined}
         */
        generateSharedStrings: function (xl, exportTask) {
            var strings = '';
            for (var i = 0; i < exportTask.sharedStrings.length; i++) {
                strings += '<si><t>' + this.removeSpecials(Ext.util.Format.htmlEncode(exportTask.sharedStrings[i])) + '</t></si>';
            }
            xl.file('sharedStrings.xml', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="' + this.totalStrings + '" uniqueCount="' + exportTask.sharedStrings.length + '">' +
                    strings +
                    '</sst>');
        },
		/**
		* Removes special symbols from the string.
		*/
	removeSpecials: function (str) {
            var spec = /[\x00-\x08\x0E-\x1F\x7F]/g;
            var tab = /\x09/g;
            str = str.replace(spec, "");
            return str.replace(tab, "   ");
        },
        /**
         * Generates a styles.xml.
         * @param {type} xl
         * @param {type} exportTask
         * @returns {undefined}
         */
        generateStyles: function (xl, exportTask) {
            var fonts = '<fonts count="3">' +
                    //common font
                    '<font>' +
                    '<sz val="10"/>' +
                    '<name val="Arial"/>' +
                    '</font>' +
                    //Table name font
                    '<font>' +
                    '<b/>' +
                    '<sz val="18"/>' +
                    '<name val="Arial"/>' +
                    '</font>' +
                    //headers font
                    '<font>' +
                    '<b/>' +
                    '<sz val="10"/>' +
                    '<name val="Arial"/>' +
                    '</font>' +
                    '</fonts>';
            var fills = '<fills count="5">' +
                    '<fill>' +
                    '<patternFill patternType="none" />' +
                    '</fill>' +
                    '<fill>' +
                    '<patternFill patternType="gray125" />' +
                    '</fill>' +
                    '<fill>' +
                    '<patternFill patternType="solid">' +
                    '<fgColor rgb="FF' + this.xlsHeaderColor + '"/>' +
                    '<bgColor indexed="64"/>' +
                    '</patternFill>' +
                    '</fill>' +
                    '<fill>' +
                    '<patternFill patternType="solid">' +
                    '<fgColor rgb="FF' + this.xlsGroupHeaderColor + '"/>' +
                    '<bgColor indexed="64"/>' +
                    '</patternFill>' +
                    '</fill>' +
                    '<fill>' +
                    '<patternFill patternType="solid">' +
                    '<fgColor rgb="FF' + this.xlsSummaryColor + '"/>' +
                    '<bgColor indexed="64"/>' +
                    '</patternFill>' +
                    '</fill>' +
                    '</fills>';
            var colStyles = '';
            var numFmtId = 500;
            var numFmtStyles = '';
            var nfsCount = 0;
            for (var i = 0; i < exportTask.style.styles.length; i++) {
                var tmpNumFmtId = 0;
                if (exportTask.style.styles[i].numFmt != null) {
                    nfsCount++;
                    numFmtStyles += '<numFmt formatCode="' + exportTask.style.styles[i].numFmt + '" numFmtId="' + numFmtId + '"/>';
                    tmpNumFmtId = numFmtId;
                    numFmtId++;

                }
                colStyles += '<xf borderId="1" fillId="0" fontId="0" numFmtId="' + tmpNumFmtId + '" xfId="0">' +
                        '<alignment wrapText="1" vertical="center" horizontal="' + exportTask.style.styles[i].align + '"/>' +
                        '</xf>';
            }
            if (numFmtStyles.length > 0) {
                numFmtStyles = '<numFmts count="' + nfsCount + '">' + numFmtStyles + '</numFmts>';
            }
            xl.file('styles.xml', '<?xml version="1.0" encoding="UTF-8"?>' +
                    '<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac" mc:Ignorable="x14ac">' +
                    numFmtStyles +
                    fonts +
                    fills +
                    '<borders count="3">' +
                    '<border>' +
                    '<left />' +
                    '<right />' +
                    '<top />' +
                    '<bottom />' +
                    '<diagonal />' +
                    '</border>' +
                    '<border>' +
                    '<left style="thin">' +
                    '<color auto="1"/>' +
                    '</left>' +
                    '<right style="thin">' +
                    '<color auto="1"/>' +
                    '</right>' +
                    '<top style="thin">' +
                    '<color auto="1"/>' +
                    '</top>' +
                    '<bottom  style="thin">' +
                    '<color auto="1"/>' +
                    '</bottom>' +
                    '<diagonal/>' +
                    '</border>' +
                    '<border>' +
                    '<left style="thin">' +
                    '<color auto="1"/>' +
                    '</left>' +
                    '<right style="thin">' +
                    '<color auto="1"/>' +
                    '</right>' +
                    '<top style="thick">' +
                    '<color auto="1"/>' +
                    '</top>' +
                    '<bottom  style="thick">' +
                    '<color auto="1"/>' +
                    '</bottom>' +
                    '<diagonal/>' +
                    '</border>' +
                    '</borders>' +
                    '<cellStyleXfs count="1">' +
                    '<xf numFmtId="0" fontId="0" fillId="0" borderId="0" />' +
                    '</cellStyleXfs>' +
                    '<cellXfs count="' + (exportTask.style.styles.length + this.staticStylesCount) + '">' +
                    '<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" />' +
                    //Title style
                    '<xf borderId="1" fillId="0" fontId="1" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="center"/>' +
                    '</xf>' +
                    //Header align right
                    '<xf borderId="1" fillId="2" fontId="2" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="right"/>' +
                    '</xf>' +
                    //Header align center
                    '<xf borderId="1" fillId="2" fontId="2" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="center"/>' +
                    '</xf>' +
                    //Header align left
                    '<xf borderId="1" fillId="2" fontId="2" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="left"/>' +
                    '</xf>' +
                    //Summary
                    '<xf borderId="2" fillId="4" fontId="2" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="left"/>' +
                    '</xf>' +
                    //Summary
                    '<xf borderId="2" fillId="4" fontId="2" numFmtId="0" xfId="0">' +
                    '<alignment wrapText="1" vertical="center" horizontal="right"/>' +
                    '</xf>' +
                    colStyles +
                    '</cellXfs>' +
                    '<cellStyles count="1">' +
                    '<cellStyle name="Standard" xfId="0" builtinId="0" />' +
                    '</cellStyles>' +
                    '<dxfs count="0" />' +
                    '<tableStyles count="0" defaultTableStyle="TableStyleMedium2" defaultPivotStyle="PivotStyleMedium9" />' +
                    '<extLst>' +
                    '<ext xmlns:x14="http://schemas.microsoft.com/office/spreadsheetml/2009/9/main" uri="{EB79DEF2-80B8-43e5-95BD-54CBDDF9020C}">' +
                    '<x14:slicerStyles defaultSlicerStyle="SlicerStyleLight1" />' +
                    '</ext>' +
                    '<ext xmlns:x15="http://schemas.microsoft.com/office/spreadsheetml/2010/11/main" uri="{9260A510-F301-46a8-8635-F512D64BE5F5}">' +
                    '<x15:timelineStyles defaultTimelineStyle="TimeSlicerStyleLight1" />' +
                    '</ext>' +
                    '</extLst>' +
                    '</styleSheet>');
        }
    }
});
