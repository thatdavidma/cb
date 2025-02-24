import java.util.*;

public class InMemoryDatabase_v2 {
    Map<String, Map<String,Field>> db;
    Map<String, InMemoryDatabase_v2> backups;

    public InMemoryDatabase_v2(){
        this.db = new HashMap<>();
    }

    public String set(String key, String field, String value){
        if (!db.containsKey(key)){
            Map<String, Field> map = new HashMap<>();
            map.put(field, new Field(value, null, null));
            db.put(key, map);
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            db.get(key).put(field, new Field(value, null, null));
        }
        return "";
    }

    public String setAt(String key, String field, String value, String timestamp){
        if (!db.containsKey(key)){
            Map<String, Field> map = new HashMap<>();
            map.put(field, new Field(value, Integer.parseInt(timestamp),null));
            db.put(key, map);
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            db.get(key).put(field, new Field(value, Integer.parseInt(timestamp), null));
        }
        return "";

    }

    public String deleteAt(String key, String field, String timestamp){
        if (!db.containsKey(key)){
            return "false";
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            return "false";
        }
        else if (db.containsKey(key) && db.get(key).containsKey(field) && isExpired(key, field, timestamp)){
            return "false";
        }
        else {
            db.get(key).remove(field);
            return "true";
        }
    }

    public String setAtWithTtl(String key, String field, String value, String timestamp, String ttl){
        if (!db.containsKey(key)){
            Map<String, Field> map = new HashMap<>();
            map.put(field, new Field(value, Integer.parseInt(timestamp), Integer.parseInt(ttl)));
            db.put(key, map);
        }
        else {
            db.get(key).put(field, new Field(value, Integer.parseInt(timestamp), Integer.parseInt(ttl)));
        }
        return "";

    }

    public String get(String key, String field){
        if (!db.containsKey(key)){
            return "";
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            return "";
        }
        else {
            return db.get(key).get(field).value;
        }
    }

    public String getAt(String key, String field, String timestamp){
        if (!db.containsKey(key)){
            return "";
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            return "";
        }
        else if (db.containsKey(key) && db.get(key).containsKey(field) && isExpired(key, field, timestamp)){
            return "";
        }
        else {
            return db.get(key).get(field).value;
        }
    }

    public String scan(String key){
        if (!db.containsKey(key)){
            return "";
        }
        Map<String, Field> fieldMap = db.get(key);
        List<String> matchingFields = new ArrayList<>();
        for (Map.Entry<String,Field> entry : fieldMap.entrySet()){
            matchingFields.add(entry.getKey() + "(" + entry.getValue().value + ")");
        }
        Collections.sort(matchingFields);
        return String.join(",", matchingFields);
    }

    public String scanAt(String key, String timestamp){
        if (!db.containsKey(key)){
            return "";
        }
        Map<String, Field> fieldMap = db.get(key);
        List<String> matchingFields = new ArrayList<>();
        for (Map.Entry<String,Field> entry : fieldMap.entrySet()){
            if (!isExpired(key, entry.getKey(), timestamp)){
                matchingFields.add(entry.getKey() + "(" + entry.getValue().value + ")");
            }
        }
        Collections.sort(matchingFields);
        return String.join(",", matchingFields);
    }

    public String scanByPrefix(String key, String prefix){
        if (!db.containsKey(key)){
            return "";
        }
        Map<String, Field> fieldMap = db.get(key);
        List<String> matchingFields = new ArrayList<>();
        for (Map.Entry<String,Field> entry : fieldMap.entrySet()){
            if (entry.getKey().startsWith(prefix)){
                matchingFields.add(entry.getKey() + "(" + entry.getValue().value + ")");
            }
        }
        Collections.sort(matchingFields);
        return String.join(",", matchingFields);
    }

    public String scanByPrefixAt(String key, String prefix, String timestamp){
        if (!db.containsKey(key)){
            return "";
        }
        Map<String, Field> fieldMap = db.get(key);
        List<String> matchingFields = new ArrayList<>();
        for (Map.Entry<String,Field> entry : fieldMap.entrySet()){
            if (!isExpired(key, entry.getKey(), timestamp)){
                if (entry.getKey().startsWith(prefix)){
                    matchingFields.add(entry.getKey() + "(" + entry.getValue().value + ")");
                }
            }
        }
        Collections.sort(matchingFields);
        return String.join(",", matchingFields);
    }

    public String delete(String key, String field){
        if (!db.containsKey(key)){
            return "false";
        }
        else if (db.containsKey(key) && !db.get(key).containsKey(field)){
            return "false";
        }
        else {
            db.get(key).remove(field);
            return "true";
        }
    }

    public boolean isExpired(String key, String field, String currentTimestamp){
        Integer ttl = db.get(key).get(field).ttl;
        Integer timestamp = db.get(key).get(field).timestamp;
        if (ttl == null){
            return false;
        }
        else if (Integer.parseInt(currentTimestamp) < timestamp + ttl){
            return false;
        }
        return true;
    }


    public static void main(String[] args){
        InMemoryDatabase_v2 db = new InMemoryDatabase_v2();

        // System.out.println(db.set("A", "B", "E"));
        // System.out.println(db.set("A", "C", "F"));
        // System.out.println(db.get("A", "B"));
        // System.out.println(db.get("A", "D"));
        // System.out.println(db.delete("A", "B"));
        // System.out.println(db.delete("A", "D"));

        // System.out.println(db.set("A", "BC", "E"));
        // System.out.println(db.set("A", "BD", "F"));
        // System.out.println(db.set("A", "C", "G"));
        // System.out.println(db.scanByPrefix("A", "B"));
        // System.out.println(db.scan("A"));
        // System.out.println(db.scanByPrefix("B", "B"));

        // System.out.println("Query 1: " + db.setAtWithTtl("A", "BC", "E", "1", "9"));
        // System.out.println("Query 2: " + db.setAtWithTtl("A", "BC", "E", "5", "10"));
        // System.out.println("Query 3: " + db.setAt("A", "BD", "F", "5"));
        // System.out.println("Query 4: " + db.scanByPrefixAt("A", "B", "14"));
        // System.out.println("Query 5: " + db.scanByPrefixAt("A", "B", "15"));

        System.out.println("Query 6: " + db.setAt("A", "B", "C", "1"));
        System.out.println("Query 7: " + db.setAtWithTtl("X", "Y", "Z", "2",  "15"));
        System.out.println("Query 8: " + db.getAt("X", "Y", "3"));
        System.out.println("Query 9: " + db.setAtWithTtl("A", "D", "E", "4", "10"));
        System.out.println("Query 10: " + db.scanAt("A", "13"));
        System.out.println("Query 11: " + db.scanAt("X", "16"));
        System.out.println("Query 12: " + db.scanAt("X", "17"));
        System.out.println("Query 13: " + db.deleteAt("X", "Y", "20"));

    }

    static class Field {
        String value;
        Integer timestamp;
        Integer ttl;
        public Field(String value, Integer timestamp, Integer ttl){
            this.value = value;
            this.timestamp = timestamp;
            this.ttl = ttl;
        }
    }
}