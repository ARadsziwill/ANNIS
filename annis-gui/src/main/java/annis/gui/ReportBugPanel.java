/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import com.vaadin.Application;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import java.io.File;
import java.io.IOException;
import javax.activation.FileDataSource;
import org.apache.commons.mail.*;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ReportBugPanel extends Panel
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaDataPanel.class);

  private Form form;
  private TextField txtSummary;
  private TextArea txtDescription;
  private TextField txtName;
  private TextField txtMail;
  private Button btSubmit;
  private Button btCancel;
  
  public ReportBugPanel(Application app, final String bugEMailAddress, final byte[] screenImage)
  {
    
    setSizeUndefined();

    FormLayout layout = new FormLayout();
    layout.setSizeUndefined();
    form = new Form(layout);
    form.setCaption("Report Bug");
    form.setSizeUndefined();

    form.setInvalidCommitted(false);
    form.setWriteThrough(false);

    getContent().setSizeFull();
    getContent().addComponent(form);

    txtSummary = new TextField("Short Summary");
    txtSummary.setRequired(true);
    txtSummary.setRequiredError("You must provide a summary");
    txtSummary.setColumns(50);
    
    txtDescription = new TextArea("Long Description");
    txtDescription.setRequired(true);
    txtDescription.setRequiredError("You must provide a description");
    txtDescription.setRows(10);
    txtDescription.setColumns(50);
    txtDescription.setValue("What steps will reproduce the problem?\n"
      + "1.\n"
      + "2.\n"
      + "3.\n"
      + "\n"
      + "What is the expected result?\n"
      + "\n"
      + "\n"
      + "What happens instead\n"
      + "\n"
      + "\n"
      + "Please provide any additional information below.\n");
    
    txtName = new TextField("Your Name");
    txtName.setRequired(true);
    txtName.setRequiredError("You must provide your name");
    txtName.setColumns(50);

    txtMail = new TextField("Your e-mail adress");
    txtMail.setRequired(true);
    txtMail.setRequiredError("You must provide a valid e-mail adress");
    txtMail.addValidator(new EmailValidator(
      "You must provide a valid e-mail adress"));
    txtMail.setColumns(50);


    form.addField("summary", txtSummary);
    form.addField("description", txtDescription);
    form.addField("name", txtName);
    form.addField("email", txtMail);

    btSubmit = new Button("Submit bug report", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        try
        {
          form.commit();

          sendBugReport(bugEMailAddress, screenImage);


          Window subwindow = getWindow();
          Window parent = subwindow.getParent();
          parent.removeWindow(subwindow);

          parent.showNotification("Bug report was sent",
            "We will answer your bug report as soon as possible",
            Window.Notification.TYPE_HUMANIZED_MESSAGE);


        }
        catch (Validator.InvalidValueException ex)
        {
          // ignore
        }
        catch (Exception ex)
        {
          getWindow().showNotification("Could not send bug report", ex.
            getMessage(),
            Window.Notification.TYPE_WARNING_MESSAGE);
        }
      }
    });

    btCancel = new Button("Cancel", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        form.discard();

        Window subwindow = getWindow();
        subwindow.getParent().removeWindow(subwindow);
      }
    });

    HorizontalLayout buttons = new HorizontalLayout();
    buttons.addComponent(btSubmit);
    buttons.addComponent(btCancel);

    form.getFooter().addComponent(buttons);
  }

  private void sendBugReport(String bugEMailAddress, byte[] screenImage)
  {
    MultiPartEmail mail = new MultiPartEmail();
    try
    {
      // server setup
      mail.setHostName("localhost");

      // content of the mail
      mail.addReplyTo(form.getField("email").getValue().toString(), 
        form.getField("name").getValue().toString());
      mail.setFrom(bugEMailAddress);
      mail.addTo(bugEMailAddress);

      mail.setSubject("[ANNIS BUG] " + form.getField("summary").getValue().
        toString());

      // TODO: add info about version etc.
      StringBuilder sbMsg = new StringBuilder();

      sbMsg.append("Reporter: ").append(form.getField("name").getValue().
        toString()).append(" (").append(form.getField("email").getValue().
        toString()).append(")\n");
      sbMsg.append("Version: ").append(getApplication().getVersion()).append(
        "\n");
      sbMsg.append("URL: ").append(getApplication().getURL().toString()).append(
        "\n");

      sbMsg.append("\n");

      sbMsg.append(form.getField("description").getValue().toString());
      mail.setMsg(sbMsg.toString());

      if (screenImage != null)
      {
        try
        {
          mail.attach(new ByteArrayDataSource(screenImage, "image/png"),
            "screendump.png", "Screenshot of the browser content at time of bug report");
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
        
        WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
        File logfile = new File(context.getHttpSession().getServletContext().getRealPath("/WEB-INF/log/annis-gui.log"));
        if(logfile.exists() && logfile.isFile() && logfile.canRead())
        {
          mail.attach(new FileDataSource(logfile), "annis-gui.log", "Logfile of the GUI (shared by all users)");
        }
      }
    
      mail.send();

    }
    catch (EmailException ex)
    {
      getWindow().showNotification("E-Mail not configured on server", 
        "If this is no Kickstarter version please ask the adminstrator of this ANNIS-instance for assistance. "
        + "Bug reports are not available for ANNIS Kickstarter", Window.Notification.TYPE_ERROR_MESSAGE);
      log.error(null,
        ex);
    }
  }
  

}
