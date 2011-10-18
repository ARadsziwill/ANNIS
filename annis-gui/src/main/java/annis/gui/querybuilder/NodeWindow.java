/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.querybuilder;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class NodeWindow extends Panel implements Button.ClickListener
{
  
  public static final int HEIGHT=100;
  public static final int WIDTH=200;

  private TigerQueryBuilder parent;
  private Button btEdge;
  private Button btAdd;
  private Button btClear;
  private Button btClose;
  private HorizontalLayout toolbar;
  private List<ConstraintLayout> constraints;
  private boolean prepareEdgeDock;
  private int id;

  public NodeWindow(int id, TigerQueryBuilder parent)
  {
    this.parent = parent;
    this.id = id;

    constraints = new ArrayList<ConstraintLayout>();

    setWidth("99%");
    setHeight("99%");

    prepareEdgeDock = false;

    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);

    toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    addComponent(toolbar);

    btEdge = new Button("Edge");
    btEdge.setStyleName(ChameleonTheme.BUTTON_LINK);
    btEdge.addListener((Button.ClickListener) this);
    toolbar.addComponent(btEdge);
    btAdd = new Button("Add");
    btAdd.setStyleName(ChameleonTheme.BUTTON_LINK);
    btAdd.addListener((Button.ClickListener) this);
    toolbar.addComponent(btAdd);
    btClear = new Button("Clear");
    btClear.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btClear);

    btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_LINK);
    btClose.addListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);

    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
  }

  public void setPrepareEdgeDock(boolean prepare)
  {
    this.prepareEdgeDock = prepare;

    btClear.setVisible(!prepare);
    btClose.setVisible(!prepare);
    btAdd.setVisible(!prepare);

    if(prepare)
    {
      btEdge.setCaption("Dock");
    }
    else
    {
      btEdge.setCaption("Edge");
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btEdge)
    {
      if(prepareEdgeDock)
      {
        setPrepareEdgeDock(false);
        parent.addEdge(this);
      }
      else
      {
        parent.prepareAddingEdge(this);
        setPrepareEdgeDock(true);
        btEdge.setCaption("Cancel");
      }
    }
    else if(event.getButton() == btClose)
    {
      parent.deleteNode(this);
    }
    else if(event.getButton() == btAdd)
    {
      ConstraintLayout c = new ConstraintLayout();
      c.setWidth("100%");
      c.setHeight("-1px");
      constraints.add(c);
      addComponent(c);
    }
  }

  public int getID()
  {
    return id;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final NodeWindow other = (NodeWindow) obj;
    return other.getID() == getID();
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 41 * hash + this.id;
    return hash;
  }

  public static class ConstraintLayout extends HorizontalLayout implements LayoutClickListener
  {

    private ComboBox cbKey;
    private ComboBox cbOperator;
    private TextField txtValue;

    public ConstraintLayout()
    {
      
      setWidth("100%");
      
      cbKey = new ComboBox();
      cbKey.setNewItemsAllowed(true);
      cbKey.setNewItemHandler(new DoNotAddNewItemHandler(cbKey));
      cbKey.setImmediate(true);
      
      cbOperator = new ComboBox();
      cbOperator.setNewItemsAllowed(true);
      cbOperator.setNewItemHandler(new DoNotAddNewItemHandler(cbOperator));
      cbOperator.setImmediate(true);

      txtValue = new TextField();

      cbOperator.setWidth("50px");
      cbKey.setWidth("100%");
      txtValue.setWidth("100%");

      addComponent(cbKey);
      addComponent(cbOperator);
      addComponent(txtValue);

      setExpandRatio(cbKey, 1.0f);
      setExpandRatio(txtValue, 1.0f);
      
      addListener((LayoutClickListener) this);
      
    }

    @Override
    public void layoutClick(LayoutClickEvent event)
    {
      Component c = event.getClickedComponent();
      if(c != null && c instanceof AbstractField)
      {
        AbstractField f = (AbstractField) c;
        f.focus();
      }
    }
  }
  public static class DoNotAddNewItemHandler  implements NewItemHandler  
  {

    private ComboBox comboBox;

    public DoNotAddNewItemHandler(ComboBox comboBox)
    {
      this.comboBox = comboBox;
    }
    
    
    
    @Override
    public void addNewItem(String newItemCaption)
    {
      if(comboBox != null)
      {
        comboBox.addItem(newItemCaption);
        comboBox.setValue(newItemCaption);
        
      }
    }
  }

}
