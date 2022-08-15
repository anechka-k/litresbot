package litresbot.books.convert;

import org.junit.Test;

import litresbot.books.convert.TagPosition.TagType;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

public class TagsInserterTest {
  String text = "lorem ipsum dolor sit amet, consectetur";

  @Test 
  public void testInsertOk() {
    String textWithTags = "lorem <i>ipsum</i> dolor sit amet, consectetur";

    List<TagPosition> tagPositions = new ArrayList<>();
    TagPosition italic = new TagPosition();
    italic.from = 6;
    italic.to = 11;
    italic.type = TagType.ITALIC;
    tagPositions.add(italic);
    String result = TagsInserter.insertTags(text, tagPositions);
    Assert.assertTrue(result.length() > text.length());
    Assert.assertEquals(textWithTags, result);
  }

  @Test 
  public void testInsertNoTagsOk() {
    List<TagPosition> tagPositions = new ArrayList<>();
    String result = TagsInserter.insertTags(text, tagPositions);
    Assert.assertTrue(result.length() == text.length());
    Assert.assertEquals(text, result);
  }

  @Test 
  public void testInsertNestedOk() {
    String textWithTags = "lorem<i> <b>ipsum</b> dolor</i> sit amet, consectetur";

    List<TagPosition> tagPositions = new ArrayList<>();
    TagPosition italic = new TagPosition();
    italic.from = 5;
    italic.to = 17;
    italic.type = TagType.ITALIC;
    TagPosition bold = new TagPosition();
    bold.from = 6;
    bold.to = 11;
    bold.type = TagType.BOLD;
    tagPositions.add(italic);
    tagPositions.add(bold);
    String result = TagsInserter.insertTags(text, tagPositions);
    Assert.assertTrue(result.length() > text.length());
    Assert.assertEquals(textWithTags, result);
  }

  @Test 
  public void testInsertOverlapOk() {
    String textWithTags = "lorem<i> <b>ipsum</i> dolor</b> sit amet, consectetur";

    List<TagPosition> tagPositions = new ArrayList<>();
    TagPosition italic = new TagPosition();
    italic.from = 5;
    italic.to = 11;
    italic.type = TagType.ITALIC;
    TagPosition bold = new TagPosition();
    bold.from = 6;
    bold.to = 17;
    bold.type = TagType.BOLD;
    tagPositions.add(italic);
    tagPositions.add(bold);
    String result = TagsInserter.insertTags(text, tagPositions);
    Assert.assertTrue(result.length() > text.length());
    Assert.assertEquals(textWithTags, result);
  }
}
