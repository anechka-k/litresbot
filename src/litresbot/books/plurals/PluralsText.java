package litresbot.books.plurals;

import litresbot.books.plurals.Plurals.PluralForm;

public final class PluralsText
{  
  private PluralsText() {} //never
    
  public static String convert(String text, Long n)
  {
    String resultText = text;
    PluralForm pluralForm = Plurals.analyze(n);
    
    if(text.equals("книга"))
    {
      switch(pluralForm)
      {
        case ONE:
          resultText = "книга";
          break;
          
        case TWO:
          resultText = "книги";
          break;
          
        case FIVE:
          resultText = "книг";
          break;
          
        default:
          break;
      }
      
      return resultText;
    }
    
    return resultText;
  }    
}
