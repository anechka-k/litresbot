package litresbot;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class HttpClientPostLocal
{
  public static JSONObject request(String url) throws JSONException
  {
    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    CloseableHttpResponse response = null;
  
    try
    {
      try
      {
        HttpPost httppost = new HttpPost(url);
        response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        
        if(response.getStatusLine().getStatusCode() == 200)
        {
          JSONObject responseJson = new JSONObject(EntityUtils.toString(entity));
          return responseJson;
        }
  
        JSONObject responseJson = new JSONObject();
        responseJson.put("result", "error");
        responseJson.put("error", "Got status: " + response.getStatusLine().getStatusCode());
          
        return responseJson;
      }
      finally
      {
        if(response != null) response.close();
      }
    }
    catch (IOException e)
    {
      JSONObject responseJson = new JSONObject();
      responseJson.put("result", "error");
      responseJson.put("error", e.getMessage());
        
      return responseJson;
    }
  }
}
