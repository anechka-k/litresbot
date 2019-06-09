package litresbot.books;

import litresbot.util.Plurals.PluralForm;

public final class PluralsTextEn
{  
  private PluralsTextEn() {} //never
    
  public static String convert(String text, Long n)
  {
    String resultText = text;
    PluralForm pluralForm = PluralForm.TWO;
        
    if(n == 1)
    {
      pluralForm = PluralForm.ONE;
    }
    
    if(text.equals("book"))
    {
      switch(pluralForm)
      {
        case ONE:
          resultText = "book";
          break;
          
        case TWO:
          resultText = "books";
          break;
          
        default:
          break;
      }
      
      return resultText;
    }
    
    return resultText;
  }    
}
