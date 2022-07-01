package litresbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class AppProperties
{
  private static final String packageName = Application.packageName;

  public static final Properties versionProperties = new Properties();
  static
  {
    try (InputStream is = ClassLoader.getSystemResourceAsStream("version.properties"))
    {
      if (is != null)
      {
        versionProperties.load(is);
      }
    }
    catch (IOException e)
    {
      //ignore
    }
  }
	
  private static final Properties properties = new Properties();
  static
  {
    try (InputStream is = ClassLoader.getSystemResourceAsStream(packageName + ".properties"))
    {
      if (is != null)
      {
        properties.load(is);
      }
    }
    catch (IOException e)
    {
      //ignore
    }
  }

  public static Integer getIntProperty(String name)
  {
    try
    {
      String str = properties.getProperty(name);
      if (str == null) return null;
      int result = Integer.parseInt(str);
      return result;
    }
    catch (NumberFormatException e)
    {
      return null;
    }
  }

  public static String getStringProperty(String name)
  {
    String str = properties.getProperty(name);
    if (str == null) return null;
    if ("".equals(str)) return null;
    return str;
  }

  public static List<String> getStringListProperty(String name)
  {
    String str = getStringProperty(name);
    if (str == null || str.length() == 0)
    {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for (String s : str.split(";"))
    {
      s = s.trim();
      if (s.length() > 0)
      {
        result.add(s);
      }
    }
    return result;
  }

  public static Boolean getBooleanProperty(String name)
  {
    String str = properties.getProperty(name);
    if (Boolean.TRUE.toString().equals(str))
    {
      return true;
    }
    
    if (Boolean.FALSE.toString().equals(str))
    {
      return false;
    }
    
    return null;
  }
}
