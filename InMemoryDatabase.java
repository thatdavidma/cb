import java.util.*;

public class InMemoryDatabase {
    private final Map<String, Map<String, Map<String, Object>>> database;

    public InMemoryDatabase() {
        this.database = new HashMap<>();
    }

    public String set_at(String key, String recordId, String value, Integer timestamp) {
        if (!database.containsKey(key)) {
            database.put(key, new HashMap<>());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("value", value);
        record.put("timestamp", timestamp);
        record.put("ttl", null);
        database.get(key).put(recordId, record);
        return "";
    }

    public String set_at_with_ttl(String key, String recordId, String value, Integer timestamp, Integer ttl) {
        if (!database.containsKey(key)) {
            database.put(key, new HashMap<>());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("value", value);
        record.put("timestamp", timestamp);
        record.put("ttl", ttl);
        database.get(key).put(recordId, record);
        return "";
    }

    public String get(String key, String recordId) {
        if (!database.containsKey(key) || !database.get(key).containsKey(recordId)) {
            return null;
        }
        Map<String, Object> record = database.get(key).get(recordId);
        Integer timestamp = (Integer) record.get("timestamp");
        Integer ttl = (Integer) record.get("ttl");

        return (String) record.get("value");
    }

    public String get_at(String key, String recordId, Integer currentTimestamp) {
        if (!database.containsKey(key) || !database.get(key).containsKey(recordId)) {
            return null;
        }

        if (!isExpired(database.get(key).get(recordId), currentTimestamp)){
            return (String) database.get(key).get(recordId).get("value");
        }
        else {
            return null;
        }
    }

    public List<String> scan_at(String key, Integer timestamp) {
        List<String> res = new ArrayList<>();
        if (database.containsKey(key)) {
            for (Map.Entry<String, Map<String, Object>> entry : database.get(key).entrySet()) {
                if (!isExpired(entry.getValue(), timestamp)) {
                    res.add(entry.getKey() + "(" + entry.getValue().get("value") + ")");
                }
            }
        }
        return res;
    }

    public List<String> scan(String key) {
        List<String> res = new ArrayList<>();
        if (database.containsKey(key)) {
            for (Map.Entry<String, Map<String, Object>> entry : database.get(key).entrySet()) {
                res.add(entry.getKey() + "(" + entry.getValue().get("value") + ")");
            }
        }
        return res;
    }
    
    public List<String> scan_by_prefix_at(String key, String recordIdPrefix, Integer currentTimestamp) {
        List<String> res = new ArrayList<>();
        if (database.containsKey(key)) {
            for (Map.Entry<String, Map<String, Object>> entry : database.get(key).entrySet()) {
                if (entry.getKey().startsWith(recordIdPrefix) && !isExpired(entry.getValue(), currentTimestamp)) {
                    res.add(entry.getKey() + "(" + entry.getValue().get("value") + ")");
                }
            }
        }
        return res;
    }

    public List<String> scan_by_prefix(String key, String recordIdPrefix) {
        List<String> res = new ArrayList<>();
        if (database.containsKey(key)) {
            for (Map.Entry<String, Map<String, Object>> entry : database.get(key).entrySet()) {
                if (entry.getKey().startsWith(recordIdPrefix)) {
                    res.add(entry.getKey() + "(" + entry.getValue().get("value") + ")");
                }
            }
        }
        return res;
    }

    public boolean delete(String key, String recordId) {
        return database.containsKey(key) && database.get(key).remove(recordId) != null;
    }

    public boolean delete_at(String key, String recordId, Integer timestamp) {
        if (database.containsKey(key) && database.get(key).containsKey(recordId)) {
            Map<String, Object> record = database.get(key).get(recordId);
            if ((int) record.get("timestamp") == timestamp) {
                database.get(key).remove(recordId);
                return true;
            }
        }
        return false;
    }

    private boolean isExpired(Map<String, Object> record, Integer currentTimestamp) {
        Integer timestamp = (Integer) record.get("timestamp");
        Integer ttl = (Integer) record.get("ttl");
        return ttl != null && (timestamp + ttl) <= currentTimestamp;
    }

    public static void main(String[] args) throws InterruptedException {
        InMemoryDatabase db = new InMemoryDatabase();
        // System.out.println("Query 1: " + db.set_at_with_ttl("A", "BC", "E", 1, 9));
        // System.out.println("Query 2: " + db.set_at_with_ttl("A", "BC", "E", 5, 10));
        // System.out.println("Query 3: " + db.set_at("A", "BD", "F", 5));
        // System.out.println("Query 4: " + db.scan_by_prefix_at("A", "B", 14));
        // System.out.println("Query 5: " + db.scan_by_prefix_at("A", "B", 15));

        System.out.println("Query 6: " + db.set_at("A", "B", "C", 1));
        System.out.println("Query 7: " + db.set_at_with_ttl("X", "Y", "Z", 2,  15));
        System.out.println("Query 8: " + db.get_at("X", "Y", 3));
        System.out.println("Query 9: " + db.set_at_with_ttl("A", "D", "E", 4, 10));
        System.out.println("Query 10: " + db.scan_at("A", 13));
        System.out.println("Query 11: " + db.scan_at("X", 16));
        System.out.println("Query 12: " + db.scan_at("X", 17));
        System.out.println("Query 13: " + db.delete_at("X", "Y", 20));

        // Queries

        // ["SET_AT", "A", "B", "C", "1"]
        // ["SET_AT_WITH_TTL", "X", "Y", "Z", "2", "15"]
        // ["GET_AT", "X", "Y", "3"]
        // ["SET_AT_WITH_TTL", "A", "D", "E", "4", "10"]
        // ["SCAN_AT", "A", "13"], 
        // ["SCAN_AT", "X", "16"], 
        // ["SCAN_AT", "X", "17"], 
        // ["DELETE_AT", "X", "Y", "20"]

        // Explanations

        // returns ""; database state: {"A": {"B": "C"}} 
        // returns ""; database state: {"X": {"Y": "Z"}, "A": {"B": "C"}} where {"Y": "Z"} expires at timestamp 17 
        // returns "Z" 
        // returns ""; database state: {"X": {"Y": "Z"}, "A": {"D": "E", "B": "C"}} where {"D": "E"} expires at timestamp 14 and {"Y": "Z"} expires at timestamp 17 
        // returns "B(C), D(E)" 
        // returns "Y(Z)" 
        // returns ""; Note that all fields in record "X" have expired 
        // returns "false"; the record "X" was expired at timestamp 17 and can't be deleted.

    }
}
