/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016  schors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package litresbot.opdssearch.opds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flicus on 14.05.16.
 */
public class Entry
{
  public String updated;
  public String id;
  public String title;
  public String author;
  public String annotation;
  public List<Link> links = new ArrayList<>();

  public Entry(String updated, String id, String title, String author, String annotation)
  {
    this.updated = updated;
    this.id = id;
    this.title = title;
    this.author = author;
    this.annotation = annotation;
  }

  public Entry() { }

  public Entry(Entry another) {
    this.updated = another.updated;
    this.id = another.id;
    this.title = another.title;
    this.author = another.author;
    this.annotation = another.annotation;
    if (another.links != null) {
      // make a deep copy of the links
      this.links = new ArrayList<Link>(another.links.size());
      for (Link l : another.links) {
        this.links.add(new Link(l));
      }
    }
  }

  @Override
  public String toString()
  {
    return "Entry{" +
      "updated='" + updated + '\'' +
      ", id='" + id + '\'' +
      ", title='" + title + '\'' +
      ", author='" + author + '\'' +
      ", links=" + links +
      '}';
  }
}
