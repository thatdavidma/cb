import java.util.*;

public class CloudStorageSystem_v2 {
    private Map<String, File> database;
    private Map<String, Integer> users;

    public CloudStorageSystem_v2() {
        this.database = new HashMap<>();
        this.users = new HashMap<>();
    }

    public boolean addFile(String fileName, String size){
        if (!database.containsKey(fileName)){
            database.put(fileName, new File(Integer.parseInt(size), "admin"));
            return true;
        }
        return false;
    }

    public boolean addUser(String user, String capacity){
        if (!users.containsKey(user)){
            users.put(user, Integer.parseInt(capacity));
            return true;
        }
        return false;
    }

    public String compressFile(String user, String file){
        if (!users.containsKey(user) || !database.containsKey(file) || !database.get(file).user.equals(user)){
            return "";
        }
        int size = database.get(file).size;
        database.remove(file);
        database.put(file + ".COMPRESSED", new File(size/2, user));
        int usedStorage = 0;
        for (Map.Entry<String, File> entry : database.entrySet()){
            if (entry.getValue().user.equals(user)){
                usedStorage += entry.getValue().size;
            }
        }
        return String.valueOf(users.get(user) - usedStorage); 
    }

    public String decompressFile(String file, String user){
        if (!users.containsKey(user) || !database.containsKey(file) || !database.get(file).user.equals(user)){
            return "";
        }
        Integer usedStorage = 0;
        Integer userCapacity = users.get(user);
        for (Map.Entry<String, File> entry : database.entrySet()){
            if (entry.getValue().user.equals(user)){
                usedStorage += entry.getValue().size;
            }
        }
        Integer decompressedSize = database.get(file).size * 2;
        if (usedStorage + decompressedSize < userCapacity){
            database.remove(file);
            database.put(file.replace(".COMPRESSED", ""), new File(decompressedSize, user));
            return String.valueOf(userCapacity - (usedStorage + decompressedSize));
        }
        else {
            return "";
        }

    }

    public String addFileBy(String user, String file, String size){
        if (user.equals("admin")){
            database.put(file, new File(Integer.parseInt(size), user));
            return "true";
        }
        if (database.containsKey(file)){
            return "";
        }
        Integer usedStorage = 0;
        if (users.containsKey(user)){
            Integer userCapacity = users.get(user);
            for (Map.Entry<String, File> entry : database.entrySet()){
                if (entry.getValue().user.equals(user)){
                    usedStorage += entry.getValue().size;
                }
            }
            if (usedStorage + Integer.parseInt(size) < userCapacity){
                database.put(file, new File(Integer.parseInt(size), user));
                return String.valueOf(userCapacity - (Integer.parseInt(size) + usedStorage));
            }
        }
        return "";
    }

    public String updateCapacity(String user, String capaity){
        if (!users.containsKey(user)){
            return "";
        }
        Integer usedStorage = 0;
        Integer userCapacity = users.get(user);
        List<String> matchingFiles = new ArrayList<>();
        for (Map.Entry<String, File> entry : database.entrySet()){
            if (entry.getValue().user.equals(user)){
                usedStorage += entry.getValue().size;
                matchingFiles.add(entry.getKey() + "-" + String.valueOf(entry.getValue().size));
            }
        }
        if (usedStorage < Integer.parseInt(capaity)){
            users.put(user, Integer.parseInt(capaity));
            return "0";
        }
        else {
            matchingFiles.sort((file1, file2) -> {
                int size1 = Integer.parseInt(file1.split("-")[1]);
                int size2 = Integer.parseInt(file2.split("-")[1]);
                if (size1 != size2){ return Integer.compare(size2, size1); }
                else {
                    return file1.compareTo(file2);
                }
            });
            Integer removedFiles = 0;
            for (String file : matchingFiles){
                usedStorage -= Integer.parseInt(file.split("-")[1]);
                removedFiles++;
                if (usedStorage < Integer.parseInt(capaity)){
                    users.put(user, Integer.parseInt(capaity));
                    return String.valueOf(removedFiles);
                }
            }
            return String.valueOf(removedFiles);
        }
        
    }

    public String copyFile(String nameFrom, String nameTo) {
        if (!database.containsKey(nameFrom)) {
            return "false"; // Source file doesn't exist
        }
        if (database.containsKey(nameTo)) {
            return "false"; // Destination file already exists
        }
        int size = database.get(nameFrom).size;
        database.put(nameTo, new File(size, "admin"));
        return "true"; // File copied successfully
    }

    public String getFileSize(String file){
        if (database.containsKey(file)){
            return String.valueOf(database.get(file).size);
        }
        return "";
    }

    public String findFile(String prefix, String suffix){
        List<String> matchingFiles = new ArrayList<>();
        for (Map.Entry<String, File> entry : database.entrySet()){
            String key = entry.getKey();
            if (key.startsWith(prefix) && key.endsWith(suffix)){
                matchingFiles.add(key + "-" + String.valueOf(entry.getValue().size));
            }
        }
        matchingFiles.sort((file1, file2) -> {
            int size1 = Integer.parseInt(file1.split("-")[1]);
            int size2 = Integer.parseInt(file2.split("-")[1]);
            if (size1 != size2){ return Integer.compare(size2, size1); }
            else {
                return file1.compareTo(file2);
            }
        });

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchingFiles.size(); i++){
            String f = matchingFiles.get(i).split("-")[0];
            String size = matchingFiles.get(i).split("-")[1];
            if (i == matchingFiles.size()-1){
                sb.append(f + "(" + size + ")");
            }
            else {
                sb.append(f + "(" + size + "),");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args){
        CloudStorageSystem_v2 storage = new CloudStorageSystem_v2();
        // Level 1 Tests
        // System.out.println("Level 1 Tests:");
        // System.out.println(storage.addFile("/dir1/dir2/file.txt","10")); // true
        // System.out.println(storage.copyFile("/not-existing.file", "/dir1/file.txt")); // false
        // System.out.println(storage.copyFile("/dir1/dir2/file.txt", "/dir1/file.txt")); // true
        // System.out.println(storage.addFile("/dir1/file.txt", "15")); // false (file already exists)
        // System.out.println(storage.copyFile("/dir1/file.txt", "/dir1/dir2/file.txt")); // false (file exists already)
        // System.out.println(storage.getFileSize("/dir1/file.txt")); // 10
        // System.out.println(storage.getFileSize("/not-existing.file")); // ""

        // // Level 2 Tests
        // System.out.println("\nLevel 2 Tests:");
        // System.out.println(storage.addFile("/root/dir/another_dir/file.mp3", "10")); // true
        // System.out.println(storage.addFile("/root/file.mp3", "5")); // true
        // System.out.println(storage.addFile("/root/music/file.mp3", "7")); // true
        // System.out.println(storage.copyFile("/root/music/file.mp3", "/root/dir/file.mp3")); // true
        // System.out.println(storage.findFile("/root", ".mp3")); // /root/dir/another_dir/file.mp3(10), /root/dir/file.mp3(7), /root/music/file.mp3(7), /root/file.mp3(5)
        // System.out.println(storage.findFile("/root", "file.txt")); // ""
        // System.out.println(storage.findFile("/dir", "file.mp3")); // ""

        // // Level 3 Tests
        // System.out.println("\nLevel 3 Tests:");
        // System.out.println(storage.addUser("user1", "125")); // true
        // System.out.println(storage.addUser("user1", "100")); // false (user already exists)
        // System.out.println(storage.addUser("user2", "100")); // true
        // System.out.println(storage.addFileBy("user1", "/file.med", "30")); // 75
        // System.out.println(storage.addFileBy("user2", "/file.med", "40")); // ""
        // System.out.println(storage.copyFile("/file.med", "/dir/another/file.med")); // true
        // System.out.println(storage.addFileBy("admin", "/dir/file_small", "5")); // true
        // System.out.println(storage.addFileBy("user1", "/my_folder/file.huge", "100")); // false (not enough capacity)
        // System.out.println(storage.addFileBy("user3", "/my_folder/file.huge", "100")); // "" (user doesn't exist)
        // System.out.println(storage.updateCapacity("user1", "300")); // 0
        // System.out.println(storage.updateCapacity("user1", "50")); // 2 (remove 2 largest files)
        // System.out.println(storage.updateCapacity("user2", "1000")); // "" (user doesn't exist)

        // Level 4 Tests
        // System.out.println("\nLevel 4 Tests:");
        System.out.println(storage.addUser("user1", "1000")); // true
        System.out.println(storage.addUser("user2", "5000")); // true
        System.out.println(storage.addFileBy("user1", "/dir/file.mp4", "500")); // 500
        System.out.println(storage.addFileBy("user2", "/dir/file.mp4", "1")); // ""
        System.out.println(storage.compressFile("user3", "/dir/file.mp4")); // "" (user doesn't exist)
        System.out.println(storage.compressFile("user1", "/folder/non_existing_file")); // false (file doesn't exist)
        System.out.println(storage.compressFile("user1", "/dir/file.mp4")); // true
        System.out.println(storage.compressFile("user1", "/dir/file.mp4")); // false (already compressed)
        System.out.println(storage.getFileSize("/dir/file.mp4.COMPRESSED")); // Returns compressed size
        System.out.println(storage.getFileSize("/dir/file.mp4")); // Returns original file size
        System.out.println(storage.copyFile("/dir/file.mp4.COMPRESSED", "/file.mp4.COMPRESSED")); // true
        System.out.println(storage.addFileBy("user1", "/dir/file.mp4", "500")); // false (file already exists)
        System.out.println(storage.decompressFile("user1", "/dir/file.mp4.COMPRESSED")); // true
        System.out.println(storage.updateCapacity("user1", "2000")); // Updated capacity
        System.out.println(storage.decompressFile("user2", "/dir/file.mp4.COMPRESSED")); // false (user doesn't own file)
        System.out.println(storage.decompressFile("user3", "/dir/file.mp4.COMPRESSED")); // "" (user doesn't exist)
        System.out.println(storage.decompressFile("user1", "/dir/file.mp4.COMPRESSED")); // false (already decompressed)
    }

    static class File {
        String user;
        Integer size;
        public File(Integer size, String user){
            this.size = size;
            this.user = user;
        }
    }

}
