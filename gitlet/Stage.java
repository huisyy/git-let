package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** Class representing the staging area.
 * @author Megan Hu*/
public class Stage implements Serializable {

    /** Constructor for a Stage object. Initializes stagedAdded, stagedRemoved,
     * trackedModified, trackedDeleted, and untracked.
     */
    Stage() {
        stagedAdded = new HashMap<>();
        stagedRemoved = new HashMap<>();
        trackedModified = new HashMap<>();
        trackedDeleted = new HashMap<>();
        untracked = new HashMap<>();
    }

    /** Adds blob associated with fileName to the given commit curr.
     * If blob object exists, adds blob id and fileName to curr. If not, creates
     * the blob and adds id and fileName to curr. Updates the file.
     * @param fileName name of the file
     * @param curr name of the commit
     */
    void add(String fileName, Commit curr) {
        String id;
        boolean changed = false;

        if (!stagedRemoved.containsKey(fileName)) {
            String blobContent = Main.readCWDFileToString("", fileName);
            if (blobContent.equals("")) {
                return;
            }
            if (currBlobs.containsKey(fileName)) {
                changed = changed(fileName, blobContent, true);
                if (!changed) {
                    return;
                }
            }
            if (changed || !currBlobs.containsKey(fileName)) {
                Blob temp = new Blob(fileName, blobContent);
                Main.writeFile(".stagedblobs", temp.getHashValue(), temp);
                id = temp.getHashValue();
            } else {
                id = currBlobs.get(fileName);
            }
        } else {
            Main.writeToCWD("", fileName, getCurrBlob(fileName, true));
            id = currBlobs.get(fileName);
        }
        if (trackedDeleted.containsKey(fileName)) {
            stagedRemoved.put(fileName, id);
            trackedDeleted.remove(fileName);
        } else if (stagedRemoved.containsKey(fileName)) {
            stagedRemoved.remove(fileName);
        } else if (trackedModified.containsKey(fileName)) {
            stagedAdded.put(fileName, id);
            trackedModified.remove(fileName);
        } else {
            stagedAdded.put(fileName, id);
            untracked.remove(fileName);
        }
    }

    /** Checks if modifications have occurred in the CWD, updating
     * trackedModified, trackedDeleted, and untracked.
     */
    public void check() {
        ArrayList<String> cwd = Main.listDirFiles("");
        if (!currBlobs.isEmpty()) {
            String content;

            for (String fileName: cwd) {
                content = Main.readCWDFileToString("", fileName);
                if ((!currBlobs.containsKey(fileName)
                        && !stagedAdded.containsKey(fileName))
                        || (!currBlobs.containsKey(fileName)
                                && stagedAdded.containsKey(fileName)
                                && changed(fileName, content, false))) {
                    untracked.put(fileName, null);
                } else if (currBlobs.containsKey(fileName)
                        && changed(fileName, content, true)
                        && !stagedAdded.containsKey(fileName)) {
                    trackedModified.put(fileName, null);
                }
            }
            for (String key: currBlobs.keySet()) {
                if (!cwd.contains(key)) {
                    stagedRemoved.put(key, currBlobs.get(key));
                }
            }
            for (String fileName: untracked.keySet()) {
                if (!cwd.contains(fileName)) {
                    untracked.remove(fileName);
                }
            }
        } else {
            if (currBlobs.isEmpty() && !cwd.isEmpty()) {
                for (String fileName: cwd) {
                    if (!stagedAdded.containsKey(fileName)
                            && !stagedRemoved.containsKey(fileName)) {
                        untracked.put(fileName, null);
                    }
                }
            }
        }
    }

    /** Unstage the file if currently staged for addition.
     * If the file is tracked, stage for removal by adding to stagedRemove.
     * @param fileName name of the file
     * @param curr the current commit
     * */
    void rm(String fileName, Commit curr) {
        if (!stagedAdded.containsKey(fileName)
                && !currBlobs.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        }
        if (stagedAdded.containsKey(fileName)) {
            stagedAdded.remove(fileName);
        }
        if (currBlobs.containsKey(fileName)) {
            stagedRemoved.put(fileName, currBlobs.get(fileName));
            if (Main.listDirFiles("").contains(fileName)) {
                Main.deleteCWDFile("", fileName);
            }
        }
    }

    /** Checks to see if the version of the file with name fileName in the
     * current working directory has changed from the previous commit.
     * @param fileName name of the file
     * @param newBlob the blob we are checking
     * @param tracked boolean representing whether the file is tracked or not
     * @return boolean representing if the file has been changed
     */
    private boolean changed(String fileName, String newBlob, boolean tracked) {
        String existingBlob = getCurrBlob(fileName, tracked);
        return !(newBlob.equals(existingBlob));
    }

    /** Returns the contents of the last commit made.
     *
     * @param fileName name of the file
     * @param tracked boolean representing whether the file is tracked or not
     * @return the contents of the last commit made
     */
    private String getCurrBlob(String fileName, boolean tracked) {
        Object curr;
        if (tracked) {
            curr = Main.readFile(".blobs", currBlobs.get(fileName));
        } else {
            curr = Main.readFile(".stagedblobs", stagedAdded.get(fileName));
        }
        return ((Blob) curr).getContent();
    }

    /** Sets currBlobs to the blobs in Commit curr.
     *
     * @param curr the current commit
     */
    public void setCurrentBlobs(Commit curr) {
        currBlobs = curr.getBlobs();
    }

    /** Clears stagedAdded, stagedRemoved, trackedModified, and trackedDeleted.
     *
     */
    public void clear() {
        stagedAdded.clear();
        stagedRemoved.clear();
        trackedModified.clear();
        trackedDeleted.clear();
    }

    /** Helper method for getting and formatting variables for method status.
     *
     * @return String representing status of the stage
     */
    public String status() {
        check();
        String ret = "";
        ret += "=== Staged Files ===" + "\n";
        ArrayList<String> added = new ArrayList<String>();
        for (String key: stagedAdded.keySet()) {
            added.add(key);
        }
        Collections.sort(added);
        for (String s: added) {
            ret += s + "\n";
        }
        ret += "\n";

        ret += "=== Removed Files ===" + "\n";
        ArrayList<String> removed = new ArrayList<String>();
        for (String key: stagedRemoved.keySet()) {
            removed.add(key);
        }
        Collections.sort(removed);
        for (String s: removed) {
            ret += s + "\n";
        }
        ret += "\n";

        ret += "=== Modifications Not Staged For Commit ===" + '\n';
        ArrayList<String> mod = new ArrayList<String>();
        for (String key: trackedDeleted.keySet()) {
            mod.add(key + " (deleted)\n");
        }
        for (String key: trackedModified.keySet()) {
            mod.add(key + " (modified)\n");
        }
        Collections.sort(mod);
        for (String s: mod) {
            ret += s;
        }
        ret += "\n";

        ret += "=== Untracked Files ===" + "\n";
        ArrayList<String> untr = new ArrayList<String>();
        for (String key: untracked.keySet()) {
            untr.add(key);
        }
        Collections.sort(untr);
        for (String s: untr) {
            ret += s + "\n";
        }

        return ret;
    }

    /** Helper method for updating system state after merge.
     * @param newBlobs blobs that are new and need to be added
     * @param removedBlobs blobs to be removed
     * @param conflictFiles blobs in conflict
     */
    public void postMerge(HashMap newBlobs, HashMap removedBlobs,
                          HashMap conflictFiles) {
        for (Object key: newBlobs.keySet()) {
            String k = (String) key;
            add(k, Main.mainTree().getCurrBranchCommit());
        }
        for (Object key: removedBlobs.keySet()) {
            String k = (String) key;
            stagedRemoved.put(k, (String) (removedBlobs.get(k)));
        }
        for (Object key: conflictFiles.keySet()) {
            String k = (String) key;
            trackedModified.put(k, null);
        }
    }

    /** Getter method for untracked.
     *
     * @return untracked
     */
    public HashMap<String, String> getUntracked() {
        return untracked;
    }

    /** Getter method for stagedAdded.
     *
     * @return stagedAdded
     */
    public HashMap<String, String> getStagedAdded() {
        return stagedAdded;
    }

    /** Getter method for getStagedRemoved.
     * @return stagedRemoved*/
    public HashMap<String, String> getStagedRemoved() {
        return stagedRemoved;
    }

    /** HashMap hashing fileNames to blob strings
     * for files to be added. */
    private HashMap<String, String> stagedAdded;
    /** HashMap hashing fileNames to blob strings
     * for files to be removed. */
    private HashMap<String, String> stagedRemoved;
    /** HashMap hashing fileNames to blob strings
     * for files that are modified. */
    private HashMap<String, String> trackedModified;
    /** HashMap hashing fileNames to blob strings
     * for files to be deleted. */
    private HashMap<String, String> trackedDeleted;
    /** HashMap hashing fileNames to blob strings
     *  for files that are untracked. */
    private HashMap<String, String> untracked;
    /** HashMap hashing fileNames to blobs
     * associated with the current commit. */
    private HashMap<String, String> currBlobs;
}
