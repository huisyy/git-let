package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** Class representing a commit object.
 * @author Megan Hu*/
public class Commit implements Serializable {

    /** Constructor for the Commit class. Sets parent, message, addedFiles,
     * and removedFiles.
     * @param p parent 1 of the commit.
     * @param p2 parent 2 of the commit, if it exists.
     * @param m message associated with the commit.
     * @param addedFiles files to be added.
     * @param removedFiles files to be removed.*/
    @SuppressWarnings("unchecked")
    Commit(String p, String p2, String m, HashMap<String, String> addedFiles,
           HashMap<String, String> removedFiles) {
        this.message = m;
        if (p.equals("")) {
            this.timestamp = new SimpleDateFormat(
                    "E MMM d HH:mm:ss yyyy Z").format(new Date(0));
        } else {
            this.timestamp = new SimpleDateFormat(
                    "E MMM d HH:mm:ss yyyy Z").format(new Date());
        }
        if (!(p2.equals(""))) {
            this._parent2 = p2;
        } else {
            this._parent2 = null;
        }
        this.parent = p;
        if (!p.equals("")) {
            blobs = (HashMap<String, String>)
                    ((Commit) Main.readFile(".commits", getParent()))
                            .getBlobs().clone();
        } else {
            this.blobs = new HashMap<>();
        }

        for (String key: addedFiles.keySet()) {
            blobs.put(key, addedFiles.get(key));
        }

        for (String key: removedFiles.keySet()) {
            try {
                blobs.remove(key);
            } catch (IllegalArgumentException i) {
                System.out.println(i.getMessage());
            }
        }

        String idText = "commit";
        for (String key: blobs.keySet()) {
            idText += key;
        }
        idText = idText + p + m + timestamp;
        id = Utils.sha1(idText);
    }

    /** Returns the latest common ancestor of this and branch.
     * @param branch the name of the commit
     * @return the latest common ancestor of this and branch
     */
    public Commit findSplit(Commit branch) {
        Commit one = this;
        Commit two = branch;
        HashMap<String, Commit> oneParent = new HashMap<>();
        oneParent.put(one.message, one);
        while (!one.getParent().equals("")) {
            one = (Commit) Main.readFile(".commits", one.getParent());
            oneParent.put(one.message, one);
        }
        while (!two.getParent().equals("")) {
            if (oneParent.containsKey(two.message)) {
                return oneParent.get(two.message);
            }
            two = (Commit) Main.readFile(".commits", two.getParent());
            if (oneParent.containsKey(two.message)) {
                return oneParent.get(two.message);
            }
        }
        return null;
    }

    /** Getter method for this._parent2.
     *
     * @return the second parent of this commit
     */
    public String getParent2() {
        return this._parent2;
    }

    /** Getter method for this.id.
     *
     * @return the id of this commit
     */
    public String getId() {
        return this.id;
    }

    /** Getter method for this.parent.
     *
     * @return the parent of this commit
     */
    public String getParent() {
        return this.parent;
    }

    /** Getter method for this.timestamp.
     *
     * @return the timestamp of this commit
     */
    public String getTimestamp() {
        return this.timestamp;
    }

    /** Getter method for this.message.
     *
     * @return the message of this commit
     */
    public String getMessage() {
        return this.message;
    }

    /** Getter method for this.blobs.
     *
     * @return the blobs of this commit
     */
    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    /** The timestamp of the commit. */
    private String timestamp;
    /** The messsage of the commit. */
    private String message;
    /** The parent of the commit. */
    private String parent;
    /** The second parent of the commit, if it is a merge commit. */
    private String _parent2;
    /** The id of the commit. */
    private String id;
    /** The blobs associated with the commit. */
    private HashMap<String, String> blobs;
}
