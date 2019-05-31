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

public class FileNameParser
{
  public static String[] parse(String url, String type)
  {
    String[] parts = url.split("/");
    String[] typeParts = type.split("/");
    
    if(!type.startsWith("application/"))
    {
      String[] fileparts = new String[2];
      fileparts[0] = parts[parts.length - 2];
      fileparts[1] = parts[parts.length - 1];
      
      return fileparts;
    }
    
    String typeMime = typeParts[1];
    typeMime = typeMime.replace("x-mobipocket-ebook", "mobi");    
    
    String typeMimeShort = typeMime.replace("+zip", "");
      
    if(typeMime.endsWith("+zip"))
    {
      String[] fileparts = new String[3];
      fileparts[0] = parts[parts.length - 2];
      fileparts[1] = typeMimeShort;
      fileparts[2] = "zip";
      return fileparts;
    }
    
    typeMimeShort = typeMime.replace("+rar", "");
    
    if(typeMime.endsWith("+rar"))
    {
      String[] fileparts = new String[3];
      fileparts[0] = parts[parts.length - 2];
      fileparts[1] = typeMimeShort;
      fileparts[2] = "rar";
      return fileparts;
    }
      
    String[] fileparts = new String[2];
    fileparts[0] = parts[parts.length - 2];
    fileparts[1] = typeMime;
      
    return fileparts;
  }
}
