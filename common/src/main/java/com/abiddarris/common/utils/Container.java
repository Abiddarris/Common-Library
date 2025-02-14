package com.abiddarris.common.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Container implements Cloneable, Serializable{

    private transient HashMap<String,Object> objects;
    private String[] excludedkeys = new String[0];
    
    private static final long serialVersionUID = 1L;
    
    public Container(){
        objects = new HashMap<>();
    }
    
    public Container(Container container){
        objects = new HashMap<>(container.objects);
    }

    public void setExcludedkeys(String[] excludedkeys){
        this.excludedkeys = excludedkeys;
    }

    public String[] getExcludedkeys(){
        return excludedkeys;
    }                                                     

    public Map<String, Object> getObjects() {
        return objects;
    }
    
    public void addAll(Container container){
        Map<String,Object> objects = container.objects;
        for(String key : objects.keySet()){
            this.objects.put(key,objects.get(key));
        }
    }

    public void put(String name, Object object){
        objects.put(name,object);
    }

    public <T> T getObject(String name,T defaultValue){
        T obj = (T) objects.get(name);
        if(obj != null){
            return obj;
        }
            
        return defaultValue;
            
    }
    
    public boolean getBoolean(String name,boolean defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Boolean){
            return (Boolean)obj;
        } else if(obj instanceof String){
            return Boolean.parseBoolean((String)obj);
        }
        
        return defaultValue;
    }
    
    public byte getByte(String name,byte defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).byteValue();
        } else if(obj instanceof String){
            return Byte.parseByte((String)obj);
        }

        return defaultValue;
    }
    
    public char getChar(String name,char defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Character)
            return (char)obj;

        return defaultValue;
    }
    
    public double getDouble(String name,double defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).doubleValue();
        } else if(obj instanceof String){
            return Double.parseDouble((String)obj);
        }

        return defaultValue;
    }
    
    public float getFloat(String name,float defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).floatValue();
        } else if(obj instanceof String){
            return Float.parseFloat((String)obj);
        }

        return defaultValue;
    }
    
    public int getInt(String name,int defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).intValue();
        } else if(obj instanceof String){
            return Integer.parseInt((String)obj);
        }

        return defaultValue;
    }
    
    public long getLong(String name,long defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).longValue();
        } else if(obj instanceof String){
            return Long.parseLong((String)obj);
        }

        return defaultValue;
    }
    
    public short getShort(String name,short defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof Number){
            return ((Number)obj).shortValue();
        } else if(obj instanceof String){
            return Short.parseShort((String)obj);
        }

        return defaultValue;
    }
    
    public String getString(String name,String defaultValue){
        Object obj = objects.get(name);
        if(obj instanceof String){
            return (String) obj;
        }

        return defaultValue;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Container container = (Container) super.clone();
        container.objects = (HashMap<String, Object>) objects.clone();
        return container;
    }
    
    public Set<String> keys(){
        return objects.keySet();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException{
        oos.defaultWriteObject();
        
        Map<String,Object> copy = new HashMap<>();
        for(String key : objects.keySet()){
            boolean notExcludedKey = true;
            for(String excludedkey : excludedkeys){
                if(key.equals(excludedkey)){
                    notExcludedKey = false;
                    break;
                }                
            }  
            if(!notExcludedKey)
                continue;
                
            Object obj = objects.get(key);
            if(obj instanceof Serializable)
                copy.put(key,obj);                        
        }
        
        oos.writeObject(copy);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException{
        ois.defaultReadObject();
        
        objects = (HashMap<String, Object>) ois.readObject();
    }
    
    
}
