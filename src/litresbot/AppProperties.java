package litresbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class AppProperties
{
  private static final String packageName = Application.packageName;
	
  private static final Properties defaultProperties = new Properties();
  static
  {
    try (InputStream is = ClassLoader.getSystemResourceAsStream(packageName + "-default.properties"))
    {
      if (is != null)
      {
        AppProperties.defaultProperties.load(is);
      }
      else
      {
        String configFile = System.getProperty(packageName + "-default.properties");
        if (configFile != null)
        {
          try (InputStream fis = new FileInputStream(configFile))
          {
            AppProperties.defaultProperties.load(fis);
          }
          catch (IOException e)
          {
            throw new RuntimeException("Error loading " + packageName + "-default.properties from " + configFile);
          }
        }
        else
        {
          throw new RuntimeException(packageName + "-default.properties not in classpath and system property " + 
              packageName + "-default.properties not defined either");
        }
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Error loading " + packageName + "-default.properties", e);
    }
  }
  
  private static final Properties properties = new Properties(defaultProperties);
  static
  {
    try (InputStream is = ClassLoader.getSystemResourceAsStream(packageName + ".properties"))
    {
      if (is != null)
      {
        AppProperties.properties.load(is);
      } // ignore if missing
    }
    catch (IOException e)
    {
      throw new RuntimeException("Error loading " + packageName + ".properties", e);
    }
  }

  public static int getIntProperty(String name)
  {
    try
    {
      int result = Integer.parseInt(properties.getProperty(name));
      return result;
    }
    catch (NumberFormatException e)
    {
      return 0;
    }
  }

  public static String getStringProperty(String name)
  {
    return getStringProperty(name, null);
  }

  public static String getStringProperty(String name, String defaultValue)
  {
    String value = properties.getProperty(name);
    if (value != null && ! "".equals(value))
    {
      return value;
    }
    else
    {
      return defaultValue;
    }
  }

  public static List<String> getStringListProperty(String name)
  {
    String value = getStringProperty(name);
    if (value == null || value.length() == 0)
    {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for (String s : value.split(";"))
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
    String value = properties.getProperty(name);
    if (Boolean.TRUE.toString().equals(value))
    {
      return true;
    }
    
    if (Boolean.FALSE.toString().equals(value))
    {
      return false;
    }
    
    return false;
  }
}
