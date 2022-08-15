package litresbot.books.convert;

class TagPosition {
  public enum TagType {
    NONE,
    ITALIC,
    BOLD,
    STRIKE
  }

  public int from;
  public int to;
  public TagType type;

  public int getFrom() { return from; }
}