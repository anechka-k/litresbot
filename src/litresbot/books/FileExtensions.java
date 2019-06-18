package litresbot.books;

public class FileExtensions
{
  public static String[] supportexExt =
  {
    "fb2",
    "fb3",
    "pdf",
    "epub",
    "mobi",
    "txt",
    "doc",
    "docx",
    "html",
    "htm",
    "zip",
    "rar",
    "chm",
    "djvu",
    "iso",
    "lit",
    "lrf",
    "mp3",
    "png",
    "ppt",
    "rtf",
    "tar",
    "xml",
    "word",
    "tex",
    "rgo",
    "prc",
    "pdg",
    "pdb",
    "odt",
    "mhtm",
    "mht",
    "7zip",
    "azw",
    "azw3",
    "cbr",
    "cbz",
    "dat",
    "dic",
    "djv",
    "exe",
    "jpg",
    "ha"
  };
  
  public static String detectFormat(String downloadString)
  {
    downloadString = downloadString.toLowerCase();
    
    for(String ext : supportexExt)
    {
      if(downloadString.startsWith(ext))
      {
        return ext;
      }
    }
    
    return null;
  }
  
  public static String detectExtension(String filename)
  {
    filename = filename.toLowerCase();
    
    for(String ext : supportexExt)
    {
      if(filename.endsWith("." + ext))
      {
        return ext;
      }
    }
    
    return null;
  }
}
