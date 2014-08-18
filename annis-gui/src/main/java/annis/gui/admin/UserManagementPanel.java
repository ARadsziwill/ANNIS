/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin;

import annis.gui.admin.view.UserManagementView;
import annis.security.User;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class UserManagementPanel extends Panel
  implements UserManagementView
{

  private final VerticalLayout layout;

  private final Table userList;

  private final BeanContainer<String, User> userContainer;

  private final List<UserManagementView.Listener> listeners = new LinkedList<>();

  private final TextField txtUserName;

  public UserManagementPanel()
  {
    setSizeFull();

    userContainer = new BeanContainer<>(User.class);
    userContainer.setBeanIdProperty("name");

    userList = new Table();
    userList.setEditable(true);
    userList.setSelectable(true);
    userList.setMultiSelect(true);
    userList.addStyleName(ChameleonTheme.TABLE_STRIPED);
    userList.addStyleName("transparent-selection");
    userList.setSizeFull();
    userList.setContainerDataSource(userContainer);
    userList.addGeneratedColumn("changepassword",
      new PasswordChangeColumnGenerator());

    userList.
      setVisibleColumns("name", "groups", "permissions", "changepassword");
    userList.
      setColumnHeaders("Username", "Groups (seperate with comma)", "Additional permissions (seperate with comma)", "");

    userList.setTableFieldFactory(new FieldFactory());

    txtUserName = new TextField();
    txtUserName.setInputPrompt("New user name");

    Button btAddNewUser = new Button("Add new user");
    btAddNewUser.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        handleAdd();
      }
    });
    btAddNewUser.addStyleName(ChameleonTheme.BUTTON_DEFAULT);

    Button btDeleteUser = new Button("Delete selected user(s)");
    btDeleteUser.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // get selected users
        Set<String> selectedUsers = (Set<String>) userList.getValue();
        for (UserManagementView.Listener l : listeners)
        {
          l.deleteUsers(selectedUsers);
        }
      }
    });

    HorizontalLayout actionLayout = new HorizontalLayout(txtUserName,
      btAddNewUser, btDeleteUser);

    layout = new VerticalLayout(userList, actionLayout);
    layout.setSizeFull();
    setContent(layout);

    addActionHandler(new AddUserHandler(txtUserName));

  }

  private void handleAdd()
  {
    for (UserManagementView.Listener l : listeners)
    {
      l.addNewUser(txtUserName.getValue());
    }
  }

  @Override
  public void attach()
  {
    super.attach();
    for (UserManagementView.Listener l : listeners)
    {
      l.attached();
    }
  }

  @Override
  public void addListener(UserManagementView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void askForPasswordChange(String userName)
  {
    NewPasswordWindow w = new NewPasswordWindow(userName, listeners);
    UI.getCurrent().addWindow(w);
    w.center();
  }

  @Override
  public void setUserList(Collection<User> users)
  {
    userContainer.removeAllItems();
    userContainer.addAll(users);
  }

  @Override
  public void emptyNewUserNameTextField()
  {
    txtUserName.setValue("");
  }

  public class AddUserHandler implements Action.Handler
  {

    private final Action enterKeyShortcutAction
      = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

    private final Object registeredTarget;

    public AddUserHandler(Object registeredTarget)
    {
      this.registeredTarget = registeredTarget;
    }

    @Override
    public Action[] getActions(Object target, Object sender)
    {
      return new Action[]
      {
        enterKeyShortcutAction
      };
    }

    @Override
    public void handleAction(Action action, Object sender, Object target)
    {
      if (action == enterKeyShortcutAction && target == registeredTarget)
      {
        handleAdd();
      }
    }
  }

  public class PasswordChangeColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, final Object itemId,
      Object columnId)
    {
      PasswordField txtNewPassword = new PasswordField();
      txtNewPassword.setInputPrompt("New password");
      Button btChangePassword = new Button("Change password");
      btChangePassword.addClickListener(new Button.ClickListener()
      {

        @Override
        public void buttonClick(Button.ClickEvent event)
        {
          askForPasswordChange((String) itemId);
        }
      });
      return btChangePassword;
    }

  }

  public class FieldFactory extends DefaultFieldFactory
  {

    @Override
    public Field<?> createField(Container container, final Object itemId,
      Object propertyId, Component uiContext)
    {
      Field<?> result = null;

      switch ((String) propertyId)
      {
        case "permissions":
        case "groups":

          PopupTwinColumnSelect selector = new PopupTwinColumnSelect();
          selector.setWidth("100%");
          selector.addValueChangeListener(new Property.ValueChangeListener()
          {

            @Override
            public void valueChange(Property.ValueChangeEvent event)
            {
              for (UserManagementView.Listener l : listeners)
              {
                l.userUpdated(userContainer.getItem(itemId).getBean());
              }
            }
          });

          result = selector;
          break;
        case "name":
          // explicitly request a read-only label for the name and groups
          result = null;
          break;
        default:
          result = super.createField(container, itemId, propertyId, uiContext);
          break;
      }

      return result;
    }

  }

}
