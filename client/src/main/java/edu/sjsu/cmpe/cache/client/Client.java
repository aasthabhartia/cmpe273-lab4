package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Cache Client...");
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("http://localhost:3000");
        urls.add("http://localhost:3001");
        urls.add("http://localhost:3002");
        CRDTClient c = new CRDTClient(urls);
        c.put("1", "a");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // handle the exception...        
            // For example consider calling Thread.currentThread().interrupt(); here.
        }
        c.put("1", "b");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // handle the exception...        
            // For example consider calling Thread.currentThread().interrupt(); here.
        }
        c.get("1");
        
        
    }

}
