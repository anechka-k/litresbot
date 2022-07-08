package litresbot.books.plurals;

public final class Plurals
{
  public static enum PluralForm
  {
    ONE, //книга
    TWO, //книги
    FIVE //книг
  }
  
  private Plurals() {} //never
    
  public static PluralForm analyze(int n)
  {
    if (n == 0) return PluralForm.FIVE;
    n = Math.abs(n) % 100;
    int n1 = n % 10;
    if (n == 11) return PluralForm.ONE;
    if (n > 11 && n < 20) return PluralForm.FIVE;
    if (n1 > 1 && n1 < 5) return PluralForm.TWO;
    if (n1 == 1) return PluralForm.ONE;
    return PluralForm.FIVE;
  }
}
