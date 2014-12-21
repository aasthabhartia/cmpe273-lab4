package edu.sjsu.cmpe.cache.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CRDTClient {
	
	int putSuccess,getSuccess = 0;
	ArrayList<HttpResponse<JsonNode>> responses = new ArrayList<HttpResponse<JsonNode>>();
	HashMap<HttpResponse<JsonNode>,String> res = new HashMap<HttpResponse<JsonNode>,String>();
	String key;
	ArrayList<String> urls = new ArrayList<String>();
	
	public CRDTClient(ArrayList<String> urls)
	{
		this.urls = urls;
	}
	
public void put(String key,String value)
	{
		putSuccess = 0;
		responses.clear();
		this.key = key;
		for(String u : urls)
		{
			put(key,value, u);
		}
		
	}
	
	
public void put(String key,final String value,final String u)
{
	Future<HttpResponse<JsonNode>> future1 = Unirest.put(u + "/cache/{key}/{value}")
            .header("accept", "application/json")
            .routeParam("key", key)
            .routeParam("value", value)
			  .asJsonAsync(new Callback<JsonNode>() {

			   public void failed(UnirestException e) {
			    	System.out.println("put request of value " + value + " to server " + u + " has failed");
			        putSuccess++;
			        if(putSuccess==3)
			    	{
			    		putSuccess = 0;
			    		checkforSuccess();
			    	}
			        
			    }

			    public void completed(HttpResponse<JsonNode> response) {
			    	
			    	putSuccess++;
			    	System.out.println("put request of value " + value + " to server " + u + " has succeeded");
			    	responses.add(response);
			    	if(putSuccess==3)
			    	{
			    		putSuccess = 0;
			    		checkforSuccess();
			    	}
			         
			    }

			    public void cancelled() {
			        System.out.println("The request has been cancelled");
			    }

			});
}


public void get(String key)
{
	getSuccess = 0;
	res.clear();
	this.key = key;
	for(String u : urls)
	{
		get(key, u);
	}
	
}


public void get(String key,final String u)
{
Future<HttpResponse<JsonNode>> future1 = Unirest.get(u + "/cache/{key}")
        .header("accept", "application/json")
        .routeParam("key", key)
		  .asJsonAsync(new Callback<JsonNode>() {

		    public void failed(UnirestException e) {
		        System.out.println("get request to server " + u + " has failed");
		        getSuccess++;
		        if(getSuccess==3)
		    	{
		    		getSuccess = 0;
		    		checkforGet();
		    	}
		    }

		    public void completed(HttpResponse<JsonNode> response) {
		    	
		    	getSuccess++;
		    	res.put(response,u);
		    	System.out.println("get request to server " + u + " succeeded");
		    	if(getSuccess==3)
		    	{
		    		getSuccess = 0;
		    		checkforGet();
		    	}
		         
		    }

		    public void cancelled() {
		        System.out.println("The request has been cancelled");
		    }

		});
}

    public void checkforGet()
    {
    	ArrayList<String> values = new ArrayList<String>();
    	for(HttpResponse<JsonNode> r : res.keySet())
		{
    		values.add(r.getBody().getObject().get("value").toString());
    		
		}
    	Map<String,Integer> map = new HashMap<String, Integer>();  
    	for(String v: values){          
            Integer count = map.get(v);         
            map.put(v, count==null?1:count+1);   //auto boxing and count  
        }  
    	Integer max = 0;
    	String keyvalue = map.keySet().toArray()[0].toString();
    	for (Map.Entry<String, Integer> entry : map.entrySet())
    	{
    		if(entry.getValue()>max)
    		{
    			max=entry.getValue();
    			keyvalue = entry.getKey();
    		}
    	}
    	for (Map.Entry<HttpResponse<JsonNode>,String> entry : res.entrySet())
		{
    		if(!keyvalue.equals(entry.getKey().getBody().getObject().get("value").toString()))
    		{
    			put(key,keyvalue, entry.getValue());
    		}
    			
    		
		}
    	System.out.println("Value returned: " + keyvalue);
    	
    }

	public void checkforSuccess()
	{
		
		
		int countSuccess = 0;
		for(HttpResponse<JsonNode> r : responses)
		{
			if(r.getCode()==200)
				countSuccess++;
		}
		if(countSuccess<2)
		{
			delete();
		}
			
	}
	
	public void delete()
	{
		for(final String u : urls)
		{
			Future<HttpResponse<JsonNode>> future1 = Unirest.delete(u + "/cache/{key}")
		            .header("accept", "application/json")
		            .routeParam("key", key)
					  .asJsonAsync(new Callback<JsonNode>() {

					    public void failed(UnirestException e) {
					    	System.out.println("delete request to server " + u + "has failed");
					        
					        
					    }

					    public void completed(HttpResponse<JsonNode> response) {
					    	
					    	System.out.println("delete request to server " + u + "has succeeded");
					    	
					         
					    }

					    public void cancelled() {
					        System.out.println("The request has been cancelled");
					    }

					});
		}
		
	}

}
