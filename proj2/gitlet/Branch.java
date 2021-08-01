package gitlet;

import javax.swing.border.MatteBorder;
import java.io.File;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.*;

/***
 *
 * @author Hongfa You
 */
public class Branch implements Serializable {
    /** Name of Brunch such as "master" **/
    private String branchName;
    /** Indicates the Commit this Branch should point to, using a SHA1 string **/
    private String whichCommit;

    /**
     * Constructor of Branch
     * @param name Name of Brunch such as "master", "HEAD".
     * @param which Indicates the Commit this Branch should point to, using a SHA1 string
     */
    public Branch(String name, String which) {
        branchName = name;
        whichCommit = which;
    }


    /**
     * Saves this Branch for future use, and name of the Object
     * in the File System is branchName
     **/
    public void saveBranch() {
        File outfile = Utils.join(Repository.BRANCH_DIR,  this.branchName);
        writeObject(outfile, this);
    }

    /**
     *  Reads in and deserializes a branch from a file.
     * @param name the name of Branch
     * @return the Branch
     */
    public static Branch readBranchIn(String name) {
//        File cwd = new File(System.getProperty("user.dir"));
        File file = join(Repository.BRANCH_DIR, name);
        if (!file.exists()) {
            System.out.println("No such branch exists. ");
            System.exit(0);
        }
        Branch result = readObject(file, Branch.class);
        return result;
    }


    /** Return the Commit pointed by this Branch. */
    public String whichCommit() {
        return this.whichCommit;
    }

    /**
     *  Make this Branch point to another Commit named commit
     * @param commit
     */
    public void resetWhichCommit(String commit) {
        this.whichCommit = commit;
        this.saveBranch();
    }

    /**
     *  Check if Branch called name exists or not.
     * @param name
     * @return
     */
    public static Boolean isBranchExist(String name) {
        File file = join(Repository.BRANCH_DIR, ".gitlet", name);
        return file.exists();
    }

    public static void deleteBranch(String branchName) {
        HEAD.readHEAD();
        if (branchName.equals(HEAD.pointBranchName)) { //do not use ==
            Repository.abort("Cannot remove the current branch.");
        }
        List<String> branchList = Utils.plainFilenamesIn(Repository.BRANCH_DIR);
        if (branchList.contains(branchName)) {
            File file = Utils.join(Repository.BRANCH_DIR, branchName);
            file.delete();
        } else {
            Repository.abort("A branch with that name does not exist.");
        }
    }


}

