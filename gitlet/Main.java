package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Megan Hu
 */
public class Main {

    /** Deletes file with the path sysDir/.gitlet/dir/filename.
     *
     * @param dir directory of the file
     * @param fileName name of the file
     */
    public static void deleteFile(String dir, String fileName) {
        if (dir != null && fileName != null) {
            Path path = Paths.get(sysDir, ".gitlet", dir).resolve(fileName);
            try {
                Files.deleteIfExists(path);
            } catch (NoSuchFileException e) {
                System.out.println("NoSuchFileException: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else if (dir.equals(null)) {
            System.out.println("Null directory.");
        } else {
            System.out.println("Null filename.");
        }
    }

    /** Returns ArrayList of Objects of files in the
     * given directory, sysDir/.gitlet/dir.
     * @param dir the given directory
     * @return arraylist of files in dir
     */
    public static ArrayList<Object> readFiles(String dir) {
        ArrayList<Object> objs = new ArrayList<>();
        if (dir == null) {
            System.out.println("Null directory.");
            return objs;
        }
        Path path = Paths.get(System.getProperty("user.dir"), ".gitlet", dir);

        File[] files = new File(path.toString()).listFiles();
        ArrayList<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
        for (String fileName : fileNames) {
            objs.add(readFile(dir, fileName));
        }
        return objs;
    }

    /** Reads file named fileName in dir, returning the object serialized
     * in the file.
     * @param dir given dir
     * @param fileName given file name
     * @return deserialized object serialized in sysDir/.gitlet/dir/fileName
     */
    public static Object readFile(String dir, String fileName) {
        Object obj = null;
        if (dir != null && fileName != null) {
            try {
                File f = Utils.join(sysDir, ".gitlet", dir, fileName);
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream(f));
                while (true) {
                    obj = in.readObject();
                    in.close();
                    break;
                }
            } catch (FileNotFoundException e) {
                System.out.println("File does not exist.");
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFoundException: " + e.getMessage());
            }

        }
        return obj;
    }

    /** Writes obj to fileName in file.
     * @param dir given dir
     * @param fileName given filename
     * @param obj object to be written to sysDir/.gitlet/dir/filename
     */
    public static void writeFile(String dir, String fileName, Object obj) {
        if (dir != null && fileName != null) {
            Path path = Paths.get(System.getProperty("user.dir"),
                    ".gitlet", dir);

            if (!Files.exists(path)) {
                File f = Utils.join(sysDir, ".gitlet", dir);
                f.mkdirs();
            }
            path = path.resolve(fileName);
            try {
                ObjectOutputStream objOut = new ObjectOutputStream(
                        new FileOutputStream(path.toString()));
                objOut.writeObject(obj);
                objOut.close();
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else if (fileName == null) {
            System.out.println("Null filename.");
        } else {
            System.out.println("Null directory.");
        }
    }

    /** Reads file in current working directory to a string.
     * @param dir given dir
     * @param fileName given fileName
     * @return contents of sysDir/dir/fileName as a string
     */
    public static String readCWDFileToString(String dir, String fileName) {
        if (dir != null && fileName != null) {
            File f = Utils.join(sysDir, dir, fileName);
            try {
                String ret = Utils.readContentsAsString(f);
                return ret;
            } catch (IllegalArgumentException e) {
                System.out.println("File does not exist.");
            }
        }
        return "";
    }

    /** Writes string content to fileName in dir in the current working
     * directory.
     * @param dir given dir
     * @param fileName given fileName
     * @param content content to be written, in the form of a string
     */
    public static void writeToCWD(String dir, String fileName, String content) {
        if (dir != null && fileName != null) {
            Path path = Paths.get(sysDir, dir);
            if (!Files.exists(path)) {
                new File(path.toString()).mkdirs();
            }
            path = path.resolve(fileName);
            File f = new File(path.toString());
            Utils.writeContents(f, content.getBytes());
        } else if (dir == null) {
            System.out.println("Null directory.");
        } else if (fileName.isEmpty()) {
            System.out.println("Null filename");
        }

    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .
     *  @param args arguments given*/
    public static void main(String... args) {
        if (Files.exists(Paths.get(System.getProperty("user.dir"),
                ".gitlet").resolve("tree.bin"))) {
            mainTree = (Tree) readFile("", "tree.bin");
            mainStage = (Stage) readFile("", "stage.bin");
        }
        runCommands(args);
        writeFile("", "tree.bin", mainTree);
        writeFile("", "stage.bin", mainStage);
    }

    /** Helper method to run commands.
     * @param args given*/
    public static void runCommands(String[] args) {
        switch (args[0]) {
        default:
            return;
        case "init":
            init();
            break;
        case "add":
            add(args[1]);
            break;
        case "commit":
            if (args[1].length() < 1 || args.length < 2) {
                System.out.println("Please enter a commit message.");
            }
            commit(args[1]);
            break;
        case "log":
            log();
            break;
        case "checkout":
            whichCheckout(args);
            break;
        case "rm":
            rm(args[1]);
            break;
        case "global-log":
            globalLog();
            break;
        case "find":
            find(args[1]);
            break;
        case "status":
            status();
            break;
        case "branch":
            branch(args[1]);
            break;
        case "rm-branch":
            rmBranch(args[1]);
            break;
        case "reset":
            reset(args[1]);
            break;
        case "merge":
            merge(args[1]);
            break;
        }

    }

    /** Helper method for choosing checkout method.
     * @param args args given
     */
    public static void whichCheckout(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            checkoutFile(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            checkoutCommit(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Helper method to run merge.
     *
     * @param branchName name of the branch to be merged
     */
    public static void merge(String branchName) {
        mainTree.merge(branchName, mainStage);
    }

    /** Helper method to run merge.
     *
     * @param commit id of the commit to be merged
     */
    public static void reset(String commit) {
        mainTree.reset(commit);
        mainStage.clear();
    }

    /** Helper method to run branch.
     *
     * @param branchName name of the branch
     */
    public static void branch(String branchName) {
        mainTree.branch(branchName);
    }

    /** Helper method to run rm-branch.
     *
     * @param branchName name of the branch
     */
    public static void rmBranch(String branchName) {
        mainTree.rmBranch(branchName);
    }

    /** Helper method to run status.
     *
     */
    public static void status() {
        System.out.println("=== Branches ===");
        System.out.println(mainTree.status());
        System.out.println(mainStage.status());
    }

    /** Helper method to run find.
     *
     * @param message message of the commit
     */
    public static void find(String message) {
        String ret = mainTree.find(message);
        if (ret.length() < 1) {
            System.out.println("Found no commit with that message.");
        } else {
            System.out.println(ret);
        }
    }

    /** Helper method to run global-log.
     *
     */
    public static void globalLog() {
        System.out.println(mainTree.globalLog());
    }

    /** Helper method to run rm.
     *
     * @param fileName name of the file
     */
    public static void rm(String fileName) {
        mainStage.rm(fileName, mainTree.getCurrBranchCommit());
    }

    /** Helper method to run add.
     *
     * @param fileName name of the file
     */
    public static void add(String fileName) {
        mainStage.add(fileName, mainTree.getCurrBranchCommit());
    }

    /** Helper method to run commit.
     *
     * @param mess the message associated with the commit
     */
    public static void commit(String mess) {
        if (mainStage.getStagedRemoved().isEmpty()
                && mainStage.getStagedAdded().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        mainTree.addCommit("", mess, mainStage.getStagedAdded(),
                mainStage.getStagedRemoved());
    }

    /** Helper method to run checkout for case 1, a file.
     *
     * @param fileName name of the file to be checked out
     */
    public static void checkoutFile(String fileName) {
        mainTree.checkoutFile(fileName);
    }

    /** Helper method to run checkout for case 2, a file in a commit.
     *
     * @param id id of the commit
     * @param fileName name of the file in the commit
     */
    public static void checkoutCommit(String id, String fileName) {
        mainTree.checkoutCommit(id, fileName);
    }

    /** Helper method to run checkout for case 3, a branch.
     *
     * @param branchName name of the branch
     */
    public static void checkoutBranch(String branchName) {
        mainTree.checkoutBranch(branchName);
    }

    /** Helper method to run log.
     *
     */
    public static void log() {
        System.out.println(mainTree.log());
    }

    /** Helper method to run init.
     *
     */
    public static void init() {
        makeInit();
        mainTree = new Tree();
        mainStage = new Stage();
        mainStage.setCurrentBlobs(mainTree.getCurrBranchCommit());
        writeFile("", "tree.bin", mainTree);
        writeFile("", "stage.bin", mainStage);
    }

    /** Initializes .gitlet and .commits, .blobs, and .stagedblobs
     * directories. */
    public static void makeInit() {
        Path mainPath = Paths.get(sysDir, ".gitlet");
        Path commitsPath = Paths.get(sysDir, ".gitlet", ".commits");
        Path blobsPath = Paths.get(sysDir, ".gitlet", ".blobs");
        Path tempBlobsPath = Paths.get(sysDir,
                ".gitlet", ".stagedblobs");
        if (!Files.exists(mainPath)) {
            new File(mainPath.toString()).mkdirs();
            new File(commitsPath.toString()).mkdirs();
            new File(blobsPath.toString()).mkdirs();
            new File(tempBlobsPath.toString()).mkdirs();
        } else {
            System.out.println("A gitlet version-control system already "
                    + "exists in the current directory.");
        }
    }

    /** Deletes file named fileName in dir in current working directory.
     *
     * @param dir name of the directory
     * @param fileName name of the file
     */
    public static void deleteCWDFile(String dir, String fileName) {
        Path path = Paths.get(sysDir, dir);
        path = path.resolve(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    /** Returns a list of the files inside directory dir in the
     * current working directory.
     * @param dir name of the directory
     */
    public static ArrayList<String> listDirFiles(String dir) {
        ArrayList<String> ret = new ArrayList<>();
        Path path = Paths.get(sysDir, dir);
        File[] files = new File(path.toString()).listFiles();
        for (File f: files) {
            ret.add(f.getName());
        }
        return ret;
    }

    /** Getter method for mainTree.
     *
     * @return mainTree
     */
    public static Tree mainTree() {
        return mainTree;
    }

    /** Getter method for mainStage.
     *
     * @return mainStage
     */
    public static Stage mainStage() {
        return mainStage;
    }

    /** Setter method for mainStage.
     *
     * @param stage the stage
     */
    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    /** Getter method for sysDir.
     *
     * @return sysDir
     */
    private static String sysDir() {
        return sysDir;
    }
    /** Tree object representing the commit tree structure. */
    private static Tree mainTree = null;

    /** Stage object representing the stage that holds blobs. */
    private static Stage mainStage = null;

    /** The current working directory. */
    private static String sysDir = System.getProperty("user.dir");

}
