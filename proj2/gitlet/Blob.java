package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Utils.*;

/**
 *
 * @author Hongfa You
 */

public class Blob implements Serializable {
    /** Map from filename to SHA1 for each added file */
    /* TreeMap for staged. */
    protected static TreeMap<String, String> blobMap;
    /* TreeMap for unstaged. */
    protected static TreeMap<String, String> removal;

    /** Serialize and save file named "name" in CWD into .gitlet/staged_obj in CWD
     *  and update the blobMap to File System.
     *  @param name : name of file.
     */
    public static void add(String name) {
        String sha1Id = Utils.sha1(name);
        File file = Utils.join(Repository.CWD, name);
        File outfile = Utils.join(Repository.STAGE_DIR, sha1Id);
        secureCopyFile(file, outfile);
        putBlobMap(sha1Id, name);
        saveBlobMap();
    }

    /** Do staging work for merge.
     *  Copy "name" file from COMMITED_DIR to STAGE_DIR.
     *  @param name : name of file.
     *  @param shaId : SHA1 String of Commit.
     *  */
    public static void stageForMerge(String name, String shaId) {
        File srcfile = Utils.join(Repository.COMMITED_DIR, shaId);
        String sha1Id = Utils.sha1(name);
        File destfile = Utils.join(Repository.STAGE_DIR, sha1Id);
        secureCopyFile(srcfile, destfile);
        putBlobMap(sha1Id, name);
        saveBlobMap();
    }

    /** Add file whose name is "name" to removal.
     *  @param name : name of file.
     *  @param toRemoval : whether put it to removal (unstaging area) or not.
     * */
    public static void remove(String name, boolean toRemoval) {
        String sha1Id = Utils.sha1(name);
        if (toRemoval) {
            putremoval(name, sha1Id);
            saveremoval();
        }
    }

    /** Load blobMap from file system, if not exists create new one.
     *  Then put the key-value pair <SHA1, name> of the new-added file into it.
     * @param key SHA1 of the new-added file.
     * @param value name of the new-added file.
     */
    public static void putBlobMap(String key, String value) {
        blobMap = getTreeMap(blobMap, false);
        blobMap.put(key, value);
    }

    /** Load removal from file system, if not exists create new one.
     *  Then put the key-value pair <SHA1, name> of the new-removed file into it.
     * @param key SHA1 of the new-removed file.
     * @param value name of the new-removed file.
     */
    public static void putremoval(String key, String value) {
        removal = getTreeMap(removal, true);
        removal.put(key, value);
    }

    /** Load blobMap from file system, if not exists create new one.
     *  */
    public static void loadBlobMap() {
        blobMap = getTreeMap(blobMap, false);
    }

    /** Load removal from file system, if not exists create new one.
     *  */
    public static void loadremoval() {
        removal = getTreeMap(removal, true);
    }

    /** Save removal into file system.
     *  Should be called after loadremoval().
     */
    public static void saveremoval() {
        File blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "removal");
        writeObject(blobmapfile, removal);
    }

    /** Save blobMap into file system.
     *  Should be called after loadBlobMap().
     */
    public  static void saveBlobMap() {
        File blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "blobMap");
        writeObject(blobmapfile, blobMap);
    }

    /** Get blobMap or removal from file system.
     *  @param map : removal or blobMap.
     *  @param isRemoval : whether to get TreeMap of removal or not. if not, get TreeMap of blobMap.
     * @return TreeMap.
     */
    public static TreeMap getTreeMap(TreeMap<String, String> map, boolean isRemoval) {
        File blobmapfile;
        if (isRemoval) {
            blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "removal");
        } else {
            blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "blobMap");
        }
        if (blobmapfile.exists()) {
            map = readObject(blobmapfile, TreeMap.class);
        } else {
            map = new TreeMap<>();
        }
        return map;
    }

    /** Delete blobMap from file system.
     *  It is used for clear staged imformation.
     */
    public static void deleteBlobMap() {
        File blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "blobMap");
        if (blobmapfile.exists()) {
            blobmapfile.delete();
        }
    }

    /** Delete removal from file system.
     *  It is used for clear staged imformation.
     */
    public static void deleteRemoval() {
        File removalfile = Utils.join(Repository.INFOSTAGE_DIR, "removal");
        if (removalfile.exists()) {
            removalfile.delete();
        }
    }

    /** Delete file from blobMap if it exists in blobMap.
     * @param key : name of file.
     * */
    public static void deteleItem(String key) {
        File blobmapfile = Utils.join(Repository.INFOSTAGE_DIR, "blobMap");
        if (blobmapfile.exists()) {
            blobMap = readObject(blobmapfile, TreeMap.class);
            blobMap.remove(Utils.sha1(key));
            saveBlobMap();
        }
    }

    /** Check if removal contains file "name".
     * @param name : name of file.
     * @return True if file exists in removal, or return false.
     */
    public static boolean isRemovalContains(String name) {
        removal = getTreeMap(removal, true);
        if (removal.containsKey(name)) {
            return true;
        }
        return false;
    }

    /** Check if blobMap contains file "name".
     * @param name : name of file.
     * @return True if file exists in blobMap, or return false.
     */
    public static boolean isBlobmapContains(String name) {
        blobMap = getTreeMap(blobMap, false);
        if (blobMap.containsValue(name)) {
            return true;
        }
        return false;
    }

    /** Check if unstage area is empty.
     *  @return True if removal is null or empty, or return false.
     * */
    public static boolean isRemovalEmpty() {
        removal = getTreeMap(removal, true);
        if (removal == null || removal.isEmpty()) {
            return true;
        }
        return false;
    }

    /** Check if staging area is empty.
     *  @return True if blobMap is null or empty, or return false.
     * */
    public static boolean isBlobMapEmpty() {
        blobMap = getTreeMap(blobMap, true);
        if (blobMap == null || blobMap.isEmpty()) {
            return true;
        }
        return false;
    }

    /** Unremove the file "name".
     * @param name : name of file.
     * */
    public static void unremove(String name) {
        removal = getTreeMap(removal, true);
        removal.remove(name);
        saveremoval();
    }
}
