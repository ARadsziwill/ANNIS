Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders = {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  //helper functions
  function killNameSpaces(name) 
  {    
    var edgeNameArray = name.split(":");
    return edgeNameArray[edgeNameArray.length - 1];
  }

  function isAmbiguous(store, records, field)
  {
    var names = {};
    var i;
    for(i=0; i < records.length; i++)
    {
      var n = killNameSpaces(records[i].get(field));
      if(n)
      {
        if(names[n] === true)
        {
          return true;
        }
        else
        {
          names[n] = true;
        }
      }
    }
    return false;
  }

  function hasDominanceEdges(records)
  {
    var i;
    for(i=0; i < records.length; i++)
    {
      var r = records[i];
      if(r.get("type") === 'edge' && r.get("subtype") === 'd')
      {
        return true;
      }
    }
    return false;
  }

  // filter function for edge types
  var edge_names = {};
  function edgeNames(record, id)
  {
    
    if (record.get("edge_name"))
    {
      var eName = record.get('edge_name');
      
      if(Ext.util.Format.trim(eName) !== '')
      {
        record.set('name', eName);
      }
      record.commit();

      if(edge_names.eName === true)
      {
        return false;
      }

      edge_names[eName] = true;
      return true;
    }
    return false;
  }
  
  //render functions
  function nameRenderer(value, metadata, record, rowIndex, colIndex, store)
  {
    if(isAmbiguous(store, store.getRange(), 'name') === true)
    {
      return value;
    }
    else
    {
      return killNameSpaces(value);
    }
  }

  function readableExample(value, metadata, record, rowIndex, colIndex, store)
  {
    return '<p style=\'white-space: normal\'>' + killNameSpaces(record.get('name')) + "=\""
        + value + "\"</p>";
  }

  function edgeAnnotation(value, metadata, record, rowIndex, colIndex, store)
  {    
    var operator = (record.get('subtype') === "d") ? '>' : '->' + killNameSpaces(record.get('edge_name'));
    return '<p style=\'white-space: normal\'> node & node & #1 ' + operator
        + '[' + killNameSpaces(record.get('name')) + "=\"" + record.get('values')
        + "\"] #2</p>";
  }

  function edgeTypes(value, metadata, record, rowIndex, colIndex, store)
  {
    var operator = (record.get('subtype') === "d") ? '>' : '->';
    return '<p style=\'white-space: normal\'> node & node & #1 ' + operator + 
        killNameSpaces(record.get('edge_name'))
        + " #2</p>";
  }

  MetaDataWindow = function(id, name)
  {
    
    var hideAttr = (name === "Search Result") ? true : false;

    var config = {};
    config.title = 'Meta Data for ' + name;
    config.width = (!hideAttr) ? 800 : 400;
    config.height = 402;

    var grid = null;

    // needed for selecting content of grids
    var selectableCell = new Ext.Template(
        '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
        '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
        '</td>'
     );
    
    // get datastore
    var storeMeta = new Ext.data.JsonStore({
      url : conf_context + '/secure/MetaData?mID=' + id,
      totalProperty : 'size',
      root : 'metadata',
      id : 'id',
      fields : [ 'key', 'value' ],
      // turn on remote sorting
      remoteSort : true
    });

    var colModel = new Ext.grid.ColumnModel([ {
      header : "Name",
      dataIndex : 'key'
    }, {
      header : "Value",
      dataIndex : 'value',
      renderer : function(value)
      {
        var css = 'white-space: normal; overflow: normal; padding-right: 5px';
        return '<p style=\'' + css + '\'>' + value + '</p>';
      }
    } ]);

    var gridMeta = new Ext.grid.GridPanel({
      ds : storeMeta,
      cm : colModel,
      title : 'meta data',
      loadMask : true,
      viewConfig : {
        forceFit : true,
        autoFill : true,
        templates : {
          cell : selectableCell
        }
      },
      height : 370,
      flex : 1
    });

    storeMeta.load();
    var gridPanel = {};

    if (!hideAttr)
    {

      // height of accordion components
      var height = 300;

      var storeAttributes = new Ext.data.JsonStore({
        url : conf_context + '/secure/AttributeList?corpusIds=' + id,
        // turn on remote sorting
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ]
      });

      var storeNodeAnnotations = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId: 'storeNodeAnnotations'
      });

      var colModelNodeAnnotations = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer: nameRenderer
      }, {
        header : "example",
        dataIndex : "values",
        renderer : readableExample
      } ]);

      var gridNodeAnnotations = new Ext.grid.GridPanel({
        ds : storeNodeAnnotations,
        cm : colModelNodeAnnotations,
        loadMask : true,
        title : 'node annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          templates : {
            cell : selectableCell
          }
        },
        autoWidth : true,
        height : height
      });

      var storeEdgeAnnotations = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId: 'storeEdgeAnnotations'
      });

      var colModelEdgeAnnotation = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer: nameRenderer
      }, {
        header : "example",
        dataIndex : "values",
        renderer : edgeAnnotation
      } ]);

      var gridEdgeAnnotations = new Ext.grid.GridPanel({
        ds : storeEdgeAnnotations,
        cm : colModelEdgeAnnotation,
        loadMask : true,
        title : 'edge annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          templates : {
            cell : selectableCell
          }
        },
        autoWidth : true,
        height : height
      });

      var storeEdgeTypes = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId: 'storeEdgeTypes'
      });

      var colEdgeTypes = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer: nameRenderer
      }, {
        header : "example",
        dataIndex : "edge_name",
        renderer : edgeTypes
      } ]);

      var gridEdgeTypes = new Ext.grid.GridPanel({
        ds : storeEdgeTypes,
        cm : colEdgeTypes,
        loadMask : true,
        title : 'edge types',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          templates : {
            cell : selectableCell
          }
        },
        autoWidth : true,
        height : height
      });

      storeAttributes.setDefaultSort('name', 'asc');
      storeAttributes.on("load", function(store, records, options)
      {

        if(hasDominanceEdges(records))
        {
          var a = [];
          a[0] = '';
          var domEdgeType = {};
          domEdgeType.name = '(dominance)';
          domEdgeType.type = 'edge';
          domEdgeType.subtype = 'd';
          domEdgeType.edge_name = ' ';
          domEdgeType.values = a;
          var domEdgeTypeRecord = new storeEdgeTypes.recordType(domEdgeType, Ext.id());
          
          storeEdgeTypes.add(domEdgeTypeRecord);
        }

        // copy and filter for the several annotation-stores
        storeEdgeAnnotations.add(store.getRange(0, store.getCount()));
        storeEdgeTypes.add(store.getRange(0, store.getCount()));
        storeNodeAnnotations.add(store.getRange(0, store.getCount()));
        
        var knownEdgeAnnotations = {};
        storeEdgeAnnotations.filterBy(function(record, id){
          if(record.get("type") === 'edge')
          {
            var annoName = record.get('subtype')
              + '.' + record.get('edge_type') + '.' + record.get("name");
            if(knownEdgeAnnotations[annoName] !== true)
            {
              knownEdgeAnnotations[annoName] = true;
              return true;
            }
          }
          return false;
        });
        storeEdgeTypes.filterBy(edgeNames);

        storeNodeAnnotations.filter("type", "node", false, true);
        
      }); // end on load storeAttributes

      storeAttributes.load();
      

      rightPanel = new Ext.Panel({
        layout : 'accordion',
        title : 'available annotations',
        layoutConfig : {
          animate : true
        },
        items : [ gridNodeAnnotations, gridEdgeTypes, gridEdgeAnnotations ],
        flex : 1
      });
    }

    // init config
    config.items = (!hideAttr) ? [ gridMeta, rightPanel ] : [ gridMeta ];
    config.autoScroll = true;
    config.layout = {
      type : 'hbox'
    };
    config.shadow = false;
    config.resizable = false;

    MetaDataWindow.superclass.constructor.call(this, config);
  };

  Ext.extend(MetaDataWindow, Ext.Window);
});