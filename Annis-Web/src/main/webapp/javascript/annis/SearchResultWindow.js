function checkIFrameLoaded(id)
{
  var elemBody = Ext.get(id);
  var dom = elemBody.child('iframe', true);
  if(dom != null)
  {
    if(dom.src.match("empty.html$") !== "empty.html") // does not end with
    {
      var doc = dom.contentWindow.document;

      if(doc.contentType != null &&doc.contentType.match("^image/") == "image/")
      {
        if(doc.getElementsByTagName('img').length !== 0)
        {
          var img = doc.getElementsByTagName('img')[0];
          dom.height = img.naturalHeight;
        }
      }
      else if(doc.body.scrollHeight > 20)
      {
        //adjust iframe size
        dom.height = doc.body.scrollHeight + 25;
      }
    }
  }
}

function toggleRowBody(id) {
  var elemBody = Ext.get('annotation-'+ id + '-body');
  var elemSelector = Ext.get('annotation-'+ id + '-selector');
  try {
    var elem = elemBody.child('iframe');
    var dom = elemBody.child('iframe', true);
    // only re-set source if not already loaded
    if(dom.src.match("empty.html$") == "empty.html") // ends with
    {
      dom.src = elem.getAttributeNS('annis', 'src');
    }

  } catch (e)
  {
    alert('toggleRowBody (id=' + id + '): ' + e);
  }
  elemBody.setDisplayed(elemBody.isDisplayed() ? false : 'block');
  elemSelector.toggleClass('annis-level-selector-expanded');
}

function addNewSliders(rowData)
{
  // add sliders
  var slider = new Ext.Slider({
    id: 'slider-comp-' + rowData.callbackId,
    renderTo: 'slider-' + rowData.callbackId,
    width: 100, // will be set by resize event of window,
    hidden: false, // se above
    minValue: 0,
    maxValue: 100, // see above
    value: 0,
    listeners : {
      drag : function()
      {
        scrollToSliderPosition(slider, rowData.callbackId);
      },
      changecomplete : function()
      {
        scrollToSliderPosition(slider, rowData.callbackId);
      }
    }
  });

  columnWidth = Ext.get("kwic-" + rowData.callbackId).getWidth();
  adjustSingleSlider(rowData.callbackId, columnWidth);
}

function adjustSingleSlider(callbackId, columnWidth)
{
  columnWidth = columnWidth -15;
  var sliderNode = Ext.ComponentMgr.get("slider-comp-" + callbackId);
  if(sliderNode != null)
  {
    var tableWidth = Ext.get("table-" + callbackId).getWidth();
    
    sliderNode.setWidth(columnWidth);
    sliderNode.setVisible(tableWidth > columnWidth ? true :false);
    var maxVal = tableWidth - columnWidth;
    sliderNode.setMaxValue(maxVal);

    var newSliderVal = sliderNode.getValue;
    newSliderVal = Math.min(0, newSliderVal);
    newSliderVal = Math.max(maxVal, newSliderVal);

    sliderNode.setValue(newSliderVal);
    scrollToSliderPosition(sliderNode, callbackId);
  }
}

function adjustAllSliders(width)
{
  // adjust all sliders
  var sliderNodes = Ext.query("*[id^=slider-comp-]");
  for(var i=0; i < sliderNodes.length; i++)
  {
    var rawId = sliderNodes[i].id;
    var callbackId = rawId.replace("slider-comp-", "");

    if(width == null)
    {
      width = Ext.get("gridSearchResult").getWidth();
    }

    adjustSingleSlider(callbackId, width - 50); // 30 is size of the other column
  }
}

function scrollToSliderPosition(slider, callbackId)
{
  Ext.get("kwic-" + callbackId).scrollTo('left', slider.getValue());
}

Ext.onReady(function() {

  var tokenLevelCheckstate = {};
  function onTokenLevelCheck(item, checked) {
    setKWICTokenLevelVisibility(item.getItemId(), checked);
    tokenLevelCheckstate[item.getItemId()] = checked;
    adjustAllSliders(null);
  }
  function setKWICTokenLevelVisibility(id, checked)
  {
    var elements = Ext.query('tr[class~=' + id + ']');
    Ext.each(elements, function(element) {
      element.style.display = (checked) ? 'table-row' : 'none';
    });
  }
  var tokenLevelSelectionMenu = new Ext.menu.Menu({
    id: 'mainMenu',
    items: [ ]
  }); // end tokenLevelSelectionMenu


  /** The JsonStore for the search result */
  var storeSearchResult = new Ext.data.JsonStore({
    proxy: new Ext.data.HttpProxy({
      url: conf_context + '/secure/SearchResult',
      timeout: global_timeout
    }),

    id: 'storeSearchResult',
    root: 'resultSet',
    totalProperty: 'totalCount',
    fields: ['id', 'callbackId', 'textIdList', 'token', 'tokenNamespaces', 'visualizer', 'corpusId', 'marker', 'markerExact'],

    // turn on remote sorting
    remoteSort: true,
    listeners : {

      'load' : function()
      {
        // remove all menu items (state is preserved in tokenLevelCheckstate
        tokenLevelSelectionMenu.removeAll();
        
        var isRTL = false;
        
        this.each(function(record)
        {
          for(var j=0; j < record.data.token.length; j++)
          {
            var text = record.data.token[j].text;
            // check if left to right or right to left text order
            for(var i=0;!isRTL && i < text.length; i++)
            {
              var cc = text.charCodeAt(i);
              // hebrew extended and basic, arabic basic and extendend
              if(cc >= 1425 && cc <=1785)
              {
                isRTL = true;
              }
              // alphabetic presentations forms (hebrwew) to arabic presentation forms A
              else if(cc >= 64286 && cc <= 65019)
              {
                isRTL = true;
              }
              // arabic presentation forms B
              else if(cc >= 65136 && cc <= 65276)
              {
                isRTL = true;
              }
            }
          }

          //lets update the token level selection button
          Ext.each(record.data.tokenNamespaces, function(item) 
          {

            var levelId = item.replace(':','_');

            if(tokenLevelCheckstate[levelId] == null)
            {
              tokenLevelCheckstate[levelId] = true;
            }

            if(tokenLevelSelectionMenu.findById(levelId) == null)
            {
              setKWICTokenLevelVisibility(levelId, tokenLevelCheckstate[levelId]);
              tokenLevelSelectionMenu.addItem(new Ext.menu.CheckItem({
                id: 		levelId,
                text: 		item,
                checked: 	tokenLevelCheckstate[levelId],
                checkHandler: onTokenLevelCheck
              }));
            }
          });

          addNewSliders(record.data);

        }); // end for each record

        if(isRTL)
        {
          // patch style attribute
          var elements = Ext.query('*.SearchResultWindow.kwic');
          Ext.each(elements, function(e)
          {
            e.dir = "RTL";
            e.style.cssFloat="right";
          });
        }
      }
    }
  }); // end storeSearchResult

  /** SearchResultWindow class */
  SearchResultWindow = Ext.extend(Ext.Window,
  {
    constructor : function(id)
    {
      var PAGE_SIZE = 25;

      var pagingToolbar = new Ext.PagingToolbar({
        pageSize: PAGE_SIZE,
        store: storeSearchResult,
        displayInfo: true,
        displayMsg: 'Displaying Results {0} - {1} of {2}',
        emptyMsg: "No Results to display.",
        items: [
        {
          text:'Token Annotations',
          xtype: 'button',
          ctCls: 'annis-toolbar-btn',
          menu: tokenLevelSelectionMenu
        },
        {
          id: 'btnShowCitation',
          text: 'Show Citation URL',
          ctCls: 'annis-toolbar-btn',
          disabled: false,
          listeners: {
            click: function() {
              Ext.Msg.alert('Citation', '<textarea readonly="f" wrap="virtual" rows="5" cols="60">' + Citation.generate() + "</textarea>");
            }
          }
        }
        ]
      }); // end pagingToolbar

      var cmItems = [];

      cmItems.push({
        id: 'actions',
        renderer: this.renderActions,
        scope: this,
        width: 30
      }); // end cmItems:match

      cmItems.push({
        id: 'kwic',
        renderer: this.renderKWIC,
        scope: this
      }); // end cmItems:match

      var cm = new Ext.grid.ColumnModel({
        columns: cmItems,
        defaults: {
          sortable : false
        }
      });

      var gridViewSearchResult = new Ext.grid.GridView({
        focusCell: Ext.emptyFn
      });


      var gridSearchResult = new Ext.grid.GridPanel({
        header: false,
        store: storeSearchResult,
        id: 'gridSearchResult',
        cm: cm,
        view: gridViewSearchResult,
        viewConfig: {
          enableRowBody: true
        },
        loadMask: true,
        collapsible: true,
        animCollapse: false,
        trackMouseOver:false,
        enableColumnMove: false,
        enableColumnResize: false,
        autoScroll : true,
        autoExpandColumn: 'kwic',
        tbar: pagingToolbar,
        disableSelection: true,
        listeners : {
          bodyresize : function(p, width, height) {
            adjustAllSliders(width);
          }
        }
      }); // end gridSearchResult

      config =
      {
        hidden: true,
        applyTo : id,
        title: 'Search Results',
        id: id,
        closable:true,
        maximizable: true,
        width: (Ext.getBody().getViewSize().width - windowSearchFormWidth - 25),
        height:Ext.getBody().getViewSize().height - 35,
        //border:false,
        plain:true,
        layout: 'fit',
        closeAction: 'hide',
        items: [
        gridSearchResult
        ]
      }; // end config

      // superclass constructor
      SearchResultWindow.superclass.constructor.call(this, config);

    },
    renderKWIC : function(value, metadata, record, rowIndex, colIndex, store)
    {
      // the column model does not correspondend to our store model, get the
      // store row data "manually""
      var row = store.getAt(rowIndex);
      var rowData = row.data; // this is the JSON result set we are sending from the frontend


      var output = '';

      output += '<div id="kwic-' + rowData.callbackId + '" class="SearchResultWindow kwic">\n';
      output += '<table id="table-' + rowData.callbackId + '">\n';

      for(var i=0; i < rowData.textIdList.length; i++)
      {
        var tid = rowData.textIdList[i];

        output += this.appendAllToken(rowData, tid);
        output += this.appendAllAnnotations(rowData, tid);
      }

      output += '</table>\n';
      output += '</div>\n';
      
      output += '<div id="slider-' + rowData.callbackId + '" ></div>\n';

      output += '<div class="SearchWindowResult visualizers" style="clear:both;" >\n';
      output += this.appendVisualizers(rowData);
      output += '</div>\n';

      return output;
    },
    appendAllToken : function(rowData, textId)
    {
      var lastTokenIndex = -1;
      var lastTokenIndexWasSet = false;

      var output = '<tr>\n';
      for(var i=0; i < rowData.token.length; i++)
      {
        if((rowData.token[i].textId*1) == textId)
        {
          var tokenIndex = rowData.token[i].tokenIndex;
          
          if(lastTokenIndexWasSet && ((tokenIndex - lastTokenIndex) > 1))
          {
            // insert empty token as an indicator for "match islands"
            output += this.appendPlaceholder(rowData.tokenNamespaces.length);
          }

          lastTokenIndex = tokenIndex;
          lastTokenIndexWasSet = true;

          output += this.appendSingleToken(rowData.token[i], rowData.marker);
        }
      }
      output += '</tr>';
      return output;
    },
    appendSingleToken : function(token, marker)
    {
      var output =  '';
      if(marker[token.id] == null || marker[token.id] == "")
      {
        output += '<td>';
      }
      else
      {
        output += '<td class="SearchResultWindow token hit" style="color:'
          + marker[token.id]
          +'">';
      }
      if(token != null && token.text != null)
      {
        output += token.text;
      }
      output += '</td>\n';
      return output;
    },
    appendPlaceholder : function(annotationCount)
    {
      return '<td rowspan=\"' + (annotationCount+1) + "\">(...)</td>\n";
    },
    appendAllAnnotations : function(rowData, textId)
    {
      var output = '';
      for(var i=0; i < rowData.tokenNamespaces.length; i++)
      {
        var key = rowData.tokenNamespaces[i];
        output += this.appendAnnotationType(key, rowData, textId);
      }

      return output;
    },
    appendAnnotationType : function(key, rowData, textId)
    {
      var output = '<tr class="' + key.replace(':', '_') + '" >\n';
      for(var i=0; i < rowData.token.length; i++)
      {
        if(rowData.token[i].textId == textId)
        {
          output += this.appendSingleAnnotation(rowData.token[i].annotations[key], key);
        }
      }
      output += '</tr>\n';
      return output;
    },
    appendSingleAnnotation : function(anno, tokenLevel)
    {
      var output = '<td ext:qtip="' + tokenLevel  + '" class="SearchResultWindow annotation" >';
      if(anno != null && anno.value != null)
      {
        output += anno.value;
      }
      output += '</td>\n';
      return output;
    },
    appendVisualizers : function(rowData)
    {
      var output = '';

      var markerString = {};
      var seen = {};
      var color = 0;
      for(key in rowData.marker)
      {
        if(rowData.marker[key] != null && rowData.marker[key] != '')
        {
          color = rowData.marker[key];
          if(seen[color] != null)
          {
            markerString[color] += ',';
          }
          else
          {
            markerString[color] = '';
          }
          seen[color] = true;
          markerString[color] += key;   
        }
      }

      var markerParam = '';
      for(c in markerString)
      {
        markerParam += '&mark:' + c + '=' + markerString[c];
      }

      var markerExactString = {};
      seen = {};
      for(key in rowData.markerExact)
      {
        if(rowData.markerExact[key] != null && rowData.markerExact[key] != '')
        {

          color = rowData.markerExact[key];
          if(seen[color] != null)
          {
            markerExactString[color] += ',';
          }
          else
          {
            markerExactString[color] = '';
          }
          seen[color] = true;
          markerExactString[color] += key;

        }
      }

      var markerExactParam = '';
      for(c in markerExactString)
      {
        markerExactParam += '&markExact:' + c + '=' + markerExactString[c];
      }

      
      for(var i=0; i < rowData.visualizer.length; i++)
      {
        var vis = rowData.visualizer[i];

        var idcommon = rowData.callbackId + '-' + vis.id;

        output += '<div id="annotation-' + idcommon + '-selector" class="annis-level-selector-collapsed"'
                + ' onclick="toggleRowBody(\'' + idcommon + '\');">'
                + vis.displayname
                + '</div>';
              
        output += '<div id="annotation-' + idcommon + '-body" class="annis-level-body">';
        output += '<iframe onload="checkIFrameLoaded(\'annotation-' + idcommon + '-body\')" width="100%" height="20px" frameborder="0" src="'
            + conf_context + '/empty.html" annis:src="'
            + conf_context + '/secure/Visualizer?callbackId=' + rowData.callbackId
            + "&visId=" + vis.id
            + '&textId=' + rowData.textIdList[0]
            + '&namespace=' + vis.namespace
            + '&vistype=' + vis.vistype
            + markerParam
            + markerExactParam
            + '" ></iframe>' +
    '						</div>\n';
      }
      return output;
    },
    renderActions : function (value, metadata, record, rowIndex, colIndex, store)
    {
      // the column model does not correspondend to our store model, get the
      // store row data "manually""
      var row = store.getAt(rowIndex);
      var rowData = row.data; // this is the JSON result set we are sending from the frontend

      var id = rowData.corpusId;
      
      var action = 'new MetaDataWindow(' + id + ', \'Search Result\').show();';

      var output = '<a href="#" onclick="' + action + '"><img src="' + conf_context + '/images/info.gif"></a>';

      return output;
    }
  }); // end SearchResultWindow

  var windowSearchResult = new SearchResultWindow('windowSearchResult');

});