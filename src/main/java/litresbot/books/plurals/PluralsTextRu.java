package litresbot.books.plurals;

import litresbot.books.plurals.Plurals.PluralForm;

public final class PluralsTextRu
{  
  private PluralsTextRu() {} //never
    
  public static String convert(String text, int n)
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
