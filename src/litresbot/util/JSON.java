package litresbot.util;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class JSON {

    private JSON() {} //never

    public final static JSONStreamAware emptyJSON = new JSONObject();
    
    @SuppressWarnings("unchecked")
    public static void putNonNull(JSONObject json, String key, Object value)
    {
      if(value == null) return;
      json.put(key, value);
    }
}
