# Gitlet Design Document

**Name**: Megan Hu

# Classes and Data Structures
## Tree

Represents the structure of the commits, keeping track of head and current commit.

**Fields**
1. `HashMap<String, String> branches`: Maps branch name to the commit the branch points to.
2. `String currentBranch`: holds the current branch
## Commit

Represents characteristics and behaviors of a commit.

**Fields**
1. `int time_stamp`: The time at which the commit was committed.
2. `String message`: The log message of the commit.
3. `String parent`: The SHAid of the parent commit of this commit.
4. `String _parent2`: The SHAid of the second parent of this commit, if one exists.
5. `HashMap<String, String> blobs`: A map connecting file names in this commit to their respective blobs.
6. `String id`: String representing the id of this commit.
## Blob

**Fields**
1. `Hashvalue`: Value representing the hashvalue of this blob. 
2. `Name`: the name of the file.
3. `Content`: the contents of the file.

# Algorithms
##Tree
`constructor`: constructs the staging area inside .gitlet and initializes head and curr variables.

`stage(File)`: serializes file and places it inside the staging area. If the file has already been staged, overwrite the
file with new contents. if the current working version of the file is identical to version in the current commit, remove
it from the staging area.

`unstage`: removes a file from the staging area.

`commit`: calls the constructor in commit, adds commit to the tree, moving necessary variables, and clears staging area.

`latest common ancestor`: finds the latest common ancestor of two commits.

`merge`: calls latest common ancestor. any files that have been modified in the given branch since the slit point, but 
not in the current branch are changed to their versions in the given branch, and then automatically staged. If 
merge conflicts exist, call `merge_conflicts`. If there are staged additions or removals present, print the error message
 You have uncommitted changes. and exit. If a branch with the given name does not exist, print the error message 
 A branch with that name does not exist. If attempting to merge a branch with itself, print the error message 
 Cannot merge a branch with itself.

 `rm`: removes the branch with the given name after finding it by calling `find`.
 
 `find`: finds the branch with the given name.
 
 `log`: starting at the current head commit, retrieve and display information about each commit backwards along the
 commit tree until the initial commit, by following the parent field of the commit.
 
 `global-log`: starting at the current head commit, retrieve and display information about each commit backwards along the
 commit tree until the initial commit. Looks at both parent and parent1 fields.
                
`branch`: creates a new branch with the given name, pointing it at the current head node. If a branch with the given name
already exists, print the error message A branch with that name already exists.

##Commit
`get`: get the hashvalue of the file associated with a file name

`constructor`: constructs a commit object, taking in the hashmap of the staging area, the current commit, and a log message.

               


## Persistence

In Main function, each time a command is entered, unserialize and retrieve tree, perform actions, and reserialize tree.
With all functions that change state of system in both Tree and Commit, first retrieve information, modify it, then change
the associated files.

Within .gitlet

-Commit tree file

-Commit folder to hold all commits

-Blob folder to hold all blobs

-Tree.java, Commit.java, Blob.java, Main.java
