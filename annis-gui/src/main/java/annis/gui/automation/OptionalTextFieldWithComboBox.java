/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.automation;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Andreas
 */
public class OptionalTextFieldWithComboBox extends CustomField<String>
{
  
  private final ComboBox comboBox;
  private final CheckBox checkBox;
  private final VerticalLayout layout;
  
  public OptionalTextFieldWithComboBox(String checkBoxCaption,
    String comboBoxCaption, Property enabledDataSource)
  {
    comboBox = new ComboBox(checkBoxCaption);
    comboBox.setTextInputAllowed(false);
    comboBox.setNewItemsAllowed(false);
    comboBox.setImmediate(true);
    
    checkBox = new CheckBox(comboBoxCaption, enabledDataSource);
    checkBox.addValueChangeListener(new ValueChangeListener()
    {
      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        if (Objects.equals(event.getProperty().getValue(), Boolean.TRUE))
        {
          if(getValue() == null)
          {
            // only set something if changed
            setValue("");
            
          }
        }
        else
        {
          if(getValue() != null)
          {
            // only set something if changed
            setValue(null);
          }
        }
      }
    });
    
    layout = new VerticalLayout(checkBox, comboBox);
  }

  @Override
  protected Component initContent()
  {
    return layout;
  }

  @Override
  public void setInternalValue(String newValue)
  {
    super.setInternalValue(newValue);
    comboBox.setEnabled(newValue != null);
    checkBox.setValue(newValue != null);
  }
  
  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    super.setPropertyDataSource(newDataSource);
    comboBox.setPropertyDataSource(newDataSource);
  }
  
  @Override
  public Class<? extends String> getType()
  {
    return String.class;
  }
}
