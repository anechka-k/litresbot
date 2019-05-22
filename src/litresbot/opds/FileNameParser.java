/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 schors
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

package litresbot.opds;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameParser
{
  private static List<FileType> types = new ArrayList<>();
  
  static
  {
    add(new FileNameParser.FileType("mobi")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
      }
    });
    
    add(new FileNameParser.FileType("\\w+\\+zip")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 2] + "." + parts[parts.length - 1] + ".zip";
      }
    });
      
    add(new FileNameParser.FileType("djvu")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 1] + ".djvu";
      }
    });
    
    add(new FileNameParser.FileType("pdf")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 1] + ".pdf";
      }
    });
    
    add(new FileNameParser.FileType("doc")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 1] + ".doc";
      }
    });
    
    add(new FileNameParser.FileType("\\w+\\+rar")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 2] + "." + parts[parts.length - 1] + ".rar";
      }
    });
    
    add(new FileNameParser.FileType("fb2")
    {
      @Override
      public String parse(String url)
      {
        String[] parts = url.split("/");
        return parts[parts.length - 2] + "." + parts[parts.length - 1] + ".zip";
      }
    });
  }
    
  private static FileType unknown = new FileType("any")
  {
    @Override
    public String parse(String url)
    {
      String[] parts = url.split("/");
      return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
  };

  public static String parse(String url)
  {
    return types
      .stream()
      .filter(fileType -> fileType.isMatch(url))
      .findAny()
      .orElse(unknown).parse(url);
  }

  public static void add(FileType fileType)
  {
    if (!types.contains(fileType))
    {
      types.add(fileType);
    }
  }

  public static abstract class FileType
  {
    private Pattern p;

    public FileType(String pattern)
    {
      this.p = Pattern.compile(pattern);
    }

    public boolean isMatch(String url)
    {
      Matcher m = p.matcher(url);
      return m.find();
    }

    public abstract String parse(String url);
  }
}
