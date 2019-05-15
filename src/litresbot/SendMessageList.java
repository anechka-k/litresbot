package litresbot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class SendMessageList
{
  private int max = 2048;
  
  private String currentPage = "";
  private String nextPage = "";
  
  private List<String> pages = new ArrayList<String>();

  public SendMessageList() {}

  public SendMessageList(int max)
  {
    this.max = max;
  }
  
  public void endPage()
  {
    if((currentPage.length() + nextPage.length()) <= max)
    {
      nextPage += currentPage;
      currentPage = "";
      return;
    }
    
    pages.add(nextPage);
    nextPage = currentPage;
    currentPage = "";
  }

  public void appendPage(String msg)
  {
    currentPage += msg;
  }

  public List<SendMessage> getMessages()
  {
    if(currentPage.length() > 0)
    {
      endPage();
    }
    
    if(nextPage.length() > 0)
    {
      pages.add(nextPage);
      nextPage = "";
    }
    
    List<SendMessage> res = new ArrayList<>();
    for (String page : pages)
    {
      SendMessage sendMessage = new SendMessage();
      sendMessage.setText(page.toString());
      sendMessage.enableHtml(true);
      res.add(sendMessage);
    }
    
    return res;
  }
}
