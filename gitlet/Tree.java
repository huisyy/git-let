package gitlet;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/** Class representing the commit tree structure of gitlet.
 * @author Megan Hu*/
public class Tree implements Serializable {

    /** The current working directory. */
    private static String sysDir = System.getProperty("user.dir");

    /** Constructor for the Tree class, initializing branches and
     * the initial commit. */
    Tree() {
        branches = new HashMap<>();
        Commit newComm = new Commit("", "", "initial commit",
                new HashMap<String, String>(), new HashMap<String, String>());
        Main.writeFile(".commits", newComm.getId(), newComm);
        branches.put("master", newComm.getId());
        currBranch = "master";
    }

    /** Adds commit to this tree.
     * @param parent2 second parent if this commit is a merge commit
     * @param message message of the commit
     * @param toAdd blobs that are to be added for the commit
     * @param toRemove blobs that are to be removed for the commit
     */
    public void addCommit(String parent2, String message,
                          HashMap<String, String> toAdd,
                          HashMap<String, String> toRemove) {
        Commit newComm = new Commit(getCurrBranchCommit().getId(), parent2,
                message, toAdd, toRemove);
        Main.writeFile(".commits", newComm.getId(), newComm);

        ArrayList<Object> files = Main.readFiles(".stagedblobs");
        for (Object file: files) {
            Blob temp = (Blob) file;
            Main.writeFile(".blobs", temp.getHashValue(), temp);
            Main.deleteFile(".stagedblobs", temp.getHashValue());
        }

        branches.put(currBranch, newComm.getId());
        Main.mainStage().setCurrentBlobs(getCurrBranchCommit());
        Main.mainStage().clear();
    }

    /** Helper method for getting and formatting values for the status command.
     *
     * @return String representing the status of the tree
     */
    public String status() {
        ArrayList<String> b = new ArrayList<>();
        for (String branch: branches.keySet()) {
            b.add(branch);
        }
        Collections.sort(b);
        String ret = "";
        for (String branch: b) {
            if (branch.equals(currBranch)) {
                ret += "*" + branch + "\n";
            } else {
                ret += branch + "\n";
            }
        }
        return ret;
    }

    /** Helper method for getting and formatting values for the log command.
     *
     * @return String representing the log of this tree
     */
    public String log() {
        String ret = "";
        Commit currentCommit = getCurrBranchCommit();

        ret += "===\n";
        ret += "commit " + currentCommit.getId() + "\n";
        if (currentCommit.getParent2() != null) {
            ret += "Merge: " + currentCommit.getParent().substring(0, 7) + " "
                    + currentCommit.getParent2().substring(0, 7) + "\n";
        }
        ret += "Date: " + currentCommit.getTimestamp() + "\n";
        ret += currentCommit.getMessage() + "\n";
        ret += "\n";
        Commit c = currentCommit;
        while (!(c.getParent().equals(""))) {
            Commit curr = (Commit) Main.readFile(".commits", c.getParent());
            ret += "===\n";
            ret += "commit " + curr.getId() + "\n";
            if (curr.getParent2() != null) {
                ret += "Merge: " + curr.getParent().substring(0, 7) + " "
                        + curr.getParent2().substring(0, 7) + "\n";
            }
            ret += "Date: " + curr.getTimestamp() + "\n";
            ret += curr.getMessage() + "\n";
            ret += "\n";
            c = (Commit) Main.readFile(".commits", c.getParent());
        }
        return ret.substring(0, ret.length() - 1);
    }

    /** Helper method for getting and formatting values for the
     * global-log command.
     * @return String representing the global log of this tree*/
    public String globalLog() {
        ArrayList com = Main.readFiles(".commits");
        String ret = "";
        for (Object commit: com) {
            Commit curr = (Commit) commit;
            ret += "===\n";
            ret += "commit " + curr.getId() + "\n";
            if (curr.getParent2() != null) {
                ret += "Merge: " + curr.getParent().substring(0, 7) + " "
                        + curr.getParent2().substring(0, 7) + "\n";
            }
            ret += "Date: " + curr.getTimestamp() + "\n";
            ret += curr.getMessage() + "\n";
            ret += "\n";
        }
        return ret.substring(0, ret.length() - 1);
    }

    /** Helper method for getting the String representing the commit with
     * the message message.
     * @param message the message of the commit
     * @return String representing the commit with message message
     */
    public String find(String message) {
        ArrayList com = Main.readFiles(".commits");
        String ret = "";
        for (Object commit: com) {
            Commit curr = (Commit) commit;
            if (curr.getMessage().equals(message)) {
                ret += curr.getId();
                ret += "\n";
            }
        }
        if (ret.equals("")) {
            return ret;
        }
        return ret.substring(0, ret.length() - 1);
    }

    /** Returns the latest commit associated with the current branch.
     *
     * @return the latest commit of the current branch
     */
    public Commit getCurrBranchCommit() {
        String currCommitId = branches.get(currBranch);
        return (Commit) Main.readFile(".commits", currCommitId);
    }

    /** Helper method for the first case of checkout command, writing the
     * file with the name fileName to the current working directory.
     * @param fileName name of the file
     */
    public void checkoutFile(String fileName) {
        HashMap<String, String> currBlobs = getCurrBranchCommit().getBlobs();
        if (currBlobs.containsKey(fileName)) {
            Blob toAdd = (Blob) Main.readFile(".blobs",
                    currBlobs.get(fileName));
            Main.writeToCWD("", fileName, toAdd.getContent());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /** Helper method for the second case of checkout command, writing the
     * file with the name fileName in the commit with id id to the current
     * working directory.
     * @param id id of the commit
     * @param fileName name of the file
     * */
    public void checkoutCommit(String id, String fileName) {
        if (commitExists(id).equals("")) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit com = (Commit) Main.readFile(".commits", id);
        HashMap<String, String> comBlobs = com.getBlobs();
        if (comBlobs.containsKey(fileName)) {
            Blob toAdd = (Blob) Main.readFile(".blobs",
                    comBlobs.get(fileName));
            Main.writeToCWD("", fileName, toAdd.getContent());
        } else {
            System.out.println("File does not exist in that commit.");
        }

    }

    /** Returns the id of commit with id id if exists,
     * else returns an empty string.
     *
     * @param id id of the commit
     * @return id of the commit if exists, else empty string
     */
    public String commitExists(String id) {
        Path path = Paths.get(System.getProperty("user.dir"), ".commits");
        ArrayList files = Main.readFiles(".commits");

        for (Object file: files) {
            Commit curr = (Commit) file;
            if (curr.getId().substring(0, id.length()).equals(id)) {
                return curr.getId();
            }
        }
        return "";
    }

    /** Helper method for the third case of checkout command.
     * Takes all files in the commit at the head of the branch
     * with name branchName and puts them in the working directory.
     * @param branchName name of the branch
     * */
    public void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit givenCommit = (Commit)
                Main.readFile(".commits", branches.get(branchName));
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();

        Main.mainStage().check();
        HashMap<String, String> untracked = Main.mainStage().getUntracked();
        if (!untracked.isEmpty()) {
            for (Map.Entry file : untracked.entrySet()) {
                String f = (String) file.getKey();
                if (givenBlobs.containsKey(f)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    return;
                }
            }
        }
        for (Map.Entry key: getCurrBranchCommit().getBlobs().entrySet()) {
            String k = (String) key.getKey();
            Main.deleteCWDFile("", k);
        }

        for (String key : givenBlobs.keySet()) {
            String content = ((Blob) Main.readFile(".blobs",
                    givenBlobs.get(key))).getContent();
            Main.writeToCWD("", key, content);
        }
        currBranch = branchName;
        Main.mainStage().setCurrentBlobs(givenCommit);
        Main.mainStage().clear();
    }

    /** Creates a new branch with name branchName.
     *
     * @param branchName name of the branch
     */
    public void branch(String branchName) {
        if (!branches.containsKey(branchName)) {
            branches.put(branchName, branches.get(currBranch));
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    /** Removes the branch with name branchName, if the branch is
     * not the current branch and it exists.
     * @param branchName name of the branch
     */
    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
        }
    }

    /** Checks out all files tracked by commit commit, removing
     * tracked files that are not present
     * and moving the current branch's head to that commit node.
     * @param commit string representing the commit
     * */
    public void reset(String commit) {
        if (commitExists(commit).equals("")) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit com = (Commit) Main.readFile(".commits", commit);
        HashMap<String, String> comBlobs = com.getBlobs();
        HashMap<String, String> currUntracked =
                Main.mainStage().getUntracked();
        if (!currUntracked.isEmpty()) {
            for (String file: currUntracked.keySet()) {
                if (comBlobs.containsKey(file)) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                    return;
                }
            }
        }
        for (String fileName: comBlobs.keySet()) {
            String[] args = ("checkout " + commit + " -- " + fileName)
                    .split(" ");
            Main.runCommands(args);
        }
    }

    /** Helper method for implementing the merge command, merging files
     * from branch branchName into the current branch.
     * @param branchName name of the branch
     * @param mainStage the mainStage
     */
    public void merge(String branchName, Stage mainStage) {
        if (!mainStage.getStagedAdded().isEmpty()
                || !mainStage.getStagedRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit com = (Commit) Main.readFile(".commits",
                branches.get(branchName));
        Commit splitPoint = getSplitPoint(branchName, com);
        if (splitPoint == null) {
            return;
        }

        HashMap<String, String> comBlobs = com.getBlobs();
        HashMap<String, String> splitPointBlobs = splitPoint.getBlobs();
        HashMap<String, String> currBlobs = getCurrBranchCommit().getBlobs();
        HashMap<String, String> removedBlobs = new HashMap<String, String>();
        HashMap<String, String> addBlobs = new HashMap<String, String>();
        HashMap<String, String> conflictFiles = new HashMap<String, String>();

        Main.writeFile("", ".temp_stage", Main.mainStage());
        Main.mainStage().check();
        updateCWDStage(comBlobs, splitPointBlobs, currBlobs, removedBlobs,
                addBlobs, conflictFiles, com);

        HashMap<String, String> untrack = Main.mainStage().getUntracked();
        if (!untrack.isEmpty()) {
            for (String file : untrack.keySet()) {
                if (addBlobs.containsKey(file)
                        || removedBlobs.keySet().contains(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    Main.setStage((Stage) Main.readFile("",
                            ".temp_stage"));
                    Main.deleteFile(sysDir, ".temp_stage");
                    return;
                }
            }
        }
        for (Object key: currBlobs.keySet()) {
            String k = (String) key;
            if (splitPointBlobs.containsKey(k) && !comBlobs.containsKey(key)) {
                if (currBlobs.get(k).equals(splitPointBlobs.get(k))) {
                    Main.deleteCWDFile("", k);
                }
            }
        }
        Main.mainStage().postMerge(addBlobs, removedBlobs, conflictFiles);
        mergeLatter(conflictFiles, addBlobs, removedBlobs, branchName, com);
    }

    /** Helper method to help with merge's length.
     *
     * @param conflictFiles conflict files
     * @param addBlobs blobs to add
     * @param removedBlobs blobs to remove
     * @param branchName branch name
     * @param com commit
     */
    public void mergeLatter(HashMap<String, String> conflictFiles,
                            HashMap<String, String> addBlobs,
                            HashMap<String, String> removedBlobs,
                            String branchName, Commit com) {
        if (conflictFiles.isEmpty()) {
            String mess = "Merged " + branchName + " into " + currBranch + ".";
            addCommit(com.getId(), mess, addBlobs, removedBlobs);
        } else {
            ArrayList<String> cwd = Main.listDirFiles("");
            for (String f: cwd) {
                if (conflictFiles.containsKey(f)) {
                    Main.writeToCWD("", f, (String) (conflictFiles.get(f)));
                }
            }
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**Updates the current working directory and stage, updating the HashMaps
     * given for use in the merge function.
     * @param comBlobs blobs of the given commit
     * @param splitPointBlobs blobs of the split point commit
     * @param currBlobs blobs of the current commit
     * @param removedBlobs blobs that will be removed
     * @param addBlobs blobs that will be added
     * @param conflictFiles blobs that are in conflict
     * @param com the commit
     */
    private void updateCWDStage(HashMap<String, String> comBlobs,
                                HashMap<String, String> splitPointBlobs,
                                HashMap<String, String> currBlobs,
                                HashMap<String, String> removedBlobs,
                                HashMap<String, String> addBlobs,
                                HashMap<String, String> conflictFiles,
                                Commit com) {
        for (Object c: currBlobs.keySet()) {
            String k = (String) c;
            if (!comBlobs.containsKey(k) && !splitPointBlobs.containsKey(k)) {
                addBlobs.put(k, currBlobs.get(k));
            }
            if (!comBlobs.containsKey(k) && splitPointBlobs.containsKey(k)) {
                if (currBlobs.get(k).equals(splitPointBlobs.get(k))) {
                    removedBlobs.put(k, comBlobs.get(k));
                }
            }
            if (!comBlobs.containsKey(k) && currBlobs.containsKey(k)
                    && splitPointBlobs.containsKey(k)) {
                if (!splitPointBlobs.get(k).equals(currBlobs.get(k))) {
                    writeConflictFile(currBlobs, comBlobs, conflictFiles, k);
                }
            }
        }
        for (String k: comBlobs.keySet()) {
            if (currBlobs.containsKey(k) && splitPointBlobs.containsKey(k)) {
                if (splitPointBlobs.get(k).equals(currBlobs.get(k))) {
                    addBlobs.put(k, comBlobs.get(k));
                } else if (!splitPointBlobs.get(k).equals(currBlobs.get(k))) {
                    addBlobs.put(k, splitPointBlobs.get(k));
                }
            }
            if (!currBlobs.containsKey(k) && !splitPointBlobs.containsKey(k)) {
                checkoutCommit(com.getId(), k);
                addBlobs.put(k, comBlobs.get(k));
            }
            if (splitPointBlobs.containsKey(k) && currBlobs.containsKey(k)) {
                if (!currBlobs.get(k).equals(comBlobs.get(k))
                        && !comBlobs.get(k).equals(splitPointBlobs.get(k))
                        && !currBlobs.get(k).equals(splitPointBlobs.get(k))) {
                    writeConflictFile(currBlobs, comBlobs, conflictFiles, k);
                }
            }
            if (currBlobs.containsKey(k) && !splitPointBlobs.containsKey(k)) {
                if (!comBlobs.get(k).equals(currBlobs.get(k))) {
                    writeConflictFile(currBlobs, comBlobs, conflictFiles, k);
                }
            }
            if (!currBlobs.containsKey(k) && comBlobs.containsKey(k)
                    && splitPointBlobs.containsKey(k)) {
                if (!comBlobs.get(k).equals(splitPointBlobs.get(k))) {
                    writeConflictFile(currBlobs, comBlobs, conflictFiles, k);
                }
            }
        }
    }

    /** Creates the conflict file with file k in currBlobs and comBlobs,
     * for use in the merge function.
     * @param currBlobs the current commit's blobs
     * @param comBlobs blobs of the given commit
     * @param conflictFiles blobs that are in conflict
     * @param k the file name
     */
    public void writeConflictFile(HashMap<String, String> currBlobs,
                                  HashMap<String, String> comBlobs,
                                  HashMap<String, String> conflictFiles,
                                  String k) {
        String message = "<<<<<<< HEAD" + "\n";
        if (!currBlobs.containsKey(k)) {
            message += "";
        } else {
            Blob curr = (Blob) Main.readFile(".blobs",
                    (String) currBlobs.get(k));
            message += curr.getContent();
        }
        message += "=======" + "\n";
        if (!comBlobs.containsKey(k)) {
            message += "";
        } else {
            Blob given = (Blob) Main.readFile(".blobs",
                    (String) comBlobs.get(k));
            message += given.getContent();
        }
        message += ">>>>>>>\n";
        conflictFiles.put(k, message);
    }

    /** Gets the split point for branch branchName and the current branch,
     * taking into account special cases.
     * @param  branchName name of the branch
     * @param comm the commit
     * @return the split point commit
     */
    public Commit getSplitPoint(String branchName, Commit comm) {
        Commit splitPoint = getCurrBranchCommit().findSplit(comm);
        if (splitPoint.equals(comm)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            return null;
        }
        if (splitPoint.equals(getCurrBranchCommit())) {
            currBranch = branchName;
            System.out.println("Current branch fast-forwarded.");
            return null;
        }
        if (splitPoint == null) {
            Commit initialCom = getCurrBranchCommit();
            while (initialCom.getParent() != null) {
                initialCom = (Commit) Main.readFile(".commits",
                        initialCom.getParent());
            }
            splitPoint = initialCom;
        }
        return splitPoint;
    }

    /** HashMap representing the branches of the tree,
     * mapping branch names to its contents. */
    private HashMap<String, String> branches;
    /** String representing the current branch of the tree. */
    private String currBranch;
}
