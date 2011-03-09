Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders =
  {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  MetaDataWindow = function(id)
  {
    var config = {};

    var grid = null;

    // init config
    config.title = 'Meta Data for id ' + id;
    config.width = 400;
    config.height = 200;

    // get datastore
    var storeMeta = new Ext.data.JsonStore(
    {
      url : conf_context + '/secure/MetaData?mID=' + id,
      totalProperty : 'size',
      root : 'metadata',
      id : 'id',
      fields : [ 'key', 'value' ],
      // turn on remote sorting
      remoteSort : true
    });

    var colModel = new Ext.grid.ColumnModel([
    {
      header : "Name",
      dataIndex : 'key'
    },

    {
      header : "Value",
      dataIndex : 'value'
    } ]);

    var gridMeta = new Ext.grid.GridPanel(
    {
      ds : storeMeta,
      cm : colModel,
      loadMask : true,
      viewConfig :
      {
        forceFit : true,
        autoFill : true
      },
      autoHeight : true,
      autoWidth : true
    });

    var storeNodeAttributes = new Ext.data.JsonStore(
    {
      url : conf_context + '/secure/AttributeList?noprefix',
      // turn on remote sorting
      remoteSort : false,
      fields : [ 'name', 'values' ]
    });

    var colModelAttribute = new Ext.grid.ColumnModel([
    {
      header : "Name",
      dataIndex : "name"
    },
    {
      header : "Value",
      dataIndex : "values"
    } ]);

    var gridAttribute = new Ext.grid.GridPanel(
    {
      ds : storeNodeAttributes,
      cm : colModelAttribute,
      loadMask : true,
      viewConfig :
      {
        forceFit : true,
        autoFill : true
      },
      autoHeight : true,
      autoWidth : true
    });

    storeMeta.load();
    storeNodeAttributes.setDefaultSort('name', 'asc');
    storeNodeAttributes.load(
    {
      params :
      {
        corpusIdList : '',
        type : 'node'
      }
    });

    config.items = [ gridMeta, gridAttribute ];
    config.autoScroll = true;
    config.layout = 'fit';

    MetaDataWindow.superclass.constructor.call(this, config);
  };

  Ext.extend(MetaDataWindow, Ext.Window);
});