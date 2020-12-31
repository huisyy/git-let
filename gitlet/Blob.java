package gitlet;

import java.io.Serializable;

/** Class representing the contents of a file.
 * @author Megan Hu*/
public class Blob implements Serializable {

    /** Constructor for the Blob Class. Sets name, content, and hashValue.
     * @param n the blob's name
     * @param c the blob's content*/
    Blob(String n, String c) {
        this.name = n;
        this.content = c;
        String id = "Blob" + name + content;
        this.hashValue = Utils.sha1(id);
    }

    /** Getter method for this.hashValue.
     * @return this.hashValue*/
    public String getHashValue() {
        return this.hashValue;
    }

    /** Getter method for this.content.
     * @return this.content*/
    public String getContent() {
        return this.content;
    }

    /** The name of the file. */
    private String name;

    /** Value representing the hashvalue of this blob. */
    private String hashValue;

    /** Contents of the file, serialized using Utils.sha1. */
    private String content;
}
