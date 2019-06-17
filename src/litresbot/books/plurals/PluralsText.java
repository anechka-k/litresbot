package litresbot.books.plurals;

import litresbot.books.plurals.Plurals.PluralForm;

public final class PluralsText
{  
  private PluralsText() {} //never
    
  public static String convert(String text, Long n)
  {
    String resultText = text;
    PluralForm pluralForm = Plurals.analyze(n);
    
    if(text.equals("�����"))
    {
      switch(pluralForm)
      {
        case ONE:
          resultText = "�����";
          break;
          
        case TWO:
          resultText = "�����";
          break;
          
        case FIVE:
          resultText = "����";
          break;
          
        default:
          break;
      }
      
      return resultText;
    }
    
    return resultText;
  }    
}
