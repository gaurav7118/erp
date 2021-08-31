# ERP

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

###  Prerequisites

Things you need to install the software and how to install them
1. Execute the following command in terminal to see if Git is installed. If git is not installed, kindly contact administrator

```
git --version
```

###  Verify Profile and Change Password
1. Click on the avatar at the top-right
2. Click on Settings and navigate to Profile at the left menu-panel. Verify that the name and e-mail address has been set correctly.
3. Navigate to Password and change your password.

###  Setup Netbeans-Git

1. Open up Netbeans and select Team.
2. Select Git > Clone.
3. Fill in the required details : 

```
repository url = http://192.168.0.159:9877/root/Deskera_eLeave.git
user = <Username>
password = <Password>
```

4. Once the above information has been filled up, click on Next. Select the remote branch that you want to clone. You can select all remote branches if you are unsure.
For more information, please refer "Confluence"

5. Select the checkout branch for development. The branch that you would checkout depends on what type of issues are you working on. 
Please refer to "Confluence" to identify the branch to checkout. Once you have completed this step, select the option to open project after cloned.

## Create a new branch to work on an issue
For each issue that is reported, there should be a branch that is created.
Each branch that is created should be defined clearly whether it's a bugfix, hotfix or feature.
For example, if it is a bug reported in SDP-1001, create a branch named bugfix/SDP-1001 from develop branch.

To create a new branch

```
Team > Branch/Tag > Create branch > Select Revision (branch source) > Checkout created branch
```

## Add & Commit Code Changes in Netbeans

### Git Add
Select the file/files and git add the changes.

```
Team > Add or Right Click Project Folder > Git > Add
```

### Git Commit
Select the file/files and git commit the changes.

```
Team > Commit or Right Click Project Folder > Git > Add
```

**Please include ticket number in commit message**
Commit often and write proper commit messages.
Please refer https://chris.beams.io/posts/git-commit/ for more information

### Git Push
This will push changes to the remote repository.

```
Right Click project folder > Git > Remote > Push to Upstream
```

## Developer's Notes

###  Branching Guide

**Do not modify develop or master branch directly!**

Please follow the structure below to checkout appropriate branch and work accordingly.

| Branch        | Source           | Merge Into      | Remark |
|---------------|:----------------:| :--------------:|:-------|
| Master|||Stable Version. Production Build.|
| Develop|Master| Master/Release|Main Integration Branch.|
| Feature |Develop| Develop|Temporary branch for new feature development.|
| Release |Develop| Master/Develop|Preparation of new releases in master branch. Also marks a new release cycle |
| Hotfix |Master| Master/Develop|Only urgent/blocker issues that needs immediate action and deployment|

###  Additional scenario

### Working on multiple issues.
Each issues should be addressed in a separate branch.
If there are multiple tickets assigned, then it should be worked on its own branch.

For example, SDP-1234 (bug), SDP-2345 (bug) and SDP-5678 (feature) is assigned to Dev xF.

Assuming that xF has already clone the project in Netbeans previously, xF should pull from remote to update the local repository.

Next, xF should create a new branch bugfix/SDP-1234 from remote/develop and make the code changes, add and commit accordingly.

When xF wants to work on SDP-2345, xF should create another branch bugfix/SDP-2345 from remote/develop and work on this bug. Note that bugfix/SDP-2345 will not have any changes that xF has committed in his local bugfix/SDP-1234 branch.

Whenever xF starts working on the new feature, he will switch to the feature/SDP-5678 branch that is branch off from remote/repository as well.

### Updating local branches
Branches that were created locally for bugfix, new feature or etc. (that are still in use) should be updated regularly. 

Suppose bugfix/SDP-9990 branch off from Develop branch in the first week and it is still in progress up until the 3rd-4th week. 
During this period, the remote Develop branch already has several updates.

Developers should do a fetch and pull from remote Develop branch into bugfix/SDP-9990 to update the bugfix branch locally.

### Pushing Code to remote repository.
As each branch is created for a single issue, the branch should be pushed into the remote repository for further action (review and merge).

For example, if bugfix/SDP-1234 is completed, push and create bugfix/SDP-1234 on remote repository.

### Code Review & Merge Request
Once the branch has been pushed to remote repository, proceed to creating 2 items below : 

1. Crucible Branch Review Request
2. Gitlab Merge Request

### Crucible Branch Review Request
Create a review and select the branch to be reviewed.
Copy the Crucible ticket link and paste it in Gitlab Merge Request (Refer to the next section)

### Gitlab Merge Request
Create a merge request based on the branch structure that has been predefined.

If it is a bugfix or feature branch, the target branch should be develop.

Include "WIP:" in the title of the merge request, ticket number and brief description.
i.e. "WIP:SDP-1234 Optimize Code"

Under description, include the Crucible Ticket Review link for this branch.

The reviewer should check the merge request and review the branch accordingly. Once the code review process is completed, remove WIP: from the merge request ticket so that this request gets merged into the upcoming build.
"# erp" 
