import java.util.*;

public class CloudStorageSystem {

    // Store files in a map, key = file path, value = size
    private Map<String, Integer> files;

    // Store users with their storage capacity and file sizes
    private Map<String, Integer> userCapacities;
    private Map<String, Map<String, Integer>> userFiles;

    public CloudStorageSystem() {
        this.files = new HashMap<>();
        this.userCapacities = new HashMap<>();
        this.userFiles = new HashMap<>();
    }

    // ADD_FILE operation (admin can add files without storage limit)
    public String addFile(String name, int size) {
        if (files.containsKey(name)) {
            return "false"; // File already exists
        }
        files.put(name, size);
        return "true"; // File added successfully
    }

    // COPY_FILE operation
    public String copyFile(String nameFrom, String nameTo) {
        if (!files.containsKey(nameFrom)) {
            return "false"; // Source file doesn't exist
        }
        if (files.containsKey(nameTo)) {
            return "false"; // Destination file already exists
        }
        int size = files.get(nameFrom);
        files.put(nameTo, size);
        return "true"; // File copied successfully
    }

    // GET_FILE_SIZE operation
    public String getFileSize(String name) {
        if (!files.containsKey(name)) {
            return ""; // File doesn't exist
        }
        return String.valueOf(files.get(name));
    }

    // FIND_FILE operation
    public String findFile(String prefix, String suffix) {
        List<String> matchingFiles = new ArrayList<>();
        
        // Search for matching files
        for (Map.Entry<String, Integer> entry : files.entrySet()) {
            String fileName = entry.getKey();
            int size = entry.getValue();
            if (fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
                matchingFiles.add(fileName + "(" + size + ")");
            }
        }

        // If no files found, return an empty string
        if (matchingFiles.isEmpty()) {
            return "";
        }

        // Sort files by size (descending) and then lexicographically for tie-break
        matchingFiles.sort((f1, f2) -> {
            // Extract sizes from filenames to compare them
            int size1 = Integer.parseInt(f1.split("\\(")[1].replace(")", ""));
            int size2 = Integer.parseInt(f2.split("\\(")[1].replace(")", ""));
            // Sort by size descending, then lexicographically ascending
            if (size1 != size2) {
                return Integer.compare(size2, size1);
            } else {
                return f1.compareTo(f2);
            }
        });

        // Join the files into a single string and return
        return String.join(", ", matchingFiles);
    }

    // ADD_USER operation
    public String addUser(String userId, int capacity) {
        if (userCapacities.containsKey(userId)) {
            return "false"; // User already exists
        }
        userCapacities.put(userId, capacity);
        userFiles.put(userId, new HashMap<>()); // Initialize user's file collection
        return "true"; // User added successfully
    }

    // ADD_FILE_BY operation (users with capacity limits)
    public String addFileBy(String userId, String name, int size) {
        if (!userCapacities.containsKey(userId)) {
            return ""; // User doesn't exist
        }

        int currentUsedSize = userFiles.get(userId).values().stream().mapToInt(Integer::intValue).sum();
        int newSize = currentUsedSize + size;

        // Check if the user has enough space
        if (newSize > userCapacities.get(userId)) {
            return ""; // Not enough space
        }

        // Add the file for the user
        userFiles.get(userId).put(name, size);
        return String.valueOf(userCapacities.get(userId) - newSize); // Return remaining space
    }

    // UPDATE_CAPACITY operation
    public String updateCapacity(String userId, int newCapacity) {
        if (!userCapacities.containsKey(userId)) {
            return ""; // User doesn't exist
        }

        // Update the user's capacity
        userCapacities.put(userId, newCapacity);

        // Get all files owned by the user and their sizes
        List<Map.Entry<String, Integer>> filesOwned = new ArrayList<>(userFiles.get(userId).entrySet());
        filesOwned.sort((f1, f2) -> {
            int size1 = f1.getValue();
            int size2 = f2.getValue();
            if (size1 != size2) {
                return Integer.compare(size2, size1); // Sort by size, descending
            } else {
                return f1.getKey().compareTo(f2.getKey()); // Tie-break lexicographically
            }
        });

        // Calculate the total size of files and remove the largest ones if necessary
        int totalSize = filesOwned.stream().mapToInt(Map.Entry::getValue).sum();
        List<String> removedFiles = new ArrayList<>();

        while (totalSize > newCapacity && !filesOwned.isEmpty()) {
            Map.Entry<String, Integer> largestFile = filesOwned.remove(0); // Remove largest file
            userFiles.get(userId).remove(largestFile.getKey());
            totalSize -= largestFile.getValue();
            removedFiles.add(largestFile.getKey());
        }

        return removedFiles.size() > 0 ? String.valueOf(removedFiles.size()) : ""; // Return number of removed files
    }

    public static void main(String[] args) {
        CloudStorageSystem storage = new CloudStorageSystem();

        // Level 1 Tests
        System.out.println("Level 1 Tests:");
        System.out.println(storage.addFile("/dir1/dir2/file.txt", 10)); // true
        System.out.println(storage.copyFile("/not-existing.file", "/dir1/file.txt")); // false
        System.out.println(storage.copyFile("/dir1/dir2/file.txt", "/dir1/file.txt")); // true
        System.out.println(storage.addFile("/dir1/file.txt", 15)); // false (file already exists)
        System.out.println(storage.copyFile("/dir1/file.txt", "/dir1/dir2/file.txt")); // false (file exists already)
        System.out.println(storage.getFileSize("/dir1/file.txt")); // 10
        System.out.println(storage.getFileSize("/not-existing.file")); // ""

        // Level 2 Tests
        System.out.println("\nLevel 2 Tests:");
        System.out.println(storage.addFile("/root/dir/another_dir/file.mp3", 10)); // true
        System.out.println(storage.addFile("/root/file.mp3", 5)); // true
        System.out.println(storage.addFile("/root/music/file.mp3", 7)); // true
        System.out.println(storage.copyFile("/root/music/file.mp3", "/root/dir/file.mp3")); // true
        System.out.println(storage.findFile("/root", ".mp3")); // /root/dir/another_dir/file.mp3(10), /root/dir/file.mp3(7), /root/music/file.mp3(7), /root/file.mp3(5)
        System.out.println(storage.findFile("/root", "file.txt")); // ""
        System.out.println(storage.findFile("/dir", "file.mp3")); // ""

        // Level 3 Tests
        System.out.println("\nLevel 3 Tests:");
        System.out.println(storage.addUser("user1", 125)); // true
        System.out.println(storage.addUser("user1", 100)); // false (user already exists)
        System.out.println(storage.addUser("user2", 100)); // true
        System.out.println(storage.addFileBy("user1", "/file.med", 30)); // 75
        System.out.println(storage.addFileBy("user2", "/file.med", 40)); // ""
        System.out.println(storage.copyFile("/file.med", "/dir/another/file.med")); // true
        System.out.println(storage.copyFile("/file.med", "/file.small")); // false
        System.out.println(storage.addFileBy("admin", "/dir/file_small", 5)); // true
        System.out.println(storage.addFileBy("user1", "/my_folder/file.huge", 100)); // false (not enough capacity)
        System.out.println(storage.addFileBy("user3", "/my_folder/file.huge", 100)); // "" (user doesn't exist)
        System.out.println(storage.updateCapacity("user1", 300)); // 0
        System.out.println(storage.updateCapacity("user1", 50)); // 2 (remove 2 largest files)
        System.out.println(storage.updateCapacity("user2", 1000)); // "" (user doesn't exist)
    }
}
